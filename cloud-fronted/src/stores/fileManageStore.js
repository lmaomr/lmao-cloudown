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
    if (pathStore.isSearchMode) {
      return;
    }
    const response = await fileApi.getFileList(pathStore.getBreadcrumbPath(), pathStore.activeMenu.section, sortOptions.value);
    fileList.value = response.data;
    safeLocalStorage.set('fileList', JSON.stringify(fileList.value));
  };

  const setSortOptions = (options) => {
    sortOptions.value = options;
    safeLocalStorage.set('sortOptions', options);
  }

  const createFolder = async (folderName) => {
    const response = await fileApi.createFolder(folderName, pathStore.getBreadcrumbPath());
    if (response.code === 200) {
      console.log('文件夹创建成功', response.data);
      getFileList(); // 刷新文件列表
    } else {
      console.error('文件夹创建失败', response.message);
    }
  };

  const createTextFile = async (fileName) => {
    const response = await fileApi.createTextFile(fileName, pathStore.getBreadcrumbPath());
    if (response.code === 200) {
      console.log('文本文档创建成功', response.data);
      getFileList(); // 刷新文件列表
    } else {
      console.error('文本文档创建失败', response.message);
    }

  };

  const downloadFile = async (file) => {
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
  };

  const deleteFile = async (file) => {
    console.log("delete fileId:", file.id);
    const response = await fileApi.deleteFile(file.id);
    if (response.code === 200) {
      console.log('文件删除成功', response.data);
      getFileList(); // 刷新文件列表
      return true;
    } else {
      console.error('文件删除失败', response.message);
    }
  }

  const searchFiles = async (searchQuery) => {
    const response = await fileApi.searchFiles(searchQuery);
    if (response.code === 200) {
      fileList.value = response.data;
      safeLocalStorage.set('fileList', JSON.stringify(fileList.value));
      return true;
    } else {
      console.error('文件搜索失败', response.message);
      return false;
    }
  }

  return {
    fileList,
    sortOptions,
    getFileList,
    setSortOptions,
    createFolder,
    createTextFile,
    downloadFile,
    deleteFile,
    searchFiles
  }
})

export default useFileManageStore;
