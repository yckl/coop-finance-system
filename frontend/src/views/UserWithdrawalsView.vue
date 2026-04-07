<template>
  <div class="module-page withdraw-page">
    <template v-if="role !== 'user'">
      <!-- Wizard Header -->
      <div class="module-topbar print-hide" style="margin-bottom: 20px;">
        <div>
          <div class="panel-title text-danger">提现出栈与外调中心 (Withdrawal)</div>
          <div class="panel-subtitle">严防死守每一笔流出！大于 5 万元必须启动风控二级面签核准。</div>
        </div>
        <div class="topbar-actions">
          <el-button color="#ea4335" :dark="true" plain icon="RefreshLeft" @click="resetForm">阻断并重置流转</el-button>
        </div>
      </div>
      
      <div class="stepper-area print-hide glass-card mb-4" style="border: 1px solid rgba(234,67,53,0.2);">
        <el-steps :active="currentStep" finish-status="success" align-center class="neo-steps steps-danger">
          <el-step title="步骤 1: 核验用户身份" icon="UserFilled" />
          <el-step title="步骤 2: 出账审计防线" icon="Lock" />
          <el-step title="步骤 3: 归档打印回执" icon="Printer" />
        </el-steps>
      </div>

      <div class="wizard-container print-hide">
         <!-- Step 1: Search -->
         <transition name="slide-fade" mode="out-in">
           <el-card v-if="currentStep === 0" class="glass-card wizard-card" shadow="never">
             <div class="wizard-content-box">
                <div class="wiz-icon bg-red"><el-icon><Postcard /></el-icon></div>
                <h2>验证取款人身份凭证</h2>
                <p class="text-desc">可通过扫描证件或手动输入查询用户信息。</p>
                
                <el-form class="neo-form mt-4" style="width: 100%; max-width: 400px; margin: 0 auto;">
                  <el-form-item class="neo-input">
                    <el-input v-model="searchUserNo" placeholder="请输入数字网联 ID (如 U1001)" size="large" prefix-icon="Search" />
                  </el-form-item>
                  <el-button type="danger" size="large" class="neo-btn w-100" @click="searchUser" :loading="isSearching">
                    <el-icon><Connection /></el-icon> 执行底层握手并锁定
                  </el-button>
                </el-form>
             </div>
           </el-card>

           <!-- Step 2: Withdraw Amount & Preview -->
           <el-card v-else-if="currentStep === 1" class="glass-card wizard-card" shadow="never">
             <div class="withdraw-grid">
                <!-- Left Details -->
                <div class="user-id-card neo-input">
                   <div class="id-header">
                      <el-avatar :size="60" style="background:#ea4335; font-size:24px;">{{ activeAccount.user_name?.charAt(0) }}</el-avatar>
                      <div class="id-meta">
                         <h3 style="margin:0;">{{ activeAccount.user_name }}</h3>
                         <span style="font-family:monospace; color:var(--color-danger);">ID: {{ activeAccount.user_no }}</span>
                      </div>
                   </div>
                   <el-divider border-style="dashed" />
                   <div class="id-row"><span>关联主结算卡</span> <strong>{{ activeAccount.account_no }}</strong></div>
                   <div class="id-row"><span>风控标识</span> <el-tag size="small" :type="activeAccount.account_status === '正常' ? 'success' : 'danger'" effect="dark" round>{{ activeAccount.account_status }}</el-tag></div>
                   
                   <div class="balance-preview-box mt-4">
                      <div class="bp-title">库箱允许出境额度上限 (¥)</div>
                      <div class="bp-amount">
                         {{ Number(activeAccount.available_balance).toLocaleString('zh-CN', {minimumFractionDigits: 2}) }}
                      </div>
                      <div class="bp-sub" v-if="form.amount > 0">
                         拟扣除: <strong class="text-danger">- {{ Number(form.amount || 0).toLocaleString('zh-CN', {minimumFractionDigits: 2}) }}</strong><br>
                         剩余预期: <strong>{{ Math.max(0, Number(activeAccount.available_balance) - Number(form.amount || 0)).toLocaleString('zh-CN', {minimumFractionDigits: 2}) }}</strong>
                      </div>
                   </div>
                </div>

                <!-- Right Form -->
                <div class="transaction-form neo-form">
                   <div style="display:flex; justify-content:space-between; align-items:center;">
                     <h3 style="margin:0;"><el-icon><WarningFilled color="#ea4335"/></el-icon> 出款指令参数</h3>
                     <el-tag v-if="isLargeSum" type="danger" effect="dark" class="pulse-tag">大额提现预警网点</el-tag>
                   </div>
                   
                   <el-form :model="form" label-position="top" class="mt-4">
                     <el-form-item label="拟提出敞口额度 (¥)" class="neo-input" required>
                       <el-input-number v-model="form.amount" :min="1" :precision="2" :step="1000" size="large" style="width: 100%;" />
                     </el-form-item>
                     
                     <el-row :gutter="20">
                       <el-col :span="12">
                         <el-form-item label="路由网络" class="neo-input" required>
                           <el-select v-model="form.type" size="large">
                             <el-option label="ATM实体钞提取" value="现金取款" />
                             <el-option label="银联跨站路由" value="跨行划拨" />
                             <el-option label="企业公户冲账" value="对公户转账" />
                           </el-select>
                         </el-form-item>
                       </el-col>
                       <el-col :span="12">
                         <el-form-item label="事由附言" class="neo-input">
                           <el-input v-model="form.remark" placeholder="非必须" size="large" />
                         </el-form-item>
                       </el-col>
                     </el-row>
                     
                     <div class="step-footer mt-4">
                        <el-button size="large" @click="currentStep = 0" class="neo-btn">终止并销毁信息</el-button>
                        <el-button type="danger" size="large" class="neo-btn" @click="handleWithdrawAuth" style="flex:1" :loading="isSubmitting">
                          <el-icon><Unlock/></el-icon> {{ isLargeSum ? '唤起高危二次密配 (指纹/人脸)' : '验证放水' }}
                        </el-button>
                     </div>
                   </el-form>
                </div>
             </div>
           </el-card>

           <!-- Step 3: Success & Print Details -->
           <el-card v-else-if="currentStep >= 2" class="glass-card wizard-card text-center" shadow="never">
              <el-icon color="#ea4335" :size="80" class="mb-3"><CircleCheckFilled /></el-icon>
              <h2 style="color:var(--color-danger)">账务处理完成！发票已生成！</h2>
              <p class="text-desc">流水记录锁: <span style="font-family:monospace; color:var(--color-danger)">{{ receiptData.serialNo }}</span></p>
              
              <div class="success-actions mt-4">
                 <el-button type="danger" size="large" @click="handlePrintModal" class="neo-btn" plain>
                   <el-icon><Printer /></el-icon> 提取出款电子票据 (PDF)
                 </el-button>
                 <el-button type="info" size="large" @click="resetForm" class="neo-btn">
                   回到冰冷原点
                 </el-button>
              </div>
           </el-card>
         </transition>
      </div>

      <!-- Authorization Modal Mock -->
      <el-dialog v-model="authVisible" title="安全拦截：全息多端核对" width="400px" style="border-radius:16px;" :show-close="false">
         <div class="auth-ui">
            <div class="auth-scanner"></div>
            <p>正在读取生物面部流 / 掌纹静脉...</p>
            <div class="auth-warning">
               当前提取额超 <strong>50,000 元</strong>大关，已触发洗钱网络红头文件，请必须肉眼核对客户脸部与证件！
            </div>
         </div>
         <template #footer>
            <el-button @click="authVisible = false; isSubmitting=false">人证不符，中断！</el-button>
            <el-button type="danger" icon="Key" class="neo-btn" @click="executeWithdrawal">密钥对接相符，强制放水！</el-button>
         </template>
      </el-dialog>

      <!-- High Fidelity Print Modal -->
      <el-dialog v-model="printVisible" title="高逼真电子印鉴预览器" width="800px" style="border-radius:16px;">
         <div class="print-container" id="printable-receipt">
            <div class="receipt-paper">
               <div class="receipt-watermark text-danger">COOP OUT-FLOW</div>
               <div class="r-header">
                  <h2>农村商业信用联合社</h2>
                  <p>【柜台出款 / 现金扣减回单】</p>
               </div>
               
               <div class="r-info-row">
                  <div><strong>防伪溯源单号:</strong> {{ receiptData.serialNo }}</div>
                  <div><strong>发生界限时间:</strong> {{ new Date().toLocaleString() }}</div>
               </div>
               
               <table class="r-table">
                 <tr>
                   <td class="r-label">储户主体名</td><td class="r-value">{{ activeAccount.user_name }}</td>
                   <td class="r-label">系统底座编号</td><td class="r-value">{{ receiptData.userNo }}</td>
                 </tr>
                 <tr>
                   <td class="r-label">发生业务口径</td><td class="r-value">{{ receiptData.type }}</td>
                   <td class="r-label">处理结果态</td><td class="r-value"><strong style="color: #ea4335;">出账放行</strong></td>
                 </tr>
                 <tr>
                   <td class="r-label">提出敞口 (RMB)</td>
                   <td colspan="3" class="r-amount-cell">
                     <span class="rmb-symbol">¥</span> 
                     {{ Number(receiptData.amount).toLocaleString('zh-CN', {minimumFractionDigits: 2}) }}
                   </td>
                 </tr>
                 <tr>
                   <td class="r-label">更新后残余额</td><td class="r-value">{{ Number(receiptData.balanceAfter).toLocaleString('zh-CN', {minimumFractionDigits: 2}) }}</td>
                   <td class="r-label">柜台处理人</td><td class="r-value">{{ receiptData.operator || 'SYS_FIN' }}</td>
                 </tr>
               </table>
               
               <div class="r-footer">
                  <div class="r-qrcode"></div>
                  <div class="r-stamp">资金出栈<br>专用章</div>
                  <div>本单据具有法律约束能力，请勿损毁或篡改。<br>操作验证流: <span style="font-family:monospace; font-size:10px;">{{ Math.random().toString(36).substring(2, 15) }}</span></div>
               </div>
            </div>
         </div>
         <template #footer>
            <el-button @click="printVisible = false">关闭窗口</el-button>
            <el-button type="danger" icon="Printer" class="neo-btn" @click="executePrint">硬列印实体纸张</el-button>
         </template>
      </el-dialog>

    </template>

    <!-- USER VIEW (List Records) -->
    <template v-else>
      <div class="module-topbar print-hide" style="margin-bottom:20px;">
        <div>
          <div class="panel-title">我的资金失血记录谱</div>
          <div class="panel-subtitle">查考核实您账户下的每一笔抽水扣减与时间线轨迹</div>
        </div>
        <div class="topbar-actions">
          <el-button color="#ea4335" :dark="true" icon="Download" @click="fakeExportPDF">提取防丢账单 PDF</el-button>
          <el-button type="danger" plain @click="loadUserData" icon="Refresh">刷新数据</el-button>
        </div>
      </div>
      
      <el-card class="glass-card panel-card" shadow="never" style="min-height: 500px;">
        <el-timeline v-if="userData.length > 0" class="mt-3">
          <el-timeline-item
            v-for="(item, index) in userData"
            :key="index"
            :timestamp="item.created_at"
            placement="top"
            color="#ea4335"
          >
            <el-card shadow="hover" class="timeline-card">
               <div class="tc-header">
                 <span class="tc-title"><el-icon color="#ea4335"><Wallet/></el-icon> {{ item.business_type }}</span>
                 <span class="tc-amount text-danger">- ¥ {{ Number(item.amount).toLocaleString('zh-CN', {minimumFractionDigits:2}) }}</span>
               </div>
               <div class="tc-body">
                 <div class="tc-row"><span>流水哈希码:</span> <strong>{{ item.serial_no }}</strong></div>
                 <div class="tc-row"><span>系统风控探针:</span> <el-tag :type="item.warning_level === '高' ? 'danger' : 'info'" size="small">{{ item.warning_level || '安全通道' }}</el-tag></div>
               </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="您的资产暂未发生失血现象" />
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RefreshLeft, UserFilled, Lock, Printer, Search, Connection, Postcard, WarningFilled, Unlock, CircleCheckFilled, Key } from '@element-plus/icons-vue'
import http from '../api'

