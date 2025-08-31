<script setup>
import { useLoadingStore } from '@/stores/loadingStore';

const loadingStore = useLoadingStore();

</script>

<template>
  <div v-if="loadingStore.isFullscreen" class="logo-loader-overlay">
    <div class="logo-loader-container">
      <!-- 网站Logo -->
      <img class="logo-image" src="/logo.png" alt="用户头像" />
      
      <!-- 旋转边框动画 -->
      <div class="rotating-border"></div>
      
      <!-- 可选加载文字 -->
      <p class="loading-text">拼命加载中...</p>
    </div>
  </div>
</template>


<style scoped>
.logo-loader-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(2px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: var(--z-index-loading);
}

.logo-loader-container {
  position: relative;
  width: 6rem;
  height: 6rem;
  display: flex;
  justify-content: center;
  align-items: center;
}

.logo-image {
  padding: 0.24rem;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: contain;
}

.rotating-border {
  position: absolute;
  width: 100%;
  height: 100%; 
  border: 2px solid transparent;
  border-radius: 50%;
  border-top-color: var(--primary-color);
  animation: spin 0.8s linear infinite;
}

.loading-text {
  position: absolute;
  bottom: -3.2rem;
  width: 100%;
  text-align: center;
  color: var(--primary-color);
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
