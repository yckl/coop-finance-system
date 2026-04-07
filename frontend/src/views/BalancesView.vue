<template>
  <div class="module-page balances-page">
    <div class="module-topbar" style="margin-bottom: 20px;">
      <div>
        <div class="panel-title text-primary">系统储户底册与穿透式资金查账台</div>
        <div class="panel-subtitle">支持宏观全量调阅与针对单一账号的微观折线图透视。</div>
      </div>
      <div class="topbar-actions">
        <el-radio-group v-model="viewMode" size="large" class="neo-radios">
          <el-radio-button value="table"><el-icon><List /></el-icon> 宏观总账册</el-radio-button>
          <el-radio-button value="chart"><el-icon><TrendCharts /></el-icon> 微观波段透视</el-radio-button>
        </el-radio-group>
        <el-button type="primary" class="neo-btn-primary ms-3" @click="loadData">强刷底座</el-button>
      </div>
    </div>
    
    <!-- View Mode: Table (Global) -->
    <transition name="slide-fade" mode="out-in">
      <div v-if="viewMode === 'table'" class="view-table" key="table">
        <el-card class="glass-card panel-card no-padding" shadow="never">
          <!-- Search Header -->
          <div class="search-bar p-3" style="border-bottom: 1px dashed var(--color-border); display:flex; gap:15px; align-items:center;">
             <el-input v-model="searchKey" placeholder="输入 ID / 卡号 极速碰撞..." class="neo-input" style="width:250px;" prefix-icon="Search" />
             <el-tag effect="plain" type="info">共挂载 {{ filteredRecords.length }} 个金融实体</el-tag>
          </div>
          
          <el-table :data="filteredRecords" style="width: 100%" size="large" class="custom-table neo-input">
            <el-table-column prop="user_no" label="金融户口" width="120">
               <template #default="scope"><strong>{{ scope.row.user_no }}</strong></template>
            </el-table-column>
            <el-table-column prop="account_no" label="存折/卡槽介质" min-width="180">
               <template #default="scope"><span class="card-hash">{{ scope.row.account_no }}</span></template>
            </el-table-column>
            <el-table-column prop="total_balance" label="在网底仓权益 (总账 ¥)" min-width="160" align="right">
               <template #default="scope"><strong class="amt-total">{{ Number(scope.row.total_balance).toLocaleString('zh-CN', {minimumFractionDigits:2}) }}</strong></template>
            </el-table-column>
            <el-table-column prop="available_balance" label="自由浮水 (¥)" min-width="160" align="right">
               <template #default="scope"><strong class="amt-avail">{{ Number(scope.row.available_balance).toLocaleString('zh-CN', {minimumFractionDigits:2}) }}</strong></template>
            </el-table-column>
            <el-table-column prop="frozen_balance" label="司法冻结 (¥)" min-width="140" align="right">
               <template #default="scope"><span class="amt-frozen">{{ Number(scope.row.frozen_balance).toLocaleString('zh-CN', {minimumFractionDigits:2}) }}</span></template>
            </el-table-column>
            <el-table-column prop="account_status" label="账号状态" width="120" align="center">
               <template #default="scope">
                 <el-tag :type="scope.row.account_status === '正常' ? 'success' : 'danger'" effect="dark" round>{{ scope.row.account_status }}</el-tag>
               </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center" fixed="right">
               <template #default="scope">
                  <el-button link type="primary" @click="investigate(scope.row)">深潜查看</el-button>
               </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>

      <!-- View Mode: Chart (Individual Deep Dive) -->
      <div v-else class="view-chart" key="chart">
         <div class="search-investigator mb-4 text-center">
            <h3 class="mb-3">开启微观对象扫描探针</h3>
            <el-input v-model="investigateId" placeholder="键入需要深潜的 User No" size="large" class="neo-input" style="max-width:400px;">
               <template #append>
                 <el-button color="#0052cc" icon="Aim" @click="doInvestigate">锁定捕获</el-button>
               </template>
            </el-input>
         </div>

         <div v-if="activeUser" class="investigate-grid">
            <!-- Big Card Output -->
            <div class="ig-left">
               <el-card class="glass-card big-balance-card" shadow="never">
                  <div class="bbc-bg-icon"><el-icon><Wallet /></el-icon></div>
                  <div class="bbc-header">
                     <el-avatar :size="50" style="background:var(--color-primary);">{{ activeUser.user_no.charAt(0) }}</el-avatar>
                     <div class="bbc-meta">
                        <h3>{{ activeUser.user_no }}</h3>
                        <span>{{ activeUser.account_no }}</span>
                     </div>
                  </div>
                  <div class="bbc-body mt-4">
                     <p>核心储备金盈余</p>
                     <div class="bbc-amount">¥ {{ Number(activeUser.available_balance).toLocaleString('zh-CN', {minimumFractionDigits:2}) }}</div>
                     <div class="bbc-tags mt-3">
                        <el-tag type="success" effect="dark" round>状态: {{ activeUser.account_status }}</el-tag>
                        <el-tag type="info" effect="plain" round>黑名单侦测: 白</el-tag>
                     </div>
                  </div>
               </el-card>

               <el-card class="glass-card mt-4" shadow="never">
                  <h4 class="m-0 mb-3" style="font-weight:700;"><el-icon><CopyDocument /></el-icon> 最近 5 笔热态流水</h4>
                  <div class="mini-tx-list">
                     <div class="mtx-item" v-for="i in 5" :key="i">
                        <div class="mtx-info">
                           <strong>{{ i%2===0 ? '现金提款' : '网银转入' }}</strong>
                           <span>2026-04-0{{ i }} 14:20</span>
                        </div>
                        <div class="mtx-amt" :class="i%2===0 ? 'text-danger' : 'text-success'">
                           {{ i%2===0 ? '-' : '+' }} {{ (Math.random() * 5000 + 100).toFixed(2) }}
                        </div>
                     </div>
                  </div>
               </el-card>
            </div>

            <!-- Line Chart -->
            <div class="ig-right">
               <el-card class="glass-card full-h" shadow="never">
                  <div style="display:flex; justify-content:space-between; align-items:center;">
                     <h3 class="m-0" style="font-weight:800;"><el-icon><TrendCharts /></el-icon> 账户 30 日水位波动溯源</h3>
                     <el-tag type="warning" effect="plain">高频抛补预警模式</el-tag>
                  </div>
                  <div ref="chartRef" class="chart-container" style="height: 480px; width: 100%; margin-top:20px;"></div>
               </el-card>
            </div>
         </div>
         <el-empty v-else description="目前尚未锁定任何对象，探针休眠中" />
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { List, TrendCharts, Search, Aim, Wallet, CopyDocument } from '@element-plus/icons-vue'
import http from '../api'

