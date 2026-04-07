<template>
  <div class="module-page admin-rbac-layout">
    <!-- Left: Role Tree Layout -->
    <div class="col-left">
       <el-card class="glass-card panel-card full-h" shadow="never">
          <div class="panel-title mb-4"><el-icon><Connection /></el-icon> 权限干道拓扑</div>
          <el-input v-model="treeFilter" placeholder="搜索节点群" class="neo-input mb-4" clearable prefix-icon="Search" />
          <el-tree 
             ref="roleTreeRef"
             :data="treeData" 
             :props="defaultProps" 
             :filter-node-method="filterNode"
             default-expand-all
             :expand-on-click-node="false"
             class="custom-tree"
          >
             <template #default="{ node, data }">
                <span class="custom-tree-node">
                  <span>
                    <el-icon v-if="data.type === 'root'" color="#ea4335"><Platform /></el-icon>
                    <el-icon v-else-if="data.type === 'node'" color="#ff9500"><Share /></el-icon>
                    <el-icon v-else color="#0052cc"><User /></el-icon>
                    {{ node.label }}
                  </span>
                </span>
             </template>
          </el-tree>
       </el-card>
    </div>

    <!-- Right: ProTable Data -->
    <div class="col-right">
      <div class="module-topbar" style="margin-bottom: 15px;">
        <div>
          <div class="panel-title">管理员与系统安全总控</div>
          <div class="panel-subtitle">系统级账号管理中心，可分配 RBAC 业务全生命周期授权</div>
        </div>
        <div class="topbar-actions">
          <el-button type="primary" class="neo-btn-primary" @click="openCreateDrawer"><el-icon><Plus /></el-icon> 调配新管理员</el-button>
          <el-button plain icon="Refresh" @click="loadData">全局同步</el-button>
        </div>
      </div>
      
      <div class="summary-grid mb-4">
        <el-card class="summary-card glass-card" shadow="never">
          <div class="summary-label">根授权账户数</div>
          <div class="summary-value" style="color: var(--color-primary);">{{ records.length }}</div>
        </el-card>
        <el-card class="summary-card glass-card" shadow="never">
          <div class="summary-label">审计异常追踪</div>
          <div class="summary-value text-success">0</div>
        </el-card>
      </div>

      <el-card class="glass-card panel-card" shadow="never">
        <!-- ProTable Toolbar -->
        <div class="pro-toolbar">
           <div class="action-left">
             <el-button size="small" type="danger" plain>批量剥夺验证</el-button>
             <el-button size="small" plain>导出结案报告</el-button>
           </div>
           <div class="action-right">
             <el-tooltip content="列集设置" placement="top">
                <el-button circle icon="Setting" />
             </el-tooltip>
             <el-tooltip content="高密度视图" placement="top">
                <el-button circle icon="Rank" />
             </el-tooltip>
           </div>
        </div>

        <el-table :data="records" style="width: 100%" class="custom-table" border>
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column prop="username" label="根账号标识" min-width="140">
             <template #default="scope">
                <span style="font-weight:700; color:var(--color-primary)">{{ scope.row.username }}</span>
             </template>
          </el-table-column>
          <el-table-column prop="real_name" label="安全员姓名" min-width="120" />
          <el-table-column prop="role_code" label="RBAC 防区" min-width="150">
            <template #default>
              <el-tag effect="dark" color="#002966" style="border:none">ROOT_ZONE</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="account_status" label="节点连通态" min-width="140">
            <template #default="scope">
              <div class="status-badge" :class="['正常', '启用'].includes(scope.row.account_status) ? 'badge-success' : 'badge-error'">
                <span class="dot"></span> {{ scope.row.account_status }}服役中
              </div>
            </template>
          </el-table-column>
          <el-table-column label="动作矩阵" fixed="right" min-width="260" align="center">
            <template #default="scope">
              <div style="display:flex; justify-content:center; gap:8px;">
                 <el-tag class="action-tag tag-blue" @click="resetPwd(scope.row)">强制密匙轮换</el-tag>
                 <el-tag class="action-tag tag-orange">权限审计剖析</el-tag>
                 <el-tag class="action-tag tag-red" v-if="scope.row.username !== 'admin'">物理剥夺</el-tag>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- Right Drawer for Creation -->
    <el-drawer v-model="drawerVisible" title="创建顶级特权信标" size="500px" style="border-radius: 20px 0 0 20px;">
       <div style="padding: 10px 20px;">
          <el-steps :active="step" align-center finish-status="success" class="mb-4 neo-steps">
            <el-step title="基础定密" />
            <el-step title="赋权沙盘" />
            <el-step title="二次核验" />
          </el-steps>

          <div class="mt-4 step-content">
             <div v-if="step === 0" class="step-animation">
               <el-form label-position="top" class="neo-form">
                 <el-form-item label="核心编号 (必填)" class="neo-input">
                   <el-input placeholder="自动生成或手填" />
                 </el-form-item>
                 <el-form-item label="操作员真名" class="neo-input">
                   <el-input placeholder="录入真实姓名以备背调" />
                 </el-form-item>
                 <el-button type="primary" class="full-width" @click="step=1">流转至下一步</el-button>
               </el-form>
             </div>
             
             <div v-if="step === 1" class="step-animation">
                <h4 style="margin-bottom: 10px;">可用授权信道雷达</h4>
                <div style="display:flex; gap:10px; flex-wrap:wrap;">
                   <el-checkbox-button checked>存款复核权</el-checkbox-button>
                   <el-checkbox-button checked>大额放哨权</el-checkbox-button>
                   <el-checkbox-button>高频拦截豁免</el-checkbox-button>
                   <el-checkbox-button>底层数据库直读</el-checkbox-button>
                </div>
                <el-button type="primary" class="full-width mt-4" @click="step=2">生成防暴签名</el-button>
                <el-button link class="full-width mt-2" @click="step=0">上一步</el-button>
             </div>
             
             <div v-if="step === 2" class="step-animation text-center">
                <el-icon :size="60" color="#ea4335"><WarnTriangleFilled /></el-icon>
                <h3 class="mt-3 text-danger">您正在授予系统最高危权限</h3>
                <p style="font-size:13px; color:var(--color-text-secondary); line-height:1.6">
                  新节点生成后，该账号将具备切断甚至清空流水库的操作可能。任何损失不可追回！
                </p>
                <el-button type="danger" style="margin-top:20px; width: 100%;" @click="finishCreate">强制下发并载入主根</el-button>
             </div>
          </div>
       </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Setting, Rank, Connection, Platform, User, Share, WarnTriangleFilled } from '@element-plus/icons-vue'
