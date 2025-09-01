import axios from 'axios'
import toast from '@/utils/toast.js';
import { safeLocalStorage } from '@/utils/storage';
import config from '@/config';
import { useUserStore } from '@/stores/userManageStore';

// 创建 axios 实例
const request = axios.create({
  baseURL: config.apiBaseUrl, // 从配置文件获取 API 基础路径
  timeout: 5000 // 超时时间
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 添加 token（示例）
    const token = safeLocalStorage.get('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    toast.error('请求失败', error.message || '网络异常')
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data

    // 根据业务状态码处理（示例）
    if (res.code !== 200) {
      if (res.code !== 400 && res.code !== 500) {
        toast.warning('有点问题！', res.msg || '服务器异常')
      }else {
        toast.error('出错啦！', res.msg || '服务器异常')
      } 
      return Promise.reject(new Error(res.msg || 'Error'))
    } else {
      return res // 返回实际需要的数据
    }
  },
  error => {
    // HTTP 状态码处理
    let res = {
      title: '出错啦！',
      message: error.message || '服务器异常'
    }
    const userStore = useUserStore();
    switch (error.response?.status) {
      case 401:
        res.title = '未授权'
        res.message = '请重新登录'
        // 退出登录
        userStore.logout();
        break
      case 404:
        res.title = '请求失败'
        res.message = '请求资源不存在'
        break
      case 500:
        res.title = '请求失败'
        res.message = '服务器异常'
        break
    }
    if (error.config?._errorToastShown) {
      return Promise.reject(error); // 已显示过提示，直接拒绝
    }

    const lastError = JSON.parse(localStorage.getItem('lastError'));
    if (lastError && Date.now() - new Date(lastError).getTime() < 2000) {
      return Promise.reject(error); // 2秒内不重复提示
    }

    toast.error(res.title, res.message);

    // 记录本次错误时间，并标记当前请求已显示错误
    localStorage.setItem('lastError', JSON.stringify(new Date()));
    if (error.config) {
      error.config._errorToastShown = true; // 防止Axios重试时重复提示
    }

    return Promise.reject(error);
  }
)

export default request;
