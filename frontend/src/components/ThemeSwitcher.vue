<template>
  <el-switch
    v-model="isDark"
    class="magic-theme-switch"
    inline-prompt
    :active-icon="Moon"
    :inactive-icon="Sunny"
    style="--el-switch-on-color: #091e42; --el-switch-off-color: #ea4335"
  />
</template>

<script setup>
import { useDark } from '@vueuse/core';
import { Moon, Sunny } from '@element-plus/icons-vue';

const isDark = useDark({ valueDark: 'dark', valueLight: '' });
</script>

<style scoped>
.magic-theme-switch :deep(.el-switch__core) { border-radius: 20px; box-shadow: inset 0 2px 4px rgba(0,0,0,0.2) !important;}
</style>
