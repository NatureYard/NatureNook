<template>
  <view class="page">
    <view class="card summary-card">
      <view class="title">现场核销工作台</view>
      <view class="desc">先核验订单与通行资格，再决定是否人工放行。当前设备：{{ deviceCode }}</view>
      <view class="summary-row">
        <view class="summary-item">
          <view class="summary-label">待处理订单</view>
          <view class="summary-value">{{ orders.length }}</view>
        </view>
        <view class="summary-item">
          <view class="summary-label">待人工复核</view>
          <view class="summary-value">{{ manualReviewCount }}</view>
        </view>
        <view class="summary-item">
          <view class="summary-label">最近核验</view>
          <view class="summary-value summary-text">{{ verifySummary }}</view>
        </view>
      </view>
      <input v-model="deviceCode" class="input" placeholder="闸机设备编码" />
      <button class="btn secondary-btn" @click="loadOrders">刷新核销队列</button>
    </view>

    <view class="card">
      <view class="row queue-head">
        <view class="title">到店核销队列</view>
        <view class="filter-row">
          <button
            v-for="filter in filters"
            :key="filter.value"
            class="filter-btn"
            :class="activeFilter === filter.value ? 'filter-btn-active' : ''"
            @click="activeFilter = filter.value"
          >
            {{ filter.label }}
          </button>
        </view>
      </view>
      <view v-if="loading" class="desc">正在加载核销订单...</view>
      <view v-else-if="!filteredOrders.length" class="desc">当前筛选条件下没有待处理订单</view>
      <view v-else v-for="item in filteredOrders" :key="item.orderId" class="queue-item">
        <view class="row">
          <view class="queue-title">{{ item.memberName }} · {{ item.petName }}</view>
          <view :class="['tag', item.activeEntitlement ? 'tag-ready' : 'tag-risk']">
            {{ item.activeEntitlement ? '可核销' : '待人工复核' }}
          </view>
        </view>
        <view class="desc">{{ item.orderNo }} | {{ item.orderTypeLabel }} | {{ item.orderStatusLabel }}</view>
        <view class="desc">{{ item.storeName }} | {{ item.reservationInfo }}</view>
        <view class="desc">人脸录入：{{ item.faceBound ? '已录入' : '未录入' }}</view>
        <view class="desc">资格：{{ item.entitlementText }}</view>
        <view v-if="!item.activeEntitlement" class="risk-copy">建议先复核资格和订单状态，再决定是否人工放行。</view>
        <view class="action-row">
          <button class="mini-btn" @click="fillManualRelease(item)">带入人工放行</button>
          <button class="mini-btn primary-mini-btn" :disabled="verifyingOrderId === item.orderId" @click="verify(item)">
            {{ verifyingOrderId === item.orderId ? '核验中...' : '核验闸机放行' }}
          </button>
        </view>
        <view
          v-if="lastVerifyResult && lastVerifyResult.orderId === item.orderId"
          :class="['verify-result', lastVerifyResult.allowed ? 'verify-pass' : 'verify-block']"
        >
          {{ lastVerifyResult.message }}
        </view>
      </view>
    </view>

    <view class="card">
      <view class="title">人工放行登记</view>
      <view class="desc">{{ manualReleaseHint }}</view>
      <input v-model="form.memberId" class="input" placeholder="会员ID" />
      <input v-model="form.orderId" class="input" placeholder="订单ID" />
      <input v-model="form.reason" class="input" placeholder="放行原因" />
      <button class="btn" @click="submit">提交</button>
      <view v-if="message" class="desc">{{ message }}</view>
    </view>
  </view>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { BASE_URL } from '../../config.js'

const orders = ref([])
const loading = ref(true)
const verifyingOrderId = ref(null)
const lastVerifyResult = ref(null)
const message = ref('')
const deviceCode = ref('GATE-SH-001')
const activeFilter = ref('ALL')

const filters = [
  { value: 'ALL', label: '全部' },
  { value: 'READY', label: '可核销' },
  { value: 'REVIEW', label: '待复核' },
]

const form = ref({
  storeId: 1,
  staffId: 2,
  memberId: '',
  orderId: '',
  reason: '',
})

const verifySummary = computed(() => {
  if (!lastVerifyResult.value) return '暂无'
  return lastVerifyResult.value.allowed ? '最近一次已放行' : '最近一次被拦截'
})

const manualReviewCount = computed(() => {
  return orders.value.filter((item) => !item.activeEntitlement).length
})

