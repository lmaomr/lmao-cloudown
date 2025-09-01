<script setup>
import { ref, watch, onMounted, onUnmounted, computed, nextTick } from 'vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue';
import useFileManageStore from '@/stores/fileManageStore';
import usePathStore from '@/stores/pathStore';
import { useUploadStore } from '@/stores/uploadStore.js'
import { useLoadingStore } from '@/stores/loadingStore';
import { formatFileSize, formatDate, formatFileType } from '@/utils/fomatUtil';
import toast from '@/utils/toast.js';

const pathStore = usePathStore();
const fileManageStore = useFileManageStore();
const loadingStore = useLoadingStore();
const uploadStore = useUploadStore();

// 分离文件夹和文件
const folders = computed(() => fileManageStore.fileList.filter(item => item.type === '文件夹'));
const files = computed(() => fileManageStore.fileList.filter(item => item.type !== '文件夹'));

const isDragging = ref(false);
// 视图模式：grid 或 list
const viewMode = ref(localStorage.getItem('viewMode') || 'grid');

// 文件列表加载状态
const isFileListLoading = ref(false);

// 切换视图模式
const toggleViewMode = (mode) => {
  viewMode.value = mode;
  localStorage.setItem('viewMode', mode);
};

// 加载文件列表
const loadFileList = async () => {
  try {
    isFileListLoading.value = true;
    await fileManageStore.getFileList();
  } catch (error) {
    console.error('加载文件列表失败:', error);
  } finally {
    isFileListLoading.value = false;
  }
}

// 刷新文件列表
const refreshFileList = async () => {
  await loadingStore.withLoading(async () => {
    await loadFileList();
  }, { text: '刷新文件列表...', fullscreen: false });
};

const handleDragEnter = (e) => {
  e.preventDefault();
  e.stopPropagation();
  isDragging.value = true;
};

// 处理拖拽悬停
const handleDragOver = (e) => {
  e.preventDefault();
  e.stopPropagation();
  isDragging.value = true;
};

// 处理拖拽离开
const handleDragLeave = (e) => {
  e.preventDefault();
  e.stopPropagation();
  // 检查是否真的离开了区域（而不是进入子元素）
  if (e.currentTarget.contains(e.relatedTarget)) return;
  isDragging.value = false;
};

//上传文件
const uploadFile = async (file) => {
  if (!file) {
    toast.warning("文件格式有误", "请选择一个有效的文件");
    return;
  }
  try {
    await uploadStore.addUploadTask(file);
    await fileManageStore.getFileList();
  } catch (error) {
    console.error('操作失败:', error);
  }
};

// 处理文件放置
const handleDrop = async (e) => {
  e.preventDefault();
  e.stopPropagation();
  isDragging.value = false;
  const droppedFiles = e.dataTransfer.files;
  if (!droppedFiles || droppedFiles.length === 0) return;
  const file = droppedFiles[0];
  // 更全面的文件验证
  if (!file || !(file instanceof File)) {
    toast.warning("文件格式有误", "请选择一个有效的文件");
    return;
  }
  console.log('选择的文件:', file);
  try {
    await uploadStore.addUploadTask(file);
    await fileManageStore.getFileList();
  } catch (error) {
    console.error('操作失败:', error);
  }
};

// 右键菜单状态
const isMenuOpen = ref(false);
const isFile = ref(false);
const menuLeft = ref(0);
const menuTop = ref(0);

// 右键菜单相关
const fileInputRef = ref(null)
const folderInputRef = ref(null)

// 当前选中的文件/文件夹
const selectedFile = ref(null);
const selectedFileIndex = ref(-1);

// 右键菜单：上传文件
const handleUploadFile = () => {
  closeMenu();
  nextTick(() => fileInputRef.value && fileInputRef.value.click());
};
const onFileInputChange = async (e) => {
  const file = e.target.files[0];
  if (file) {
    await uploadFile(file);
    await fileManageStore.getFileList();
  }
  e.target.value = '';
};

// 右键菜单：上传目录
const handleUploadFolder = () => {
  closeMenu();
  nextTick(() => folderInputRef.value && folderInputRef.value.click());
};

