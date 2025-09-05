<script setup>
import { ref, watch, computed, onMounted, onUnmounted } from 'vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import useFileManageStore from '@/stores/fileManageStore'
import usePathStore from '@/stores/pathStore'
import { useUploadStore } from '@/stores/uploadStore.js'
import { useLoadingStore } from '@/stores/loadingStore'
import { formatFileSize, formatDate, formatFileType } from '@/utils/fomatUtil'
import toast from '@/utils/toast.js'
// 导入新创建的组件
import ContextMenu from '@/components/main/ContextMenu.vue'
import FileToolbar from '@/components/main/FileToolbar.vue'

const pathStore = usePathStore()
const fileManageStore = useFileManageStore()
const loadingStore = useLoadingStore()
const uploadStore = useUploadStore()

// 分离文件夹和文件
const folders = computed(() => fileManageStore.fileList.filter((item) => item.type === '文件夹'))
const files = computed(() => fileManageStore.fileList.filter((item) => item.type !== '文件夹'))

const isDragging = ref(false)
// 视图模式：grid 或 list
const viewMode = ref(localStorage.getItem('viewMode') || 'grid')

// 文件列表加载状态
const isFileListLoading = ref(false)

// 切换视图模式
const toggleViewMode = (mode) => {
  viewMode.value = mode
  localStorage.setItem('viewMode', mode)
}

// 加载文件列表
const loadFileList = async () => {
  try {
    isFileListLoading.value = true
    await fileManageStore.getFileList()
  } catch (error) {
    console.error('加载文件列表失败:', error)
    toast.error('加载文件列表失败', error.message || '请稍后重试')
  } finally {
    clearSelectedFiles();
    isFileListLoading.value = false
  }
}

// 刷新文件列表
const refreshFileList = async () => {
  await loadingStore.withLoading(
    async () => {
      await loadFileList()
    },
    { text: '刷新文件列表...', fullscreen: false },
  )
}

const handleDragEnter = (e) => {
  e.preventDefault()
  e.stopPropagation()
  isDragging.value = true
}

// 处理拖拽悬停
const handleDragOver = (e) => {
  e.preventDefault()
  e.stopPropagation()
  isDragging.value = true
}

// 处理拖拽离开
const handleDragLeave = (e) => {
  e.preventDefault()
  e.stopPropagation()
  // 检查是否真的离开了区域（而不是进入子元素）
  if (e.currentTarget.contains(e.relatedTarget)) return
  isDragging.value = false
}

// 处理文件放置
const handleDrop = async (e) => {
  e.preventDefault()
  e.stopPropagation()
  isDragging.value = false
  const droppedFiles = e.dataTransfer.files
  if (!droppedFiles || droppedFiles.length === 0) return
  const file = droppedFiles[0]
  // 更全面的文件验证
  if (!file || !(file instanceof File)) {
    toast.warning('文件格式有误', '请选择一个有效的文件')
    return
  }
  console.log('选择的文件:', file)
  try {
    await uploadStore.addUploadTask(file)
    await fileManageStore.getFileList()
  } catch (error) {
    console.error('操作失败:', error)
    toast.error('操作失败', error.message || '上传过程中发生错误')
  }
}

// 右键菜单相关
// 当前选中的文件/文件夹
const selectedFiles = ref([])

const contextMenuState = ref({
  isOpen: false,
  left: 0,
  top: 0
})

// 打开菜单
const openMenu = (e, file = null) => {
  e.preventDefault()
  e.stopPropagation()

  const w = window.screen.width
  const h = window.screen.height

  contextMenuState.value.left = e.clientX
  contextMenuState.value.top = e.clientY

  if (w - e.clientX < 210) {
    contextMenuState.value.left = w - 210
  }
  if (h - e.clientY < 450) {
    contextMenuState.value.top = h - 470
  }

  // 设置菜单类型和选中的文件
  if (file) {
    if (selectedFiles.value.indexOf(file) === -1) {
      selectedFiles.value = [file]
    }
  }
  contextMenuState.value.isOpen = true
}

