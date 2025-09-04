<!-- src/components/ContextMenu.vue -->
<script setup>
import { ref, computed, nextTick } from 'vue'
import useFileManageStore from '@/stores/fileManageStore'
import { useUploadStore } from '@/stores/uploadStore.js'
import toast from '@/utils/toast.js'

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
  'refresh-file-list',
  'clear-selection'
])

// Store 实例
const fileManageStore = useFileManageStore()
const uploadStore = useUploadStore()

// 文件输入引用
const fileInputRef = ref(null)
const folderInputRef = ref(null)

const hasSelection = computed(() => props.selectedFiles.length > 0)
const isSingleSelection = computed(() => props.selectedFiles.length === 1)

const closeMenu = () => {
  emit('close-menu')
}

// 上传单个文件
const uploadFile = async (file) => {
  if (!file) {
    toast.warning('文件格式有误', '请选择一个有效的文件')
    return
  }
  try {
    await uploadStore.addUploadTask(file)
    await fileManageStore.getFileList()
  } catch (error) {
    console.error('操作失败:', error)
    toast.error('操作失败', error.message || '上传过程中发生错误')
  }
}

// 右键菜单：上传文件
const handleUploadFile = () => {
  closeMenu()
  nextTick(() => fileInputRef.value && fileInputRef.value.click())
}

// 文件选择变化处理
const onFileInputChange = async (e) => {
  const files = e.target.files;
  if (!files || files.length === 0) return;

  const loadingId = toast.loading('开始上传文件...', `0/${files.length} 个文件`);
  let completedCount = 0;

  try {
    // 创建所有上传任务的Promise
    const uploadPromises = Array.from(files).map((file) =>
      uploadFile(file)
        .then(() => {
          completedCount++;
          toast.updateLoading(loadingId, '上传中...', `${completedCount}/${files.length} 完成`);
          return { success: true, file };
        })
        .catch(error => {
          completedCount++;
          console.error(`文件 ${file.name} 上传失败:`, error);
          toast.updateLoading(loadingId, '上传中...', `${completedCount}/${files.length} 完成 (有文件失败)`);
          return { success: false, file, error };
        })
    );

    // 等待所有上传任务完成（无论成功失败）
    const results = await Promise.allSettled(uploadPromises);

    // 统计结果
    const successfulUploads = results.filter(result =>
      result.status === 'fulfilled' && result.value.success
    );
    const failedUploads = results.filter(result =>
      result.status === 'fulfilled' && !result.value.success
    );

    toast.closeLoading(loadingId);

    // 显示最终结果
    if (failedUploads.length === 0) {
      toast.success('上传成功', `所有 ${successfulUploads.length} 个文件上传完成`);
    } else if (successfulUploads.length > 0) {
      toast.warning('部分成功', `成功: ${successfulUploads.length}, 失败: ${failedUploads.length}`);
    } else {
      toast.error('全部失败', '所有文件上传都失败了');
    }

    // 只要有成功的文件就刷新列表
    if (successfulUploads.length > 0) {
      await fileManageStore.getFileList();
    }

  } catch (error) {
    toast.closeLoading(loadingId);
    console.error('上传过程错误:', error);
    toast.error('上传失败', '上传过程中发生错误');
  } finally {
    e.target.value = '';
  }
}

// 右键菜单：上传目录
const handleUploadFolder = () => {
  closeMenu()
  nextTick(() => folderInputRef.value && folderInputRef.value.click())
}

// 文件夹选择变化处理
const onFolderInputChange = async (e) => {
  const files = e.target.files
  if (files && files.length > 0) {
    for (const file of files) {
      await uploadFile(file)
    }
    await fileManageStore.getFileList()
  }
  e.target.value = ''
}

// 右键菜单：刷新
const handleRefresh = () => {
  emit('refresh-file-list')
  closeMenu()
}

// 文件操作：重命名
const handleRename = () => {
  console.log('重命名文件:', props.selectedFiles)
  // 这里实现重命名逻辑
  closeMenu();
}