import http from '../api'

const records = ref([])
const treeFilter = ref('')
const roleTreeRef = ref(null)
const drawerVisible = ref(false)
const step = ref(0)

const treeData = [
  { id: 1, label: '全球统筹委员会 (ROOT)', type: 'root', children: [
    { id: 2, label: '华北金融审计署', type: 'node', children: [
      { id: 5, label: '审计长 (A1)', type: 'user' },
      { id: 6, label: '清算专员 (B2)', type: 'user' }
    ]},
    { id: 3, label: '南方风控防御局', type: 'node', children: [
      { id: 7, label: '高级拦截手 (F1)', type: 'user' }
    ]},
    { id: 4, label: '亚太特别支队', type: 'node' }
  ]}
]

const defaultProps = { children: 'children', label: 'label' }

watch(treeFilter, (val) => {
  if(roleTreeRef.value) roleTreeRef.value.filter(val)
})

function filterNode(value, data) {
  if (!value) return true
  return data.label.includes(value)
}

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
    await ElMessageBox.confirm(`系统将对核心账户 ${row.username} 进行不可逆的哈希轮转, 确认?`, '最高权指令核查', { type: 'error' })
    const res = await http.post(`/admin/users/reset-password/${row.id}`)
    ElMessage.success(res.message || '哈希环已重置完毕')
  } catch(e) { if(e !== 'cancel') ElMessage.error(String(e)) }
}

function openCreateDrawer() {
  step.value = 0
  drawerVisible.value = true
}

function finishCreate() {
  ElMessage.success('特权账户信标下发完毕，正在同步全网分发路由。')
  drawerVisible.value = false
}

onMounted(loadData)
</script>

<style scoped>
.admin-rbac-layout { display: grid; grid-template-columns: 300px 1fr; gap: 24px; min-height: calc(100vh - 120px); align-items: start; }
.full-h { height: 100%; min-height: 600px; }

.custom-tree { background: transparent; }
.custom-tree :deep(.el-tree-node__content) { height: 38px; border-radius: 8px; transition: all 0.2s; margin-bottom: 2px; }
.custom-tree :deep(.el-tree-node__content:hover) { background-color: var(--color-surface-hover); }
.custom-tree-node { display: flex; align-items: center; justify-content: space-between; width: 100%; font-size: 14px; font-weight: 600; color: var(--color-text); }
.custom-tree-node span { display:flex; align-items:center; gap:8px;}

.pro-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; padding-bottom: 15px; border-bottom: 1px dashed var(--color-border); }
.action-left, .action-right { display: flex; gap: 10px; }

.custom-table { border-radius: 12px; overflow: hidden; --el-table-border-color: var(--color-border); }

.status-badge { display: inline-flex; align-items: center; gap: 6px; padding: 4px 12px; border-radius: 999px; font-size: 12px; font-weight: 700; }
.badge-success { background: rgba(52, 168, 83, 0.1); color: #34a853; }
.badge-error { background: rgba(234, 67, 53, 0.1); color: #ea4335; }
.status-badge .dot { width: 6px; height: 6px; border-radius: 50%; }
.badge-success .dot { background: #34a853; box-shadow: 0 0 5px #34a853; }
.badge-error .dot { background: #ea4335; box-shadow: 0 0 5px #ea4335; }

.action-tag { cursor: pointer; transition: all 0.2s; border: none !important; font-weight: 600; padding: 0 10px; height: 26px; line-height: 26px; border-radius: 6px; }
.action-tag:hover { transform: translateY(-2px); box-shadow: var(--shadow-sm); }
.tag-blue { background: rgba(0, 82, 204, 0.1); color: #0052cc; }
.tag-orange { background: rgba(255, 149, 0, 0.1); color: #ff9500; }
.tag-red { background: rgba(234, 67, 53, 0.1); color: #ea4335; }

.step-animation { animation: slideIn 0.3s ease-out; }
@keyframes slideIn { from { opacity:0; transform: translateX(20px); } to {opacity:1; transform: translateX(0);} }

@media (max-width: 1200px) {
  .admin-rbac-layout { grid-template-columns: 1fr; }
  .full-h { min-height: 400px; }
}
</style>
