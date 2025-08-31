<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/userManageStore'
import useFileManageStore from '@/stores/fileManageStore';
import useSettingStore from '@/stores/settingStore';
import { formatDate, formatFileSize } from '@/utils/fomatUtil'
import toast from '@/utils/toast';

// 状态管理
const userStore = useUserStore()
const fileStore = useFileManageStore()
const settingStore = useSettingStore()

// 响应式数据
const isPanelOpen = ref(false)

// 计算属性
const user = computed(() => userStore.user)

// 方法
const closePanel = () => {
  isPanelOpen.value = false
}

const openLogoutModal = () => {
  settingStore.isLogoutModalVisible = true
}

const uploadAvatarVisible = ref(false);
const maxAvatarSize = 20 * 1024 * 1024; // 20MB
const avatarFile = ref(null);
const showAvatarPreview = ref(false);
const avatarImageUrl = ref('');
const hintText = ref('');

const selectFile = () => {
  const fileInput = document.querySelector('.selectImage-btn input[type="file"]');
  fileInput?.click();
};

const resetAvatarForm = (isClose = true, file = null, text = '') => {
  showAvatarPreview.value = !isClose;
  hintText.value = text;

  if (avatarImageUrl.value) {
    URL.revokeObjectURL(avatarImageUrl.value);
  }
  avatarImageUrl.value = file ? URL.createObjectURL(file) : '';
  if (isClose) {
    avatarFile.value = null;
    const fileInput = document.querySelector('.selectImage-btn input[type="file"]');
    if (fileInput) fileInput.value = '';
  }
};

const handleFileChange = (event) => {
  const file = event.target.files[0];
  if (!file) {
    resetAvatarForm(true);
    return;
  }

  if (!file.type.startsWith('image/')) {
    toast.warning('文件格式有误', '请选择一张图片作为头像');
    return;
  }

  if (file.size > maxAvatarSize) {
    toast.warning('文件过大', `头像图片大小不能超过${formatFileSize(maxAvatarSize)}`);
    event.target.value = '';
    return;
  }

  avatarFile.value = file;
  resetAvatarForm(false, file, `已选择: ${file.name} (${formatFileSize(file.size)})`);
};

const updateAvatarImg = async () => {
  if (!avatarFile.value) {
    toast.warning('没有选择文件', '请先选择一张图片作为头像');
    return;
  }
  const file = avatarFile.value.files[0];
  try {
    await userStore.updateAvatarImg(file);
    toast.success('头像上传成功', '您的头像已更新');
  } catch (error) {
    console.error('上传头像失败:', error);
    toast.error('头像上传失败', '请稍后重试');
    return;
  }
  uploadAvatarVisible.value = false;
  resetAvatarForm(true);
};

const editNicknameVisible = ref(false);
const newNickname = ref('');

const updateNickname = async () => {
  console.log('新昵称:', typeof newNickname.value);
  const trimmedNickname = newNickname.value.trim();

  if (trimmedNickname === '') {
    toast.warning('昵称不能为空', '请输入新的昵称');
    return;
  }

  if (trimmedNickname.length < 2 || trimmedNickname.length > 20) {
    toast.warning('昵称长度不合法', '昵称长度应为2-20个字符');
    return;
  }

  if (/^[a-zA-Z0-9_]+$/.test(trimmedNickname)) {
    try {
      await userStore.updateUserNickname(trimmedNickname);
      toast.success('昵称修改成功', '您的昵称已更新');
    } catch (error) {
      console.error('修改昵称失败:', error);
      toast.error('昵称修改失败', '请稍后重试');
    }finally {
      editNicknameVisible.value = false;
      newNickname.value = '';
    }
  } else {
    toast.warning('昵称格式不合法', '昵称只能包含字母、数字和下划线');
  }
};


const changePasswordVisible = ref(false);
const oldPassword = ref('');
const newPassword = ref(''); // 新密码
const confirmPassword = ref(''); // 确认密码
const showPasswordError = ref(false); // 控制提示显示的状态变量

