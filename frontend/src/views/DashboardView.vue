<template>
  <div class="dashboard-page">
    <div class="metric-grid">
      <el-card v-for="item in dashboard.metrics || []" :key="item.label" class="metric-card" shadow="hover">
        <div class="metric-label">{{ item.label }}</div>
        <div class="metric-value">{{ item.value }}<span>{{ item.unit }}</span></div>
        <div class="metric-trend">{{ item.trend }}</div>
      </el-card>
    </div>

    <div v-if="role === 'user'" class="user-dashboard-grid">
      <el-card class="balance-hero" shadow="hover">
        <div class="balance-caption">当前账户总览</div>
        <div class="balance-value">¥ {{ dashboard.metrics?.[0]?.value || '0' }}</div>
        <div class="balance-footer">本月收入 {{ dashboard.metrics?.[1]?.value || '0' }} 元，本月支出 {{ dashboard.metrics?.[2]?.value || '0' }} 元</div>
      </el-card>
      <el-card class="panel-card" shadow="hover">
        <template #header><div class="panel-title">最近交易流水</div></template>
        <el-table :data="dashboard.recentTransactions || []" stripe>
          <el-table-column prop="type" label="交易类型" />
          <el-table-column prop="amount" label="金额" />
          <el-table-column prop="operator" label="经办人" />
          <el-table-column prop="time" label="时间" />
        </el-table>
      </el-card>
      <el-card class="panel-card" shadow="hover">
        <template #header><div class="panel-title">快捷入口</div></template>
        <div class="quick-links">
          <el-tag v-for="item in dashboard.quickLinks || []" :key="item" class="quick-tag" type="primary">{{ item }}</el-tag>
        </div>
      </el-card>
      <el-card class="panel-card" shadow="hover">
        <template #header><div class="panel-title">我的消息中心</div></template>
        <div v-for="item in dashboard.notifications || []" :key="item.id" class="simple-list-item">
          <strong>{{ item.标题 }}</strong>
          <span>{{ item.状态 }}</span>
        </div>
      </el-card>
    </div>

    <div v-else class="dashboard-grid">
      <el-card class="panel-card chart-span" shadow="hover">
        <template #header><div class="panel-title">趋势分析</div></template>
        <div ref="lineRef" class="chart-box"></div>
      </el-card>
      <el-card class="panel-card" shadow="hover">
        <template #header><div class="panel-title">业务结构</div></template>
        <div ref="pieRef" class="chart-box"></div>
      </el-card>
      <el-card class="panel-card" shadow="hover">
        <template #header><div class="panel-title">待办事项</div></template>
        <div v-for="item in dashboard.todo || []" :key="item" class="simple-list-item">{{ item }}</div>
      </el-card>
      <el-card class="panel-card" shadow="hover">
        <template #header><div class="panel-title">最近公告</div></template>
        <div v-for="item in dashboard.notices || []" :key="item.id" class="simple-list-item">
          <strong>{{ item.标题 }}</strong>
          <span>{{ item.类型 }}</span>
        </div>
      </el-card>
      <el-card class="panel-card" shadow="hover">
        <template #header><div class="panel-title">风险预警</div></template>
        <div v-for="item in dashboard.warnings || []" :key="item" class="warning-item">{{ item }}</div>
      </el-card>
      <el-card v-if="role === 'finance'" class="panel-card chart-span" shadow="hover">
        <template #header><div class="panel-title">最近经办记录</div></template>
        <el-table :data="dashboard.recentRecords || []" stripe>
          <el-table-column prop="serialNo" label="流水号" min-width="140" />
          <el-table-column prop="userName" label="用户姓名" />
          <el-table-column prop="type" label="业务类型" />
          <el-table-column prop="amount" label="金额" />
          <el-table-column prop="warning" label="预警状态" />
          <el-table-column prop="time" label="办理时间" min-width="160" />
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import http from '../api'

const route = useRoute()
const role = route.meta.role
const dashboard = ref({})
const lineRef = ref(null)
const pieRef = ref(null)
let lineChart
let pieChart

async function loadDashboard() {
  try {
    const result = await http.get(`/${role}/dashboard`)
    dashboard.value = result.data
    await nextTick()
    if (role !== 'user') {
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
      color: ['#0052CC', '#00B2FF', '#FF9500'],
      tooltip: { trigger: 'axis' },
      legend: { data: ['存款', '取款', '净流入'] },
      xAxis: { type: 'category', data: trend.trendLabels || [] },
      yAxis: { type: 'value' },
      series: [
        { name: '存款', type: 'line', smooth: true, data: trend.depositTrend || [] },
        { name: '取款', type: 'bar', data: trend.withdrawTrend || [] },
        { name: '净流入', type: 'line', smooth: true, data: trend.netInflow || [] }
      ]
    })
  }
  if (pieRef.value) {
    pieChart?.dispose()
    pieChart = echarts.init(pieRef.value)
    pieChart.setOption({
      tooltip: { trigger: 'item' },
      color: ['#0052CC', '#00B2FF', '#5BD3B4', '#FF9500'],
      series: [{ type: 'pie', radius: ['45%', '72%'], data: trend.pieData || [] }]
    })
  }
}

onMounted(loadDashboard)
onBeforeUnmount(() => {
  lineChart?.dispose()
  pieChart?.dispose()
})
</script>
