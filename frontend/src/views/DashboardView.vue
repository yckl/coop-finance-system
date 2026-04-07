<template>
  <div class="dashboard-page" :class="{'is-fullscreen': isFullscreen}">
    
    <!-- ADMIN DASHBOARD -->
    <template v-if="role === 'admin'">
      <div class="dashboard-header print-hide">
        <div>
          <div class="panel-title">金融调度指挥舱 (Admin Dashboard)</div>
          <div class="panel-subtitle">上帝视角 / 实时吞吐 / 风险极图</div>
        </div>
        <div class="topbar-actions">
          <el-button color="#0052cc" :dark="true" icon="MagicStick" @click="generateAiSummary">AI 经营简报</el-button>
          <el-button type="success" plain icon="Refresh" :class="{'spin-anim': isRefreshing}" @click="refreshData">态势同步</el-button>
          <el-button plain icon="FullScreen" @click="toggleFullscreen">{{ isFullscreen ? '退出全屏' : '全屏指挥' }}</el-button>
        </div>
      </div>
      
      <!-- 8 KPI Cards with micro Sparklines -->
      <div class="metric-grid-8x mt-3">
        <el-card v-for="(item, i) in kpis" :key="i" class="glass-card spark-card" shadow="never">
          <div class="spark-top">
            <div class="spark-label">{{ item.label }}</div>
            <el-icon class="spark-icon" :style="{color: item.color}"><component :is="item.icon" /></el-icon>
          </div>
          <div class="spark-mid">
            <span class="spark-val">{{ item.value }}</span>
            <span class="spark-unit">{{ item.unit }}</span>
          </div>
          <div class="spark-bot">
            <div class="spark-trend" :class="item.trend > 0 ? 'trend-up' : 'trend-down'">
               <el-icon><CaretTop v-if="item.trend > 0" /><CaretBottom v-else/></el-icon>
               {{ Math.abs(item.trend) }}%
            </div>
            <div class="sparkline" :ref="(el) => sparkRefs[i] = el"></div>
          </div>
        </el-card>
      </div>

      <!-- 3 Column Layout -->
      <div class="dashboard-3col mt-4">
        <!-- Left: Trend Echarts -->
        <div class="col-left">
          <el-card class="glass-card panel-card full-h" shadow="never">
             <template #header><div class="panel-title">全量资金吞吐流变带 (多Y轴)</div></template>
             <div ref="lineRef" class="chart-box" style="height: 400px;"></div>
          </el-card>
        </div>
        
        <!-- Mid: Pie Echarts + Mock Map -->
        <div class="col-mid">
          <el-card class="glass-card panel-card mb-4" shadow="never" style="height: calc(50% - 10px);">
             <template #header><div class="panel-title">存贷业务成分结构</div></template>
             <div ref="pieRef" class="chart-box" style="height: 100%;"></div>
          </el-card>
          <el-card class="glass-card panel-card relative-overflow" shadow="never" style="height: calc(50% - 10px);">
             <div class="map-mock-bg"></div>
             <div class="map-overlay">
                <h3 class="map-title"><el-icon><Location /></el-icon> 活跃网点热力映射</h3>
                <div class="map-pin pin-1"><div class="ripple"></div></div>
                <div class="map-pin pin-2"><div class="ripple"></div></div>
                <div class="map-pin pin-3"><div class="ripple"></div></div>
             </div>
          </el-card>
        </div>
        
        <!-- Right: Realtime Tasks & Warnings -->
        <div class="col-right">
          <el-card class="glass-card panel-card" shadow="never" style="height: 100%;">
             <template #header><div class="panel-title">实时风控与待办闸门</div></template>
             
             <el-collapse v-model="activeNames" class="neo-collapse">
               <el-collapse-item name="1">
                 <template #title>
                   <span class="warning-title"><el-icon><WarnTriangleFilled /></el-icon> 熔断预警队列 (3)</span>
                 </template>
                 <div class="timeline-log danger-log">
                    <div class="log-time">10:45:12</div>
                    <div class="log-txt">分行-A 大额提现 250,000 元，触发二次审验。</div>
                 </div>
                 <div class="timeline-log danger-log">
                    <div class="log-time">09:12:05</div>
                    <div class="log-txt">密集尝试异常操作日志：U1002 密码哈希爆破。</div>
                 </div>
               </el-collapse-item>
               
               <el-collapse-item name="2">
                 <template #title>
                   <span class="todo-title"><el-icon><List /></el-icon> 高优审批事项 (1)</span>
                 </template>
                 <div class="timeline-log info-log">
                    <div class="log-time">等待中</div>
                    <div class="log-txt">新入职出纳员权限授信复盘。</div>
                 </div>
               </el-collapse-item>
             </el-collapse>
          </el-card>
        </div>
      </div>
    </template>

    <!-- FINANCE DASHBOARD -->
    <template v-else-if="role === 'finance'">
      <div class="dashboard-header print-hide">
        <div>
          <div class="panel-title">柜面财务工作台 (Finance Workspace)</div>
          <div class="panel-subtitle">大厅业务签到 / 敏捷业务卡片 / 实时交易看板</div>
        </div>
        <div class="topbar-actions">
          <el-button type="primary" class="neo-btn-primary" icon="Calendar" @click="fakeCheckIn">网点日始签到</el-button>
        </div>
      </div>

      <div class="finance-grid mt-4">
         <!-- Section 1: Quick Actions Chips -->
         <div class="fin-col-left">
            <el-card class="glass-card fin-card mb-4" shadow="never">
               <h3 class="fin-sec-title"><el-icon><Compass /></el-icon> 快捷业务中台</h3>
               <div class="quick-action-grid mt-3">
                 <div class="quick-btn" @click="$router.push('/finance/deposits')">
                    <div class="qb-icon" style="background:#00c9a7"><el-icon><Money /></el-icon></div>
                    <div class="qb-text">现钞存入</div>
                 </div>
                 <div class="quick-btn" @click="$router.push('/finance/withdrawals')">
                    <div class="qb-icon" style="background:#ea4335"><el-icon><Wallet /></el-icon></div>
                    <div class="qb-text">大额取款</div>
                 </div>
                 <div class="quick-btn" @click="$router.push('/finance/balances')">
                    <div class="qb-icon" style="background:#0052cc"><el-icon><Tickets /></el-icon></div>
                    <div class="qb-text">挂失查账</div>
                 </div>
                 <div class="quick-btn" @click="$router.push('/finance/reimbursements')">
                    <div class="qb-icon" style="background:#ff9500"><el-icon><Collection /></el-icon></div>
                    <div class="qb-text">凭据审核</div>
                 </div>
               </div>
            </el-card>

            <!-- Draggable KanBan Tasks Simulator -->
            <el-card class="glass-card fin-card" shadow="never">
               <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:15px;">
                  <h3 class="fin-sec-title m-0"><el-icon><Notebook /></el-icon> 我的待办日历 (Task Board)</h3>
                  <el-button link type="primary" icon="Plus">新增便签</el-button>
               </div>
               
               <div class="fin-kanban">
                  <!-- Todo Column -->
                  <div class="f-col">
                     <div class="f-col-title">未开始 (2)</div>
                     <div class="f-card pointer-row">联系支行长解款 <div class="f-tag bg-cyan">网点调拨</div></div>
                     <div class="f-card pointer-row">催收恒生企业贷 <div class="f-tag bg-red">紧急</div></div>
                  </div>
                  <!-- Doing Column -->
                  <div class="f-col">
                     <div class="f-col-title">进行中 (1)</div>
                     <div class="f-card pointer-row" style="border-left: 3px solid #ff9500">
                        盘点今日库箱现金
                        <div class="f-tag bg-orange">结账日</div>
                     </div>
                  </div>
                  <!-- Done Column -->
                  <div class="f-col">
                     <div class="f-col-title">已完成</div>
                     <div class="f-card finished-card">
                        早盘安全播报
                        <el-icon class="check-icon" color="#34a853"><Select /></el-icon>
                     </div>
                  </div>
               </div>
            </el-card>
         </div>

         <!-- Section 2: Realtime Ticker & Stats -->
         <div class="fin-col-right">
            <el-card class="glass-card fin-card full-h ticker-card" shadow="never">
               <h3 class="fin-sec-title"><el-icon><DataBoard /></el-icon> 全网大额交易实况 (Ticker)</h3>
               
               <div class="ticker-box mt-3" ref="tickerBox">
                  <div class="ticker-track" :class="{'running': isTickerRunning}">
                     <div class="t-item" v-for="t in mockTickers" :key="t.id">
                        <div class="t-time">{{ t.time }}</div>
                        <div class="t-content">
                           <span class="t-type" :class="t.type==='存入'?'text-success':'text-danger'">[{{ t.type }}]</span>
                           {{ t.branch }} - 客户{{ t.user }}
                        </div>
                        <div class="t-amt">¥ {{ t.amt }}</div>
                     </div>
                     <!-- Duplicate for seamless scroll -->
                     <div class="t-item" v-for="t in mockTickers" :key="t.id+'_dup'">
                        <div class="t-time">{{ t.time }}</div>
                        <div class="t-content">
                           <span class="t-type" :class="t.type==='存入'?'text-success':'text-danger'">[{{ t.type }}]</span>
                           {{ t.branch }} - 客户{{ t.user }}
                        </div>
                        <div class="t-amt">¥ {{ t.amt }}</div>
                     </div>
                  </div>
               </div>
               
               <div class="ticker-stats mt-4">
                 <div class="t-stat">
                    <div>当前库箱余额</div>
                    <strong class="text-primary" style="font-size:22px;">¥2,549,000.00</strong>
                 </div>
                 <div class="t-stat">
                    <div>全辖大额流出</div>
                    <strong class="text-danger" style="font-size:22px;">¥1,200,000.00</strong>
                 </div>
               </div>
            </el-card>
         </div>
      </div>
    </template>

    <!-- USER DASHBOARD -->
    <template v-else>
      <div class="dashboard-header print-hide">
        <div>
          <div class="panel-title">我的个人财富中心</div>
          <div class="panel-subtitle">随时随地，掌握您的每一分资金安全</div>
        </div>
      </div>

      <div class="user-dash-grid mt-4">
         <!-- Left Side: Big Balance Card & Quick Entry -->
         <div class="u-col-left">
            <div class="u-balance-card glass-card relative-overflow">
               <div class="u-bc-bg"></div>
               <div class="u-bc-header">
                  <span class="u-bc-title">总资产估值 (CNY)</span>
                  <div class="u-bc-actions">
                     <el-icon class="u-icon-btn" @click="balancesVisible = !balancesVisible"><View v-if="balancesVisible"/><Hide v-else/></el-icon>
                  </div>
               </div>
               <div class="u-bc-body">
                  <div class="u-bc-amount" v-if="balancesVisible">¥ {{ Number(85400.50).toLocaleString('zh-CN', {minimumFractionDigits: 2}) }}</div>
                  <div class="u-bc-amount u-hidden-stars" v-else>****</div>
               </div>
               <div class="u-bc-footer">
                  <div class="u-acc-no">
                     通证标段: <span style="font-family:monospace; margin-left:5px;">6228 **** **** 1001</span>
                     <el-tooltip content="复制到剪贴板">
                        <el-icon class="ml-2 hover-scale pointer" @click="copyAccount"><CopyDocument /></el-icon>
                     </el-tooltip>
                  </div>
               </div>
            </div>

            <!-- Quick Entry Grid 4x1 or 2x2 -->
            <div class="u-quick-grid mt-4">
               <div class="u-quick-item glass-card" @click="$router.push('/user/deposits')">
                  <div class="u-qi-icon" style="color: #00c9a7; background: rgba(0,201,167,0.1);"><el-icon><Money /></el-icon></div>
                  <div class="u-qi-text">入账流水</div>
               </div>
               <div class="u-quick-item glass-card" @click="$router.push('/user/withdrawals')">
                  <div class="u-qi-icon" style="color: #ea4335; background: rgba(234,67,53,0.1);"><el-icon><Wallet /></el-icon></div>
                  <div class="u-qi-text">支出账单</div>
               </div>
               <div class="u-quick-item glass-card" @click="$router.push('/user/reimbursements')">
                  <div class="u-qi-icon" style="color: #0052cc; background: rgba(0,82,204,0.1);"><el-icon><Tickets /></el-icon></div>
                  <div class="u-qi-text">我要报销</div>
               </div>
               <div class="u-quick-item glass-card" @click="$router.push('/user/messages')">
                  <div class="u-qi-icon" style="color: #ff9500; background: rgba(255,149,0,0.1);"><el-icon><ChatDotRound /></el-icon></div>
                  <div class="u-qi-text">服务留言</div>
               </div>
            </div>
         </div>

         <!-- Right Side: Recent Transactions Auto-scroll & News -->
         <div class="u-col-right">
            <el-card class="glass-card u-card mb-4" shadow="never">
               <div style="display:flex; justify-content:space-between; align-items:center;">
                  <h3 class="m-0" style="font-size:16px; font-weight:700;"><el-icon><Calendar /></el-icon> 近期热点流水</h3>
                  <el-button link type="primary" @click="$router.push('/user/balances')">查看全部</el-button>
               </div>
               
               <div class="u-scroll-tx mt-3">
                  <div class="utx-item" v-for="i in 4" :key="i">
                     <div class="utx-icon" :class="i%2===0 ? 'utx-down' : 'utx-up'">
                        <el-icon><Top v-if="i%2!==0"/><Bottom v-else/></el-icon>
                     </div>
                     <div class="utx-info">
                        <strong>{{ i%2===0 ? 'ATM 线下取现' : '大额银联汇入' }}</strong>
                        <span>2026-04-0{{ i }} 14:{{ 10+i }} • 高新支行</span>
                     </div>
                     <div class="utx-amt" :class="i%2===0 ? 'text-danger' : 'text-success'">
                        {{ i%2===0 ? '-' : '+' }} {{ (Math.random() * 2000 + 100).toFixed(2) }}
                     </div>
                  </div>
               </div>
            </el-card>

            <el-card class="glass-card u-card mini-notice" shadow="never">
               <h3 class="m-0 mb-2" style="font-size:14px; color:var(--color-text-secondary);"><el-icon><Bell /></el-icon> 最新网点公告</h3>
               <div class="u-notice-item">【重要】关于切回夏令时营业期间的通告</div>
               <div class="u-notice-item">防范电信网络诈骗，警惕非法集资套路！</div>
            </el-card>
         </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick, Refresh, FullScreen, CaretTop, CaretBottom, Wallet, Money, DataLine, TrendCharts, Odometer, CreditCard, PieChart, Location, WarnTriangleFilled, List, Compass, Tickets, Collection, Notebook, Select, DataBoard, Plus, Calendar, View, Hide, CopyDocument, ChatDotRound, Top, Bottom, Bell, User } from '@element-plus/icons-vue'

