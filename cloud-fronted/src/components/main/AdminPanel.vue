<script setup>
import usePathStore from '@/stores/pathStore';

const pathStore = usePathStore();
</script>

<template>
    <div class="admin-panel">
        <h1>管理面板</h1>
        <div class="empty-file-list dev-empty" id="emptyFileList">
            <div class="empty-file-icon">
                <i class="fas fa-code"></i>
            </div>
            <div class="empty-file-title">管理面板正在开发中</div>
            <div class="empty-file-description">此功能正在开发中，敬请期待</div>
            <div class="empty-file-actions">
                <button class="btn btn-primary go-to-files-btn" @click="pathStore.setActiveMenu('文件分类', 'my-files')">
                    <i class="fas fa-folder-open"></i> 返回我的文件
                </button>
            </div>
        </div>
    </div>
</template>


<style scoped>
.admin-panel {
    padding: 1rem 3rem;
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    gap: 0.8rem;
    background-color: var(--card-bg);
    border: 1px solid var(--border-color);
    border-radius: var(--card-border-radius);

}
.empty-file-list {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 1.2rem;
    flex: 1;
}

.empty-file-title {
    font-size: 1.2rem;
    font-weight: 600;
}

.empty-file-list .fa-code{
    font-size: 3rem;
}

.empty-file-icon {
    width: 6rem;
    height: 6rem;
    border-radius: 50%;
    background-color: rgba(96, 125, 139, 0.1);
    display: flex;
    align-items: center;
    justify-content: center;
}

.fa-code:before {
    content: "\f121";
    color: #607d8b;
}

.fa-solid, .fas {
    font-family: "Font Awesome 6 Free";
    font-weight: 800;
}

.empty-file-description {
    font-size: 1rem;
    color: var(--i-color);
}
</style>
