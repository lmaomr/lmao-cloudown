package cn.lmao.cloudown.util;

import java.util.HashMap;
import java.util.Map;

public class FileTypeChecker {

    // 文件类型枚举
    public enum FileType {
        IMAGE, DOCUMENT, VIDEO, AUDIO, ARCHIVE, CODE, EXECUTABLE, OTHER
    }

    // 后缀名到文件类型的映射
    private static final Map<String, FileType> FILE_TYPES = new HashMap<>();

    static {
        // 图片类型
        addType("jpg", FileType.IMAGE);
        addType("jpeg", FileType.IMAGE);
        addType("png", FileType.IMAGE);
        addType("gif", FileType.IMAGE);
        addType("bmp", FileType.IMAGE);
        addType("svg", FileType.IMAGE);
        addType("webp", FileType.IMAGE);

        // 文档类型
        addType("pdf", FileType.DOCUMENT);
        addType("doc", FileType.DOCUMENT);
        addType("docx", FileType.DOCUMENT);
        addType("xls", FileType.DOCUMENT);
        addType("xlsx", FileType.DOCUMENT);
        addType("ppt", FileType.DOCUMENT);
        addType("pptx", FileType.DOCUMENT);
        addType("txt", FileType.DOCUMENT);

        // 视频类型
        addType("mp4", FileType.VIDEO);
        addType("avi", FileType.VIDEO);
        addType("mov", FileType.VIDEO);
        addType("mkv", FileType.VIDEO);
        addType("webm", FileType.VIDEO);

        // 音频类型
        addType("mp3", FileType.AUDIO);
        addType("wav", FileType.AUDIO);
        addType("flac", FileType.AUDIO);

        // 压缩文件
        addType("zip", FileType.ARCHIVE);
        addType("rar", FileType.ARCHIVE);
        addType("7z", FileType.ARCHIVE);
        addType("tar", FileType.ARCHIVE);
        addType("gz", FileType.ARCHIVE);

        // 代码文件
        addType("java", FileType.CODE);
        addType("py", FileType.CODE);
        addType("js", FileType.CODE);
        addType("html", FileType.CODE);
        addType("css", FileType.CODE);
        addType("xml", FileType.CODE);
        addType("json", FileType.CODE);

        // 可执行文件
        addType("exe", FileType.EXECUTABLE);
        addType("msi", FileType.EXECUTABLE);
        addType("bat", FileType.EXECUTABLE);
        addType("sh", FileType.EXECUTABLE);
    }

    private static void addType(String extension, FileType type) {
        FILE_TYPES.put(extension.toLowerCase(), type);
    }

    /**
     * 根据文件后缀名判断文件类型
     * 
     * @param extension 文件后缀名（带或不带点都可以）
     * @return 文件类型枚举
     */
    public static FileType getFileType(String extension) {
        if (extension == null || extension.isEmpty()) {
            return FileType.OTHER;
        }

        // 统一处理：移除可能存在的点号并转为小写
        String ext = extension.replaceAll("^\\.", "").toLowerCase();

        return FILE_TYPES.getOrDefault(ext, FileType.OTHER);
    }

    /**
     * 根据文件后缀名获取中文类型描述
     * 
     * @param extension 文件后缀名
     * @return 类型中文描述
     */
    public static String getFileTypeDescription(String extension) {
        FileType type = getFileTypeFromName(extension);

        return switch (type) {
            case IMAGE -> "图片文件";
            case DOCUMENT -> "文档文件";
            case VIDEO -> "视频文件";
            case AUDIO -> "音频文件";
            case ARCHIVE -> "压缩文件";
            case CODE -> "代码文件";
            case EXECUTABLE -> "可执行文件";
            default -> "其他文件";
        };
    }

    /**
     * 根据文件名获取文件类型
     * 
     * @param fileName 完整文件名
     * @return 文件类型枚举
     */
    public static FileType getFileTypeFromName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return FileType.OTHER;
        }

        // 获取文件后缀
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            String extension = fileName.substring(dotIndex + 1);
            return getFileType(extension);
        }

        return FileType.OTHER;
    }

    /**
     * 根据文件类型获取文件分类
     */
    public static String getFileCategory(FileType type) {
        switch (type) {
            case IMAGE:
                return "images";
            case DOCUMENT, CODE:
                return "documents";
            case VIDEO:
                return "videos";
            case AUDIO:
                return "music";
            default:
                return "others";
        }
    }

    public static void main(String[] args) {
        // 测试文件类型检查
        String[] testFiles = {
                "D:\\Cloud\\upload\\1\\Windows2.1.7.zip",
                "example.jpg",
                "document.pdf",
                "video.mp4",
                "audio.mp3",
                "archive.zip",
                "script.js",
                "executable.exe",
                "unknown.xyz"
        };

        for (String file : testFiles) {
            FileType type = getFileTypeFromName(file);
            System.out.println("文件: " + file + ", 类型: " + type + ", 描述: " + getFileTypeDescription(file));
        }

        // 测试文件分类
        for (FileType type : FileType.values()) {
            String category = getFileCategory(type);
            System.out.println("文件类型: " + type + ", 分类: " + category);
        }
    }
}