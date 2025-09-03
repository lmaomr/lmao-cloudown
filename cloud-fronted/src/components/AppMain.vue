<script setup>
import { ref, onMounted, computed, inject, watch, nextTick } from 'vue';
import useSettingStore from '@/stores/settingStore';
import usePathStore from '@/stores/pathStore';
import FileList from '@/components/main/FileList.vue';
import { useUserStore } from '@/stores/userManageStore';
import Shared from '@/components/main/Shared.vue';
import Connections from '@/components/main/Connections.vue';
import AdminPanel from '@/components/main/AdminPanel.vue';
import OfflineDownload from '@/components/main/OfflineDownload.vue';
import { useUploadStore } from '@/stores/uploadStore';
import UploadManager from './main/UploadManager.vue';
import useFileManageStore from '@/stores/fileManageStore';
import toast from '@/utils/toast.js'

const settingStore = useSettingStore();
const pathStore = usePathStore();
const fileManageStore = useFileManageStore();
const uploadStore = useUploadStore();
const userStore = useUserStore();

//新建下拉框
const isOpen = ref(false);
// 新建文件夹弹窗
const isCreateFolderModalVisible = ref(false);
// 新建文本文档弹窗
const isCreateTextModalVisible = ref(false);
// 文件夹名称
const folderName = ref('');
// 文本文件名称
const textFileName = ref('');

// 组件映射表 - 将section映射到对应的组件
const componentMap = {
  'shared': Shared,
  'connections': Connections,
  'offline-download': OfflineDownload,
  'admin-panel': AdminPanel
};

// 计算当前应该显示的组件
const currentComponent = computed(() => {
  const section = pathStore.activeMenu.section;
  return componentMap[section] || FileList; // 默认返回FileList
});

const toggleDropdown = (e) => {
  e.stopPropagation(); // 阻止事件冒泡
  isOpen.value = !isOpen.value;
};