const filteredOrders = computed(() => {
  if (activeFilter.value === 'READY') {
    return orders.value.filter((item) => item.activeEntitlement)
  }
  if (activeFilter.value === 'REVIEW') {
    return orders.value.filter((item) => !item.activeEntitlement)
  }
  return orders.value
})

const manualReleaseHint = computed(() => {
  if (form.value.memberId || form.value.orderId) {
    return `当前带入会员 ${form.value.memberId || '-'}，订单 ${form.value.orderId || '-'}，请补充放行原因。`
  }
  return '当闸机拦截且现场复核后需要放行时，可先从上方队列带入订单。'
})

onMounted(() => {
  loadOrders()
})

function formatOrderType(type) {
  if (type === 'TICKET') return '单次门票'
  if (type === 'GROOMING') return '洗护套餐'
  if (type === 'BOARDING') return '寄养预约'
  if (type === 'YEAR_CARD') return '年卡'
  if (type === 'MONTH_CARD') return '月卡'
  if (type === 'SEASON_CARD') return '季卡'
  return type || '未知订单'
}

function formatOrderStatus(status) {
  if (status === 'PAID') return '已支付'
  if (status === 'BOOKED') return '已预约'
  if (status === 'COMPLETED') return '已完成'
  if (status === 'CANCELLED') return '已取消'
  return status || '处理中'
}

function formatEntitlementName(name) {
  if (name === 'DAY_TICKET') return '单次门票资格'
  if (name === 'GROOMING_PACKAGE') return '洗护到店资格'
  if (name === 'BOARDING_DAY') return '寄养到店资格'
  if (name === 'YEAR_CARD') return '年卡资格'
  if (name === 'MONTH_CARD') return '月卡资格'
  if (name === 'SEASON_CARD') return '季卡资格'
  return name || '暂无资格'
}

function mapOrder(item) {
  return {
    ...item,
    orderTypeLabel: formatOrderType(item.orderType),
    orderStatusLabel: formatOrderStatus(item.orderStatus),
    reservationInfo: [item.reservationDate, item.timeSlot].filter(Boolean).join(' | ') || '无需预约时间段',
    entitlementText: item.activeEntitlement
      ? `${formatEntitlementName(item.entitlementName)}，有效至 ${item.entitlementValidTo}`
      : '当前无有效通行资格',
  }
}

function loadOrders() {
  loading.value = true
  uni.request({
    url: `${BASE_URL}/api/m-app/checkin/orders`,
    data: { storeId: form.value.storeId },
    success: (res) => {
      const rows = res.data?.data || []
      orders.value = rows.map(mapOrder)
    },
    fail: () => {
      orders.value = [
        mapOrder({
          orderId: 2,
          orderNo: 'ORD202604050002',
          memberId: 2,
          memberName: '李四',
          petName: '布丁',
          orderType: 'GROOMING',
          orderStatus: 'PAID',
          storeName: '上海萌宠乐园旗舰店',
          reservationDate: '2026-04-05',
          timeSlot: '13:00-15:00',
          faceBound: true,
          activeEntitlement: false,
          entitlementName: '',
          entitlementValidTo: '',
        }),
        mapOrder({
          orderId: 1,
          orderNo: 'ORD202604050001',
          memberId: 1,
          memberName: '张三',
          petName: '奶球',
          orderType: 'TICKET',
          orderStatus: 'PAID',
          storeName: '上海萌宠乐园旗舰店',
          reservationDate: '2026-04-05',
          timeSlot: '09:00-12:00',
          faceBound: true,
          activeEntitlement: true,
          entitlementName: 'DAY_TICKET',
          entitlementValidTo: '2026-04-06T04:15:53',
        }),
      ]
    },
    complete: () => {
      loading.value = false
    },
  })
}

function fillManualRelease(item) {
  form.value.memberId = String(item.memberId)
  form.value.orderId = String(item.orderId)
  if (!form.value.reason) {
    form.value.reason = '闸机拦截后现场复核放行'
  }
}

