<template>
  <div class="module-page admin-msg-layout">
    
    <!-- ADMIN / FINANCE VIEW: Management Table -->
    <template v-if="role !== 'user'">
      <div class="module-topbar" style="margin-bottom: 20px;">
        <div>
          <div class="panel-title">云端智慧客服反馈枢纽</div>
          <div class="panel-subtitle">集中收拢全网实体客诉，借助 AI 辅助生成标准回执与 FAQ。</div>
        </div>
        <div class="topbar-actions">
          <el-button color="#0052cc" :dark="true" icon="MagicStick" @click="fakeBatchAction('知识库固化')" v-if="role === 'admin'">批量抽炼知识库 (FAQ)</el-button>
          <el-button plain icon="Refresh" @click="loadData">强启收信池</el-button>
        </div>
      </div>
      
      <div class="msg-grid">
        <!-- Left: Tag Cloud & Quick Filters -->
        <div class="msg-nav">
           <el-card class="glass-card panel-card no-border" shadow="never" style="height: 100%;">
              <h4 class="nav-title"><el-icon><Filter /></el-icon> 会话流漏斗</h4>
              <div class="nav-list mt-3">
                 <div class="nav-item" :class="{'nav-active': activeTab === '未处理'}" @click="activeTab='未处理'">
                    紧急收件箱 <el-badge :value="unprocessedCount" class="nav-badge" type="danger" />
                 </div>
                 <div class="nav-item" :class="{'nav-active': activeTab === '处理中'}" @click="activeTab='处理中'">
                    接管处理中
                 </div>
                 <div class="nav-item" :class="{'nav-active': activeTab === '已处理'}" @click="activeTab='已处理'">
                    闭环结档区
                 </div>
                 <div class="nav-item" :class="{'nav-active': activeTab === '全部'}" @click="activeTab='全部'">
                    全景索引库
                 </div>
              </div>

              <el-divider border-style="dashed" />

              <h4 class="nav-title"><el-icon><Discount /></el-icon> AI 探路器标签</h4>
              <div class="tag-cloud mt-3">
                 <el-tag round type="danger" effect="plain" class="cursor-tag">资金安全疑点</el-tag>
                 <el-tag round type="warning" effect="plain" class="cursor-tag">系统功能报障</el-tag>
                 <el-tag round type="primary" effect="plain" class="cursor-tag">报销流水催办</el-tag>
                 <el-tag round type="info" effect="plain" class="cursor-tag">制度细则问询</el-tag>
              </div>
           </el-card>
        </div>

        <!-- Right: Main Messages Content -->
        <div class="msg-content">
          <el-card class="glass-card panel-card" shadow="never">
            
            <el-table :data="filteredRecords" style="width: 100%;" class="custom-table neo-input" row-class-name="pointer-row">
              <!-- Expanded Chat View -->
              <el-table-column type="expand">
                <template #default="scope">
                  <div class="chat-container">
                    <div class="chat-header">
                       <strong>{{ scope.row.message_title }}</strong>
                       <el-tag size="small" :type="scope.row.message_type === '投诉' ? 'danger' : 'info'" effect="dark">{{ scope.row.message_type }}</el-tag>
                    </div>
                    
                    <div class="chat-history">
                       <div class="chat-bubble user-bubble">
                          <div class="chat-av"><el-avatar style="background:#ff9500">{{ scope.row.sender_name.charAt(0) }}</el-avatar></div>
                          <div class="chat-msg">
                             <div class="chat-meta">{{ scope.row.sender_name }} ({{ scope.row.sender_no }}) · {{ scope.row.created_at }}</div>
                             <div class="chat-txt">{{ scope.row.message_content }}</div>
                          </div>
                       </div>
                       
                       <div class="chat-bubble ai-bubble" v-if="!replyText[scope.row.id]">
                          <div class="chat-av"><el-avatar style="background:#0052cc"><el-icon><MagicStick/></el-icon></el-avatar></div>
                          <div class="chat-msg">
                             <div class="chat-meta">谛听语言大模型预判解析建议</div>
                             <div class="chat-txt ai-txt">经分析语义，该实体极有可能在咨询解决路径。建议您使用系统预设回执或将操作转交二级柜台。</div>
                             <el-button size="small" type="primary" plain class="mt-2" @click="replyText[scope.row.id] = '您好，我们已经收到您的反馈。已安排专员为您进行核对，请耐心等待！'">一键采纳填充</el-button>
                          </div>
                       </div>
                    </div>

                    <div class="chat-editor">
                       <el-input
                         v-model="replyText[scope.row.id]"
                         type="textarea"
                         :rows="3"
                         class="neo-input"
                         placeholder="在此以权限中心身份下发指导批示..."
                       />
                       <div class="editor-actions mt-3">
                          <div class="left-actions">
                             <el-button type="warning" size="small" plain icon="Collection" @click="fakeAction('转入FAQ')">淬炼为FAQ库</el-button>
                             <el-dropdown trigger="click" @command="fakeAction">
                               <el-button size="small" plain><el-icon><Connection /></el-icon> 强转下级节点</el-button>
                               <template #dropdown>
                                 <el-dropdown-menu>
                                   <el-dropdown-item command="转交机要财务组">转交机要财务组</el-dropdown-item>
                                   <el-dropdown-item command="转交安保合规组">转交安保合规组</el-dropdown-item>
                                 </el-dropdown-menu>
                               </template>
                             </el-dropdown>
                          </div>
                          <div class="right-actions">
                             <el-button type="primary" @click="fakeAction('发送回执')">发送归档回执</el-button>
                          </div>
                       </div>
                    </div>
                  </div>
                </template>
              </el-table-column>

              <el-table-column prop="message_type" label="发文类别" width="100" align="center">
                <template #default="scope">
                  <el-icon v-if="scope.row.message_type==='投诉'" color="#ea4335" size="20"><Warning /></el-icon>
                  <el-icon v-else color="#0052cc" size="20"><Service /></el-icon>
                </template>
              </el-table-column>
              <el-table-column prop="message_title" label="公文主旨提要" min-width="200" show-overflow-tooltip />
              <el-table-column prop="sender_name" label="原发体" min-width="120" />
              <el-table-column prop="process_status" label="拦截点" min-width="120" align="center">
                <template #default="scope">
                  <el-tag :type="scope.row.process_status === '未处理' ? 'danger' : 'success'" round effect="dark" style="border:none;">
                    {{ scope.row.process_status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="created_at" label="时间" min-width="150" align="center" />
              
              <el-table-column label="操作" fixed="right" width="100" align="center">
                <template #default="scope">
                  <el-button link type="danger" @click="fakeAction('信件抹除')" v-if="role === 'admin'">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <el-empty v-if="filteredRecords.length === 0" description="探照池内查无任何存留" />
          </el-card>
        </div>
      </div>
    </template>

    <!-- USER VIEW: Live Chat Interface -->
    <template v-else>
      <div class="user-chat-wrapper">
         <div class="chat-sidebar glass-card">
            <div class="sidebar-header">
               <h3>联系服务舱</h3>
               <el-button type="primary" size="small" class="neo-btn-primary" icon="Plus" @click="startNewChat">发起新会话</el-button>
            </div>
            <div class="chat-list">
               <div class="chat-sess" :class="{'sess-active': isNewChat}" @click="isNewChat=true; activeChat=null">
                 <el-avatar :size="40" style="background:#00c9a7"><el-icon><ChatDotRound /></el-icon></el-avatar>
                 <div class="sess-info">
                   <div class="sess-title">新建咨询工单</div>
                   <div class="sess-desc text-success">等待输入...</div>
                 </div>
               </div>
               
               <div class="chat-sess" v-for="item in records" :key="item.id" :class="{'sess-active': !isNewChat && activeChat?.id === item.id}" @click="selectChat(item)">
                 <el-avatar :size="40" style="background:var(--color-surface-hover); color:var(--color-text)"><el-icon><document /></el-icon></el-avatar>
                 <div class="sess-info">
                   <div class="sess-title">{{ item.message_title }}</div>
                   <div class="sess-desc">{{ item.process_status === '未处理' ? '等待客服介入' : '有新回复' }}</div>
                 </div>
                 <div class="sess-time">{{ item.created_at.substring(5, 10) }}</div>
               </div>
            </div>
         </div>

         <div class="chat-main glass-card">
            <!-- Chat Window: New -->
            <div class="chat-window" v-if="isNewChat">
               <div class="chat-win-header">
                  <div>
                    <strong>发起新反馈工单</strong>
                    <p style="margin:0; font-size:12px; color:var(--color-text-secondary);"><span class="pulse-dot"></span>当前人工座席在线，预计 3 分钟内应答</p>
                  </div>
               </div>
               
               <div class="chat-win-body empty-body">
                  <el-icon :size="60" color="rgba(0,82,204,0.2)"><Service /></el-icon>
                  <p style="color:var(--color-text-secondary); margin-top:20px;">请在下方描述您遇到的问题，我们将尽快处理。</p>
               </div>
               
               <div class="chat-win-footer">
                  <div class="neo-form p-3" style="border-top:1px solid var(--color-border);">
                     <el-input v-model="newMsgForm.title" placeholder="一句话描述问题简明概要..." class="neo-input mb-3"/>
                     <div class="d-flex" style="gap:10px;">
                        <el-select v-model="newMsgForm.type" class="neo-input" style="width:120px;">
                          <el-option label="普通咨询" value="咨询" />
                          <el-option label="紧急投诉" value="投诉" />
                        </el-select>
                        <el-input v-model="newMsgForm.content" placeholder="输入具体细节。支持上传附件（模拟）..." class="neo-input flex-1" @keyup.enter="submitNewMsg" />
                        <el-button type="primary" class="neo-btn-primary" icon="Position" @click="submitNewMsg"></el-button>
                     </div>
                  </div>
               </div>
            </div>

            <!-- Chat Window: Existing -->
            <div class="chat-window" v-else-if="activeChat">
               <div class="chat-win-header">
                  <div>
                    <strong>{{ activeChat.message_title }}</strong>
                    <p style="margin:0; font-size:12px; color:var(--color-text-secondary);">单号: {{ activeChat.id || 'SYS-0X1' }} · 状态: {{ activeChat.process_status }}</p>
                  </div>
                  <el-tag :type="activeChat.message_type === '投诉' ? 'danger' : 'info'" effect="plain">{{ activeChat.message_type }}</el-tag>
               </div>
               
               <div class="chat-win-body">
                  <div class="msg-bubble author">
                     <div class="msg-contentbox">
                        <div class="msg-txt">{{ activeChat.message_content }}</div>
                        <div class="msg-time">{{ activeChat.created_at }}</div>
                     </div>
                     <el-avatar :size="36" style="background:#0052cc">我</el-avatar>
                  </div>
                  
                  <div class="msg-bubble system" v-if="activeChat.process_status !== '未处理'">
                     <el-avatar :size="36" style="background:#00c9a7"><el-icon><Service/></el-icon></el-avatar>
                     <div class="msg-contentbox">
                        <div class="msg-txt">您好，您的反馈我们已经收到。已加急催办核实，请留意资金账户变动或再次主动追问。</div>
                        <div class="msg-time">刚刚</div>
                     </div>
                  </div>
                  <div class="msg-bubble system" v-else>
                     <el-avatar :size="36" style="background:#e1e7f0; color:#172b4d"><el-icon><Loading/></el-icon></el-avatar>
                     <div class="msg-contentbox" style="background:transparent; border:none; box-shadow:none;">
                        <div class="msg-txt" style="color:var(--color-text-secondary);">客服调度中，请稍作等待...</div>
                     </div>
                  </div>
               </div>
               
               <div class="chat-win-footer">
                  <div class="neo-form p-3" style="border-top:1px solid var(--color-border); display:flex; gap:10px;">
                     <el-input v-model="followUpText" placeholder="继续追问..." class="neo-input flex-1" @keyup.enter="sendFollowUp" />
                     <el-button type="primary" class="neo-btn-primary" icon="Position" @click="sendFollowUp"></el-button>
                  </div>
               </div>
            </div>
         </div>
      </div>
    </template>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Filter, Discount, MagicStick, ChatDotRound, Warning, Service, Collection, Connection, Refresh, Plus, Document, Position, Loading } from '@element-plus/icons-vue'
import http from '../api'

const route = useRoute()
const role = route.meta.role || 'admin'
const records = ref([])

// Admin / Finance
const activeTab = ref('未处理')
const replyText = ref({})

// User
const isNewChat = ref(true)
const activeChat = ref(null)
const newMsgForm = ref({ title: '', content: '', type: '咨询' })
const followUpText = ref('')

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
    records.value = res.data.records ? res.data.records.reverse() : []
    
    if (role === 'user' && records.value.length > 0 && isNewChat.value) {
       // Optionally auto select first
    }
  } catch (e) {
    ElMessage.error('信箱接口遭受污染: ' + e)
  }
}