const onFolderInputChange = async (e) => {
  const files = e.target.files;
  if (files && files.length > 0) {
    for (const file of files) {
      await uploadFile(file);
    }
    await fileManageStore.getFileList();
  }
  e.target.value = '';
};

// 右键菜单：刷新
const handleRefresh = () => {
  closeMenu();
  refreshFileList();
};

// 文件操作：重命名
const handleRename = () => {
  console.log('重命名文件:', selectedFile.value);
  closeMenu();
  // 这里实现重命名逻辑
};

// 文件操作：下载
const handleDownload = () => {
  console.log('下载文件:', selectedFile.value);
  closeMenu();
  // 这里实现下载逻辑
};

// 文件操作：删除
const handleDelete = () => {
  console.log('删除文件:', selectedFile.value);
  closeMenu();
  // 这里实现删除逻辑
};

// 文件操作：复制
const handleCopy = () => {
  console.log('复制文件:', selectedFile.value);
  closeMenu();
  // 这里实现复制逻辑
};

// 文件操作：剪切
const handleCut = () => {
  console.log('剪切文件:', selectedFile.value);
  closeMenu();
  // 这里实现剪切逻辑
};

// 打开右键菜单
const openMenu = (e, file = null, index = -1) => {
  e.preventDefault();
  e.stopPropagation();

  const w = window.screen.width
  const h = window.screen.height

  menuLeft.value = e.clientX;
  menuTop.value = e.clientY

  if (w - e.clientX < 210) {
    menuLeft.value = w - 210;
  }
  if (h - e.clientY < 450) {
    menuTop.value = h - 470;
  }

  // 设置菜单类型和选中的文件
  if (file) {
    isFile.value = true;
    selectedFile.value = file;
    selectedFileIndex.value = index;
    console.log('选中的文件:', file);
  } else {
    isFile.value = false;
    selectedFile.value = null;
    selectedFileIndex.value = -1;
    console.log('点击在空白区域');
  }

  isMenuOpen.value = true;
};

// 关闭右键菜单
const closeMenu = () => {
  isMenuOpen.value = false;
};

// 点击外部区域关闭菜单
onMounted(() => {
  document.addEventListener('click', closeMenu);
  document.addEventListener('contextmenu', closeMenu);
});

onUnmounted(() => {
  document.removeEventListener('click', closeMenu);
  document.removeEventListener('contextmenu', closeMenu);
});

watch(
  [
    () => pathStore.activeMenu.section,
    () => pathStore.breadcrumbPath[pathStore.breadcrumbPath.length - 1]?.section,
    () => fileManageStore.sortOptions,
  ],
  () => {
    loadFileList();
  }
);

const fileSortBox = ref(false);
const sortOptions = [
  { label: '按名称升序', value: 'name-asc' },
  { label: '按名称降序', value: 'name-desc' },
  { label: '按日期升序', value: 'time-asc' },
  { label: '按日期降序', value: 'time-desc' },
  { label: '按大小升序', value: 'size-asc' },
  { label: '按大小降序', value: 'size-desc' }
];

const selectOption = (value) => {
  fileManageStore.setSortOptions(value);
  setTimeout(() => {
    fileSortBox.value = false;
  }, 100);
};

// 处理双击进入文件夹
const handleFolderDoubleClick = (folder) => {
  if (folder.type === '文件夹') {
    console.log('双击进入文件夹:', folder.name);
    pathStore.setBreadcrumbPath(folder.name);
    const path = pathStore.getBreadcrumbPath();
    console.log('更新后的路径:', path);
    refreshFileList();
  }
};

const setDesigPath = (index) => {
  pathStore.setDesigPath(index + 1);
  refreshFileList();
};

</script>