function verify(item) {
  verifyingOrderId.value = item.orderId
  lastVerifyResult.value = null
  uni.request({
    url: `${BASE_URL}/api/m-app/checkin/verify`,
    method: 'POST',
    header: {
      'Content-Type': 'application/json',
    },
    data: {
      orderId: item.orderId,
      deviceCode: deviceCode.value,
      direction: 'ENTRY',
    },
    success: (res) => {
      const payload = res.data?.data
      if (!payload) {
        lastVerifyResult.value = {
          orderId: item.orderId,
          allowed: false,
          message: '核验结果为空',
        }
        return
      }
      lastVerifyResult.value = {
        orderId: item.orderId,
        allowed: payload.allowed,
        message: payload.allowed
          ? `${payload.memberName} 核验通过，闸机可放行`
          : `${payload.memberName} 核验被拦截，原因 ${payload.reasonCode || 'UNKNOWN'}`,
      }
      if (!payload.allowed) {
        fillManualRelease(item)
      }
      loadOrders()
    },
    fail: () => {
      lastVerifyResult.value = {
        orderId: item.orderId,
        allowed: false,
        message: '核验失败，请稍后重试',
      }
    },
    complete: () => {
      verifyingOrderId.value = null
    },
  })
}

function submit() {
  uni.request({
    url: `${BASE_URL}/api/m-app/manual-releases`,
    method: 'POST',
    header: {
      'Content-Type': 'application/json',
    },
    data: {
      storeId: form.value.storeId,
      staffId: form.value.staffId,
      memberId: form.value.memberId ? Number(form.value.memberId) : null,
      orderId: form.value.orderId ? Number(form.value.orderId) : null,
      reason: form.value.reason,
    },
    success: (res) => {
      const id = res.data?.data?.id
      message.value = id ? `登记成功，记录ID ${id}` : '提交完成'
      form.value.memberId = ''
      form.value.orderId = ''
      form.value.reason = ''
      loadOrders()
    },
    fail: () => {
      message.value = '提交失败'
    },
  })
}
</script>

<style scoped>
.page {
  padding: 24rpx;
}

.card {
  background: var(--surface-card);
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.summary-card {
  background: linear-gradient(135deg, var(--surface-highlight-start) 0%, var(--surface-card) 100%);
}

.summary-row {
  display: flex;
  gap: 16rpx;
  margin-top: 16rpx;
  flex-wrap: wrap;
}

.summary-item {
  flex: 1;
  min-width: 0;
  padding: 20rpx;
  border-radius: 16rpx;
  background: var(--surface-highlight-success);
}

.summary-label {
  color: var(--text-secondary);
  font-size: 24rpx;
}

.summary-value {
  margin-top: 8rpx;
  font-size: 34rpx;
  font-weight: 700;
  color: var(--text-primary);
}

.summary-text {
  font-size: 28rpx;
}

.queue-head {
  align-items: flex-start;
  gap: 16rpx;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  justify-content: flex-end;
}

.filter-btn {
  padding: 10rpx 20rpx;
  border-radius: 999rpx;
  background: var(--surface-soft);
  color: var(--text-secondary);
  font-size: 24rpx;
}

.filter-btn-active {
  background: var(--brand-secondary);
  color: #fff;
}

.input {
  margin-top: 16rpx;
  padding: 20rpx;
  border-radius: 16rpx;
  background: var(--surface-muted);
}

.btn {
  margin-top: 16rpx;
  background: var(--brand-secondary);
  color: #fff;
  border-radius: 16rpx;
}

.secondary-btn {
  background: var(--surface-soft);
  color: var(--text-primary);
}

.title {
  font-size: 32rpx;
  font-weight: 700;
}

.desc {
  margin-top: 10rpx;
  color: var(--text-secondary);
}

.risk-copy {
  margin-top: 12rpx;
  color: var(--state-risk-fg);
  font-size: 24rpx;
}

.queue-item {
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid var(--border-subtle);
}

.queue-title {
  font-size: 30rpx;
  font-weight: 700;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tag {
  font-size: 24rpx;
  padding: 6rpx 18rpx;
  border-radius: 999rpx;
}

.tag-ready {
  background: var(--state-success-bg);
  color: var(--brand-secondary);
}

.tag-risk {
  background: var(--state-risk-bg);
  color: var(--state-risk-fg);
}

.action-row {
  display: flex;
  gap: 16rpx;
  margin-top: 16rpx;
}

.mini-btn {
  flex: 1;
  border-radius: 16rpx;
  background: var(--surface-soft);
  color: var(--text-primary);
  font-size: 26rpx;
}

.primary-mini-btn {
  background: var(--brand-secondary);
  color: #fff;
}

.verify-result {
  margin-top: 12rpx;
  padding: 16rpx;
  border-radius: 16rpx;
  font-size: 26rpx;
}

.verify-pass {
  background: var(--state-success-bg);
  color: var(--state-success-fg);
}

.verify-block {
  background: var(--state-risk-bg);
  color: var(--state-risk-fg);
}
</style>