// Admin Fake Actions
function fakeAction(act) {
  ElMessage.success(`动作指令 [${act}] 已抵达前端控制塔，模拟阻断成功。`)
}

function fakeBatchAction(act) {
  ElMessage.warning(`触发大盘批量操作 [${act}]，系统计算缓存暂未落地，请依赖下个工程周期支撑。`)
}

// User Actions
function startNewChat() {
  isNewChat.value = true
  activeChat.value = null
  newMsgForm.value = { title: '', content: '', type: '咨询' }
}

function selectChat(item) {
  isNewChat.value = false
  activeChat.value = item
  followUpText.value = ''
}

async function submitNewMsg() {
  if (!newMsgForm.value.title || !newMsgForm.value.content) return ElMessage.warning('工单要素并不完整！')
  try {
     await http.post(`/user/message/submit`, newMsgForm.value)
     ElMessage.success('信标已抛射。客服阵营已响铃。')
     loadData()
     newMsgForm.value = { title: '', content: '', type: '咨询' }
     // Dummy jump to view
     setTimeout(() => {
       if (records.value.length > 0) selectChat(records.value[0])
     }, 400)
  } catch(e) {
     ElMessage.success('工单投递成功 (模拟本地链路)！')
     loadData()
  }
}

function sendFollowUp() {
  if (!followUpText.value) return
  ElMessage.success('追问已附加在会话流中。(模拟)')
  followUpText.value = ''
}

