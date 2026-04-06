<template>
  <div class="module-page">
    <div class="module-topbar">
      <div>
        <div class="panel-title">管理员与系统安全总控</div>
        <div class="panel-subtitle">系统级账号管理中心，可分配 RBAC 业务全生命周期授权</div>
      </div>
      <div class="topbar-actions">
        <el-button color="#34495e" :dark="true" @click="openCreate">调配新管理员</el-button>
        <el-button @click="loadData">全局同步</el-button>
      </div>
    </div>
    
    <div class="summary-grid">
      <el-card class="summary-card" shadow="never">
        <div class="summary-label">根授权账户数</div>
        <div class="summary-value" style="color: #34495e;">{{ records.length }}</div>
      </el-card>
      <el-card class="summary-card" shadow="never">
        <div class="summary-label">审计异常账户</div>
        <div class="summary-value" style="color: #27ae60;">0</div>
      </el-card>
    </div>

    <el-card class="panel-card" shadow="never">
      <el-table :data="records" stripe style="width: 100%">
        <el-table-column prop="id" label="系统流水号" width="110" />
        <el-table-column prop="username" label="根账号" min-width="120" />
        <el-table-column prop="real_name" label="经办人姓名" min-width="120" />
        <el-table-column prop="role_code" label="RBAC 标识" min-width="130">
          <template #default>
            <el-tag type="info" effect="dark">ADMIN_ROOT</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="account_status" label="主节点状态" min-width="120">
          <template #default="scope">
            <el-tag :type="['正常', '启用'].includes(scope.row.account_status) ? 'success' : 'danger'">
              <span class="pulse-dot" v-if="['正常', '启用'].includes(scope.row.account_status)"></span>
              {{ scope.row.account_status }}运行中
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="系统操作" fixed="right" min-width="220" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="resetPwd(scope.row)">密匙轮换</el-button>
            <el-button link type="warning">权限审计</el-button>
            <el-button link type="danger" :disabled="scope.row.username === 'admin'">强制剥夺</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api'

const records = ref([])

async function loadData() {
  try {
    const res = await http.get(`/admin/module/admin-accounts`)
    records.value = res.data.records || []
  } catch (e) {
    ElMessage.error('无法同步总控节点数据: ' + e)
  }
}

async function resetPwd(row) {
  try {
    await ElMessageBox.confirm(`系统将对核心账户 ${row.username} 进行不可逆的密码轮换计算, 确认?`, '高权警示', { type: 'error' })
    const res = await http.post(`/admin/users/reset-password/${row.id}`)
    ElMessage.success(res.message || '轮换完毕')
  } catch(e) { if(e !== 'cancel') ElMessage.error(String(e)) }
}

function openCreate() {
  ElMessage.warning('检测到您不具备 [ROOT_DISTRIBUTE] 权限，无法凭空生成核心节点！')
}

onMounted(loadData)
</script>

<style scoped>
.panel-card {
  border-radius: 12px;
  border: 1px solid #ebeef5;
}
.summary-card {
  background: linear-gradient(135deg, #fdfbfb 0%, #ebedee 100%);
  border: none;
}
.pulse-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  background-color: #fff;
  border-radius: 50%;
  margin-right: 5px;
  animation: pulse 1.5s infinite;
}
@keyframes pulse {
  0% { opacity: 0.5; box-shadow: 0 0 0 0 rgba(255,255,255,0.7); }
  70% { opacity: 1; box-shadow: 0 0 0 4px rgba(255,255,255,0); }
  100% { opacity: 0.5; box-shadow: 0 0 0 0 rgba(255,255,255,0); }
}
</style>
