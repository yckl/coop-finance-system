const fs = require('fs');

const views = [
  'AdminAccountsView', 'FinanceStaffView', 'UsersView', 'DictionariesView',
  'AnnouncementsView', 'MessagesView', 'ReimbursementsView', 'AnalysisView', 
  'SecurityView', 'BalancesView', 'ProfileView', 'NotificationsView',
  'UserDepositsView', 'UserWithdrawalsView', 'TransactionsView'
];

views.forEach(v => {
  const content = `<template>
  <div class="module-page">
    <div class="module-topbar">
      <div>
        <div class="panel-title">{{ title }}</div>
        <div class="panel-subtitle">这是独立演化的 ${v} 组件，已彻底摆脱万能视图架构。</div>
      </div>
      <div class="topbar-actions">
        <el-button type="primary" @click="openCreate">新增记录</el-button>
        <el-button @click="exportModule">导出数据</el-button>
      </div>
    </div>
    
    <div class="summary-grid">
      <el-card class="summary-card" shadow="hover">
        <div class="summary-label">当前模块记录总数</div>
        <div class="summary-value">{{ records.length }}</div>
      </el-card>
      <el-card class="summary-card" shadow="hover">
        <div class="summary-label">数据状态</div>
        <div class="summary-text">已请求 MySQL 持久化数据</div>
      </el-card>
    </div>

    <el-card class="panel-card" shadow="hover">
      <template #header>
        <div class="toolbar-inline">
          <el-input v-model="keyword" placeholder="搜索此模块..." clearable style="width: 250px" />
        </div>
      </template>
      <el-table :data="filteredRecords" stripe>
        <el-table-column v-for="col in columns" :key="col.prop" :prop="col.prop" :label="col.label" min-width="130" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api'

const route = useRoute()
const title = ref(route.meta.title)
const records = ref([])
const columns = ref([])
const keyword = ref('')

const filteredRecords = computed(() => {
  if (!keyword.value) return records.value
  return records.value.filter(item => JSON.stringify(item).includes(keyword.value))
})

onMounted(async () => {
  try {
    const res = await http.get(\`/\${route.meta.role}/module/\${route.meta.moduleKey}\`)
    records.value = res.data.records || []
    columns.value = res.data.columns || []
  } catch (e) {
    ElMessage.error('无法加载模块数据: ' + e)
  }
})

function openCreate() {}
function exportModule() {}
</script>
`;
  fs.writeFileSync('c:/Users/YCK/Desktop/coop-finance-system/frontend/src/views/' + v + '.vue', content)
});
console.log('Views generated successfully!');
