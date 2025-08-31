<script setup>
defineProps({
  ui: {
    type: String,
    default: 'fas fa-file-alt'
  },
  title: {
    type: String,
    default: '这是一个标题'
  },
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'confirm'])

const handleClose = () => {
  emit('close')
}

const handleConfirm = () => {
  emit('confirm')
}
</script>

<template>
  <Transition name="modal">
    <div v-show="visible" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <div class="modal-title">
            <i :class="ui"></i>
            {{ title }}
          </div>
          <button class="modal-close" @click="handleClose">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <slot></slot>
        </div>
        <div class="modal-footer">
          <button class="btn btn-cancel" @click="handleClose">取消</button>
          <button class="btn btn-submit" @click="handleConfirm">确定</button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: var(--z-index-modal);
  backdrop-filter: blur(4px);
}

/* 进入和离开动画 */
.modal-enter-active,
.modal-leave-active {
  transition: opacity var(--transition-speed) ease;
}

.modal-enter-active .modal,
.modal-leave-active .modal {
  transition: transform var(--transition-speed) ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal {
  transform: translateY(20px);
}

.modal-leave-to .modal {
  transform: translateY(20px);
}

.modal {
  background-color: var(--card-bg, #fff);
  border-radius: 8px;
  box-shadow: var(--shadow-md, 0 2px 4px rgba(0, 0, 0, 0.1));
  width: 400px;
  max-width: 90%;
  overflow: hidden;
  border: 1px solid var(--border-color, #e5e6eb);
  min-width: 32rem;
}

.modal .fas {
  color: var(--primary-color);
  font-size: 1.4rem;
}

.modal-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color, #e5e6eb);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.modal-title {
  font-size: 16px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-color, #2b3e51);
}

.modal-title i {
  color: var(--primary-color, #1976d2);
}

.modal-close {
  background: transparent;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: var(--text-secondary, #8a8f99);
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s;
}

.modal-close:hover {
  background-color: var(--button-hover);
  color: var(--text-color, #2b3e51);
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  padding: 16px 20px;
  border-top: 1px solid var(--border-color, #e5e6eb);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn {
  padding: 10px 16px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  transition: all 0.2s;
  font-weight: 500;
}

.btn-cancel {
  background-color: transparent;
  color: var(--text-color, #2b3e51);
  border: 1px solid var(--border-color, #e5e6eb);
}

.btn-cancel:hover {
  background-color: var(--button-hover);
}

.btn-submit {
  background-color: var(--primary-color, #1976d2);
  color: #fff;
}

.btn-submit:hover {
  background-color: var(--primary-hover, #1565c0);
}

/* 响应式适配 */
@media (max-width: 768px) {
  .modal {
    width: 90%;
    min-width: 28rem;
  }

  .btn {
    padding: 8px 14px;
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .modal {
    width: 90%;
    min-width: 24rem;
  }

  .modal-header,
  .modal-body,
  .modal-footer {
    padding: 12px 16px;
  }

  .modal-title {
    font-size: 15px;
  }
}
</style>