// 文件操作：下载
const handleDownload = async () => {
  // 检查是否有选中的文件
  if (props.selectedFiles.length === 0) {
    toast.info('提示', '请先选择要下载的文件')
    return
  }

  const toastId = toast.loading('正在准备下载文件...', '请稍候')

  try {
    // 过滤出可下载的文件（排除文件夹）
    const downloadableFiles = props.selectedFiles.filter(f => f.type !== '文件夹')

    // 检查是否有文件夹被选中
    const folderFiles = props.selectedFiles.filter(f => f.type === '文件夹')
    if (folderFiles.length > 0) {
      toast.warning('跳过文件夹', `跳过了 ${folderFiles.length} 个文件夹，无法下载文件夹`)
    }

    // 如果没有可下载的文件
    if (downloadableFiles.length === 0) {
      toast.info('提示', '没有可下载的文件')
      return
    }

    // 批量下载文件
    const downloadPromises = downloadableFiles.map(f =>
      fileManageStore.downloadFile(f).catch((error) => {
        // 单个文件下载失败不会影响其他文件
        console.error(`下载文件 ${f.name} 失败:`, error)
        return { f, success: false, error }
      }),
    )

    // 等待所有下载完成
    const results = await Promise.allSettled(downloadPromises)

    // 统计下载结果
    const successCount = results.filter(
      (result) => result.status === 'fulfilled' && result.value?.success !== false,
    ).length

    const failedCount = results.length - successCount

    // 显示总结信息
    if (successCount > 0 && failedCount === 0) {
      toast.success('下载完成', `成功下载 ${successCount} 个文件`)
    } else if (successCount > 0 && failedCount > 0) {
      toast.warning('部分完成', `成功下载 ${successCount} 个文件，失败 ${failedCount} 个`)
    } else {
      toast.error('下载失败', '所有文件下载失败')
    }
  } catch (error) {
    console.error('下载过程发生错误:', error)
    toast.error('下载失败', error.message || '下载过程中发生未知错误')
  } finally {
    toast.closeLoading(toastId)
    closeMenu();
  }
}

// 文件操作：删除（支持多文件）
const handleDelete = async () => {
  console.log('删除文件:', props.selectedFiles)

  // 检查是否有选中的文件
  if (props.selectedFiles.length === 0) {
    toast.info("提示", "请选择要删除的文件")
    return
  }

  const toastId = toast.loading('正在准备删除文件...', '请稍候')

  try {
    // 批量删除文件
    const deletePromises = props.selectedFiles.map(file =>
      fileManageStore.deleteFile(file).catch(error => {
        // 单个文件删除失败不会影响其他文件
        console.error(`删除文件 ${file.name} 失败:`, error)
        return { file, success: false, error }
      })
    )

    // 等待所有删除操作完成
    const results = await Promise.allSettled(deletePromises)

    // 统计删除结果
    const successResults = results.filter(result =>
      result.status === 'fulfilled' && result.value === true
    )
    const successCount = successResults.length
    const failedCount = results.length - successCount
    console.log('成功删除' + successCount + '个文件');
    console.log('失败' + failedCount + '个文件')

    // 显示总结信息
    if (successCount > 0 && failedCount === 0) {
      toast.success('删除成功', `成功删除 ${successCount} 个文件`)
    } else if (successCount > 0 && failedCount > 0) {
      toast.warning('部分完成', `成功删除 ${successCount} 个文件，失败 ${failedCount} 个`)
    } else {
      toast.error('删除失败', '所有文件删除失败，请重试')
    }

  } catch (error) {
    console.error('删除过程发生错误:', error)
    toast.error('删除失败', error.message || '删除过程中发生未知错误')
  } finally {
    toast.closeLoading(toastId)
    closeMenu();
    // 清空选中状态
    emit('clear-selection')
  }
}

// 文件操作：复制
const handleCopy = () => {
  console.log('复制文件:', props.selectedFiles)
  // 这里实现复制逻辑
  closeMenu();
}

// 文件操作：剪切
const handleCut = () => {
  console.log('剪切文件:', props.selectedFiles)
  // 这里实现剪切逻辑
  closeMenu();
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
  
  <!-- 隐藏的文件输入元素 -->
  <input ref="fileInputRef" type="file" style="display: none" multiple @change="onFileInputChange" />
  <input ref="folderInputRef" type="file" style="display: none" webkitdirectory directory multiple
    @change="onFolderInputChange" />
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
