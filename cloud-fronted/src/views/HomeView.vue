<script setup>
import { onMounted, ref, provide, nextTick } from 'vue';
import { onClickOutside } from '@vueuse/core';
import AppSidebar from '@/components/AppSidebar.vue';
import AppMain from '@/components/AppMain.vue';
import AppUserInfo from '@/components/AppUserInfo.vue';
import { useLoadingStore } from '@/stores/loadingStore';
import { useUserStore } from '@/stores/userManageStore';
import useFileManageStore from '@/stores/fileManageStore';


const isCollapse = ref(false);
const userInfoPanel = ref({
  isPanelOpen: false,
  closePanel: () => {}
});
const loadingStore = useLoadingStore();
const userStore = useUserStore();
const fileManageStore = useFileManageStore();

const openUserInfoPanel = () => {
  if (userInfoPanel.value) {
    userInfoPanel.value.isPanelOpen = true;
  }
};

// 提供方法给子组件
provide('openUserInfoPanel', openUserInfoPanel);

onClickOutside(userInfoPanel, (event) => {
  if (userInfoPanel.value?.isPanelOpen && // 面板打开
    !event.target.closest('.user-info-panel') && // 点击不在用户信息面板内
    !event.target.closest('button') &&  // 不是按钮或按钮的子元素
    !event.target.closest('li') &&
    !event.target.closest('.modal') && // 不关闭模态框内的点击
    !event.target.closest('.modal-overlay')) { // 不关闭模态框遮罩的点击
    userInfoPanel.value.closePanel();
  }
});

onMounted(async () => {
  // 显示加载动画
  loadingStore.isFullscreen = true;
  try {
    // 并行执行所有初始化请求
    await Promise.all([
      userStore.getUserInfo(),
      fileManageStore.getFileList()
    ]);
    await nextTick(); // 确保 DOM 更新完成
  } catch (error) {
    console.error('初始化失败:', error);
    // 可以在这里添加错误处理逻辑
  } finally {
    // 平滑隐藏加载动画
    loadingStore.isFullscreen = false;
  }
});
</script>

<template>
  <!-- 加载中状态 -->
  <div class="container">
    <div class="sidebar" :class="{ collapsed: isCollapse }">
      <AppSidebar />
    </div>
    <div class="main" :class="{ expanded: isCollapse }">
      <AppMain @toggleSidebar="isCollapse = !isCollapse" />
    </div>
    <div class="sidebar-backdrop" v-if="isCollapse" @click="isCollapse = !isCollapse"></div>
    <div class="user-info-panel">
      <AppUserInfo ref="userInfoPanel" />
    </div>
  </div>
</template>

<style scoped>
.container {
  display: flex;
  width: 100%;
  height: 100%;
}

.sidebar {
  z-index: var(--z-index-sidebar);
  width: var(--sidebar-width);
  transition: transform var(--transition-speed) cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar.collapsed {
  transform: translateX(-100%);
}

.main {
  flex: 1;
  padding: 1rem 1.2rem;
  transition: margin-left var(--transition-speed) cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.main.expanded {
  margin-left: calc(-1 * var(--sidebar-width));
}

.sidebar-backdrop {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: calc(var(--z-index-sidebar) - 1);
}

@media (max-width: 768px) {
  .container {
    min-width: 29rem;
    /* min-height: 66.7rem; */
  }

  .sidebar {
    transform: translateX(-100%);
    position: fixed;
    top: 0;
    left: 0;
    height: 100vh;
    box-shadow: var(--shadow-lg);
  }

  .sidebar.collapsed {
    transform: translateX(0);
  }

  .main {
    margin-left: 0;
    width: 100%;
    padding: 0.75rem;
    display: flex;
    flex-direction: column;
    align-items: center;
    /* 使内容水平居中 */
  }

  .main.expanded {
    margin-left: 0;
  }

  .sidebar-backdrop {
    display: block;
  }
}
</style>
