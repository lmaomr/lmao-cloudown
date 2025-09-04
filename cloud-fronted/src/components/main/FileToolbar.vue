<script setup>
import { ref, computed } from 'vue'
import useFileManageStore from '@/stores/fileManageStore'
import usePathStore from '@/stores/pathStore'

// 定义组件的 props
const props = defineProps({
  viewMode: {
    type: String,
    default: 'grid'
  }
})

// 定义组件的 emits
const emit = defineEmits([
  'refresh',
  'view-toggle',
  'sort-change',
  'path-click'
])

// Store 实例
const pathStore = usePathStore()
const fileManageStore = useFileManageStore()

// 排序相关状态
const fileSortBox = ref(false)
const sortOptions = [
  { label: '按名称升序', value: 'name-asc' },
  { label: '按名称降序', value: 'name-desc' },
  { label: '按日期升序', value: 'time-asc' },
  { label: '按日期降序', value: 'time-desc' },
  { label: '按大小升序', value: 'size-asc' },
  { label: '按大小降序', value: 'size-desc' },
]

// 计算属性来自动截断搜索查询
const truncatedSearchQuery = computed(() => {
  const w = window.screen.width
  const query = pathStore.searchQuery || ''
  const maxLength = w / 60 // 设置最大长度

  if (query.length <= maxLength) {
    return query
  }

  return query.substring(0, maxLength) + '...'
})

// 事件处理函数
const handleRefresh = () => {
  emit('refresh')
}

const handleViewToggle = () => {
  const newMode = props.viewMode === 'grid' ? 'list' : 'grid'
  emit('view-toggle', newMode)
}

const toggleSortBox = () => {
  fileSortBox.value = !fileSortBox.value
}

const closeSortBox = () => {
  fileSortBox.value = false
}

const handleSortOptionSelect = (value) => {
  emit('sort-change', value)
  setTimeout(() => {
    fileSortBox.value = false
  }, 100)
}

const handlePathClick = (index) => {
  emit('path-click', index)
}
</script>

<template>
  <div class="tool-bar">
    <div class="path-bar">
      <div class="path-item" v-for="(item, index) in pathStore.isSearchMode
        ? pathStore.breadcrumbPath.slice(0, 1)
        : pathStore.breadcrumbPath" :key="index">
        <span class="path-item-label" @click="handlePathClick(index)">
          <i class="fas fa-home" v-if="pathStore.getActiveElement(item).icon === 'fas fa-home'"></i>
          {{ pathStore.getActiveElement(item).label || item.path }}
        </span>
        <i class="fas fa-chevron-right"
          v-show="index < pathStore.breadcrumbPath.length - 1 || pathStore.isSearchMode"></i>
        <span v-if="pathStore.searchQuery" class="search-result">搜索结果："{{ truncatedSearchQuery }}"
          <button class="exit-search" title="退出搜索" @click="pathStore.exitSearchMode()">
            <i class="fas fa-times"></i>
          </button>
        </span>
      </div>
    </div>
    <div class="tool-left">
      <button class="ref-btn" @click="handleRefresh" title="刷新">
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
      <button class="view-toggle" @click="handleViewToggle" title="视图">
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
      <button title="排序" @click="toggleSortBox" class="sort-btn">
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
        <ul v-if="fileSortBox" @v-click-outside="closeSortBox" class="file-sort-box">
          <li v-for="option in sortOptions" :key="option.value" class="file-sort-item"
            @click="handleSortOptionSelect(option.value)">
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

.tool-bar {
  flex: 0 0 3.2rem;
  display: flex;
  gap: 1rem;
}

.tool-bar>div {
  padding: 0 0.5rem;
  background-color: var(--card-bg);
  border-radius: var(--card-border-radius);
  border: 1px solid var(--border-color);
}

.path-bar {
  display: flex;
  align-items: center;
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

.path-item-label,
.search-result {
  color: var(--i-color);
  font-size: 0.875rem;
  margin: 0 8.75px;
}

.path-item-label:hover {
  color: var(--primary-color);
  cursor: pointer;
}

.path-item-label+i {
  color: var(--text-secondary);
  font-size: 0.7rem;
}

.exit-search {
  margin-left: 8px;
  color: var(--danger-color);
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px 8px;
  font-size: 0.85rem;
  display: inline-flex;
  align-items: center;
}

.exit-search:focus {
  outline: none;
  outline-offset: 0;
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
}
</style>