const route = useRoute()
const role = route.meta.role || 'finance'
const records = ref([])
const searchKey = ref('')

const viewMode = ref('table')
const investigateId = ref('U1001')
const activeUser = ref(null)

const chartRef = ref(null)
let balanceChart = null

const filteredRecords = computed(() => {
  if (!searchKey.value) return records.value
  const k = searchKey.value.toLowerCase()
  return records.value.filter(r => r.user_no.toLowerCase().includes(k) || r.account_no.includes(k))
})

async function loadData() {
  try {
    const res = await http.get(`/${role}/module/balances`)
    records.value = res.data.records || []
  } catch (e) {
    ElMessage.error('底册数据链接污染: ' + e)
  }
}

function investigate(row) {
  investigateId.value = row.user_no
  viewMode.value = 'chart'
  doInvestigate()
}

function doInvestigate() {
  const user = records.value.find(r => r.user_no === investigateId.value)
  if (!user) return ElMessage.warning(`未能在本域内锁定到标识为 [${investigateId.value}] 的实体向量`)
  
  activeUser.value = user
  ElMessage.success('已剥离并提取该实体资产画像')
  nextTick(renderChart)
}

function renderChart() {
  if (!chartRef.value) return
  if (balanceChart) balanceChart.dispose()
  balanceChart = echarts.init(chartRef.value)
  
  // Generate fake 30 days data
  const data = []
  let base = Number(activeUser.value.available_balance) || 10000
  for(let i=30; i>=0; i--) {
     data.unshift(base)
     base += (Math.random() - 0.4) * 2000 // erratic changes
  }

  const isDark = document.documentElement.getAttribute('data-theme') === 'dark'
  
  balanceChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', backgroundColor: isDark ? 'rgba(20,26,40,0.9)' : 'rgba(255,255,255,0.9)', textStyle: { color: isDark ? '#fff' : '#333' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: Array.from({length:31}, (_,i) => `03-${(i+1).toString().padStart(2,'0')}`) },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: isDark ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.05)' } } },
    series: [
      {
        name: '物理水位', type: 'line', smooth: true, symbol: 'none',
        sampling: 'average', itemStyle: { color: '#0052cc' },
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{offset:0,color:'rgba(0,82,204,0.5)'}, {offset:1,color:'rgba(0,82,204,0)'}]) },
        data: data
      }
    ]
  })
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', () => { if(balanceChart) balanceChart.resize() })
})
</script>

