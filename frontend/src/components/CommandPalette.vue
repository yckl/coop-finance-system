<template>
  <el-dialog 
    v-model="searchStore.isSearchOpen" 
    title="全域探测引擎 (⌘+K / Ctrl+K)" 
    width="650px" 
    align-center
    class="cmd-dialog"
    :show-close="false"
  >
    <el-input 
      v-model="query" 
      size="large" 
      placeholder="输入提款人、金额、交易号或功能名称..."
      clearable 
      class="neo-search"
      @input="onSearch"
    >
      <template #prefix>
        <el-icon size="20" color="var(--color-primary)"><Search /></el-icon>
      </template>
    </el-input>
    
    <div class="result-box mt-4">
       <el-empty v-if="results.length === 0 && query" description="探针游离，未发现匹配目标" :image-size="60" />
       
       <transition-group name="stagger-list" tag="div" class="r-list">
         <div 
            v-for="item in results" 
            :key="item.item.id" 
            class="r-item" 
            @click="jump(item.item.path)"
         >
            <div class="r-icon"><el-icon><component :is="item.item.icon" /></el-icon></div>
            <div class="r-info">
               <div class="r-title">{{ item.item.title }} <span class="r-path">{{ item.item.path }}</span></div>
               <div class="r-desc">{{ item.item.desc }}</div>
            </div>
            <el-tag size="small" type="info" effect="plain" style="border:none;">{{ item.item.type }}</el-tag>
         </div>
       </transition-group>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { Search, Warning, Document, Position, Wallet } from '@element-plus/icons-vue';
import Fuse from 'fuse.js';
import { useSearchStore } from '../stores/useSearchStore';

const searchStore = useSearchStore();
const router = useRouter();
const query = ref('');
const results = ref([]);

const searchIndex = [
  { id: 1, title: '大额取款预警监测', desc: '财务核心拦截监控与资金追踪', path: '/finance/withdrawals', type: '风险', icon: 'Warning' },
  { id: 2, title: '农资采买与基建报销工单', desc: '差旅与基础建设款核拨', path: '/user/reimbursements', type: '工单', icon: 'Document' },
  { id: 3, title: '发布应急公文与制度', desc: '紧急操作下发至全网支行', path: '/admin/announcements', type: '权限', icon: 'Position' },
  { id: 4, title: '账户体系状态大盘', desc: '全部储户资产快照查询', path: '/finance/balances', type: '账目', icon: 'Wallet' },
  { id: 5, title: '用户权限大盘', desc: '对所有储户做控制', path: '/admin/users', type: '用户', icon: 'User' }
];

const fuse = new Fuse(searchIndex, { 
  keys: ['title', 'desc', 'type'], 
  threshold: 0.4
});

const onSearch = () => {
   if (!query.value) results.value = [];
   else results.value = fuse.search(query.value);
};

const jump = (path) => {
   searchStore.toggleSearch();
   query.value = '';
   results.value = [];
   router.push(path);
};

const handleKeydown = (e) => {
  if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
    e.preventDefault();
    searchStore.toggleSearch();
  }
};

onMounted(() => document.addEventListener('keydown', handleKeydown));
onUnmounted(() => document.removeEventListener('keydown', handleKeydown));
</script>

<style scoped>
.cmd-dialog { border-radius: 20px; background: rgba(var(--color-surface), 0.85); backdrop-filter: blur(25px); border: 1px solid var(--color-border); }
.neo-search :deep(.el-input__wrapper) { box-shadow: none !important; border-bottom: 2px solid var(--color-border); border-radius: 0; background: transparent; padding: 20px 0 10px; font-size: 18px;}
.neo-search :deep(.el-input__wrapper.is-focus) { border-bottom-color: var(--color-primary); }

.r-list { max-height: 350px; overflow-y: auto; padding-right: 5px; }
.r-item { display: flex; align-items: center; padding: 16px; border-radius: 12px; cursor: pointer; gap: 15px; margin-bottom: 10px; background: var(--color-surface); border: 1px solid var(--color-border); transition: all 0.3s;}
.r-item:hover { transform: translateX(5px); border-color: var(--color-primary-light); background: rgba(0,82,204,0.03); }
.r-icon { background: rgba(0,82,204,0.1); color: var(--color-primary); width:40px; height:40px; display:flex; align-items:center; justify-content:center; border-radius: 10px; font-size: 20px;}
.r-title { font-weight: 800; color: var(--color-text); font-size: 15px;}
.r-path { font-size:11px; color:var(--color-text-secondary); margin-left:8px; font-family:monospace; }
.r-desc { font-size: 12px; color: var(--color-text-secondary); margin-top: 4px; }
.r-info { flex: 1; }

.stagger-list-enter-active { transition: all 0.4s cubic-bezier(0.2, 0.8, 0.2, 1); }
.stagger-list-enter-from { opacity: 0; transform: translateY(15px); }
</style>
