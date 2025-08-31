<script setup>
import { ref } from 'vue';
import { useUploadStore } from '@/stores/uploadStore';
import { formatFileSize, formatSpeed, formatFileType } from '@/utils/fomatUtil';

defineProps({
  // 是否显示上传管理器
  visible: {
    type: Boolean,
    default: false
  }
});

defineEmits(['close']);

// 获取上传store
const uploadStore = useUploadStore();

// 是否折叠
const isCollapsed = ref(false);

// 切换折叠状态
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value;
};

// 清除已完成的上传
const clearCompleted = () => {
  uploadStore.clearCompletedTasks();
};

// 删除任务
const removeTask = (taskId) => {
  uploadStore.cancelUploadTask(taskId);
};

// 暂停任务
const pauseTask = (taskId) => {
  // 暂停功能已移除，改为取消任务
  uploadStore.cancelUploadTask(taskId);
};

// 继续任务
const resumeTask = (taskId) => {
  // 继续功能已移除，改为重试任务
  uploadStore.retryUploadTask(taskId);
};

// 重试任务
const retryTask = (taskId) => {
  uploadStore.retryUploadTask(taskId);
};
</script>

<template>
  <div v-if="visible"
    class="upload-manager"
    :class="{ 'collapsed': isCollapsed }">
    <div class="upload-manager-header">
      <div class="upload-manager-title">
        <span>上传队列</span>
        <span class="upload-stats" v-if="uploadStore.uploadTasks.length > 0">
          {{ uploadStore.uploadStats.completed }}/{{ uploadStore.uploadTasks.length }}
        </span>
      </div>
      <div class="upload-manager-actions">
        <button v-if="!isCollapsed && uploadStore.uploadStats.completed > 0" class="header-btn clear-btn" @click="clearCompleted" title="清除已完成">
          <i class="fas fa-trash-alt"></i>
        </button>
        <button class="header-btn toggle-btn" @click="toggleCollapse" :title="isCollapsed ? '展开' : '折叠'">
          <i :class="isCollapsed ? 'fas fa-chevron-up' : 'fas fa-chevron-down'"></i>
        </button>
        <button class="header-btn close-btn" @click="$emit('close')" title="关闭">
          <i class="fas fa-times"></i>
        </button>
      </div>
    </div>
    <div v-if="!isCollapsed" class="upload-manager-body">
      <div v-if="uploadStore.uploadTasks.length === 0" class="no-uploads">
        <span>暂无上传任务</span>
      </div>
      <div v-else class="upload-list">
        <div v-for="task in uploadStore.uploadTasks" :key="task.id"
             class="upload-task-item"
             :class="task.status">
          <div class="task-progress-bg" :style="{ width: task.progress + '%' }"></div>
          <div class="task-content">
            <div class="task-icon">
              <i :class="formatFileType(task.fileName).icon"></i>
            </div>
            <div class="task-info">
              <div class="task-name">{{ task.fileName }}</div>

              <div class="task-details">
                <div class="task-status-info" :class="task.status">
                  <span v-if="task.status === 'success'">已上传</span>
                  <span v-else-if="task.status === 'error'">上传失败: {{ task.message }}</span>
                  <span v-else-if="task.status === 'paused'">已暂停</span>
                  <span v-else-if="task.status === 'merge'">{{ task.message }}</span>
                  <span v-else>
                    {{ formatFileSize(task.processedBytes) }} / {{ formatFileSize(task.fileSize) }} - {{ task.progress.toFixed(1) }}%
                    <span v-if="task.status === 'uploading'" class="task-speed">
                      {{ formatSpeed(task.speed) }}
                    </span>
                  </span>
                </div>
                <div class="task-actions">
                  <button v-if="task.status === 'uploading'" class="task-btn pause" @click="pauseTask(task.id)" title="暂停">
                    <i class="fas fa-pause"></i>
                  </button>
                  <button v-if="task.status === 'paused'" class="task-btn resume" @click="resumeTask(task.id)" title="继续">
                    <i class="fas fa-play"></i>
                  </button>
                  <button v-if="task.status === 'error'" class="task-btn retry" @click="retryTask(task.id)" title="重试">
                    <i class="fas fa-redo"></i>
                  </button>
                  <button class="task-btn delete" @click="removeTask(task.id)" title="删除">
                    <i class="fas fa-times"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.upload-manager {
  position: fixed;
  bottom: 1rem;
  right: 2rem;
  width: 30rem;
  max-height: 25rem;
  background-color: white;
  border-radius: var(--card-border-radius);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  z-index: var(--z-index-modal);
  overflow: hidden;
  max-width: calc(100vw - 2rem);
  transition: all 0.2s ease;
}

