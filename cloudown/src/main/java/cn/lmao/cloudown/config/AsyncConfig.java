package cn.lmao.cloudown.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {
    
    @Bean(name = "fileUploadTaskExecutor")
    public ThreadPoolTaskExecutor fileUploadTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 根据服务器配置调整这些参数
        executor.setCorePoolSize(5);          // 核心线程数
        executor.setMaxPoolSize(10);           // 最大线程数
        executor.setQueueCapacity(100);        // 队列容量
        executor.setThreadNamePrefix("FileUpload-"); // 线程名前缀
        executor.initialize();
        return executor;
    }
}
