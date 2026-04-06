import axios from 'axios'

const http = axios.create({
  baseURL: 'http://127.0.0.1:8080/api',
  timeout: 10000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('coop-finance-token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response.data,
  (error) => Promise.reject(error?.response?.data?.message || error.message || '请求失败')
)

export default http