onMounted(loadData)
</script>

<style scoped>
.admin-msg-layout { display: flex; flex-direction: column; min-height: calc(100vh - 120px); }

/* ADMIN/FINANCE GRID */
.msg-grid { display: grid; grid-template-columns: 250px 1fr; gap: 24px; flex: 1; align-items: start; }

.no-border { border:none !important; }
.nav-title { font-size: 14px; font-weight: 700; color: var(--color-text-secondary); margin: 0; display:flex; align-items:center; gap:6px; }
.nav-list { display: flex; flex-direction: column; gap: 8px; }
.nav-item { padding: 12px 15px; border-radius: 12px; font-size: 14px; font-weight: 600; cursor: pointer; color: var(--color-text); transition: all 0.2s; display:flex; justify-content:space-between; align-items:center; }
.nav-item:hover { background: var(--color-surface-hover); }
.nav-active { background: rgba(0,82,204,0.1) !important; color: var(--color-primary) !important; }

.tag-cloud { display: flex; flex-wrap: wrap; gap: 10px; }
.cursor-tag { cursor: pointer; border: none; font-weight: 600; padding: 0 12px; }

.custom-table { --el-table-header-bg-color: var(--color-surface-hover); border-radius: 12px; overflow: hidden; }

/* Chat UI in Expand Row */
.chat-container { padding: 15px 30px; background: var(--color-surface-hover); border-radius: 16px; margin: 10px 20px; box-shadow: inset 0 2px 10px rgba(0,0,0,0.02); }
.chat-header { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; font-size: 16px; }

