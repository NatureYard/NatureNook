<template>
  <view class="page">
    <view class="hero card">
      <view class="panel-head">
        <view>
          <view class="section-title">任务看板</view>
          <view class="hero-desc">聚合门店当日待处理事项，优先盯住异常、核销和低库存。</view>
        </view>
        <button class="btn-refresh" :disabled="loading" @click="fetchTasks">
          {{ loading ? '刷新中' : '刷新' }}
        </button>
      </view>

      <view class="summary-row">
        <view class="summary-item">
          <view class="summary-label">待处理总量</view>
          <view class="summary-value">{{ totalPending }}</view>
        </view>
        <view class="summary-item highlight">
          <view class="summary-label">高优先级</view>
          <view class="summary-value">{{ urgentCount }}</view>
        </view>
      </view>

      <view class="hero-foot">
        <view class="hero-foot-copy">{{ boardStatus }}</view>
        <view class="updated-at">{{ lastUpdatedText }}</view>
      </view>
    </view>

    <view v-if="sortedTasks.length === 0" class="card empty-card">
      <view class="empty-title">当前没有待处理事项</view>
      <view class="empty-desc">可以继续巡检、补录信息或等待新的预约与异常进入看板。</view>
    </view>

    <view class="card task-card" v-for="item in sortedTasks" :key="item.type">
      <view class="row task-top">
        <view>
          <view class="task-kicker">{{ item.kicker }}</view>
          <view class="title">{{ item.title }}</view>
        </view>
        <view class="task-badge" :class="item.toneClass">{{ item.badgeText }}</view>
      </view>

      <view class="row metric-row">
        <view>
          <view class="summary-label">待处理数量</view>
          <view class="count">{{ item.count }}</view>
        </view>
        <view class="task-priority" :class="item.toneClass">{{ item.priorityText }}</view>
      </view>

      <view class="desc">{{ item.desc }}</view>
      <view class="task-action">{{ item.actionText }}</view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { BASE_URL } from '../../config.js'

const defaultTasks = [
  { type: 'CHECKIN', title: '待核销到店', count: 12 },
  { type: 'BOARDING', title: '待记录寄养', count: 5 },
  { type: 'MATERIAL', title: '待补货物资', count: 3 },
  { type: 'RISK', title: '待复核异常', count: 2 },
]

const taskMeta = {
  CHECKIN: {
    kicker: '到店核验',
    desc: '优先确认已支付或已预约的到店订单，避免入口拥堵。',
    actionText: '建议先打开核销页，处理门口排队和闸机异常。',
    toneClass: 'tone-mint',
  },
  BOARDING: {
    kicker: '寄养照护',
    desc: '寄养订单需要持续补录喂养、巡检和特殊提醒。',
    actionText: '建议补齐今日喂养与状态记录，避免交接遗漏。',
    toneClass: 'tone-sun',
  },
  MATERIAL: {
    kicker: '库存补给',
    desc: '低库存或异常损耗需要尽快复核，避免服务中断。',
    actionText: '建议先检查安全库存并安排补货或报损复核。',
    toneClass: 'tone-rose',
  },
  RISK: {
    kicker: '异常复核',
    desc: '人工放行和风险事件需要店长尽快确认处理意见。',
    actionText: '建议优先查看异常详情并补充处理结论。',
    toneClass: 'tone-risk',
  },
}

const tasks = ref(mapTasks(defaultTasks))
const loading = ref(false)
const lastUpdatedAt = ref('')

function mapTasks(items) {
  return items.map((item) => {
    const meta = taskMeta[item.type] ?? {
      kicker: '门店任务',
      desc: '当前事项需要值守人员继续跟进。',
      actionText: '建议先查看详情并及时处理。',
      toneClass: 'tone-sun',
    }
    return {
      ...item,
      ...meta,
      priorityText: item.count >= 8 ? '立即处理' : item.count >= 1 ? '今日处理' : '已清空',
      badgeText: item.count > 0 ? `${item.count} 项` : '0 项',
    }
  })
}

const sortedTasks = computed(() => {
  return [...tasks.value].sort((left, right) => {
    if (left.count !== right.count) {
      return right.count - left.count
    }
    return left.title.localeCompare(right.title, 'zh-CN')
  })
})

const totalPending = computed(() => {
  return tasks.value.reduce((sum, item) => sum + item.count, 0)
})