.upload-manager button{
  cursor: pointer;
  border: none;
  box-shadow: none;
}

.upload-manager button:focus{
  outline: none;
}

.upload-manager.collapsed {
  max-height: 4rem;
}

.upload-manager-header {
  padding: 0.875rem 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: nowrap;
  background-color: white;
  border-bottom: 1px solid #f0f0f0;
}

.upload-manager-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 500;
  font-size: 0.9rem;
  color: #333;
}

.upload-stats {
  font-size: 0.75rem;
  color: #666;
  font-weight: normal;
}

.upload-manager-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.header-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 1.5rem;
  height: 1.5rem;
  border-radius: 50%;
  background-color: transparent;
  border: none;
  color: #666;
  cursor: pointer;
  transition: all 0.2s ease;
}

.clear-btn:hover {
  color: var(--warning-color);
}

.toggle-btn:hover {
  color: var(--primary-color);
}

.close-btn:hover {
  color: var(--danger-color);
}

.upload-manager-body {
  padding: 0;
  overflow-y: auto;
  max-height: 25rem;
}

.no-uploads {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 1.5rem 0;
  color: #999;
  font-size: 0.9rem;
  gap: 1rem;
}

.upload-list {
  display: flex;
  flex-direction: column;
}

.upload-task-item {
  position: relative;
  padding: 0.75rem 1rem;
  border-bottom: 1px solid #f0f0f0;
  overflow: hidden;
}

.upload-task-item:last-child {
  border-bottom: none;
}

.task-progress-bg {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background-color: rgba(33, 150, 243, 0.1); /* 浅蓝色背景 */
  z-index: 0;
  transition: width 0.3s ease;
}

.upload-task-item.success .task-progress-bg {
  background-color: rgba(76, 175, 80, 0.1); /* 浅绿色背景 */
  width: 100% !important;
}

.upload-task-item.error .task-progress-bg {
  background-color: rgba(244, 67, 54, 0.1); /* 浅红色背景 */
}

.upload-task-item.paused .task-progress-bg {
  background-color: rgba(255, 152, 0, 0.1); /* 浅橙色背景 */
}

.task-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
}

.task-icon {
  width: 1.5rem;
  height: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  flex-shrink: 0;
  margin-right: 0.75rem;
}

.task-icon {
  font-size: 1.15rem;
}

.task-info {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.task-name {
  font-size: 0.85rem;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.task-details {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-status-info {
  font-size: 0.75rem;
  color: #666;
}

.task-status-info.success {
  color: #4caf50;
}

.task-status-info.error {
  color: #f44336;
}

.task-status-info.paused {
  color: #ff9800;
}

.task-status-info.uploading {
  color: #2196f3;
}

.task-speed {
  color: #666;
  margin-left: 0.5rem;
}

.task-actions {
  display: flex;
  align-items: center;
  gap: 0.35rem;
}

.task-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 1.25rem;
  height: 1.25rem;
  border-radius: 50%;
  background-color: transparent;
  border: none;
  color: #bbb;
  cursor: pointer;
  transition: all 0.2s ease;
}

.task-btn.pause:hover {
  color: #ff9800;
}

.task-btn.resume:hover {
  color: #4caf50;
}

.task-btn.retry:hover {
  color: #2196f3;
}

.task-btn.delete:hover {
  color: #f44336;
}

/* 在小屏幕上调整上传管理器的位置和大小 */
@media (max-width: 480px) {
  .upload-manager {
    width: 20rem;
    bottom: 0.75rem;
    right: 0.75rem;
  }
}

/* 当上传管理器宽度较小时 */
@media (max-width: 360px) {
  .upload-manager {
    width: 18rem;
    bottom: 0.5rem;
    right: 0.5rem;
  }

  .upload-manager-header {
    padding: 0.5rem 0.75rem;
  }

  .task-details {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }

  .task-actions {
    align-self: flex-end;
  }
}
</style>
