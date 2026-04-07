<template>
  <view class="page">
    <view class="card" v-for="item in orders" :key="item.id">
      <view class="row">
        <view class="title">{{ item.petName }}（{{ item.memberName }}）</view>
        <view :class="['tag', item.status === 'COMPLETED' ? 'tag-done' : 'tag-active']">
          {{ item.status === 'COMPLETED' ? '已完工' : item.status === 'BOOKED' ? '已预约' : item.status }}
        </view>
      </view>
      <view class="desc">技师 {{ item.staffName || '-' }} | 预约 {{ item.scheduledAt || '-' }}</view>
      <view class="desc">费用 ¥{{ item.totalFee }}</view>
    </view>

    <view class="card">
      <view class="title">新建美容单</view>
      <input v-model="form.memberId" class="input" placeholder="会员ID" />
      <input v-model="form.petId" class="input" placeholder="宠物ID" />
      <input v-model="form.staffId" class="input" placeholder="技师员工ID（可选）" />
      <input v-model="form.scheduledAt" class="input" placeholder="预约时间 yyyy-MM-ddTHH:mm:ss（可选）" />
      <input v-model="form.totalFee" class="input" placeholder="服务费用" />
      <button class="btn" @click="submitOrder">创建美容单</button>
      <view v-if="message" class="desc">{{ message }}</view>
    </view>

    <view class="card">
      <view class="title">完工登记</view>
      <input v-model="completeForm.orderId" class="input" placeholder="美容单ID" />
      <input v-model="completeForm.note" class="input" placeholder="完工备注" />
      <button class="btn" @click="submitComplete">确认完工</button>
      <view v-if="completeMessage" class="desc">{{ completeMessage }}</view>
    </view>
  </view>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { BASE_URL } from '../../config.js'
const orders = ref([])
const message = ref('')
const completeMessage = ref('')

const form = ref({
  storeId: 1,
  memberId: '',
  petId: '',
  staffId: '',
  scheduledAt: '',
  totalFee: '',
})

const completeForm = ref({
  orderId: '',
  note: '',
})

onMounted(() => {
  loadOrders()
})

function loadOrders() {
  uni.request({
    url: `${BASE_URL}/api/m-app/grooming/orders`,
    success: (res) => {
      orders.value = res.data?.data || []
    },
  })
}

function submitOrder() {
  message.value = ''
  uni.request({
    url: `${BASE_URL}/api/m-app/grooming/orders`,
    method: 'POST',
    header: { 'Content-Type': 'application/json' },
    data: {
      storeId: Number(form.value.storeId),
      memberId: Number(form.value.memberId),
      petId: Number(form.value.petId),
      staffId: form.value.staffId ? Number(form.value.staffId) : null,
      scheduledAt: form.value.scheduledAt || null,
      totalFee: form.value.totalFee ? Number(form.value.totalFee) : 0,
    },
    success: (res) => {
      const id = res.data?.data?.id
      message.value = id ? `美容单创建成功，ID ${id}` : '创建完成'
      form.value.memberId = ''
      form.value.petId = ''
      form.value.staffId = ''
      form.value.scheduledAt = ''
      form.value.totalFee = ''
      loadOrders()
    },
    fail: () => {
      message.value = '创建失败'
    },
  })
}

function submitComplete() {
  completeMessage.value = ''
  uni.request({
    url: `${BASE_URL}/api/m-app/grooming/orders/complete`,
    method: 'POST',
    header: { 'Content-Type': 'application/json' },
    data: {
      orderId: Number(completeForm.value.orderId),
      note: completeForm.value.note,
    },
    success: () => {
      completeMessage.value = '完工确认成功'
      completeForm.value.orderId = ''
      completeForm.value.note = ''
      loadOrders()
    },
    fail: () => {
      completeMessage.value = '提交失败'
    },
  })
}
</script>

<style scoped>
.page {
  padding: 24rpx;
}

.card {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 30rpx;
  font-weight: 700;
}

.desc {
  margin-top: 10rpx;
  color: var(--text-secondary);
  font-size: 26rpx;
}

.tag {
  font-size: 24rpx;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
}

.tag-active {
  background: var(--state-warning-bg);
  color: var(--state-warning-fg);
}

.tag-done {
  background: var(--state-success-bg);
  color: var(--state-success-fg);
}

.input {
  margin-top: 16rpx;
  padding: 20rpx;
  border-radius: 16rpx;
  background: var(--surface-muted);
  display: block;
  width: 100%;
}

.btn {
  margin-top: 16rpx;
  background: var(--brand-secondary);
  color: #fff;
  border-radius: 16rpx;
}
</style>
