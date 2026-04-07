<template>
  <div class="module-page notif-layout">
    <!-- Main Content Simulation / Left Side -->
    <div class="notif-hero glass-card">
      <div class="hero-content">
        <h1>全局通讯塔 / Message Hub</h1>
        <p>安全保姆通讯频道，可查阅所有系统重要通知与业务回执。</p>
        <div class="hero-stats">
          <div class="stat-box">
             <div class="stat-num">{{ notifications.length }}</div>
             <div class="stat-label">待阅密电</div>
          </div>
          <div class="stat-box">
             <div class="stat-num text-success">0</div>
             <div class="stat-label">拦截风险</div>
          </div>
        </div>
        
        <el-input v-model="searchQuery" class="neo-input mt-4" size="large" prefix-icon="Search" placeholder="在密电库中进行穿透式检索" clearable />
      </div>
      <!-- Background decoration -->
      <el-icon class="hero-bg-icon"><BellFilled /></el-icon>
    </div>

    <!-- Right Side Drawer-Style Panel -->
    <div class="notif-drawer">
      <div class="drawer-header">
        <div class="drawer-title">
          <el-badge :value="notifications.length" :hidden="notifications.length===0" class="notif-badge">
             收件箱 (Inbox)
          </el-badge>
        </div>
        <div class="drawer-actions">
           <el-button type="primary" plain circle icon="Refresh" @click="loadData" title="强制同步云端" />
           <el-tooltip content="熔断信箱，一键燃毁" placement="top">
             <el-button type="danger" circle icon="Delete" @click="clearAll" />
           </el-tooltip>
        </div>
      </div>

      <div class="drawer-body">
         <el-empty v-if="filteredNotifs.length === 0" description="密电舱空载" :image-size="120" />
         
         <transition-group name="slide-fade" tag="div" class="timeline-container" v-else>
           <div class="notif-item glass-card" v-for="msg in filteredNotifs" :key="msg.id">
             <!-- Icon Area -->
             <div class="notif-icon-box" :class="'icon-' + msg.type">
               <el-icon v-if="msg.type === 'danger'"><WarningFilled /></el-icon>
               <el-icon v-else-if="msg.type === 'success'"><FolderChecked /></el-icon>
               <el-icon v-else><ChatDotRound /></el-icon>
             </div>
             
             <!-- Content Area -->
             <div class="notif-content">
               <div class="notif-top">
                 <h4 class="notif-subject">{{ msg.title }}</h4>
                 <span class="notif-time">{{ msg.time }}</span>
               </div>
               <p class="notif-desc">{{ msg.content }}</p>
               <div class="notif-actions" v-if="msg.type === 'danger'">
                  <el-button size="small" type="danger" plain>立刻进行核查</el-button>
               </div>
             </div>
           </div>
         </transition-group>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { WarningFilled, FolderChecked, ChatDotRound, Search, Refresh, Delete, BellFilled } from '@element-plus/icons-vue'
import http from '../api'

// Simple mock id generator for transitions
let idCounter = 0
const generateId = () => `msg-${idCounter++}`

const notifications = ref([
  { id: generateId(), title: "防诈骗温馨提醒", content: "请注意保护您的密码，切勿泄露给任何陌生人哪怕对方自称办案人员。如遇可疑大额冻结，请第一时间来物理网点提交质询单。", time: "Just now", type: 'danger' },
  { id: generateId(), title: "存款定期续期播报", content: "您存入的一笔春耕发展专属定存已录入住建系统。请留存相关证据文件。", time: "2 hours ago", type: 'success' }
])

const searchQuery = ref('')

const filteredNotifs = computed(() => {
  if (!searchQuery.value) return notifications.value
  const q = searchQuery.value.toLowerCase()
  return notifications.value.filter(n => 
    n.title.toLowerCase().includes(q) || n.content.toLowerCase().includes(q)
  )
})

async function loadData() {
  try {
    const res = await http.get(`/user/module/notifications`)
    if(res.data.records && res.data.records.length > 0) {
       res.data.records.forEach(r => {
           notifications.value.unshift({ 
             id: generateId(), 
             title: r.notice_title, 
             content: '收到全体下发通知。前往主站公告区查看！', 
             time: r.created_at, 
             type: 'info' 
           })
       })
    }
    ElMessage.success("云端密电同步完毕")
  } catch (e) {
    ElMessage.error(String(e))
  }
}

