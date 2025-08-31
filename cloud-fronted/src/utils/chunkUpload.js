// src/utils/chunkUpload.js
import { calculateFileHash } from './fileHash';
import { uploadFile, mergeFileChunks, checkUploadTask } from '@/api/file';
import toast from '@/utils/toast';
import usePathStore from '@/stores/pathStore';


export const uploadFileInChunks = async (file, options = {}) => {
  const {
    chunkSize = 5 * 1024 * 1024, // 默认5MB一片
    onProgress = () => { }, // 进度回调
    onSuccess = () => { }, // 成功回调
    onError = () => { } // 错误回调
  } = options;

  const pathStore = usePathStore();

  try {
    // 1. 计算文件哈希（作为文件唯一标识）
    const quickHash = await calculateFileHash([file.slice(0, chunkSize), file.slice(file.size - chunkSize, file.size)]);

    // 2. 检查是否有未完成的上传任务
    const uploadTask = await checkUploadTask(quickHash);
    // 3. 如果服务器已有完整文件，直接返回成功（秒传）
    if (uploadTask.completed) {
      toast.success('秒传成功', '文件已存在，无需重复上传');
      onSuccess(uploadTask.url);
      return;
    }

    // 4. 分割文件
    const chunks = [];
    const chunksCount = Math.ceil(file.size / chunkSize);

    for (let i = 0; i < chunksCount; i++) {
      const start = i * chunkSize;
      const end = Math.min(start + chunkSize, file.size);
      const chunk = file.slice(start, end);
      chunks.push({
        index: i,
        chunk,
        size: chunk.size,
        uploaded: uploadTask.data?.uploadedChunks?.includes(i) || false // 已上传的分片
      });
    }

    // 5. 上传所有未上传的分片
    const uploadedChunks = [];
    let completedSize = 0;

    // 已上传的分片直接计入进度
    chunks.forEach(item => {
      if (item.uploaded) {
        uploadedChunks.push(item.index);
        completedSize += item.size;
        onProgress(Math.floor((completedSize / file.size) * 100));
      }
    });

    // 并发上传分片，这里限制5个并发
    const concurrency = 5;
    const pool = []; // 并发池

    for (const chunk of chunks) {
      // 跳过已上传的分片
      if (chunk.uploaded) continue;

      // 等待并发池有空位
      if (pool.length >= concurrency) {
        await Promise.race(pool);
      }

      // 上传分片
      const uploadPromise = uploadChunk(file.name, chunk, chunk.index, chunksCount, (progress) => {
        completedSize += chunk.size;
        onProgress({
          progress: Math.floor((completedSize / file.size) * 100),
          processedBytes: Math.floor(progress / 100 * chunk.size + completedSize),
        })
      }).then(res => {
        uploadedChunks.push(chunk.index);
        // 从并发池移除已完成的Promise
        const index = pool.indexOf(uploadPromise);
        if (index !== -1) pool.splice(index, 1);
        return res;
      }).catch(err => {
        const index = pool.indexOf(uploadPromise);
        if (index !== -1) pool.splice(index, 1);
        throw err;
      });
      pool.push(uploadPromise);
    }

    // 等待所有分片上传完成
    await Promise.all(pool);

    // 6. 通知服务器所有分片已上传完成，合并文件
    const mergeResult = await mergeFileChunks(file.name, file.size, chunksCount, pathStore.getBreadcrumbPath());
    toast.success('上传成功', '文件已上传到服务器');
    onSuccess(mergeResult.url);
  } catch (error) {
    onError(error);
  }
};

// 上传单个分片
const uploadChunk = async (filename, chunk, index, totalChunks, onProgress) => {
  try {
    const response = await uploadFile(filename, chunk, index, totalChunks, {
      onUploadProgress: (progressEvent) => {
        const percentCompleted = Math.round((progressEvent.loaded * 100) / chunk.size);
        onProgress(percentCompleted);
      }
    });
    return response.data;
  } catch (error) {
    throw new Error(`分片${index}上传失败: ${error.message}`);
  }
};
