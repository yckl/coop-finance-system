<template>
  <div class="module-page deposit-page">
    <template v-if="role !== 'user'">
    <div class="module-topbar print-hide">
      <div>
        <div class="panel-title">柜面取款办理</div>
        <div class="panel-subtitle">严格遵循先核对人、后验定可用额度、最后完成实盘划拨的金融流程</div>
      </div>
      <div class="topbar-actions">
        <el-button type="success" plain :disabled="!stepCompleted" @click="handlePrint">打印出款凭证</el-button>
        <el-button @click="resetForm">重置网点办理流</el-button>
      </div>
    </div>
    
    <div class="stepper-area print-hide">
      <el-steps :active="currentStep" finish-status="success" simple>
        <el-step title="步骤 1: 查验取款人身份" />
        <el-step title="步骤 2: 核准资金出账" />
        <el-step title="步骤 3: 授权与出单" />
      </el-steps>
    </div>

    <!-- 步骤1 -->
    <el-card v-if="currentStep === 0" class="panel-card" shadow="never">
      <div class="search-user-form print-hide">
        <h3>检索要取现的目标账户</h3>
        <el-form :inline="true" @submit.prevent>
          <el-form-item label="核心客户号 (User No):">
            <el-input v-model="searchUserNo" placeholder="请输入核心号码 (如 U1001)" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchUser">检索并校验状态</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 步骤2 -->
    <el-card v-if="currentStep === 1" class="panel-card" shadow="never">
      <div class="account-info-box">
        <el-descriptions title="已锁定储户档案" :column="3" border>
          <el-descriptions-item label="客户姓名"><strong>{{ activeAccount.user_name }}</strong></el-descriptions-item>
          <el-descriptions-item label="账户编号">{{ activeAccount.account_no }}</el-descriptions-item>
          <el-descriptions-item label="安全管控">
            <el-tag :type="activeAccount.account_status === '正常' ? 'success' : 'danger'">{{ activeAccount.account_status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="可用出款额度 (¥)">
            <span style="color:#27ae60; font-weight:bold; font-size:18px;">{{ activeAccount.available_balance }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="transaction-form" style="margin-top: 30px;">
        <el-alert
          v-show="form.amount > 50000"
          type="error"
          show-icon
          :closable="false"
          style="max-width: 600px; margin-bottom: 20px;"
          title="系统高危操作预警：单笔大额取款触发复核拦截，系统将记录审计风险！"
        />

        <el-form :model="form" label-width="120px" style="max-width: 600px;">
          <el-form-item label="提现金额 (¥):">
            <el-input-number v-model="form.amount" :min="1" :precision="2" :step="100" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="出款通道:" required>
            <el-select v-model="form.type" placeholder="请选取">
              <el-option label="现金提款" value="现金取款" />
              <el-option label="对公户转账" value="对公转账" />
              <el-option label="跨行划拨" value="外调取款" />
            </el-select>
          </el-form-item>
          <el-form-item label="附言/备注:">
            <el-input v-model="form.remark" placeholder="非必填" />
          </el-form-item>
          <el-form-item>
            <el-button type="danger" size="large" @click="submitWithdraw" style="width: 200px;">扣减可用额度并出单</el-button>
            <el-button size="large" @click="currentStep = 0">取消退出</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 步骤3 -->
    <el-card v-if="currentStep >= 2" class="panel-card print-section" shadow="hover">
      <div class="receipt-container">
        <h2 class="receipt-title withdraw-title">农业信用合作社 - 款项出账回单</h2>
        <div class="receipt-header">
           <span>流转单号: {{ receiptData.serialNo }}</span>
           <span>签发行: 高新总柜</span>
        </div>
        
        <table class="receipt-table">
          <tr>
            <th>户主名鉴</th><td>{{ activeAccount.user_name }}</td>
            <th>核心流水归属</th><td>{{ receiptData.userNo }}</td>
          </tr>
          <tr>
            <th>事由类别</th><td>{{ receiptData.type }}</td>
            <th>节点状态</th><td><strong style="color: #e74c3c;">出款核准</strong></td>
          </tr>
          <tr>
            <th>提出金额 (¥)</th><td colspan="3" class="money-text">{{ Number(receiptData.amount).toFixed(2) }}</td>
          </tr>
          <tr>
            <th>剩余可用 (¥)</th><td>{{ receiptData.balanceAfter }}</td>
            <th>经办签批</th><td>{{ receiptData.operator || 'SYSTEM_TELLER' }}</td>
          </tr>
          <tr>
             <th>机器印鉴时间</th><td colspan="3">{{ new Date().toLocaleString() }}</td>
          </tr>
        </table>
        
        <div class="receipt-footer">
          防伪标识码有效，请妥善保管此交易凭证。<br>
          <span style="font-size:12px; color:#999;">*** COOP-FINANCE-WITHDRAWAL-TICKET ***</span>
        </div>
      </div>
    </el-card>
    </template>

    <template v-else>
      <div class="module-topbar print-hide">
        <div>
          <div class="panel-title">我的出款流水账</div>
          <div class="panel-subtitle">查考所有取现和外调支付动作与扣留状态</div>
        </div>
        <div class="topbar-actions">
          <el-button type="success" plain @click="loadUserData">刷新出款进度</el-button>
        </div>
      </div>
      <el-card class="panel-card" shadow="hover">
        <el-table :data="userData" stripe style="width: 100%">
          <el-table-column prop="serial_no" label="结算交易单号" />
          <el-table-column prop="amount" label="交易流出 (¥)">
            <template #default="scope"><span style="color:#e74c3c;font-weight:bold;">-{{ scope.row.amount }}</span></template>
          </el-table-column>
          <el-table-column prop="business_type" label="划拨事由" />
          <el-table-column prop="created_at" label="执行完成时间" />
          <el-table-column prop="warning_level" label="系统风控标记">
             <template #default="scope">
               <el-tag :type="scope.row.warning_level === '高' ? 'danger' : 'info'" effect="dark">{{ scope.row.warning_level || '正常' }}</el-tag>
             </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElLoading, ElMessageBox } from 'element-plus'
import http from '../api'

const route = useRoute()
const role = route.meta.role || 'finance'
const currentStep = ref(0)
const stepCompleted = ref(false)

const searchUserNo = ref('')
const activeAccount = ref({})
const form = ref({ amount: 100, type: '现金取款', remark: '' })
const receiptData = ref({})
const userData = ref([])

async function loadUserData() {
  if (role !== 'user') return
  try {
    const res = await http.get(`/user/module/withdrawals`)
    userData.value = res.data.records || []
  } catch (e) {
    ElMessage.error(String(e))
  }
}

onMounted(() => {
  if (role === 'user') loadUserData()
})

async function searchUser() {
  if (!searchUserNo.value) return ElMessage.warning('编号为空')
  const loading = ElLoading.service({ text: '查询档案中...' })
  try {
    const res = await http.get(`/${role}/transaction/query?userNo=${searchUserNo.value}`)
    activeAccount.value = res.data
    if(!activeAccount.value.user_name) activeAccount.value.user_name = "受方用户" 
    currentStep.value = 1
  } catch (e) {
    ElMessage.error(String(e))
  } finally {
    loading.close()
  }
}

async function submitWithdraw() {
  if (activeAccount.value.account_status !== '正常') {
    return ElMessage.error('触发银监拦截：账户异常！')
  }
  if (form.value.amount > activeAccount.value.available_balance) {
    return ElMessage.error(`您的提出金额 ${form.value.amount} 元明显超过了可用余额 ${activeAccount.value.available_balance} 元`)
  }

  if (form.value.amount > 50000) {
    try {
      await ElMessageBox.confirm('这笔操作高达 5 万以上，您将为此操作背书入风险监控库，确定放款吗？', '反洗钱与大额控制', { type: 'error' })
    } catch {
      return ElMessage.info('您中止了该笔危险操作')
    }
  }

  const loading = ElLoading.service({ text: '安全出账校验中...' })
  try {
    const payload = {
      userNo: activeAccount.value.user_no,
      amount: form.value.amount,
      type: form.value.type,
      operator: '出纳柜员-01',
      remark: form.value.remark
    }
    const res = await http.post(`/${role}/transaction/withdraw`, payload)
    
    receiptData.value = {
      ...payload,
      serialNo: res.data.serialNo || res.serialNo || ('TX-'+Date.now()),
      balanceAfter: res.data.balanceAfter || res.balanceAfter
    }
    
    currentStep.value = 2
    stepCompleted.value = true
    ElMessage.success('提现动作已固化，请引导储户签字')
  } catch(e) {
    ElMessage.error(String(e))
  } finally {
    loading.close()
  }
}

function handlePrint() {
  window.print()
}

function resetForm() {
  currentStep.value = 0
  stepCompleted.value = false
  searchUserNo.value = ''
  activeAccount.value = {}
  form.value = { amount: 100, type: '现金取款', remark: '' }
}
</script>

<style scoped>
.panel-card { border-radius: 8px; margin-bottom: 20px; }
.stepper-area { background: #fff; padding: 15px; margin-bottom: 15px; border-radius: 6px; }

/* 伪凭证渲染样式 */
.receipt-container {
  max-width: 800px; padding: 25px; margin: 0 auto;
  border: 2px solid #333; background: #fffaf0; position: relative;
}
.withdraw-title { color: #8e44ad !important; }
.receipt-title { text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 4px; margin-bottom: 20px; }
.receipt-header { display: flex; justify-content: space-between; font-size: 14px; margin-bottom: 10px; font-family: monospace; color: #555; }
.receipt-table { width: 100%; border-collapse: collapse; font-size: 15px; }
.receipt-table th, .receipt-table td { border: 1px solid #666; padding: 12px; text-align: left; }
.receipt-table th { background-color: #f2e3d5; width: 150px; }
.money-text { font-family: consolas, monospace; font-size: 20px; font-weight: bold; color: #c0392b; letter-spacing: 2px;}
.receipt-footer { margin-top: 20px; text-align: center; font-size: 13px; line-height: 1.8; border-top: 1px dashed #666; padding-top: 15px; }

@media print {
  body * { visibility: hidden; }
  .print-hide { display: none !important; }
  .receipt-container, .receipt-container * {
    visibility: visible;
  }
  .receipt-container {
    position: absolute; left: 0; top: 0; width: 100%; border: none; background: white;
  }
}
</style>
