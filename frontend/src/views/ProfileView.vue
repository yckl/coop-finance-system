<template>
  <div class="module-page hover-page">
    <div class="module-topbar print-hide">
      <div>
        <div class="panel-title text-primary">系统底层信标与权限档案库</div>
        <div class="panel-subtitle">集中控制身份画像阵列、多基验证与 KPI 模型评测仪。</div>
      </div>
      <div class="topbar-actions">
        <el-button type="primary" class="neo-btn-primary" size="large" @click="saveProfile">同步区块档案网格</el-button>
      </div>
    </div>
    
    <el-row :gutter="24" style="margin-top: 20px;">
      <!-- Avatar & Radar Panel -->
      <el-col :xs="24" :lg="8">
        <el-card class="glass-card panel-card text-center relative-overflow full-h" shadow="never">
          <div class="card-bg-decoration"></div>
          
          <div class="avatar-ring-container p-4">
             <el-progress type="dashboard" :percentage="85" :color="myRole === 'admin' ? '#ea4335' : (myRole === 'finance' ? '#ff9500' : '#00c9a7')" :width="160">
               <template #default>
                  <div class="avatar-dropper" @click="fakeUpload">
                    <img src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" class="avatar-img" />
                    <div class="avatar-overlay"><el-icon><Camera /></el-icon></div>
                  </div>
               </template>
             </el-progress>
             <div class="profile-score-text mt-1">数据完备度 85%</div>
          </div>
          
          <div class="mt-3">
             <h2 class="profile-name">{{ myRole === 'admin' ? '总行管理员' : (myRole === 'finance' ? '柜面财务系统' : '终端自助储户') }}实体</h2>
             <div class="profile-role-badge mt-2" :class="'badge-' + myRole">
               {{ myRole === 'admin' ? 'GOD_MODE 权限极点' : (myRole === 'finance' ? 'BUSINESS_OWNER 信贷权' : 'USER_ROLE 读视图') }}
             </div>
          </div>
          
          <el-divider border-style="dashed" style="margin: 25px 0" />
          
          <!-- Echarts Radar KPI -->
          <div style="height: 240px; width: 100%;" ref="radarRef"></div>
          
          <div class="info-list mt-3">
            <div class="info-row">
               <span class="info-label"><el-icon><Location /></el-icon> 当前登录位置</span>
               <span class="info-val">内网 VPN - 机房核心柜</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- Multi-Tab Interaction -->
      <el-col :xs="24" :lg="16">
        <el-card class="glass-card panel-card full-h" shadow="never">
          <el-tabs v-model="activeTab" class="custom-tabs neo-tabs">
            
            <!-- Tab 1: Base Identity -->
            <el-tab-pane name="INFO">
               <template #label><span class="tab-label"><el-icon><User /></el-icon> 实体信标归档</span></template>
               <div class="tab-content pt-3">
                 <h3 class="tab-h3 mb-4">底层身份联络网格</h3>
                 <el-form label-position="top" class="neo-form" size="large" style="max-width: 600px;">
                    <el-row :gutter="20">
                      <el-col :span="12">
                        <el-form-item label="网联注册主姓名" class="neo-input">
                          <el-input value="系统演示设备" readonly></el-input>
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="系统强映射矩阵 ID" class="neo-input">
                          <el-input value="AUTH-U1011-" readonly></el-input>
                        </el-form-item>
                      </el-col>
                    </el-row>
                    <el-form-item label="安全验证网络 (加密信道)" class="neo-input">
                      <el-input value="138****0001"></el-input>
                    </el-form-item>
                    <el-form-item label="资金动帐流式抄送" class="neo-input">
                      <el-input value="cloud_notify@coop.net"></el-input>
                    </el-form-item>
                    <el-button type="primary" class="neo-btn-primary mt-4" size="large" @click="fakeSave">挂载协议栈执行</el-button>
                 </el-form>
               </div>
            </el-tab-pane>

            <!-- Tab 2: Security Engine -->
            <el-tab-pane name="SECURITY">
               <template #label><span class="tab-label"><el-icon><Lock /></el-icon> 密码安全中心</span></template>
               <div class="tab-content pt-3">
                  <div class="stepper-wrapper border-dash">
                    <el-steps :active="pwdStep" finish-status="success" align-center class="neo-steps" style="--el-color-primary:#ea4335">
                      <el-step title="原印鉴校验" icon="Key" />
                      <el-step title="高熵新轨生成" icon="Refresh" />
                      <el-step title="执行密码重置" icon="CircleCheck" />
                    </el-steps>
                  </div>
                  
                  <div class="step-card neo-input mt-5" style="max-width: 480px; margin-left:auto; margin-right:auto;">
                    <el-form label-position="top">
                      <div v-if="pwdStep === 0">
                        <el-form-item label="鉴权请求：请释放根节点的哈希口令">
                          <el-input v-model="oldPwd" type="password" show-password size="large" />
                        </el-form-item>
                        <el-button type="danger" class="full-width neo-btn mt-3" size="large" @click="pwdStep=1">请求校验合法性</el-button>
                      </div>
                      
                      <div v-if="pwdStep === 1">
                        <el-form-item label="注入高强度混合熵谱 (密码)">
                          <el-input v-model="newPwd" type="password" show-password size="large" @input="checkPwdStrength" />
                        </el-form-item>
                        <div class="pwd-strength-bar mt-3 mb-4">
                           <div style="font-size:12px; color:var(--color-text-secondary); margin-bottom:5px;">破译拦截强度极差: {{ pwdScoreText }}</div>
                           <div class="strength-track">
                             <div class="strength-fill" :class="'strength-'+pwdScore" :style="{ width: Math.max(5,(pwdScore*25))+'%' }"></div>
                           </div>
                        </div>
                        <el-row :gutter="15">
                           <el-col :span="12"><el-button size="large" class="neo-btn full-width" @click="pwdStep=0">切断重置</el-button></el-col>
                           <el-col :span="12"><el-button type="danger" class="neo-btn full-width" size="large" :disabled="pwdScore < 2" @click="pwdStep=2; fakeSave()">剥离覆盖</el-button></el-col>
                        </el-row>
                      </div>
                      
                      <div v-if="pwdStep === 2" class="text-center" style="padding: 30px 0;">
                         <el-icon :size="70" color="#ea4335"><CircleCheckFilled /></el-icon>
                         <h3 style="margin:20px 0 10px; color:#ea4335">新印鉴矩阵部署完备</h3>
                         <p style="color:var(--color-text-secondary);font-size:14px;line-height:1.6;">系统正在清理旧的密钥缓存，云端同步将受阻塞 5 分钟拦截访问。</p>
                         <el-button type="danger" plain class="neo-btn mt-4" @click="pwdStep=0; oldPwd=''; newPwd=''; pwdScore=0">关闭进程钩子</el-button>
                      </div>
                    </el-form>
                  </div>
               </div>
            </el-tab-pane>

            <!-- Tab 3: Device Monitor -->
            <el-tab-pane name="DEVICES">
               <template #label><span class="tab-label"><el-icon><Monitor /></el-icon> 活动设备防御阵(2)</span></template>
               <div class="tab-content pt-3">
                  <div class="device-list mt-3">
                    <div class="device-card active-device glass-card">
                       <div class="d-icon-box bg-blue"><el-icon><Monitor /></el-icon></div>
                       <div class="device-info">
                         <strong>本地算力核心站 - Node #1</strong>
                         <span class="d-sub"><el-icon><Position/></el-icon> 广域网 IP: 192.168.1.55 • 地理坐标: 本地机房 A区</span>
                       </div>
                       <el-tag type="success" size="large" effect="dark" round>在线 (此端)</el-tag>
                    </div>
                    
                    <div class="device-card glass-card">
                       <div class="d-icon-box bg-gray"><el-icon><Cellphone /></el-icon></div>
                       <div class="device-info">
                         <strong>外部网络终端 - Mobile #2</strong>
                         <span class="d-sub"><el-icon><Position/></el-icon> 蜂窝池 IP: 110.12.33.45 • 地理坐标: 卫星漫游地</span>
                       </div>
                       <el-button type="danger" plain @click="killDevice">阻断设备连接</el-button>
                    </div>
                  </div>
               </div>
            </el-tab-pane>
            
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { Camera, Location, Monitor, User, Lock, CircleCheckFilled, Cellphone, Key, Refresh, CircleCheck, Position } from '@element-plus/icons-vue'