// 关闭菜单
const closeContextMenu = () => {
  contextMenuState.value.isOpen = false
}

// 右键菜单事件处理
const handleContextMenuRefresh = () => {
  refreshFileList()
}

const handleContextMenuClearSelection = () => {
  selectedFiles.value = []
}


watch(
  [
    () => pathStore.activeMenu.section,
    () => pathStore.breadcrumbPath[pathStore.breadcrumbPath.length - 1]?.section,
    () => fileManageStore.sortOptions,
    () => pathStore.isSearchMode,
  ],
  () => {
    loadFileList()
  },
)

// 工具栏事件处理函数
const handleToolbarRefresh = () => {
  refreshFileList()
}

const handleViewToggle = (mode) => {
  toggleViewMode(mode)
}

const handleSortChange = (value) => {
  fileManageStore.setSortOptions(value)
}

const handlePathClick = (index) => {
  setDesigPath(index)
}

const setDesigPath = (index) => {
  pathStore.setDesigPath(index + 1)
  refreshFileList()
}

// 处理双击进入文件夹
const handleFolderDoubleClick = (folder) => {
  if (folder.type === '文件夹' && folder.status === 'ACTIVE') {
    console.log('双击进入文件夹:', folder.name)
    pathStore.setBreadcrumbPath(folder.name)
    const path = pathStore.getBreadcrumbPath()
    console.log('更新后的路径:', path)
    refreshFileList()
  }
}

const handleSelectFile = (file, event = {}) => {
  closeContextMenu();
  event.stopPropagation()
  if (event && event.ctrlKey) {
    // 多选
    const idx = selectedFiles.value.findIndex((f) => f.id === file.id)
    if (idx === -1) {
      selectedFiles.value.push(file)
    } else {
      selectedFiles.value.splice(idx, 1)
    }
  } else {
    // 单选
    selectedFiles.value = [file]
  }
}

const clearSelectedFiles = () => {
  // 只有当点击的是文件内容区域本身时才清空选择，避免子元素点击时误触发
  selectedFiles.value = []
}

const includesFile = (file) => {
  return selectedFiles.value.find((f) => f.id === file.id)
}

const reverseSelect = () => {
  const currentlySelectedIds = selectedFiles.value.map(f => f.id)
  selectedFiles.value = fileManageStore.fileList.filter(f => !currentlySelectedIds.includes(f.id))
}

const handleSelectAll = () => {
  selectedFiles.value = fileManageStore.fileList.slice()
}

// 键盘事件处理
const handleKeyDown = (event) => {
  // Ctrl+A 全选
  if (event.ctrlKey && event.key === 'a') {
    event.preventDefault()
    handleSelectAll()
  }
}

// 组件挂载时添加键盘监听
onMounted(() => {
  document.addEventListener('keydown', handleKeyDown)
})

// 组件卸载时移除键盘监听
onUnmounted(() => {
  document.removeEventListener('keydown', handleKeyDown)
})



</script>

