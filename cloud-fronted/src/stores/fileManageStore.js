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

  const uploadFile = async (file) => {
    try {
      const response = await fileApi.uploadFile(file);
      if (response.code === 200) {
        console.log('文件上传成功', response.data);
        getFileList(); // 刷新文件列表
      } else {
        console.error('文件上传失败', response.message);
      }
    } catch (err) {
      console.error('文件上传异常', err);
    }
  };

  const setSortOptions = (options) => {
    sortOptions.value = options;
    safeLocalStorage.set('sortOptions', options);
  }

  return {
    fileList,
    sortOptions,
    getFileList,
    uploadFile,
    setSortOptions
  }
})

export default useFileManageStore;
