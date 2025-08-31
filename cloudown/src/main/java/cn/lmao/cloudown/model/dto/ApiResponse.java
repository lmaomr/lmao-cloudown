package cn.lmao.cloudown.model.dto;

import cn.lmao.cloudown.excepiton.CustomException;
import cn.lmao.cloudown.model.enums.ErrorOperationStatus;
import lombok.Getter;

/**
 * 统一API响应封装类
 * 
 * @param <T> 响应数据类型
 */
@Getter
public class ApiResponse<T> {
    private static final int DEFAULT_SUCCESS_CODE = 200;
    private static final String DEFAULT_SUCCESS_MSG = "success";

    private final int code;
    private final String msg;
    private final T data;

    // 私有构造器
    private ApiResponse(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static class Builder<T> {
        private int code = DEFAULT_SUCCESS_CODE;
        private String msg = DEFAULT_SUCCESS_MSG;
        private T data;

        public Builder<T> code(int code) {
            this.code = code;
            return this;
        }

        public Builder<T> msg(String msg) {
            this.msg = msg;
            return this;
        }

        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }

        public ApiResponse<T> build() {
            return new ApiResponse<>(code, msg, data);
        }
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static <T> ApiResponse<T> exception(CustomException e) {
        return new ApiResponse<>(e.getCode(), e.getMessage(), null);
    }

    public static <T> ApiResponse<T> exception(ErrorOperationStatus e) {
        return new ApiResponse<>(e.getCode(), e.getMsg(), null);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(DEFAULT_SUCCESS_CODE, DEFAULT_SUCCESS_MSG, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(DEFAULT_SUCCESS_CODE, DEFAULT_SUCCESS_MSG, data);
    }

    public static <T> ApiResponse<T> success(String msg, T data) {
        return new ApiResponse<>(DEFAULT_SUCCESS_CODE, msg, data);
    }

    public static <T> ApiResponse<T> error(int code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }

    public <U> ApiResponse<U> map(java.util.function.Function<? super T, ? extends U> mapper) { 
        return new ApiResponse<>(code, msg, mapper.apply(data));
    }

}   
