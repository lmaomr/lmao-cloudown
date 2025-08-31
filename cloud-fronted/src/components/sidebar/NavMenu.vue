<script setup>

/**
 * 定义组件的属性 (Props)
 * - menuItems: 必传的数组类型，用于生成导航菜单的分组及项目
 * - activeSection: 字符串类型，表示当前激活的菜单项标识
 */
defineProps({
  menuItems: {
    type: Array,
    required: true,
    default: () => []  // 默认值为空数组
  },
  activeSection: {
    type: String,
    default: ''  // 默认空字符串表示没有激活项
  }
});

/**
 * 定义组件可触发的事件
 * - 声明一个名为 'menuAction' 的自定义事件，
 *   当菜单项被点击时触发，传递被选中的 section 标识和标题
 */
defineEmits(['menuAction']);
</script>

<template>
  <!-- 导航容器 -->
  <nav>
    <!-- 循环遍历菜单分组 (group) -->
    <div v-for="(group, index) in menuItems" :key="index">
      <!-- 如果不是第一组菜单项，显示分组标题和分隔线 -->
      <div v-if="index !== 0" class="group-title">
        <!-- 分组分隔线 -->
        <div class="nav-divider"></div>
        <!-- 分组标题 -->
        <div class="nav-title">{{ group.title }}</div>
      </div>

      <!-- 菜单项分组容器 -->
      <!-- 存储section标识便于调试 -->
      <!-- 点击时触发select事件 -->
      <!-- 动态添加active类 -->
      <ul class="nav-group">
        <!-- 循环遍历当前分组中的每个菜单项 -->
        <li v-for="item in group.items" :key="item.section" :class="{ active: activeSection === item.section }"
          :data-section="item.section" @click="$emit('menuAction', { title: group.title, section: item.section })">
          <!-- 可点击的菜单链接 -->
          <!-- 阻止默认链接跳转行为 -->
          <!-- 无障碍标签 -->
          <a href="#" @click.prevent :aria-label="item.label">
            <!-- 菜单项图标 -->
            <i :class="item.icon"></i>
            <!-- 菜单项文本标签 -->
            <span>{{ item.label }}</span>
          </a>
        </li>
      </ul>
    </div>
  </nav>
</template>

<style scoped>
/* 导航容器基本样式 */
nav {
  display: flex;
  /* 垂直排列 */
  flex-direction: column;
  /* 分组间距 */
  gap: 0.75rem;
}

/* 单个菜单项样式 */
.nav-group li {
  /* 内边距 */
  padding: 8px 18px;
  /* 指针样式 */
  cursor: pointer;
  /* 平滑过渡效果 */
  transition: margin-left var(--transition-speed) ease-in-out;
  /*弹性布局 */
  display: flex;
  /* 垂直居中 */
  align-items: center;
  /* 圆角 */
  border-radius: var(--menu-border-radius);
  /* 外边距 */
  margin: 2px 6px;
}

/* 激活状态和悬停状态的菜单项 */
.nav-group li.active,
.nav-group li:hover {
  /* 使用CSS变量替代硬编码颜色 */
  background: var(--active-bg, #eaf3ff);
  color: var(--primary-color);
}

/* 菜单链接样式 */
.nav-group li a {
  /* 去除下划线 */
  text-decoration: none;
  /* 继承父元素颜色 */
  color: inherit;
  /*弹性布局 */
  display: flex;
  /*垂直居中 */
  align-items: center;
  /* 图标和文字间距 */
  gap: 10px;
  /* 占满容器宽度 */
  width: 100%;
}

/* 菜单图标基础样式 */
.nav-group li i {
  /* 图标字体 */
  font-size: calc(var(--i-font-size));
  /* 固定宽度 */
  width: 20px;
  /* 居中图标 */
  text-align: center;
  /* 默认图标颜色 */
  color: var(--i-color);

}

/* 激活/悬停状态的图标颜色 */
.nav-group li.active i,
.nav-group li:hover i {
  color: var(--primary-color);
  /* 主题色图标 */
}

.nav-group li:hover {
  margin-left: 0.8rem;
}

/* 激活菜单项的特殊指示样式 */
.nav-group li.active {
  /* 左侧主题色边框 */
  border-left: 3px solid var(--primary-color);
  margin-left: 3px;
}

/* 分组分隔线样式 */
.nav-divider {
  /* 1像素高度 */
  height: 1px;
  /* 浅灰色 */
  background-color: #dee2e6;
  /* 左右外边距 */
  margin: 0 12px;
}

/* 分组标题样式 */
.nav-title {
  font-size: 0.8rem;
  /* 较小字号 */
  /* 次级文字色 */
  color: var(--text-secondary);
  /* 内边距 */
  padding: 6px 18px 2px 18px;
  /* 上外边距 */
  margin-top: 8px;
  /* 下外边距 */
  margin-bottom: 2px;
  /* 字母间距 */
  letter-spacing: 1px;
}
</style>