const createFolder = () => {
  console.log('新建文件夹');
  if (!folderName.value) {
    toast.warning("文件夹名称不能为空", "请输入文件夹名称");
    return;
  }
  // 验证文件夹名称是否包含特殊字符
  const invalidChars = /[\\/:*?"<>|]/;
  if (invalidChars.test(folderName.value)) {
    toast.warning("文件夹名称包含非法字符", "请勿使用 \\ / : * ? \" < > | 等特殊字符");
    return;
  }
  fileManageStore.createFolder(folderName.value);
  handleClose();
}

const createText = () => {
  console.log('新建文本文档');
  if (!textFileName.value) {
    toast.warning("文件名称不能为空", "请输入文件名称");
    return;
  }
  fileManageStore.createTextFile(textFileName.value);
  handleClose();
}

const createUpload = () => {
  console.log('上传文件');
  document.getElementById('file-input').click();
}

const selectedFile = async (event) => {
  const files = event.target.files;
  if (!files || files.length === 0) return;
  const file = files[0];
  // 更全面的文件验证
  if (!file || !(file instanceof File)) {
    toast.warning("文件格式有误", "请选择一个有效的文件");
    event.target.value = '';
    return;
  }
  console.log('选择的文件:', file);
  try {
    const res = await uploadStore.addUploadTask(file);
    if (res !== 'error') {
      fileManageStore.getFileList();
    }
  } catch (error) {
    console.error('操作失败:', error);
    // 可以添加用户提示
  } finally {
    event.target.value = ''; // 清空选择
  }
}

const createUploadFolder = () => {
  console.log('上传文件夹');
  document.getElementById('folder-input').click();
}

// 关闭弹窗
const handleClose = () => {
  isCreateFolderModalVisible.value = false;
  isCreateTextModalVisible.value = false;
  // 清空输入框的值
  folderName.value = '';
  textFileName.value = '';
}

const openUserInfoPanel = inject('openUserInfoPanel');

const handleOpenUserInfoPanel = (e) => {
  if (!e.target.tagName === 'BUTTON' || !e.target.closest('button')) {
    e.stopPropagation(); // 阻止事件冒泡，避免触发其他点击事件
    openUserInfoPanel();
  }
};

// 点击外部关闭
onMounted(() => {
  document.addEventListener('click', () => {
    isOpen.value = false;
  });
});

defineEmits(['toggleSidebar']);

const searchQuery = ref('');

const performSearch = async () => {
  const query = searchQuery.value.trim();
  if (query === '') {
    pathStore.isSearchMode = false;
    pathStore.searchQuery = '';
    return;
  }
  const lodingId = toast.loading("搜索中...", "请稍候");
  try {
    pathStore.searchQuery = searchQuery.value;
    pathStore.isSearchMode = true;
    closeSearch();
    await fileManageStore.searchFiles(query);
    toast.success("搜索完成", `为你找到 ${fileManageStore.fileList.length} 个相关文件`);
  } catch (error) {
    console.error('搜索失败:', error);
    toast.error("搜索失败", "请稍后重试");
  } finally {
    toast.closeLoading(lodingId);
  }
}

const inputing = () => {
  if (searchQuery.value.trim() === '') {
    pathStore.isSearchMode = false;
  }
}

watch(() => pathStore.isSearchMode, (newVal) => {
  if (!newVal) {
    searchQuery.value = '';
  }
});

const isExpanded = ref(false);
const searchInputRef = ref(null);
const handleExpand = async () => {
  isExpanded.value = true;
   // 等待 DOM 更新（确保输入框已经渲染）
  await nextTick();

  // 聚焦输入框
  searchInputRef.value?.focus();
}

const closeSearch = () => {
  if (!isExpanded.value) return;
  isExpanded.value = false;
  searchQuery.value = '';
}

const setting = () => {
  toast.info("设置", "正在开发中，敬请期待！");
}

</script>

<template>
  <div class="main-container">
    <div class="header-bar">
      <!-- 侧边栏切换按钮 -->
      <div class="header-left">
        <button class="sidebar-toggle" title="菜单" aria-label="切换菜单" @click="$emit('toggleSidebar')">
          <i class="fas fa-bars"></i>
        </button>
        <div class="dropdown-container">
          <button class="creat-btn" id="createBtn" aria-label="新建" @click="toggleDropdown">
            <i class="fas fa-plus"></i>
            <span>新建</span>
          </button>
          <transition name="slide">
            <ul v-if="isOpen" class="create-menu-content">
              <li class="create-menu-item" @click="isCreateFolderModalVisible = true">
                <i class="fas fa-folder"></i> 新建文件夹
              </li>
              <li class="create-menu-item" @click="isCreateTextModalVisible = true">
                <i class="fas fa-file-alt"></i> 新建文本文档
              </li>
              <li class="create-menu-item" @click="createUpload">
                <i class="fas fa-file-upload"></i> 上传文件
              </li>
              <li class="create-menu-item" @click="createUploadFolder">
                <i class="fas fa-folder-plus"></i> 上传文件夹
              </li>
            </ul>
          </transition>
          <input type="file" @change="selectedFile" id="file-input" multiple class="file-input" />
          <input type="file" id="folder-input" webkitdirectory directory multiple class="file-input">
        </div>
        <div class="search-box" :class="{ expand: isExpanded }" v-click-outside="closeSearch" @click="handleExpand">
          <i class="fas fa-search"></i>
          <input type="text" placeholder="搜索你的文件" aria-label="搜索文件" @keyup.enter="performSearch" v-model="searchQuery"
            @input="inputing" ref="searchInputRef"/>
        </div>
        <div v-if="isExpanded" class="overlay" @click="closeSearch"></div>
      </div>
      <div class="header-right">
        <button class="theme-toggle" title="切换主题" aria-label="切换明暗主题" @click="settingStore.toggleTheme">
          <i :class="!settingStore.isDarkTheme ? 'fas fa-moon' : 'fas fa-sun'"></i>
        </button>
        <button class="setting-btn" @click="setting" title="设置" aria-label="设置"><i class="fas fa-cog"></i></button>
        <div class="user-avatar" @click="handleOpenUserInfoPanel">
          <img v-if="userStore.user.avatarUrl" :src="userStore.user.avatarUrl" alt="用户头像" />
          <span v-else>{{ userStore.user.nickname?.substring(0, 1) || 'u' }}</span>
        </div>
      </div>
    </div>
    <div class="main-content">
      <!-- 使用动态组件加载当前应显示的内容 -->
      <component :is="currentComponent" />
    </div>
  </div>
  <Teleport to="body">
    <ModalBox ui="fas fa-folder" title="新建文件夹" :visible="isCreateFolderModalVisible" @confirm="createFolder()"
      @close="handleClose()">
      <div class="modal-body">
        <label for="folderName" class="form-label">文件夹名称</label>
        <input type="text" class="form-input" placeholder="请输入文件夹名称" v-model="folderName">
        <div class="form-hint">文件夹名称不能包含特殊字符 \\ / : * ? " &lt; &gt;|</div>
      </div>
    </ModalBox>
    <ModalBox ui="fas fa-file-alt" title="新建文本文档" :visible="isCreateTextModalVisible" @confirm="createText()"
      @close="handleClose()">
      <div class="modal-body">
        <label for="fileName" class="form-label">文件名称</label>
        <input type="text" class="form-input" placeholder="请输入文件名称" v-model="textFileName">
        <div class="form-hint">文件名称不能包含特殊字符 \\ / : * ? " &lt; &gt; |</div>
      </div>
    </ModalBox>
    <UploadManager :visible="uploadStore.isUploadManagerVisible" @close="uploadStore.hideUploadManager" />
  </Teleport>
</template>

<style scoped>
.main-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1.2rem;
}

