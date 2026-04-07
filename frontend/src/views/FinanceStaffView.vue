<template>
  <div class="module-page admin-staff-layout">
    
    <div class="full-w-col">
      <div class="module-topbar" style="margin-bottom: 20px;">
        <div>
          <div class="panel-title">机要柜面与财务集群池</div>
          <div class="panel-subtitle">物理网点财务柜员管理矩阵，执行涉密权限审计与入职审查。</div>
        </div>
        <div class="topbar-actions">
          <el-button type="success" plain icon="Upload" @click="openUploadDrawer">批量离线档案导入</el-button>
          <el-button type="primary" class="neo-btn-primary" icon="Plus" @click="openCreate">建档入列</el-button>
          <el-button plain icon="Refresh" @click="loadData">强制追档</el-button>
        </div>
      </div>
      
      <!-- Smart Search Tags -->
      <div class="search-matrix glass-card mb-4">
        <el-input v-model="keyword" placeholder="高精语义搜索 (工号、姓名、网点坐标)" class="neo-input search-input" prefix-icon="Search" clearable />
        <div class="tag-filters">
           <span class="filter-label">快筛雷达:</span>
           <el-tag effect="plain" round class="filter-tag" @click="keyword='异常'">异常行为迹象</el-tag>
           <el-tag effect="plain" round class="filter-tag" @click="keyword='大额'">近期经办大额</el-tag>
           <el-tag effect="plain" round class="filter-tag" @click="keyword=''">重置所有过滤网</el-tag>
        </div>
      </div>

      <el-card class="glass-card panel-card" shadow="never">
        <el-table :data="filteredRecords" @row-click="showStaffDetails" style="width: 100%" class="custom-table" row-class-name="pointer-row">
          <el-table-column width="40" align="center">
             <template #default="scope">
                <el-icon v-if="scope.row.login_username === 'caiwu'" color="#ff9500" :size="18"><StarFilled /></el-icon>
                <el-icon v-else color="#ccc"><Star /></el-icon>
             </template>
          </el-table-column>
          <el-table-column prop="staff_no" label="高阶授权码" min-width="110">
             <template #default="scope">
                <span style="font-family:monospace; font-weight:700;">{{ scope.row.staff_no }}</span>
             </template>
          </el-table-column>
          <el-table-column prop="staff_name" label="安全责任人" min-width="120" />
          <el-table-column prop="branch_name" label="所属安全营" min-width="150" show-overflow-tooltip />
          <el-table-column label="风险评估阈值" min-width="130" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.login_username === 'caiwu'" effect="dark" color="#0052cc" style="border:none;">A级 (无风险)</el-tag>
              <el-tag v-else effect="dark" color="#e67e22" style="border:none;">B级 (常规审查)</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="employment_status" label="编制与存续态" min-width="120" align="center">
            <template #default="scope">
              <span class="status-indicator" :class="scope.row.employment_status === '在职' ? 'si-green' : 'si-gray'">
                {{ scope.row.employment_status }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" min-width="180" align="center">
            <template #default="scope">
              <el-button link type="primary" @click.stop="openEditRecord(scope.row)">调防</el-button>
              <el-button link type="danger" @click.stop="suspendRecord(scope.row)">物理销号</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- Right Drawer Details -->
    <el-drawer v-model="detailsVisible" :title="activeStaff ? `机密档案 - ${activeStaff.staff_name}` : '机密档案'" size="700px">
       <div v-if="activeStaff" class="staff-details-layout">
          <!-- Left Small Card -->
          <div class="staff-bio neo-input text-center">
             <el-avatar :size="80" style="background:#0052cc">
               {{ activeStaff.staff_name.charAt(0) }}
             </el-avatar>
             <h3 style="margin: 15px 0 5px;">{{ activeStaff.staff_name }}</h3>
             <p style="color:var(--color-text-secondary);font-size:13px;margin:0;">{{ activeStaff.staff_no }}</p>
             <el-tag size="small" type="success" effect="dark" class="mt-3">核心柜员组</el-tag>
             <el-divider border-style="dashed" />
             <div style="text-align:left; font-size:12px; line-height:1.8;">
               <div>入列：2024-01-12</div>
               <div>IP池：高新内网段</div>
               <div>监控级别：<span style="color:#ff9500">L2</span></div>
             </div>
          </div>
          
          <!-- Right Tabs -->
          <div class="staff-tabs">
            <el-tabs class="neo-tabs">
               <el-tab-pane label="近期动账流">
                  <el-timeline style="margin-top:20px;">
                    <el-timeline-item type="primary" timestamp="12 mins ago">复核通过 U1001 报销工单 1,200元。</el-timeline-item>
                    <el-timeline-item type="warning" timestamp="1 hour ago">打回一份不合规的差旅票据。</el-timeline-item>
                    <el-timeline-item type="info" timestamp="昨天 15:30">代理 U1002 进行了柜面存取活动。</el-timeline-item>
                  </el-timeline>
               </el-tab-pane>
               <el-tab-pane label="考公与追责记录">
                  <el-empty description="目前履历清白，无任何合规部追责处分。" />
               </el-tab-pane>
            </el-tabs>
          </div>
       </div>
    </el-drawer>

    <!-- Upload Drawer -->
    <el-drawer v-model="uploadVisible" title="批量涉密档案导入通道" size="450px" direction="ltr">
       <div style="padding: 20px;">
         <el-upload
            class="upload-demo neo-input"
            drag
            action="#"
            :auto-upload="false"
            style="text-align:center; padding: 40px 0; border-radius: 16px;"
          >
            <el-icon class="el-icon--upload" :size="60" color="#0052cc"><UploadFilled /></el-icon>
            <div class="el-upload__text mt-3">
              拖拽公安网离线打包加密库到此，或 <em>点击上传查验</em>
            </div>
            <template #tip>
              <div class="el-upload__tip mt-4" style="color:var(--color-text-secondary);">
                要求必须为 GPG 加密的 .xls 或 .csv 数据容器。<br>系统将在导入前进行沙盒病毒分析。
              </div>
            </template>
          </el-upload>
          <el-button type="primary" class="full-width mt-4" size="large" @click="doFakeUpload">注入系统总线</el-button>
       </div>
    </el-drawer>

  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Refresh, Upload, UploadFilled, Star, StarFilled } from '@element-plus/icons-vue'
