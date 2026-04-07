<template>
  <div class="login-wrapper">
    <!-- Particle Canvas Background -->
    <canvas ref="particleCanvas" class="particle-canvas"></canvas>

    <div class="login-page">
      <!-- Hero Content -->
      <div class="login-hero glass-hero">
        <div class="brand-showcase">
          <div class="brand-logo-3d">COOP</div>
          <h2>农业合作社财务管理系统</h2>
          <p class="hero-desc">提供全方位的财务管理服务，保障资金安全，提升业务效率。</p>
        </div>
        
        <div class="hero-grid">
          <div class="hero-card glass-card hover-float">
            <h3>🛡️ 安全可靠</h3>
          </div>
          <div class="hero-card glass-card hover-float" style="animation-delay: 0.1s;">
            <h3>📊 高效结算</h3>
          </div>
          <div class="hero-card glass-card hover-float" style="animation-delay: 0.2s;">
            <h3>🤖 智能信贷</h3>
          </div>
        </div>
      </div>

      <!-- Action Panel -->
      <div class="login-panel">
        <div class="official-badge">
          <el-icon><Monitor /></el-icon> 中国农业信用合作社官方系统
        </div>
        
        <el-card class="login-card glass-card" shadow="never" :body-style="{padding: '0'}">
          <!-- Auth Tabs Header -->
          <div class="auth-tabs-header">
            <div class="auth-tab pointer" :class="{active: activeTab === 'login'}" @click="activeTab = 'login'">登录</div>
            <div class="auth-tab pointer" :class="{active: activeTab === 'register'}" @click="activeTab = 'register'">注册</div>
          </div>

          <div class="auth-box">
            <!-- LOGIN PANEL -->
            <transition name="fade-slide" mode="out-in">
              <div v-if="activeTab === 'login'" key="login">
                <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-position="top" size="large" class="neo-form" @validate="onValidate">
                  <el-form-item label="登录账号" prop="username" class="neo-input" aria-label="用户名输入">
                    <el-input 
                      v-model="loginForm.username" 
                      placeholder="如 Admin / U1001" 
                      prefix-icon="User" />
                  </el-form-item>
                  
                  <el-form-item label="安全密码" prop="password" class="neo-input" aria-label="密码输入框">
                    <el-input 
                      v-model="loginForm.password" 
                      type="password" 
                      show-password 
                      placeholder="请输入您的验证密码" 
                      prefix-icon="Key" 
                      @keyup.enter="login" />
                  </el-form-item>
                  
                  <div class="login-options">
                     <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
                     <el-link type="primary" :underline="false" style="font-weight:600" @click="showForgotPwd = true">忘记密码？</el-link>
                  </div>

                  <el-button 
                    type="primary" 
                    class="full-width login-btn" 
                    @click="login" 
                    :loading="isLoggingIn"
                    :disabled="!loginForm.username || !loginForm.password">
                    <template v-if="!isLoggingIn"><el-icon class="mr-2"><Position /></el-icon> 登录系统</template>
                    <template v-else>处理中...</template>
                  </el-button>
                </el-form>
              </div>

              <!-- REGISTER PANEL -->
              <div v-else key="register">
                <el-form ref="regFormRef" :model="regForm" :rules="regRules" label-position="top" size="large" class="neo-form">
                  <el-form-item label="用户名" prop="username">
                    <el-input v-model="regForm.username" placeholder="请输入拼音或字母组成的用户名" prefix-icon="User" />
                  </el-form-item>
                  
                  <el-form-item label="安全手机号" prop="phone">
                     <el-input v-model="regForm.phone" placeholder="请输入有效手机号" prefix-icon="Phone">
                       <template #append>
                          <el-button @click="sendCode" :disabled="countdown > 0" style="width: 110px">
                            {{ countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
                          </el-button>
                       </template>
                     </el-input>
                  </el-form-item>
                  
                  <el-row :gutter="16">
                    <el-col :span="12">
                      <el-form-item label="验证码" prop="code">
                        <el-input v-model="regForm.code" placeholder="6位数" maxlength="6" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="登录密码" prop="password">
                        <el-input v-model="regForm.password" type="password" show-password placeholder="8-20位强度密码" />
                      </el-form-item>
                    </el-col>
                  </el-row>

                  <el-form-item prop="agree">
                    <el-checkbox v-model="regForm.agree">同意《用户协议》和《隐私政策》</el-checkbox>
                  </el-form-item>

                  <el-button 
                    type="primary" 
                    class="full-width login-btn" 
                    @click="registerAccount" 
                    :loading="isRegistering">
                    <template v-if="!isRegistering"><el-icon class="mr-2"><Plus /></el-icon> 立即注册</template>
                    <template v-else>处理中...</template>
                  </el-button>
                </el-form>
              </div>
            </transition>
            
            <!-- DEMO LINKS (BOTTOM) -->
            <div class="demo-links-wrapper">
              <el-link type="info" :underline="false" @click="showDemoAccounts = !showDemoAccounts" class="demo-toggle-link">
                [演示账号一键登录] <el-icon class="ml-1"><component :is="showDemoAccounts ? 'ArrowUp' : 'ArrowDown'" /></el-icon>
              </el-link>
              
              <transition name="el-zoom-in-top">
                <div class="account-grid pt-3" v-if="showDemoAccounts">
                  <div class="account-item neo-btn" @click="fillLogin('admin', '123456')">
                    <span class="acc-icon unify-icon"><el-icon><Avatar /></el-icon></span>
                    <div class="acc-text"><strong>管理员</strong><span class="text-xs">最高权限控制台</span></div>
                  </div>
                  <div class="account-item neo-btn" @click="fillLogin('user', '123456')">
                    <span class="acc-icon unify-icon"><el-icon><User /></el-icon></span>
                    <div class="acc-text"><strong>普通用户</strong><span class="text-xs">查询与日常业务</span></div>
                  </div>
                </div>
              </transition>
              <div class="test-tip mt-2" v-if="showDemoAccounts"><el-tag type="info" effect="plain" size="small">仅供测试</el-tag></div>
            </div>
            
          </div>
        </el-card>

        <div class="legal-footer">
           © 2026 农业信用合作社
           <div class="meta-tags">
             v1.0.0 <span class="divider">|</span> <el-tag size="small" type="success" effect="dark" round>本地安全环境运行</el-tag>
           </div>
        </div>
      </div>
    </div>

    <!-- FORGOT PASSWORD MODAL -->
    <el-dialog v-model="showForgotPwd" title="找回安全密码" width="480px" center destroy-on-close class="rounded-dialog">
      <el-steps :active="pwdStep" finish-status="success" align-center class="mb-5">
        <el-step title="验证身份" />
        <el-step title="安全验证" />
        <el-step title="设置新密码" />
      </el-steps>

      <!-- Step 0 -->
      <div v-show="pwdStep === 0" class="px-3">
        <el-input v-model="pwdForm.account" placeholder="请输入您的用户名或手机号" size="large" prefix-icon="User" />
        <el-button type="primary" class="mt-4" style="width:100%; border-radius: 8px;" size="large" @click="handleVerifyAccount">下一步</el-button>
      </div>
      <!-- Step 1 -->
      <div v-show="pwdStep === 1" class="px-3">
         <div class="mb-2 text-primary fw-bold text-center">已向号码发送短信验证码</div>
         <div class="text-danger mb-4 text-center" style="font-size: 13px;">为保障资金安全，请勿向任何人泄露验证码！</div>
         <el-input v-model="pwdForm.code" placeholder="输入 6 位短信验证码" maxlength="6" size="large">
            <template #append>
              <el-button>重新获取邮件/短信</el-button>
            </template>
         </el-input>
         <div class="flex-row mt-4 gap-3">
           <el-button @click="pwdStep = 0" style="flex:1; border-radius: 8px;" size="large">上一步</el-button>
           <el-button type="primary" style="flex:1; border-radius: 8px;" size="large" @click="pwdStep = 2" :disabled="pwdForm.code.length < 6">下一步</el-button>
         </div>
      </div>
      <!-- Step 2 -->
      <div v-show="pwdStep === 2" class="px-3">
         <el-input v-model="pwdForm.newPwd" type="password" show-password placeholder="设置新的高强度密码" size="large" class="mb-4" prefix-icon="Lock" />
         <el-input v-model="pwdForm.newPwdConfirm" type="password" show-password placeholder="请确认新密码" size="large" class="mb-2" prefix-icon="Lock" />
         <div class="flex-row mt-4 gap-3">
           <el-button @click="pwdStep = 1" style="flex:1; border-radius: 8px;" size="large">上一步</el-button>
           <el-button type="primary" style="flex:1; border-radius: 8px;" size="large" @click="finishReset" :loading="isResetting">确认安全重置</el-button>
         </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User, Key, Position, Avatar, Monitor, Phone, Plus, ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import http from '../api'
import { setSession } from '../stores/app'

const router = useRouter()
const activeTab = ref('login')

// ---- LOGIN LOGIC ----
const rememberMe = ref(false)
const loginFormRef = ref(null)
const isLoggingIn = ref(false)

const loginForm = reactive({ username: 'admin', password: '123456' })
const loginRules = reactive({
  username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
  password: [{ required: true, message: '密码不能为空', trigger: 'blur' }]
})

function fillLogin(username, password) {
  activeTab.value = 'login'
  loginForm.username = username
  loginForm.password = password
}

function onValidate(prop, isValid, message) {}

async function login() {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      isLoggingIn.value = true
      try {
        const result = await http.post('/auth/login', loginForm)
        setSession(result.data)
        ElMessage.success('登录成功。')
        router.push(result.data.user.homePath)
      } catch (error) {
        ElMessage.error(String(error))
      } finally {
        isLoggingIn.value = false
      }
    } else {
      ElMessage.warning('请输入完整的登录信息。')
    }
  })
}