.chat-history { display: flex; flex-direction: column; gap: 20px; margin-bottom: 30px; }
.chat-bubble { display: flex; gap: 15px; }
.chat-av { flex-shrink: 0; }
.chat-msg { background: var(--color-surface); padding: 15px 20px; border-radius: 20px; border-top-left-radius: 0; box-shadow: var(--shadow-sm); max-width: 85%; }
.chat-meta { font-size: 12px; color: var(--color-text-secondary); margin-bottom: 8px; font-weight: 600; }
.chat-txt { font-size: 14px; line-height: 1.6; color: var(--color-text); }

.ai-bubble .chat-msg { background: rgba(0,82,204,0.05); border: 1px solid rgba(0,82,204,0.1); }
.ai-txt { color: var(--color-primary-variant); }

.chat-editor { background: var(--color-surface); padding: 20px; border-radius: 16px; box-shadow: var(--shadow-neo); }
.editor-actions { display: flex; justify-content: space-between; align-items: center; }
.left-actions { display: flex; gap: 10px; }

/* USER CHAT WRAPPER */
.user-chat-wrapper { display: flex; height: calc(100vh - 160px); max-height: 800px; max-width: 1200px; margin: 0 auto; gap: 20px; }

.chat-sidebar { width: 320px; display: flex; flex-direction: column; border-radius: 20px; border: 1px solid var(--color-border); overflow: hidden; }
.sidebar-header { padding: 20px; border-bottom: 1px solid var(--color-border); display: flex; justify-content: space-between; align-items: center; background: var(--color-surface); }
.sidebar-header h3 { margin: 0; font-size: 18px; font-weight: 800; color: var(--color-text); }

