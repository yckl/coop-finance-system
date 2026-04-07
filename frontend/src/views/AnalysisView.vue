<template>
  <div class="module-page">
    <div class="module-topbar">
      <div>
        <div class="panel-title">深度洞察分析中心 (Deep Analysis)</div>
        <div class="panel-subtitle">穿透底层业务数据，提供高分辨率图谱分析与报表导出。</div>
      </div>
      <div class="topbar-actions">
        <el-button type="warning" class="neo-btn" icon="Download" @click="fakeExportPPT">一键提取管理层 PPT 简报</el-button>
        <el-button color="#0052cc" :dark="true" icon="Refresh" @click="loadData">强制刷新底层源</el-button>
      </div>
    </div>
    
    <el-card class="glass-card panel-card" shadow="never" style="min-height: 700px; position:relative;">
       <!-- Magic AI Floater -->
       <el-tooltip content="谛听大模型智能数据解译" placement="left">
         <div class="ai-floater" @click="summonAI"><el-icon><MagicStick /></el-icon></div>
       </el-tooltip>
       
       <el-tabs v-model="activeTab" class="neo-tabs" @tab-change="handleTabChange">
         <el-tab-pane label="全周期资金趋势走势" name="trend">
            <h3 class="tab-h3"><el-icon><TrendCharts /></el-icon> 资金蓄水池流变监测器</h3>
            <p class="tab-desc">已启用无级滑移漫游算法 (DataZoom)，您可以通过滚轮或拖拽选取任意时间窗口期进行切片显微。</p>
            <div ref="trendChartRef" class="chart-box" style="height: 500px;"></div>
         </el-tab-pane>
         
         <el-tab-pane label="核心资源分布热力" name="heatmap">
            <h3 class="tab-h3"><el-icon><Grid /></el-icon> 高危/高密业务时段热辐射图</h3>
            <p class="tab-desc">深色区块代表该时段内吞吐业务量激增，利于精准调配柜员人力资源。</p>
            <div ref="heapmapRef" class="chart-box" style="height: 500px;"></div>
         </el-tab-pane>
         
         <el-tab-pane label="行部网点业务琅琊榜" name="rank">
            <h3 class="tab-h3"><el-icon><Trophy /></el-icon> 横向网点 KPI 极地大乱斗</h3>
            <p class="tab-desc">拉取各地区信用网点的日内考核指标排行，自动剥离异常指标。</p>
            <div ref="rankRef" class="chart-box" style="height: 500px;"></div>
         </el-tab-pane>
       </el-tabs>
    </el-card>

    <!-- AI Dialog -->
    <el-dialog v-model="aiVisible" title="🤖 智能分析域" width="600px" style="border-radius: 20px;">
       <div class="ai-chat-body">
         <div class="ai-msg">
            正在分析[{{ activeTab === 'trend' ? '资金趋势' : (activeTab === 'heatmap' ? '时段热力' : '网点排行') }}]图谱的底层点阵数据...
         </div>
         <div class="ai-msg final-msg mt-3">
            <strong>核心洞察已生成：</strong> <br><br>
            <span v-if="activeTab === 'trend'">资金流入曲率在昨日下午14:00发生极速拉升，这与本地拆迁款下拨时间点完全吻合。建议增设结构性存款理财通道。</span>
            <span v-if="activeTab === 'heatmap'">每周四周五下午的业务发生频率呈高热状态，属于业务洪峰期。柜员缺口约 15%。建议在此时间段开启备用窗口并挂载临时排班。</span>
            <span v-if="activeTab === 'rank'">中心北区网点揽储遥遥领先（超出次席 45%），但其网点内部风险警报也激增。需派出飞行审查小组核查虚假拉存舞弊行为。</span>
         </div>
       </div>
       <template #footer>
         <el-button @click="aiVisible = false">关闭</el-button>
         <el-button type="primary" @click="aiVisible = false">生成正式决策文档导出</el-button>
       </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { MagicStick, TrendCharts, Grid, Trophy } from '@element-plus/icons-vue'

