<template>
  <div class="module-page admin-announce-layout">
    
    <div class="hero-header glass-card">
       <div class="hero-left">
          <h2>{{ role === 'user' ? '农信社公告大屏幕' : '全域广播阵列舱' }}</h2>
          <p>{{ role === 'user' ? '掌握最新惠农政策与业务动态更新。' : '维护系统高优制度与规则的统一发文下达，具备多维渗透投递能力。' }}</p>
       </div>
       <div class="hero-right">
          <div class="stat-circle">
             <strong>{{ records.length }}</strong>
             <span>{{ role === 'user' ? '总通告' : '已归档库' }}</span>
          </div>
          <el-button type="primary" class="neo-btn-primary publish-btn" @click="openPublish" v-if="role === 'admin'">
             <el-icon><Microphone /></el-icon> 起草新公文
          </el-button>
       </div>
    </div>

    <!-- Announcement Cards Grid -->
    <div class="announce-grid mt-4">
       <el-card v-for="item in records" :key="item.id" class="glass-card neo-card announce-card" shadow="never">
          <div class="card-top">
             <div class="card-badges">
                <el-tag v-if="item.top_flag === '是'" type="danger" effect="dark" class="badge-top">紧急置顶</el-tag>
                <el-tag type="info" effect="plain" class="badge-type">{{ item.notice_type }}</el-tag>
             </div>
             <div class="card-actions" v-if="role === 'admin'">
                <el-dropdown trigger="click">
                  <el-button link icon="MoreFilled" />
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item v-if="item.publish_status !== '已发布'" @click="fakeAction('越级推流')">强制推流</el-dropdown-item>
                      <el-dropdown-item type="danger" @click="fakeAction('物理撤稿')">阻断回收</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
             </div>
          </div>
          
          <h3 class="announce-title" @click="showContent(item)">{{ item.notice_title }}</h3>
          
          <div class="announce-meta mt-3">
             <div class="meta-item"><el-icon><Location /></el-icon> 渗透区: {{ item.visible_scope }}</div>
             <div class="meta-item"><el-icon><View /></el-icon> 触达数: <span class="text-success">{{ item.read_count }} 人</span></div>
          </div>
          
          <div class="announce-footer mt-2">
             <div class="footer-status">
               <span class="status-dot" :class="item.publish_status === '已发布' ? 'bg-green' : 'bg-gray'"></span>
               {{ item.publish_status }}
             </div>
             <div class="footer-time">{{ item.created_at }}</div>
          </div>
       </el-card>

       <!-- Empty State -->
       <div v-if="records.length === 0" class="empty-state glass-card">
          <el-empty description="公文舱内未发现任何存续条例" />
       </div>
    </div>

    <!-- Right Drawer for Powerful Editor -->
    <el-drawer v-model="publishVisible" title="高纯净度公告排版台 (Quill / TipTap 引擎模拟)" size="800px" class="editor-drawer" :close-on-click-modal="false">
       <div class="editor-container">
          <el-form label-position="top" class="neo-form">
             <!-- Header Settings -->
             <el-row :gutter="20">
               <el-col :span="16">
                 <el-form-item label="主论点定级 (Title)" class="neo-input">
                   <el-input v-model="form.notice_title" placeholder="最高规格警示: 大屏展示字号" size="large" />
                 </el-form-item>
               </el-col>
               <el-col :span="8">
                 <el-form-item label="归档类型" class="neo-input">
                   <el-select v-model="form.notice_type" size="large" style="width: 100%;">
                     <el-option label="合规业务" value="业务公告" />
                     <el-option label="系统规则" value="系统规则" />
                     <el-option label="极端熔断" value="紧急熔断" />
                   </el-select>
                 </el-form-item>
               </el-col>
             </el-row>

             <!-- Targeting Tree -->
             <el-form-item label="渗透目标拓扑网 (Audience Target)">
                <div class="target-tree-box neo-input p-3">
                   <el-radio-group v-model="form.visible_scope" class="custom-radios">
                     <el-radio-button value="全部用户">
                       <el-icon><Platform /></el-icon> 公网全向广播
                     </el-radio-button>
                     <el-radio-button value="财务人员">
                       <el-icon><Lock /></el-icon> 收线限流管控区
                     </el-radio-button>
                     <el-radio-button value="指定网点" disabled>
                       <el-icon><MapLocation /></el-icon> 特定物理节点掩护
                     </el-radio-button>
                   </el-radio-group>
                   
                   <div style="margin-top: 15px; display: flex; align-items: center; justify-content: space-between;">
                     <el-checkbox v-model="form.top_flag" true-value="是" false-value="否" border>
                       强行覆盖置顶算法
                     </el-checkbox>
                     <div class="schedule-box">
                       <el-icon><Timer /></el-icon>
                       <span class="fs-13">可搭配定时拨盘以规避审查</span>
                     </div>
                   </div>
                </div>
             </el-form-item>

             <!-- Mock Rich Text Editor -->
             <el-form-item label="正文排版器 (Visual Editor)">
                <div class="mock-quill-editor neo-input">
                   <div class="quill-toolbar">
                     <div class="toolbar-group">
                       <el-button link icon="EditPen" />
                       <el-button link icon="Picture" />
                       <el-button link icon="Link" />
                       <el-button link icon="VideoCamera" />
                     </div>
                     <div class="toolbar-group">
                       <el-button link style="font-weight:bold">B</el-button>
                       <el-button link style="font-style:italic">I</el-button>
                       <el-button link style="text-decoration:underline">U</el-button>
                     </div>
                     <el-button size="small" type="primary" plain class="preview-btn">渲染核对预览 (Markdown)</el-button>
                   </div>
                   <div class="quill-body">
                      <el-input 
                        v-model="form.notice_content" 
                        type="textarea" 
                        :rows="12" 
                        placeholder="在此处倾泻具有法律效力的制度条幅... 全局支持图床拖拽嵌入和智能高亮检查。" 
                        resize="none"
                        class="quill-textarea"
                      />
                   </div>
                </div>
             </el-form-item>
          </el-form>
          
          <div class="drawer-footer-actions">
             <el-button @click="publishVisible = false" size="large" class="neo-btn">转入暗网休整期 (存草稿)</el-button>
             <el-button type="primary" size="large" @click="fakeAction('全面推流并签名执行')" class="neo-btn-primary push-btn">
               <el-icon><Position /></el-icon> 切断审核线，立刻广域下发
             </el-button>
          </div>
       </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Microphone, MoreFilled, Location, View, Platform, Lock, MapLocation, Timer, EditPen, Picture, Link, VideoCamera, Position } from '@element-plus/icons-vue'
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
    ElMessage.error('无法加载密函信箱: ' + e)
  }
}