const urgentCount = computed(() => {
  return tasks.value.filter((item) => item.count >= 8 || (item.type === 'RISK' && item.count > 0)).length
})

const boardStatus = computed(() => {
  if (totalPending.value === 0) {
    return '当前门店节奏平稳，可以继续巡检和补录。'
  }
  if (urgentCount.value > 0) {
    return '看板中存在需要优先盯住的事项，先处理高优先级任务。'
  }
  return '当前事项可控，按队列顺序处理即可。'
})

const lastUpdatedText = computed(() => {
  if (!lastUpdatedAt.value) {
    return '尚未同步'
  }
  return `更新于 ${lastUpdatedAt.value}`
})

function formatNow() {
  const now = new Date()
  const hours = `${now.getHours()}`.padStart(2, '0')
  const minutes = `${now.getMinutes()}`.padStart(2, '0')
  return `${hours}:${minutes}`
}

function setFallbackTasks() {
  tasks.value = mapTasks(defaultTasks)
  lastUpdatedAt.value = formatNow()
}

function fetchTasks() {
  loading.value = true
  uni.request({
    url: `${BASE_URL}/api/m-app/task-board`,
    method: 'GET',
    success: (res) => {
      const data = res.data?.data
      if (Array.isArray(data) && data.length > 0) {
        tasks.value = mapTasks(data.map((item) => ({
          type: item.type,
          title: item.title,
          count: Number(item.count ?? 0),
        })))
      } else {
        setFallbackTasks()
      }
      lastUpdatedAt.value = formatNow()
    },
    fail: () => {
      setFallbackTasks()
    },
    complete: () => {
      loading.value = false
    },
  })
}

onMounted(fetchTasks)
</script>

<style scoped>
.page {
  padding: 24rpx;
  background: var(--surface-base);
}

.hero {
  margin-bottom: 16rpx;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24rpx;
}

.section-title {
  font-size: 36rpx;
  font-weight: 700;
}

.hero-desc {
  margin-top: 10rpx;
  color: var(--text-secondary);
  line-height: 1.6;
  font-size: 26rpx;
}

.btn-refresh {
  font-size: 24rpx;
  padding: 10rpx 24rpx;
  background: var(--brand-secondary);
  color: #fff;
  border-radius: 999rpx;
}

.card {
  background: var(--surface-card);
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.summary-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
  margin-top: 24rpx;
}

.summary-item {
  padding: 22rpx;
  border-radius: 18rpx;
  background: var(--surface-muted);
}

.summary-item.highlight {
  background: var(--surface-highlight-success);
}

.summary-label {
  color: var(--text-secondary);
  font-size: 24rpx;
}

.summary-value {
  margin-top: 10rpx;
  color: var(--text-primary);
  font-size: 40rpx;
  font-weight: 700;
}

.hero-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20rpx;
  margin-top: 24rpx;
}

.hero-foot-copy,
.updated-at {
  color: var(--text-secondary);
  font-size: 24rpx;
}

.updated-at {
  text-align: right;
}

.task-card {
  box-shadow: 0 14rpx 36rpx rgba(47, 143, 125, 0.08);
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-top {
  align-items: flex-start;
  gap: 20rpx;
}

.task-kicker {
  color: var(--text-tertiary);
  font-size: 22rpx;
  letter-spacing: 1rpx;
}

.title {
  font-size: 32rpx;
  font-weight: 700;
}

.task-badge,
.task-priority {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  font-weight: 600;
}

.tone-mint {
  background: var(--state-success-bg);
  color: var(--state-success-fg);
}

.tone-sun {
  background: var(--state-warning-bg);
  color: var(--state-warning-fg);
}

.tone-rose,
.tone-risk {
  background: var(--state-risk-bg);
  color: var(--state-risk-fg);
}

.metric-row {
  margin-top: 20rpx;
  align-items: flex-end;
}

.count {
  color: var(--brand-secondary);
  font-size: 42rpx;
  font-weight: 700;
}

.desc {
  margin-top: 18rpx;
  color: var(--text-secondary);
  line-height: 1.6;
}

.task-action {
  margin-top: 16rpx;
  color: var(--text-primary);
  font-size: 24rpx;
}

.empty-card {
  text-align: center;
}

.empty-title {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text-primary);
}

.empty-desc {
  margin-top: 12rpx;
  color: var(--text-secondary);
  line-height: 1.6;
}
</style>