<template>
  <div class="tool-bar">
    <div class="path-bar">
      <div class="path-item" v-for="(item, index) in pathStore.breadcrumbPath" :key="index">
        <span class="path-item-label" @click="setDesigPath(index)">
          <i class="fas fa-home" v-if="pathStore.getActiveElement(item).icon === 'fas fa-home'"></i>
          {{ pathStore.getActiveElement(item).label || item.label }}
        </span>
        <i class="fas fa-chevron-right" v-show="index < pathStore.breadcrumbPath.length - 1"></i>
      </div>
    </div>
    <div class="tool-left">
      <button class="ref-btn" @click="refreshFileList" title="刷新">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
          class="icon icon-tabler icons-tabler-outline icon-tabler-refresh">
          <path stroke="none" d="M0 0h24v24H0z" fill="none" />
          <path d="M20 11a8.1 8.1 0 0 0 -15.5 -2m-.5 -4v4h4" />
          <path d="M4 13a8.1 8.1 0 0 0 15.5 2m.5 4v-4h-4" />
        </svg>
      </button>
      <div class="divider"></div>
      <button class="more-btn">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
          class="icon icon-tabler icons-tabler-outline icon-tabler-dots">
          <path stroke="none" d="M0 0h24v24H0z" fill="none" />
          <path d="M5 12m-1 0a1 1 0 1 0 2 0a1 1 0 1 0 -2 0" />
          <path d="M12 12m-1 0a1 1 0 1 0 2 0a1 1 0 1 0 -2 0" />
          <path d="M19 12m-1 0a1 1 0 1 0 2 0a1 1 0 1 0 -2 0" />
        </svg>
      </button>
    </div>
    <div class="tool-right">
      <button class="view-toggle" @click="viewMode === 'grid' ? toggleViewMode('list') : toggleViewMode('grid')"
        title="视图">
        <svg v-if="viewMode === 'grid'" class="MuiSvgIcon-root MuiSvgIcon-fontSizeSmall css-vh810p" focusable="false"
          aria-hidden="true" viewBox="0 0 24 24">
          <path
            d="M3 6.25A3.25 3.25 0 0 1 6.25 3h11.5A3.25 3.25 0 0 1 21 6.25v5.772a6.471 6.471 0 0 0 -1.5-.709V10h-4v1.313a6.471 6.471 0 0 0-1.5.709V10h-4v4h2.022a6.471 6.471 0 0 0-.709 1.5H10v4h1.313c.173.534.412 1.037.709 1.5H6.25A3.25 3.25 0 0 1 3 17.75V6.25ZM6.25 4.5A1.75 1.75 0 0 0 4.5 6.25V8.5h4v-4H6.25ZM4.5 10v4h4v-4h-4Zm11-1.5h4V6.25a1.75 1.75 0 0 0-1.75-1.75H15.5v4Zm-1.5-4h-4v4h4v-4Zm-9.5 11v2.25c0 .966.784 1.75 1.75 1.75H8.5v-4h-4Zm9.778-1.525a2 2 0 0 1-1.441 2.497l-.584.144a5.729 5.729 0 0 0 .006 1.807l.54.13a2 2 0 0 1 1.45 2.51l-.187.632c.44.386.94.699 1.484.921l.494-.518a2 2 0 0 1 2.899 0l.498.525a5.281 5.281 0 0 0 1.483-.913l-.198-.686a2 2 0 0 1 1.441-2.496l.584-.144a5.716 5.716 0 0 0-.006-1.808l-.54-.13a2 2 0 0 1-1.45-2.51l.187-.63a5.278 5.278 0 0 0-1.484-.923l-.493.519a2 2 0 0 1-2.9 0l-.498-.525c-.544.22-1.044.53-1.483.912l.198.686ZM17.5 19c-.8 0-1.45-.672-1.45-1.5 0-.829.65-1.5 1.45-1.5.8 0 1.45.671 1.45 1.5 0 .828-.65 1.5-1.45 1.5Z">
          </path>
        </svg>
        <svg v-else xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
          class="icon icon-tabler icons-tabler-outline icon-tabler-list">
          <path stroke="none" d="M0 0h24v24H0z" fill="none" />
          <path d="M9 6l11 0" />
          <path d="M9 12l11 0" />
          <path d="M9 18l11 0" />
          <path d="M5 6l0 .01" />
          <path d="M5 12l0 .01" />
          <path d="M5 18l0 .01" />
        </svg>
        <span>视图</span>
      </button>
      <div class="divider"></div>
      <button title="排序" @click="fileSortBox = !fileSortBox" class="sort-btn">
        <svg class="MuiSvgIcon-root MuiSvgIcon-fontSizeSmall css-vh810p" focusable="false" aria-hidden="true"
          viewBox="0 0 24 24">
          <path
            d="m17.25 4-.1.007a.75.75 0 0 0-.65.743v12.692l-3.22-3.218-.084-.072a.75.75 0 0 0-.976 1.134l4.504 4.5.084.072a.75.75 0 0 0 .976-.073l4.497-4.5.072-.084a.75.75 0 0 0-.073-.977l-.084-.072a.75.75 0 0 0-.977.073L18 17.446V4.75l-.006-.102A.75.75 0 0 0 17.251 4Zm-11.036.22L1.72 8.715l-.073.084a.75.75 0 0 0 .073.976l.084.073a.75.75 0 0 0 .976-.073l3.217-3.218v12.698l.008.102a.75.75 0 0 0 .743.648l.101-.007a.75.75 0 0 0 .649-.743L7.497 6.559l3.223 3.217.084.072a.75.75 0 0 0 .975-1.134L7.275 4.22l-.085-.072a.75.75 0 0 0-.976.073Z">
          </path>
        </svg>
        <span>排序</span>
      </button>
      <!-- 下拉菜单 -->
      <Transition name="slide-fade">
        <ul v-if="fileSortBox" @v-click-outside="closeDropdown" class="file-sort-box">
          <li v-for="option in sortOptions" :key="option.value" class="file-sort-item"
            @click="selectOption(option.value)">
            {{ option.label }}
            <span class="checkmark" v-if="fileManageStore.sortOptions === option.value">✓</span>
          </li>
        </ul>
      </Transition>
      <div class="divider hidden"></div>
      <button class="more-btn hidden">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
          class="icon icon-tabler icons-tabler-outline icon-tabler-dots">
          <path stroke="none" d="M0 0h24v24H0z" fill="none" />
          <path d="M5 12m-1 0a1 1 0 1 0 2 0a1 1 0 1 0 -2 0" />
          <path d="M12 12m-1 0a1 1 0 1 0 2 0a1 1 0 1 0 -2 0" />
          <path d="M19 12m-1 0a1 1 0 1 0 2 0a1 1 0 1 0 -2 0" />
        </svg>
      </button>
    </div>
  </div>

  <div class="file-content" :class="{ 'drag-over': isDragging }" id="fileContent" @dragenter="handleDragEnter"
    @dragleave="handleDragLeave" @dragover="handleDragOver" @drop="handleDrop" @contextmenu.prevent="openMenu">
    <!-- 右键菜单 -->
    <div v-if="isMenuOpen" class="custom-context-menu" :style="{ left: menuLeft + 'px', top: menuTop + 'px' }">
      <!-- 空白区域右键菜单 -->
      <div v-if="!isFile">
        <div class="menu-section">
          <div class="menu-item" @click="handleUploadFile">
            <i class="fas fa-arrow-up"></i>
            <span>上传文件</span>
          </div>
          <div class="menu-item" @click="handleUploadFolder">
            <i class="fas fa-folder-plus"></i>
            <span>上传目录</span>
          </div>
          <div class="menu-item">
            <i class="fas fa-clipboard"></i>
            <span>从剪贴板上传</span>
          </div>
          <div class="menu-item">
            <i class="fas fa-cloud-download-alt"></i>
            <span>离线下载</span>
          </div>
        </div>

        <div class="menu-divider"></div>

        <div class="menu-section">
          <div class="menu-item">
            <i class="fas fa-folder-plus"></i>
            <span>创建文件夹</span>
          </div>
          <div class="menu-item">
            <i class="fas fa-file-alt"></i>
            <span>创建文件</span>
            <i class="fas fa-chevron-right submenu-indicator"></i>

            <!-- 创建文件子菜单 -->
            <div class="submenu">
              <div class="submenu-item">
                <i class="fab fa-markdown"></i>
                <span>Markdown (.md)</span>
              </div>
              <div class="submenu-item">
                <i class="fas fa-project-diagram"></i>
                <span>draw.io</span>
              </div>
              <div class="submenu-item">
                <i class="fas fa-file-alt"></i>
                <span>文本 (.txt)</span>
              </div>
            </div>
          </div>
        </div>
        <div class="menu-divider"></div>
        <div class="menu-section">
          <div class="menu-item" @click="handleRefresh">
            <i class="fas fa-sync-alt"></i>
            <span>刷新</span>
          </div>
        </div>
      </div>

      <!-- 文件/文件夹右键菜单 -->
      <div v-else>
        <div class="menu-section">
          <div class="menu-item" @click="handleDownload">
            <i class="fas fa-download"></i>
            <span>下载</span>
          </div>
          <div class="menu-item">
            <i class="fas fa-share-alt"></i>
            <span>分享</span>
          </div>
        </div>

        <div class="menu-divider"></div>

        <div class="menu-section">
          <div class="menu-item" @click="handleCopy">
            <i class="fas fa-copy"></i>
            <span>复制</span>
          </div>
          <div class="menu-item" @click="handleCut">
            <i class="fas fa-cut"></i>
            <span>剪切</span>
          </div>
          <div class="menu-item">
            <i class="fas fa-clone"></i>
            <span>创建副本</span>
          </div>
        </div>

        <div class="menu-divider"></div>

        <div class="menu-section">
          <div class="menu-item" @click="handleRename">
            <i class="fas fa-edit"></i>
            <span>重命名</span>
          </div>
          <div class="menu-item" @click="handleDelete">
            <i class="fas fa-trash-alt"></i>
            <span>删除</span>
          </div>
        </div>

        <div class="menu-divider"></div>

        <div class="menu-section">
          <div class="menu-item">
            <i class="fas fa-info-circle"></i>
            <span>属性</span>
          </div>
        </div>
      </div>
    </div>


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
            @contextmenu.prevent="openMenu($event, item, index)" @dblclick="handleFolderDoubleClick(item)"
            style="cursor: pointer">
            <div class="file-container-item">
              <div class="file-list-item-icon">
                <i class="fas fa-folder"></i>
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
          <div class="file-list-item" v-for="(item, index) in files" :key="index"
            @contextmenu.prevent="openMenu($event, item, index)">
            <div class="file-container-item">
              <div class="file-list-item-icon">
                <i :class="formatFileType(item.name).icon || 'fa-solid fa-file'"></i>
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
            @contextmenu.prevent="openMenu($event, item, index)" @dblclick="handleFolderDoubleClick(item)"
            style="cursor: pointer">
            <td class="file-name">
              <div class="file-name-container">
                <i class="fas fa-folder"></i>
                <span :title="item.name">{{ item.name }}</span>
              </div>
            </td>
            <td class="file-size">-</td>
            <td class="file-modified">{{ formatDate(item.updateTime) ?? '-' }}</td>
            <td class="file-type">文件夹</td>
          </tr>
          <!-- 文件区 -->
          <tr v-for="(item, index) in files" :key="'file-' + index" class="file-row"
            @contextmenu.prevent="openMenu($event, item, index)" style="cursor: default">
            <td class="file-name">
              <div class="file-name-container">
                <i :class="formatFileType(item.name).icon || 'fa-solid fa-file'"></i>
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
  <input ref="fileInputRef" type="file" style="display:none" @change="onFileInputChange" />
  <input ref="folderInputRef" type="file" style="display:none" webkitdirectory directory multiple
    @change="onFolderInputChange" />