.chat-list { flex: 1; overflow-y: auto; display: flex; flex-direction: column; background: var(--color-surface-hover); }
.chat-sess { display: flex; align-items: center; gap: 15px; padding: 15px 20px; cursor: pointer; transition: background 0.3s; position: relative; border-bottom: 1px solid var(--color-border); }
.chat-sess:hover { background: rgba(0,82,204,0.05); }
.sess-active { background: var(--color-surface) !important; box-shadow: -2px 0 0 inset var(--color-primary); }
.sess-info { flex: 1; min-width: 0; }
.sess-title { font-weight: 700; font-size: 14px; color: var(--color-text); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 4px;}
.sess-desc { font-size: 12px; color: var(--color-text-secondary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis;}
.sess-time { font-size: 11px; color: var(--color-text-secondary); white-space: nowrap; }

.chat-main { flex: 1; display: flex; flex-direction: column; border-radius: 20px; border: 1px solid var(--color-border); overflow: hidden; background: var(--color-surface); }

.chat-win-header { padding: 20px 25px; border-bottom: 1px solid var(--color-border); display: flex; justify-content: space-between; align-items: center; }
.chat-win-header strong { font-size: 18px; color: var(--color-text); }
.pulse-dot { display: inline-block; width: 8px; height: 8px; background: #34a853; border-radius: 50%; margin-right: 6px; animation: pulse 2s infinite; }

.chat-win-body { flex: 1; overflow-y: auto; padding: 25px; display: flex; flex-direction: column; gap: 20px; background: rgba(0, 82, 204, 0.02); }
.empty-body { align-items: center; justify-content: center; }

.msg-bubble { display: flex; gap: 15px; width: 100%; align-items: flex-end; }
.msg-bubble.author { justify-content: flex-end; }

.msg-contentbox { background: var(--color-surface); padding: 12px 18px; border-radius: 16px; box-shadow: var(--shadow-sm); max-width: 70%; position: relative; }
.msg-bubble.author .msg-contentbox { background: var(--color-primary); color: white; border-bottom-right-radius: 4px; }
.msg-bubble.system .msg-contentbox { border-bottom-left-radius: 4px; border: 1px solid var(--color-border); }

.msg-txt { font-size: 14px; line-height: 1.6; }
.msg-time { font-size: 11px; margin-top: 6px; opacity: 0.7; text-align: right;}

.chat-win-footer { background: var(--color-surface); }
.d-flex { display: flex; align-items: center; }
.flex-1 { flex: 1; }
.mb-3 { margin-bottom: 15px; }

@keyframes pulse { 0% { box-shadow: 0 0 0 0 rgba(52,168,83,0.4); } 70% { box-shadow: 0 0 0 6px rgba(52,168,83,0); } 100% { box-shadow: 0 0 0 0 rgba(52,168,83,0); } }

@media (max-width: 900px) {
  .msg-grid { grid-template-columns: 1fr; }
  .user-chat-wrapper { flex-direction: column; }
  .chat-sidebar { width: 100%; height: 300px; flex: none; }
}
</style>
