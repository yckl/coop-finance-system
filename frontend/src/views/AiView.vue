<template>
  <div class="module-page ai-page">
    
    <div class="ai-layout glass-card">
       <!-- Left Sidebar: Capabilities & Chips -->
       <div class="ai-sidebar">
          <div class="sidebar-header">
             <el-icon :size="24" color="#0052cc"><Cpu /></el-icon>
             <h3 class="m-0">谛听大模型矩阵</h3>
             <el-tag size="small" type="success" effect="dark" round>v4.0.0-Turbo</el-tag>
          </div>
          
          <div class="sidebar-section mt-4 pt-3">
             <h4 class="sec-title"><el-icon><Lightning /></el-icon> 核心赋能场景</h4>
             <div class="cap-list">
                <div v-for="cap in capabilities" :key="cap" class="cap-item">
                   <el-icon color="#00c9a7"><Check /></el-icon> {{ cap }}
                </div>
             </div>
          </div>

          <div class="sidebar-section mt-4 pt-3">
             <h4 class="sec-title"><el-icon><ChatDotRound /></el-icon> 高频 Prompt 预设胶囊</h4>
             <div class="preset-list">
               <div class="preset-chip" v-for="(preset, index) in presets" :key="index" @click="fillPreset(preset.text)">
                 {{ preset.label }}
               </div>
             </div>
          </div>
       </div>

       <!-- Right Main: Chat UI -->
       <div class="ai-chat-area">
          <div class="chat-header print-hide">
            <div>
               <strong style="font-size:16px;">AI 对话控制台</strong>
               <span class="ms-2" style="font-size:12px; color:var(--color-success);"><span class="pulse-dot"></span>模型状态: 在线 (延迟 12ms)</span>
            </div>
            <el-button link icon="Delete" @click="clearHistory">清空上下文</el-button>
          </div>
          
          <div class="chat-flow-box" ref="flowBox">
             <el-empty v-if="history.length===0" description="试着提问些什么吧，比如：分析最近的资金流转？" :image-size="80" />
             
             <div class="chat-flow">
               <!-- Reverse iteration because history is unshifted in logic (we'll fix it to append for better flow) -->
               <div v-for="(item, index) in history" :key="index" class="msg-pair">
                  <!-- User -->
                  <div class="m-bubble m-user">
                     <div class="m-content">{{ item.question }}</div>
                     <el-avatar class="m-av" style="background:#0052cc">{{ role.charAt(0).toUpperCase() }}</el-avatar>
                  </div>
                  <!-- AI -->
                  <div class="m-bubble m-ai">
                     <el-avatar class="m-av" style="background:#0a1128"><el-icon><Cpu/></el-icon></el-avatar>
                     <div class="m-content ai-md-content">
                        <!-- Mocking Markdown Rendering -->
                        <div v-html="formatMockMarkdown(item.answer)"></div>
                        
                        <div class="m-actions mt-2">
                           <el-tooltip content="将内容复制到剪贴板">
                              <el-button link size="small" @click="mockCopy"><el-icon><CopyDocument /></el-icon> 拷贝策略</el-button>
                           </el-tooltip>
                        </div>
                     </div>
                  </div>
               </div>
             </div>
          </div>

          <!-- Bottom Input Area -->
          <div class="chat-input-area">
             <div class="input-wrapper neo-input">
                <el-input 
                  v-model="question" 
                  type="textarea" 
                  :rows="3" 
                  :placeholder="inputPlaceholder" 
                  resize="none"
                  @keydown.enter.prevent="ask"
                />
                <div class="input-controls">
                   <!-- Web Speech API Mock -->
                   <div class="mic-btn" :class="{'is-recording': isRecording}" @mousedown="startVoice" @mouseup="stopVoice" @mouseleave="stopVoice">
                      <el-icon><Microphone /></el-icon>
                   </div>
                   <el-button type="primary" class="neo-btn-primary" icon="Position" @click="ask" :loading="isThinking" circle size="large"></el-button>
                </div>
             </div>
             <div class="input-tip text-center mt-2">按住 <el-icon><Microphone/></el-icon> 语音输入 · <kbd>Shift</kbd> + <kbd>Enter</kbd> 换行 · <kbd>Enter</kbd> 发送预测</div>
          </div>
       </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Cpu, Lightning, Check, ChatDotRound, Delete, CopyDocument, Microphone, Position } from '@element-plus/icons-vue'
