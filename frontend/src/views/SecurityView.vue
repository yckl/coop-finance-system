<template>
  <div class="module-page">
    <div class="module-topbar">
      <div>
        <div class="panel-title">安全中枢与日志审计墙</div>
        <div class="panel-subtitle">防范外部攻击，审查内部提权。系统自动收集风险信号并拦截越权操作。</div>
      </div>
      <div class="topbar-actions">
        <el-button type="danger" @click="fakeTool('封锁当前所有异地IP')">一键封停高危 IP</el-button>
        <el-button color="#2980b9" :dark="true" @click="fakeTool('构建物理介质备份')">执行数据库冷备份</el-button>
        <el-button @click="loadData">刷新监视图</el-button>
      </div>
    </div>
    
    <div class="stepper-area" style="background:#fff; border-radius:8px; padding:15px; margin-bottom: 20px;">
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="audit-stat-box" style="border-right: 1px solid #eee;">
            <div style="font-size: 14px; color: #7f8c8d;">今日拦截攻击</div>
            <div style="font-size: 28px; color: #e74c3c; font-weight: bold;">0</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="audit-stat-box" style="border-right: 1px solid #eee;">
            <div style="font-size: 14px; color: #7f8c8d;">高危预警累积</div>
            <div style="font-size: 28px; color: #e67e22; font-weight: bold;">{{ highRiskWarningCount }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="audit-stat-box">
            <div style="font-size: 14px; color: #7f8c8d;">累计日志数</div>
            <div style="font-size: 28px; color: #2980b9; font-weight: bold;">{{ auditLogs.length }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-card class="panel-card" shadow="never">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="🚨 业务风险侦测哨" name="WARNINGS">
          <el-table :data="riskWarnings" stripe style="width: 100%" max-height="500">
            <el-table-column prop="warning_code" label="警情编号" width="180" />
            <el-table-column prop="warning_level" label="等级" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.warning_level === '高' ? 'danger' : 'warning'" effect="dark">{{ scope.row.warning_level }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="warning_type" label="风险定性" width="150" />
            <el-table-column prop="warning_object" label="涉事主客体" width="150" />
            <el-table-column prop="warning_content" label="警示情报" min-width="250">
               <template #default="scope">
                 <strong style="color: #c0392b;">{{ scope.row.warning_content }}</strong>
               </template>
            </el-table-column>
            <el-table-column prop="process_status" label="干预进度" width="120" />
            <el-table-column prop="created_at" label="警报触发时间" width="170" />
            <el-table-column label="动作" fixed="right">
              <template #default>
                <el-button size="small" type="primary" link disabled>下发调查</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="📜 全量追踪审计册" name="LOGS">
          <el-table :data="auditLogs" stripe style="width: 100%" size="small" max-height="500">
            <el-table-column prop="log_type" label="日志溯源" width="120" />
            <el-table-column prop="risk_level" label="影响面" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.risk_level === '高' ? 'danger' : (scope.row.risk_level === '中' ? 'warning' : 'info')">{{ scope.row.risk_level }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="operator_name" label="操作身份" width="120" />
            <el-table-column prop="module_name" label="涉事模块" width="150" />
            <el-table-column prop="action_name" label="执行动作" width="100">
               <template #default="scope">
                 <strong style="color: #2c3e50;">{{ scope.row.action_name }}</strong>
               </template>
            </el-table-column>
            <el-table-column prop="action_content" label="指令载荷明细 (Payload/Desc)" min-width="250" />
            <el-table-column prop="ip_address" label="来源地址 (IPv4)" width="130" />
            <el-table-column prop="created_at" label="物理时间戳" width="160" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="🔒 第三方安全工具链" name="TOOLS">
          <div style="padding: 20px;">
             <el-descriptions title="基础防护策略" :column="2" border>
               <el-descriptions-item label="SSL证书状态"><el-tag type="success">有效 (Valid)</el-tag></el-descriptions-item>
               <el-descriptions-item label="防火墙引流"><el-tag type="info">透传模式</el-tag></el-descriptions-item>
               <el-descriptions-item label="密码锁定策略">连续 5 次错误将封停 24 小时</el-descriptions-item>
               <el-descriptions-item label="异地登录校验">已开启强提示</el-descriptions-item>
             </el-descriptions>

             <div style="margin-top: 30px;">
               <el-alert title="高危维护区：以下操作需配合验证器二次授权" type="error" :closable="false" />
               <div style="margin-top: 15px;">
                 <el-button plain @click="fakeTool('重置所有密码散列')">洗切所有管理员鉴权</el-button>
                 <el-button plain @click="fakeTool('阻断特定段位路由')">封锁海外流量IP通道</el-button>
                 <el-button plain @click="fakeTool('应用系统重入')">拉下集群物理闸</el-button>
               </div>
             </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api'

const activeTab = ref('WARNINGS')
const auditLogs = ref([])
const riskWarnings = ref([])

const highRiskWarningCount = computed(() => {
  return riskWarnings.value.filter(rw => rw.warning_level === '高').length
})

async function loadData() {
  try {
    const res = await http.get(`/admin/module/security`)
    auditLogs.value = res.data.auditLogs || []
    riskWarnings.value = res.data.riskWarnings || []
  } catch (e) {
    ElMessage.error('安保系统联调失败')
  }
}

function fakeTool(act) {
  ElMessage.warning(`安全审计局域锁已拦截: ${act}。该行为需上级持盾牌方可突防。`)
}

onMounted(loadData)
</script>

<style scoped>
.panel-card { border-radius: 8px; }
.audit-stat-box { text-align: center; padding: 10px; }
</style>
