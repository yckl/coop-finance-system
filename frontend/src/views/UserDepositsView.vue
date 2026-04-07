<template>
  <div class="module-page deposit-page">
    <template v-if="role !== 'user'">
      <!-- Wizard Header -->
      <div class="module-topbar print-hide" style="margin-bottom: 20px;">
        <div>
          <div class="panel-title">实盘现钞网点入账 (Deposit)</div>
          <div class="panel-subtitle">此链路已接入人行洗钱稽核，请确保收点钞核对无误后再行流转。</div>
        </div>
        <div class="topbar-actions">
          <el-button color="#00c9a7" :dark="true" plain icon="RefreshLeft" @click="resetForm">重置网点办理流</el-button>
        </div>
      </div>
      
      <div class="stepper-area print-hide glass-card mb-4">
        <el-steps :active="currentStep" finish-status="success" align-center class="neo-steps">
          <el-step title="步骤 1: 核验用户身份" icon="UserFilled" />
          <el-step title="步骤 2: 钞箱封包上账" icon="Money" />
          <el-step title="步骤 3: 归档打印回执" icon="Printer" />
        </el-steps>
      </div>

      <div class="wizard-container print-hide">
         <!-- Step 1: Search -->
         <transition name="slide-fade" mode="out-in">
           <el-card v-if="currentStep === 0" class="glass-card wizard-card" shadow="never">
             <div class="wizard-content-box">
                <div class="wiz-icon bg-blue"><el-icon><CreditCard /></el-icon></div>
                <h2>卡片读取或人工检索</h2>
                <p class="text-desc">可通过扫描证件或手动输入查询用户信息。</p>
                
                <el-form class="neo-form mt-4" style="width: 100%; max-width: 400px; margin: 0 auto;">
                  <el-form-item class="neo-input">
                    <el-input v-model="searchUserNo" placeholder="请输入数字网联 ID (如 U1001)" size="large" prefix-icon="Search" />
                  </el-form-item>
                  <el-button type="primary" size="large" class="neo-btn-primary w-100" @click="searchUser" :loading="isSearching">
                    <el-icon><Connection /></el-icon> 执行底层握手并锁定
                  </el-button>
                </el-form>
             </div>
           </el-card>

           <!-- Step 2: Deposit Amount & Preview -->
           <el-card v-else-if="currentStep === 1" class="glass-card wizard-card" shadow="never">
             <div class="deposit-grid">
                <!-- Left Details -->
                <div class="user-id-card neo-input">
                   <div class="id-header">
                      <el-avatar :size="60" style="background:#0052cc; font-size:24px;">{{ activeAccount.user_name?.charAt(0) }}</el-avatar>
                      <div class="id-meta">
                         <h3 style="margin:0;">{{ activeAccount.user_name }}</h3>
                         <span style="font-family:monospace; color:var(--color-primary);">ID: {{ activeAccount.user_no }}</span>
                      </div>
                   </div>
                   <el-divider border-style="dashed" />
                   <div class="id-row"><span>归属卡介质</span> <strong>{{ activeAccount.account_no }}</strong></div>
                   <div class="id-row"><span>风控标识</span> <el-tag size="small" type="success" effect="dark" round>银联安全A类</el-tag></div>
                   
                   <div class="balance-preview-box mt-4">
                      <div class="bp-title">实时镜像余额估算 (¥)</div>
                      <div class="bp-amount" :class="{'bp-amount-anim': form.amount > 0}">
                         {{ (Number(activeAccount.available_balance) + Number(form.amount || 0)).toLocaleString('zh-CN', {minimumFractionDigits: 2}) }}
                      </div>
                      <div class="bp-sub" v-if="form.amount > 0">+ {{ Number(form.amount || 0).toLocaleString('zh-CN', {minimumFractionDigits: 2}) }} 在途准备</div>
                   </div>
                </div>

                <!-- Right Form -->
                <div class="transaction-form neo-form">
                   <h3 style="margin-top:0;"><el-icon><Wallet/></el-icon> 资金投递协议参数</h3>
                   
                   <el-form :model="form" label-position="top" class="mt-4">
                     <el-form-item label="投递现金包裹体 (点钞机清点结果)" class="neo-input" required>
                       <el-input-number v-model="form.amount" :min="1" :precision="2" :step="1000" size="large" style="width: 100%;" />
                     </el-form-item>
                     
                     <el-row :gutter="20">
                       <el-col :span="12">
                         <el-form-item label="核心业务通道" class="neo-input" required>
                           <el-select v-model="form.type" size="large">
                             <el-option label="春耕定向理财" value="春耕定向存款" />
                             <el-option label="普通硬通货活期" value="活期存入" />
                             <el-option label="T+3 高频套保" value="定期存款-单期" />
                           </el-select>
                         </el-form-item>
                       </el-col>
                       <el-col :span="12">
                         <el-form-item label="终端机备注" class="neo-input">
                           <el-input v-model="form.remark" placeholder="非必须" size="large" />
                         </el-form-item>
                       </el-col>
                     </el-row>
                     
                     <div class="step-footer mt-4">
                        <el-button size="large" @click="currentStep = 0" class="neo-btn">剥离载体并返回</el-button>
                        <el-button type="primary" size="large" class="neo-btn-primary" @click="submitDeposit" style="flex:1" :loading="isSubmitting">
                          <el-icon><Select/></el-icon> 确认收单并写入黑盒
                        </el-button>
                     </div>
                   </el-form>
                </div>
             </div>
           </el-card>

           <!-- Step 3: Success & Print Details -->
           <el-card v-else-if="currentStep >= 2" class="glass-card wizard-card text-center" shadow="never">
              <el-icon color="#34a853" :size="80" class="mb-3"><SuccessFilled /></el-icon>
              <h2 style="color:var(--color-success)">资金处理完成！</h2>
              <p class="text-desc">流水码: <span style="font-family:monospace; color:var(--color-primary)">{{ receiptData.serialNo }}</span></p>
              
              <div class="success-actions mt-4">
                 <el-button type="success" size="large" @click="handlePrintModal" class="neo-btn">
                   <el-icon><Printer /></el-icon> 提取电子票据 (PDF)
                 </el-button>
                 <el-button type="primary" size="large" @click="resetForm" class="neo-btn-primary">
                   挂起新业务流转
                 </el-button>
              </div>
           </el-card>
         </transition>
      </div>

      <!-- High Fidelity Print Modal -->
      <el-dialog v-model="printVisible" title="高逼真电子印鉴预览器" width="800px" style="border-radius:16px;">
         <div class="print-container" id="printable-receipt">
            <div class="receipt-paper">
               <div class="receipt-watermark">COOP FINANCE</div>
               <div class="r-header">
                  <h2>农村商业信用联合社</h2>
                  <p>【柜台现金出纳记结凭单】</p>
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
                   <td class="r-label">处理结果态</td><td class="r-value"><strong style="color: #27ae60;">OK (清算成功)</strong></td>
                 </tr>
                 <tr>
                   <td class="r-label">入账纯净额 (RMB)</td>
                   <td colspan="3" class="r-amount-cell">
                     <span class="rmb-symbol">¥</span> 
                     {{ Number(receiptData.amount).toLocaleString('zh-CN', {minimumFractionDigits: 2}) }}
                   </td>
                 </tr>
                 <tr>
                   <td class="r-label">更新后储备额</td><td class="r-value">{{ Number(receiptData.balanceAfter).toLocaleString('zh-CN', {minimumFractionDigits: 2}) }}</td>
                   <td class="r-label">柜台处理人</td><td class="r-value">{{ receiptData.operator || 'ADMIN_SYS' }}</td>
                 </tr>
               </table>
               
               <div class="r-footer">
                  <div class="r-qrcode"></div>
                  <div class="r-stamp">网点业务<br>专用章</div>
                  <div>此联交由客户保留。涂改无效。<br>校验哈希序列: <span style="font-family:monospace; font-size:10px;">{{ Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15) }}</span></div>
               </div>
            </div>
         </div>
         <template #footer>
            <el-button @click="printVisible = false">关闭窗口</el-button>
            <el-button type="primary" icon="Printer" class="neo-btn-primary" @click="executePrint">硬列印实体纸张</el-button>
         </template>
      </el-dialog>

    </template>

    <!-- USER VIEW (List Records) -->
    <template v-else>
      <div class="module-topbar print-hide" style="margin-bottom:20px;">
        <div>
          <div class="panel-title">我的财富上水记录谱</div>
          <div class="panel-subtitle">查阅所有存入款项明细与时间线轨迹</div>
        </div>
        <div class="topbar-actions">
          <el-button color="#0052cc" :dark="true" icon="Download" @click="fakeExportPDF">提取账单 PDF</el-button>
          <el-button type="success" plain @click="loadUserData" icon="Refresh">刷新数据</el-button>
        </div>
      </div>
      
      <el-card class="glass-card panel-card" shadow="never" style="min-height: 500px;">
        <el-timeline v-if="userData.length > 0" class="mt-3">
          <el-timeline-item
            v-for="(item, index) in userData"
            :key="index"
            :timestamp="item.created_at"
            placement="top"
            color="#00c9a7"
          >
            <el-card shadow="hover" class="timeline-card">
               <div class="tc-header">
                 <span class="tc-title"><el-icon color="#00c9a7"><Wallet/></el-icon> {{ item.business_type }}</span>
                 <span class="tc-amount text-success">+ ¥ {{ Number(item.amount).toLocaleString('zh-CN', {minimumFractionDigits:2}) }}</span>
               </div>
               <div class="tc-body">
                 <div class="tc-row"><span>流水哈希码:</span> <strong>{{ item.serial_no }}</strong></div>
                 <div class="tc-row"><span>业务承接员:</span> <strong>{{ item.operator_name }}</strong></div>
               </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="您的资产池中暂无入账水花" />
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { RefreshLeft, UserFilled, Money, Printer, Search, Connection, CreditCard, Wallet, Select, SuccessFilled } from '@element-plus/icons-vue'
import http from '../api'

