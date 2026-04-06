import { createRouter, createWebHistory } from 'vue-router'
import { portalMenus } from './modules'
import { appStore } from '../stores/app'
import PortalLayout from '../layouts/PortalLayout.vue'
import LoginView from '../views/LoginView.vue'
import DashboardView from '../views/DashboardView.vue'
import FinanceTransactionView from '../views/FinanceTransactionView.vue'
import AiView from '../views/AiView.vue'

function resolveComponent(item) {
  if (item.view === 'dashboard') return DashboardView
  if (item.view === 'transaction') return FinanceTransactionView
  if (item.view === 'ai') return AiView
  if (item.view === 'module' && item.moduleKey) {
    const key = item.moduleKey
    let name = key.split('-').map(p => p.charAt(0).toUpperCase() + p.slice(1)).join('') + 'View'
    if (key === 'balance') name = 'BalancesView'
    if (key === 'deposits') name = 'UserDepositsView'
    if (key === 'withdrawals') name = 'UserWithdrawalsView'
    return () => import(`../views/${name}.vue`)
  }
}

function createPortalRoute(role) {
  return {
    path: `/${role}`,
    component: PortalLayout,
    redirect: portalMenus[role][0].path,
    children: portalMenus[role].map((item) => ({
      path: item.path.replace(`/${role}/`, ''),
      name: item.path,
      component: resolveComponent(item),
      meta: {
        role,
        title: item.title,
        moduleKey: item.moduleKey || '',
        transactionKind: item.transactionKind || ''
      }
    }))
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', component: LoginView, meta: { public: true, title: '登录' } },
    createPortalRoute('admin'),
    createPortalRoute('finance'),
    createPortalRoute('user')
  ]
})

router.beforeEach((to, from, next) => {
  document.title = `${to.meta?.title || '系统'} - 农业合作社综合财务业务管理系统`
  if (to.meta.public) {
    next()
    return
  }
  if (!appStore.token) {
    next('/login')
    return
  }
  const role = appStore.user?.role;
  if (to.meta.role && role && to.meta.role !== role) {
    next(`/${role}/dashboard`)
    return
  }
  next()
})

export default router
