<template>
  <el-container class="portal-shell" :class="{'mobile-terminal': isMobile}">
    <el-header class="portal-header print-hide" v-if="!isMobile">
      <div class="brand-box">
        <div class="brand-logo">农</div>
        <div>
          <div class="brand-title">农业合作社综合财务业务管理系统</div>
          <div class="brand-subtitle">农村信用社综合财务业务管理系统</div>
        </div>
      </div>
      
      <!-- Placeholder that says "Press Cmd+K to search" -->
      <div class="header-search pointer" @click="searchStore.toggleSearch()">
        <el-input readonly placeholder="唤醒全局探针 (⌘+K / Ctrl+K)" class="cmd-k-input">
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
        <el-button type="danger" plain @click="logout" size="small">退出</el-button>
      </div>
    </el-header>

    <el-container class="portal-body">
      <!-- PC Sidebar -->
      <el-aside :width="collapsed ? '82px' : '252px'" class="portal-aside print-hide" :class="{'is-collapsed': collapsed}" v-if="!isMobile">
        <div class="aside-toggle" @click="collapsed = !collapsed">
           <el-icon><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
           <span v-if="!collapsed">收起导航</span>
        </div>
        <el-menu :default-active="route.path" class="side-menu" :collapse="collapsed" router>
           <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
             <el-icon><component :is="item.icon || 'Menu'" /></el-icon>
             <template #title>
               <span class="menu-title">{{ item.title }}</span>
             </template>
           </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="portal-main">
        <div class="page-head print-hide" v-if="!isMobile">
           <el-breadcrumb separator=">">
             <el-breadcrumb-item>{{ roleName }}</el-breadcrumb-item>
             <el-breadcrumb-item>{{ route.meta.title }}</el-breadcrumb-item>
           </el-breadcrumb>
        </div>
        
        <router-view v-slot="{ Component, route }">
           <transition name="fade-slide" mode="out-in">
             <component :is="Component" :key="route.path" />
           </transition>
        </router-view>
      </el-main>
    </el-container>
    
    <!-- Mobile Tabbar -->
    <nav v-if="isMobile" class="mobile-tabbar glass-panel print-hide">
       <div class="m-tab" :class="{'active': route.path.includes('/dashboard')}" @click="router.push(`/${currentRole}/dashboard`)">
          <el-icon><Odometer /></el-icon>
          <span>大盘</span>
       </div>
       <div class="m-tab" :class="{'active': route.path.includes('/deposits') || route.path.includes('/withdrawals')}" @click="router.push(`/${currentRole}/deposits`)">
          <el-icon><Wallet /></el-icon>
          <span>收支</span>
       </div>
       <div class="m-tab" @click="searchStore.toggleSearch()">
          <el-icon><Search /></el-icon>
          <span>寻迹</span>
       </div>
       <div class="m-tab" :class="{'active': route.path.includes('/messages')}" @click="router.push(`/${currentRole}/messages`)">
          <el-icon><Bell /></el-icon>
          <span>信箱</span>
       </div>
       <div class="m-tab" :class="{'active': route.path.includes('/profile')}" @click="router.push(`/${currentRole}/profile`)">
          <el-icon><User /></el-icon>
          <span>我</span>
       </div>
    </nav>

    <!-- Global Searcb component -->
    <CommandPalette />
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Bell, Expand, Fold, Menu, Search, Odometer, Wallet, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useWindowSize } from '@vueuse/core'
import { appStore, clearSession } from '../stores/app'
import { portalMenus } from '../router/modules'
import CommandPalette from '../components/CommandPalette.vue'
import { useSearchStore } from '../stores/useSearchStore'

const route = useRoute()
const router = useRouter()
const searchStore = useSearchStore()
const collapsed = ref(false)

const currentRole = computed(() => route.path.split('/')[1] || appStore.user?.role || 'admin')
const menus = computed(() => portalMenus[currentRole.value] || [])
const userName = computed(() => appStore.user?.name || '访客')
const roleName = computed(() => appStore.user?.roleName || '未登录')

const { width } = useWindowSize()
const isMobile = computed(() => width.value <= 768)

function logout() {
  clearSession()
  router.push('/login')
}
</script>

<style scoped>
.portal-shell { min-height: 100vh; display: flex; flex-direction: column; background: var(--color-bg); }