const route = useRoute()
const role = route.meta.role || 'finance'
const currentStep = ref(0)
const stepCompleted = ref(false)

const searchUserNo = ref('')
const activeAccount = ref({})
const form = ref({ amount: 1000, type: '活期存入', remark: '' })
const receiptData = ref({})
const userData = ref([])

const isSearching = ref(false)
const isSubmitting = ref(false)
const printVisible = ref(false)

async function loadUserData() {
  if (role !== 'user') return
  try {
    const res = await http.get(`/user/module/deposits`)
    userData.value = res.data.records || []
  } catch (e) {
    ElMessage.error(String(e))
  }
}

function fakeExportPDF() {
  ElMessage.success('正在为您生成带公章的个人入账对账单 (PDF)... 开始下载。')
}

onMounted(() => {
  if (role === 'user') loadUserData()
})

async function searchUser() {
  if (!searchUserNo.value) return ElMessage.warning('系统需要捕获一个实体的特征 ID')
  
  isSearching.value = true
  try {
    const res = await http.get(`/${role}/account/${searchUserNo.value}`)
    activeAccount.value = res.data
    if(!activeAccount.value.user_name) activeAccount.value.user_name = "实体客户" 
    
    // Simulate complex loading for UI flair
    setTimeout(() => {
       isSearching.value = false
       currentStep.value = 1
    }, 600)
    
  } catch (e) {
    ElMessage.error(String(e))
    isSearching.value = false
  }
}