// ---- DEMO ACCOUNTS LOGIC ----
const showDemoAccounts = ref(false)

// ---- REGISTER LOGIC ----
const regFormRef = ref(null)
const isRegistering = ref(false)
const countdown = ref(0)
let timer = null

const regForm = reactive({ username: '', phone: '', code: '', password: '', agree: false })
const regRules = reactive({
  username: [{ required: true, message: '请输入由字母/拼音组成的用户名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入11位手机号', trigger: 'blur' }],
  code: [{ required: true, message: '请输入6位短信验证码', trigger: 'blur' }],
  password: [{ required: true, message: '密码不能为空', trigger: 'blur' }],
  agree: [{ validator: (rule, value, callback) => value ? callback() : callback(new Error('须同意相关协议方可注册')), trigger: 'change' }]
})

function sendCode() {
  if (!regForm.phone) {
    ElMessage.warning('请先输入相关手机号')
    return
  }
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
  ElMessage.success('短信验证码已下发，请注意查收')
}

async function registerAccount() {
  if (!regFormRef.value) return
  await regFormRef.value.validate((valid) => {
    if (valid) {
      isRegistering.value = true
      setTimeout(() => {
        isRegistering.value = false
        ElMessage.success('恭喜，账号注册成功！请使用新账密直接登录。')
        activeTab.value = 'login'
        loginForm.username = regForm.username
        loginForm.password = regForm.password
      }, 1000)
    }
  })
}

