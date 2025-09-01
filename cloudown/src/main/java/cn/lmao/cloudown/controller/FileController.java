package cn.lmao.cloudown.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cn.lmao.cloudown.excepiton.CustomException;
import cn.lmao.cloudown.model.dto.ApiResponse;
import cn.lmao.cloudown.model.entity.File;
import cn.lmao.cloudown.model.entity.User;
import cn.lmao.cloudown.model.enums.ErrorOperationStatus;
import cn.lmao.cloudown.service.FileService;
import cn.lmao.cloudown.service.UserService;
import cn.lmao.cloudown.util.LogUtil;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/file")
public class FileController {

    private final UserService userService;
    private final FileService fileService;
    private final Logger log = LogUtil.getLogger();

    /**
     * 获取文件列表
     *
     * @param path 文件路径，默认为根目录
     * @param sort 排序方式，默认按名称升序
     * @return 文件列表
     */
    @GetMapping("/list")
    public ApiResponse<List<File>> list(
            @RequestParam(value = "path", defaultValue = "my-files/") String path,
            @RequestParam(value = "category", defaultValue = "my-files") String category,
            @RequestParam(value = "sort", defaultValue = "time-desc") String sort) {
        User user = getUserFromToken();

        log.info("接收到获取文件列表请求: path={}, category={} sort={}, email={}", path, category, sort, user.getNickname());

        List<File> files = fileService.getFileList(user, path, category, sort);

        log.info("文件列表获取成功: path={}, fileCount={}, email={}", path, files.size(), user.getEmail());
        return ApiResponse.success(files);
    }

    @GetMapping("/check-upload")
    public ApiResponse<Map<String, Object>> checkUpload(@RequestParam(value = "quickHash") String quickHash) {
        // 1. 创建Map对象（推荐使用HashMap）
        Map<String, Object> map = new HashMap<>();
        map.put("completed", 0);
        map.put("uploadedChunks", new int[0]);
        return ApiResponse.success(map);
    }

    @PostMapping("/upload")
    public ApiResponse<String> upload(
            @RequestHeader("Authorization") String token,
            @RequestParam("filename") String filename,
            @RequestParam("file") MultipartFile file,
            @RequestParam("index") int chunkIndex,
            @RequestParam("totalChunks") int totalChunks) {

        long startTime = System.currentTimeMillis();
        String threadName = Thread.currentThread().getName();

        try {
            // 参数验证
            if (StringUtils.isBlank(filename)) {
                return ApiResponse.exception(ErrorOperationStatus.INVALID_FILENAME);
            }

            User user = getUserFromToken();
            log.info("用户: {} 开始上传文件分片: {} [{}/{}] (线程: {})",
                    user.getNickname(), filename, chunkIndex + 1, totalChunks, threadName);

            fileService.uploadFile(user, filename, file, chunkIndex, totalChunks);

            long duration = System.currentTimeMillis() - startTime;
            log.info("用户: {} 上传分片成功: {} [{}/{}] 耗时: {}ms (线程: {})",
                    user.getNickname(), filename, chunkIndex + 1, totalChunks, duration, threadName);

            return ApiResponse.success("分片上传成功");

        } catch (IllegalArgumentException e) {
            log.warn("参数错误: {}", e.getMessage());
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("上传文件分片失败: filename={}, chunk={}/{}, error={}",
                    filename, chunkIndex + 1, totalChunks, e.getMessage(), e);
            return ApiResponse.exception(ErrorOperationStatus.FILE_UPLOAD_FAILED);
        }
    }

