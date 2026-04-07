<template>
  <div class="module-page admin-sec-layout">
    <div class="module-topbar print-hide" style="margin-bottom: 20px;">
      <div>
        <div class="panel-title">系统安全与审计中心</div>
        <div class="panel-subtitle">记录全局涉密指令流转与异常入侵，提供金融级风控与事后溯源能力。</div>
      </div>
      <div class="topbar-actions">
        <el-button type="danger" class="neo-btn" @click="fakeTool('全网紧急断网')"><el-icon><WarnTriangleFilled /></el-icon> 激活紧急熔断预案</el-button>
        <el-button plain @click="loadData">链路重新加载</el-button>
      </div>
    </div>
    
    <!-- Top Dashboard: Gauge & Key Metrics -->
    <div class="sec-dashboard">
       <!-- Risk Gauge Chart -->
       <el-card class="glass-card gauge-card" shadow="never">
          <h4 class="sec-title"><el-icon><HelpFilled /></el-icon> 实时渗透压力指数</h4>
          <div ref="gaugeRef" style="width:100%; height: 200px; margin-top: -10px;"></div>
       </el-card>

       <!-- Stats Cards -->
       <el-card class="glass-card stat-card" shadow="never">
          <div class="stat-icon bg-red"><el-icon><Odometer /></el-icon></div>
          <div class="stat-info">
             <span>拦截极高危入侵</span>
             <strong>{{ highRiskWarningCount }} <small>次</small></strong>
          </div>
       </el-card>

       <el-card class="glass-card stat-card" shadow="never">
          <div class="stat-icon bg-blue"><el-icon><Cpu /></el-icon></div>
          <div class="stat-info">
             <span>底层审计轨迹总量</span>
             <strong>{{ auditLogs.length }} <small>条记录</small></strong>
          </div>
       </el-card>

       <el-card class="glass-card stat-card" shadow="never">
          <div class="stat-icon bg-green"><el-icon><Lock /></el-icon></div>
          <div class="stat-info">
             <span>SSL / WAF 装甲</span>
             <strong class="text-success">坚如磐石 <small>全天候接管</small></strong>
          </div>
       </el-card>
    </div>

    <!-- Main Content: Logs & Warnings -->
    <div class="sec-content mt-4">
      <el-card class="glass-card panel-card no-padding" shadow="never">
        <el-tabs v-model="activeTab" class="neo-tabs line-tabs">
          <!-- Warnings Tab (Timeline Layout) -->
          <el-tab-pane label="🚨 业务风控预警流靶向监控" name="WARNINGS">
            <div class="timeline-container">
               <el-empty v-if="!riskWarnings.length" description="目前没有任何风控报警生成" />
               <el-timeline v-else>
                  <el-timeline-item 
                     v-for="warn in riskWarnings" 
                     :key="warn.id"
                     :type="warn.warning_level === '高' ? 'danger' : 'warning'"
                     :timestamp="warn.created_at"
                     placement="top"
                     class="sec-timeline-item"
                     :hollow="warn.process_status === '已处理'"
                  >
                     <div class="warn-card glass-card neo-input">
                        <div class="warn-header">
                           <span class="warn-code">{{ warn.warning_code }}</span>
                           <el-tag :type="warn.warning_level === '高' ? 'danger' : 'warning'" effect="dark" size="small">{{ warn.warning_level }}危</el-tag>
                           <el-tag :type="warn.process_status === '已处理' ? 'success' : 'danger'" effect="plain" size="small" class="ml-2">{{ warn.process_status }}</el-tag>
                        </div>
                        <h4 class="warn-title mt-2">{{ warn.warning_type }} <span class="warn-obj">@ {{ warn.warning_object }}</span></h4>
                        <div class="warn-body mt-2">
                           <strong>情报解析：</strong> {{ warn.warning_content }}
                        </div>
                        <div class="warn-footer mt-3" v-if="warn.process_status !== '已处理'">
                           <el-button size="small" type="danger" @click="fakeTool('高危处置')">介入冻结涉事物</el-button>
                           <el-button size="small" type="info" plain @click="fakeTool('误报洗白')">标记为系统误报</el-button>
                        </div>
                     </div>
                  </el-timeline-item>
               </el-timeline>
            </div>
          </el-tab-pane>

          <!-- Audit Logs Tab (Table Layout) -->
          <el-tab-pane label="📜 底层加密审计流水账册" name="LOGS">
            <div class="logs-container p-3">
              <el-table :data="auditLogs" style="width: 100%" class="custom-table" border max-height="600">
                <el-table-column prop="risk_level" label="冲击等级" width="100" align="center">
                  <template #default="scope">
                    <span class="pulse-dot" :class="'pd-'+(scope.row.risk_level === '高' ? 'red' : (scope.row.risk_level === '中' ? 'orange' : 'blue'))"></span>
                    {{ scope.row.risk_level }}级
                  </template>
                </el-table-column>
                <el-table-column prop="log_type" label="事件定性" width="120" />
                <el-table-column prop="operator_name" label="操作元" width="120" />
                <el-table-column prop="module_name" label="突入象限" width="150" />
                <el-table-column prop="action_name" label="施加向量" width="130">
                  <template #default="scope"><span class="action-tag">{{ scope.row.action_name }}</span></template>
                </el-table-column>
                <el-table-column prop="action_content" label="逆向解析载荷 (Payload)" min-width="250" show-overflow-tooltip />
                <el-table-column prop="ip_address" label="发起端 IP" width="130">
                   <template #default="scope"><code class="ip-code">{{ scope.row.ip_address }}</code></template>
                </el-table-column>
                <el-table-column prop="created_at" label="时间戳" width="160" />
              </el-table>
            </div>
          </el-tab-pane>

          <!-- Tools Tab -->
          <el-tab-pane label="🔒 第三方安固件与应急网关" name="TOOLS">
            <div class="tools-container p-4">
              <el-alert title="请注意：您正处于系统最核心控制层。这里执行的命令会无视所有软拦截导致直接瘫痪系统服务。" type="error" show-icon :closable="false" class="mb-4" />
              
              <div class="tools-grid">
                 <el-card shadow="hover" class="tool-card border-danger">
                    <template #header><div class="tool-title text-danger"><el-icon><Unlock /></el-icon> 密码防线洗切</div></template>
                    <p class="tool-desc">强制全网所有在线人员下线，并在下次登录时要求验证身份及重置密码。</p>
                    <el-button type="danger" plain class="mt-3 w-100" @click="fakeTool('全局洗切鉴权')">执行指令流</el-button>
                 </el-card>

                 <el-card shadow="hover" class="tool-card border-warning">
                    <template #header><div class="tool-title text-warning"><el-icon><Connection /></el-icon> 骨干网络阻断</div></template>
                    <p class="tool-desc">直接在硬件级别下发 ACL 指令，彻底阻断海外可疑 IP 网段对于核心端口 (80/443) 的所有握手信号。</p>
                    <el-button type="warning" plain class="mt-3 w-100" @click="fakeTool('高危网段阻断')">执行注入</el-button>
                 </el-card>

                 <el-card shadow="hover" class="tool-card border-primary">
                    <template #header><div class="tool-title text-primary"><el-icon><DataLine /></el-icon> 容灾数据硬切</div></template>
                    <p class="tool-desc">立刻调用从可用区挂载的底层快照。将业务主线降级切换至冷备区。主要应对大规模的勒索接管。</p>
                    <el-button type="primary" plain class="mt-3 w-100" @click="fakeTool('启动冷备降级')">投递主令</el-button>
                 </el-card>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { WarnTriangleFilled, HelpFilled, Odometer, Cpu, Lock, Unlock, Connection, DataLine } from '@element-plus/icons-vue'