// ---- FORGOT PWD LOGIC ----
const showForgotPwd = ref(false)
const pwdStep = ref(0)
const isResetting = ref(false)
const pwdForm = reactive({ account: '', code: '', newPwd: '', newPwdConfirm: '' })

function handleVerifyAccount() {
  if (!pwdForm.account) {
    ElMessage.warning('请输入身份标识信息')
    return
  }
  pwdStep.value = 1
}

function finishReset() {
  if (!pwdForm.newPwd || pwdForm.newPwd !== pwdForm.newPwdConfirm) {
    ElMessage.error('两次密码输入不一致或密码为空！')
    return
  }
  isResetting.value = true
  setTimeout(() => {
    isResetting.value = false
    ElMessage.success('密码重置成功，请使用新密码登录。')
    showForgotPwd.value = false
    pwdStep.value = 0
    pwdForm.account = ''; pwdForm.code = ''; pwdForm.newPwd = ''; pwdForm.newPwdConfirm = '';
  }, 1200)
}

// ---- PARTICLE CANVAS ----
const particleCanvas = ref(null)
let animationFrameId
let particles = []

function initParticles() {
  const cn = particleCanvas.value
  if (!cn) return
  const ctx = cn.getContext('2d')
  
  const resize = () => { cn.width = window.innerWidth; cn.height = window.innerHeight; }
  window.addEventListener('resize', resize)
  resize()

  for (let i=0; i<60; i++) {
    particles.push({
      x: Math.random() * cn.width,
      y: Math.random() * cn.height,
      vx: (Math.random() - 0.5) * 0.5,
      vy: (Math.random() - 0.5) * 0.5,
      r: Math.random() * 2 + 1
    })
  }

  function render() {
    ctx.clearRect(0, 0, cn.width, cn.height)
    ctx.fillStyle = 'rgba(0, 160, 216, 0.05)'
    ctx.strokeStyle = 'rgba(0, 160, 216, 0.03)'
    
    particles.forEach(p => {
      p.x += p.vx; p.y += p.vy
      if (p.x < 0 || p.x > cn.width) p.vx *= -1
      if (p.y < 0 || p.y > cn.height) p.vy *= -1
      ctx.beginPath()
      ctx.arc(p.x, p.y, p.r, 0, Math.PI*2)
      ctx.fill()
    })
    
    for(let i=0; i<particles.length; i++) {
      for(let j=i+1; j<particles.length; j++) {
         const dx = particles[i].x - particles[j].x
         const dy = particles[i].y - particles[j].y
         const dist = dx*dx + dy*dy
         if (dist < 15000) {
           ctx.beginPath()
           ctx.moveTo(particles[i].x, particles[i].y)
           ctx.lineTo(particles[j].x, particles[j].y)
           ctx.stroke()
         }
      }
    }
    animationFrameId = requestAnimationFrame(render)
  }
  render()
}

