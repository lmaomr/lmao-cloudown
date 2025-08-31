import SparkMD5 from 'spark-md5';

/**
 * 使用Web Worker多线程进行文件块的增量MD5哈希计算
 * @param {Array<Blob>} chunks - 文件块数组
 * @param {Object} options - 配置选项
 * @param {number} options.workerCount - 要使用的worker数量，默认为可用CPU核心数或4
 * @param {Function} options.onProgress - 进度回调函数
 * @param {AbortSignal} options.signal - 用于取消计算的AbortSignal
 * @returns {Promise<string>} 返回一个Promise，resolve时返回文件的MD5哈希值
 */
export const calculateFileHash = (chunks, options = {}) => {
  return new Promise((resolve, reject) => {
    // 参数校验
    if (!Array.isArray(chunks) || chunks.length === 0) {
      reject(new Error('Invalid chunks: must be a non-empty array'));
      return;
    }

    // 配置选项
    const {
      workerCount = Math.min(navigator.hardwareConcurrency || 4, 12),
      onProgress = () => {},
      signal
    } = options;

    // 检查是否已被取消
    if (signal?.aborted) {
      reject(new Error('Calculation was aborted'));
      return;
    }

    // 计算总大小
    const totalBytes = chunks.reduce((total, chunk) => total + chunk.size, 0);
    let processedBytes = 0;
    let processedChunks = 0;
    let lastReportTime = Date.now();
    let lastProcessedBytes = 0;

    // 创建SparkMD5实例进行增量哈希计算
    const spark = new SparkMD5.ArrayBuffer();

    // 跟踪下一个要添加到哈希计算器的块索引
    let nextChunkToHash = 0;

    // 存储已处理但尚未添加到哈希计算器的块
    const processedBuffers = new Map();

    // 创建一个worker池
    const workers = [];
    const availableWorkers = [];
    const pendingChunks = [...chunks.keys()];

    // 清理函数
    const cleanup = () => {
      workers.forEach(worker => worker.terminate());
      processedBuffers.clear();
    };

    // 处理取消信号
    const abortHandler = () => {
      cleanup();
      reject(new Error('Calculation was aborted'));
    };

    signal?.addEventListener('abort', abortHandler);

    // 节流的进度报告函数
    const reportProgress = throttle((progress, speed) => {
      onProgress({
        progress,
        processedChunks,
        totalChunks: chunks.length,
        processedBytes,
        totalBytes,
        speed
      });
    }, 500);

    // 尝试按顺序将已处理的块添加到哈希计算器
    const tryAppendToHash = () => {
      while (processedBuffers.has(nextChunkToHash)) {
        // 将下一个块添加到哈希计算器
        spark.append(processedBuffers.get(nextChunkToHash));

        // 释放内存
        processedBuffers.delete(nextChunkToHash);

        // 移动到下一个块
        nextChunkToHash++;

        // 如果所有块都已添加到哈希计算器，完成计算
        if (nextChunkToHash === chunks.length) {
          const finalHash = spark.end();
          cleanup();
          signal?.removeEventListener('abort', abortHandler);
          resolve(finalHash);
          return;
        }
      }
    };

    // 创建worker的工厂函数
    const createWorker = () => {
      const worker = new Worker(new URL('./hashWorker.js', import.meta.url), { type: 'module' });

      worker.onmessage = (e) => {
        const { success, chunkIndex, buffer, size, error } = e.data;

        if (success) {
          // 存储处理结果
          processedBuffers.set(chunkIndex, buffer);
          processedBytes += size;
          processedChunks++;

          // 尝试按顺序添加到哈希计算器
          tryAppendToHash();

          // 计算并报告进度
          const now = Date.now();
          const timeDiff = (now - lastReportTime) / 1000;
          if (timeDiff >= 0.5 || processedChunks === chunks.length) {
            const progress = Math.round((processedBytes / totalBytes) * 100);
            const bytesDiff = processedBytes - lastProcessedBytes;
            const speed = timeDiff > 0 ? Math.round((bytesDiff / timeDiff / 1024 / 1024) * 100) / 100 : 0;

            reportProgress(progress, speed);

            lastReportTime = now;
            lastProcessedBytes = processedBytes;
          }

          // 将worker放回可用池
          availableWorkers.push(worker);

          // 继续处理下一个块
          processNextChunk();
        } else {
          console.error(`处理块 ${chunkIndex} 时出错:`, error);
          cleanup();
          signal?.removeEventListener('abort', abortHandler);
          reject(new Error(`处理文件块时出错: ${error}`));
        }
      };

      worker.onerror = (error) => {
        console.error('Worker错误:', error);
        cleanup();
        signal?.removeEventListener('abort', abortHandler);
        reject(new Error(`Worker错误: ${error.message}`));
      };

      return worker;
    };

    // 处理下一个待处理的块
    const processNextChunk = () => {
      if (pendingChunks.length === 0 || availableWorkers.length === 0) {
        return;
      }

      const chunkIndex = pendingChunks.shift();
      const worker = availableWorkers.pop();

      worker.postMessage({
        chunk: chunks[chunkIndex],
        chunkIndex,
        totalChunks: chunks.length
      });
    };

    // 创建worker池
    const actualWorkerCount = Math.min(workerCount, chunks.length);
    for (let i = 0; i < actualWorkerCount; i++) {
      const worker = createWorker();
      workers.push(worker);
      availableWorkers.push(worker);
    }

    // 开始处理块
    for (let i = 0; i < actualWorkerCount; i++) {
      processNextChunk();
    }
  });
};

/**
 * 节流函数
 * @param {Function} func - 要节流的函数
 * @param {number} wait - 等待时间（毫秒）
 * @returns {Function} 节流后的函数
 */
const throttle = (func, wait) => {
  let timeout;
  let previous = 0;

  return function executedFunction(...args) {
    const now = Date.now();
    const remaining = wait - (now - previous);

    if (remaining <= 0 || remaining > wait) {
      if (timeout) {
        clearTimeout(timeout);
        timeout = null;
      }
      previous = now;
      func.apply(this, args);
    } else if (!timeout) {
      timeout = setTimeout(() => {
        previous = Date.now();
        timeout = null;
        func.apply(this, args);
      }, remaining);
    }
  };
}