.pointer { cursor: pointer; }
.cmd-k-input :deep(.el-input__wrapper) { background: var(--color-surface-hover); cursor: pointer;}
.mr-3 { margin-right: 15px; }

/* 炫酷的侧边栏菜单样式重塑 */
.portal-aside {
  background: var(--color-surface);
  border-right: 1px solid var(--color-border);
  transition: width 0.3s cubic-bezier(0.2, 0.8, 0.2, 1);
  overflow-x: hidden;
  box-shadow: var(--shadow-sm);
  z-index: 10;
  display: flex;
  flex-direction: column;
}

.aside-toggle {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  color: var(--color-text-secondary);
  cursor: pointer;
  border-bottom: 1px solid var(--color-border);
  transition: all 0.3s;
  background: var(--color-surface-hover);
}
.aside-toggle:hover {
  color: var(--color-primary);
  background: rgba(0, 82, 204, 0.05);
}
.aside-toggle .el-icon {
  font-size: 20px;
  margin-right: 12px;
}
.aside-toggle span {
  font-weight: 600;
  font-size: 14px;
  letter-spacing: 0.5px;
}

:deep(.side-menu) {
  flex: 1;
  border-right: none;
  background: transparent;
  padding: 16px 12px;
}

:deep(.side-menu .el-menu-item) {
  height: 50px;
  line-height: 50px;
  border-radius: 12px;
  margin-bottom: 8px;
  color: var(--color-text-secondary);
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  overflow: hidden;
}

:deep(.side-menu .el-menu-item .el-icon) {
  font-size: 20px;
  margin-right: 14px;
  transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

:deep(.side-menu .el-menu-item:hover) {
  background: rgba(0, 82, 204, 0.05);
  color: var(--color-primary);
  transform: translateX(4px);
}
html[data-theme="dark"] :deep(.side-menu .el-menu-item:hover) {
  background: rgba(76, 154, 255, 0.1);
}

:deep(.side-menu .el-menu-item.is-active) {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
  color: #fff !important;
  box-shadow: 0 6px 16px rgba(0, 82, 204, 0.25);
  transform: translateX(0);
}

:deep(.side-menu .el-menu-item.is-active .el-icon) {
  color: #fff;
  transform: scale(1.15);
}

.portal-aside.is-collapsed .aside-toggle {
  justify-content: center;
  padding: 0;
}
.portal-aside.is-collapsed .aside-toggle .el-icon {
  margin-right: 0;
}

:deep(.el-menu--collapse.side-menu .el-menu-item) {
  padding: 0 !important;
  display: flex !important;
  justify-content: center;
  align-items: center;
}
:deep(.el-menu--collapse.side-menu .el-menu-item .el-icon) {
  margin-right: 0;
}
:deep(.el-menu--collapse.side-menu .el-menu-item:hover),
:deep(.el-menu--collapse.side-menu .el-menu-item.is-active) {
  transform: translateY(-2px);
}

/* 炫酷的 App 级移动底栏 */
.mobile-tabbar {
  position: fixed; bottom: 0; left: 0; width: 100vw; height: 75px;
  display: flex; justify-content: space-around; align-items: center;
  border-top: 1px solid var(--color-border); z-index: 999;
  padding-bottom: env(safe-area-inset-bottom);
  background: rgba(var(--color-surface), 0.9); backdrop-filter: blur(20px);
}
.m-tab { display: flex; flex-direction: column; align-items: center; justify-content: center; color: var(--color-text-secondary); width: 60px; transition: var(--transition-smooth); cursor: pointer;}
.m-tab .el-icon { font-size: 24px; margin-bottom: 4px; }
.m-tab span { font-size: 11px; font-weight: 700; }
.m-tab.active { color: var(--color-primary); transform: translateY(-4px); }

.mobile-terminal .portal-main { padding-bottom: 90px; }

.fade-slide-enter-active, .fade-slide-leave-active { transition: all 0.3s cubic-bezier(0.2, 0.8, 0.2, 1); }
.fade-slide-enter-from { opacity: 0; transform: translateY(15px); }
.fade-slide-leave-to { opacity: 0; transform: translateY(-15px); }

@media print { .print-hide { display: none !important; } .portal-main { padding: 0 !important; } }
</style>