import http from '../api'

const route = useRoute()
const role = route.meta.role || 'user'
const history = ref([])

const defaultQuestion = role === 'user' ? '帮我解释一下我最近一笔取款的含义，账户目前安全吗？' : '请基于最新各项表单数据，为下周股东大会草拟经营摘要。'
const question = ref(defaultQuestion)
const isThinking = ref(false)
const isRecording = ref(false)
const flowBox = ref(null)

const inputPlaceholder = computed(() => {
  return role === 'user' ? '请输入财务疑惑，例如：为什么会有手续费？ / 去哪里报销最快？' : '请输入业务咨询问题，例如：最近异常提现规律 / 生成上月结余简报'
})

const presets = computed(() => {
  if (role === 'user') {
    return [
      { text: '我的余额为什么变化了？', label: '资金变化疑惑' },
      { text: '帮我解释这条异常报销为什么遭驳回', label: '报销诊断' },
      { text: '最新的一份公告和我有关吗？', label: '制度通晓' }
    ]
  }
  return [
    { text: '请生成最新存款周期净流出风险排查大纲', label: '风险排查' },
    { text: '辅助解释近期大量差旅农资报销背后的审计盲点', label: '报销审计拆解' },
    { text: '帮我草拟一份面向全网点下发的“休眠账户冻结新规”通知', label: '制度与公文推演' }
  ]
})

const capabilities = computed(() => {
  if (role === 'user') return ['自然语言解读流水记录', '智能报销驳回在线答疑', '资金异常风险提早预警', '网点公告摘要速读与转化']
  return ['多维结构化报表矩阵解释', '金融周期净流入波动总结', '识别异常提现与红线预警', '辅助生成管理层内部中文简报']
})

function fillPreset(value) {
  question.value = value
  ask()
}

function startVoice() { isRecording.value = true }
function stopVoice() { 
  if(!isRecording.value) return
  isRecording.value = false
  ElMessage.success('语音模拟捕获：转译文本就绪。')
}

// Very basic Markdown mock for bullet points and bold text
function formatMockMarkdown(text) {
  if(!text) return ''
  let html = text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/\n\s*-\s(.*)/g, '<li>$1</li>')
  if(html.includes('<li>')) html = `<ul style="padding-left:20px;margin-top:10px;">${html}</ul>`
  html = html.replace(/\n/g, '<br>')
  return html
}

function mockCopy() {
  ElMessage.success('已物理拷贝大模型答温至剪贴板栈。')
}

function clearHistory() {
  history.value = []
}

async function ask() {
  if(!question.value.trim() || isThinking.value) return
  
  const qText = question.value
  question.value = ''
  isThinking.value = true
  
  // Push user question immediately
  history.value.push({ question: qText, answer: '' })
  scrollToBottom()

  try {
    const result = await http.post(`/ai/${role}`, { question: qText })
    history.value[history.value.length - 1].answer = result.data.answer
  } catch (error) {
    history.value[history.value.length - 1].answer = `*网络连接通信故障: ${error}*`
  } finally {
    isThinking.value = false
    scrollToBottom()
  }
}

function scrollToBottom() {
  nextTick(() => {
    if(flowBox.value) flowBox.value.scrollTop = flowBox.value.scrollHeight
  })
}

// Initial ask
setTimeout(ask, 300)
</script>

<style scoped>
.ai-page { display: flex; flex-direction: column; min-height: calc(100vh - 120px); }

.ai-layout { flex: 1; display: grid; grid-template-columns: 280px 1fr; border-radius: 20px; overflow: hidden; border: 1px solid var(--color-border); }

/* Left Sidebar */
.ai-sidebar { background: var(--color-surface-hover); padding: 25px; border-right: 1px dashed var(--color-border); }
.sidebar-header { display: flex; align-items: center; gap: 10px; margin-bottom: 30px; font-weight: 800; font-size:18px;}