<template>
  <!-- 工具栏组件 -->
  <FileToolbar :view-mode="viewMode" @refresh="handleToolbarRefresh" @view-toggle="handleViewToggle"
    @sort-change="handleSortChange" @path-click="handlePathClick" @select-all="handleSelectAll"
    @reverse-select="reverseSelect" @cancel-select="clearSelectedFiles" />

  <div class="file-content" :class="{ 'drag-over': isDragging }" id="fileContent" @dragenter="handleDragEnter"
    @dragleave="handleDragLeave" @dragover="handleDragOver" @drop="handleDrop" @contextmenu.prevent="openMenu">
    <!-- 右键菜单 -->
    <ContextMenu :is-open="contextMenuState.isOpen" :menu-left="contextMenuState.left" :menu-top="contextMenuState.top"
      :selected-files="selectedFiles" @close-menu="closeContextMenu" @refresh-file-list="handleContextMenuRefresh"
      @clear-selection="handleContextMenuClearSelection" />

    <!-- 加载中状态 -->
    <div v-if="isFileListLoading" class="file-list-loading">
      <LoadingSpinner type="dots" size="medium" showText text="加载文件列表中..." />
    </div>

    <!-- 网格视图 -->
    <div v-else-if="viewMode === 'grid' && fileManageStore.fileList.length > 0" class="grid-container">
      <!-- 文件夹区域 -->
      <div v-if="folders.length > 0" class="folders-section">
        <div class="section-title">
          文件夹
          <span class="count">({{ folders.length }})</span>
        </div>
        <div class="file-list-container grid-view">
          <div class="file-list-item" v-for="(item, index) in folders" :key="index"
            @contextmenu.prevent="openMenu($event, item)" @dblclick="handleFolderDoubleClick(item)"
            style="cursor: pointer">
            <div class="file-container-item" @click="handleSelectFile(item, $event)">
              <div class="file-list-item-icon">
                <i v-if="!includesFile(item)" class="fas fa-folder"></i>
                <div v-else class="active-icon">
                  <i class="fas fa-check"></i>
                </div>
              </div>
              <div :title="item.name" class="file-list-item-name">
                {{ item.name }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 文件区域 -->
      <div v-if="files.length > 0" class="files-section">
        <div class="section-title">
          文件
          <span class="count">({{ files.length }})</span>
        </div>
        <div class="file-list-container grid-view">
          <div @click="handleSelectFile(item, $event)" class="file-list-item" v-for="(item, index) in files"
            :key="index" @contextmenu.prevent="openMenu($event, item)">
            <div class="file-container-item">
              <div class="file-list-item-icon">
                <i v-if="!includesFile(item)" :class="formatFileType(item.name).icon || 'fa-solid fa-file'"></i>
                <div v-else class="active-icon">
                  <i class="fas fa-check"></i>
                </div>
              </div>
              <div :title="item.name" class="file-list-item-name">
                {{ item.name }}
              </div>
            </div>
            <div class="file-thumbnail">
              <img v-if="item.thumbnailPath" :src="item.thumbnailPath" alt="文件缩略图" aria-haspopup="true" />
              <i v-else :class="formatFileType(item.name).icon || 'fa-solid fa-file'"></i>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 列表视图 -->
    <div v-else-if="viewMode === 'list' && fileManageStore.fileList.length > 0" class="file-list-container list-view">
      <table class="file-table">
        <thead>
          <tr>
            <th class="file-name">文件名</th>
            <th class="file-size">大小</th>
            <th class="file-modified">修改日期</th>
            <th class="file-type">类型</th>
          </tr>
        </thead>
        <tbody>
          <!-- 文件夹区 -->
          <tr v-for="(item, index) in folders" :key="'folder-' + index" class="file-row"
            @contextmenu.prevent="openMenu($event, item)" @dblclick="handleFolderDoubleClick(item)"
            style="cursor: pointer">
            <td class="file-name" @click="handleSelectFile(item, $event)">
              <div class="file-name-container">
                <i v-if="!includesFile(item)" class="fas fa-folder"></i>
                <div v-else class="active-icon">
                  <i class="fas fa-check"></i>
                </div>
                <span :title="item.name">{{ item.name }}</span>
              </div>
            </td>
            <td class="file-size">-</td>
            <td class="file-modified">{{ formatDate(item.updateTime) ?? '-' }}</td>
            <td class="file-type">文件夹</td>
          </tr>
          <!-- 文件区 -->
          <tr v-for="(item, index) in files" :key="'file-' + index" class="file-row"
            @contextmenu.prevent="openMenu($event, item)" style="cursor: default">
            <td class="file-name" @click="handleSelectFile(item, $event)">
              <div class="file-name-container">
                <i v-if="!includesFile(item)" :class="formatFileType(item.name).icon || 'fa-solid fa-file'"></i>
                <div v-else class="active-icon">
                  <i class="fas fa-check"></i>
                </div>
                <span :title="item.name">{{ item.name }}</span>
              </div>
            </td>
            <td class="file-size">{{ formatFileSize(item.size) ?? '-' }}</td>
            <td class="file-modified">{{ formatDate(item.updateTime) ?? '-' }}</td>
            <td class="file-type">{{ item.type || '-' }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 文件列表为空时的提示 -->
    <div v-else class="empty-file-list">
      <svg class="empty-file-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
        <path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
          d="M12 22c-.818 0-1.6-.335-3.163-1.006C4.946 19.324 3 18.49 3 17.085V7.747M12 22c.818 0 1.6-.335 3.163-1.006C19.054 19.324 21 18.49 21 17.085V7.747M12 22v-9.83m9-4.422c0 .603-.802.984-2.405 1.747l-2.92 1.39C13.87 11.741 12.97 12.17 12 12.17m9-4.423c0-.604-.802-.985-2.405-1.748M3 7.747c0 .604.802.986 2.405 1.748l2.92 1.39c1.804.857 2.705 1.286 3.675 1.286M3 7.748c0-.604.802-.985 2.405-1.748m.927 7.311l1.994.948M12 2v2m4-1l-1.5 2M8 3l1.5 2"
          color="currentColor"></path>
      </svg>
      <h5 class="empty-message">什么都没有找到</h5>
    </div>
  </div>
</template>

<style scoped>
.grid-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  padding: 1rem;
}

.folders-section,
.files-section {
  background-color: var(--card-bg);
  border-radius: var(--card-border-radius);
}

.section-title {
  display: flex;
  align-items: center;
  color: var(--text-color);
  font-size: 0.92rem;
  font-weight: 500;
  gap: 0.25rem;
  padding-bottom: 1rem;
}

.count {
  color: var(--text-secondary);
}

.file-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: var(--card-bg);
  border-radius: var(--card-border-radius);
  border: 1px solid var(--border-color);
  overflow-y: auto;
}

