<template>
  <div class="transaction-page">
    <el-row :gutter="20">
      <el-col :xs="24" :lg="14">
        <el-card class="panel-card" shadow="hover">
          <template #header><div class="panel-title">{{ panelTitle }}</div></template>
          <el-form :model="form" label-position="top">
            <el-form-item label="用户编号">
              <el-input v-model="form.userNo" placeholder="请输入用户编号，如 U1001">
                <template #append>
                  <el-button @click="queryAccount">检索用户</el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="业务类型">
              <el-select v-model="form.type" class="full-width">
                <el-option label="定期存款" value="定期存款" v-if="kind === 'deposit'" />
                <el-option label="活期存款" value="活期存款" v-if="kind === 'deposit'" />
                <el-option label="现金取款" value="现金取款" v-if="kind === 'withdraw'" />
                <el-option label="转账取款" value="转账取款" v-if="kind === 'withdraw'" />
              </el-select>
            </el-form-item>
            <el-form-item label="交易金额">
              <el-input-number v-model="form.amount" :min="1" :precision="2" class="full-width" />
            </el-form-item>
            <el-form-item label="经办人">
              <el-input v-model="form.operator" />
            </el-form-item>
            <el-form-item label="备注说明">
              <el-input v-model="form.remark" type="textarea" :rows="4" />
            </el-form-item>
            <div class="topbar-actions">
              <el-button type="primary" @click="submit">提交办理</el-button>
              <el-button @click="exportModule">导出凭证</el-button>
            </div>
          </el-form>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10">
        <el-card class="panel-card" shadow="hover">
          <template #header><div class="panel-title">用户信息与余额预览</div></template>
          <div v-if="account.accountNo" class="account-panel">
            <div class="account-row"><span>账户编号</span><strong>{{ account.accountNo }}</strong></div>
            <div class="account-row"><span>用户姓名</span><strong>{{ account.name }}</strong></div>
            <div class="account-row"><span>账户状态</span><strong>{{ account.status }}</strong></div>
            <div class="account-row"><span>风险等级</span><strong>{{ account.riskLevel }}</strong></div>
            <div class="account-row"><span>当前余额</span><strong>¥ {{ account.balance }}</strong></div>
            <div class="account-row"><span>可用余额</span><strong>¥ {{ account.availableBalance }}</strong></div>
            <el-alert v-if="kind === 'withdraw' && Number(form.amount || 0) > Number(account.balance || 0)" type="error" show-icon title="余额不足拦截，当前金额超过账户可用余额" />
            <el-alert v-else-if="Number(form.amount || 0) >= 50000" type="warning" show-icon title="大额预警：建议主管复核并留痕" />
          </div>
          <div v-else class="dialog-empty">请先输入用户编号并检索账户</div>
        </el-card>
        <el-card class="panel-card" shadow="hover">
          <template #header><div class="panel-title">最近 5 笔交易</div></template>
          <div v-if="account.recentTransactions?.length">
            <div v-for="item in account.recentTransactions" :key="item.serialNo" class="simple-list-item">
              <strong>{{ item.type }} {{ item.amount }} 元</strong>
              <span>{{ item.time }}</span>
            </div>
          </div>
          <div v-else class="dialog-empty">暂无交易记录</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api'

const route = useRoute()
const kind = route.meta.transactionKind
const panelTitle = kind === 'deposit' ? '存款办理页' : '取款办理页'
const account = ref({})

const form = reactive({
  userNo: 'U1001',
  amount: kind === 'deposit' ? 5000 : 1200,
  type: kind === 'deposit' ? '定期存款' : '现金取款',
  operator: '张会计',
  remark: kind === 'deposit' ? '春耕补贴入账' : '农资采购支出'
})

async function queryAccount() {
  try {
    const result = await http.get(`/finance/account/${form.userNo}`)
    account.value = result.data
  } catch (error) {
    ElMessage.error(String(error))
  }
}

async function submit() {
  try {
    const result = await http.post(`/finance/transaction/${kind}`, form)
    ElMessage.success(`${result.data.message}，流水号：${result.data.serialNo}`)
    queryAccount()
  } catch (error) {
    ElMessage.error(String(error))
  }
}

async function exportModule() {
  try {
    const target = kind === 'deposit' ? 'transactions' : 'transactions'
    const result = await http.get(`/finance/export/${target}`)
    ElMessage.success(result.data.message)
  } catch (error) {
    ElMessage.error(String(error))
  }
}

queryAccount()
</script>