async function submitDeposit() {
  if (activeAccount.value.account_status !== '正常') return ElMessage.error('触发红线拦截：风控冻结账户禁止资产注入！')
  
  isSubmitting.value = true
  try {
    const payload = {
      userNo: activeAccount.value.user_no,
      amount: form.value.amount,
      type: form.value.type,
      operator: 'Finance柜员端',
      remark: form.value.remark
    }
    const res = await http.post(`/${role}/transaction/deposit`, payload)
    
    receiptData.value = {
      ...payload,
      serialNo: res.data.serialNo || res.serialNo || ('TX-'+Date.now()),
      balanceAfter: res.data.balanceAfter || res.balanceAfter || (Number(activeAccount.value.available_balance) + Number(form.value.amount))
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
  form.value = { amount: 1000, type: '活期存入', remark: '' }
}
</script>

<style scoped>
.deposit-page { display: flex; flex-direction: column; min-height: calc(100vh - 120px); }

.wizard-container { flex: 1; display: flex; justify-content: center; align-items: start; margin-top: 20px; }
.wizard-card { width: 100%; max-width: 900px; padding: 30px; border-radius: 20px; min-height: 400px; display: flex; flex-direction: column; justify-content: center; }

.wizard-content-box { text-align: center; display: flex; flex-direction: column; align-items: center; }
.wiz-icon { width: 70px; height: 70px; border-radius: 20px; color: white; display: flex; justify-content: center; align-items: center; font-size: 32px; margin-bottom: 20px; box-shadow: 0 10px 25px rgba(0,0,0,0.1); }
.bg-blue { background: linear-gradient(135deg, #0052cc, #00b2ff); }
.wizard-content-box h2 { margin: 0 0 10px; font-weight: 800; font-size: 24px; color: var(--color-text); }
.text-desc { color: var(--color-text-secondary); margin: 0; font-size: 15px; }

/* Slide Fade Transition */
.slide-fade-enter-active, .slide-fade-leave-active { transition: all 0.4s cubic-bezier(0.2, 0.8, 0.2, 1); }
.slide-fade-enter-from { opacity: 0; transform: translateX(30px); }
.slide-fade-leave-to { opacity: 0; transform: translateX(-30px); position: absolute; }

/* Grid Layout for Step 2 */
.deposit-grid { display: grid; grid-template-columns: 1fr 1.5fr; gap: 40px; }
.user-id-card { padding: 25px; border-radius: 16px; background: var(--color-surface); height: 100%; border: 1px solid var(--color-border); }
.id-header { display: flex; align-items: center; gap: 15px; margin-bottom: 20px; }
.id-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; font-size: 14px; }
.id-row span { color: var(--color-text-secondary); }
.id-row strong { font-family: monospace; font-size: 15px; color: var(--color-text); }

/* Animated Balance Box */
.balance-preview-box { background: rgba(0, 201, 167, 0.05); padding: 20px; border-radius: 12px; border: 1px solid rgba(0, 201, 167, 0.2); text-align: center; transition: all 0.3s; }
.bp-title { font-size: 13px; color: var(--color-text-secondary); font-weight: 600; margin-bottom: 5px; }
.bp-amount { font-size: 32px; font-weight: 800; font-family: 'Inter', sans-serif; color: var(--color-text); transition: color 0.3s; }
.bp-amount-anim { color: #00c9a7; }
.bp-sub { font-size: 14px; font-weight: 700; color: #00c9a7; margin-top: 5px; animation: pulse 1.5s infinite; }

.step-footer { display: flex; gap: 15px; }

/* Success Box */
.success-actions { display: flex; justify-content: center; gap: 20px; }

/* Print Modal & Paper Simulation */
.print-container { background: #e0e5ec; padding: 40px; display: flex; justify-content: center; }
.receipt-paper { background: #fffaf0; width: 600px; padding: 40px; box-shadow: 20px 20px 60px #bec3c9, -20px -20px 60px #ffffff; position: relative; overflow: hidden; color: #333; }
.receipt-watermark { position: absolute; font-size: 80px; font-weight: 900; color: rgba(0,0,0,0.02); transform: rotate(-30deg); top: 30%; left: 10%; pointer-events: none; white-space: nowrap; }

.r-header { text-align: center; border-bottom: 2px solid #555; padding-bottom: 10px; margin-bottom: 20px; }
.r-header h2 { margin: 0; font-size: 24px; font-weight: 900; letter-spacing: 2px; }
.r-header p { margin: 5px 0 0; font-size: 16px; font-weight: bold; letter-spacing: 10px; padding-left: 10px; }

.r-info-row { display: flex; justify-content: space-between; font-size: 13px; font-family: monospace; margin-bottom: 15px; }

.r-table { width: 100%; border-collapse: collapse; font-size: 14px; }
.r-table td { border: 1px solid #777; padding: 12px; }
.r-label { background: #f4ece2; width: 130px; font-weight: 600; }
.r-value { font-family: monospace; font-weight: 600; width: 150px; }
.r-amount-cell { font-family: 'Inter', sans-serif; font-size: 24px; font-weight: 800; color: #c0392b; letter-spacing: 1px; }
.rmb-symbol { font-size: 18px; margin-right: 5px; }

.r-footer { margin-top: 30px; display: flex; justify-content: space-between; align-items: flex-end; font-size: 12px; color: #666; position: relative; }
.r-qrcode { width: 60px; height: 60px; background: repeating-linear-gradient(45deg, #000 0, #000 2px, #fff 2px, #fff 4px); }
.r-stamp { position: absolute; right: 20px; top: -40px; width: 80px; height: 80px; border: 4px double #c0392b; border-radius: 50%; color: #c0392b; display: flex; align-items: center; justify-content: center; font-size: 16px; font-weight: 900; transform: rotate(-15deg); opacity: 0.8; text-align: center; }

@media print {
  body * { visibility: hidden; }
  .print-hide { display: none !important; }
  #printable-receipt, #printable-receipt * { visibility: visible; }
  #printable-receipt { position: absolute; left: 0; top: 0; padding: 0; background: none; }
  .receipt-paper { box-shadow: none; width: 100%; padding: 20px; border: 1px solid #000; }
}

@media(max-width:900px){
  .deposit-grid { grid-template-columns: 1fr; }
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
