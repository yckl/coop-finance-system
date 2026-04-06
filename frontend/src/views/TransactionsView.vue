<template>
  <div class="module-page">
    <div class="module-topbar print-hide">
      <div>
        <div class="panel-title">统一网点流水检索机</div>
        <div class="panel-subtitle">此乃财务人员高频流转口，汇聚所有渠道的入金和出息动向，一目了然验证账目的发生额</div>
      </div>
      <div class="topbar-actions">
        <!-- 导表入口 -->
        <el-button type="success" @click="handlePrint">导出/打印月度合集流水卷宗</el-button>
        <el-button @click="loadData">强制追查最新账动</el-button>
      </div>
    </div>
    
    <div class="stepper-area print-hide" style="background:#fff; border-radius:8px; padding:15px; margin-bottom: 20px;">
      <el-row :gutter="20">
        <el-col :span="6">
           <el-input v-model="searchKey" placeholder="按核心号或户名模糊狙击..." prefix-icon="Search"></el-input>
        </el-col>
        <el-col :span="18" style="display:flex; justify-content: flex-end; align-items:center;">
           <span style="font-size:14px; margin-right:15px; color:#555;">财务总管统计:</span>
           <el-tag type="success" size="large" effect="plain" style="margin-right:10px;">检索总笔数: {{ filteredRecords.length }}</el-tag>
        </el-col>
      </el-row>
    </div>

    <!-- 这里的 table 也将在打印时展现 -->
    <el-card class="panel-card" shadow="never">
      <div class="print-header" style="display: none; text-align:center; font-size: 20px; font-weight:bold; margin-bottom: 20px;">
        内部统一交易流水备份印册 (打印件)
      </div>

      <el-table :data="filteredRecords" stripe border style="width: 100%" size="small">
        <el-table-column prop="serial_no" label="底层流水编码（串）" min-width="160">
           <template #default="scope">
             <span style="font-family: monospace;">{{ scope.row.serial_no }}</span>
           </template>
        </el-table-column>
        <el-table-column prop="user_name" label="业务申兑户" min-width="100">
           <template #default="scope">
             <strong>{{ scope.row.user_name }}</strong> <br><span style="color:#777; font-size:12px;">{{ scope.row.user_no }}</span>
           </template>
        </el-table-column>
        <el-table-column prop="transaction_type" label="划拨动向" min-width="90">
           <template #default="scope">
             <el-tag :type="scope.row.transaction_type === '存款' ? 'success' : 'danger'" effect="dark">{{ scope.row.transaction_type }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="business_type" label="业态释义" min-width="120" />
        <el-table-column prop="amount" label="发生变动额 (¥)" min-width="130" align="right">
           <template #default="scope">
             <span :style="{ fontWeight:'bold', fontSize:'15px', color: scope.row.transaction_type==='存款' ? '#27ae60' : '#c0392b' }">
               {{ scope.row.transaction_type === '存款' ? '+' : '-' }}{{ Number(scope.row.amount).toFixed(2) }}
             </span>
           </template>
        </el-table-column>
        <el-table-column prop="balance_after" label="即期结余 (¥)" min-width="120" align="right" />
        <el-table-column prop="transaction_status" label="状态" min-width="80">
           <template #default="scope">
             <span style="color:#2ecc71"><el-icon><Select /></el-icon> {{ scope.row.transaction_status }}</span>
           </template>
        </el-table-column>
        <el-table-column prop="operator_name" label="执行柜员" min-width="100" />
        <el-table-column prop="created_at" label="锚入时间" min-width="160" />
        <el-table-column prop="remark" label="挂单摘要" min-width="150" class-name="print-hide" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api'

const route = useRoute()
const role = route.meta.role || 'finance'
const records = ref([])
const searchKey = ref('')

const filteredRecords = computed(() => {
  if (!searchKey.value) return records.value
  const low = searchKey.value.toLowerCase()
  return records.value.filter(r => 
    (r.user_no && r.user_no.toLowerCase().includes(low)) ||
    (r.user_name && r.user_name.toLowerCase().includes(low)) ||
    (r.serial_no && r.serial_no.toLowerCase().includes(low))
  )
})

async function loadData() {
  try {
    const res = await http.get(`/${role}/module/transactions`)
    records.value = res.data.records || []
  } catch (e) {
    ElMessage.error('调取集中流水匣失败')
  }
}

function handlePrint() {
  window.print()
}

onMounted(loadData)
</script>

<style scoped>
.panel-card { border-radius: 8px; }

@media print {
  body * { visibility: hidden; }
  .print-hide { display: none !important; }
  .panel-card, .panel-card * {
    visibility: visible;
  }
  .panel-card {
    position: absolute; left: 0; top: 0; width: 100%; border: none; background: white; zoom: 80%;
  }
  .print-header { display: block !important; }
}
</style>
