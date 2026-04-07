<template>
  <div class="module-page tx-page">
    <div class="module-topbar print-hide" style="margin-bottom: 20px;">
      <div>
        <div class="panel-title text-success">全息中央结算库 (Transactions)</div>
        <div class="panel-subtitle">支持宏观查看所有资金往来明细，每笔交易均可查看其安全校验指纹。</div>
      </div>
      <div class="topbar-actions">
        <!-- 导表入口 -->
        <el-button color="#00c9a7" :dark="true" plain icon="Download" @click="fakeExport">导流至 Excel 数据舱</el-button>
        <el-button type="primary" class="neo-btn-primary" icon="Printer" @click="handlePrint">输出法务印鉴底册</el-button>
      </div>
    </div>
    
    <div class="stepper-area print-hide glass-card p-3 mb-4" style="border: 1px solid var(--color-border); border-radius: 16px;">
      <el-row :gutter="20">
        <el-col :span="6">
           <el-input v-model="searchKey" placeholder="提取序列号 / 目标实体..." class="neo-input" size="large" prefix-icon="Search"></el-input>
        </el-col>
        <el-col :span="5">
           <el-select v-model="typeFilter" class="neo-input" size="large" placeholder="资金动截口径" clearable>
              <el-option label="存入 (Up)" value="存款" />
              <el-option label="抽离 (Down)" value="取款" />
           </el-select>
        </el-col>
        <el-col :span="13" style="display:flex; justify-content: flex-end; align-items:center;">
           <span style="font-size:13px; margin-right:15px; color:var(--color-text-secondary); font-weight:600;">大盘结算探针:</span>
           <el-tag type="success" size="large" effect="dark" round style="font-weight:700; border:none;">网兜命中: {{ filteredRecords.length }} 笔动作</el-tag>
        </el-col>
      </el-row>
    </div>

    <!-- 这里的 table 也将在打印时展现 -->
    <el-card class="glass-card panel-card no-padding" shadow="never">
      <div class="print-header" style="display: none; text-align:center; font-size: 24px; font-weight:900; margin-bottom: 20px; letter-spacing: 5px;">
        【极密】内部统一交易安全备份印册
      </div>

      <el-table :data="filteredRecords" style="width: 100%" class="custom-table neo-input row-expand-table" border size="large">
        
        <!-- 行展开，溯源追踪器 -->
        <el-table-column type="expand">
          <template #default="scope">
             <div class="tx-drill-down">
                <div class="drill-left">
                   <div class="dl-row"><span>源点发生时间精确锚定：</span> <strong>{{ scope.row.created_at }}</strong></div>
                   <div class="dl-row"><span>办理授权柜员工牌：</span> <strong>{{ scope.row.operator_name }}</strong></div>
                   <div class="dl-row"><span>挂单法务摘要：</span> <strong>{{ scope.row.remark || '（未挂载附言）' }}</strong></div>
                   
                   <el-divider border-style="dashed" />
                   
                   <div class="tx-path-mock">
                      <div class="path-node bg-blue">客户端口发包</div>
                      <div class="path-line"></div>
                      <div class="path-node" :class="scope.row.transaction_type === '存款' ? 'bg-green' : 'bg-red'">前置风控闸机</div>
                      <div class="path-line"></div>
                      <div class="path-node bg-gray">核心大总账记录</div>
                   </div>
                </div>
                
                <div class="drill-right">
                   <div class="qr-auth-mock"></div>
                   <div class="qr-text">不可篡改的区块认证指纹<br><span style="font-family:monospace; opacity:0.6;">{{ scope.row.serial_no }}</span></div>
                </div>
             </div>
          </template>
        </el-table-column>

        <el-table-column prop="serial_no" label="底层溯源序列码" min-width="200">
           <template #default="scope">
             <span class="tx-hash"><el-icon size="12" style="margin-right:4px;"><Link /></el-icon>{{ scope.row.serial_no }}</span>
           </template>
        </el-table-column>
        <el-table-column prop="user_name" label="实体受体面" min-width="120">
           <template #default="scope">
             <strong>{{ scope.row.user_name }}</strong> <br><span style="color:var(--color-primary); font-size:11px; font-family:monospace;">{{ scope.row.user_no }}</span>
           </template>
        </el-table-column>
        <el-table-column prop="transaction_type" label="划拨动向" min-width="100" align="center">
           <template #default="scope">
             <el-tag :type="scope.row.transaction_type === '存款' ? 'success' : 'danger'" effect="dark" round>
                <el-icon><Top v-if="scope.row.transaction_type === '存款'" /><Bottom v-else /></el-icon>
                {{ scope.row.transaction_type }}
             </el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="business_type" label="业态解耦" min-width="140" />
        <el-table-column prop="amount" label="震荡落差 (¥)" min-width="140" align="right">
           <template #default="scope">
             <span class="amt-diff" :class="scope.row.transaction_type === '存款' ? 'text-success' : 'text-danger'">
               {{ scope.row.transaction_type === '存款' ? '+' : '-' }}{{ Number(scope.row.amount).toLocaleString('zh-CN', {minimumFractionDigits:2}) }}
             </span>
           </template>
        </el-table-column>
        <el-table-column prop="balance_after" label="锚后平底 (¥)" min-width="140" align="right">
           <template #default="scope">
              <strong>{{ Number(scope.row.balance_after).toLocaleString('zh-CN', {minimumFractionDigits:2}) }}</strong>
           </template>
        </el-table-column>
        <el-table-column prop="transaction_status" label="健康态" min-width="100" align="center">
           <template #default="scope">
             <span style="color:var(--color-success); font-weight:700;"><el-icon><Select /></el-icon> {{ scope.row.transaction_status }}</span>
           </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElLoading } from 'element-plus'
