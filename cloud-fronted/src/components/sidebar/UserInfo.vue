<script setup>
import { watchEffect, inject } from 'vue';
import { useUserStore } from '@/stores/userManageStore';
import useSettingStore from '@/stores/settingStore';
import ModalBox from '@/components/modal/ModalBox.vue';

const userStore = useUserStore();
const setting = useSettingStore();

const handleClose = () => {
  setting.isLogoutModalVisible = false;
}

const handleLogout = () => {
  userStore.logout().then(() => {
    setting.isLogoutModalVisible = false;
  }).catch(err => {
    console.error('登出失败！:', err);
  });
}

const openUserInfoPanel = inject('openUserInfoPanel');

const handleOpenUserInfoPanel = (e) => {
  if (!e.target.tagName === 'BUTTON' || !e.target.closest('button')) {
    e.stopPropagation(); // 阻止事件冒泡，避免触发其他点击事件
    openUserInfoPanel();
  }
};

// 监听登录状态变化，当登录状态改变时自动获取用户信息
watchEffect(() => {
  if (userStore.isLoggedIn && !userStore.user) {
    userStore.getUserInfo();
  }
});
</script>

<template>
  <!-- 桌面版用户信息区域 - 只在大屏幕显示 -->
  <div class="user-info" @click="handleOpenUserInfoPanel">
    <div class="avatar">
      <img v-if="userStore.user.avatarUrl" :src="userStore.user.avatarUrl" alt="用户头像" />
      <span v-else>{{ userStore.user.nickname?.substring(0, 1) || 'u' }}</span>
    </div>
    <div class="user-details">
      <div class="nickname">{{ userStore.user?.nickname || 'user' }}</div>
      <div class="user-role">{{ userStore.user?.role.name || '用户' }}</div>
    </div>
    <button class="logout-btn" title="退出登录" aria-label="退出登录" @click="setting.isLogoutModalVisible = true">
      <i class="fas fa-sign-out-alt"></i>
    </button>
  </div>
  <Teleport to="body">
    <ModalBox ui="fas fa-sign-out-alt" title="退出登录" :visible="setting.isLogoutModalVisible" @confirm="handleLogout"
      @close="handleClose">
      <div class="modal-body">
        <i class="fas fa-question-circle"></i>
        <div>
          <h3>确定要退出登录吗？</h3>
          <p>退出后需要重新登录才能访问您的文件</p>
        </div>
      </div>
    </ModalBox>
  </Teleport>
</template>

<style scoped>
.user-info {
  display: flex;
  align-items: center;
  margin-top: 12px;
  padding: 12px 16px;
  border-top: 1px solid var(--border-color);
  gap: 0.6rem;
}

.user-info:hover {
  background-color: var(--hover-bg);
}

.avatar,
.avatar img {
  width: 2.6rem;
  height: 2.6rem;
  border-radius: 50%;
  background-color: var(--primary-color);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  font-size: 1rem;
  text-transform: uppercase;
}

.user-details {
  flex: 1;
  overflow: hidden;
}

.nickname {
  font-weight: 500;
  font-size: 14px;
  color: var(--text-color);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 12px;
  color: var(--text-secondary);
}

.logout-btn {
  background: none;
  border: none;
  color: var(--i-color);
  cursor: pointer;
  font-size: 16px;
}

.logout-btn:hover {
  background-color: var(--hover-bg);
  color: var(--danger-color);
}

.modal-body {
  display: flex;
  align-items: center;
  gap: 1.2rem;
}


.modal-body .fas {
  color: var(--primary-color);
  font-size: 1.6rem;
}

@media (max-width: 768px) {
  .user-info {
    border-top: none;
    margin: 0;
    padding: 0;
  }

  .user-details {
    display: none;
    /* 在小屏幕上隐藏用户详情 */
  }

  .logout-btn {
    display: none;
    /* 在小屏幕上隐藏退出按钮 */
  }

}
</style>
