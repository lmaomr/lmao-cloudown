<script setup>
import {computed } from 'vue';
import { formatFileSize, formatSpeed } from '@/utils/fomatUtil';

const props = defineProps({
  // 文件名
  fileName: {
    type: String,
    required: true
  },
  // 上传进度 (0-100)
  progress: {
    type: Number,
    default: 0
  },
  // 上传速度 (bytes/s)
  speed: {
    type: Number,
    default: 0
  },
  // 文件大小 (bytes)
  fileSize: {
    type: Number,
    required: true
  },
  // 状态: uploading, success, error, waiting
  status: {
    type: String,
    default: 'waiting'
  },
  // 错误信息
  error: {
    type: String,
    default: ''
  }
});

// 获取文件图标
const getFileIcon = () => {
  const fileName = props.fileName.toLowerCase();

  if (fileName.endsWith('.pdf')) return 'fa-file-pdf';
  if (fileName.endsWith('.doc') || fileName.endsWith('.docx')) return 'fa-file-word';
  if (fileName.endsWith('.xls') || fileName.endsWith('.xlsx')) return 'fa-file-excel';
  if (fileName.endsWith('.ppt') || fileName.endsWith('.pptx')) return 'fa-file-powerpoint';
  if (fileName.endsWith('.jpg') || fileName.endsWith('.jpeg') || fileName.endsWith('.png') || fileName.endsWith('.gif')) return 'fa-file-image';
  if (fileName.endsWith('.mp4') || fileName.endsWith('.avi') || fileName.endsWith('.mov')) return 'fa-file-video';
  if (fileName.endsWith('.mp3') || fileName.endsWith('.wav') || fileName.endsWith('.ogg')) return 'fa-file-audio';
  if (fileName.endsWith('.zip') || fileName.endsWith('.rar') || fileName.endsWith('.7z')) return 'fa-file-archive';
  if (fileName.endsWith('.txt')) return 'fa-file-alt';
  if (fileName.endsWith('.html') || fileName.endsWith('.css') || fileName.endsWith('.js')) return 'fa-file-code';

  return 'fa-file';
};

// 计算剩余时间
const remainingTime = computed(() => {
  if (props.progress >= 100 || props.progress <= 0 || props.speed <= 0) {
    return '--';
  }

  const remainingBytes = props.fileSize * (1 - props.progress / 100);
  const seconds = Math.ceil(remainingBytes / props.speed);

  if (seconds < 60) {
    return `${seconds}秒`;
  } else if (seconds < 3600) {
    return `${Math.floor(seconds / 60)}分${seconds % 60}秒`;
  } else {
    return `${Math.floor(seconds / 3600)}时${Math.floor((seconds % 3600) / 60)}分`;
  }
});

// 状态图标
const statusIcon = computed(() => {
  switch (props.status) {
    case 'uploading':
      return 'fas fa-sync fa-spin';
    case 'success':
      return 'fas fa-check-circle';
    case 'error':
      return 'fas fa-exclamation-circle';
    default:
      return 'fas fa-hourglass';
  }
});

// 状态颜色
const statusColor = computed(() => {
  switch (props.status) {
    case 'uploading':
      return 'var(--primary-color)';
    case 'success':
      return 'var(--success-color)';
    case 'error':
      return 'var(--danger-color)';
    default:
      return 'var(--text-secondary)';
  }
});
</script>

<template>
  <div class="upload-progress" :class="status">
    <div class="upload-info">
      <div class="file-info">
        <div class="file-icon" :style="{ color: statusColor }">
          <i class="fas" :class="getFileIcon()"></i>
        </div>
        <div class="file-details">
          <div class="file-name">{{ fileName }}</div>
          <div class="file-meta">
            <span>{{ formatFileSize(fileSize) }}</span>
            <span v-if="status === 'uploading'" class="upload-stats">
              <i class="fas fa-tachometer-alt"></i> {{ formatSpeed(speed) }}
              <i class="fas fa-clock"></i> {{ remainingTime }}
            </span>
            <span v-else-if="status === 'error'" class="error-message">
              <i class="fas fa-exclamation-triangle"></i> {{ error }}
            </span>
            <span v-else-if="status === 'success'" class="success-message">
              <i class="fas fa-check-circle"></i> 上传完成
            </span>
            <span v-else class="waiting-message">
              <i class="fas fa-hourglass-half"></i> 等待上传
            </span>
          </div>
        </div>
      </div>
      <div class="status-icon" :style="{ color: statusColor }">
        <i :class="statusIcon"></i>
      </div>
    </div>
    <div class="progress-bar-container">
      <div class="progress-bar" :style="{ width: `${progress}%`, backgroundColor: statusColor }"></div>
    </div>
  </div>
</template>

<style scoped>
.upload-progress {
  background-color: var(--card-bg);
  border-radius: var(--card-border-radius);
  border: 1px solid var(--border-color);
  padding: 0.85rem 1rem;
  margin-bottom: 0.85rem;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.upload-progress:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.upload-progress.success {
  border-left: 3px solid var(--success-color);
}

.upload-progress.error {
  border-left: 3px solid var(--danger-color);
}

.upload-progress.uploading {
  border-left: 3px solid var(--primary-color);
}

.upload-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.85rem;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 0.85rem;
  overflow: hidden;
}

.file-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: rgba(var(--primary-color-rgb, 25, 118, 210), 0.1);
}

.file-details {
  overflow: hidden;
}

.file-name {
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 0.3rem;
}

.file-meta {
  font-size: 0.75rem;
  color: var(--text-secondary);
  display: flex;
  gap: 0.5rem;
  align-items: center;
  flex-wrap: wrap;
}

.file-meta i {
  margin-right: 0.2rem;
}

.upload-stats, .error-message, .success-message, .waiting-message {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.error-message {
  color: var(--danger-color);
}

.success-message {
  color: var(--success-color);
}

.waiting-message {
  color: var(--warning-color);
}

.status-icon {
  font-size: 1.25rem;
  flex-shrink: 0;
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: rgba(var(--primary-color-rgb, 25, 118, 210), 0.1);
}

.progress-bar-container {
  height: 0.4rem;
  background-color: var(--progress-bg);
  border-radius: 1rem;
  overflow: hidden;
  position: relative;
}

.progress-bar {
  height: 100%;
  border-radius: 1rem;
  transition: width 0.3s ease;
  position: relative;
}

.progress-bar::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0.1) 25%,
    rgba(255, 255, 255, 0.2) 50%,
    rgba(255, 255, 255, 0.1) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  display: none;
}

.uploading .progress-bar::after {
  display: block;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

@media (max-width: 480px) {
  .upload-progress {
    padding: 0.75rem;
  }

  .file-icon, .status-icon {
    width: 1.75rem;
    height: 1.75rem;
    font-size: 1.25rem;
  }

  .file-info {
    gap: 0.65rem;
  }
}
</style>
