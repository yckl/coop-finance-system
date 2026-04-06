<template>
  <div class="module-page">
    <div class="module-topbar">
      <div>
        <div class="panel-title">用户管理 / User Management</div>
        <div class="panel-subtitle">此页面受到系统保护，仅管理员可以维护系统中的普通访客和客户终端档案。</div>
      </div>
      <div class="topbar-actions">
        <el-button color="#2c3e50" :dark="true" @click="openCreate">新增系统用户</el-button>
        <el-button @click="loadData">强制刷新记录</el-button>
      </div>
    </div>
    
    <div class="summary-grid">
      <el-card class="summary-card" shadow="never">
        <div class="summary-label">登记在册用户总数</div>
        <div class="summary-value" style="color: #2c3e50;">{{ records.length }}</div>
      </el-card>
      <el-card class="summary-card" shadow="never">
        <div class="summary-label">冻结与高风险</div>
        <div class="summary-value" style="color: #e74c3c;">{{ records.filter(r => r.account_status !== '正常' && r.account_status !== '启用').length }}</div>
      </el-card>
    </div>

    <el-card class="panel-card" shadow="never">
      <template #header>
        <el-form :inline="true" class="toolbar-inline" @submit.prevent>
          <el-form-item label="模糊检索：">
            <el-input v-model="keyword" placeholder="姓名 / 账号 / 手机号" clearable style="width: 250px" />
          </el-form-item>
        </el-form>
      </template>
      <el-table :data="filteredRecords" stripe style="width: 100%">
        <el-table-column prop="id" label="系统ID" width="90" />
        <el-table-column prop="username" label="登录账号" min-width="120" />
        <el-table-column prop="real_name" label="身份姓名" min-width="120" />
        <el-table-column prop="phone" label="联系电话" min-width="130" />
        <el-table-column prop="account_status" label="账户状态" min-width="100">
          <template #default="scope">
            <el-tag :type="['正常', '启用'].includes(scope.row.account_status) ? 'success' : 'danger'" effect="dark">
              {{ scope.row.account_status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="risk_level" label="自动化风险评级" min-width="140">
          <template #default="scope">
            <div style="display: flex; align-items: center; gap: 5px;">
              <span :style="{ display:'inline-block', width:'8px', height:'8px', borderRadius:'50%', background: scope.row.risk_level === '低' ? '#2ecc71' : (scope.row.risk_level==='高' ? '#e74c3c' : '#f39c12') }"></span>
              {{ scope.row.risk_level }}风险
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="接入时间" min-width="160" />
        <el-table-column label="系统操作控制台" fixed="right" min-width="200" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="resetPwd(scope.row)">重置密锁</el-button>
            <el-button link type="warning" v-if="['正常', '启用'].includes(scope.row.account_status)" @click="freeze(scope.row)">紧急封停</el-button>
            <el-button link type="success" v-else @click="unfreeze(scope.row)">解除封停</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api'

const records = ref([])
const keyword = ref('')

const filteredRecords = computed(() => {
  if (!keyword.value) return records.value
  return records.value.filter(item => JSON.stringify(item).includes(keyword.value))
})

async function loadData() {
  try {
    const res = await http.get(`/admin/module/users`)
    records.value = res.data.records || []
  } catch (e) {
    ElMessage.error('通讯异常无法加载列表: ' + e)
  }
}

async function resetPwd(row) {
  try {
    await ElMessageBox.confirm(`此操作将强制重置用户 ${row.real_name} 的密码为 123456, 是否继续?`, '风险安全覆盖', { type: 'warning' })
    const res = await http.post(`/admin/users/reset-password/${row.id}`)
    ElMessage.success(res.message || '重设完毕')
  } catch(e) { if(e !== 'cancel') ElMessage.error(String(e)) }
}

async function freeze(row) {
  try {
    await ElMessageBox.confirm(`确定要冻结封停账户 [${row.real_name}] 吗？`, '警示', { type: 'warning' })
    const res = await http.post(`/admin/users/freeze/${row.id}`)
    ElMessage.success(res.message || '账户已封停')
    loadData()
  } catch(e) { if(e !== 'cancel') ElMessage.error(String(e)) }
}

async function unfreeze(row) {
  try {
    const res = await http.post(`/admin/users/unfreeze/${row.id}`)
    ElMessage.success(res.message || '强制解封成功')
    loadData()
  } catch(e) { ElMessage.error(String(e)) }
}

function openCreate() {
  ElMessage.warning('系统管理员无法直接通过前端自设用户数据流，该事务已被网点物理网禁拦截。')
}

onMounted(loadData)
</script>

<style scoped>
.panel-card {
  border-radius: 12px;
  border: 1px solid #ebeef5;
}
.summary-card {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border: none;
}
</style>
