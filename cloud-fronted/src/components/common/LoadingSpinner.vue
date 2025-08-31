<script setup>

defineProps({
  // 加载器大小: small, medium, large
  size: {
    type: String,
    default: 'medium'
  },
  // 加载器类型: spinner, dots, pulse
  type: {
    type: String,
    default: 'spinner'
  },
  // 是否显示文本
  showText: {
    type: Boolean,
    default: false
  },
  // 加载文本
  text: {
    type: String,
    default: '加载中...'
  },
  // 是否全屏显示
  fullscreen: {
    type: Boolean,
    default: false
  }
});
</script>

<template>
  <div class="loading-container" :class="{ 'loading-fullscreen': fullscreen }">
    <!-- 旋转加载器 -->
    <div v-if="type === 'spinner'" class="loading-spinner" :class="[`spinner-${size}`]">
      <div class="spinner-circle"></div>
    </div>

    <!-- 点状加载器 -->
    <div v-else-if="type === 'dots'" class="loading-dots" :class="[`dots-${size}`]">
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
    </div>

    <!-- 脉冲加载器 -->
    <div v-else-if="type === 'pulse'" class="loading-pulse" :class="[`pulse-${size}`]">
      <div class="pulse-circle"></div>
    </div>

    <!-- 加载文本 -->
    <div v-if="showText" class="loading-text" :class="[`text-${size}`]">{{ text }}</div>
  </div>
</template>

<style scoped>
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
}

.loading-fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: var(--overlay-bg);
  z-index: 9999;
}

/* 旋转加载器样式 */
.loading-spinner {
  display: inline-block;
  position: relative;
}

.spinner-small {
  width: 1.5rem;
  height: 1.5rem;
}

.spinner-medium {
  width: 2.5rem;
  height: 2.5rem;
}

.spinner-large {
  width: 3.5rem;
  height: 3.5rem;
}

.spinner-circle {
  box-sizing: border-box;
  display: block;
  position: absolute;
  width: 100%;
  height: 100%;
  border: 3px solid transparent;
  border-radius: 50%;
  animation: spin 1.2s cubic-bezier(0.5, 0, 0.5, 1) infinite;
  border-color: var(--primary-color) transparent transparent transparent;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 点状加载器样式 */
.loading-dots {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.dots-small .dot {
  width: 0.5rem;
  height: 0.5rem;
}

.dots-medium .dot {
  width: 0.75rem;
  height: 0.75rem;
}

.dots-large .dot {
  width: 1rem;
  height: 1rem;
}

.dot {
  background-color: var(--primary-color);
  border-radius: 50%;
  display: inline-block;
  animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) {
  animation-delay: -0.32s;
}

.dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

/* 脉冲加载器样式 */
.loading-pulse {
  position: relative;
}

.pulse-small {
  width: 1.5rem;
  height: 1.5rem;
}

.pulse-medium {
  width: 2.5rem;
  height: 2.5rem;
}

.pulse-large {
  width: 3.5rem;
  height: 3.5rem;
}

.pulse-circle {
  width: 100%;
  height: 100%;
  background-color: var(--primary-color);
  border-radius: 50%;
  opacity: 0.6;
  animation: pulse 1.2s cubic-bezier(0, 0.2, 0.8, 1) infinite;
}

@keyframes pulse {
  0% {
    transform: scale(0);
    opacity: 1;
  }
  100% {
    transform: scale(1);
    opacity: 0;
  }
}

/* 文本样式 */
.loading-text {
  color: var(--text-color);
  font-weight: 500;
}

.text-small {
  font-size: 0.75rem;
}

.text-medium {
  font-size: 0.875rem;
}

.text-large {
  font-size: 1rem;
}
</style>