function openPublish() {
  form.value = { notice_title: '', notice_type: '业务公告', visible_scope: '全部用户', top_flag: '否', notice_content: '' }
  publishVisible.value = true
}

function showContent(row) {
  ElMessageBox.alert(`<div style="white-space: pre-wrap; line-height: 1.8; font-size: 14px; padding: 10px;">${row.notice_content}</div>`, 
  `公函：${row.notice_title}`, {
    dangerouslyUseHTMLString: true,
    confirmButtonText: '已知悉最高指示'
  })
}

function fakeAction(act) {
  ElMessage.success(`物理指令【${act}】拦截成功，正在接管前端流控制器！`)
  publishVisible.value = false
}

onMounted(loadData)
</script>

<style scoped>
.hero-header { display: flex; justify-content: space-between; align-items: center; padding: 30px 40px; border-radius: 20px; background: linear-gradient(135deg, var(--color-primary), var(--color-secondary)); color: #fff; box-shadow: var(--shadow-lg); }
.hero-left h2 { margin: 0 0 10px; font-size: 28px; font-weight: 800; letter-spacing: 1px; }
.hero-left p { margin: 0; font-size: 14px; opacity: 0.8; }
.hero-right { display: flex; align-items: center; gap: 30px; }
.stat-circle { display: flex; flex-direction: column; align-items: center; background: rgba(0,0,0,0.15); border-radius: 50%; width: 90px; height: 90px; justify-content: center; backdrop-filter: blur(10px); }
.stat-circle strong { font-size: 30px; font-family: 'Inter', sans-serif; font-weight: 800; }
.stat-circle span { font-size: 11px; opacity: 0.8; }
.publish-btn { height: 50px; font-size: 16px; padding: 0 25px; border-radius: 999px; background: #fff !important; color: var(--color-primary) !important; box-shadow: 0 10px 20px rgba(0,0,0,0.15); transition: all 0.3s; }
.publish-btn:hover { transform: translateY(-3px); box-shadow: 0 15px 25px rgba(0,0,0,0.2) !important; color: var(--color-primary-light) !important; }

.announce-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 20px; }
.announce-card { padding: 20px; display: flex; flex-direction: column; height: 200px; transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1); cursor: pointer; animation: slideUp 0.6s backwards; }
.announce-card:hover { transform: translateY(-5px); box-shadow: var(--shadow-md) !important; border-color: var(--color-primary-light) !important; }

