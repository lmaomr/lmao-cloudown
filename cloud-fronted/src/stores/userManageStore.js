// stores/userStore.ts
import { ref, computed } from 'vue';
import { defineStore } from 'pinia';
import { safeLocalStorage } from '@/utils/storage';
import router from '@/router/index';
import userApi from '@/api/user';
import fileApi from '@/api/file';

export const useUserStore = defineStore('user-store', () => {
  const user = ref(JSON.parse(safeLocalStorage.get('user') || '{}'));
  const token = ref(safeLocalStorage.get('token') || '');

  // Getters
  const isLoggedIn = computed(() => !!token.value);

  /**
   * 获取用户信息
   */
  const getUserInfo = async () => {
    const res = await userApi.getUserInfo();
    if (res.code == 200) {
      user.value = res.data;
      safeLocalStorage.set('user', JSON.stringify(user.value));
    }
  };

  /**
   * 用户注册
   * @param {Object} registerForm - 注册表单数据
   * @param {string} registerForm.email - 邮箱
   * @param {string} registerForm.password - 密码
   * @returns {Promise<boolean>} 注册是否成功
   */
  const register = async (registerForm) => {
    try {
      const res = await userApi.register(registerForm);
      if (res.code == 200) {
        return res.data;
      }
      return false;
    } catch (error) {
      console.error('注册时发生错误:', error);
      return false;
    }
  };

  /**
   * 用户登录
   * @param {Object} loginForm - 登录表单数据
   * @param {string} loginForm.email - 邮箱
   * @param {string} loginForm.password - 密码
   * @returns {Promise<boolean>} 登录是否成功
   */
  const login = async (loginForm) => {
    try {
      const res = await userApi.login(loginForm);
      if (res.code == 200) {
        token.value = res.data;
        safeLocalStorage.set('token', token.value);

        // 获取用户信息
        getUserInfo();
        return true;
      }
      return false;
    } catch (error) {
      // 登录失败时清空状态
      resetUserState();
      throw error;
    }
  };

  /**
   * 用户登出
   */
  const logout = async () => {
    try {
      // 可以添加后端登出API调用
      // await userApi.logout();
    } catch (error) {
      console.error('登出时发生错误:', error);
    } finally {
      // 无论后端是否成功，都清除本地状态
      token.value = '';
      resetUserState();
      safeLocalStorage.remove('token');
      safeLocalStorage.remove('user');
      router.push('/login');
    }
  };

  /**
   * 重置用户状态
   * @private
   */
  const resetUserState = () => {
    user.value = {
      id: '',
      nickname: '',
      email: '',
      avatarUrl: '',
      role: '用户',
      createTime: '',
      updateTime: '',
      lastLoginTime: '',
      usedCapacity: 0,
      totalCapacity: 0
    };
  };


  /**
   * 检查并刷新令牌
   * @returns {Promise<boolean>} 刷新是否成功
   */
  const refreshToken = async () => {
    try {
      if (!isLoggedIn.value) {
        logout();
      }

      // 可以添加刷新令牌的API调用
      // const res = await userApi.refreshToken(token.value);
      // token.value = res.data;
      // saveUserState();
    } catch (error) {
      console.error('刷新令牌失败:', error);
    }
  };

  const updateAvatarImg = async (file) => {
      console.log('开始上传头像:', file);
      const res = await fileApi.uploadAvatar(file);
      if (res.code == 200) {
        user.value = res.data;
        safeLocalStorage.set('user', JSON.stringify(user.value));
      }
  };

  const updateUserNickname = async (newNickname) => {
      const res = await userApi.updateNickname(newNickname);
      if (res.code == 200) {
        user.value.nickname = newNickname;
        safeLocalStorage.set('user', JSON.stringify(user.value));
      }
  };

  const updateUserPassword = async (oldPassword, newPassword) => {
    const res = await userApi.updatePassword(oldPassword, newPassword);
    if (res.code == 200) {
      logout();
    }
  };



  return {
    user,
    token,
    isLoggedIn,
    getUserInfo,
    register,
    login,
    logout,
    refreshToken,
    updateAvatarImg,
    updateUserNickname,
    updateUserPassword
  };
});
