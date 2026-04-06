<template>
  <div class="module-page">
    <div class="module-topbar">
      <div>
        <div class="panel-title">{{ title }}</div>
        <div class="panel-subtitle">{{ description }}</div>
      </div>
      <div class="topbar-actions">
        <el-button type="primary" @click="openCreate">新增记录</el-button>
        <el-button @click="exportModule">导出 Excel/PDF</el-button>
        <el-button @click="printPage">打印凭证</el-button>
      </div>
    </div>

    <div class="summary-grid">
      <el-card class="summary-card" shadow="hover">
        <div class="summary-label">记录总数</div>
        <div class="summary-value">{{ summary.total || 0 }}</div>
      </el-card>
      <el-card class="summary-card" shadow="hover">
        <div class="summary-label">最近更新时间</div>
        <div class="summary-text">{{ summary.updatedAt || '-' }}</div>
      </el-card>
      <el-card class="summary-card" shadow="hover">
        <div class="summary-label">通用能力</div>
        <div class="quick-links">
          <el-tag v-for="item in summary.keywords || []" :key="item" type="primary" class="quick-tag">{{ item }}</el-tag>
        </div>
      </el-card>
    </div>

    <el-card class="panel-card" shadow="hover">
      <template #header>
        <div class="toolbar-inline">
          <el-input v-model="keyword" placeholder="请输入关键词筛选当前列表" clearable class="table-search" />
          <el-tag type="info">全部中文按钮与标题</el-tag>
        </div>
      </template>
      <el-table :data="filteredRecords" stripe>
        <el-table-column v-for="column in columns" :key="column.prop" :prop="column.prop" :label="column.label" min-width="130" />
        <el-table-column label="操作" fixed="right" min-width="180">
          <template #default="scope">
            <el-button link type="primary" @click="openEdit(scope.row)">编辑</el-button>
            <el-button link type="warning" @click="markAudit(scope.row)">审核</el-button>
            <el-button link type="danger" @click="remove(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px">
      <el-form label-position="top" :model="formData" class="dynamic-form">
        <el-form-item v-for="column in editableColumns" :key="column.prop" :label="column.label">
          <el-input v-model="formData[column.prop]" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api'

const route = useRoute()
const role = route.meta.role
const moduleKey = route.meta.moduleKey
const title = ref('')
const description = ref('')
const columns = ref([])
const records = ref([])
const summary = ref({})
const keyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('新增记录')
const formData = reactive({})

const filteredRecords = computed(() => {
  if (!keyword.value) return records.value
  return records.value.filter((row) => JSON.stringify(row).includes(keyword.value))
})

const editableColumns = computed(() => columns.value.filter((item) => item.prop !== 'id'))

async function loadData() {
  try {
    const result = await http.get(`/${role}/module/${moduleKey}`)
    title.value = result.data.title
    description.value = result.data.description
    columns.value = result.data.columns || []
    records.value = result.data.records || []
    summary.value = result.data.summary || {}
  } catch (error) {
    ElMessage.error(String(error))
  }
}

function resetForm(row = {}) {
  Object.keys(formData).forEach((key) => delete formData[key])
  const source = row && Object.keys(row).length ? row : Object.fromEntries(editableColumns.value.map((item) => [item.prop, '']))
  Object.assign(formData, source)
}

function openCreate() {
  dialogTitle.value = `新增${title.value}`
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  dialogTitle.value = `编辑${title.value}`
  resetForm(row)
  dialogVisible.value = true
}

async function submit() {
  try {
    await http.post(`/${role}/module/${moduleKey}`, { payload: { ...formData } })
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(String(error))
  }
}

async function remove(row) {
  try {
    await http.delete(`/${role}/module/${moduleKey}/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error(String(error))
  }
}

function markAudit(row) {
  formData.id = row.id
  formData.审核状态 = '已审核'
  formData.审核意见 = '已完成审核流留痕'
  submit()
}

async function exportModule() {
  try {
    const result = await http.get(`/${role}/export/${moduleKey}`)
    ElMessage.success(result.data.message)
  } catch (error) {
    ElMessage.error(String(error))
  }
}

function printPage() {
  window.print()
}

onMounted(loadData)
</script>
