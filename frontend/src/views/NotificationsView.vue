<template>
  <div class="module-page">
    <div class="module-topbar">
      <div>
        <div class="panel-title">我的消息通知中心</div>
        <div class="panel-subtitle">接收和查阅平台下发的服务通知、警报及业务进度流转变更</div>
      </div>
      <div class="topbar-actions">
        <el-button type="primary" plain @click="loadData">获取新消息</el-button>
        <el-button type="success" @click="clearAll">全部标记为已读</el-button>
      </div>
    </div>
    
    <el-card class="panel-card" shadow="never" style="margin-top:20px;">
      <el-timeline style="max-width: 800px">
        <el-timeline-item
          v-for="(msg, index) in notifications"
          :key="index"
          :type="msg.type || 'primary'"
          :timestamp="msg.time"
          placement="top"
        >
          <el-card shadow="hover">
            <h4 style="margin-top:0;">{{ msg.title }}</h4>
            <p style="color: #666; line-height:1.6;">{{ msg.content }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      
      <el-empty v-if="notifications.length === 0" description="暂无新消息" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api'

const notifications = ref([
  { title: "防诈骗温馨提醒", content: "请注意保护您的密码，切勿泄露给任何陌生人哪怕对方自称办案人员。如遇可疑大额冻结，请第一时间来物理网点提交质询单。", time: new Date().toLocaleString(), type: 'danger' },
  { title: "存款定期续期播报", content: "您存入的一笔春耕发展专属定存已录入住建系统。请留存相关证据文件。", time: new Date().toLocaleString(), type: 'success' }
])

async function loadData() {
  try {
    const res = await http.get(`/user/module/notifications`)
    if(res.data.records && res.data.records.length > 0) {
       res.data.records.forEach(r => {
           notifications.value.unshift({ title: r.notice_title, content: '收到全体下发通知。前往主站公告区查看！', time: r.created_at, type: 'info' })
       })
    }
  } catch (e) {
    ElMessage.error(String(e))
  }
}

function clearAll() {
  notifications.value = []
  ElMessage.success("收件箱已燃毁结课。")
}

onMounted(loadData)
</script>

<style scoped>
.panel-card { border-radius: 8px; }
</style>