import http from '../api'

const activeTab = ref('WARNINGS')
const auditLogs = ref([])
const riskWarnings = ref([])
const gaugeRef = ref(null)

const highRiskWarningCount = computed(() => {
  return riskWarnings.value.filter(rw => rw.warning_level === '高').length
})

async function loadData() {
  try {
    const res = await http.get(`/admin/module/security`)
    auditLogs.value = res.data.auditLogs || []
    riskWarnings.value = res.data.riskWarnings || []
    nextTick(renderGauge)
  } catch (e) {
    ElMessage.error('安保系统底座加载失败: ' + e)
  }
}

function renderGauge() {
  if(!gaugeRef.value) return
  const chart = echarts.init(gaugeRef.value)
  
  // Calculate risk score based on warnings
  let riskScore = 15;
  if (highRiskWarningCount.value > 0) {
    riskScore = 80 + (highRiskWarningCount.value * 5);
  } else if (riskWarnings.value.length > 0) {
    riskScore = 40 + (riskWarnings.value.length * 2);
  }
  if(riskScore > 100) riskScore = 100

  const isDark = document.documentElement.getAttribute('data-theme') === 'dark'
  const textColor = isDark ? '#fff' : '#333'

  chart.setOption({
    series: [
      {
        type: 'gauge',
        startAngle: 180,
        endAngle: 0,
        min: 0,
        max: 100,
        splitNumber: 4,
        itemStyle: {
          color: riskScore > 70 ? '#ea4335' : (riskScore > 40 ? '#ff9500' : '#34a853'),
          shadowColor: 'rgba(0,138,255,0.45)',
          shadowBlur: 10,
          shadowOffsetX: 2,
          shadowOffsetY: 2
        },
        progress: { show: true, width: 18 },
        pointer: { length: '60%', width: 5, icon: 'triangle' },
        axisLine: { lineStyle: { width: 18 } },
        axisTick: { show: false },
        splitLine: { length: 22, lineStyle: { width: 2, color: 'auto' } },
        axisLabel: { distance: 25, color: '#999', fontSize: 12 },
        title: { show: false },
        detail: {
          valueAnimation: true, width: '60%', lineHeight: 20,
          borderRadius: 8, offsetCenter: [0, '20%'], fontSize: 24, fontWeight: 'bolder', 
          formatter: '{value}%', color: 'auto'
        },
        data: [{ value: riskScore, name: 'Risk' }]
      }
    ]
  })
}