const route = useRoute()
const role = route.meta.role || 'admin'

const isFullscreen = ref(false)
const isRefreshing = ref(false)
const activeNames = ref(['1', '2'])
const sparkRefs = ref([])
const lineRef = ref(null)
const pieRef = ref(null)

let lineChart, pieChart
let sparkCharts = []

const kpis = ref([
  { label: '在储本金', value: '45.2', unit: '亿', trend: 5.2, color: '#0052cc', icon: 'Wallet', data: [10, 15, 20, 18, 30, 45] },
  { label: '本月放水', value: '2.1', unit: '亿', trend: -1.4, color: '#ea4335', icon: 'Money', data: [40, 30, 25, 28, 22, 21] },
  { label: '备付金水位', value: '18', unit: '%', trend: 2.1, color: '#00c9a7', icon: 'Odometer', data: [15, 15, 16, 17, 16, 18] },
  { label: '客管主体数', value: '1.2', unit: '万', trend: 12.5, color: '#ff9500', icon: 'User', data: [8, 9, 10, 10, 11, 12] }
])

const isTickerRunning = ref(true)
const mockTickers = [
  { id: 1, time: '10:45:12', type: '转出', branch: '高新支行', user: 'U8801', amt: '500,000.00' },
  { id: 2, time: '10:43:05', type: '存入', branch: '大学城营业部', user: 'U9912', amt: '1,200,000.00' },
  { id: 3, time: '10:39:44', type: '转出', branch: '中心主站', user: 'U1145', amt: '350,000.00' },
  { id: 4, time: '10:32:11', type: '存入', branch: '城北储蓄所', user: 'U2234', amt: '800,000.00' },
  { id: 5, time: '10:28:50', type: '转出', branch: '高新支行', user: 'U1001', amt: '250,000.00' }
]