//修改密码
const updatePassword = async () => {
  if (!oldPassword.value || !newPassword.value || !confirmPassword.value) {
    toast.warning('密码不能为空', '请填写所有密码字段');
    return;
  }
  if (/^(?=.*[a-zA-Z])(?=.*\d).{6,20}$/.test(newPassword.value) === false) {
    toast.warning('密码格式不合法', '请输入6-20位的密码，且包含字母和数字');
    return;
  }
  if (oldPassword.value === newPassword.value) {
    toast.warning('新密码不能与旧密码相同', '请设置一个新的密码');
    return;
  }
  // 检查新密码和确认密码是否一致
  if (newPassword.value !== confirmPassword.value) {
    toast.warning('密码不一致', '请确保两次输入的新密码一致');
    showPasswordError.value = true; // 显示密码不一致的提示
    return;
  }
  try {
    console.log('修改密码:', oldPassword.value, newPassword.value, confirmPassword.value);
    await userStore.updateUserPassword(oldPassword.value, newPassword.value);
    toast.success('密码修改成功', '请重新登录');
    changePasswordVisible.value = false; // 关闭修改密码面板
  } catch (error) {
    console.error('修改密码异常:', error);
  } finally {
    // 重置表单状态
    oldPassword.value = '';
    newPassword.value = '';
    showPasswordError.value = false; // 重置密码错误提示状态
  }
};

defineExpose({
  isPanelOpen,
  closePanel
})
</script>

<template>
  <div class="user-profile-panel" :class="{ active: isPanelOpen }">
    <div class="user-profile-header">
      <h3>用户信息</h3>
      <button class="close-btn" @click="closePanel" aria-label="关闭">
        <i class="fas fa-times"></i>
      </button>
    </div>

    <div class="user-profile-content">
      <!-- 头像部分 -->
      <div class="avatar-wrapper">
        <img v-if="user.avatarUrl" :src="userStore.user.avatarUrl" alt="用户头像" />
        <span v-else>{{ user.nickname?.substring(0, 1) || 'u' }}</span>
        <button class="edit-avatar-btn" title="修改头像" @click="uploadAvatarVisible = true">
          <i class="fas fa-camera"></i>
        </button>
      </div>

      <!-- 用户信息详情 -->
      <div class="user-info-details">
        <!-- 可编辑昵称 -->
        <div class="info-item">
          <div>
            <span class="info-label">昵称</span>
            <div class="info-value-container">
              <span class="info-value">{{ user.nickname }}</span>
              <button class="edit-nickname-btn" @click="editNicknameVisible = true" title="修改昵称">
                <i class="fas fa-edit"></i>
              </button>
            </div>
          </div>
        </div>

        <!-- 其他用户信息 -->
        <div class="info-item">
          <span class="info-label">邮箱</span>
          <span class="info-value">{{ user.email }}</span>
        </div>

        <div class="info-item">
          <span class="info-label">角色</span>
          <span class="info-value">{{ user.role.name }}</span>
        </div>

        <div class="info-item">
          <span class="info-label">文件数量</span>
          <span class="info-value">{{ fileStore.fileList.length }}</span>
        </div>

        <div class="info-item">
          <span class="info-label">注册时间</span>
          <span class="info-value">{{ formatDate(user.createTime) }}</span>
        </div>
      </div>

      <!-- 用户操作按钮 -->
      <div class="user-actions">
        <button class="btn-secondary" @click="changePasswordVisible = true">
          <i class="fas fa-key"></i> 修改密码
        </button>
        <button class="btn-danger" @click="openLogoutModal">
          <i class="fas fa-sign-out-alt"></i> 退出登录
        </button>
      </div>
    </div>
  </div>

  <Teleport to="body">
    <ModalBox ui="fas fa-camera" title="上传头像" :visible="uploadAvatarVisible" @confirm="updateAvatarImg"
      @close="uploadAvatarVisible = false">
      <div class="upload-avatar-form">
        <div class="form-group">
          <p>请选择一张图片作为您的头像</p>
          <p class="form-hint">支持 jpg, jpeg, png 格式，大小不超过 {{
            formatFileSize(maxAvatarSize) }}</p>
        </div>
        <div class="form-group">
          <button class="selectImage-btn" @click="selectFile" aria-label="选择图片">
            <input type="file" accept="image/*" @change="handleFileChange" style="display: none;" ref="avatarFile">
            <i class="fas fa-file-image"></i> 选择图片
          </button>
          <div v-show="showAvatarPreview" class="avatar-preview" style="margin-top: 15px;">
            <img :src="avatarImageUrl" alt="头像预览">
            <div class="form-hint">{{ hintText }}</div>
          </div>
        </div>
      </div>
    </ModalBox>
    <ModalBox ui="fas fa-edit" title="修改昵称" :visible="editNicknameVisible" @confirm="updateNickname"
      @close="editNicknameVisible = false">
      <div class="edit-nickname-form">
        <div class="form-group">
          <label for="newNickname" class="form-label">新昵称</label>
          <input type="text" class="form-input" placeholder="请输入新昵称" v-model="newNickname" />
          <p class="form-hint">昵称长度为2-20个字符，支持字母、数字、下划线</p>
        </div>
      </div>
    </ModalBox>
    <ModalBox ui="fas fa-key" title="修改密码" :visible="changePasswordVisible" @confirm="updatePassword"
      @close="changePasswordVisible = false">
      <div class="change-password-form">
        <div class="form-group">
          <label for="oldPassword" class="form-label">旧密码</label>
          <input type="password" class="form-input" placeholder="请输入旧密码" v-model="oldPassword" />
        </div>
        <div class="form-group">
          <label for="newPassword" class="form-label">新密码</label>
          <input type="password" class="form-input" placeholder="请输入新密码" v-model="newPassword" />
          <p class="form-hint">密码长度为6-20位，包含字母和数字</p>
        </div>
        <div class="form-group">
          <label for="confirmPassword" class="form-label">确认密码</label>
          <input type="password" class="form-input" placeholder="请再次输入新密码" v-model="confirmPassword" />
          <p style="color: red;" class="form-hint" v-if="newPassword !== confirmPassword && showPasswordError">
            两次输入的密码不一致</p>
        </div>
      </div>
    </ModalBox>
  </Teleport>