.active-icon {
  width: 1.2rem;
  height: 1.2rem;
  background-color: var(--primary-color);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  padding: 0.35rem;
}

.active-icon .fa-check {
  font-size: 0.55rem;
}

.grid-container {
  height: 100%;
  width: 100%;
  padding: 1rem;
}

.grid-container p {
  font-size: 0.95rem;
  font-weight: 500;
  margin-bottom: 0.6rem;
}

/* 网格视图样式 */
.file-list-container.grid-view {
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  grid-auto-columns: 0;
  gap: 0.8rem;
  grid-auto-rows: minmax(min-content, max-content);
}

.file-container-item {
  width: 100%;
  display: flex;
  justify-content: start;
  align-items: center;
  gap: 0.875rem;
  line-height: 2rem;
}

.file-list-item-icon i,
.file-name-container i {
  font-size: 1.25rem;
}

/* 文件列表项样式 */
.grid-view .file-list-item {
  width: 100%;
  height: 100%;
  border-radius: var(--file-border-radius);
  padding: 0.675rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: all 0.3s ease;
  background-color: var(--file-bg);
}

.grid-view .file-list-item:hover {
  background-color: var(--file-hover);
}

.grid-view .file-list-item:active {
  background-color: var(--active-bg);
}

/* 文件图标样式 */
.grid-view .file-list-item-icon {
  padding-left: 0.8rem;
}

/* 文件名样式 */
.grid-view .file-list-item-name {
  font-size: 0.875rem;
  font-weight: 500;
  /* 长文件名换行 */
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 18ch;
}

.file-thumbnail {
  width: 100%;
  height: 9.8rem;
}

.file-thumbnail img {
  border-radius: var(--card-border-radius);
  object-fit: cover;
  width: 100%;
  height: 100%;
}