<style scoped>
.balances-page { display: flex; flex-direction: column; min-height: calc(100vh - 120px); }

/* Slide Fade */
.slide-fade-enter-active, .slide-fade-leave-active { transition: all 0.3s ease-out; }
.slide-fade-enter-from { opacity: 0; transform: translateY(20px); }
.slide-fade-leave-to { opacity: 0; transform: translateY(-20px); position: absolute; width:100%;}

/* Table View */
.card-hash { font-family: monospace; background: var(--color-surface-hover); padding: 4px 8px; border-radius: 4px; font-weight: 600; color: var(--color-text-secondary); }
.amt-total { color: var(--color-primary); font-size: 16px; font-family: 'Inter', sans-serif;}
.amt-avail { color: var(--color-success); font-size: 16px; font-family: 'Inter', sans-serif;}
.amt-frozen { color: var(--color-danger); font-family: 'Inter', sans-serif; font-weight: 600; }

/* Chart View / Investigate */
.investigate-grid { display: grid; grid-template-columns: 400px 1fr; gap: 24px; margin-top: 20px;}
.full-h { height: 100%; display: flex; flex-direction: column; }
.full-h :deep(.el-card__body) { flex: 1; }

.big-balance-card { background: linear-gradient(135deg, #0a1128, #1a295c); color: white; border: none; position: relative; overflow: hidden; padding: 10px;}
.bbc-bg-icon { position: absolute; right: -20px; bottom: -20px; font-size: 180px; color: rgba(255,255,255,0.03); transform: rotate(-20deg); pointer-events: none;}
.bbc-header { display: flex; align-items: center; gap: 15px; position: relative; z-index: 2;}
.bbc-meta h3 { margin: 0; font-size: 20px; font-weight: 800; }
.bbc-meta span { font-family: monospace; color: rgba(255,255,255,0.6); }
.bbc-body p { margin: 0 0 5px; color: rgba(255,255,255,0.6); font-size: 13px; }
.bbc-amount { font-family: 'Inter', sans-serif; font-size: 40px; font-weight: 900; letter-spacing: 1px; text-shadow: 0 4px 10px rgba(0,0,0,0.3); }

.mini-tx-list { display: flex; flex-direction: column; gap: 10px; }
.mtx-item { display: flex; justify-content: space-between; align-items: center; padding: 12px; background: var(--color-surface-hover); border-radius: 8px; transition: all 0.2s;}
.mtx-item:hover { transform: translateX(5px); background: var(--color-border); }
.mtx-info { display: flex; flex-direction: column; gap: 5px; }
.mtx-info strong { font-size: 14px; color: var(--color-text); }
.mtx-info span { font-size: 11px; color: var(--color-text-secondary); font-family: monospace; }
.mtx-amt { font-family: 'Inter', sans-serif; font-size: 16px; font-weight: 800; }
.text-danger { color: #ea4335; }
.text-success { color: #00c9a7; }

@media(max-width:1200px){
  .investigate-grid { grid-template-columns: 1fr; }
}
</style>
