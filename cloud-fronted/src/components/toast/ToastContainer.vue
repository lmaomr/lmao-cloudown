<script setup>
import { ref, computed } from 'vue'

defineProps({
  toast: {
    type: Object,
    required: false,
    default: () => ({ type: '', title: '', message: '' })
  }
});

// 动态计算图标类名
const getToastIconClass = (toastType) => {
  const base = 'fas'; // Font Awesome Solid 风格
  switch (toastType) {
    case 'success': return `${base} fa-check-circle`;
    case 'error': return `${base} fa-exclamation-circle`;
    case 'warning': return `${base} fa-exclamation-triangle`;
    case 'loading': return `${base} fa-spinner fa-spin`; // 旋转动画
    case 'info':
    default: return `${base} fa-info-circle`;
  }
};

// Toast 数据
const toasts = ref([])

// 容器样式（可动态调整位置）
const containerStyle = computed(() => ({
  top: '20px',
  right: '20px',
}))

// 添加 Toast
const addToast = (type, title, message, duration = 3000, id) => {
  id = id || Date.now().toString(36) + Math.random().toString(36).substring(2,9)
  console.log(id)
  toasts.value.push({ id, type, title, message })
  if (toasts.value.length > 6) {
    toasts.value.shift(); // 移除最早的通知
  }
  // 自动消失
  if (duration > 0) {
    setTimeout(() => {
      removeToast(id)
    }, duration)
  }
}

// 移除 Toast
const removeToast = (id) => {
  toasts.value = toasts.value.filter(toast => toast.id !== id)
}

// 暴露方法给父组件
defineExpose({ addToast, removeToast })
</script>

<template>
  <!-- 使用 Teleport 挂载到 body 末端，避免层级问题 -->
  <Teleport to="body">
    <!-- 容器定位在视图顶部 -->
    <div class="toast-container" :style="containerStyle">
      <!-- 循环渲染每个 toast -->
      <TransitionGroup name="toast">
        <div v-for="toast in toasts" :key="toast.id" class="toast" :class="`toast-${toast.type}`">
          <!-- 动态图标 -->
          <div class="toast-icon">
            <i :class="getToastIconClass(toast.type)"></i>
          </div>
          <div class="toast-content">
            <div class="toast-title">{{ toast.title }}</div>
            <div class="toast-message">{{ toast.message }}</div>
          </div>
          <button class="toast-close" @click.stop="removeToast(toast.id)">
            <i class="fas fa-times"></i>
          </button>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
/* 容器样式 */
.toast-container {
  position: fixed;
  z-index: var(--z-index-toast);
  max-width: 16rem;
  padding-right: 5px;
}

/* 基本样式 */
.toast {
  position: relative;
  display: flex;
  align-items: flex-start;
  padding: 12px 15px;
  margin-bottom: 10px;
  border-radius: 6px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.15);
  background-color: var(--toast-bg);
  color: var(--text-primary);
  max-width: 100%;
}

/* Vue过渡动画类 */
.toast-enter-active {
  animation: toast-slide-in 0.3s forwards;
}

.toast-leave-active {
  animation: toast-slide-out 0.3s forwards;
}

.toast-move {
  transition: transform 0.3s ease;
}

/* 不同类型通知的样式 */
.toast-success {
  --toast-color: var(--success-color);
}

.toast-error {
  --toast-color: var(--danger-color);
}

.toast-warning {
  --toast-color: var(--warning-color);
}

.toast-info,
.toast-loading {
  --toast-color: var(--default-color);
}

.toast[class*="toast-"] {
  border-left: 4px solid var(--toast-color, var(--default-color));
}

.toast-icon {
  flex-shrink: 0;
  margin-right: 12px;
  font-size: 20px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--toast-color, var(--default-color));
}

.toast-content {
  flex-grow: 1;
  padding-right: 10px;
}

.toast-title {
  font-weight: 600;
  margin-bottom: 3px;
  font-size: 14px;
}

.toast-message {
  font-size: 13px;
  line-height: 1.4;
  color: var(--text-secondary);
  word-break: break-word;
}

.toast-close {
  position: absolute;
  top: 8px;
  right: 8px;
  background: none;
  border: none;
  color: var(--i-color);
  cursor: pointer;
  font-size: 14px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0.7;
  transition: opacity 0.2s;
}

.toast-close:hover {
  opacity: 1;
}

/* 动画 */
@keyframes toast-slide-in {
  from {
    opacity: 0;
    transform: translateX(100%);
  }

  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes toast-slide-out {
  from {
    opacity: 1;
    transform: translateX(0);
  }

  to {
    opacity: 0;
    transform: translateX(100%);
  }
}
</style>
