<template>
  <div class="module-page">
    <div class="module-topbar">
      <div>
        <div class="panel-title">员工安全与职场档案管理</div>
        <div class="panel-subtitle">查考个人在网点的业务绩效、管理密保口令及身份核验媒介。</div>
      </div>
      <div class="topbar-actions">
        <el-button type="success" plain @click="saveProfile">同步云端</el-button>
      </div>
    </div>
    
    <el-row :gutter="20" style="margin-top: 15px;">
      <el-col :span="8">
        <el-card class="panel-card" shadow="never" style="text-align:center;">
          <div style="margin: 20px 0;">
             <el-avatar :size="100" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
             <div style="margin-top:15px; font-size:18px; font-weight:bold;">当前在线工号：{{ myRole }}</div>
          </div>
          <el-button size="small" @click="fakeUpload">上传新制服照</el-button>
          
          <el-divider>核心业务防线</el-divider>
          <div style="text-align:left; font-size: 14px; line-height: 2;">
            <div><strong>岗位锚点：</strong>中心网点 - 报销核准出纳柜</div>
            <div><strong>风险评分：</strong><el-tag type="success" size="small">极低险域 (98分)</el-tag></div>
            <div><strong>上次述职：</strong>2026-04-01 08:30:12</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="16">
        <el-card class="panel-card" shadow="never">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基础身份图谱" name="INFO">
               <el-form label-width="120px" style="max-width: 500px; margin-top:20px;">
                  <el-form-item label="在网花名">
                    <el-input value="张柜员" readonly></el-input>
                  </el-form-item>
                  <el-form-item label="密保电话">
                    <el-input value="138****0001"></el-input>
                  </el-form-item>
                  <el-form-item label="常驻网点IP群">
                    <el-input value="192.168.1.1 - 192.168.1.255" readonly></el-input>
                  </el-form-item>
                  <el-form-item>
                     <el-button type="primary" @click="fakeSave">更新接触信息</el-button>
                  </el-form-item>
               </el-form>
            </el-tab-pane>

            <el-tab-pane label="绩效与流水溯源" name="PERFORMANCE">
               <div style="display:flex; justify-content:space-around; margin: 30px 0;">
                  <div style="text-align:center;">
                    <div style="font-size:32px; color:#3498db; font-weight:bold;">128</div>
                    <div style="color:#7f8c8d; font-size:14px;">本月经办复核数</div>
                  </div>
                  <div style="text-align:center;">
                    <div style="font-size:32px; color:#e74c3c; font-weight:bold;">0</div>
                    <div style="color:#7f8c8d; font-size:14px;">引发预警被拦截</div>
                  </div>
                  <div style="text-align:center;">
                    <div style="font-size:32px; color:#2ecc71; font-weight:bold;">100%</div>
                    <div style="color:#7f8c8d; font-size:14px;">合规结款率</div>
                  </div>
               </div>
               <el-alert title="绩效数据将于每自然月 5 日凌晨同步系统核心库汇总。" type="info" show-icon :closable="false" />
            </el-tab-pane>

            <el-tab-pane label="安全密匙更迭" name="SECURITY">
               <el-form label-width="120px" style="max-width: 400px; margin-top:20px;">
                  <el-form-item label="旧登录口令">
                    <el-input type="password" placeholder="验证原口令" />
                  </el-form-item>
                  <el-form-item label="强校验新口令">
                    <el-input type="password" placeholder="须包含大小写特符" />
                  </el-form-item>
                  <el-form-item>
                     <el-button type="warning" @click="fakeSave">申请下发新密匙</el-button>
                  </el-form-item>
               </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const myRole = route.meta.role || 'user'
const activeTab = ref('INFO')

function fakeUpload() {
  ElMessage.warning('由于缺乏图床依赖，当前照片无法被覆盖投射到云端！')
}

function fakeSave() {
  ElMessage.success('您的安全信使请求已送达总前置！下个轮次即会生效。')
}

function saveProfile() {
  ElMessage.success('职工全案图谱暂存完毕。')
}
</script>

<style scoped>
.panel-card { border-radius: 8px; }
</style>