@keyframes slideUp { 0% { opacity: 0; transform: translateY(30px); } 100% { opacity: 1; transform: translateY(0); } }

.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.card-badges { display: flex; gap: 8px; }
.badge-top { font-weight: 700; border: none; }
.badge-type { border: none; background: var(--color-surface-hover); font-weight: 600; }

.announce-title { font-size: 17px; margin: 0; font-weight: 700; color: var(--color-text); display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; height: 48px; }
.announce-meta { display: flex; flex-direction: column; gap: 6px; font-size: 13px; color: var(--color-text-secondary); flex: 1; }
.meta-item { display: flex; align-items: center; gap: 6px; }
.text-success { color: var(--color-success); font-weight: 600; }

.announce-footer { display: flex; justify-content: space-between; align-items: center; font-size: 12px; padding-top: 15px; border-top: 1px dashed var(--color-border); }
.footer-status { display: flex; align-items: center; gap: 6px; font-weight: 600; color: var(--color-text); }
.status-dot { width: 8px; height: 8px; border-radius: 50%; }
.bg-green { background: #34a853; box-shadow: 0 0 5px #34a853; }
.bg-gray { background: #8b99b0; }
.footer-time { color: var(--color-text-secondary); font-family: monospace; }

.empty-state { grid-column: 1 / -1; min-height: 400px; display: flex; align-items: center; justify-content: center; }

/* Editor Drawer Styles */
.editor-container { display: flex; flex-direction: column; padding: 10px 20px; height: 100%; }
.target-tree-box { border-radius: 12px; background: var(--color-surface); border: 1px solid var(--color-border); }
.custom-radios :deep(.el-radio-button__inner) { border: none !important; border-radius: 8px !important; background: var(--color-surface-hover); margin-right: 10px; font-weight: 600; box-shadow: none !important; display:flex; align-items:center; gap:6px; color: var(--color-text); }
.custom-radios :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) { background: var(--color-primary-light); color: white; }

.schedule-box { display: flex; align-items: center; gap: 6px; font-size: 13px; color: var(--color-warning); font-weight: 600; }

.mock-quill-editor { border: 1px solid var(--color-border); border-radius: 12px; overflow: hidden; background: var(--color-surface); }
.quill-toolbar { display: flex; align-items: center; justify-content: space-between; padding: 10px 15px; background: var(--color-surface-hover); border-bottom: 1px solid var(--color-border); }
.toolbar-group { display: flex; align-items: center; gap: 10px; }
.preview-btn { font-weight: 600; border-radius: 8px; }
.quill-body { padding: 0; }
.quill-textarea :deep(.el-textarea__inner) { border: none !important; box-shadow: none !important; padding: 20px; font-family: 'Inter', sans-serif; font-size: 15px; line-height: 1.6; background: transparent; }
.quill-textarea :deep(.el-textarea__inner:focus) { box-shadow: none !important; }

.drawer-footer-actions { margin-top: auto; padding-top: 20px; display: flex; gap: 15px; }
.neo-btn, .neo-btn-primary { flex: 1; border-radius: 12px; font-weight: 700; height: 50px; }
.push-btn { background: linear-gradient(135deg, #ea4335, #ff7500); border: none; box-shadow: 0 8px 20px rgba(234,67,53,0.3); font-size: 16px; display:flex; gap:8px;}
.push-btn:hover { background: linear-gradient(135deg, #c0392b, #e67e22); box-shadow: 0 10px 25px rgba(234,67,53,0.4); transform: translateY(-2px); }
</style>
