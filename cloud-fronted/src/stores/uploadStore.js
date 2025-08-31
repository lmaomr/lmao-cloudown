import { ref, computed } from 'vue';
import { defineStore } from 'pinia';
import { calculateFileHash } from '@/utils/fileHash';
// import { uploadFile } from '@/utils/fileUpload.js'
import {uploadFileInChunks as uploadFile} from '@/utils/chunkUpload';

export const useUploadStore = defineStore('upload', () => {
  // 上传任务列表
  const uploadTasks = ref([]);
  // 上传管理器是否可见
  const isUploadManagerVisible = ref(false);
  // 上传任务ID计数器
  let taskIdCounter = 1;

  // 计算上传统计
  const uploadStats = computed(() => {
    const total = uploadTasks.value.length;
    const completed = uploadTasks.value.filter(task => task.status === 'success').length;
    const failed = uploadTasks.value.filter(task => task.status === 'error').length;
    const uploading = uploadTasks.value.filter(task => task.status === 'uploading').length;

    return { total, completed, failed, uploading };
  });

  // 显示上传管理器
  const showUploadManager = () => {
    isUploadManagerVisible.value = true;
  };

  // 隐藏上传管理器
  const hideUploadManager = () => {
    isUploadManagerVisible.value = false;
  };

  // 切换上传管理器显示状态
  const toggleUploadManager = () => {
    isUploadManagerVisible.value = !isUploadManagerVisible.value;
  };

  // 添加上传任务
  const addUploadTask = async (file) => {
    const taskId = taskIdCounter++;

    const task = {
      id: taskId,
      fileName: file.name,
      fileSize: file.size,
      file: file,
      progress: 0,
      speed: 0,
      status: 'waiting',
      message: '',
      lastUpdateTime: Date.now(),
      processedBytes: 0
    };

    uploadTasks.value.push(task);

    // 如果有上传任务，自动显示上传管理器
    if (uploadTasks.value.length > 0 && !isUploadManagerVisible.value) {
      showUploadManager();
    }

    await uploadFile(file, {
      onProgress: ({ progress, processedBytes }) => {
        updateUploadProgress(taskId, progress, processedBytes);
      },
      onSuccess: () => {
        completeUploadTask(taskId);
      },
      onError: (error) => {
        failUploadTask(taskId, error);
      }
    });
    return task.status;
  };

  // 更新上传进度
  const updateUploadProgress = (taskId, progress, processedBytes) => {
    const task = uploadTasks.value.find(t => t.id === taskId);
    if (!task) return;

    const now = Date.now();
    const timeDiff = (now - task.lastUpdateTime) / 1000; // 转换为秒

    if (timeDiff > 0) {
      const bytesDiff = processedBytes - task.processedBytes;
      task.speed = bytesDiff / timeDiff;
    }

    task.progress = progress;
    task.processedBytes = processedBytes;
    task.lastUpdateTime = now;
    if (progress < 100) {
      task.status = 'uploading';
    }else {
      task.status = 'merge';
      task.message = '正在合并文件分片...';
    }
  };

  // 完成上传任务
  const completeUploadTask = (taskId) => {
    const task = uploadTasks.value.find(t => t.id === taskId);
    if (!task) return;

    task.progress = 100;
    task.speed = 0;
    task.status = 'success';
  };

  // 上传任务失败
  const failUploadTask = (taskId, error) => {
    const task = uploadTasks.value.find(t => t.id === taskId);
    if (!task) return;

    task.speed = 0;
    task.status = 'error';
    task.message = error || '上传失败';
  };

  // 重试上传任务
  const retryUploadTask = (taskId) => {
    const task = uploadTasks.value.find(t => t.id === taskId);
    if (!task) return;

    task.progress = 0;
    task.speed = 0;
    task.status = 'waiting';
    task.message = '';
    task.processedBytes = 0;
    task.lastUpdateTime = Date.now();

    // 这里应该调用实际的上传逻辑
    startUpload(task);
  };

  // 取消上传任务
  const cancelUploadTask = (taskId) => {
    const task = uploadTasks.value.find(t => t.id === taskId);
    if (!task) return;

    if (task.status === 'uploading' || task.status === 'waiting') {
      task.status = 'error';
      task.message = '用户取消';
      task.speed = 0;
    }else if (task.status === 'success') {
      // 如果已经成功上传，直接删除任务
      uploadTasks.value = uploadTasks.value.filter(t => t.id !== taskId);
    }
  };

  // 清除已完成的上传
  const clearCompletedTasks = () => {
    uploadTasks.value = uploadTasks.value.filter(task => task.status !== 'success');
  };

  // 重试所有失败的上传
  const retryAllFailedTasks = () => {
    const failedTasks = uploadTasks.value.filter(task => task.status === 'error');
    failedTasks.forEach(task => retryUploadTask(task.id));
  };

  // 取消所有上传
  const cancelAllTasks = () => {
    uploadTasks.value.forEach(task => {
      if (task.status === 'uploading' || task.status === 'waiting') {
        task.status = 'error';
        task.message = '用户取消';
        task.speed = 0;
      }
    });
  };

  // 开始上传文件
  const startUpload = async (task) => {
    try {
      // 更新任务状态为上传中
      task.status = 'uploading';

      // 模拟上传进度更新
      const simulateUpload = () => {
        // 模拟上传进度
        const interval = setInterval(() => {
          if (task.progress < 100 && task.status === 'uploading') {
            const increment = Math.random() * 10;
            const newProgress = Math.min(task.progress + increment, 99); // 不到100，留给实际完成的时候
            const newProcessedBytes = Math.floor(task.fileSize * (newProgress / 100));

            updateUploadProgress(task.id, newProgress, newProcessedBytes);

            if (newProgress >= 99) {
              clearInterval(interval);
              // 模拟上传完成
              setTimeout(() => {
                completeUploadTask(task.id);
              }, 1000);
            }
          } else {
            clearInterval(interval);
          }
        }, 500);

        // 随机模拟失败
        if (Math.random() < 0.2) { // 20%的概率失败
          setTimeout(() => {
            if (task.status === 'uploading') {
              clearInterval(interval);
              failUploadTask(task.id, '网络连接中断');
            }
          }, Math.random() * 5000);
        }
      };

      // 计算文件哈希
      const chunks = [];
      for (let i = 0; i < task.file.size; i += 1024 * 1024) {
        const chunk = task.file.slice(i, i + 1024 * 1024);
        chunks.push(chunk);
      }

      // 计算文件哈希
      await calculateFileHash(chunks, {
        onProgress: (progress) => {
          // 哈希计算进度不更新到上传进度，只在控制台显示
          console.log(`文件 ${task.fileName} 哈希计算进度: ${progress.progress}%`);
        }
      });

      // 开始模拟上传
      simulateUpload();

    } catch (error) {
      failUploadTask(task.id, error.message || '上传准备失败');
    }
  };

  // 上传文件
  const uploadFiles = (files) => {
    if (!files || files.length === 0) return;

    Array.from(files).forEach(file => {
      const taskId = addUploadTask(file);
      const task = uploadTasks.value.find(t => t.id === taskId);
      if (task) {
        startUpload(task);
      }
    });
  };

  return {
    uploadTasks,
    isUploadManagerVisible,
    uploadStats,
    showUploadManager,
    hideUploadManager,
    toggleUploadManager,
    addUploadTask,
    updateUploadProgress,
    completeUploadTask,
    failUploadTask,
    retryUploadTask,
    cancelUploadTask,
    clearCompletedTasks,
    retryAllFailedTasks,
    cancelAllTasks,
    uploadFiles
  };
});
