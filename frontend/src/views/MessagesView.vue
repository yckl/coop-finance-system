<template>
  <div class="module-page">
    <div class="module-topbar">
      <div>
        <div class="panel-title">投诉工单与客服信箱</div>
        <div class="panel-subtitle">处理来自全网的客诉、咨询与反馈信件</div>
      </div>
      <div class="topbar-actions">
        <!-- 批量操作 -->
        <el-button color="#e67e22" :dark="true" @click="fakeBatchAction('知识库固化')" v-if="role === 'admin'">构建本地 FAQ 知识库</el-button>
        <el-button type="primary" v-if="role === 'user'" @click="showForm = true">发起新工单咨询</el-button>
        <el-button type="success" plain @click="loadData">强制追新</el-button>
      </div>
    </div>
    
    <div class="summary-grid">
      <el-card class="summary-card" shadow="never">
         <div class="summary-label">待处理工单红线</div>
         <div class="summary-value" style="color: #e74c3c;">{{ unprocessedCount }}</div>
      </el-card>
      <el-card class="summary-card" shadow="never">
         <div class="summary-label">已化解 / 回复</div>
         <div class="summary-value" style="color: #27ae60;">{{ records.length - unprocessedCount }}</div>
      </el-card>
    </div>

    <el-card class="panel-card" shadow="never">
      <el-tabs v-model="activeTab" class="demo-tabs">
        <el-tab-pane label="紧急待处理" name="未处理"></el-tab-pane>
        <el-tab-pane label="派件进行中" name="处理中"></el-tab-pane>
        <el-tab-pane label="已闭环结案" name="已处理"></el-tab-pane>
        <el-tab-pane label="信件全览" name="全部"></el-tab-pane>
      </el-tabs>

      <el-table :data="filteredRecords" stripe style="width: 100%; margin-top: 10px;">
        <el-table-column type="expand">
          <template #default="scope">
            <div style="padding: 20px; background-color: #fafbfc; border-radius: 4px; margin: 0 20px;">
              <p style="margin: 0; color: #555;"><strong>寄件详情:</strong> {{ scope.row.message_content }}</p>
              <div style="margin-top: 15px; border-top: 1px dashed #ddd; padding-top: 15px;">
                 <el-input
                   v-model="replyText[scope.row.id]"
                   type="textarea"
                   :rows="3"
                   placeholder="请以管理员身份给与回复指导..."
                 />
                 <div style="margin-top: 10px; text-align: right;">
                    <el-button type="primary" size="small" @click="fakeAction('发送回执')">发送回执</el-button>
                    <el-button type="warning" size="small" plain @click="fakeAction('转入FAQ')">转入知识库</el-button>
                 </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="message_type" label="工单分类" min-width="100">
          <template #default="scope">
            <el-tag :type="scope.row.message_type === '投诉' ? 'danger' : 'info'">{{ scope.row.message_type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message_title" label="公文主旨" min-width="200" />
        <el-table-column prop="sender_name" label="寄件人" min-width="120">
          <template #default="scope">
             <span>{{ scope.row.sender_name }} ({{ scope.row.sender_no }})</span>
          </template>
        </el-table-column>
        <el-table-column prop="process_status" label="流程节点" min-width="120">
          <template #default="scope">
            <el-tag :type="scope.row.process_status === '未处理' ? 'danger' : 'success'" effect="dark">
              {{ scope.row.process_status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="寄投日时" min-width="160" />
        
        <el-table-column label="动作分配" fixed="right" min-width="160" align="center">
          <template #default="scope">
            <el-button link type="primary" v-if="role !== 'user'" :disabled="scope.row.process_status !== '未处理'" @click="fakeAction(role === 'admin' ? '转派下级' : '接手处理')">{{ role === 'admin' ? '专员受派' : '接手处理' }}</el-button>
            <el-button link type="danger" @click="fakeAction('删除信件')" v-if="role === 'admin' || role === 'user'">{{ role === 'user' ? '撤回咨询' : '焚烧' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showForm" title="建立新系统咨询" width="500px">
       <el-form label-width="80px">
          <el-form-item label="咨询类目" required>
             <el-select v-model="newForm.type" placeholder="请选择定位标签" style="width:100%">
               <el-option label="卡号资金解冻" value="账户风控相关" />
               <el-option label="报销进度催款" value="报销资金查询" />
               <el-option label="系统故障报告" value="平台技术故障" />
             </el-select>
          </el-form-item>
          <el-form-item label="事务载荷" required>
             <el-input v-model="newForm.content" type="textarea" :rows="4" />
          </el-form-item>
       </el-form>
       <template #footer>
         <div class="dialog-footer">
           <el-button @click="showForm = false">放弃请求</el-button>
           <el-button type="primary" @click="submitMsg">抛信入队列</el-button>
         </div>
       </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api'

const route = useRoute()
const role = route.meta.role || 'admin'
const records = ref([])
const activeTab = ref('未处理')
const replyText = ref({})
const showForm = ref(false)
const newForm = ref({ type: '', content: '' })

const unprocessedCount = computed(() => {
  return records.value.filter(r => r.process_status === '未处理').length
})

const filteredRecords = computed(() => {
  if (activeTab.value === '全部') return records.value
  return records.value.filter(r => r.process_status === activeTab.value)
})

async function loadData() {
  try {
    const res = await http.get(`/${role}/module/messages`)
    records.value = res.data.records || []
  } catch (e) {
    ElMessage.error('信箱接口遭受污染: ' + e)
  }
}

function fakeAction(act) {
  ElMessage.success(`Mock指令命中: [${act}] 后端接口正在模拟处理状态...`)
}

function fakeBatchAction(act) {
  ElMessage.warning(`触发大盘批量操作 [${act}]，系统计算缓存暂未落地，请依赖下个工程周期支撑。`)
}

onMounted(loadData)
</script>

<style scoped>
.panel-card {
  border-radius: 8px;
}
.summary-card {
  background: #fdfbfb;
  border: none;
}
</style>
