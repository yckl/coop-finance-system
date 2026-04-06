import { reactive } from 'vue'

export const systemName = '农业合作社综合财务业务管理系统（农村信用社综合财务业务管理系统）'

const initialUser = JSON.parse(localStorage.getItem('coop-finance-user') || 'null')
const initialToken = localStorage.getItem('coop-finance-token') || ''

export const appStore = reactive({
  token: initialToken,
  user: initialUser,
  searchVisible: false,
  searchLoading: false,
  searchKeyword: '',
  searchResults: []
})

export function setSession(payload) {
  appStore.token = payload.token
  appStore.user = payload.user
  localStorage.setItem('coop-finance-token', payload.token)
  localStorage.setItem('coop-finance-user', JSON.stringify(payload.user))
}

export function clearSession() {
  appStore.token = ''
  appStore.user = null
  localStorage.removeItem('coop-finance-token')
  localStorage.removeItem('coop-finance-user')
}
