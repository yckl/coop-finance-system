<template>
  <div class="ai-page">
    <el-row :gutter="20">
      <el-col :xs="24" :lg="16">
        <el-card class="panel-card" shadow="hover">
          <template #header><div class="panel-title">AI 智能助手</div></template>
          <div class="chat-list">
            <div v-for="(item, index) in history" :key="index" class="chat-item">
              <div class="chat-question">问：{{ item.question }}</div>
              <div class="chat-answer">答：{{ item.answer }}</div>
            </div>
          </div>
          <el-input v-model="question" type="textarea" :rows="4" :placeholder="inputPlaceholder" />
          <div class="topbar-actions top-gap">
            <el-button type="primary" @click="ask">发送问题</el-button>
            <el-button v-for="(preset, index) in presets" :key="index" @click="fillPreset(preset.text)">{{ preset.label }}</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card class="panel-card" shadow="hover">
          <template #header><div class="panel-title">助手能力</div></template>
          <div v-for="cap in capabilities" :key="cap" class="simple-list-item">{{ cap }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api'

const route = useRoute()
const role = route.meta.role || 'user'
const history = ref([])

const defaultQuestion = role === 'user' ? '帮我解释一下我最近一笔取款的含义，我的账户目前安全吗？' : '请基于最新各项表单数据，为下周股东大会草拟经营摘要。'
const question = ref(defaultQuestion)

const inputPlaceholder = computed(() => {
  return role === 'user' ? '请输入财务疑惑，例如：为什么会有手续费？ / 去哪里报销最快？' : '请输入业务咨询问题，例如：最近异常提现规律 / 生成上月结余简报'
})

const presets = computed(() => {
  if (role === 'user') {
    return [
      { text: '我的余额为什么变化了？', label: '资金变化疑惑' },
      { text: '帮我解释这条异常报销为什么遭驳回', label: '报销诊断仪' },
      { text: '最新的一份公告和我有关吗？', label: '制度通晓' }
    ]
  }
  return [
    { text: '请生成最新存款周期净流出风险排查大纲', label: '风险排查大纲' },
    { text: '辅助解释近期大量差旅农资报销背后的审计盲点', label: '报销审计解释' },
    { text: '帮我草拟一份面向全网点下发的“休眠账户冻结新规”通知', label: '制度文案生成' }
  ]
})

const capabilities = computed(() => {
  if (role === 'user') return ['自然语言解读流水', '智能报销驳回答疑', '资金异常风险早筛', '网点公告摘要速读']
  return ['自然语言解释报表', '自动总结资金变化', '识别异常波动与风险预警', '辅助生成公告草稿与中文简报']
})

function fillPreset(value) {
  question.value = value
}

async function ask() {
  try {
    const result = await http.post(`/ai/${role}`, { question: question.value })
    history.value.unshift({ question: result.data.question, answer: result.data.answer })
    question.value = ''
  } catch (error) {
    ElMessage.error(String(error))
  }
}

ask()
</script>