const route = useRoute()
const role = route.meta.role || 'finance'
const currentStep = ref(0)
const stepCompleted = ref(false)

const searchUserNo = ref('')
const activeAccount = ref({})
const form = ref({ amount: 1000, type: '现金取款', remark: '' })
const receiptData = ref({})
const userData = ref([])

const isSearching = ref(false)
const isSubmitting = ref(false)
const printVisible = ref(false)
const authVisible = ref(false)

const isLargeSum = computed(() => form.value.amount > 50000)

async function loadUserData() {
  if (role !== 'user') return
  try {
    const res = await http.get(`/user/module/withdrawals`)
    userData.value = res.data.records || []
  } catch (e) {
    ElMessage.error(String(e))
  }
}

function fakeExportPDF() {
  ElMessage.success('正在为您生成带公章的个人提取对账单 (PDF)... 开始下载。')
}

onMounted(() => {
  if (role === 'user') loadUserData()
})

async function searchUser() {
  if (!searchUserNo.value) return ElMessage.warning('未能扑捉实体目标 ID')
  
  isSearching.value = true
  try {
    const res = await http.get(`/${role}/account/${searchUserNo.value}`)
    activeAccount.value = res.data
    if(!activeAccount.value.user_name) activeAccount.value.user_name = "审核验签主体" 
    
    setTimeout(() => {
       isSearching.value = false
       currentStep.value = 1
    }, 600)
    
  } catch (e) {
    ElMessage.error(String(e))
    isSearching.value = false
  }
}

