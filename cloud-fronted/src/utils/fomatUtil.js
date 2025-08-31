/**
 * 格式化字节大小为易读格式
 * @param {number} bytes - 字节数
 * @param {number} decimals - 小数位数
 * @returns {string} 格式化后的字符串
 */
export const formatFileSize = (bytes, decimals = 2) =>{
  if (bytes === 0) return '0B';

  const k = 1024;
  const dm = decimals < 0 ? 0 : decimals;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));

  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))}${sizes[i]}`;
}

/**
 * 格式化速度为易读格式
 * @param {number} bytesPerSecond - 每秒字节数
 * @returns {string} 格式化后的速度字符串
 */
export const formatSpeed = (bytesPerSecond) => {
  return `${formatFileSize(bytesPerSecond)}/s`;
}

/**
 * 计算预估剩余时间
 * @param {number} processedBytes - 已处理字节数
 * @param {number} totalBytes - 总字节数
 * @param {number} speed - 处理速度（字节/秒）
 * @returns {string} 格式化后的时间字符串
 */
export const formatTime = (processedBytes, totalBytes, speed) => {
  if (speed === 0 || processedBytes >= totalBytes) {
    return '未知';
  }

  const remainingBytes = totalBytes - processedBytes;
  const remainingSeconds = Math.ceil(remainingBytes / speed);

  if (remainingSeconds < 60) {
    return `${remainingSeconds}秒`;
  } else if (remainingSeconds < 3600) {
    const minutes = Math.floor(remainingSeconds / 60);
    const seconds = remainingSeconds % 60;
    return `${minutes}分${seconds}秒`;
  } else {
    const hours = Math.floor(remainingSeconds / 3600);
    const minutes = Math.floor((remainingSeconds % 3600) / 60);
    return `${hours}小时${minutes}分`;
  }
}

export const formatDate = (date) => {
  if (!(date instanceof Date)) {
    date = new Date(date);
  }
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

export const formatFileType = (fileName) => {
  if (!fileName) return { type: 'unknown', subtype: '', icon: 'fa-solid fa-file' }

  const extension = fileName.includes('.')
     ? fileName.split('.').pop().toLowerCase()
     : '';
  const types = {
    //文件夹
    folder: {
      extensions: ['folder', 'dir', 'directory'],
      icon: 'fa-solid fa-folder'
    },
    // 图片类型
    image: {
      extensions: ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg', 'ico'],
      icon: 'fa-solid fa-image'
    },
    // 文档类型
    word: {
      extensions: ['doc', 'docx', 'odt', 'rtf'],
      icon: 'fas fa-file-word'
    },
    // PDF 文件
    pdf: {
      extensions: ['pdf'],
      icon: 'fas fa-file-pdf'
    },
    // 表格文件
    spreadsheet: {
      extensions: ['xls', 'xlsx', 'ods', 'csv'],
      icon: 'fas fa-file-excel'
    },
    // 演示文稿文件
    presentation: {
      extensions: ['ppt', 'pptx', 'odp'],
      icon: 'fas fa-file-powerpoint'
    },
    // 文本文件
    text: {
      extensions: ['txt', 'md', 'log', 'csv'],
      icon: 'fas fa-file-alt'
    },
    document: {
      extensions: ['pdf', 'doc', 'docx', 'ppt', 'pptx', 'xls', 'xlsx', 'txt', 'rtf'],
      icon: 'fas fa-file-alt'
    },
    // 压缩文件
    archive: {
      extensions: ['zip', 'rar', '7z', 'tar', 'gz', 'bz2'],
      icon: 'fa-solid fa-file-zipper'
    },
    // 视频文件
    video: {
      extensions: ['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv', 'webm'],
      icon: 'fa-solid fa-video'
    },
    // 音频文件
    audio: {
      extensions: ['mp3', 'wav', 'ogg', 'flac', 'aac', 'm4a'],
      icon: 'fa-solid fa-music'
    },
    // 代码文件
    code: {
      extensions: ['js', 'jsx', 'ts', 'tsx', 'html', 'css', 'scss', 'json', 'vue', 'py', 'java', 'php', 'cpp', 'c', 'h', 'sh'],
      icon: 'fa-solid fa-code'
    },
    // 可执行文件
    executable: {
      extensions: ['exe', 'bat', 'sh', 'cmd', 'msi'],
      icon: 'fab fa-windows'
    },
  }

  for (const [type, info] of Object.entries(types)) {
    if (info.extensions.includes(extension)) {
      return {
        type,
        subtype: extension,
        icon: info.icon
      }
    }
  }

  // 默认未知文件类型
  return {
    type: 'other',
    subtype: extension,
    icon: 'fa-solid fa-file'
  }
}

export default {
  formatFileSize,
  formatSpeed,
  formatTime,
  formatDate
};
