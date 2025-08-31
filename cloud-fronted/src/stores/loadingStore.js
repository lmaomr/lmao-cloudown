import { ref } from 'vue';
import { defineStore } from 'pinia';

export const useLoadingStore = defineStore('loading', () => {
  // 全局加载状态
  const isLoading = ref(false);

  // 加载文本
  const loadingText = ref('加载中...');

  // 加载类型
  const loadingType = ref('spinner');

  // 是否全屏加载
  const isFullscreen = ref(false);

  // 设置加载状态
  const startLoading = (options = {}) => {
    if (options.text) loadingText.value = options.text;
    if (options.type) loadingType.value = options.type;
    isFullscreen.value = options.fullscreen !== undefined ? options.fullscreen : false;
    isLoading.value = true;
  };

  // 结束加载
  const endLoading = () => {
    isLoading.value = false;
  };

  // 创建一个带超时的加载函数
  const withLoading = async (fn, options = {}) => {
    try {
      startLoading(options);
      return await fn();
    } finally {
      endLoading();
    }
  };

  return {
    isLoading,
    loadingText,
    loadingType,
    isFullscreen,
    startLoading,
    endLoading,
    withLoading
  };
});