function clearAll() {
  if (notifications.value.length === 0) return ElMessage.info("毫无痕迹")
  notifications.value = []
  ElMessage.success("全部收件箱已被物理级燃毁。")
}

onMounted(loadData)
</script>

<style scoped>
.notif-layout { display: grid; grid-template-columns: 1fr 450px; gap: 24px; min-height: calc(100vh - 120px); align-items: stretch; }

/* Left Hero Box */
.notif-hero { position: relative; padding: 40px; border-radius: 24px; overflow: hidden; background: linear-gradient(135deg, var(--color-primary-variant) 0%, var(--color-primary) 100%); color: #fff; border:none !important; box-shadow: var(--shadow-lg) !important; }
.hero-content { position: relative; z-index: 2; max-width: 80%; }
.hero-content h1 { font-size: 36px; margin: 0 0 15px; font-weight: 800; }
.hero-content p { font-size: 16px; line-height: 1.6; color: rgba(255,255,255,0.8); margin-bottom: 40px; }

.hero-stats { display: flex; gap: 30px; margin-bottom: 40px; }
.stat-box { background: rgba(255,255,255,0.1); padding: 15px 25px; border-radius: 16px; backdrop-filter: blur(10px); }
.stat-num { font-size: 32px; font-weight: 800; font-family: 'Inter', sans-serif; }
.stat-label { font-size: 13px; color: rgba(255,255,255,0.7); margin-top: 5px; }
.text-success { color: #00e0ba; }

.hero-bg-icon { position: absolute; right: -50px; bottom: -50px; font-size: 350px; color: rgba(255,255,255,0.05); z-index: 1; transform: rotate(-15deg); }

/* Right Drawer Panel */
.notif-drawer { background: var(--color-surface); border-radius: 24px; box-shadow: var(--shadow-sm); border: 1px solid var(--color-border); display: flex; flex-direction: column; overflow: hidden; }

.drawer-header { padding: 24px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--color-border); background: var(--color-surface-hover); }
.drawer-title { font-size: 18px; font-weight: 700; color: var(--color-text); }
.notif-badge :deep(.el-badge__content) { background-color: var(--color-danger); border: none; font-weight: bold; }

.drawer-body { flex: 1; overflow-y: auto; padding: 20px; background: var(--color-bg); }

.timeline-container { display: flex; flex-direction: column; gap: 16px; }

.notif-item { display: flex; gap: 16px; padding: 16px; border-radius: 16px; transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1); cursor: default; }
.notif-item:hover { transform: translateX(-5px); box-shadow: var(--shadow-md) !important; border-color: var(--color-primary-light) !important; }

.notif-icon-box { flex-shrink: 0; width: 44px; height: 44px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 20px; color: #fff; box-shadow: 0 4px 10px rgba(0,0,0,0.1); }
.icon-danger { background: linear-gradient(135deg, #ea4335, #ff7500); }
.icon-success { background: linear-gradient(135deg, #00c9a7, #34a853); }
.icon-info { background: linear-gradient(135deg, #0052cc, #00b2ff); }

.notif-content { flex: 1; min-width: 0; }
.notif-top { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 6px; }
.notif-subject { margin: 0; font-size: 15px; font-weight: 700; color: var(--color-text); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.notif-time { font-size: 12px; color: var(--color-text-secondary); white-space: nowrap; margin-left: 10px; }
.notif-desc { margin: 0; font-size: 13px; color: var(--color-text-secondary); line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
.notif-actions { margin-top: 10px; }

/* Transitions */
.slide-fade-enter-active { transition: all 0.4s ease-out; }
.slide-fade-leave-active { transition: all 0.3s cubic-bezier(1, 0.5, 0.8, 1); position: absolute; }
.slide-fade-enter-from, .slide-fade-leave-to { transform: translateX(30px); opacity: 0; }
.slide-fade-move { transition: transform 0.4s ease; }

@media (max-width: 1200px) {
  .notif-layout { grid-template-columns: 1fr; }
  .notif-hero { border-radius: 20px; padding: 30px; }
}
</style>
