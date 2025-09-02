// stores/userStore.ts
import { ref } from 'vue';
import { defineStore } from 'pinia';
import fileApi from '@/api/file';
import { safeLocalStorage } from '@/utils/storage';
import usePathStore from './pathStore';

const useFileManageStore = defineStore('fileManage', () => {
  const fileList = ref(JSON.parse(safeLocalStorage.get('fileList')) || []);
  const pathStore = usePathStore();
  const sortOptions = ref(safeLocalStorage.get('sortOptions') || 'time-desc');

  const getFileList = async () => {
    try {
      const response = await fileApi.getFileList(pathStore.getBreadcrumbPath(), pathStore.activeMenu.section, sortOptions.value);
      fileList.value = response.data;
      safeLocalStorage.set('fileList', JSON.stringify(fileList.value));
    } catch (err) {
      console.error('获取文件列表失败', err);
    }
  };

  const setSortOptions = (options) => {
    sortOptions.value = options;
    safeLocalStorage.set('sortOptions', options);
  }

  const createFolder = async (folderName) => {
    try {
      const response = await fileApi.createFolder(folderName, pathStore.getBreadcrumbPath());
      if (response.code === 200) {
        console.log('文件夹创建成功', response.data);
        getFileList(); // 刷新文件列表
      } else {
        console.error('文件夹创建失败', response.message);
      }
    } catch (err) {
      console.error('文件夹创建异常', err);
    }
  };

  const createTextFile = async (fileName) => {
    try {
      const response = await fileApi.createTextFile(fileName, pathStore.getBreadcrumbPath());
      if (response.code === 200) {
        console.log('文本文档创建成功', response.data);
        getFileList(); // 刷新文件列表
      } else {
        console.error('文本文档创建失败', response.message);
      }
    } catch (err) {
      console.error('文本文档创建异常', err);
    }
  };

  const downloadFile = async (file) => {
    try {
      // 获取当前完整路径
      const currentPath = pathStore.breadcrumbPath
        .map(item => pathStore.getActiveElement(item).label)
        .join('/');
      
      const filePath = `${currentPath}/${file.name}`.replace(/^\/+/, '');
      
      // 调用下载API
      const response = await fileApi.downloadFile(file.id, file.name);
      
      // 创建Blob对象
      const blob = new Blob([response.data], {
        type: response.headers['content-type']
      });
      
      // 创建下载链接
      const downloadUrl = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = downloadUrl;
      link.download = file.name;
      
      // 添加到页面并触发点击
      document.body.appendChild(link);
      link.click();
      
      // 清理
      window.URL.revokeObjectURL(downloadUrl);
      document.body.removeChild(link);
      
      return true;
    } catch (err) {
      console.error('下载文件失败:', err);
      throw err;
    }
  };

  return {
    fileList,
    sortOptions,
    getFileList,
    setSortOptions,
    createFolder,
    createTextFile,
    downloadFile,
  }
})

export default useFileManageStore;
