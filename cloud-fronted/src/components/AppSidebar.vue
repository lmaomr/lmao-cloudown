<script setup>
import { ref } from 'vue';
import usePathStore from '@/stores/pathStore';
import NavMenu from './sidebar/NavMenu.vue';
import StorageInfo from './sidebar/StorageInfo.vue';
import UserInfo from './sidebar/UserInfo.vue';
import { useUserStore } from '@/stores/userManageStore';

const userStore = useUserStore();
const pathStore = usePathStore();
const menuItems = ref(pathStore.menuItems);

const handleMenuAction = (active) => {
  pathStore.setActiveMenu(active.title, active.section);
};

</script>

<template>
  <aside class="sidebar-container">
    <div class="logo">
      <img src="/logo.png" alt="Logo" />
      <span class="logo-text">Cloudown</span>
    </div>
    <div class="nav-menu">
      <nav-menu :menu-items="menuItems" :active-section="pathStore.activeMenu.section" @menuAction="handleMenuAction" />
    </div>
    <!-- 存储信息组件 -->
    <div class="storage-info">
      <storage-info class="storage-info" :used="userStore.user?.usedCapacity" :total="userStore.user?.totalCapacity"
        :percentage="userStore.user?.usedCapacity / userStore.user?.totalCapacity * 100" />
    </div>
    <!-- 分割线 -->
    <div class="nav-divider"></div>
    <!-- 用户信息组件 -->
    <div class="user-info">
      <user-info />
    </div>
  </aside>
</template>

<style scoped>
.sidebar-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: var(--card-bg);
  border-right: 1px solid var(--border-color);
  box-shadow: var(--shadow-color);
}

.logo {
  display: flex;
  align-items: center;
  padding: 0.5rem 0 0.5rem 1rem;
}
.logo img {
  width: 4.8rem;
  height: 4rem;
  border-radius: 50%;
  object-fit: contain;
}


.logo-text {
  font-size: 1.6rem;
  font-weight: 700;
  color: var(--primary-color);
}

.nav-menu {
  flex: 1;
  overflow-y: auto;
}

@media (max-width: 768px) {
  .storage-info {
    margin-bottom: 1rem;
  }


  .user-info {
    display: none;
  }

}
</style>
