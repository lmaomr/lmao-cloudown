package cn.lmao.cloudown.util;

public class UserUtil {
    /**
     * 敏感信息脱敏处理
     */
    public static String sanitize(String input) {
        if (input == null)
            return null;
        // 手机号脱敏
        input = input.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
        // 邮箱脱敏
        input = input.replaceAll("(\\w{2})[^@]*(@.*)", "$1****$2");
        return input;
    }
}