const activeTab = ref('trend')
const aiVisible = ref(false)

const trendChartRef = ref(null)
const heapmapRef = ref(null)
const rankRef = ref(null)

let chartInstances = {}

const isDark = () => document.documentElement.getAttribute('data-theme') === 'dark'

function summonAI() {
  aiVisible.value = true
}

function loadData() {
  window.location.reload()
}

function fakeExportPPT() {
  import('element-plus').then(({ ElLoading, ElMessage }) => {
    const loading = ElLoading.service({ text: '正在截取高分辨率星图并转换...', background: 'rgba(0,0,0,0.8)' })
    setTimeout(() => {
      loading.close()
      ElMessage.success('【分析简报.pptx】已成功降落在您的物理下载托盘中。')
    }, 1500)
  })
}

function handleTabChange() {
  nextTick(renderActiveChart)
}

function renderActiveChart() {
  setTimeout(() => {
    if(activeTab.value === 'trend') renderTrend()
    else if(activeTab.value === 'heatmap') renderHeatmap()
    else if(activeTab.value === 'rank') renderRank()
  }, 100)
}

function getEchartsTheme() {
  return isDark() ? 'dark' : 'light'
}

function getTextColor() { return isDark() ? '#e1e7f0' : '#1f2a44' }

function renderTrend() {
  if(!trendChartRef.value) return
  if(chartInstances['trend']) chartInstances['trend'].dispose()
  const chart = echarts.init(trendChartRef.value)
  
  let base = +new Date(2026, 3, 1)
  let oneDay = 24 * 3600 * 1000
  let data = [[base, Math.random() * 300]]
  for (let i = 1; i < 90; i++) {
    let now = new Date(base += oneDay)
    data.push([
      [now.getFullYear(), now.getMonth() + 1, now.getDate()].join('/'),
      Math.abs(Math.round((Math.random() - 0.4) * 20 + data[i - 1][1]))
    ])
  }

  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', position: function (pt) { return [pt[0], '10%']; } },
    title: { text: '资金流速显微波动图', textStyle: {color: getTextColor()} },
    toolbox: { feature: { dataZoom: { yAxisIndex: 'none' }, restore: {}, saveAsImage: { name: '资金流图', pixelRatio: 2 } }, iconStyle: { borderColor: '#0052cc' } },
    xAxis: { type: 'category', boundaryGap: false, axisLabel: { color: getTextColor() } },
    yAxis: { type: 'value', boundaryGap: [0, '100%'], splitLine: { lineStyle: { color: 'rgba(0,136,255,0.1)' } }, axisLabel: { color: getTextColor() } },
    dataZoom: [ { type: 'inside', start: 0, end: 100 }, { start: 0, end: 100 } ],
    series: [
      {
        name: '流水数据', type: 'line', symbol: 'none', sampling: 'lttb',
        itemStyle: { color: '#0052cc' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0,82,204,0.8)' },
            { offset: 1, color: 'rgba(0,82,204,0.1)' }
          ])
        },
        data: data.map(i => i[1])
      }
    ]
  })
  chartInstances['trend'] = chart
}