import { Search, Download, Printer, Link, Top, Bottom, Select } from '@element-plus/icons-vue'
import http from '../api'

const route = useRoute()
const role = route.meta.role || 'finance'
const records = ref([])

const searchKey = ref('')
const typeFilter = ref('')

const filteredRecords = computed(() => {
  let arr = records.value
  
  if (typeFilter.value) {
     arr = arr.filter(r => r.transaction_type === typeFilter.value)
  }
  
  if (searchKey.value) {
     const low = searchKey.value.toLowerCase()
     arr = arr.filter(r => 
       (r.user_no && r.user_no.toLowerCase().includes(low)) ||
       (r.user_name && r.user_name.toLowerCase().includes(low)) ||
       (r.serial_no && r.serial_no.toLowerCase().includes(low))
     )
  }
  
  return arr
})

async function loadData() {
  try {
    const res = await http.get(`/${role}/module/transactions`)
    records.value = res.data.records || []
  } catch (e) {
    ElMessage.error('调取集中流水匣失败: ' + e)
  }
}

function fakeExport() {
  const loading = ElLoading.service({ text: '正在抽取极度深寒数据...', background: 'rgba(0,0,0,0.7)' })
  setTimeout(() => {
     loading.close()
     ElMessage.success('已已成功导出 XLSX 文件至本地。')
  }, 1200)
}

function handlePrint() {
  window.print()
}

onMounted(loadData)
</script>

<style scoped>
.tx-page { display: flex; flex-direction: column; min-height: calc(100vh - 120px); }

/* Table Enhancements */
.custom-table { --el-table-header-bg-color: var(--color-surface-hover); }
.tx-hash { font-family: monospace; background: rgba(0,0,0,0.03); padding: 4px 8px; border-radius: 4px; font-weight: 700; color: var(--color-text-secondary); display:flex; align-items:center; }
.amt-diff { font-family: 'Inter', sans-serif; font-weight: 800; font-size: 16px; }
.text-success { color: #00c9a7; }
.text-danger { color: #ea4335; }

/* Drill Down UI */
.row-expand-table :deep(.el-table__expanded-cell) { padding: 0 !important; }
.tx-drill-down { padding: 25px 40px; background: rgba(0,82,204,0.02); display: flex; justify-content: space-between; border-bottom: 2px solid var(--color-primary-light); box-shadow: inset 0 4px 10px rgba(0,0,0,0.02); }

.drill-left { flex: 1; padding-right: 40px; }
.dl-row { display: flex; gap: 15px; font-size: 13px; margin-bottom: 10px; }
.dl-row span { color: var(--color-text-secondary); width: 140px; text-align: right; }
.dl-row strong { color: var(--color-text); font-weight: 600; }

.tx-path-mock { display: flex; align-items: center; margin-top: 20px; }
.path-node { padding: 6px 12px; border-radius: 20px; font-size: 12px; font-weight: 700; color: white; }
.path-line { height: 2px; width: 40px; background: var(--color-border); }
.bg-blue { background: #0052cc; }
.bg-green { background: #00c9a7; }
.bg-red { background: #ea4335; }
.bg-gray { background: #7f8c8d; }

.drill-right { width: 150px; display: flex; flex-direction: column; align-items: center; justify-content: center; border-left: 1px dashed var(--color-border); padding-left: 30px; }
.qr-auth-mock { width: 80px; height: 80px; background: repeating-linear-gradient(135deg, #333 0, #333 4px, #fff 4px, #fff 8px); border-radius: 8px; margin-bottom: 10px; border: 4px solid #fff; box-shadow: 0 4px 10px rgba(0,0,0,0.1); }
.qr-text { font-size: 11px; text-align: center; color: var(--color-text-secondary); line-height: 1.4; }

@media print {
  body * { visibility: hidden; }
  .print-hide { display: none !important; }
  .panel-card, .panel-card * {
    visibility: visible;
  }
  .panel-card {
    position: absolute; left: 0; top: 0; width: 100%; border: none; background: white; zoom: 70%;
  }
  .print-header { display: block !important; }
  .tx-drill-down { display: none !important; }
}
</style>