    @PostMapping("/merge")
    public ApiResponse<String> merge(
            @RequestHeader("Authorization") String token,
            @RequestParam("filename") String filename,
            @RequestParam("size") Long size,
            @RequestParam("chunksCount") int chunksCount,
            @RequestParam("path") String path) {

        long startTime = System.currentTimeMillis();
        log.info("收到文件合并请求: filename={}, size={}字节, chunksCount={}, path={}",
                filename, size, chunksCount, path);

        try {
            // 参数验证
            if (chunksCount <= 0) {
                return ApiResponse.error(400, "分片数量必须大于0");
            }
            if (size <= 0) {
                return ApiResponse.error(400, "文件大小必须大于0");
            }

            User user = getUserFromToken();
            fileService.mergeFile(user, filename, size, chunksCount, path);

            long duration = System.currentTimeMillis() - startTime;
            log.info("文件合并成功: {} (耗时: {}ms)", filename, duration);

            return ApiResponse.success("文件合并成功！");

        } catch (Exception e) {
            log.error("合并文件失败: filename={}, error={}", filename, e.getMessage(), e);
            return ApiResponse.exception(ErrorOperationStatus.FILE_MERGE_FAILED);
        }
    }

    @PostMapping("/uploadAvatar")
    public ApiResponse<User> uploadAvatar(@RequestHeader("Authorization") String token,
            @RequestParam("avatar") MultipartFile file) {
        User user = getUserFromToken();
        log.info("用户 {} 请求上传头像", user.getEmail());
        try {
            if (file.isEmpty()) {
                log.error("上传头像失败，文件为空");
                return ApiResponse.exception(ErrorOperationStatus.FILE_UPLOAD_FAILED);
            }
            user.setAvatarUrl(fileService.uploadAvatar(user, file));
            userService.updateUserInfo(user);
            log.info("用户 {} 头像上传成功", user.getEmail());
            return ApiResponse.success(user);
        } catch (Exception e) {
            log.error("上传头像失败: {}", e.getMessage());
            return ApiResponse.exception(ErrorOperationStatus.FILE_UPLOAD_FAILED);
        }
    }

    @PostMapping("/createFolder")
    public ApiResponse<String> createFolder(
            @RequestParam String folderName,
            @RequestParam String path) {
        
        try {
            // 参数验证
            if (StringUtils.isBlank(folderName)) {
                return ApiResponse.exception(ErrorOperationStatus.INVALID_FILENAME);
            }
            if (StringUtils.isBlank(path)) {
                return ApiResponse.exception(ErrorOperationStatus.INVALID_PATH);
            }

            User user = getUserFromToken();
            log.info("用户: {} 请求创建文件夹: {}, 路径: {}", user.getNickname(), folderName, path);

            // 调用服务层创建文件夹
            fileService.createFolder(user, folderName, path);
            
            log.info("用户: {} 创建文件夹成功: {}", user.getNickname(), folderName);
            return ApiResponse.success("文件夹创建成功！");
        } catch (CustomException e){
            log.warn("创建文件夹参数错误: {}", e.getMessage());
            return ApiResponse.exception(e);
        } catch (IllegalArgumentException e) {
            log.warn("创建文件夹参数错误: {}", e.getMessage());
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("创建文件夹失败: folderName={}, path={}, error={}", 
                    folderName, path, e.getMessage(), e);
            return ApiResponse.exception(ErrorOperationStatus.FOLDER_CREATE_FAILED);
        }
    }

    @PostMapping("/createFile")
    public ApiResponse<String> createFile(
            @RequestParam String fileName,
            @RequestParam String path,
            @RequestParam(required = false) String content) {
        
        try {
            // 参数验证
            if (StringUtils.isBlank(fileName)) {
                return ApiResponse.exception(ErrorOperationStatus.INVALID_FILENAME);
            }
            if (StringUtils.isBlank(path)) {
                return ApiResponse.exception(ErrorOperationStatus.INVALID_PATH);
            }

            User user = getUserFromToken();
            log.info("用户: {} 请求创建文件: {}, 路径: {}", user.getNickname(), fileName, path);

            // 调用服务层创建文件
            fileService.createFile(user, fileName, path, content);
            
            log.info("用户: {} 创建文件成功: {}", user.getNickname(), fileName);
            return ApiResponse.success("文件创建成功！");
            
        } catch (IllegalArgumentException e) {
            log.warn("创建文件参数错误: {}", e.getMessage());
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("创建文件失败: fileName={}, path={}, error={}", 
                    fileName, path, e.getMessage(), e);
            return ApiResponse.exception(ErrorOperationStatus.FILE_CREATE_FAILED);
        }
    }

    private User getUserFromToken() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserInfo(email);
    }
}