function renderHeatmap() {
  if(!heapmapRef.value) return
  if(chartInstances['heatmap']) chartInstances['heatmap'].dispose()
  const chart = echarts.init(heapmapRef.value)
  
  const hours = ['12a', '1a', '2a', '3a', '4a', '5a', '6a','7a','8a','9a','10a','11a','12p','1p','2p','3p','4p','5p','6p','7p','8p','9p','10p','11p']
  const days = ['Sat', 'Fri', 'Thu', 'Wed', 'Tue', 'Mon', 'Sun']
  const data = []
  for(let i=0; i<7; i++) {
     for(let j=0; j<24; j++) {
        let val = Math.floor(Math.random()*10);
        if(j >= 9 && j <= 17) val += Math.floor(Math.random()*20); // working hours high activity
        data.push([j, i, val])
     }
  }

  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: { position: 'top' },
    toolbox: { feature: { saveAsImage: { name: '热力图', pixelRatio: 2 } }, iconStyle: { borderColor: '#0052cc' } },
    grid: { height: '50%', top: '10%' },
    xAxis: { type: 'category', data: hours, splitArea: { show: true }, axisLabel: { color: getTextColor() } },
    yAxis: { type: 'category', data: days, splitArea: { show: true }, axisLabel: { color: getTextColor() } },
    visualMap: { min: 0, max: 30, calculable: true, orient: 'horizontal', left: 'center', bottom: '15%', inRange: { color: ['#eaf9ff', '#00b2ff', '#0052cc', '#002966'] } },
    series: [{ name: '业务吞吐点阵', type: 'heatmap', data: data, label: { show: true }, emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0, 0, 0, 0.5)' } } }]
  })
  chartInstances['heatmap'] = chart
}

function renderRank() {
  if(!rankRef.value) return
  if(chartInstances['rank']) chartInstances['rank'].dispose()
  const chart = echarts.init(rankRef.value)

  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    toolbox: { feature: { dataZoom: { yAxisIndex: 'none' }, saveAsImage: { name: '营收排行图', pixelRatio: 2 } }, iconStyle: { borderColor: '#00c9a7' } },
    legend: { textStyle: { color: getTextColor() } },
    grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
    xAxis: { type: 'value', boundaryGap: [0, 0.01], splitLine: { lineStyle: { color: 'rgba(0,201,167,0.1)' } }, axisLabel: { color: getTextColor() } },
    yAxis: { type: 'category', data: ['高新支行', '城北储蓄所', '中心总站', '大学城支行', '高铁园区站', '东部沿海网点'], axisLabel: { color: getTextColor() } },
    dataZoom: [ { type: 'slider', yAxisIndex: 0, start: 0, end: 100 } ],
    series: [
      { name: '定存揽储', type: 'bar', itemStyle: { color: '#00c9a7', borderRadius: [0, 4, 4, 0] }, data: [18203, 23489, 29034, 104970, 131744, 630230] },
      { name: '坏账暴露', type: 'bar', itemStyle: { color: '#ea4335', borderRadius: [0, 4, 4, 0] }, data: [19325, 23438, 31000, 121594, 134141, 198180] }
    ]
  })
  chartInstances['rank'] = chart
}

onMounted(() => {
  nextTick(renderTrend)
  window.addEventListener('resize', () => {
    Object.values(chartInstances).forEach(c => c.resize())
  })
})

onBeforeUnmount(() => {
  Object.values(chartInstances).forEach(c => c.dispose())
})
</script>

<style scoped>
.neo-tabs :deep(.el-tabs__item) { font-size: 16px; height: 50px; font-weight: 600; }
.tab-h3 { font-size: 20px; font-weight: 700; color: var(--color-text); margin: 20px 0 10px; display:flex; align-items:center; gap:8px;}
.tab-desc { font-size: 14px; color: var(--color-text-secondary); margin-bottom: 20px;}

.ai-floater { position: absolute; right: 30px; top: 10px; z-index: 99; width: 50px; height: 50px; border-radius: 50%; background: linear-gradient(135deg, #0052cc, #00b2ff); display: flex; align-items: center; justify-content: center; color: white; font-size: 24px; cursor: pointer; box-shadow: 0 10px 20px rgba(0,82,204,0.4); transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275); animation: float 4s infinite; }
.ai-floater:hover { transform: scale(1.1) rotate(15deg); box-shadow: 0 15px 30px rgba(0,82,204,0.6); }

.ai-chat-body { background: rgba(0,82,204,0.03); padding: 20px; border-radius: 12px; }
.ai-msg { font-size: 14px; color: var(--color-text-secondary); margin-bottom: 10px; }
.final-msg { color: var(--color-text); font-size: 15px; line-height: 1.6; }
</style>
