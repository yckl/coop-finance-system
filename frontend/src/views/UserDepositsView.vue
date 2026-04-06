<template>
  <div class="module-page deposit-page">
    <template v-if="role !== 'user'">
    <div class="module-topbar print-hide">
      <div>
        <div class="panel-title">柜面存款办理</div>
        <div class="panel-subtitle">严格遵循先核对人、后验定可用额度、最后完成实盘划拨的金融流程</div>
      </div>
      <div class="topbar-actions">
        <!-- 打印只在特定阶段启用 -->
        <el-button type="success" plain :disabled="!stepCompleted" @click="handlePrint">打印入账凭证</el-button>
        <el-button @click="resetForm">重置网点办理流</el-button>
      </div>
    </div>
    
    <div class="stepper-area print-hide">
      <el-steps :active="currentStep" finish-status="success" simple>
        <el-step title="步骤 1: 查验储户身份" />
        <el-step title="步骤 2: 填充款项要素" />
        <el-step title="步骤 3: 授权与下账回执" />
      </el-steps>
    </div>

    <!-- 步骤1: 查验身份 -->
    <el-card v-if="currentStep === 0" class="panel-card" shadow="never">
      <div class="search-user-form print-hide">
        <h3>检索要存入金钱的目标账户</h3>
        <el-form :inline="true" @submit.prevent>
          <el-form-item label="核心客户号 (User No):">
            <el-input v-model="searchUserNo" placeholder="请输入核心号码 (如 U1001)" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchUser">校验并锁定账户</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 步骤2: 填充与办理 -->
    <el-card v-if="currentStep === 1" class="panel-card" shadow="never">
      <div class="account-info-box">
        <el-descriptions title="已锁定储户档案" :column="3" border>
          <el-descriptions-item label="客户姓名">{{ activeAccount.user_name }}</el-descriptions-item>
          <el-descriptions-item label="绑定卡号">{{ activeAccount.account_no }}</el-descriptions-item>
          <el-descriptions-item label="账户状态">
            <el-tag :type="activeAccount.account_status === '正常' ? 'success' : 'danger'">{{ activeAccount.account_status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="原可用余额 (¥)">
            <span style="color:#e67e22; font-weight:bold;">{{ activeAccount.available_balance }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="transaction-form" style="margin-top: 30px;">
        <el-form :model="form" label-width="120px" style="max-width: 600px;">
          <el-form-item label="存入金额 (¥):">
            <el-input-number v-model="form.amount" :min="1" :precision="2" :step="100" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="业务类别:" required>
            <el-select v-model="form.type" placeholder="请选取">
              <el-option label="春耕定向存款" value="春耕定向存款" />
              <el-option label="定期存款 (一年)" value="定期存款-单期" />
              <el-option label="活期现金存入" value="活期存入" />
            </el-select>
          </el-form-item>
          <el-form-item label="附言/备注:">
            <el-input v-model="form.remark" placeholder="非必填" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" @click="submitDeposit" style="width: 200px;">核对无误，开始上账</el-button>
            <el-button size="large" @click="currentStep = 0">取消锁定并返回</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 步骤3: 凭证生成 (此部分在打印时会独立呈现) -->
    <el-card v-if="currentStep >= 2" class="panel-card print-section" shadow="hover">
      <div class="receipt-container">
        <h2 class="receipt-title">农业信用合作社 - 款项入账回单</h2>
        <div class="receipt-header">
           <span>流水单号: {{ receiptData.serialNo }}</span>
           <span>交易分行: 中心柜台</span>
        </div>
        
        <table class="receipt-table">
          <tr>
            <th>户名</th><td>{{ activeAccount.user_name }}</td>
            <th>核心系统编号</th><td>{{ receiptData.userNo }}</td>
          </tr>
          <tr>
            <th>业务类型</th><td>{{ receiptData.type }}</td>
            <th>状态</th><td><strong style="color: #27ae60;">入账成功</strong></td>
          </tr>
          <tr>
            <th>入账金额 (¥)</th><td colspan="3" class="money-text">{{ Number(receiptData.amount).toFixed(2) }}</td>
          </tr>
          <tr>
            <th>结余 (¥)</th><td>{{ receiptData.balanceAfter }}</td>
            <th>经办柜员</th><td>{{ receiptData.operator || 'SYSTEM' }}</td>
          </tr>
          <tr>
             <th>机器时间</th><td colspan="3">{{ new Date().toLocaleString() }}</td>
          </tr>
        </table>
        
        <div class="receipt-footer">
          此凭证由系统自生成，作为登账物理留证有效。<br>
          <span style="font-size:12px; color:#999;">*** COOP-FINANCE-SYSTEM-AUTO-GENERATED ***</span>
        </div>
      </div>
    </el-card>
    </template>

    <template v-else>
      <div class="module-topbar print-hide">
        <div>
          <div class="panel-title">我的存款账单记录</div>
          <div class="panel-subtitle">查阅所有存入款项明细与入账凭单</div>
        </div>
        <div class="topbar-actions">
          <el-button type="success" plain @click="loadUserData">刷新明细</el-button>
        </div>
      </div>
      <el-card class="panel-card" shadow="hover">
        <el-table :data="userData" stripe style="width: 100%">
          <el-table-column prop="serial_no" label="业务流水号" />
          <el-table-column prop="amount" label="金额 (¥)">
            <template #default="scope"><span style="color:#27ae60;font-weight:bold;">+{{ scope.row.amount }}</span></template>
          </el-table-column>
          <el-table-column prop="business_type" label="款项类型" />
          <el-table-column prop="created_at" label="完成时间" />
          <el-table-column prop="operator_name" label="网点经办人" />
        </el-table>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElLoading } from 'element-plus'
import http from '../api'

const route = useRoute()
const role = route.meta.role || 'finance'
const currentStep = ref(0)
const stepCompleted = ref(false)

const searchUserNo = ref('')
const activeAccount = ref({})
const form = ref({ amount: 100, type: '活期存入', remark: '' })
const receiptData = ref({})
const userData = ref([])

async function loadUserData() {
  if (role !== 'user') return
  try {
    const res = await http.get(`/user/module/deposits`)
    userData.value = res.data.records || []
  } catch (e) {
    ElMessage.error(String(e))
  }
}

onMounted(() => {
  if (role === 'user') {
    loadUserData()
  }
})

async function searchUser() {
  if (!searchUserNo.value) return ElMessage.warning('请输入待查询储户编号')
  const loading = ElLoading.service({ text: '通讯网关请求中...', target: '.deposit-page' })
  try {
    const res = await http.get(`/${role}/transaction/query?userNo=${searchUserNo.value}`)
    activeAccount.value = res.data
    // Mock user_name if query endpoint returns bare fields
    if(!activeAccount.value.user_name) activeAccount.value.user_name = "该用户" 
    currentStep.value = 1
  } catch (e) {
    ElMessage.error(String(e))
  } finally {
    loading.close()
  }
}

async function submitDeposit() {
  if (activeAccount.value.account_status !== '正常') {
    return ElMessage.error('该账户已被锁定，系统拒绝注资！')
  }
  const loading = ElLoading.service({ text: '正在调取核心上账程序...', target: '.deposit-page' })
  try {
    const payload = {
      userNo: activeAccount.value.user_no,
      amount: form.value.amount,
      type: form.value.type,
      operator: '当前柜台人员',
      remark: form.value.remark
    }
    const res = await http.post(`/${role}/transaction/deposit`, payload)
    
    // 生成凭证数据
    receiptData.value = {
      ...payload,
      serialNo: res.data.serialNo || res.serialNo || ('TX-'+Date.now()),
      balanceAfter: res.data.balanceAfter || res.balanceAfter
    }
    
    currentStep.value = 2
    stepCompleted.value = true
    ElMessage.success(res.message || '上账完成，请打印电子回单')
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
  form.value = { amount: 100, type: '活期存入', remark: '' }
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
.receipt-title { text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 4px; margin-bottom: 20px; color: #b71c1c; }
.receipt-header { display: flex; justify-content: space-between; font-size: 14px; margin-bottom: 10px; font-family: monospace; color: #555; }
.receipt-table { width: 100%; border-collapse: collapse; font-size: 15px; }
.receipt-table th, .receipt-table td { border: 1px solid #666; padding: 12px; text-align: left; }
.receipt-table th { background-color: #f2e3d5; width: 150px; }
.money-text { font-family: consolas, monospace; font-size: 20px; font-weight: bold; color: #c0392b; letter-spacing: 2px;}
.receipt-footer { margin-top: 20px; text-align: center; font-size: 13px; line-height: 1.8; border-top: 1px dashed #666; padding-top: 15px; }

/* 打印专供样式 */
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
