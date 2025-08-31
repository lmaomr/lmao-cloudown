package cn.lmao.cloudown.service;

import cn.lmao.cloudown.util.FileUtil;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.io.FilenameUtils;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ThumbnailService {

    @Autowired
    private FileUtil fileUtil;

    private static final int THUMB_WIDTH = 400;
    private static final int THUMB_HEIGHT = 400;

    /**
     * 为上传的文件生成缩略图
     * 
     * @param file 上传的文件
     * @return 缩略图路径 (相对于uploadPath)
     */
    public String generateThumbnail(Path file, Long id) throws IOException {
        // 获取上传文件的原始文件名
        String originalFilename = String.valueOf(file.getFileName());
        // 获取上传文件的文件扩展名
        String fileExtension = null;
        if (FilenameUtils.getExtension(originalFilename) != null) {
            fileExtension = FilenameUtils.getExtension(originalFilename).toLowerCase();
        }

        // 生成缩略图文件名
        String thumbFilename = "thumb_" + UUID.randomUUID() + ".jpg";

        // 生成缩略图路径
        String thumbPath = fileUtil.getThumbPath(id) + "/" + thumbFilename;

        // 根据文件类型处理
        switch (fileExtension) {
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
                // 普通图片处理
                generateImageThumbnail(Files.readAllBytes(file), thumbPath);
                break;
            case "mp4":
            case "mov":
            case "avi":
            case "mkv":
                // 视频文件处理
                generateVideoThumbnail(Files.readAllBytes(file), thumbPath);
                break;
            case "pdf":
                // PDF第一页转缩略图
                generatePdfThumbnail(Files.readAllBytes(file), thumbPath);
                break;
            case "doc":
            case "docx":
                // Word文档转缩略图
                generateDocThumbnail(Files.readAllBytes(file), thumbPath);
                break;
            default:
                return "";
        }

        // 返回缩略图路径
        return fileUtil.buildThumbUrl(id, thumbFilename);
    }

    // 生成缩略图
    private void generateImageThumbnail(byte[] imageBytes, String outputPath) throws IOException {
        // 从字节数组中读取图片
        Thumbnails.of(new ByteArrayInputStream(imageBytes))
                // 设置缩略图大小
                .size(THUMB_WIDTH, THUMB_HEIGHT)
                // 设置输出格式为jpg
                .outputFormat("jpg")
                // 保存缩略图到指定路径
                .toFile(new File(outputPath));
    }

    private void generateVideoThumbnail(byte[] videoBytes, String outputPath) throws IOException {
        Path tempVideoFile = Files.createTempFile("video_", ".mp4");
        Files.write(tempVideoFile, videoBytes);

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-ss", "00:00:01",
                    "-i", tempVideoFile.toString(),
                    "-vframes", "1",
                    "-an",
                    "-vf", "scale=" + THUMB_WIDTH + ":-1",  // 固定宽度，高度自动保持比例
                    "-qscale:v", "2",
                    "-y",
                    outputPath);
            // 打印执行的命令（调试用）
            System.out.println("Executing: " + String.join(" ", pb.command()));

            Process process = pb.start();

            // 捕获错误输出
            String errorOutput;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                errorOutput = reader.lines().collect(Collectors.joining("\n"));
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("FFmpeg failed with exit code " + exitCode + "\nError output:\n" + errorOutput);
            }

            // 验证输出文件
            if (!Files.exists(Paths.get(outputPath))) {
                throw new IOException("Output file was not created: " + outputPath);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Operation interrupted", e);
        } finally {
            Files.deleteIfExists(tempVideoFile);
        }
    }

    private void generatePdfThumbnail(byte[] pdfBytes, String outputPath) throws IOException {
        // 读取PDF文件
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
            // 创建PDF渲染器
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(0, 150); // 第一页，150 DPI
            Thumbnails.of(image)
                    .size(THUMB_WIDTH, THUMB_HEIGHT)
                    .outputFormat("jpg")
                    .toFile(new File(outputPath));
        }
    }

    private void generateDocThumbnail(byte[] docBytes, String outputPath) throws IOException {
        // 实际项目中可以使用Apache POI或第三方库处理
        // 这里简化为使用默认图标
        // copyDefaultThumbnail("doc", outputPath);
    }

}
