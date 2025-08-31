<script setup>
import { RouterView } from 'vue-router'
import GlobalLoading from '@/components/common/GlobalLoading.vue'
</script>

<template>
  <div class="app">
    <RouterView />
    <GlobalLoading />
  </div>
</template>

<style scoped>
.app {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}
</style>
