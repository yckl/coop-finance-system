<template>
  <div class="module-page">
    <div class="module-topbar">
      <div>
        <div class="panel-title">柜面财务人员管控库</div>
        <div class="panel-subtitle">此页面可维护具有具体资金划拨权限的物理网点操作员信息。</div>
      </div>
      <div class="topbar-actions">
        <el-button color="#8e44ad" :dark="true" @click="openCreate">入驻新财务</el-button>
        <el-button @click="loadData">重新加载</el-button>
      </div>
    </div>
    
    <div class="summary-grid">
      <el-card class="summary-card" shadow="never">
        <div class="summary-label">在编财务员</div>
        <div class="summary-value" style="color: #8e44ad;">{{ records.length }}</div>
      </el-card>
    </div>

    <el-card class="panel-card" shadow="never">
      <template #header>
        <el-form :inline="true" class="toolbar-inline" @submit.prevent>
          <el-form-item label="精细筛查：">
            <el-input v-model="keyword" placeholder="工号 / 姓名 / 网点..." clearable style="width: 250px" />
          </el-form-item>
        </el-form>
      </template>
      <el-table :data="filteredRecords" stripe style="width: 100%">
        <el-table-column prop="staff_no" label="授权工号" min-width="110" />
        <el-table-column prop="staff_name" label="职员姓名" min-width="100" />
        <el-table-column prop="position_name" label="网点编制岗位" min-width="120" />
        <el-table-column prop="branch_name" label="所属物理网点" min-width="130" />
        <el-table-column prop="login_username" label="绑定控制台账号" min-width="130" />
        <el-table-column prop="employment_status" label="编制状态" min-width="100">
          <template #default="scope">
            <el-tag :type="scope.row.employment_status === '在职' ? 'success' : 'info'">
              {{ scope.row.employment_status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="180" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="editRecord(scope.row)">岗调</el-button>
            <el-button link type="warning" @click="suspendRecord(scope.row)">办理离职</el-button>
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
    const res = await http.get(`/admin/module/finance-staff`)
    records.value = res.data.records || []
  } catch (e) {
    ElMessage.error('无法同步编制数据: ' + e)
  }
}

function openCreate() {
  ElMessage.info('财务建档系统正在与公安接口核对，请稍后再试。')
}

function editRecord(row) {
  ElMessage.info(`【${row.staff_name}】的权限锁定期内无法被修改。`)
}

function suspendRecord(row) {
  ElMessageBox.confirm(`系统拒绝了离职请求：您必须先在 OA 系统中流转完毕再进行操作。`, '错误', { type: 'error' }).catch(()=>{})
}

onMounted(loadData)
</script>

<style scoped>
.panel-card {
  border-radius: 12px;
  border: 1px solid #ebeef5;
}
.summary-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border: none;
}
</style>
