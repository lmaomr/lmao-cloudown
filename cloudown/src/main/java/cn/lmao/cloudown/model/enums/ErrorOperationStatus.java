package cn.lmao.cloudown.model.enums;

import lombok.Getter;

@Getter
public enum ErrorOperationStatus {
    /* ========== 基础认证异常 (4xx) ========== */
    AUTH_FAIL(401, "认证失败"),
    BAD_CREDENTIALS(401, "邮箱或密码错误"),
    ACCOUNT_LOCKED(401, "账号已被锁定"),
    PASSWORD_ERROR(401, "旧密码错误"),
    PASSWORD_NOT_MATCH(401, "新密码与旧密码相同"),
    PASSWORD_CHANGE_FAILED(401, "密码修改失败"),
    TOKEN_EXPIRED(401, "令牌已过期"),
    TOKEN_INVALID(401, "无效的令牌"),
    TOKEN_FORMAT_ERROR(401, "无效的Authorization头格式"),
    ACCESS_DENIED(403, "无权访问该资源"),

    /* ========== 请求参数异常 (4xx) ========== */
    PARAM_REQUIRED(400, "缺少必要参数"),
    INVALID_PATH(400, "无效的路径"),
    
    /* ========== 文件操作异常 ========== */
    FILE_CREATE_FAILED(500, "文件创建失败"),
    FOLDER_CREATE_FAILED(500, "文件夹创建失败"),
    PARAM_INVALID(400, "参数格式错误"),
    PARAM_ERROR(400, "参数错误"),
    PARAM_TYPE_NOT_FOUND(400, "不支持该请求类型"),
    PARAM_METHOD_NOT_FOUND(400, "不支持该请求方法"),
    PARAM_TYPE_MISMATCH(400, "参数类型不匹配"),
    REQUEST_BODY_INVALID(400, "请求体解析失败"),
    FILE_UPLOAD_FAILED(400, "文件上传失败, 请检查文件大小和类型"),
    FILE_SIZE_FORMAT_ERROR(400, "文件大小格式错误"),

    /* ========== 系统异常 (5xx) ========== */
    INTERNAL_ERROR(500, "服务器内部错误"),
    DATABASE_ERROR(501, "数据库操作异常"),
    THIRD_PARTY_SERVICE_ERROR(502, "第三方服务异常"),

    /* ========== 业务逻辑异常 (10xxx) ========== */
    // 空值校验相关（新增）
    EMPTY_PARAMETER(10401, "参数不能为空"),
    EMPTY_PASSWORD(10403, "密码不能为空"),
    EMPTY_EMAIL(10404, "邮箱不能为空"),
    EMPTY_FILE(10406, "上传文件不能为空"),
    EMPTY_SEARCH_KEYWORD(10407, "搜索关键词不能为空"),
    EMPTY_VERIFICATION_CODE(10408, "验证码不能为空"),

    // 用户相关
    EMAIL_EXISTS(10003, "邮箱已注册"),
    USER_NICKNAME_UPDATE_FAILED(10004, "用户昵称更新失败"),
    INVALID_VERIFICATION_CODE(10005, "验证码无效或已过期"),

    // 资源相关
    RESOURCE_NOT_FOUND(10101, "请求的资源不存在"),
    RESOURCE_ALREADY_EXISTS(10102, "资源已存在"),
    RESOURCE_OPERATION_FORBIDDEN(10103, "不允许操作该资源"),

    // 文件/云盘相关
    FILE_SIZE_EXCEEDED(10201, "文件大小超过限制"),
    STORAGE_QUOTA_EXHAUSTED(10202, "存储空间不足"),
    FILE_TYPE_NOT_ALLOWED(10203, "不支持的文件类型"),
    FILE_DIR_CREATE_FAIL(10204, "文件路径创建失败"),
    FILE_UPLOAD_CONFLICT(10205, "文件已存在，请勿重复上传"),
    FILE_EMPTY(10206, "上传的文件为空"),
    FILE_UPLOAD_FAIL(10207, "文件上传失败"),
    FILE_RENAME_FAILED(10208, "文件重命名失败"),
    FILE_NOT_FOUND(10209, "文件不存在或已被删除"),
    FILE_PATH_INVALID(10210, "文件路径非法"),
    DELETE_FILE_FAIL(10211, "删除文件失败"),
    CLOUD_QUOTA_EXCEEDED(10212, "云盘容量配额不足"),
    FILE_IO_ERROR(10213, "文件读写失败"),
    FILE_PERMISSION_DENIED(10214, "文件访问权限不足"),
    FILE_EXISTS(10215, "文件或目录已存在"),
    FILE_NOT_IMAGE(10216, "文件不是图片"),

    // 系统/数据相关
    DATA_INTEGRITY_VIOLATION(10301, "数据完整性冲突"),
    OPTIMISTIC_LOCK_CONFLICT(10302, "数据版本冲突，请重试"),
    SERVICE_UNAVAILABLE(10303, "服务暂时不可用"),

    // 成功
    SUCCESS(200, "操作成功"),

    // 通用错误
    UNKNOWN_ERROR(500, "未知错误"),
    NOT_FOUND(404, "资源不存在"),
    FORBIDDEN(403, "无权访问"),
    UNAUTHORIZED(401, "未授权"),

    // 用户相关错误
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_DISABLED(1002, "用户已禁用"),
    USER_INFO_NOT_FOUND(1003, "用户信息未找到"),
    LOGIN_FAILED(1004, "登录失败"),
    REGISTER_FAILED(1005, "注册失败"),
    INVALID_TOKEN(1006, "无效的令牌"),
    NICKNAME_EXISTS(1006, "昵称已存在"),
    PASSWORD_TOO_SIMPLE(1007, "密码太简单"),
    NICKNAME_TOO_LONG(1008, "昵称太长"),
    LOGIN_ATTEMPTS_EXCEEDED(1009, "登录尝试次数过多，请稍后再试"),

    // 文件相关错误
    FILE_DOWNLOAD_FAIL(2003, "文件下载失败"),
    FILE_DELETE_FAIL(2004, "文件删除失败"),
    FILE_TOO_LARGE(2007, "文件太大"),
    FILE_MOVE_FAILED(2010, "文件移动失败"),
    FILE_MERGE_FAILED(2011, "文件合并失败"),

    // 云存储相关错误
    CLOUD_CAPACITY_NOT_ENOUGH(3002, "云存储空间不足"),
    CLOUD_INIT_FAILED(3003, "云存储空间初始化失败"),

    // 其他错误
    SYSTEM_ERROR(9001, "系统错误"),
    IO_ERROR(9003, "IO错误"),
    NETWORK_ERROR(9004, "网络错误"),
    INVALID_FILENAME(9006, "初始化文件名失败"),
    INVALID_CHUNK_COUNT(9005, "无效的分片数量");


    private final int code;
    private final String msg;

    ErrorOperationStatus(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