onMounted(() => {
  initParticles()
})

onBeforeUnmount(() => {
  cancelAnimationFrame(animationFrameId)
})
</script>

<style scoped>
.login-wrapper { position: relative; min-height: 100vh; overflow: hidden; background: var(--color-bg); }
.particle-canvas { position: absolute; top:0; left:0; width: 100%; height: 100%; z-index: 0; }

.login-page { position: relative; z-index: 1; display: grid; grid-template-columns: 1.2fr 0.8fr; min-height: 100vh; gap: 40px; padding: 50px 8%; align-items: center; }

/* Left Hero Alignments */
.glass-hero { background: rgba(0, 160, 216, 0.05); padding: 50px; border-radius: 30px; border: 1px solid var(--color-border); box-shadow: var(--shadow-lg); backdrop-filter: blur(15px); text-align: left; }
.brand-showcase h2 { font-size: 46px; margin:0; font-weight: 800; background: linear-gradient(to right, var(--color-primary), var(--color-primary-light)); -webkit-background-clip: text; color: transparent; line-height: 1.3; }
.hero-desc { font-size: 18px; color: var(--color-text-secondary); line-height: 1.4; margin: 24px 0; max-width: 85%; }

.brand-logo-3d { display:inline-flex; align-items:center; justify-content:center; width:80px; height:80px; border-radius:24px; font-size:22px; font-weight:800; color:#fff; background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light)); box-shadow: 0 8px 24px rgba(0, 160, 216, 0.4); margin-bottom: 24px; transition: all 0.3s cubic-bezier(0.2, 0.8, 0.2, 1); cursor: default;}
.brand-logo-3d:hover { transform: scale(1.05) translateY(-5px); box-shadow: 0 12px 30px rgba(0, 160, 216, 0.6); }

/* Hero Features Uniform Gaps */
.hero-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-top: 56px; }
.hover-float { transition: transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275); animation: float 6s infinite ease-in-out; }
.hover-float:hover { transform: translateY(-8px) scale(1.02); }
.hero-card { padding: 16px; border-radius: 12px; }
.hero-card h3 { font-size: 16px; margin:0; text-align: center; color: var(--color-text); }

/* Action Panel Right Alignments */
.login-panel { display: flex; flex-direction: column; justify-content: center; max-width: 480px; width:100%; justify-self: center; text-align: left; }
.official-badge { text-align: center; color: var(--color-text-secondary); font-size: 12px; font-weight: 600; margin-bottom: 12px; letter-spacing: 0.5px; opacity: 0.9;}
.login-card { border-radius: 24px !important; overflow: hidden; border: 1px solid var(--color-border); }