.sec-title { font-size: 13px; font-weight: 700; color: var(--color-text-secondary); margin: 0 0 15px; display:flex; align-items:center; gap:6px; border-bottom:1px dashed var(--color-border); padding-bottom:10px;}

.cap-list { display: flex; flex-direction: column; gap: 10px; }
.cap-item { font-size: 13px; color: var(--color-text); display:flex; align-items:flex-start; gap:8px; line-height: 1.4; }

.preset-list { display: flex; flex-direction: column; gap: 10px; }
.preset-chip { background: var(--color-surface); border: 1px solid var(--color-border); padding: 10px 15px; border-radius: 8px; font-size: 13px; cursor: pointer; transition: all 0.2s; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; font-weight: 600; }
.preset-chip:hover { border-color: var(--color-primary); color: var(--color-primary); transform: translateX(5px); }

/* Right Chat Area */
.ai-chat-area { display: flex; flex-direction: column; background: var(--color-surface); }
.chat-header { padding: 15px 25px; border-bottom: 1px dashed var(--color-border); display:flex; justify-content:space-between; align-items:center; }
.pulse-dot { display:inline-block; width:8px; height:8px; background:var(--color-success); border-radius:50%; margin-right:5px; box-shadow:0 0 8px var(--color-success); }

.chat-flow-box { flex: 1; padding: 30px; overflow-y: auto; scroll-behavior: smooth; }
.chat-flow { display: flex; flex-direction: column; gap: 30px; }

.m-bubble { display: flex; gap: 15px; max-width: 85%; }
.m-av { flex-shrink: 0; }
.m-user { align-self: flex-end; flex-direction: row-reverse; }
.m-user .m-content { background: var(--color-primary); color: white; border-radius: 16px; border-top-right-radius: 0; padding: 15px 20px; font-size: 14px; line-height: 1.5; box-shadow: 0 4px 10px rgba(0,82,204,0.3); }

.m-ai { align-self: flex-start; }
.m-ai .m-content { background: rgba(0,0,0,0.03); color: var(--color-text); border-radius: 16px; border-top-left-radius: 0; padding: 20px; font-size: 14.5px; line-height: 1.7; box-shadow: var(--shadow-sm); border: 1px solid var(--color-border); min-width: 200px; }
.ai-md-content :deep(strong) { color: var(--color-primary); }
.ai-md-content :deep(li) { margin-bottom: 8px; }

/* Input */
.chat-input-area { padding: 20px 30px; background: var(--color-surface-hover); }
.input-wrapper { display: flex; background: var(--color-surface); border-radius: 16px; border: 1px solid var(--color-border); padding: 5px; transition: all 0.3s; }
.input-wrapper:focus-within { border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(0,82,204,0.1); }
.input-wrapper :deep(.el-textarea__inner) { border: none !important; box-shadow: none !important; background: transparent; padding: 15px; font-size: 15px;}

.input-controls { display: flex; align-items: flex-end; gap: 10px; padding: 10px; }
.mic-btn { width: 40px; height: 40px; border-radius: 50%; display: flex; justify-content: center; align-items: center; cursor: pointer; color: var(--color-text-secondary); transition: all 0.3s; background: rgba(0,0,0,0.05); }
.mic-btn:hover { background: rgba(0,0,0,0.1); }
.is-recording { background: var(--color-danger); color: white; box-shadow: 0 0 0 5px rgba(234,67,53,0.3); animation: micPulse 1.5s infinite; }
@keyframes micPulse { 0% { box-shadow: 0 0 0 0 rgba(234,67,53,0.5); } 70% { box-shadow: 0 0 0 15px rgba(234,67,53,0); } 100% { box-shadow: 0 0 0 0 rgba(234,67,53,0); } }

.input-tip { font-size: 11px; color: var(--color-text-secondary); }
kbd { background: var(--color-surface); padding: 2px 6px; border-radius: 4px; border: 1px solid var(--color-border); font-family: monospace; font-size: 10px; }

@media(max-width:1000px){
  .ai-layout { grid-template-columns: 1fr; }
  .ai-sidebar { display: none; }
  .m-bubble { max-width: 95%; }
}
</style>