.main-container>div {
  width: 100%;
  border-radius: var(--card-border-radius);
}

.form-input {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  color: var(--text-color);
  font-size: 15px;
  transition: border-color 0.2s;
  margin: 1rem 0;
  background-color: var(--input-bg);
}

.form-input:focus {
  outline: none;
  border-color: var(--primary-color);
}

.form-hint {
  font-size: 12px;
  color: var(--text-secondary);
}

.dropdown-container {
  position: relative;
}

.create-menu-content {
  position: absolute;
  top: 100%;
  left: 0;
  z-index: 1000;
  border-radius: var(--card-border-radius);
  min-width: 10rem;
  background-color: var(--card-bg);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color);
  margin-top: 0.5rem;
  list-style: none;
}

.create-menu-content li {
  padding: 0;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  color: var(--text-color);
  text-decoration: none;
  transition: all 0.2s;
}

.create-menu-content li:hover {
  background-color: var(--hover-bg);
  color: var(--primary-color);
}

.create-menu-content li i {
  font-size: 1rem;
}


/* 过渡动画 */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.file-input {
  display: none;
}

.header-bar {
  display: flex;
  align-items: center;
  flex: 0 0 4.25rem;
  padding: 0 0.6rem;
  background-color: var(--card-bg);
  gap: 0.5rem;
  border: 1px solid var(--border-color);
}

.header-left {
  flex: 1;
  display: flex;
  gap: 1rem;
}

.sidebar-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--i-color);
  font-size: var(--i-font-size);
  font-weight: 900;
  background-color: var(--card-bg);
  max-height: 3rem;
}

.sidebar-toggle:hover,
.theme-toggle:hover,
.setting-btn:hover {
  background-color: var(--hover-bg);
  color: var(--primary-color);
}

.creat-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  max-height: 3rem;
  min-width: 4.8rem;
}

.creat-btn i {
  font-size: var(--i-font-size);
}

.dropdown-container {
  position: relative;
}

.search-box {
  display: flex;
  align-items: center;
  background: var(--input-bg, #f0f2f5);
  border-radius: 4px;
  padding: 0 12px;
  transition: all 0.2s;
  min-width: 10rem;
}

.search-box i {
  color: var(--i-color);
  font-size: 0.875rem;
  margin-right: 0.5rem;
}

.search-box input {
  border: none;
  background: transparent;
  outline: none;
  color: var(--i-color);
  width: 100%;
}

.search-box input:focus {
  background: transparent;
}

.search-box:focus-within {
  background: var(--card-bg, white);
  box-shadow: 0 0 0 2px var(--focus-shadow);
}

.overlay {
  display: none;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 0.325rem;
}

.header-right button {
  background-color: var(--card-bg);
  color: var(--i-color);
}

.header-right button:hover {
  background-color: var(--hover-bg);
  color: var(--primary-color);
}

.header-right i {
  font-size: var(--i-font-size);
}

.theme-toggle {
  margin-left: auto;
}

.user-avatar {
  width: 2.6rem;
  height: 2.6rem;
  border-radius: 50%;
  display: none;
  overflow: hidden;
}

.user-avatar span {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background-color: var(--primary-color);
  font-weight: 500;
  font-size: 1.2rem;
  color: white;
  text-transform: uppercase;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 1.2rem;
  overflow-y: auto;
}


@media (max-width: 768px) {
  .main-container {
    min-width: 29rem;
  }

  .header-left {
    gap: 0.6rem;
  }

  .header-left button {
    min-width: 2.2rem;
    padding: 0.4rem;
  }

  .creat-btn {
    min-width: 2.2rem;
    color: var(--i-color);
    background-color: var(--card-bg);
  }

  .creat-btn span {
    display: none;
  }

  .search-box {
    overflow: hidden;
    background-color: transparent;
  }

  .search-box input {
    width: 0;
    transition: all 0.3s ease;
  }

  .expand {
    position: absolute;
    top: 1rem;
    left: 0.4rem;
    right: 0.4rem;
    width: calc(100% - 0.8rem);
    height: 3.9rem;
    z-index: var(--z-index-modal);
    background-color: var(--card-bg);
    border-radius: var(--card-border-radius);
    box-shadow: var(--shadow-md);
  }

  .expand input {
    width: 100%;
  }

  .overlay {
    display: block;
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background-color: rgba(0, 0, 0, 0.5);
    backdrop-filter: blur(2px);
    z-index: var(--z-index-backdrop);
  }

  .header-right button {
    padding: 1rem;
  }

  .user-avatar {
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 0.5rem;
  }

}
</style>
