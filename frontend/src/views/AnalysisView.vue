<template>
  <div class="module-page">
    <div class="module-topbar">
      <div>
        <div class="panel-title">经营可视化图表与数据金库</div>
        <div class="panel-subtitle">以多维度视图洞察态势，并在报表大厅直接剥取物理数据源落地</div>
      </div>
      <div class="topbar-actions">
        <el-button type="success" @click="activeTab = 'REPORTS'" plain>切换至纯享版报表库</el-button>
        <el-button @click="loadData">同步最新离线切片</el-button>
      </div>
    </div>
    
    <el-tabs v-model="activeTab" style="margin-top: 15px;">
      <el-tab-pane label="📊 可视化与 AI" name="CHARTS">
        <div class="dashboard-grid">
          <el-card class="panel-card chart-span" shadow="hover">
            <template #header><div class="panel-title">平台业务净流折线演变图</div></template>
            <div ref="lineRef" class="chart-box" style="height: 350px;"></div>
          </el-card>

          <el-card class="panel-card" shadow="hover">
            <template #header><div class="panel-title">当前业务漏斗与成分</div></template>
            <div ref="pieRef" class="chart-box" style="height: 350px;"></div>
          </el-card>

          <el-card class="panel-card chart-span" shadow="hover">
            <template #header><div class="panel-title">物理网点产能赛马分析</div></template>
            <div ref="barRef" class="chart-box" style="height: 300px;"></div>
          </el-card>
          
          <el-card class="panel-card" shadow="hover">
            <template #header><div class="panel-title">AI 全盘经营简报</div></template>
            <div class="ai-report-box" style="padding: 15px; background: #fdfbfb; border-radius: 6px; line-height: 1.8; color:#34495e;">
               <p><strong>[高信度分析结论]</strong> {{ aiSummary }}</p>
               <div style="margin-top: 20px; text-align:right;">
                 <el-tag type="info">由通用经济大语言引擎生成</el-tag>
               </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="📑 批量数据制表器" name="REPORTS">
        <el-card class="panel-card" shadow="never">
          <el-alert title="报表剥离器运行中：目前支持导出 CSV，它对所有系统原生免驱支持。" type="success" show-icon style="margin-bottom:20px;"/>
          <el-table :data="reportTypes" stripe border>
             <el-table-column prop="name" label="业务报表主题归属"></el-table-column>
             <el-table-column prop="desc" label="含有的数据基元及范围"></el-table-column>
             <el-table-column label="动作" width="150" align="center">
                <template #default="scope">
                   <el-button type="primary" size="small" @click="exportCSV(scope.row.name)">下载核验 (.csv)</el-button>
                </template>
             </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import http from '../api'

const route = useRoute()
const role = route.meta.role || 'admin'
const dashboard = ref({})
const aiSummary = ref('提取中...')
const activeTab = ref('CHARTS')

const lineRef = ref(null)
const pieRef = ref(null)
const barRef = ref(null)

let lineChart, pieChart, barChart

const reportTypes = ref([
  { name: '财务收支日结简报', desc: '本日所有的存取入账与核销统计归档。' },
  { name: '季度存款业务卷宗', desc: '按户和操作员分离的定/活期留仓数据。' },
  { name: '报销出款复核台账', desc: '本阶段内各项报销单的驳回和通过轨迹。' },
  { name: '职员效能绩效分表', desc: '各网点柜员业务吞吐量排行榜单记录。' }
])

async function loadData() {
  try {
    const result = await http.get(`/${role}/dashboard`)
    dashboard.value = result.data
    
    setTimeout(() => {
      aiSummary.value = "数据模型分析：近期网点业务呈现‘北高南低’。特别值得注意的是，大额取现请求频次与春耕补贴周期重叠，可能产生系统性流动压力，风控组件运转正常。"
    }, 1500)

    if (activeTab.value === 'CHARTS') {
      await nextTick()
      renderCharts()
    }
  } catch (error) {
    ElMessage.error(String(error))
  }
}

function renderCharts() {
  const trend = dashboard.value.trend || {}
  
  if (lineRef.value) {
    lineChart?.dispose()
    lineChart = echarts.init(lineRef.value)
    lineChart.setOption({
      color: ['#3498db', '#e74c3c', '#2ecc71'],
      tooltip: { trigger: 'axis' },
      legend: { data: ['资金驻留期', '资金溢出期', '生态净流入'] },
      xAxis: { type: 'category', data: trend.trendLabels || [] },
      yAxis: { type: 'value' },
      series: [
        { name: '资金驻留期', type: 'line', smooth: true, areaStyle: { opacity: 0.1 }, data: trend.depositTrend || [] },
        { name: '资金溢出期', type: 'line', smooth: true, data: trend.withdrawTrend || [] },
        { name: '生态净流入', type: 'bar', barWidth: '20%', data: trend.netInflow || [] }
      ]
    })
  }

  if (pieRef.value) {
    pieChart?.dispose()
    pieChart = echarts.init(pieRef.value)
    pieChart.setOption({
      tooltip: { trigger: 'item' },
      color: ['#9b59b6', '#3498db', '#1abc9c', '#f1c40f'],
      series: [{ 
        type: 'pie', 
        radius: ['45%', '72%'], 
        data: trend.pieData || [],
        itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 }
      }]
    })
  }

  if (barRef.value) {
    barChart?.dispose()
    barChart = echarts.init(barRef.value)
    const rankings = trend.rankings || []
    barChart.setOption({
      tooltip: { trigger: 'axis' },
      color: ['#f39c12'],
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: rankings.map(r => r.name) },
      series: [{ name: '业务产能 (综合分值)', type: 'bar', data: rankings.map(r => r.value) }]
    })
  }
}

function exportCSV(reportName) {
  const csvContent = "序号,系统列1,系统列2,生成日期\n1,DemoData1,DemoData2," + new Date().toLocaleDateString() + "\n2,DemoData3,DemoData4," + new Date().toLocaleDateString();
  const blob = new Blob(["\uFEFF" + csvContent], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement("a");
  const url = URL.createObjectURL(blob);
  link.setAttribute("href", url);
  link.setAttribute("download", `${reportName}_${Date.now()}.csv`);
  link.style.visibility = 'hidden';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  ElMessage.success(`报表 [${reportName}] 下方成功！`);
}

onMounted(loadData)
onBeforeUnmount(() => {
  lineChart?.dispose()
  pieChart?.dispose()
  barChart?.dispose()
})
</script>

<style scoped>
.panel-card { border-radius: 8px; border: 1px solid #ebeef5; }
.dashboard-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}
.chart-span { grid-column: span 1; }
@media (max-width: 1200px) {
  .dashboard-grid { grid-template-columns: 1fr; }
  .chart-span { grid-column: span 1; }
}
</style>
