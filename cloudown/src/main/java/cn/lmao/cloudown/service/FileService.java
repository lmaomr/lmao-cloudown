package cn.lmao.cloudown.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import cn.lmao.cloudown.model.entity.File;
import cn.lmao.cloudown.model.entity.User;

public interface FileService {

    // 上传文件
    void uploadFile(User user, String filename, MultipartFile file, int chunkIndex, int totalChunks) throws IOException, NoSuchAlgorithmException;

    // 合并文件
    void mergeFile(User user, String fileName, Long size, int chunksCount, String path) throws IOException, NoSuchAlgorithmException;

    // 下载文件
    Resource downloadFile(User user, Long fileId, String fileName) throws IOException;

    // 创建文件夹
    void createFolder(User user, String folderName, String path) throws IOException;

    // 创建文件
    void createFile(User user, String fileName, String path, String content) throws IOException;

    // 删除文件
    void deleteFile(User user, Long fileId);

    // 获取文件列表
    List<File> getFileList(User user, String path, String category, String sort);

    //获取文件信息
    File getFile(Long fileId);

    String uploadAvatar(User user, MultipartFile file) throws IOException, NoSuchAlgorithmException;

    Boolean updateFileName(Long fileId, String newFileName);

    List<File> searchFiles(User user, String searchQuery);
}

