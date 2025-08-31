package cn.lmao.cloudown.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import cn.lmao.cloudown.model.entity.File;
import cn.lmao.cloudown.model.entity.User;

public interface FileService {

    // 上传文件
    void uploadFile(User user, String filename, MultipartFile file, int chunkIndex, int totalChunks) throws IOException, NoSuchAlgorithmException;

    // 合并文件
    void mergeFile(User user, String fileName, Long size, int chunksCount, String path) throws IOException, NoSuchAlgorithmException;

    // 下载文件
    File downloadFile(Long fileId);

    // 删除文件
    void deleteFile(Long fileId);

    // 获取文件列表
    List<File> getFileList(User user, String path, String category, String sort);

    //创建文件夹
    void createFolder(String folderName, String relativePath, User user) throws IOException;

    //创建文本文件
    void createTextFile(String fileName, String relativePath, User user) throws IOException;

    //获取文件信息
    File getFile(Long fileId);

    String uploadAvatar(User user, MultipartFile file) throws IOException, NoSuchAlgorithmException;

    Boolean updateFileName(Long fileId, String newFileName);
}

