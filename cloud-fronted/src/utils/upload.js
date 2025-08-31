// src/utils/upload.js
import { calculateFileHash } from './fileHash';
import request from '@/api/request';
import toast from '@/utils/toast';

export const uploadWithFastUpload = async (file) => {
  try {
    // 1. 计算文件哈希
    const hash = await calculateFileHash(file);

    // 2. 检查服务器是否已有该文件
    const checkResult = await request.post('/api/check-file-exist', {
      hash,
      filename: file.name,
      size: file.size
    });

    // 3. 如果服务器已有该文件，直接返回成功
    if (checkResult.exists) {
      toast.success('秒传成功', '文件已存在，无需重复上传');
      return {
        success: true,
        fileUrl: checkResult.url,
        isQuickUpload: true
      };
    }

    // 4. 如果服务器没有，则正常上传
    const formData = new FormData();
    formData.append('file', file);
    formData.append('hash', hash);

    const uploadResult = await request.post('/api/upload', formData, {
      onUploadProgress: (progressEvent) => {
        const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
        console.log(`上传进度: ${percentCompleted}%`);
        // 这里可以更新上传进度UI
      }
    });

    toast.success('上传成功', '文件已上传到服务器');
    return {
      success: true,
      fileUrl: uploadResult.url,
      isQuickUpload: false
    };

  } catch (error) {
    toast.error('上传失败', error.message);
    return {
      success: false,
      error
    };
  }
};