const route = useRoute()
const myRole = route.meta.role || 'finance'
const activeTab = ref('INFO')

// Stepper states
const pwdStep = ref(0)
const oldPwd = ref('')
const newPwd = ref('')
const pwdScore = ref(0)

const radarRef = ref(null)

const pwdScoreText = computed(() => {
  if (pwdScore.value === 0) return '尚未探明扫描源'
  if (pwdScore.value === 1) return '极易被爆破解析'
  if (pwdScore.value === 2) return '符合金融防御基线'
  if (pwdScore.value === 3) return '异常坚如磐石'
  return '量子级反译干扰'
})

function checkPwdStrength() {
  const p = newPwd.value
  let score = 0
  if (p.length > 5) score += 1
  if (p.length > 8 && /[A-Z]/.test(p)) score += 1
  if (/[0-9]/.test(p) && /[^A-Za-z0-9]/.test(p)) score += 1
  if (p.length > 12) score += 1
  pwdScore.value = score
}

function fakeUpload() {
  ElMessage.warning('底层生物识别硬件流未接入总线，剥离失败。')
}

function fakeSave() {
  ElMessage.success('配置已保存至云端，等待系统确认。')
}

function saveProfile() {
  ElMessage.success('设备信息已全量同步到系统主控端！')
}

async function killDevice() {
  try {
    await ElMessageBox.confirm('您将永久性地剥夺该物理设备的 JWT 会话权，这可能导致其立刻黑屏，要彻底熔断吗？', '系统强制警告', { type: 'error' })
    ElMessage.success('目标通讯协议已擦除，正断开隧道连接。')
  } catch(e){}
}

