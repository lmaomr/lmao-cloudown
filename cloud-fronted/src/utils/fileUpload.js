import toast from '@/utils/toast';
import usePathStore from '@/stores/pathStore';
import { uploadFile as upload, mergeFileChunks} from '@/api/file';

export const uploadFile = async (file, options = {}) => {
  const {
    chunkSize = 5 * 1024 * 1024, // 默认5MB一片
    onProgress = () => { }, // 进度回调
    onSuccess = () => { }, // 成功回调
    onError = () => { } // 错误回调
  } = options;

  const pathStore = usePathStore();

  try {
    // 分割文件
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
      });
    }
    // 上传所有分片
    for (let i = 0; i < chunks.length; i++) {
      const chunk = chunks[i];
      console.log(`开始上传分片 ${i + 1}/${chunks.length}，大小: ${chunk.size} bytes`);
      // 调用上传分片的函数
      const response = await uploadChunk(file.name, chunk, i, chunksCount, (progress) => {
        onProgress({
          progress: Math.floor(((i + progress / 100) / chunks.length) * 100),
          processedBytes: Math.floor(((i + progress / 100) / chunks.length) * file.size),
        })
      });
      if (response.error) {
        throw new Error(`分片${i}上传失败: ${response.error}`);
      }
    }
    // 所有分片已上传完成，合并文件
    const mergeResult = await mergeFileChunks(file.name, file.size, chunksCount, pathStore.getBreadcrumbPath());
    toast.success('上传成功', '文件已成功上传');
    onSuccess(mergeResult.data);
  } catch (error) {
    toast.error('上传失败', error.message || '文件上传失败，请重试');
    onError(error);
  }
}

//上传单个分片
export const uploadChunk = async (filename, chunk, index, totalChunks, onProgress) => {
  try {
    const response = await upload(filename, chunk, index, totalChunks,{
      onUploadProgress: (progressEvent) => {
        const percentCompleted = Math.round((progressEvent.loaded * 100) / chunk.size);
        onProgress(percentCompleted);
      }
    });
    return response.data;
  } catch (error) {
    throw new Error(`分片${index}上传失败: ${error.message}`);
  }
}
