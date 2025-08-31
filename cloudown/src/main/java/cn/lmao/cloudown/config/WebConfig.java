package cn.lmao.cloudown.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import cn.lmao.cloudown.util.LogUtil;

import org.slf4j.Logger;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final Logger log = LogUtil.getLogger();

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("配置资源处理器，头像路径: {}", uploadPath + "/avatar/");
        log.info("配置资源处理器，缩略图路径: {}", uploadPath + "/thumb/");

        // 将本地路径映射到 "/upload/**" URL
        registry.addResourceHandler("/avatar/**")
                .addResourceLocations("file:" + uploadPath + "/avatar/")
                .setCachePeriod(3600); // 设置缓存时间为1小时

        // 将本地路径映射到 "/upload/**" URL
        registry.addResourceHandler("/thumb/**")
                .addResourceLocations("file:" + uploadPath + "/thumb/")
                .setCachePeriod(3600); // 设置缓存时间为1小时
    }

}