</template>

<style scoped>
/* 用户信息面板样式 */
.user-profile-panel {
  position: fixed;
  top: 0;
  right: -360px;
  width: 360px;
  height: 100vh;
  background-color: var(--card-bg);
  box-shadow: -2px 0 10px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  transition: right 0.3s ease;
  overflow-y: auto;
  padding: 0;
  display: flex;
  flex-direction: column;
}

.user-profile-panel.active {
  right: 0;
}

.user-profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);
}

.user-profile-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: var(--text-color);
}

.close-btn {
  background: transparent;
  border: none;
  color: var(--text-secondary);
  font-size: 16px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.close-btn:hover {
  background-color: var(--hover-bg);
  color: var(--text-color);
}

.user-profile-content {
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.avatar-wrapper span,
.avatar-wrapper img {
  width: 9rem;
  height: 9rem;
  border-radius: 50%;
  background-color: var(--primary-color);
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 42px;
  font-weight: 500;
  color: white;
  text-transform: uppercase
}

.user-info-details {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 14px;
  color: var(--text-secondary);
}

.info-value {
  font-size: 16px;
  color: var(--text-color);
  font-weight: 500;
}

.user-actions {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.user-actions button {
  width: 100%;
  padding: 0.8rem 1.2rem;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: start;
  gap: 8px;
}

.btn-secondary {
  background-color: var(--input-bg);
  color: var(--text-color);
  box-shadow: none;
}

.btn-danger {
  background-color: var(--danger-color);
  color: white;
  box-shadow: none;
}

/* 头像编辑按钮 */
.avatar-wrapper {
  position: relative;
  display: inline-block;
}

.edit-avatar-btn {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 2rem;
  height: 2rem;
  background-color: var(--primary-color);
  border: 2px solid var(--card-bg);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.2s;
  padding: 0;
}

.info-value-container {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.edit-nickname-btn {
  background: transparent;
  border: none;
  color: var(--primary-color);
  padding: 0.2rem 0 0 0;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 24px;
  min-height: 24px;
}

.edit-nickname-btn:hover {
  background-color: rgba(25, 118, 210, 0.1);
}

.form-hint {
  font-size: 0.8rem;
  color: var(--text-secondary);
  margin-top: 0.5rem;
}

.selectImage-btn {
  margin-top: 1.2rem;
  color: var(--text-color);
  background: var(--input-bg);
}

.avatar-preview img {
  max-width: 100%;
  max-height: 200px;
  border-radius: 5px;
}

.form-input {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  color: var(--text-color);
  font-size: 15px;
  transition: border-color 0.2s;
  background-color: var(--input-bg);
}

.form-input:focus {
  outline: 2px solid var(--focus-shadow);
  outline-offset: -1px;
}
</style>
