<script setup>
</script>
<template>
    <div class="error-container">
        <div id="error">404</div>
        <h1>未找到相关页面</h1>
        <p>抱歉，您访问的页面可能不存在或者被删除！</p>
        <router-link class="error-link" to="/">返回首页</router-link>
    </div>
</template>

<style scoped>
/* 全局样式 */
.error-container {
    font-family: Arial, sans-serif;
    background-color: var(--bg-color);
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
}

h1 {
    color: var(--i-color);
}

p {
    color: var(--i-color);
}

/* 404 元素样式 */
#error {
    font-size: 100px;
    color: var(--text-secondary);
}

/* 按钮样式 */
.error-link {
    background-color: var(--primary-color);
    color: white;
    border: none;
    padding: 10px 20px;
    border-radius: 5px;
    cursor: pointer;
    margin-top: 20px;
    transition: background-color 0.3s ease;
    text-decoration: none;
}
</style>