function renderRadar() {
  if (!radarRef.value) return
  const chart = echarts.init(radarRef.value)
  
  const isDark = document.documentElement.getAttribute('data-theme') === 'dark'
  const textColor = isDark ? '#fff' : '#666'
  
  // Set KPI indicator based on role
  let indicators, data
  if(myRole === 'admin') {
     indicators = [{name:'风控巡查',max:100},{name:'系统布署',max:100},{name:'漏洞封号',max:100},{name:'AI调优',max:100},{name:'数据审计',max:100}]
     data = [95, 88, 92, 80, 98]
  } else if(myRole === 'finance') {
     indicators = [{name:'收单效率',max:100},{name:'坏账截停',max:100},{name:'流水结算',max:100},{name:'报销审批',max:100},{name:'日结盘点',max:100}]
     data = [90, 85, 96, 88, 92]
  } else {
     indicators = [{name:'账户活跃',max:100},{name:'信用积分',max:100},{name:'资产健康',max:100},{name:'负债水平',max:100},{name:'互动粘性',max:100}]
     data = [70, 90, 85, 40, 60] // low debt is good, but just raw numbers for chart
  }

  chart.setOption({
     backgroundColor: 'transparent',
     tooltip: {},
     radar: {
        indicator: indicators,
        shape: 'polygon',
        splitNumber: 4,
        axisName: { color: textColor, borderRadius: 3, padding: [3, 5], fontSize: 11 },
        splitArea: { show: false },
        axisLine: { lineStyle: { color: isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)' } },
        splitLine: { lineStyle: { color: isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)' } }
     },
     series: [{
        type: 'radar',
        data: [{
           value: data,
           name: '综合评估画像',
           itemStyle: { color: '#0052cc' },
           areaStyle: { color: 'rgba(0,82,204,0.3)' }
        }]
     }]
  })
}

onMounted(() => {
  nextTick(renderRadar)
})
</script>

<style scoped>
.full-h { height: 100%; display: flex; flex-direction: column; }
.full-h :deep(.el-card__body) { flex: 1; }

.relative-overflow { position: relative; overflow: hidden; }
.card-bg-decoration { position: absolute; top: -100px; right: -100px; width: 200px; height: 200px; background: radial-gradient(circle, var(--color-primary-light) 0%, transparent 70%); opacity: 0.15; border-radius: 50%; pointer-events: none; }

.text-center { text-align: center; }

.avatar-ring-container { display: flex; flex-direction: column; align-items: center; justify-content: center; position: relative; }
.avatar-dropper { position: relative; width: 140px; height: 140px; border-radius: 50%; overflow: hidden; cursor: pointer; border: 4px solid var(--color-surface); box-shadow: var(--shadow-neo); }
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.avatar-overlay { position: absolute; bottom: 0; left: 0; width: 100%; height: 45px; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; color: white; opacity: 0; transition: opacity 0.3s; }
.avatar-dropper:hover .avatar-overlay { opacity: 1; }
.profile-score-text { font-size: 13px; font-weight: 700; color: var(--color-text-secondary); }

.profile-name { font-size: 20px; margin:0; color: var(--color-text); font-weight: 800; }
.profile-role-badge { display: inline-block; padding: 6px 14px; border-radius: 999px; font-size: 12px; font-weight: 800; font-family: monospace;}
.badge-admin { background: rgba(234, 67, 53, 0.1); color: #ea4335; border: 1px dashed rgba(234,67,53,0.3) }
.badge-finance { background: rgba(255, 149, 0, 0.1); color: #ff9500; border: 1px dashed rgba(255,149,0,0.3) }
.badge-user { background: rgba(0, 201, 167, 0.1); color: #00c9a7; border: 1px dashed rgba(0,201,167,0.3) }

.info-list { display: flex; flex-direction: column; gap: 16px; text-align: left; padding: 0 10px; }
.info-row { display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid var(--color-border); padding-bottom: 8px;}
.info-label { font-size: 13px; color: var(--color-text-secondary); display: flex; align-items: center; gap: 6px; }
.info-val { font-size: 14px; font-weight: 700; color: var(--color-text); font-family: monospace; }

.custom-tabs :deep(.el-tabs__item) { height: 60px; font-size: 16px; }
.tab-label { display: flex; align-items: center; gap: 8px; font-weight: 700; }
.tab-h3 { font-size: 18px; color: var(--color-text); margin: 0; font-weight: 800; }

.border-dash { border: 1px dashed var(--color-border); padding: 25px; border-radius: 16px; background: rgba(0,82,204,0.02); }

.full-width { width: 100%; }

.strength-track { height: 8px; background: var(--color-surface-hover); border-radius: 4px; overflow: hidden; }
.strength-fill { height: 100%; border-radius: 4px; transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1); }
.strength-1 { background: #ea4335; }
.strength-2 { background: #ff9500; }
.strength-3 { background: #0052cc; }
.strength-4 { background: #00c9a7; }

.device-list { display: grid; gap: 20px; }
.device-card { display: flex; align-items: center; justify-content: space-between; padding: 25px; gap: 20px; border: 1px solid var(--color-border); transition: all 0.3s; }
.device-card:hover { transform: translateY(-3px); box-shadow: var(--shadow-sm); }
.active-device { border-color: var(--color-primary-light); background: rgba(0,82,204,0.02); }

.d-icon-box { font-size: 26px; color: white; width: 60px; height: 60px; display: flex; align-items: center; justify-content: center; border-radius: 16px; flex-shrink: 0; box-shadow: 0 4px 10px rgba(0,0,0,0.1); }
.bg-blue { background: linear-gradient(135deg, #0052cc, #00b2ff); }
.bg-gray { background: linear-gradient(135deg, #7f8c8d, #bdc3c7); }

.device-info { flex: 1; display: flex; flex-direction: column; gap: 8px; }
.device-info strong { font-size: 16px; color: var(--color-text); }
.d-sub { font-size: 12px; color: var(--color-text-secondary); display:flex; align-items:center; gap:4px; font-family:monospace; }
</style>