</template>

<style scoped>
.sort-btn {
  padding: 0.4rem;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.file-sort-box {
  position: absolute;
  top: 110%;
  right: 0;
  background-color: var(--card-bg);
  border-radius: var(--card-border-radius);
  box-shadow: var(--shadow-lg);
  z-index: var(--z-index-menu);
}

.file-sort-item {
  display: flex;
  gap: 0.5rem;
  padding: 0.5rem 0.8rem;
  cursor: pointer;
  justify-content: space-between;
  align-items: center;
}

.custom-context-menu {
  position: fixed;
  min-width: 12.5rem;
  background-color: var(--card-bg);
  border-radius: var(--card-border-radius);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-color);
  z-index: var(--z-index-menu);
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 0.8rem 0 0.6rem 1rem;
  cursor: pointer;
  transition: background-color 0.2s;
  position: relative;
}

.menu-item:hover {
  background-color: var(--hover-bg);
}

.menu-item i {
  width: 1.4rem;
  margin-right: 0.8rem;
  display: flex;
  justify-content: center;
}

.submenu-indicator {
  margin-left: auto;
  margin-right: 0;
  font-size: 0.8rem;
  color: var(--text-secondary);
}

i.submenu-indicator {
  margin-right: 0.2rem;
}

.menu-divider {
  height: 1px;
  background-color: var(--border-color);
}

