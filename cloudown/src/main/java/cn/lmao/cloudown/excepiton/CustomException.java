package cn.lmao.cloudown.excepiton;

import cn.lmao.cloudown.model.enums.ErrorOperationStatus;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final int code;
    private final String msg;

    public CustomException(ErrorOperationStatus errorOperationStatus) {
        super(errorOperationStatus.getMsg());  // 传递消息给父类
        this.code = errorOperationStatus.getCode();
        this.msg = errorOperationStatus.getMsg();
    }

    public CustomException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

}
