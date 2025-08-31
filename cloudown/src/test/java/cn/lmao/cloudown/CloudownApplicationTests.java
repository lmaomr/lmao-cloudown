package cn.lmao.cloudown;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import cn.lmao.cloudown.util.FileUtil;

@SpringBootTest
class CloudownApplicationTests {
    @Autowired
    private FileUtil fileUtil;// 包含buildAvatarUrl方法的服务类

    @Test
    void testBuildAvatarUrl() {
        // 1. 调用方法
        String url = fileUtil.buildAvatarUrl(123L, "test.jpg");
        System.out.println("生成的URL: " + url); // 打印结果

        // 2. 添加断言
        assertNotNull(url, "URL不应为null");
        assertTrue(url.contains("123"), "URL应包含用户ID");
        assertTrue(url.contains("test.jpg"), "URL应包含文件名");
    }
}
