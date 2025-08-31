package cn.lmao.cloudown.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FileUtil {

    @Value("${file.upload.path}")
    private String FILE_PATH = "D:/Cloudown/upload";

    @Value("${file.temp.path}")
    private String TEMP_PATH = "D:/Cloudown/temp";

    // 使用配置 + UriComponentsBuilder（推荐）
    @Value("${app.server.base-url}")
    private String serverBaseUrl;

    private MultipartProperties multipartProperties;

    private static final Logger log = LogUtil.getLogger();

    /**
     * 获取用户路径
     * 
     * @param userId 用户ID
     * @return 用户路径
     * @throws IOException
     */
    public String getUserPath(Long userId) throws IOException {
        return createDirectory(FILE_PATH + "/" + userId);
    }

    /**
     * 获取临时路径
     * 
     * @param userId 用户ID
     * @return 临时路径
     * @throws IOException
     */
    public String getTempPath(Long userId) throws IOException {
        return createDirectory(TEMP_PATH + "/" + userId);
    }

    /**
     * 获取用户头像路径
     * 
     * @param userId 用户ID
     * @return 用户头像路径
     */
    public String getUserAvatarPath(Long userId) throws IOException {
        return createDirectory(FILE_PATH + File.separator + "avatar" + File.separator + userId);
    }

    /**
     * 获取用户文件缩略图
     *
     * @param userId 用户ID
     * @return 用户文件缩略图
     */
    public String getThumbPath(Long userId) throws IOException {
        return createDirectory(FILE_PATH + "/thumb/" + userId);
    }

    /**
     * 构建用户头像的URL
     * 
     * @param userId   用户ID
     * @param fileName 文件名
     * @return 完整的头像URL
     */
    public String buildAvatarUrl(Long userId, String fileName) {
        return UriComponentsBuilder.fromUriString(getLocalIpAddress())
                .path("/avatar/{userId}/{fileName}")
                .buildAndExpand(userId, fileName)
                .toUriString();
    }

    /**
     * 构建缩略图的URL
     *
     * @param userId   用户ID
     * @param fileName 文件名
     * @return 完整的缩略图URL
     */
    public String buildThumbUrl(Long userId, String fileName) {
        return UriComponentsBuilder.fromUriString(getLocalIpAddress())
                .path("/thumb/{userId}/{fileName}")
                .buildAndExpand(userId, fileName)
                .toUriString();
    }

    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // 跳过回环接口、虚拟接口或未启用的接口
                if (iface.isLoopback() || !iface.isUp() || iface.isVirtual()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // 过滤 IPv6 地址，只返回 IPv4
                    if (!addr.isLoopbackAddress() && addr.getHostAddress().indexOf(':') == -1) {
                        return "http://" + addr.getHostAddress() + ":8080";
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取本地ip地址出现问题：", e);
        }
        return "http://127.0.0.1:8080"; // 默认回退到本地回环地址
    }

    public long getMaxFileSizeInBytes() {
        return multipartProperties.getMaxFileSize().toBytes();
    }

    public long getMaxRequestSizeInBytes() {
        return multipartProperties.getMaxRequestSize().toBytes();
    }

    // public static File convert(MultipartFile multipartFile) throws IOException {
    // // 创建临时文件
    // File file = File.createTempFile("upload-",
    // multipartFile.getOriginalFilename());

    // // 将内容传输到文件
    // multipartFile.transferTo(file);

    // return file;
    // }

    public static String calculateFileHash(File file)
            throws IOException, NoSuchAlgorithmException {

        // 创建MessageDigest实例
        MessageDigest digest = MessageDigest.getInstance("MD5");

        // 使用try-with-resources确保流关闭
        try (FileInputStream fis = new FileInputStream(file)) {
            // 创建缓冲区
            byte[] buffer = new byte[8192]; // 8KB缓冲区
            int bytesRead;

            // 读取文件并更新哈希计算
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }

        // 完成哈希计算并转换为十六进制字符串
        return bytesToHex(digest.digest());
    }

    /**
     * 将字节数组转换为十六进制字符串
     * 
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private void ensurePathExists(String dir) throws IOException {
        Path path = Path.of(dir);
        if (Files.exists(path)) {
            if (Files.isDirectory(path)) {
                log.debug("目录已存在");
            } else {
                log.debug("路径已存在但不是目录: {}", path);
            }
        } else {
            // 创建目录（包括所有不存在的父目录）
            Files.createDirectories(path);
            log.debug("目录已创建: {}", path);
        }
    }

    private static final Object TEMP_DIR_LOCK = new Object(); // 类级锁

    private String createDirectory(String dir) throws IOException {
        try {
            // 创建目录
            synchronized (TEMP_DIR_LOCK) {
                ensurePathExists(dir);
                // 测试写入权限
                Path testFile = Path.of(dir).resolve(".permission_test");
                // 写入测试文件
                Files.write(testFile, "test".getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.DELETE_ON_CLOSE);
                return dir;
            }
        } catch (IOException e) {
            // 抛出异常
            throw new IOException("目录不可写: " + dir +
                    "。请检查应用程序是否有足够权限", e);
        }
    }

}