function handleWithdrawAuth() {
  if (activeAccount.value.account_status !== '正常') return ElMessage.error('红线防线：该金库通道目前被法务锁死操作栈！')
  if (form.value.amount > activeAccount.value.available_balance) return ElMessage.error('严重：您所拟剥除的储备远大于该对象的可用极限！')
  
  isSubmitting.value = true
  if(isLargeSum.value) {
     authVisible.value = true
  } else {
     executeWithdrawal()
  }
}

async function executeWithdrawal() {
  authVisible.value = false
  try {
    const payload = {
      userNo: activeAccount.value.user_no,
      amount: form.value.amount,
      type: form.value.type,
      operator: '超级柜员',
      remark: form.value.remark
    }
    const res = await http.post(`/${role}/transaction/withdraw`, payload)
    
    receiptData.value = {
      ...payload,
      serialNo: res.data.serialNo || res.serialNo || ('TX-'+Date.now()),
      balanceAfter: res.data.balanceAfter || res.balanceAfter || (Number(activeAccount.value.available_balance) - Number(form.value.amount))
    }
    
    setTimeout(() => {
       isSubmitting.value = false
       currentStep.value = 2
       stepCompleted.value = true
    }, 800)
    
  } catch(e) {
    ElMessage.error(String(e))
    isSubmitting.value = false
  }
}

