<template>
  <div class="module-page admin-dict-layout">
    <div class="module-topbar" style="margin-bottom: 20px;">
      <div>
        <div class="panel-title">基础字典库与系统魔方阵</div>
        <div class="panel-subtitle">此高危区域维护系统一切下拉列表和全局变量值，非开发期请勿胡乱更改。</div>
      </div>
      <div class="topbar-actions">
        <el-button type="primary" class="neo-btn-primary" icon="Upload">持久化下发集群</el-button>
        <el-button plain icon="Refresh" @click="loadData">反向拉取</el-button>
      </div>
    </div>

    <!-- Parameter Config Cards -->
    <div class="sys-params-grid mb-4">
       <el-card class="glass-card param-card" shadow="never">
          <div class="param-header">
             <strong>夜间清算开关</strong>
             <el-switch v-model="sysParams.nightClear" active-color="#00c9a7" inactive-color="#ff4949" />
          </div>
          <p class="param-desc">关闭后，跨行调拨系统会在每日凌晨 2:00-4:00 进行强停结账。</p>
       </el-card>

       <el-card class="glass-card param-card" shadow="never">
          <div class="param-header">
             <strong>AI 侦听大模型等级</strong>
             <el-rate v-model="sysParams.aiLevel" :colors="['#99A9BF', '#F7BA2A', '#0052cc']" />
          </div>
          <p class="param-desc">控制谛听引擎在系统聊天、智能摘要中调用的浮点算力上限。</p>
       </el-card>
       
       <el-card class="glass-card param-card" shadow="never">
          <div class="param-header">
             <strong>前端品牌基调覆盖</strong>
             <el-color-picker v-model="sysParams.brandColor" show-alpha />
          </div>
          <p class="param-desc">您可以选择新的信任主色系，强行覆盖所有前台网点的 CSS 变量。</p>
       </el-card>
    </div>

    <!-- Multi Tab Dictionary Editor -->
    <el-card class="glass-card panel-card" shadow="never" style="min-height: 500px;">
       <el-tabs v-model="activeTab" class="neo-tabs custom-dict-tabs" tab-position="left">
         
         <el-tab-pane v-for="(dict, dIndex) in records" :key="dIndex" :name="'dict_'+dIndex">
            <template #label>
              <div class="dict-tab-label">
                <el-icon><Menu /></el-icon> {{ dict.name }}
              </div>
            </template>
            
            <div class="dict-tab-content">
               <div class="dict-header">
                  <h3 class="tab-h3">{{ dict.name }}</h3>
                  <el-button type="primary" size="small" icon="Plus" plain @click="addItem(dict)">注入新词条</el-button>
               </div>
               
               <el-table :data="dict.items" style="width: 100%" class="custom-table neo-input mt-3" border>
                 <el-table-column width="60" align="center">
                    <template #default>
                       <el-icon class="drag-handle" color="#99a9bf"><Rank /></el-icon>
                    </template>
                 </el-table-column>
                 
                 <el-table-column label="键值映射 (Value)" min-width="150">
                    <template #default="scope">
                       <el-input v-model="scope.row.value" :disabled="scope.row.locked" class="dict-input" />
                    </template>
                 </el-table-column>
                 
                 <el-table-column label="多语言标签 (Label)" min-width="150">
                    <template #default="scope">
                       <el-input v-model="scope.row.label" class="dict-input" />
                    </template>
                 </el-table-column>
                 
                 <el-table-column label="底层锁定" width="100" align="center">
                    <template #default="scope">
                       <el-icon v-if="scope.row.locked" color="#ea4335"><Lock /></el-icon>
                       <el-icon v-else color="#00c9a7"><Unlock /></el-icon>
                    </template>
                 </el-table-column>
                 
                 <el-table-column label="擦除动作" min-width="100" align="center">
                    <template #default="scope">
                       <el-button link type="danger" icon="Delete" :disabled="scope.row.locked" @click="removeItem(dict, scope.$index)" />
                    </template>
                 </el-table-column>
               </el-table>
            </div>
         </el-tab-pane>

       </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Upload, Plus, Rank, Lock, Unlock, Delete, Menu } from '@element-plus/icons-vue'

const activeTab = ref('dict_0')

const sysParams = ref({
   nightClear: true,
   aiLevel: 4,
   brandColor: '#0052cc'
})

// MOCK detailed editable dictionary structures
const records = ref([
  {
    name: '金融存款险种库',
    items: [
      { value: 'LIVE_DEPO', label: '活期散储', locked: true },
      { value: 'FIXED_DEPO_3M', label: '三个月定期', locked: false },
      { value: 'AGRI_AID_DEPO', label: '农资专项互助金', locked: false }
    ]
  },
  {
    name: '审批节点网络库',
    items: [
      { value: 'APPROVE', label: '核签放款', locked: true },
      { value: 'REJECT', label: '凭证打回', locked: true },
      { value: 'WAIT_FOR_MORE', label: '要求补充发票', locked: false }
    ]
  },
  {
    name: '物理网点大盘表',
    items: [
      { value: 'BRANCH_01', label: '中心机要总站', locked: true },
      { value: 'BRANCH_02', label: '大学城营业支行', locked: false },
      { value: 'BRANCH_03', label: '东南高新科技所', locked: false }
    ]
  }
])

function loadData() {
  ElMessage.success('字典元数据重载成功。底层参数映射完成。')
}

function addItem(dict) {
  dict.items.push({ value: 'NEW_KEY', label: '新释义词', locked: false })
}

function removeItem(dict, idx) {
  dict.items.splice(idx, 1)
}

onMounted(loadData)
</script>

<style scoped>
.sys-params-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; }
.param-card { padding: 20px; }
.param-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.param-header strong { font-size: 15px; color: var(--color-text); font-weight: 700; }
.param-desc { margin: 0; font-size: 13px; color: var(--color-text-secondary); line-height: 1.5; }

.custom-dict-tabs :deep(.el-tabs__header) { margin-right: 20px; width: 220px; }
.custom-dict-tabs :deep(.el-tabs__item) { height: 50px; text-align: left; padding: 0 20px; justify-content: flex-start; margin-bottom: 5px; border-radius: 8px; }
.custom-dict-tabs :deep(.el-tabs__item.is-active) { background: rgba(0,82,204,0.1); color: var(--color-primary); font-weight: 700; }

.dict-tab-label { display: flex; align-items: center; gap: 10px; font-size: 14px; }
.dict-tab-content { padding: 10px; animation: fadeIn 0.4s; }

.dict-header { display: flex; justify-content: space-between; align-items: center; }
.tab-h3 { font-size: 18px; margin: 0; color: var(--color-text); font-weight: 700; }

.custom-table { border-radius: 12px; --el-table-header-bg-color: var(--color-surface-hover); }
.drag-handle { cursor: grab; font-size: 18px; }
.drag-handle:active { cursor: grabbing; }

.dict-input :deep(.el-input__wrapper) { background: transparent !important; box-shadow: none !important; border-bottom: 1px dashed var(--color-border) !important; border-radius: 0; padding: 0; }
.dict-input :deep(.el-input__wrapper.is-focus) { box-shadow: none !important; border-bottom: 1px solid var(--color-primary) !important; }
.dict-input :deep(.el-input__inner) { text-align: center; color: var(--color-text); font-weight: 600; }

@keyframes fadeIn { from { opacity:0; transform:translateY(10px); } to { opacity:1; transform:translateY(0); } }
</style>
