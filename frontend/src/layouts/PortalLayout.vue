<template>
  <el-container class="portal-shell">
    <el-header class="portal-header">
      <div class="brand-box">
        <div class="brand-logo">农</div>
        <div>
          <div class="brand-title">农业合作社综合财务业务管理系统</div>
          <div class="brand-subtitle">农村信用社综合财务业务管理系统</div>
        </div>
      </div>
      <div class="header-search">
        <el-input v-model="searchKeyword" placeholder="请输入用户、交易、报销单、公告或留言关键词" clearable @keyup.enter="handleSearch">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <div class="header-actions">
        <el-badge :value="5" class="action-badge">
          <el-button circle>
            <el-icon><Bell /></el-icon>
          </el-button>
        </el-badge>
        <div class="user-chip">
          <el-avatar size="small">{{ userName.slice(0, 1) }}</el-avatar>
          <div>
            <div class="user-name">{{ userName }}</div>
            <el-tag size="small" type="primary">{{ roleName }}</el-tag>
          </div>
        </div>
        <el-button type="danger" plain @click="logout">退出</el-button>
      </div>
    </el-header>

    <el-container class="portal-body">
      <el-aside :width="collapsed ? '82px' : '252px'" class="portal-aside">
        <div class="aside-toggle" @click="collapsed = !collapsed">
          <el-icon><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
          <span v-if="!collapsed">收起导航</span>
        </div>
        <el-menu :default-active="route.path" class="side-menu" :collapse="collapsed" router>
          <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
            <el-icon><Menu /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="portal-main">
        <div class="page-head">
          <el-breadcrumb separator=">">
            <el-breadcrumb-item>{{ roleName }}</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <router-view />
      </el-main>
    </el-container>
  </el-container>

  <el-dialog v-model="appStore.searchVisible" title="全局搜索结果" width="720px">
    <div v-if="appStore.searchLoading" class="dialog-empty">正在检索...</div>
    <div v-else-if="!appStore.searchResults.length" class="dialog-empty">暂无匹配结果</div>
    <div v-else class="search-results">
      <div v-for="item in appStore.searchResults" :key="`${item.module}-${item.id}`" class="search-item">
        <div class="search-title">{{ item.title }}</div>
        <div class="search-text">{{ item.text }}</div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Bell, Expand, Fold, Menu, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import http from '../api'
import { appStore, clearSession } from '../stores/app'
import { portalMenus } from '../router/modules'

const route = useRoute()
const router = useRouter()
const collapsed = ref(false)
const searchKeyword = ref('')

const currentRole = computed(() => route.path.split('/')[1] || appStore.user?.role || 'admin')
const menus = computed(() => portalMenus[currentRole.value] || [])
const userName = computed(() => appStore.user?.name || '访客')
const roleName = computed(() => appStore.user?.roleName || '未登录')

async function handleSearch() {
  appStore.searchVisible = true
  appStore.searchLoading = true
  try {
    const result = await http.get('/search', {
      params: { role: currentRole.value, keyword: searchKeyword.value }
    })
    appStore.searchResults = result.data.results || []
  } catch (error) {
    ElMessage.error(String(error))
  } finally {
    appStore.searchLoading = false
  }
}

function logout() {
  clearSession()
  router.push('/login')
}
</script>
