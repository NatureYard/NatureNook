<template>
  <main class="page">
    <aside class="sidebar">
      <div class="brand">
        <p class="eyebrow">MCLY Admin</p>
        <h1>萌宠乐园管理后台</h1>
      </div>
      <nav class="nav">
        <button
          v-for="item in sections"
          :key="item.key"
          :class="['nav-item', { active: activeSection === item.key }]"
          @click="activeSection = item.key"
        >
          <span>{{ item.label }}</span>
          <small>{{ item.meta }}</small>
        </button>
      </nav>
    </aside>

    <section class="content">
      <div class="top-grid">
        <article v-for="item in kpis" :key="item.label" class="metric-card">
          <p>{{ item.label }}</p>
          <strong>{{ item.value }}</strong>
        </article>
      </div>

      <section class="panel hero-panel">
        <h2>{{ currentSection.title }}</h2>
        <p class="summary">{{ currentSection.summary }}</p>
      </section>

      <section class="two-col">
        <article class="panel">
          <h3>模块范围</h3>
          <ul class="item-list">
            <li v-for="item in currentSection.items" :key="item">{{ item }}</li>
          </ul>
        </article>

        <article class="panel">
          <h3>近期待建</h3>
          <ul class="item-list">
            <li v-for="todo in currentSection.todos" :key="todo">{{ todo }}</li>
          </ul>
        </article>
      </section>

      <section class="panel">
        <div class="panel-head">
          <h3>{{ tableTitle }}</h3>
          <button class="refresh" @click="refresh">刷新</button>
        </div>
        <p v-if="loading" class="hint">正在加载数据...</p>
        <p v-else-if="error" class="error">{{ error }}</p>
        <div v-else class="cards">
          <article v-for="item in sectionData" :key="`${item.title}-${item.meta}`" class="card">
            <h4>{{ item.title }}</h4>
            <p>{{ item.meta }}</p>
          </article>
        </div>
      </section>

      <section v-if="activeSection === 'members'" class="panel">
        <div class="panel-head">
          <h3>新增会员</h3>
        </div>
        <div class="form-grid">
          <input v-model="createForm.name" class="input" placeholder="会员姓名" />
          <input v-model="createForm.phone" class="input" placeholder="手机号" />
          <select v-model="createForm.level" class="input">
            <option value="NORMAL">NORMAL</option>
            <option value="SILVER">SILVER</option>
            <option value="GOLD">GOLD</option>
          </select>
          <button class="refresh" @click="submitMember">提交</button>
        </div>
        <p v-if="submitMessage" class="hint">{{ submitMessage }}</p>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import {
  createMember,
  fetchCards,
  fetchDashboardSummary,
  fetchGateEvents,
  fetchGateRules,
  fetchMaterialStocks,
  fetchMembers,
  fetchPets,
} from './api'

const activeSection = ref('dashboard')
const loading = ref(false)
const error = ref('')
const summary = ref(null)
const sectionData = ref([])
const createForm = ref({
  storeId: 1,
  name: '',
  phone: '',
  level: 'NORMAL',
})
const submitMessage = ref('')

const sections = [
  { key: 'dashboard', label: '经营概览', meta: '总部视图' },
  { key: 'gate', label: '闸机通行', meta: '入园风控' },
  { key: 'materials', label: '物资管理', meta: '饲料与耗材' },
  { key: 'members', label: '会员与宠物', meta: '卡种与档案' },
]

