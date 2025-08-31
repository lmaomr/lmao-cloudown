package cn.lmao.cloudown.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.StopWatch;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 高级日志工具类
 * 功能：
 * 1. 自动TraceID管理
 * 2. 耗时监控日志
 * 3. 结构化日志输出
 * 4. 安全审计日志
 */
public final class LogUtil {
    private static final Map<String, Logger> LOGGER_CACHE = new ConcurrentHashMap<>();
    private static final String TRACE_ID_KEY = "traceId";
    private static final String AUDIT_FLAG = "[AUDIT]";

    // 私有构造器防止实例化
    private LogUtil() {}

    /**
     * 获取Logger实例（带缓存）
     */
    public static Logger getLogger() {
        String className = Thread.currentThread().getStackTrace()[2].getClassName();
        return LOGGER_CACHE.computeIfAbsent(className, LoggerFactory::getLogger);
    }

    /**
     * 初始化请求上下文（在过滤器/拦截器中调用）
     */
    public static void initRequestContext(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId != null ? traceId : generateTraceId());
    }

    /**
     * 清除请求上下文（在请求结束时调用）
     */
    public static void clearRequestContext() {
        MDC.clear();
    }

    /**
     * 生成TraceID
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 耗时监控日志
     */
    public static StopWatch startWatch(String taskName) {
        StopWatch watch = new StopWatch(taskName);
        watch.start();
        debug("⏱️ Start monitoring: {}", taskName);
        return watch;
    }

    public static void stopWatch(StopWatch watch) {
        if (watch != null && watch.isRunning()) {
            watch.stop();
            info("⏱️ Task [{}] cost: {}ms", watch.getId(), watch.getTotalTimeMillis());
        }
    }

    /**
     * 标准日志方法（自动携带TraceID）
     */
    public static void trace(String format, Object... args) {
        getLogger().trace(format, args);
    }

    public static void debug(String format, Object... args) {
        getLogger().debug(format, args);
    }

    public static void info(String format, Object... args) {
        getLogger().info(format, args);
    }

    public static void warn(String format, Object... args) {
        getLogger().warn(format, args);
    }

    public static void error(String format, Object... args) {
        getLogger().error(format, args);
    }

    /**
     * 异常日志（自动解析堆栈）
     */
    public static void error(Throwable ex, String format, Object... args) {
        Logger logger = getLogger();
        if (logger.isErrorEnabled()) {
            logger.error("{}\nStacktrace:", String.format(format, args), ex);
        }
    }

    /**
     * 安全审计日志（单独文件输出）
     */
    public static void audit(String operation, Object data) {
        String traceId = MDC.get(TRACE_ID_KEY);
        try {
            getLogger().info("{} {} - Operation: {}, Data: {}", AUDIT_FLAG, traceId, operation, JsonUtil.toJson(data));
        } catch (JsonUtil.JsonException e) {
            getLogger().error("Failed to serialize audit data: {}", e.getMessage());
        }
    }

    /**
     * 结构化业务日志
     */
    public static void bizLog(String eventType, Map<String, Object> fields) {
        fields.putIfAbsent("traceId", MDC.get(TRACE_ID_KEY));
        fields.putIfAbsent("timestamp", System.currentTimeMillis());
        getLogger().info("📊 [BIZ] {} - {}", eventType, JsonUtil.toJson(fields));
    }

}