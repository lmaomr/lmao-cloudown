<script setup>
import { onMounted, ref, nextTick } from 'vue';
import useSettingStore from '@/stores/settingStore';
import { useUserStore } from '@/stores/userManageStore';
import toast from '@/utils/toast';
import router from '@/router/index';

// 切换主题
const isDarkTheme = ref(false);

const userStore = useUserStore();

const settingStore = useSettingStore();
// 表单数据
const loginForm = ref({
  email: '',
  password: ''
});

const registerForm = ref({
  email: '',
  password: '',
  confirmPassword: ''
});

// 错误信息
const errors = ref({
  email: '',
  password: '',
  confirmPassword: ''
});

// 校验规则
const rules = {
  email: val => /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(val) ? '' : '请输入正确的邮箱格式',
  password: val => /^(?=.*[a-zA-Z])(?=.*\d).{6,20}$/.test(val) ? '' : '密码格式不符合规范',
  confirmPassword: (val) => val === registerForm.value.password ? '' : '两次输入的密码不一致'
};

// 失去焦点时校验
const validate = (field, value) => {
  errors.value[field] = rules[field](value) || '';
};

//提交表单
const submitForm = (form) => {
  // 表单验证
  Object.keys(form).forEach(field => validate(field, form[field]));
  // 只检查当前表单中存在的字段是否有错误
  const hasErrors = Object.keys(form).some(field => errors.value[field]);
  if (!hasErrors) {
    return true;
  }
  return false;
};

// 控制表单显示
const showLoginForm = ref(true);

// 加载状态
const isLoading = ref(false);

// 切换到注册表单
const switchToRegister = () => {
  showLoginForm.value = false;
  errors.value = {
    email: '',
    password: '',
    confirmPassword: ''
  };
};

// 切换到登录表单
const switchToLogin = () => {
  showLoginForm.value = true;
  errors.value = {
    email: '',
    password: '',
    confirmPassword: ''
  };
};



// 登录提交
const handleLogin = async (e) => {
  e.preventDefault();
  if (!submitForm(loginForm.value)) return;
  // 防止重复提交
  if (isLoading.value) return;
  isLoading.value = true;
  const loadingId = toast.loading('登录中...');
  try {
    // 调用登录接口
    const loginSuccess = await userStore.login(loginForm.value);

    if (loginSuccess) {
      // 登录成功
      toast.success('登录成功', '欢迎回来！');
      // 跳转到首页或来源页
      const redirectPath = router.currentRoute.value.query.redirect || '/';
      await nextTick(); // 确保 DOM 更新完成
      router.push(redirectPath).catch(err => {
        console.error('路由跳转失败:', err);
      });
    } else {
      // 登录失败但没有抛出异常的情况
      toast.error('登录失败', '邮箱或密码错误');
    }

    toast.closeLoading(loadingId);
  } catch (error) {
    // 处理网络或未知错误
    console.error('登录异常:', error);
  } finally {
    // 无论成功失败，最终解除加载状态
    isLoading.value = false;
    toast.closeLoading(loadingId);
  }
};

// 注册提交
const handleRegister = async (e) => {
  e.preventDefault();
  if (!submitForm(registerForm.value)) return;
  // 防止重复提交
  if (isLoading.value) return;
  isLoading.value = true;
  const loadingId = toast.loading('注册中...');
  try {
    // 调用注册接口
    const registerSuccess = await userStore.register(registerForm.value);

    if (registerSuccess) {
      // 注册成功，自动填充登录表单
      loginForm.value.email = registerForm.value.email;
      loginForm.value.password = registerForm.value.password;

      toast.success('注册成功', '快去登录吧');
      switchToLogin();
    } else {
      // 注册失败
      toast.error('注册失败', '邮箱可能已被注册');
    }

    toast.closeLoading(loadingId);
  } catch (error) {
    // 处理网络或未知错误
    console.error('注册异常:', error);
  } finally {
    // 无论成功失败，最终解除加载状态
    isLoading.value = false;
    toast.closeLoading(loadingId);
  }
};

onMounted(() => {

});
</script>

