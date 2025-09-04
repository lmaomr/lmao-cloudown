import { createApp } from 'vue'
import ToastContainer from '@/components/toast/ToastContainer.vue'

// 创建一个 toast 容器实例
let toastInstance = null

// 确保只创建一个 toast 容器实例
const getToastInstance = () => {
  if (!toastInstance) {
    // 创建一个 div 作为挂载点
    const mountPoint = document.createElement('div')
    document.body.appendChild(mountPoint)

    // 创建 Vue 应用
    const app = createApp(ToastContainer)

    // 挂载应用，获取实例
    toastInstance = app.mount(mountPoint)
  }

  return toastInstance
}

const toast = {
  success(title, message, duration = 3000) {
    getToastInstance().addToast('success', title, message, duration)
  },

  error(title, message, duration = 3000) {
    getToastInstance().addToast('error', title, message, duration)
  },

  warning(title, message, duration = 3000) {
    getToastInstance().addToast('warning', title, message, duration)
  },

  info(title, message, duration = 3000) {
    getToastInstance().addToast('info', title, message, duration)
  },

  loading(title, message, duration = 0) {
    const id = Date.now().toString()
    getToastInstance().addToast('loading', title, message, duration, id)
    return id
  },

  updateLoading(id, title, message) {
    if (id) {
      getToastInstance().updateToast(id, title, message)
    }
  },

  // 手动关闭 loading toast
  closeLoading(id) {
    if (id) {
      getToastInstance().removeToast(id)
    }
  }
}

export default toast
