package cn.lmao.cloudown.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import cn.lmao.cloudown.service.ThumbnailService;
import org.slf4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import cn.lmao.cloudown.excepiton.CustomException;
import cn.lmao.cloudown.model.entity.File;
import cn.lmao.cloudown.model.entity.File.FileStatus;
import cn.lmao.cloudown.model.entity.User;
import cn.lmao.cloudown.model.enums.ErrorOperationStatus;
import cn.lmao.cloudown.repository.FileRepository;
import cn.lmao.cloudown.repository.UserRepository;
import cn.lmao.cloudown.service.FileService;
import cn.lmao.cloudown.util.FileTypeChecker;
import cn.lmao.cloudown.util.FileUtil;
import cn.lmao.cloudown.util.LogUtil;
import lombok.RequiredArgsConstructor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final FileUtil fileUtil;
    private final Logger log = LogUtil.getLogger();
    private final ThumbnailService thumbnailService;

    @Override
    public void uploadFile(User user, String filename, MultipartFile file, int chunkIndex, int totalChunks)
            throws IOException, NoSuchAlgorithmException {

        log.debug("开始上传文件分片: {} [分片 {}/{}]", filename, chunkIndex + 1, totalChunks);

        try {
            if (totalChunks * file.getSize() + user.getUsedCapacity() > user.getTotalCapacity()) {
                throw new CustomException(ErrorOperationStatus.STORAGE_QUOTA_EXHAUSTED);
            }
            // 1. 参数校验
            validateUploadParams(file, filename, chunkIndex, totalChunks);

            // 2. 准备文件信息
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(filename));
            
            // 3. 保存分片文件
            saveChunkFile(user.getId(), fileName, file, chunkIndex, totalChunks);

            log.debug("文件分片上传成功: {} [分片 {}]", filename, chunkIndex + 1);

        } catch (Exception e) {
            log.error("文件分片上传失败: filename={}, chunk={}/{}, error={}",
                    filename, chunkIndex + 1, totalChunks, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void mergeFile(User user, String fileName, Long size, int chunksCount, String path)
            throws IOException, NoSuchAlgorithmException {

        log.info("开始合并文件: {} (共{}个分片, 大小:{}字节)", fileName, chunksCount, size);

        Path userDir = Path.of(fileUtil.getUserPath(user.getId()));
        Path tempDir = Path.of(fileUtil.getTempPath(user.getId()));
        Path tempMergedFile = userDir.resolve(fileName + ".tmp." + System.currentTimeMillis());
        Path finalFile = userDir.resolve(fileName);

        try {
            // 1. 验证所有分片完整性
            validateAllChunks(tempDir, fileName, chunksCount);

            // 2. 合并文件
            mergeChunksToFile(tempDir, tempMergedFile, fileName, chunksCount, size);

            // 3. 验证合并后文件大小
            long mergedSize = Files.size(tempMergedFile);
            if (mergedSize != size) {
                throw new IOException(String.format("文件大小不匹配，期望: %d, 实际: %d", size, mergedSize));
            }

            // 4. 原子性重命名
            Files.move(tempMergedFile, finalFile, StandardCopyOption.REPLACE_EXISTING);

            // 5. 保存文件记录
            saveFileEntity(user, fileName, finalFile, size, path);

            // 6. 更新用户存储使用量
            user.setUsedCapacity(user.getUsedCapacity() + size);
            userRepository.save(user);

            log.info("文件合并完成: {} (最终大小:{}字节)", fileName, mergedSize);

        } catch (Exception e) {
            // 清理临时文件
            try {
                Files.deleteIfExists(tempMergedFile);
            } catch (IOException cleanupEx) {
                log.warn("清理临时文件失败: {}", tempMergedFile, cleanupEx);
            }
            throw e;
        } finally {
            // 清理分片文件
            cleanTempChunksAsync(tempDir, fileName, chunksCount);
        }
    }

    // 私有辅助方法
    private void validateUploadParams(MultipartFile file, String filename, int chunkIndex, int totalChunks) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (filename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        if (chunkIndex < 0 || chunkIndex >= totalChunks) {
            throw new IllegalArgumentException(
                    String.format("无效的分片索引: %d, 总分片数: %d", chunkIndex, totalChunks));
        }
        if (totalChunks <= 0) {
            throw new IllegalArgumentException("分片总数必须大于0");
        }
    }

    private void saveChunkFile(Long userId, String fileName, MultipartFile file,
            int chunkIndex, int totalChunks) throws IOException {
        Path tempDir = Path.of(fileUtil.getTempPath(userId));
        String chunkFileName = String.format("%s.%d.%d.part", fileName, chunkIndex, totalChunks);
        Path chunkFile = tempDir.resolve(chunkFileName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, chunkFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void validateAllChunks(Path tempDir, String fileName, int chunksCount) throws IOException {
        for (int i = 0; i < chunksCount; i++) {
            String chunkFileName = String.format("%s.%d.%d.part", fileName, i, chunksCount);
            Path chunkFile = tempDir.resolve(chunkFileName);

            if (!Files.exists(chunkFile)) {
                throw new IOException("分片文件不存在: " + chunkFileName);
            }
            if (Files.size(chunkFile) == 0) {
                throw new IOException("分片文件为空: " + chunkFileName);
            }
        }
    }

    private void mergeChunksToFile(Path tempDir, Path outputFile, String fileName,
            int chunksCount, Long expectedSize) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(outputFile,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                BufferedOutputStream bufferedOut = new BufferedOutputStream(outputStream, 64 * 1024)) {

            long totalWritten = 0;
            byte[] buffer = new byte[64 * 1024]; // 64KB 缓冲区

            for (int i = 0; i < chunksCount; i++) {
                String chunkFileName = String.format("%s.%d.%d.part", fileName, i, chunksCount);
                Path chunkFile = tempDir.resolve(chunkFileName);

                try (InputStream inputStream = Files.newInputStream(chunkFile);
                        BufferedInputStream bufferedIn = new BufferedInputStream(inputStream, 64 * 1024)) {

                    int bytesRead;
                    while ((bytesRead = bufferedIn.read(buffer)) != -1) {
                        bufferedOut.write(buffer, 0, bytesRead);
                        totalWritten += bytesRead;
                    }
                }

                log.debug("已合并分片: {}/{} (已写入: {}字节)", i + 1, chunksCount, totalWritten);
            }

            bufferedOut.flush();
        }
    }

    // 异步清理临时文件
    private void cleanTempChunksAsync(Path tempDir, String fileName, int chunksCount) {
        CompletableFuture.runAsync(() -> {
            try {
                cleanTempChunks(tempDir, fileName, chunksCount);
            } catch (Exception e) {
                log.warn("清理临时分片文件失败: {}", e.getMessage());
            }
        });
    }

    private void saveFileEntity(User user, String fileName, Path filePath, Long size, String relativePath)
            throws IOException {
        String type = FileTypeChecker.getFileTypeDescription(fileName);

        File fileEntity = new File(fileName, filePath.toString(), null, type, user);
        fileEntity.setSize(size);
        fileEntity.setStatus(FileStatus.ACTIVE);
        fileEntity.setRelativePath(relativePath);
        fileEntity.setThumbnailPath(thumbnailService.generateThumbnail(filePath, user.getId()));

        // 暂时设置临时哈希，后续可异步计算真实哈希
        fileEntity.setHash(generateTempHash(fileName, size));

        fileRepository.save(fileEntity);
    }

    private String generateTempHash(String fileName, Long size) {
        return "temp_" + fileName.hashCode() + "_" + size;
    }

    @Override
    public String uploadAvatar(User user, MultipartFile file) throws IOException, NoSuchAlgorithmException {
        log.debug("开始上传用户头像: {}", user.getEmail());

        // 1. 参数校验
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传头像不能为空");
        }

        // 2. 准备文件信息
        String fileName = "avatar_" + user.getId() + UUID.randomUUID() + ".png";
        Path userDir = Paths.get(fileUtil.getUserAvatarPath(user.getId()));
        Path avatarFile = userDir.resolve(fileName);

        // 3. 保存头像文件
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, avatarFile, StandardCopyOption.REPLACE_EXISTING);
        }

        String avatarUrl = fileUtil.buildAvatarUrl(user.getId(), fileName);

        log.debug("已保存上传的用户头像: avatarUrl={}", avatarUrl);
        return avatarUrl;
    }

    private void cleanTempChunks(Path tempDir, String fileName, int chunksCount) {
        log.debug("正在异步删除临时文件");
        // 遍历分片数量
        for (int i = 0; i < chunksCount; i++) {
            try {
                // 构造分片文件名
                String chunkFileName = String.format("%s.%d.%d.part", fileName, i, chunksCount);
                // 构造分片文件路径
                Path chunkFile = tempDir.resolve(chunkFileName);
                // 删除分片文件
                if(!Files.deleteIfExists(chunkFile)){
                    log.warn("删除分片文件失败：{}", chunkFile.getFileName());
                }
            } catch (IOException e) {
                // 记录删除分片文件失败的信息
                log.warn("无法删除临时分片: {}", e.getMessage());
            }
        }
    }

    @Override
    public void deleteFile(User user, Long fileId) {
        log.debug("删除文件: {}", fileId);
        File file = fileRepository.findByUserAndId(user, fileId)
                .orElseThrow(() -> new CustomException(ErrorOperationStatus.FILE_NOT_FOUND));
        file.setStatus(FileStatus.DELETED);
        fileRepository.save(file);
        log.debug("删除文件成功");
    }

    @Override
    public List<File> getFileList(User user, String path, String category, String sort) {
        if (user == null) {
            log.error(ErrorOperationStatus.USER_NOT_FOUND.getMsg());
            throw new CustomException(ErrorOperationStatus.USER_NOT_FOUND);
        }

        log.info("获取文件列表: 用户ID={}, 路径={}, 分类={}, 排序={}", user.getId(), path, category, sort);

        // 获取用户所有文件
        List<File> allFiles = fileRepository.findByUser(user);
        log.info("用户总文件数: {}", allFiles.size());

        List<File> filteredFiles = new ArrayList<>();

        filteredFiles = allFiles.stream()
                .filter(file -> {
                    if (file.getType().equals("文件夹")) {
                        // 文件夹只在 "我的文件" 分类下显示
                        return "my-files".equals(category) && file.getRelativePath().equals(path);
                    }
                    String type = FileTypeChecker.getFileCategory(FileTypeChecker.getFileTypeFromName(file.getName()));
                    switch (category) {
                        case "my-files":
                            return getFile(file.getId()).getRelativePath().equals(path) && file.getStatus() == FileStatus.ACTIVE;
                        case "trash":
                            return file.getStatus() == FileStatus.DELETED;
                        default:
                            return category.equals(type);
                    }
                })
                .peek(file -> log.debug("文件匹配: {}", file.getName()))
                .collect(Collectors.toList());
        log.info("过滤后文件数: {}", filteredFiles.size());
        // 根据排序参数对文件列表进行排序
        sortFiles(filteredFiles, sort);
        return filteredFiles;
    }

    @Override
    public void createFolder(User user, String folderName, String path) throws IOException {
        log.debug("创建文件夹: {}", folderName);
        if (fileRepository.findByName(folderName) != null) {
            throw new CustomException(ErrorOperationStatus.FILE_EXISTS);
        }
        File file = new File(folderName, fileUtil.getUserPath(user.getId()) + "/" + folderName, path, "文件夹",
                user);
        fileRepository.save(file);
        log.debug("创建文件夹成功");
    }

    @Override
    public void createFile(User user, String fileName, String path, String content) throws IOException {
        log.debug("创建文本文件: {}", fileName);
        File file = new File(fileName, fileUtil.getUserPath(user.getId()) + "/" + fileName, path, "文本文件", user);
        fileRepository.save(file);
        log.debug("创建文本文件成功");
    }

    @Override
    public File getFile(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorOperationStatus.FILE_NOT_FOUND));
    }

    @Override
    public Resource downloadFile(User user, Long fileId, String fileName) throws IOException {
        log.debug("开始下载文件: filename={}, userId={}", fileName, user.getId());
        
        try {
            // 在数据库中查找文件
            File fileEntity = fileRepository.findByUserAndId(user, fileId)
                .orElseThrow(() -> new CustomException(ErrorOperationStatus.FILE_NOT_FOUND));

            // 检查文件类型
            if ("文件夹".equals(fileEntity.getType())) {
                throw new CustomException(ErrorOperationStatus.FILE_DOWNLOAD_FAIL);
            }

            // 获取文件的物理路径
            Path filePath = Path.of(fileEntity.getPath());
            
            // 检查文件是否存在
            if (!Files.exists(filePath)) {
                throw new CustomException(ErrorOperationStatus.FILE_NOT_FOUND);
            }

            // 创建文件资源
            Resource resource = new FileSystemResource(filePath.toFile());
            
            if (!resource.exists()) {
                throw new CustomException(ErrorOperationStatus.FILE_NOT_FOUND);
            }

            log.debug("文件下载准备完成: {}", fileEntity.getName());
            return resource;

        } catch (Exception e) {
            log.error("文件下载失败: filename={}, error={}", fileName, e.getMessage(), e);
            throw new CustomException(ErrorOperationStatus.FILE_DOWNLOAD_FAIL);
        }
    }

    @Override
    public Boolean updateFileName(Long fileId, String newFileName) {
        log.debug("更新文件名: 文件ID={}, 新文件名={}", fileId, newFileName);
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorOperationStatus.FILE_NOT_FOUND));

        // 检查新文件名是否有效
        if (!StringUtils.hasText(newFileName)) {
            throw new CustomException(ErrorOperationStatus.EMPTY_FILE);
        }

        // 更新文件名
        file.setName(newFileName);
        fileRepository.save(file);
        log.debug("更新文件名成功: {}", newFileName);
        return true;
    }

    /**
     * 根据排序参数对文件列表进行排序
     *
     * @param files 文件列表
     * @param sort  排序参数
     */
    private void sortFiles(List<File> files, String sort) {
        if (sort == null || sort.isEmpty()) {
            return;
        }

        switch (sort) {
            case "name-asc":
                files.sort(Comparator.comparing(File::getName));
                break;
            case "name-desc":
                files.sort(Comparator.comparing(File::getName).reversed());
                break;
            case "time-asc":
                files.sort(Comparator.comparing(File::getCreateTime));
                break;
            case "time-desc":
                files.sort(Comparator.comparing(File::getCreateTime).reversed());
                break;
            case "size-asc":
                files.sort(Comparator.comparing(File::getSize));
                break;
            case "size-desc":
                files.sort(Comparator.comparing(File::getSize).reversed());
                break;
            default:
                // 默认按创建时间降序排序
                files.sort(Comparator.comparing(File::getCreateTime).reversed());
                break;
        }
    }

}