// User specific
const balancesVisible = ref(true)

function copyAccount() {
  ElMessage.success('已将编号载入剪贴板。')
}

function fakeCheckIn() {
  ElMessage.success('库箱指纹核准通过，网点早盘已成功签到。开启今日业务流。')
}

function toggleFullscreen() {
  isFullscreen.value = !isFullscreen.value
  setTimeout(() => { if(role==='admin') renderCharts() }, 300)
}

function refreshData() {
  isRefreshing.value = true
  setTimeout(() => {
    isRefreshing.value = false
    ElMessage.success('指标矩阵热更完毕')
  }, 1000)
}

function generateAiSummary() {
  ElMessageBox.alert('🤖 AI 已准备就绪...', '智能简报')
}

// Admin Charts Rendering (Stripped down version of exact previous logic for brevity here)
function renderCharts() {
  if (role !== 'admin') return
  if (lineRef.value) {
    if (lineChart) lineChart.dispose()
    lineChart = echarts.init(lineRef.value)
    lineChart.setOption({
      backgroundColor: 'transparent',
      xAxis: { type: 'category', data: ['M','T','W','T','F','S','S'] },
      yAxis: { type: 'value' },
      series: [{ data: [120, 200, 150, 80, 70, 110, 130], type: 'line', smooth:true, areaStyle:{}, itemStyle:{color:'#0052cc'} }]
    })
  }
}

