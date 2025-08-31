import { ref, watch } from 'vue';
import { defineStore } from 'pinia';

const useSettingStore = defineStore('setting', () => {
  // 尝试从localStorage读取主题设置，如果没有则默认为浅色主题
  const isDarkTheme = ref(localStorage.getItem('darkTheme') === 'true' || false);

  const isLogoutModalVisible = ref(false);

  // 窗口宽度
  const screenWidth = ref(window.innerWidth);

  // 设备类型
  const screenType = ref('desktop');

  // 监听主题变化并应用到DOM
  watch(isDarkTheme, (newValue) => {
    // 保存到localStorage
    localStorage.setItem('darkTheme', newValue);

    // 应用主题到html元素
    if (newValue) {
      document.documentElement.setAttribute('data-theme', 'dark');
    } else {
      document.documentElement.removeAttribute('data-theme');
    }
  }, { immediate: true }); // 立即执行一次以应用初始主题

  // 检测系统主题偏好
  const detectSystemTheme = () => {
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    isDarkTheme.value = prefersDark;
  };

  const detectDeviceType = () => {
    const { userAgent, maxTouchPoints } = navigator;
    const screenWidth = window.innerWidth;

    // 优先用屏幕宽度 + 触摸支持判断（比 UA 更可靠）
    if (maxTouchPoints > 0 && screenWidth < 768) {
      screenType.value = 'mobile';
    }
    // iPad 或中等屏幕宽度设备
    else if (
      /iPad|tablet/i.test(userAgent) ||
      (maxTouchPoints > 0 && screenWidth < 1024)
    ) {
      screenType.value = 'tablet';
    }
    // 桌面设备
    else {
      screenType.value = 'desktop';
    }
  };

  return {
    isLogoutModalVisible,
    isDarkTheme,
    screenWidth,
    screenType,
    toggleTheme: () => {
      isDarkTheme.value = !isDarkTheme.value;
    },
    detectSystemTheme,
    detectDeviceType,
  };
});

export default useSettingStore;