.file-thumbnail i {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  font-size: 3.6rem;
  border-radius: var(--card-border-radius);
  background-color: var(--card-bg);
}

/* 列表视图样式 */
.file-list-container.list-view {
  width: 100%;
  height: 100%;
  padding: 0;
}

.file-table {
  width: 100%;
  border-collapse: collapse;
}

.file-table th {
  text-align: left;
  padding: 0.8rem 1rem;
  font-weight: 500;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-color);
}

.file-table td {
  padding: 0.8rem 1rem;
  border-bottom: 1px solid var(--border-color);
  color: var(--text-color);
}

.file-row:hover {
  background-color: var(--hover-bg);
}

.file-name-container {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.file-name-container i {
  width: 1.25rem;
  font-size: 1.25rem;
}

.file-name {
  width: 40%;
}

.file-size,
.file-modified,
.file-type {
  width: 20%;
}

.empty-file-list {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1.2rem;
}

.empty-file-icon {
  color: var(--icon-color);
  width: 6.8rem;
}

.empty-message {
  margin: 0;
  font-family: Roboto, Helvetica, Arial, sans-serif;
  font-size: 1.8rem;
  line-height: 1.334;
  letter-spacing: 0;
  color: var(--icon-color);
  font-weight: 500;
}

/* 拖拽上传样式 */
.drag-over {
  border: 2px dashed var(--primary-color);
  background-color: rgba(25, 118, 210, 0.05);
}

.drag-over::after {
  position: absolute;
  content: '释放鼠标上传文件';
  font-size: 1.8rem;
  color: var(--primary-color);
  font-weight: 500;
  background-color: var(--drag-shadow);
  border-radius: inherit;
  box-shadow: var(--shadow-md);
  pointer-events: none;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  /* 添加这一行 */
  z-index: 900;
  padding: 1rem 2rem;
}

/* 文件拖放区域动画 */
.drag-over {
  animation: pulse 1.5s infinite;
}

/* 脉冲动画 - 用于提示用户注意某个元素 */
@keyframes pulse {
  0% {
    transform: scale(0.97);
  }

  50% {
    transform: scale(1);
  }

  100% {
    transform: scale(0.97);
  }
}

@media (max-width: 1920px) {
  .file-list-container.grid-view {
    grid-template-columns: repeat(6, 1fr);
    gap: 1.2rem;
  }

  .file-thumbnail {
    width: 100%;
    height: 12rem;
  }

  /* 文件名样式 */
  .grid-view .file-list-item-name {
    width: 22ch;
  }
}

/* 响应式调整列数 */
@media (max-width: 1831px) {
  .file-list-container.grid-view {
    grid-template-columns: repeat(5, 1fr);
  }

  .file-thumbnail {
    height: 12rem;
  }

  /* 文件名样式 */
  .grid-view .file-list-item-name {
    width: 22ch;
  }
}

/* 响应式调整列数 */
@media (max-width: 1286px) {
  .file-list-container.grid-view {
    grid-template-columns: repeat(4, 1fr);
  }

  /* 文件名样式 */
  .grid-view .file-list-item-name {
    width: 20ch;
  }
}

@media (max-width: 945px) {
  .file-list-container.grid-view {
    grid-template-columns: repeat(3, 1fr);
  }

  /* 文件名样式 */
  .grid-view .file-list-item-name {
    width: 18ch;
  }
}

@media (max-width: 768px) {
  .file-list-container.grid-view {
    grid-template-columns: repeat(2, 1fr);
  }

  .file-table th:not(.file-name),
  .file-table td:not(.file-name) {
    display: none;
  }

  .file-name {
    width: 100%;
  }

  /* 文件名样式 */
  .grid-view .file-list-item-name {
    width: 16ch;
  }
}

@media (max-width: 480px) {

  /* 小屏幕下网格改为两列 */
  .file-list-container.grid-view {
    grid-template-columns: repeat(2, 1fr);
    gap: 0.8rem;
  }
}
</style>
