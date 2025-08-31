package cn.lmao.cloudown.excepiton;

import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import cn.lmao.cloudown.model.dto.ApiResponse;
import cn.lmao.cloudown.model.enums.ErrorOperationStatus;
import cn.lmao.cloudown.util.LogUtil;
// import jakarta.servlet.http.HttpServletRequest;
import cn.lmao.cloudown.util.UserUtil;

@RestControllerAdvice
public class GlobalException  {

    private final Logger log = LogUtil.getLogger();

    // private void pvfLog(HttpServletRequest request, String errorMsg) {
    //     log.warn("参数校验失败 [traceId={}]: {}", MDC.get("traceId"), errorMsg);
    // }

    @ExceptionHandler(CustomException.class)
    public ApiResponse<String> handleClobalException(CustomException e) {
        log.error("业务异常: [traceId={}]: code={}, msg={}",
                MDC.get("traceId"), e.getCode(), UserUtil.sanitize(e.getMsg()), e);
        return ApiResponse.exception(e);
    }

    @ExceptionHandler(AuthenticationException.class) // 捕获所有认证异常
    public <T> ApiResponse<T> handleAuthException(AuthenticationException e) {
        log.warn("认证失败");
        return ApiResponse.exception(e instanceof BadCredentialsException ? ErrorOperationStatus.BAD_CREDENTIALS
                : ErrorOperationStatus.AUTH_FAIL);
    }

    // 添加文件大小超过限制的异常处理
    // 处理文件上传大小超限异常
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public <T> ApiResponse<T> handleMaxSizeException(MaxUploadSizeExceededException e) {
        log.error("文件大小超过限制: {}", e.getMessage());
        return ApiResponse.exception(ErrorOperationStatus.FILE_SIZE_EXCEEDED);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<String> handleException(Exception e) {
        log.error("系统异常 [traceId={}]: {}", MDC.get("traceId"), e.getMessage(), e);
        return ApiResponse.exception(ErrorOperationStatus.SYSTEM_ERROR);
    }
    
}