/* 子菜单样式 */
.submenu {
  position: absolute;
  top: 0;
  left: 100%;
  min-width: 14rem;
  background-color: var(--card-bg);
  border-radius: var(--card-border-radius);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-color);
  display: none;
  z-index: var(--z-index-tooltip);
}

.menu-item:hover .submenu {
  display: block;
}

.submenu-item {
  display: flex;
  align-items: center;
  padding: 0.6rem 1.2rem;
  cursor: pointer;
}

.submenu-item:hover {
  background-color: var(--hover-bg);
}

.submenu-item i {
  width: 1.4rem;
  margin-right: 0.8rem;
  display: flex;
  justify-content: center;
}

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

.tool-bar {
  flex: 0 0 3.2rem;
  display: flex;
  gap: 1rem;
}

.tool-bar>div {
  padding: 0 1.25rem;
  background-color: var(--card-bg);
  border-radius: var(--card-border-radius);
  border: 1px solid var(--border-color);
}

.path-bar {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0 1.25rem;
  flex: 1;
}

.tool-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

div.tool-left,
div.tool-right {
  padding: 0 1rem;
}

svg.MuiSvgIcon-root {
  fill: var(--text-color);
}

.tool-left button,
.tool-right button {
  padding: 0 0.4rem;
  background-color: transparent;
  color: var(--text-color);
  font-size: 1rem;
  font-weight: 500;
  box-shadow: none;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.divider {
  width: 1px;
  height: 100%;
  background-color: var(--border-color);
}

.tool-left svg,
.tool-right svg {
  width: 1.4rem;
}

.tool-left button:focus,
.tool-right button:focus {
  outline: none;
}

.tool-left button:hover,
.tool-right button:hover {
  color: var(--primary-color);
}

.tool-right button:hover svg {
  fill: var(--primary-color);
}

.ref-btn {
  border-right-color: var(--i-color);
}

.tool-right {
  position: relative;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  justify-content: space-around;
  padding: 0;
}

/* 视图切换按钮 */
.view-toggle {
  display: flex;
  align-items: center;
}

.view-toggle svg {
  width: 1.4rem;
}

.view-btn i {
  font-size: 1rem;
}

/* 同时匹配 divider 和 hidden 类 */
.divider.hidden,
.more-btn.hidden {
  /* 你的样式 */
  display: none;
}

.path-item-label {
  color: var(--i-color);
  font-size: 0.875rem;
  margin: 0 4px;
}

.path-item-label:hover {
  color: var(--primary-color);
  cursor: pointer;
}

.path-item-label+i {
  color: var(--text-secondary);
  font-size: 0.7rem;
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

.file-container-item {
  display: flex;
  align-items: center;
  line-height: 2.4rem;
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
  content: "释放鼠标上传文件";
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
    transform: scale(1.00);
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

  /* 确保菜单不会超出视口 */
  .custom-context-menu {
    min-width: 14rem;
  }

  .menu-item {
    padding: 0.5rem 1rem;
    font-size: 0.9rem;
  }

  .submenu {
    min-width: 12rem;
    left: auto;
    right: 100%;
  }

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
  .tool-left {
    display: none;
  }

  .file-sort-box {
    right: 18%;
  }

  .tool-right span {
    display: none;
  }

  /* 同时匹配 divider 和 hidden 类 */
  .divider.hidden,
  .more-btn.hidden {
    /* 你的样式 */
    display: flex;
  }

  /* 小屏幕下网格改为两列 */
  .file-list-container.grid-view {
    grid-template-columns: repeat(2, 1fr);
    gap: 0.8rem;
  }
}
</style>
