<!-- src/components/ContextMenu.vue -->
<script setup>
import { computed } from 'vue'

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  },
  menuLeft: {
    type: Number,
    default: 0
  },
  menuTop: {
    type: Number,
    default: 0
  },
  selectedFiles: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits([
  'close-menu',
  'upload-file',
  'upload-folder',
  'refresh',
  'download',
  'rename',
  'delete',
  'copy',
  'cut'
])

const hasSelection = computed(() => props.selectedFiles.length > 0)
const isSingleSelection = computed(() => props.selectedFiles.length === 1)

const closeMenu = () => {
  emit('close-menu')
}

const handleUploadFile = () => {
  emit('upload-file')
  closeMenu()
}

const handleUploadFolder = () => {
  emit('upload-folder')
  closeMenu()
}

const handleRefresh = () => {
  emit('refresh')
  closeMenu()
}

const handleDownload = () => {
  emit('download')
  closeMenu()
}

const handleRename = () => {
  emit('rename')
  closeMenu()
}

const handleDelete = () => {
  emit('delete')
  closeMenu()
}

const handleCopy = () => {
  emit('copy')
  closeMenu()
}

const handleCut = () => {
  emit('cut')
  closeMenu()
}
</script>

<template>
  <div
    v-if="isOpen"
    class="custom-context-menu"
    :style="{ left: menuLeft + 'px', top: menuTop + 'px' }"
    v-click-outside="closeMenu"
  >
    <!-- 空白区域右键菜单 -->
    <div v-if="!hasSelection">
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
    <div v-else-if="isSingleSelection">
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

    <!-- 多选右键菜单 -->
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
        <div class="menu-item" @click="handleDelete">
          <i class="fas fa-trash-alt"></i>
          <span>删除</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
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
  border-radius: var(--card-border-radius);
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
}
</style>