const sectionMap = {
  dashboard: {
    title: '经营概览',
    summary: '提供今日预约、入园、卡种活跃度、风控事件和低库存物资的统一总览。',
    items: ['预约与订单概览', '活跃会员与卡种监控', '门店经营简报', '风险与物资预警'],
    todos: ['增加门店筛选器', '补报表图表区域', '增加经营趋势视图'],
  },
  gate: {
    title: '闸机通行中心',
    summary: '覆盖首次入园、当日多次复入、卡种通行、人工放行和异常追踪。',
    items: ['通行规则配置', '闸机事件时间线', '人工放行记录', '风控命中详情'],
    todos: ['补风控处置流转', '增加门店闸机健康状态', '增加人工放行审批视图'],
  },
  materials: {
    title: '园区物资管理',
    summary: '管理饲料、清洁用品、洗护耗材及内部物资的库存、领用、盘点与报损。',
    items: ['物资分类与主数据', '门店仓库库存', '领用与消耗流水', '安全库存预警'],
    todos: ['增加入库和盘点单页面', '补报损审批流', '增加成本归集图表'],
  },
  members: {
    title: '会员与宠物中心',
    summary: '管理会员档案、宠物档案、卡种权益、人脸绑定和售后记录。',
    items: ['会员列表', '宠物档案', '卡种与续费', '人脸绑定状态'],
    todos: ['增加卡种详情页', '补售后记录视图', '增加会员检索条件'],
  },
}

const currentSection = computed(() => sectionMap[activeSection.value])

const kpis = computed(() => {
  if (!summary.value) {
    return [
      { label: '今日预约', value: '--' },
      { label: '今日入园', value: '--' },
      { label: '活跃卡种', value: '--' },
      { label: '低库存物资', value: '--' },
    ]
  }
  return [
    { label: '今日预约', value: summary.value.todayReservations },
    { label: '今日入园', value: summary.value.todayEntries },
    { label: '活跃卡种', value: summary.value.activeCards },
    { label: '低库存物资', value: summary.value.lowStockItems },
  ]
})

const tableTitle = computed(() => {
  if (activeSection.value === 'gate') return '闸机实时数据'
  if (activeSection.value === 'materials') return '库存与预警'
  if (activeSection.value === 'members') return '会员与宠物数据'
  return '经营汇总'
})

async function refresh() {
  loading.value = true
  error.value = ''
  try {
    summary.value = await fetchDashboardSummary()
    if (activeSection.value === 'gate') {
      const [rules, events] = await Promise.all([fetchGateRules(), fetchGateEvents()])
      sectionData.value = [
        { title: '通行策略', meta: `${rules.sameDayReentryPolicy} / ${rules.antiFraudPolicy}` },
        ...events.map((item) => ({
          title: `${item.memberName} ${item.direction}`,
          meta: `${item.result} | ${item.occurredAt}`,
        })),
      ]
    } else if (activeSection.value === 'materials') {
      const stocks = await fetchMaterialStocks()
      sectionData.value = stocks.map((item) => ({
        title: item.name,
        meta: `${item.category} | 库存 ${item.quantity}${item.unit} | 安全库存 ${item.safetyStock}`,
      }))
    } else if (activeSection.value === 'members') {
      const [members, pets, cards] = await Promise.all([fetchMembers(), fetchPets(), fetchCards()])
      sectionData.value = [
        ...members.map((item) => ({
          title: `${item.name} (${item.level})`,
          meta: `${item.phone} | 人脸绑定: ${item.faceBound ? '已绑定' : '未绑定'}`,
        })),
        ...pets.map((item) => ({
          title: `宠物 ${item.name}`,
          meta: `${item.species} ${item.breed || ''} | 主人ID ${item.ownerId}`,
        })),
        ...cards.map((item) => ({
          title: `${item.memberName} ${item.cardType}`,
          meta: `${item.status} | 到期 ${item.validTo}`,
        })),
      ]
    } else {
      sectionData.value = [
        { title: '总会员数', meta: String(summary.value.totalMembers) },
        { title: '今日预约', meta: String(summary.value.todayReservations) },
        { title: '开放风控事件', meta: String(summary.value.openRiskEvents) },
      ]
    }
  } catch (err) {
    error.value = err.message || '加载失败'
    sectionData.value = []
  } finally {
    loading.value = false
  }
}

async function submitMember() {
  submitMessage.value = ''
  try {
    const result = await createMember(createForm.value)
    submitMessage.value = `会员创建成功，ID ${result.id}`
    createForm.value = { storeId: 1, name: '', phone: '', level: 'NORMAL' }
    if (activeSection.value === 'members') {
      await refresh()
    }
  } catch (err) {
    submitMessage.value = err.message || '会员创建失败'
  }
}

watch(activeSection, refresh)
onMounted(refresh)
</script>
