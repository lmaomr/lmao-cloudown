package cn.lmao.cloudown.model.enums;

import lombok.Getter;

@Getter
public enum FileOperationStatus {
    // 成功状态
    SUCCESS(200, "成功"),

    // 客户端错误
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    
    // 服务端错误
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    // 业务扩展错误（6xxxx）
    FILE_UPLOAD_FAILED(60001, "文件上传失败"),
    FILE_TYPE_INVALID(60002, "文件类型不支持"),
    
    // 文件操作状态
    FILE_EXISTS(60003, "文件已存在"),
    FILE_NOT_FOUND(60004, "文件不存在"),
    FILE_SIZE_TOO_LARGE(60005, "文件大小超过限制"),
    FILE_TYPE_NOT_SUPPORTED(60006, "文件类型不支持"),
    FILE_DOWNLOAD_FAILED(60007, "文件下载失败"),

    FILE_UPLOAD_SUCCESS(60008, "文件上传成功"),
    FILE_CREATE_SUCCESS(60009, "文件创建成功"),
    FILE_DOWNLOAD_SUCCESS(60010, "文件下载成功"),
    FILE_DELETE_SUCCESS(60011, "文件删除成功");

    private final int code;
    private final String msg;

    FileOperationStatus(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