function handlePrintModal() {
  printVisible.value = true
}

function executePrint() {
  printVisible.value = false
  setTimeout(() => window.print(), 300)
}

function resetForm() {
  currentStep.value = 0
  stepCompleted.value = false
  searchUserNo.value = ''
  activeAccount.value = {}
  form.value = { amount: 1000, type: '现金取款', remark: '' }
}
</script>

<style scoped>
.withdraw-page { display: flex; flex-direction: column; min-height: calc(100vh - 120px); }

/* Warning Modifiers */
.text-danger { color: var(--color-danger); }
.bg-red { background: linear-gradient(135deg, #ea4335, #c0392b); }
.steps-danger :deep(.is-success) { color: #ea4335; border-color: #ea4335; }
.steps-danger :deep(.is-success .el-step__line) { background-color: #ea4335; }

.wizard-container { flex: 1; display: flex; justify-content: center; align-items: start; margin-top: 20px; }
.wizard-card { width: 100%; max-width: 900px; padding: 30px; border-radius: 20px; min-height: 400px; display: flex; flex-direction: column; justify-content: center; border: 1px solid rgba(234,67,53,0.1); }

.wizard-content-box { text-align: center; display: flex; flex-direction: column; align-items: center; }
.wiz-icon { width: 70px; height: 70px; border-radius: 20px; color: white; display: flex; justify-content: center; align-items: center; font-size: 32px; margin-bottom: 20px; box-shadow: 0 10px 25px rgba(234,67,53,0.2); }
.wizard-content-box h2 { margin: 0 0 10px; font-weight: 800; font-size: 24px; color: var(--color-text); }
.text-desc { color: var(--color-text-secondary); margin: 0; font-size: 15px; }

/* Slide Fade Transition */
.slide-fade-enter-active, .slide-fade-leave-active { transition: all 0.4s cubic-bezier(0.2, 0.8, 0.2, 1); }
.slide-fade-enter-from { opacity: 0; transform: translateX(30px); }
.slide-fade-leave-to { opacity: 0; transform: translateX(-30px); position: absolute; }

/* Grid Layout for Step 2 */
.withdraw-grid { display: grid; grid-template-columns: 1fr 1.5fr; gap: 40px; }
.user-id-card { padding: 25px; border-radius: 16px; background: var(--color-surface); height: 100%; border: 1px solid var(--color-border); }
.id-header { display: flex; align-items: center; gap: 15px; margin-bottom: 20px; }
.id-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; font-size: 14px; }
.id-row span { color: var(--color-text-secondary); }
.id-row strong { font-family: monospace; font-size: 15px; color: var(--color-text); }

/* Animated Balance Box */
.balance-preview-box { background: rgba(234, 67, 53, 0.05); padding: 20px; border-radius: 12px; border: 1px solid rgba(234, 67, 53, 0.2); text-align: center; }
.bp-title { font-size: 13px; color: var(--color-text-secondary); font-weight: 600; margin-bottom: 5px; }
.bp-amount { font-size: 26px; font-weight: 800; font-family: 'Inter', sans-serif; color: var(--color-text); margin-bottom: 5px;}
.bp-sub { font-size: 12px; color: var(--color-text-secondary); padding-top: 10px; border-top: 1px dashed rgba(234, 67, 53, 0.2);}

@keyframes pulseBorder { 0% { box-shadow: 0 0 0 0 rgba(234,67,53,0.4); } 70% { box-shadow: 0 0 0 10px rgba(234,67,53,0); } 100% { box-shadow: 0 0 0 0 rgba(234,67,53,0); } }
.pulse-tag { animation: pulseBorder 2s infinite; }

.step-footer { display: flex; gap: 15px; }
.success-actions { display: flex; justify-content: center; gap: 20px; }

/* Auth Simulation */
.auth-ui { display: flex; flex-direction: column; align-items: center; padding: 20px 0; }
.auth-scanner { width: 120px; height: 120px; border: 2px dashed #0052cc; border-radius: 50%; position: relative; overflow: hidden; margin-bottom: 20px; }
.auth-scanner::after { content: ''; position: absolute; top:0; left:0; right:0; height: 10px; background: rgba(0,201,167,0.5); box-shadow: 0 0 20px #00c9a7; animation: scanAnim 2s infinite alternate; }
@keyframes scanAnim { 0% { top: 0; } 100% { top: calc(100% - 10px); } }
.auth-warning { background: rgba(234,67,53,0.1); border-left: 4px solid #ea4335; padding: 10px; font-size: 13px; margin-top: 15px; border-radius: 4px; }

/* Print Modal & Paper Simulation */
.print-container { background: #e0e5ec; padding: 40px; display: flex; justify-content: center; }
.receipt-paper { background: #fffaf0; width: 600px; padding: 40px; box-shadow: 20px 20px 60px #bec3c9, -20px -20px 60px #ffffff; position: relative; overflow: hidden; color: #333; }
.receipt-watermark { position: absolute; font-size: 70px; font-weight: 900; color: rgba(234,67,53,0.04); transform: rotate(-30deg); top: 30%; left: 5%; pointer-events: none; white-space: nowrap; }

.r-header { text-align: center; border-bottom: 2px solid #555; padding-bottom: 10px; margin-bottom: 20px; }
.r-header h2 { margin: 0; font-size: 24px; font-weight: 900; letter-spacing: 2px; }
.r-header p { margin: 5px 0 0; font-size: 16px; font-weight: bold; letter-spacing: 10px; padding-left: 10px; }

.r-info-row { display: flex; justify-content: space-between; font-size: 13px; font-family: monospace; margin-bottom: 15px; }

.r-table { width: 100%; border-collapse: collapse; font-size: 14px; }
.r-table td { border: 1px solid #777; padding: 12px; }
.r-label { background: #fdf5f5; width: 130px; font-weight: 600; }
.r-value { font-family: monospace; font-weight: 600; width: 150px; }
.r-amount-cell { font-family: 'Inter', sans-serif; font-size: 24px; font-weight: 800; color: #ea4335; letter-spacing: 1px; }
.rmb-symbol { font-size: 18px; margin-right: 5px; }

.r-footer { margin-top: 30px; display: flex; justify-content: space-between; align-items: flex-end; font-size: 12px; color: #666; position: relative; }
.r-qrcode { width: 60px; height: 60px; background: repeating-linear-gradient(-45deg, #000 0, #000 2px, #fff 2px, #fff 4px); }
.r-stamp { position: absolute; right: 20px; top: -40px; width: 80px; height: 80px; border: 4px double #ea4335; border-radius: 50%; color: #ea4335; display: flex; align-items: center; justify-content: center; font-size: 16px; font-weight: 900; transform: rotate(15deg); opacity: 0.8; text-align: center; }

@media print {
  body * { visibility: hidden; }
  .print-hide { display: none !important; }
  #printable-receipt, #printable-receipt * { visibility: visible; }
  #printable-receipt { position: absolute; left: 0; top: 0; padding: 0; background: none; }
  .receipt-paper { box-shadow: none; width: 100%; padding: 20px; border: 1px solid #000; }
}

@media(max-width:900px){
  .withdraw-grid { grid-template-columns: 1fr; }
}

/* Timeline Styles */
.timeline-card { border-radius: 12px; border: 1px solid var(--color-border); background: var(--color-surface); padding: 15px; margin-bottom: 5px;}
.tc-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px dashed var(--color-border); padding-bottom: 12px; margin-bottom: 12px; }
.tc-title { font-size: 15px; font-weight: 700; color: var(--color-text); display: flex; align-items: center; gap: 8px;}
.tc-amount { font-size: 18px; font-weight: 800; font-family: 'Inter', sans-serif; }
.tc-body { display: flex; flex-direction: column; gap: 8px; }
.tc-row { display: flex; justify-content: space-between; font-size: 13px; }
.tc-row span { color: var(--color-text-secondary); }
.tc-row strong { font-family: monospace; color: var(--color-text); }
</style>