<template>
  <div :class="['container', { 'dark-theme': isDarkTheme }]">
    <button class="theme-toggle" @click="settingStore.toggleTheme" title="切换主题" aria-label="切换明暗主题">
      <i :class="['fas', settingStore.isDarkTheme ? 'fa-sun' : 'fa-moon']"></i>
    </button>

    <div class="login-card">
      <div class="logo">
        <img src="/logo.png" alt="Logo" />
        <div class="logo-text">Cloudreve</div>
      </div>

      <!-- 登录表单 -->
      <div :class="['login-form', showLoginForm ? 'visible' : 'hidden']" id="loginForm">
        <h2 class="form-title">登录你的账号</h2>
        <form @submit="handleLogin">
          <div class="form-group">
            <label for="loginEmail" class="form-label">
              <i class="fas fa-envelope"></i> 邮箱
            </label>
            <input type="text" id="loginEmail" class="form-input" placeholder="请输入邮箱" v-model="loginForm.email" @blur="validate('email', loginForm.email)"
              required>
            <div class="form-error" v-if="errors.email">{{ errors.email }}</div>
          </div>
          <div class="form-group">
            <label for="loginPassword" class="form-label">
              <i class="fas fa-lock"></i> 密码
            </label>
            <input type="password" id="loginPassword" class="form-input" placeholder="请输入密码"
              v-model="loginForm.password" @blur="validate('password', loginForm.password)" required>
            <div class="form-error" v-if="errors.password">{{ errors.password }}</div>
          </div>
          <div class="form-group">
            <button type="submit" class="btn btn-primary">
              <i class="fas fa-sign-in-alt"></i> 登录
            </button>
          </div>
        </form>
        <div class="form-switch">
          还没有账号？<a href="#" @click.prevent="switchToRegister">立即注册</a>
        </div>
      </div>

      <!-- 注册表单 -->
      <div :class="['register-form', !showLoginForm ? 'visible' : 'hidden']" id="registerForm">
        <h2 class="form-title">创建账号</h2>
        <form @submit="handleRegister">
          <div class="form-group">
            <label for="registerEmail" class="form-label">
              <i class="fas fa-envelope"></i> 邮箱
            </label>
            <input type="email" id="registerEmail" class="form-input" placeholder="请输入邮箱" v-model="registerForm.email"
              required @blur="validate('email',registerForm.email)">
            <div class="form-error" v-if="errors.email">{{ errors.email }}</div>
          </div>
          <div class="form-group">
            <label for="registerPassword" class="form-label">
              <i class="fas fa-lock"></i> 密码
            </label>
            <input type="password" id="registerPassword" class="form-input" placeholder="请输入密码"
              v-model="registerForm.password" required @blur="validate('password', registerForm.password)">
            <div class="form-hint">密码长度为6-20位，包含字母和数字</div>
            <div class="form-error" v-if="errors.password">{{ errors.password }}</div>
          </div>
          <div class="form-group">
            <label for="registerConfirmPassword" class="form-label">
              <i class="fas fa-check-circle"></i> 确认密码
            </label>
            <input type="password" id="registerConfirmPassword" class="form-input" placeholder="请再次输入密码"
              v-model="registerForm.confirmPassword" required @blur="validate('confirmPassword', registerForm.confirmPassword)">
            <div class="form-error" v-if="errors.confirmPassword">{{ errors.confirmPassword }}</div>
          </div>
          <div class="form-group">
            <button type="submit" class="btn btn-primary">
              <i class="fas fa-user-plus"></i> 注册
            </button>
            <button type="button" class="btn btn-secondary" @click="switchToLogin">
              <i class="fas fa-arrow-left"></i> 返回登录
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 容器样式 */
.container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: var(--bg-color);
  color: var(--text-color);
  font-family: 'Arial', sans-serif;
  padding: 20px;
  transition: all 0.3s ease;
}

/* 主题切换按钮 */
.theme-toggle {
  position: fixed;
  top: 20px;
  right: 20px;
  background: var(--card-bg);
  border: none;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  box-shadow: 0 2px 10px var(--shadow-color);
  color: var(--text-color);
  transition: all 0.3s ease;
  z-index: 100;
}

.theme-toggle:hover {
  transform: scale(1.1);
}

/* 登录卡片 */
.login-card {
  background-color: var(--card-bg);
  border-radius: 10px;
  box-shadow: 0 4px 20px var(--shadow-color);
  width: 100%;
  max-width: 28rem;
  padding: 2.4rem;
  transition: all 0.3s ease;
}

/* Logo样式 */
.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1.2rem;
}

.logo img {
  width: 5.8rem;
  height: 4.6rem;
  border-radius: 50%;
  object-fit: contain;
}

.logo-text {
  font-size: 2rem;
  font-weight: bold;
  color: var(--primary-color);
}

/* 表单样式 */
.form-title {
  text-align: center;
  font-size: 1.3rem;
  font-weight: 500;
  color: var(--text-color);
  margin-bottom: 1rem;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-input {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--border-color);
  border-radius: 5px;
  background-color: var(--input-bg);
  color: var(--text-color);
  font-size: 1rem;
  transition: all 0.3s ease;
}

.form-input:focus {
  border-color: var(--primary-color);
  outline: none;
  box-shadow: 0 0 0 2px rgba(25, 118, 210, 0.2);
}

.form-hint {
  font-size: 0.8rem;
  color: var(--secondary-color);
  margin-top: 5px;
}

.form-error:not(:empty)::before {
    content: "\f071";
    font-family: "Font Awesome 6 Free";
    font-weight: 900;
    margin-right: 6px;
    font-size: 12px;
}

.form-error {
  color: var(--danger-color);
  font-size: 0.85rem;
  margin-top: 5px;
  min-height: 20px;
}

/* 按钮样式 */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 12px 20px;
  border: none;
  border-radius: 5px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn i {
  margin-right: 8px;
}

.btn-primary {
  background-color: var(--primary-color);
  color: white;
  width: 100%;
}

.btn-primary:hover {
  background-color: #1565c0;
  transform: translateY(-2px);
}

.btn-secondary {
  background-color: var(--secondary-color);
  color: var(--primary-color);
  margin-top: 10px;
  width: 100%;
  border: 1px solid var(--primary-color);
}

.btn-secondary:hover {
  background-color: var(--button-hover);
  transform: translateY(-2px);
}

/* 表单切换 */
.form-switch {
  text-align: center;
  margin-top: 20px;
}

.form-switch a {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 500;
  cursor: pointer;
}

.form-switch a:hover {
  text-decoration: underline;
}

/* 表单显示/隐藏 */
.visible {
  display: block;
}

.hidden {
  display: none;
}

/* 响应式设计 */
@media (max-width: 576px) {
  .login-card {
    padding: 20px;
  }

  .form-group {
    margin-bottom: 15px;
  }
}
</style>
