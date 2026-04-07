<template>
  <div class="module-page admin-users-layout">
    <div class="full-w-col">
      <div class="module-topbar" style="margin-bottom: 20px;">
        <div>
          <div class="panel-title">信用社网域居民数字体资产库</div>
          <div class="panel-subtitle">包含用户开户、身份验证与设备解绑等功能。</div>
        </div>
        <div class="topbar-actions">
          <el-button type="success" plain icon="Upload" @click="openUploadDrawer">人行数据对齐</el-button>
          <el-button type="primary" class="neo-btn-primary" icon="Plus" @click="openCreate">发放新卡</el-button>
          <el-button plain icon="Refresh" @click="loadData">数据归拢</el-button>
        </div>
      </div>
      
      <!-- Smart Search Tags -->
      <div class="search-matrix glass-card mb-4">
        <el-input v-model="keyword" placeholder="超声波探扫 (姓名、证件、手机尾号)" class="neo-input search-input" prefix-icon="Search" clearable />
        <div class="tag-filters">
           <span class="filter-label">行为侦测雷达:</span>
           <el-tag effect="plain" round class="filter-tag" @click="keyword='异常'">异常转出池</el-tag>
           <el-tag effect="plain" round class="filter-tag" @click="keyword='超额'">超发理财库</el-tag>
           <el-tag effect="plain" round class="filter-tag" @click="keyword='休眠'">三年休眠挂失户</el-tag>
           <el-tag effect="plain" round class="filter-tag" @click="keyword=''">关闭雷达</el-tag>
        </div>
      </div>

      <el-card class="glass-card panel-card" shadow="never">
        <el-table :data="filteredRecords" @row-click="showUserDetails" style="width: 100%" class="custom-table" row-class-name="pointer-row">
          <el-table-column width="40" align="center">
             <template #default="scope">
                <el-icon v-if="scope.row.login_username === 'user'" color="#00c9a7" :size="18"><StarFilled /></el-icon>
                <el-icon v-else color="#ccc"><Star /></el-icon>
             </template>
          </el-table-column>
          <el-table-column prop="user_no" label="数字网联 ID" min-width="120">
             <template #default="scope">
                <span style="font-family:monospace; font-weight:700;">{{ scope.row.user_no }}</span>
             </template>
          </el-table-column>
          <el-table-column prop="real_name" label="法人/自然人名称" min-width="140" />
          <el-table-column prop="phone" label="通讯信标" min-width="120" />
          <el-table-column label="银联画像定级" min-width="130" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.login_username === 'user'" effect="dark" color="#ff9500" style="border:none;">重点理财客户</el-tag>
              <el-tag v-else effect="dark" color="#8b99b0" style="border:none;">普通存取长尾</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="account_status" label="冻结态势" min-width="120" align="center">
            <template #default="scope">
              <span class="status-indicator" :class="scope.row.account_status === '正常' ? 'si-green' : 'si-red'">
                {{ scope.row.account_status }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="强权制裁" fixed="right" min-width="180" align="center">
            <template #default="scope">
              <el-button link type="warning" @click.stop="openEditRecord(scope.row)">锁定账户</el-button>
              <el-button link type="danger" @click.stop="suspendRecord(scope.row)">移交公安</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- Right Drawer Details -->
    <el-drawer v-model="detailsVisible" :title="activeUser ? `网联征信档案 - ${activeUser.real_name}` : '网联征信档案'" size="700px">
       <div v-if="activeUser" class="staff-details-layout">
          <!-- Left Small Card -->
          <div class="staff-bio neo-input text-center">
             <el-avatar :size="80" style="background:#00c9a7">
               {{ activeUser.real_name.charAt(0) }}
             </el-avatar>
             <h3 style="margin: 15px 0 5px;">{{ activeUser.real_name }}</h3>
             <p style="color:var(--color-text-secondary);font-size:13px;margin:0;">{{ activeUser.phone }}</p>
             <el-tag size="small" type="warning" effect="dark" class="mt-3">A类白皮书客户</el-tag>
             <el-divider border-style="dashed" />
             <div style="text-align:left; font-size:12px; line-height:1.8;">
               <div>网联ID：{{ activeUser.user_no }}</div>
               <div>归属：西城支行网点</div>
               <div>征信：<span style="color:#34a853">纯净无瑕</span></div>
             </div>
          </div>
          
          <!-- Right Tabs -->
          <div class="staff-tabs">
            <el-tabs class="neo-tabs">
               <el-tab-pane label="大循环流水账">
                  <el-timeline style="margin-top:20px;">
                    <el-timeline-item type="danger" timestamp="今天 10:12">春耕采购款电汇 150,000 元。</el-timeline-item>
                    <el-timeline-item type="success" timestamp="昨天 09:30">网银跨行转入 500,000 元。</el-timeline-item>
                    <el-timeline-item type="info" timestamp="本月初">自动存入粮农补贴 2,100 元。</el-timeline-item>
                  </el-timeline>
               </el-tab-pane>
               <el-tab-pane label="待处置信托件">
                  <el-empty description="该实体没有任何在排队审核中的高危报销或者理财购买事件。" />
               </el-tab-pane>
            </el-tabs>
          </div>
       </div>
    </el-drawer>

    <!-- Upload Drawer -->
    <el-drawer v-model="uploadVisible" title="人民银行大数据集群导入" size="450px" direction="ltr">
       <div style="padding: 20px;">
         <el-upload
            class="upload-demo neo-input"
            drag
            action="#"
            :auto-upload="false"
            style="text-align:center; padding: 40px 0; border-radius: 16px;"
          >
            <el-icon class="el-icon--upload" :size="60" color="#00c9a7"><UploadFilled /></el-icon>
            <div class="el-upload__text mt-3">
              拖拽公安网离线打包加密库到此，或 <em>点击上传查验</em>
            </div>
            <template #tip>
              <div class="el-upload__tip mt-4" style="color:var(--color-text-secondary);">
                接入必须符合央行 2026 最新信披准则。<br>由于涉及敏感数据，已开启严格安全监控。
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
const activeUser = ref(null)

const filteredRecords = computed(() => {
  if (!keyword.value) return records.value
  return records.value.filter(item => JSON.stringify(item).includes(keyword.value))
})

async function loadData() {
  try {
    const res = await http.get(`/admin/users`)
    records.value = res.data.records || []
  } catch (e) {
    ElMessage.error('无法同步数据: ' + e)
  }
}

function showUserDetails(row) {
  activeUser.value = row
  detailsVisible.value = true
}

function openCreate() {
  ElMessage.info('本系统响应国家【断卡行动】号召，拒绝通过系统直接大批量生造虚拟实体。')
}

function openUploadDrawer() {
  uploadVisible.value = true
}

function doFakeUpload() {
  ElMessage.success('系统安全扫描通过。所有数据已同步完成。')
  uploadVisible.value = false
}

function openEditRecord(row) {
  ElMessageBox.confirm(`冻结资金是极其危险的法律性动作，请确信已经拿到法院执行批文。`, '警告', { type: 'error' }).catch(()=>{})
}

function suspendRecord(row) {
  ElMessageBox.confirm(`系统拒绝了你的粗暴操作请求。反洗钱调查请提交专属公文信道请求。`, '权限禁忌', { type: 'error' }).catch(()=>{})
}

onMounted(loadData)
</script>

<style scoped>
.admin-users-layout { display: flex; flex-direction: column; min-height: calc(100vh - 120px); }

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
.si-red { color: #ea4335; }
.si-red::before { background: #ea4335; box-shadow: 0 0 4px #ea4335; }

.staff-details-layout { display: grid; grid-template-columns: 200px 1fr; gap: 30px; height: 100%; align-items: start; }
.staff-bio { padding: 20px 15px; border-radius: 16px; background: var(--color-surface); }
.staff-tabs { flex: 1; }

.neo-tabs :deep(.el-tabs__item) { font-size: 15px; font-weight: 600; }
</style>
