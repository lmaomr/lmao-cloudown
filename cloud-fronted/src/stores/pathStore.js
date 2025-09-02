// stores/pathStore.ts
import { ref, watch } from 'vue';
import { defineStore } from 'pinia';
import { safeLocalStorage } from '@/utils/storage';

const usePathStore = defineStore('path', () => {
  // 是否处于搜索模式
  const isSearchMode = ref(false);

  const menuItems = ref([
    {
      title: '文件分类',
      items: [
        { section: 'my-files', label: '我的文件', icon: 'fas fa-home' },
        { section: 'images', label: '图片', icon: 'fas fa-image' },
        { section: 'videos', label: '视频', icon: 'fas fa-film' },
        { section: 'music', label: '音乐', icon: 'fas fa-music' },
        { section: 'documents', label: '文档', icon: 'fas fa-file-alt' },
        { section: 'others', label: '其他', icon: 'fas fa-file-archive' },
        { section: 'trash', label: '回收站', icon: 'fas fa-trash' }
      ]
    },
    {
      title: '更多',
      items: [
        { section: 'shared', label: '互发共享', icon: 'fas fa-share-alt' },
        { section: 'connections', label: '连接与挂载', icon: 'fas fa-link' },
        { section: 'offline-download', label: '离线下载', icon: 'fas fa-cloud-download-alt' },
        { section: 'admin-panel', label: '管理面板', icon: 'fas fa-tools' }
      ]
    }
  ]);

  // 当前激活的菜单项
  const activeMenu = ref(
    JSON.parse(safeLocalStorage.get('activeMenu') || 'null') || {
      title: '文件分类',
      section: 'my-files'
    }
  );

  // 面包屑路径
  const breadcrumbPath = ref(
    JSON.parse(safeLocalStorage.get('breadcrumbPath') || '[]') || [
      {
        title: '文件分类',
        section: 'my-files'
      }
    ]
  );

  // 设置当前激活的菜单项
  const setActiveMenu = (title, section) => {
    activeMenu.value = { title: title, section: section };
    safeLocalStorage.set('activeMenu', JSON.stringify(activeMenu.value));
  };

  //跳转到指定路径
  const setDesigPath = (index) => {
    breadcrumbPath.value.splice(index);
    if (breadcrumbPath.value.length === 1) {
      setActiveMenu(breadcrumbPath.value[0].title, breadcrumbPath.value[0].section);
    }
  }

  const getBreadcrumbPath = () => {
    let path = '';
    breadcrumbPath.value.forEach(p => {
      if (p.section === activeMenu.value.section && activeMenu.value.section !== 'my-files') {
        return;
      }
      if (p.title === '目录' && isSearchMode.value === false) {
        path += p.path + '/';
      } else {
        path += p.section + '/';
      }
    });
    return path;
  }

  //监听当前激活的菜单项
  watch(activeMenu, (newActiveMenu) => {
    if (!newActiveMenu) return;
    // 基础路径项
    const baseItem = { title: '文件分类', section: 'my-files' };
    if (newActiveMenu.title === '文件分类' && newActiveMenu.section !== 'my-files') {
      breadcrumbPath.value = [
        baseItem,
        {
          title: newActiveMenu.title,
          section: newActiveMenu.section
        }
      ];
    }else {
      breadcrumbPath.value = [baseItem];
    }
    // 统一存储
    safeLocalStorage.set('breadcrumbPath', JSON.stringify(breadcrumbPath.value));
  }, { immediate: true }); // 建议添加immediate选项初始化

  //获取当前section的label和icon
  const getActiveElement = (item) => {
    for (const element of menuItems.value) {
      const group = element.items.find(val => val.section === item.section);
      if (group) return group;
    }
    return { section: '', label: '', icon: '' };
  };

  // 设置面包屑路径
  const setBreadcrumbPath = (path) => {
    breadcrumbPath.value.push({
      title: '目录',
      path: path
    });
    safeLocalStorage.set('breadcrumbPath', JSON.stringify(breadcrumbPath.value));
  };

  return {
    menuItems,
    activeMenu,
    breadcrumbPath,
    setActiveMenu,
    setDesigPath,
    setBreadcrumbPath,
    getActiveElement,
    getBreadcrumbPath
  };
});

export default usePathStore;
