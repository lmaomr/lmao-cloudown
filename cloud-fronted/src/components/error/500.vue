<script setup>

</script>
<template>
    <div class="error-container">
        <div id="error">500</div>
        <h1>服务器错误</h1>
        <p>糟糕，服务器遇到了错误，请稍后再试！</p>
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

/* 500 元素样式 */
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