<template>
  <div class="login-page">
    <div class="login-hero">
      <div class="hero-badge">三端角色化 · 毕设级全栈项目</div>
      <h1>农业合作社综合财务业务管理系统</h1>
      <p>
        覆盖管理员端、财务人员端、用户端，支持首页驾驶舱、存取款流程、统一流水、报销审核、公告留言、风险预警、全局搜索、导出打印与 AI 助手。
      </p>
      <div class="hero-grid">
        <div class="hero-card">
          <h3>管理员端</h3>
          <p>系统配置、权限控制、经营分析、审计与风险预警</p>
        </div>
        <div class="hero-card">
          <h3>财务人员端</h3>
          <p>存款办理、取款办理、报销审核、报表分析、AI 业务助手</p>
        </div>
        <div class="hero-card">
          <h3>用户端</h3>
          <p>余额查询、记录查询、报销申请、留言咨询、消息中心与 AI 问答</p>
        </div>
      </div>
    </div>

    <el-card class="login-card" shadow="hover">
      <template #header>
        <div class="login-header">
          <div>
            <div class="panel-title">系统登录</div>
            <div class="panel-subtitle">默认账号已内置，所有提示文字均为中文</div>
          </div>
        </div>
      </template>

      <el-form :model="form" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" @keyup.enter="login" />
        </el-form-item>
        <el-button type="primary" class="full-width" @click="login">登录系统</el-button>
        <el-button class="full-width top-gap" @click="registerVisible = true">用户注册</el-button>
      </el-form>

      <div class="account-block">
        <div class="account-title">默认账号</div>
        <div class="account-grid">
          <div class="account-item" @click="fill('admin', '123456')">
            <strong>管理员</strong>
            <span>admin / 123456</span>
          </div>
          <div class="account-item" @click="fill('caiwu', '123456')">
            <strong>财务人员</strong>
            <span>caiwu / 123456</span>
          </div>
          <div class="account-item" @click="fill('user', '123456')">
            <strong>普通用户</strong>
            <span>user / 123456</span>
          </div>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="registerVisible" title="普通用户注册" width="460px">
      <el-form :model="registerForm" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="registerForm.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="registerForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="registerForm.name" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="registerForm.phone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="registerVisible = false">取消</el-button>
        <el-button type="primary" @click="register">立即注册</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api'
import { setSession } from '../stores/app'

const router = useRouter()
const registerVisible = ref(false)

const form = reactive({
  username: 'admin',
  password: '123456'
})

const registerForm = reactive({
  username: '',
  password: '123456',
  name: '',
  phone: ''
})

function fill(username, password) {
  form.username = username
  form.password = password
}

async function login() {
  try {
    const result = await http.post('/auth/login', form)
    setSession(result.data)
    ElMessage.success('登录成功')
    router.push(result.data.user.homePath)
  } catch (error) {
    ElMessage.error(String(error))
  }
}

async function register() {
  try {
    const result = await http.post('/auth/register', registerForm)
    setSession(result.data)
    registerVisible.value = false
    ElMessage.success('注册成功，已自动登录')
    router.push('/user/dashboard')
  } catch (error) {
    ElMessage.error(String(error))
  }
}
</script>
