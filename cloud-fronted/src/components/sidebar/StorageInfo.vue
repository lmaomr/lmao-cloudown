<script setup>
import { formatFileSize } from '@/utils/fomatUtil';

// 定义组件的props
defineProps({
  used: {
    type: Number,
    default: 0
  },
  total: {
    type: Number,
    default: 0
  },
  percentage: {
    type: Number,
    default: 0,
    validator: value => value >= 0 && value <= 100
  }
});
</script>

<template>
  <div class="storage-info card">
    <div class="storage-title">存储空间</div>
    <div class="storage-progress">
      <div class="progress-bar" role="progressbar" :aria-valuenow="percentage" aria-valuemin="0" aria-valuemax="100">
        <div class="progress" :style="{ width: `${percentage}%` }"></div>
      </div>
      <div class="storage-text">
        <span>{{ formatFileSize(used) }} / {{ formatFileSize(total) }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.storage-info {
  margin: 0 12px;
  background: var(--card-bg);
  border-radius: var(--card-border-radius);
  box-shadow: var(--shadow-sm);
  padding: 14px 16px 10px 16px;
  border: 1px solid #f0f0f0;
}

.storage-title {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.storage-progress {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.progress-bar {
  height: 6px;
  background-color: var(--progress-bg);
  border-radius: 3px;
  overflow: hidden;
}

.progress {
  height: 100%;
  background: linear-gradient(90deg, #42a5f5 0%, var(--primary-color) 100%);
  border-radius: 3px;
  transition: width var(--transition-speed) ease;
}

.storage-text {
  font-size: 12px;
  color: #6c757d;
  text-align: right;
}
</style>
