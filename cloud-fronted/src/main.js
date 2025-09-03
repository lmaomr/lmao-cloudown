import './assets/main.css'
import ModalBox from './components/modal/ModalBox.vue'
import useSettingStore from './stores/settingStore'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.component('ModalBox', ModalBox)

// 初始化主题
const settingStore = useSettingStore();

// 如果localStorage中没有主题设置，则检测系统主题
if (localStorage.getItem('darkTheme') === null) {
  settingStore.detectSystemTheme();
}

// 初始化设备类型
settingStore.detectDeviceType();

// directives.js 或 main.js
const clickOutside = {
  beforeMount(el, binding) {
    el.__ClickOutsideHandler__ = event => {
      // 检查点击是否在元素内部
      if (!(el === event.target || el.contains(event.target))) {
        // 调用传递的方法
        binding.value(event);
      }
    };
    document.addEventListener('click', el.__ClickOutsideHandler__);
  },
  unmounted(el) {
    document.removeEventListener('click', el.__ClickOutsideHandler__);
  }
};

// 在 Vue 应用中注册指令
app.directive('click-outside', clickOutside);

app.mount('#app')
