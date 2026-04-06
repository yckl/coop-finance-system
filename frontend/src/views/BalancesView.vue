<template>
  <div class="module-page">
    <div class="module-topbar">
      <div>
        <div class="panel-title">系统储户底册与资金冻结台账</div>
        <div class="panel-subtitle">宏观调控与校验各储户账户级资产配比，支持反洗钱级别的资金盘点</div>
      </div>
      <div class="topbar-actions">
        <el-button @click="loadData">强制校验底层数据库</el-button>
      </div>
    </div>
    
    <el-card class="panel-card" shadow="never">
      <el-table :data="records" stripe style="width: 100%" size="large">
        <el-table-column prop="user_no" label="金融户口" width="120">
           <template #default="scope">
             <strong>{{ scope.row.user_no }}</strong>
           </template>
        </el-table-column>
        <el-table-column prop="account_no" label="存折/卡槽绑定号" width="180">
           <template #default="scope">
             <span style="font-family: monospace;">{{ scope.row.account_no }}</span>
           </template>
        </el-table-column>
        
        <el-table-column prop="total_balance" label="在网底仓权益 (总账 ¥)" min-width="150" align="right">
           <template #default="scope">
             <strong style="color: #2980b9; font-size: 16px;">{{ Number(scope.row.total_balance).toFixed(2) }}</strong>
           </template>
        </el-table-column>
        <el-table-column prop="available_balance" label="自由可用下水 (¥)" min-width="150" align="right">
           <template #default="scope">
             <strong style="color: #27ae60; font-size: 16px;">{{ Number(scope.row.available_balance).toFixed(2) }}</strong>
           </template>
        </el-table-column>
        <el-table-column prop="frozen_balance" label="司法冻结暂扣 (¥)" min-width="140" align="right">
           <template #default="scope">
             <span style="color: #e74c3c;">{{ Number(scope.row.frozen_balance).toFixed(2) }}</span>
           </template>
        </el-table-column>

        <el-table-column prop="account_status" label="管控锚点" width="120" align="center">
           <template #default="scope">
             <el-tag :type="scope.row.account_status === '正常' ? 'success' : 'danger'" effect="dark">{{ scope.row.account_status }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="risk_level" label="反洗钱定档" width="120" align="center">
           <template #default="scope">
             <el-tag :type="scope.row.risk_level === '低' ? 'info' : (scope.row.risk_level === '中' ? 'warning' : 'danger')" effect="plain">{{ scope.row.risk_level }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="updated_at" label="上次资产变更有感" min-width="160" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api'

const route = useRoute()
const role = route.meta.role || 'finance'
const records = ref([])

async function loadData() {
  try {
    const res = await http.get(`/${role}/module/balances`)
    records.value = res.data.records || []
  } catch (e) {
    ElMessage.error('获取底层资产底册失连')
  }
}

onMounted(loadData)
</script>

<style scoped>
.panel-card { border-radius: 8px; }
</style>