onMounted(() => {
  if(role === 'admin') {
     nextTick(renderCharts)
     window.addEventListener('resize', renderCharts)
  }
})

onBeforeUnmount(() => {
  if(role === 'admin') {
     window.removeEventListener('resize', renderCharts)
     if(lineChart) lineChart.dispose()
  }
})
</script>

<style scoped>
.dashboard-page { display: flex; flex-direction: column; gap: 20px; transition: all 0.3s; }
.is-fullscreen { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; z-index: 1000; background: var(--color-bg); padding: 20px; overflow-y: auto; }

.dashboard-header { display: flex; justify-content: space-between; align-items: center; }

@keyframes spin { 100% { transform: rotate(360deg); } }
.spin-anim :deep(.el-icon) { animation: spin 1s linear infinite; }

/* Admin Grid */
.metric-grid-8x { display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px; }
.spark-card { padding: 12px; display: flex; flex-direction: column; }
.spark-top { display: flex; justify-content: space-between; align-items: center; }
.spark-label { font-size: 13px; color: var(--color-text-secondary); font-weight: 600; }
.spark-icon { font-size: 18px; border-radius: 6px; padding: 4px; background: var(--color-surface-hover); }
.spark-mid { margin-top: 10px; display: flex; align-items: baseline; gap: 4px; }
.spark-val { font-size: 24px; font-weight: 800; font-family: 'Inter', sans-serif; color: var(--color-text); }
.spark-unit { font-size: 13px; color: var(--color-text-secondary); }
.spark-trend { font-size: 12px; font-weight: 700; display: flex; align-items: center; margin-top:8px;}
.trend-up { color: var(--color-secondary); }
.trend-down { color: var(--color-danger); }

