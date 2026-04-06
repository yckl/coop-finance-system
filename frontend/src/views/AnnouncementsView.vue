<template>
  <div class="module-page">
    <div class="module-topbar">
      <div>
        <div class="panel-title">全局公告下发中心</div>
        <div class="panel-subtitle">在此管理向全体成员、特定角色下发的置顶规则与业务通知</div>
      </div>
      <div class="topbar-actions">
        <el-button type="primary" icon="Position" @click="openPublish" v-if="role === 'admin'">发布新公告</el-button>
        <el-button @click="loadData">刷新投递箱</el-button>
      </div>
    </div>
    
    <div class="summary-grid">
      <el-card class="summary-card" shadow="never">
         <div class="summary-label">已发送通知</div>
         <div class="summary-value" style="color: #3498db;">{{ records.length }}</div>
      </el-card>
    </div>

    <el-card class="panel-card" shadow="never">
      <el-table :data="records" stripe style="width: 100%">
        <el-table-column label="标题" min-width="250">
          <template #default="scope">
            <strong>{{ scope.row.notice_title }}</strong>
            <el-tag v-if="scope.row.top_flag === '是'" size="small" type="danger" effect="dark" style="margin-left: 8px;">置顶</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="notice_type" label="发文类型" min-width="120" />
        <el-table-column prop="visible_scope" label="圈定受众" min-width="120" />
        <el-table-column prop="publish_status" label="推流状态" min-width="100">
          <template #default="scope">
            <el-badge is-dot :type="scope.row.publish_status === '已发布' ? 'success' : 'info'" style="margin-right:8px;"></el-badge>
            <span>{{ scope.row.publish_status }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="read_count" label="阅读量" min-width="100">
          <template #default="scope">
            <span>{{ scope.row.read_count }} 人</span>
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="创建落款" min-width="160" />
        
        <el-table-column label="操作" fixed="right" min-width="200" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="showContent(scope.row)">阅览全文</el-button>
            <el-button link type="success" v-if="scope.row.publish_status !== '已发布' && role === 'admin'" @click="fakeAction('推流')">立即推流</el-button>
            <el-button link type="danger" @click="fakeAction('撤稿删除')" v-if="role === 'admin'">撤稿</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="publishVisible" title="公告起草器" width="60%">
      <el-form label-width="100px">
        <el-form-item label="主标题">
          <el-input v-model="form.notice_title" placeholder="请输入公文主标题" />
        </el-form-item>
        <el-form-item label="类型归属">
          <el-select v-model="form.notice_type" placeholder="选中类型以归档">
             <el-option label="业务公告" value="业务公告" />
             <el-option label="系统规则" value="系统规则" />
             <el-option label="紧急熔断" value="紧急熔断" />
          </el-select>
        </el-form-item>
        <el-form-item label="可见范围">
          <el-radio-group v-model="form.visible_scope">
             <el-radio value="全部用户">公网可访问 (公开)</el-radio>
             <el-radio value="财务人员">内部员工限流</el-radio>
             <el-radio value="指定网点">下沉基建组</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="重要选项">
          <el-checkbox v-model="form.top_flag" true-value="是" false-value="否">设定特权置顶强提醒</el-checkbox>
        </el-form-item>
        <el-form-item label="内容编辑区">
          <!-- 原生的 textarea 满足基础富文本的折行需求 -->
          <el-input 
            v-model="form.notice_content" 
            type="textarea" 
            :rows="10" 
            placeholder="起草文档内容... (由于缺乏富文本插件，仅支持软换行)" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="publishVisible = false">保存草稿库</el-button>
          <el-button type="primary" @click="fakeAction('发布本文档')">直接过审发布</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api'

const route = useRoute()
const role = route.meta.role || 'admin'
const records = ref([])
const publishVisible = ref(false)
const form = ref({
  notice_title: '',
  notice_type: '业务公告',
  visible_scope: '全部用户',
  top_flag: '否',
  notice_content: ''
})

async function loadData() {
  try {
    const res = await http.get(`/${role}/module/announcements`)
    records.value = res.data.records || []
  } catch (e) {
    ElMessage.error('无法加载公告信箱: ' + e)
  }
}

function openPublish() {
  form.value = { notice_title: '', notice_type: '业务公告', visible_scope: '全部用户', top_flag: '否', notice_content: '' }
  publishVisible.value = true
}

function showContent(row) {
  ElMessageBox.alert(`<div style="white-space: pre-wrap; line-height: 1.6;">${row.notice_content}</div>`, row.notice_title, {
    dangerouslyUseHTMLString: true,
    confirmButtonText: '阅毕'
  })
}

function fakeAction(act) {
  ElMessage.success(`${act} 操作已被前端模拟接管，并未真正入库！`)
  publishVisible.value = false
}

onMounted(loadData)
</script>

<style scoped>
.panel-card {
  border-radius: 8px;
}
.summary-card {
  background: #f8f9fa;
  border: none;
}
</style>