import http from '../api'

const records = ref([])
const keyword = ref('')
const detailsVisible = ref(false)
const uploadVisible = ref(false)
const activeStaff = ref(null)

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

function showStaffDetails(row) {
  activeStaff.value = row
  detailsVisible.value = true
}

function openCreate() {
  ElMessage.info('财务建档系统受到公安接口管制，此时拒绝明文生成！')
}

function openUploadDrawer() {
  uploadVisible.value = true
}

function doFakeUpload() {
  ElMessage.success('离线沙盒安全扫描通过。所有编制档案已合并至生产线。')
  uploadVisible.value = false
}

function openEditRecord(row) {
  ElMessage.warning(`正在触发对 [${row.staff_name}] 的全国网点跨域调防，暂未获取该级授权。`)
}

function suspendRecord(row) {
  ElMessageBox.confirm(`即将发起“数字抹除”命令：将该员工从公安网、金融总闸和本地服务器物理销毁。`, '最高防线越权', { type: 'error' }).catch(()=>{})
}

onMounted(loadData)
</script>

<style scoped>
.admin-staff-layout { display: flex; flex-direction: column; min-height: calc(100vh - 120px); }

.search-matrix { display: flex; flex-direction: column; gap: 15px; padding: 20px; border-radius: 16px; background: var(--color-glass-bg); }
.search-input { max-width: 600px; }
.tag-filters { display: flex; align-items: center; gap: 10px; font-size: 13px; }
.filter-label { font-weight: 700; color: var(--color-text-secondary); }
.filter-tag { cursor: pointer; border: 1px solid var(--color-border); background: var(--color-surface); color: var(--color-text); }
.filter-tag:hover { background: var(--color-primary); color: #fff; border-color: var(--color-primary); }

.custom-table { border-radius: 12px; overflow: hidden; --el-table-border-color: var(--color-border); }
.pointer-row { cursor: pointer; transition: background 0.2s; }
.pointer-row:hover { background-color: var(--color-surface-hover) !important; }

.status-indicator { display: inline-flex; align-items: center; gap: 6px; font-size: 13px; font-weight: 600; }
.status-indicator::before { content: ''; display: inline-block; width: 6px; height: 6px; border-radius: 50%; }
.si-green { color: #34a853; }
.si-green::before { background: #34a853; box-shadow: 0 0 4px #34a853; }
.si-gray { color: #8b99b0; }
.si-gray::before { background: #8b99b0; }

.staff-details-layout { display: grid; grid-template-columns: 200px 1fr; gap: 30px; height: 100%; align-items: start; }
.staff-bio { padding: 20px 15px; border-radius: 16px; background: var(--color-surface); }
.staff-tabs { flex: 1; }

.neo-tabs :deep(.el-tabs__item) { font-size: 15px; font-weight: 600; }
</style>