.dashboard-3col { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; }
.full-h { height: 100%; display: flex; flex-direction: column; }
.full-h :deep(.el-card__body) { flex: 1; display:flex; flex-direction:column; }
.chart-box { flex: 1; width: 100%; }

/* Finance Grid */
.finance-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; min-height: 500px; align-items: start;}
.fin-sec-title { margin:0; font-size: 16px; font-weight: 700; display:flex; align-items:center; gap:8px;}

.quick-action-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px; }
.quick-btn { height: 100px; background: var(--color-surface-hover); border-radius: 16px; display: flex; flex-direction: column; justify-content: center; align-items: center; cursor: pointer; transition: all 0.3s cubic-bezier(0.2,0.8,0.2,1); border: 1px solid transparent; }
.quick-btn:hover { transform: translateY(-5px); background: var(--color-surface); box-shadow: var(--shadow-neo); border-color: rgba(0,82,204,0.2); }
.qb-icon { width: 44px; height: 44px; border-radius: 12px; color: #fff; font-size: 22px; display: flex; justify-content: center; align-items: center; margin-bottom: 10px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); }
.qb-text { font-size: 14px; font-weight: 700; color: var(--color-text); }

.fin-kanban { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin-top: 15px; }
.f-col { background: rgba(0,82,204,0.03); border-radius: 12px; padding: 12px; min-height: 300px;}
.f-col-title { font-size: 13px; font-weight: 700; color: var(--color-text-secondary); margin-bottom: 12px; padding-bottom: 8px; border-bottom: 1px dashed var(--color-border); }
.f-card { background: var(--color-surface); padding: 12px; border-radius: 8px; font-size: 14px; font-weight: 600; color: var(--color-text); box-shadow: var(--shadow-sm); margin-bottom: 10px; border: 1px solid var(--color-border); }
.pointer-row { cursor: grab; }
.pointer-row:active { cursor: grabbing; opacity: 0.8; transform: scale(0.98); }
.finished-card { opacity: 0.6; text-decoration: line-through; display:flex; justify-content:space-between;}
.f-tag { display: inline-block; padding: 2px 6px; border-radius: 4px; font-size: 11px; margin-top: 8px; color:#fff;}
.bg-cyan { background: #00c9a7; }
.bg-red { background: #ea4335; }
.bg-orange { background: #ff9500; }

.ticker-card { display: flex; flex-direction: column; }
.ticker-box { flex: 1; background: #0a1128; border-radius: 12px; overflow: hidden; position: relative; border: 2px solid #1a295c; padding: 10px 0; max-height: 400px; }
.ticker-track { display: flex; flex-direction: column; gap: 8px; }
.running { animation: scrollUp 15s linear infinite; }
.ticker-box:hover .running { animation-play-state: paused; }
@keyframes scrollUp { from { transform: translateY(0); } to { transform: translateY(-50%); } }

.t-item { display: flex; justify-content: space-between; align-items: center; padding: 8px 15px; font-family: monospace; font-size: 13px; border-bottom: 1px dashed rgba(255,255,255,0.1); }
.t-time { color: rgba(255,255,255,0.4); }
.t-content { color: #ccd6f6; flex: 1; padding: 0 10px;}
.t-type { font-weight: 700; margin-right: 5px; }
.text-success { color: #00c9a7; }
.text-danger { color: #ff4949; }
.text-primary { color: #0052cc; }
.t-amt { font-weight: 800; color: #64ffda; letter-spacing: 1px; }

.ticker-stats { background: var(--color-surface-hover); padding: 15px; border-radius: 12px; display:flex; justify-content:space-between; }
.t-stat div { font-size: 13px; color: var(--color-text-secondary); margin-bottom: 5px; font-weight:600;}

/* USER DASHBOARD */
.user-dash-grid { display: grid; grid-template-columns: 1.5fr 1fr; gap: 25px; }
.u-balance-card { padding: 30px; border-radius: 20px; overflow: hidden; position: relative; color: white; background: linear-gradient(135deg, #0052cc, #002966); box-shadow: 0 15px 35px rgba(0,82,204,0.3); }
.u-bc-bg { position: absolute; right: -50px; bottom: -50px; width: 250px; height: 250px; background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 60%); border-radius: 50%; pointer-events: none;}
.u-bc-header { display: flex; justify-content: space-between; align-items: center; position: relative; z-index: 2; opacity: 0.8;}
.u-bc-title { font-size: 14px; letter-spacing: 1px; font-weight: 600;}
.u-icon-btn { font-size: 20px; cursor: pointer; transition: transform 0.2s; }
.u-icon-btn:hover { transform: scale(1.1); color: #00c9a7; }
.u-bc-body { margin-top: 20px; position: relative; z-index: 2; }
.u-bc-amount { font-family: 'Inter', sans-serif; font-size: 46px; font-weight: 800; letter-spacing: 1px; }
.u-hidden-stars { font-size: 50px; transform: translateY(10px); }
.u-bc-footer { margin-top: 30px; position: relative; z-index: 2; padding-top: 15px; border-top: 1px dashed rgba(255,255,255,0.2); display: flex; justify-content: space-between;}
.u-acc-no { font-size: 14px; display: flex; align-items: center; }
.pointer { cursor: pointer; transition: color 0.2s; }
.pointer:hover { color: #00c9a7; }

.u-quick-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px; }
.u-quick-item { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 110px; border-radius: 16px; cursor: pointer; transition: all 0.3s; border: 1px solid var(--color-border); }
.u-quick-item:hover { transform: translateY(-5px); box-shadow: var(--shadow-sm); border-color: var(--color-primary-light); }
.u-qi-icon { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 22px; margin-bottom: 10px; }
.u-qi-text { font-size: 13px; font-weight: 700; color: var(--color-text); }

.u-scroll-tx { display: flex; flex-direction: column; gap: 12px; }
.utx-item { display: flex; align-items: center; justify-content: space-between; padding: 10px; background: var(--color-surface-hover); border-radius: 12px; }
.utx-icon { width: 36px; height: 36px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 18px; margin-right: 12px; color: white; }
.utx-up { background: #00c9a7; }
.utx-down { background: #ea4335; }
.utx-info { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.utx-info strong { font-size: 14px; color: var(--color-text); }
.utx-info span { font-size: 11px; color: var(--color-text-secondary); }
.utx-amt { font-family: 'Inter', sans-serif; font-size: 15px; font-weight: 800; }
.text-danger { color: #ea4335; }
.text-success { color: #00c9a7; }

.mini-notice .u-notice-item { padding: 10px 0; border-bottom: 1px dashed var(--color-border); font-size: 13px; color: var(--color-text); cursor: pointer; transition: color 0.2s; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.mini-notice .u-notice-item:hover { color: var(--color-primary); }
.mini-notice .u-notice-item:last-child { border-bottom: none; padding-bottom: 0; }

@media(max-width:1400px){
  .dashboard-3col { grid-template-columns: 1fr; }
  .finance-grid { grid-template-columns: 1fr; }
  .quick-action-grid { grid-template-columns: repeat(2, 1fr); }
  .user-dash-grid { grid-template-columns: 1fr; }
  .u-quick-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