/* Custom Auth Tabs */
.auth-tabs-header { display: flex; align-items: center; justify-content: center; background: var(--color-surface-hover); border-bottom: 1px solid var(--color-border); gap: 24px; }
.auth-tab { position: relative; height: 60px; line-height: 60px; font-size: 16px; font-weight: 600; color: var(--color-text-secondary); transition: all 0.3s; }
.auth-tab:hover { color: var(--color-primary); }
.auth-tab.active { font-weight: 800; color: var(--color-primary); }
.auth-tab.active::after { content: ''; position: absolute; bottom: -1px; left: 0; width: 100%; height: 3px; background: var(--color-primary); border-radius: 3px 3px 0 0; }

.auth-box { padding: 30px; }

/* Transitions */
.fade-slide-enter-active, .fade-slide-leave-active { transition: opacity 0.3s ease, transform 0.3s ease; }
.fade-slide-enter-from { opacity: 0; transform: translateX(-15px); }
.fade-slide-leave-to { opacity: 0; transform: translateX(15px); }

/* Form Elements */
:deep(.el-form-item) { margin-bottom: 16px !important; }
:deep(.el-form-item__label) { font-size: 14px; color: var(--color-text-secondary) !important; padding-bottom: 4px !important; }
.login-options { display:flex; justify-content: space-between; align-items: center; width: 100%; margin-bottom: 24px; font-size: 13px; }

.login-btn { width: 100%; display: flex; justify-content: center; align-items: center; height: 48px; font-size: 16px; border-radius: 8px; font-weight: 700; border:none; box-shadow: 0 8px 15px rgba(0,0,0,0.1); transition: all 0.3s; }
.login-btn.el-button--primary { background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light)); }
.login-btn:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 10px 20px rgba(0,0,0,0.2); }
.login-btn:disabled { background: #e4e7ed; box-shadow: none; color: #a8abb2; cursor: not-allowed; }

/* Demo Quick Accounts */
.demo-links-wrapper { margin-top: 24px; display: flex; flex-direction: column; align-items: center; }
.demo-toggle-link { font-size: 13px; }
.account-grid { display: grid; gap: 12px; width: 100%; }
.neo-btn { display: flex; align-items: center; gap: 15px; padding: 12px 16px; border-radius: 12px; background: var(--color-surface); box-shadow: var(--shadow-sm); cursor: pointer; transition: all 0.3s; border: 1px solid var(--color-border); }
.neo-btn:hover { border-color: var(--color-primary-light); background: rgba(0, 160, 216, 0.05); transform: translateY(-2px); box-shadow: 0 8px 16px rgba(0,0,0,0.06); }

.acc-icon { width:40px; height:40px; display:flex; align-items:center; justify-content:center; border-radius:12px; color:#fff; font-weight:bold; font-size:18px; flex-shrink: 0;}
.unify-icon { background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light)); }
.acc-text { display: flex; flex-direction: column; justify-content: center; }
.acc-text strong { display:block; font-size: 16px; color: var(--color-text); font-weight: 700;}
.acc-text .text-xs { display:block; font-size: 12px; color: var(--color-text-secondary); margin-top: 2px; }
.test-tip { font-size: 12px; color: var(--color-text-secondary); text-align: center; opacity: 0.8; }

/* Footer */
.legal-footer { text-align: center; margin-top: 16px; font-size: 12px; color: var(--color-text-secondary); line-height: 1.8; font-weight: 500;}
.meta-tags { margin-top: 4px; display: flex; align-items: center; justify-content: center; gap: 8px; }
.divider { color: var(--color-border); }
.pointer { cursor: pointer; }
.text-muted { color: var(--color-text-secondary); opacity: 0.8; }
.flex-row { display: flex; align-items: center; }
.gap-3 { gap: 12px; }
.px-3 { padding-left: 12px; padding-right: 12px; }
.rounded-dialog :deep(.el-dialog) { border-radius: 16px; }

@media (max-width: 992px) {
  .login-page { grid-template-columns: 1fr; padding: 30px 5%; margin-top: 40px; gap: 20px; }
  .login-panel { order: 1; }
  .login-hero { order: 0; padding: 30px; text-align: center; }
  .hero-card h3 { text-align: center; }
  .brand-showcase h2 { font-size: 28px; }
  .hero-desc { max-width: 100%; margin: 0 auto; font-size: 16px; }
  .hero-grid { grid-template-columns: 1fr; gap: 16px; margin-top: 30px; }
}
</style>