function fakeTool(act) {
  ElMessageBox.prompt(`由于您试图挂载最高灾备指令【${act}】，请出示双因子加密核销密匙 (随便填即可)：`, '特级风控防线', {
    confirmButtonText: '通过验证执行',
    cancelButtonText: '放弃操作',
    inputPattern: /.+/,
    inputErrorMessage: '请输入授权凭证',
    type: 'error'
  }).then(({ value }) => {
    ElMessage.success(`安全验证完成，【${act}】指令已执行！`)
  }).catch(() => {})
}

onMounted(loadData)
</script>

<style scoped>
.admin-sec-layout { display: flex; flex-direction: column; min-height: calc(100vh - 120px); }

.sec-dashboard { display: grid; grid-template-columns: 350px 1fr 1fr 1fr; gap: 20px; }
.sec-title { margin: 0 0 10px; font-size: 15px; font-weight: 700; color: var(--color-text); display:flex; align-items:center; gap:6px; }

.stat-card { padding: 20px; display: flex; align-items: center; gap: 20px; }
.stat-icon { width: 60px; height: 60px; border-radius: 16px; display: flex; justify-content: center; align-items: center; font-size: 28px; color: #fff; flex-shrink: 0; box-shadow: 0 8px 16px rgba(0,0,0,0.1); }
.bg-red { background: linear-gradient(135deg, #ea4335, #c0392b); }
.bg-blue { background: linear-gradient(135deg, #00b2ff, #0052cc); }
.bg-green { background: linear-gradient(135deg, #34a853, #27ae60); }
.stat-info { display: flex; flex-direction: column; }
.stat-info span { font-size: 13px; color: var(--color-text-secondary); margin-bottom: 5px; }
.stat-info strong { font-size: 24px; font-family: 'Inter', sans-serif; font-weight: 800; color: var(--color-text); display:flex; align-items:baseline; gap:5px; }
.stat-info small { font-size: 12px; font-weight: 600; color: var(--color-text-secondary); }

.line-tabs :deep(.el-tabs__item) { font-size: 16px; font-weight: 600; height: 50px; line-height: 50px; }

/* Timeline UI */
.timeline-container { padding: 30px; }
.sec-timeline-item :deep(.el-timeline-item__timestamp) { font-family: monospace; font-size: 13px; font-weight: 600; color: var(--color-text-secondary); margin-bottom: 10px;}
.warn-card { padding: 15px 20px; border-radius: 12px; }
.warn-header { display: flex; align-items: center; }
.warn-code { font-family: monospace; font-size: 13px; font-weight: 700; color: var(--color-text); margin-right: 15px; }
.warn-title { margin: 0; font-size: 16px; font-weight: 700; color: var(--color-text); display:flex; align-items:center; gap:8px;}
.warn-obj { font-size: 14px; color: var(--color-primary); font-weight: 600; }
.warn-body { font-size: 14px; color: var(--color-text-secondary); line-height: 1.6; }

/* Table UI */
.custom-table { border-radius: 0 0 12px 12px; border:none; --el-table-header-bg-color: var(--color-surface-hover); }
.pulse-dot { display: inline-block; width: 6px; height: 6px; border-radius: 50%; margin-right: 6px; }
.pd-red { background: #ea4335; box-shadow: 0 0 6px #ea4335; }
.pd-orange { background: #ff9500; }
.pd-blue { background: #00b2ff; }
.action-tag { border-bottom: 2px solid var(--color-primary); font-weight: 700; padding-bottom: 2px; }
.ip-code { background: var(--color-surface-hover); padding: 4px 8px; border-radius: 4px; font-family: monospace; color: var(--color-text); }

/* Tools */
.tools-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.tool-card { border-radius: 12px; height: 100%; display: flex; flex-direction: column; background: var(--color-surface); }
.tool-title { font-size: 16px; font-weight: 800; display:flex; align-items:center; gap:8px;}
.tool-desc { flex: 1; font-size: 14px; color: var(--color-text-secondary); line-height: 1.6; }

@media (max-width: 1400px) {
  .sec-dashboard { grid-template-columns: 1fr 1fr; }
  .gauge-card { grid-column: span 2; }
  .tools-grid { grid-template-columns: 1fr; }
}
</style>
