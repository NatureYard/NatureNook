<template>
  <view class="page">
    <view class="card" v-for="item in orders" :key="item.id">
      <view class="row">
        <view class="title">{{ item.petName }}（{{ item.memberName }}）</view>
        <view :class="['tag', item.status === 'CHECKED_IN' ? 'tag-active' : 'tag-done']">
          {{ item.status === 'CHECKED_IN' ? '在住' : item.status }}
        </view>
      </view>
      <view class="desc">笼位 {{ item.cageNo || '-' }} | 入住 {{ item.checkInTime }}</view>
      <view class="desc">计划离园 {{ item.plannedCheckOutTime }}</view>
    </view>

    <view class="card">
      <view class="title">新建寄养单</view>
      <input v-model="form.memberId" class="input" placeholder="会员ID" />
      <input v-model="form.petId" class="input" placeholder="宠物ID" />
      <input v-model="form.cageNo" class="input" placeholder="笼位编号" />
      <input v-model="form.checkInTime" class="input" placeholder="入住时间 yyyy-MM-ddTHH:mm:ss" />
      <input v-model="form.plannedCheckOutTime" class="input" placeholder="计划离园 yyyy-MM-ddTHH:mm:ss" />
      <button class="btn" @click="submitOrder">创建寄养单</button>
      <view v-if="message" class="desc">{{ message }}</view>
    </view>

    <view class="card">
      <view class="title">添加每日记录</view>
      <input v-model="dailyForm.boardingOrderId" class="input" placeholder="寄养单ID" />
      <input v-model="dailyForm.recordDate" class="input" placeholder="记录日期 yyyy-MM-dd" />
      <input v-model="dailyForm.healthNote" class="input" placeholder="健康备注" />
      <input v-model="dailyForm.exceptionNote" class="input" placeholder="异常记录（可选）" />
      <button class="btn" @click="submitDailyRecord">提交记录</button>
      <view v-if="dailyMessage" class="desc">{{ dailyMessage }}</view>
    </view>
  </view>
</template>

<script setup>
import { onMounted, ref } from 'vue'

const BASE_URL = 'http://localhost:8080'
const orders = ref([])
const message = ref('')
const dailyMessage = ref('')

const form = ref({
  storeId: 1,
  memberId: '',
  petId: '',
  cageNo: '',
  checkInTime: '',
  plannedCheckOutTime: '',
})

const dailyForm = ref({
  boardingOrderId: '',
  recordDate: '',
  healthNote: '',
  exceptionNote: '',
  staffId: 2,
})

onMounted(() => {
  loadOrders()
})

function loadOrders() {
  uni.request({
    url: `${BASE_URL}/api/m-app/boarding/orders`,
    success: (res) => {
      orders.value = res.data?.data || []
    },
  })
}

function submitOrder() {
  message.value = ''
  uni.request({
    url: `${BASE_URL}/api/m-app/boarding/orders`,
    method: 'POST',
    header: { 'Content-Type': 'application/json' },
    data: {
      storeId: Number(form.value.storeId),
      memberId: Number(form.value.memberId),
      petId: Number(form.value.petId),
      cageNo: form.value.cageNo,
      checkInTime: form.value.checkInTime,
      plannedCheckOutTime: form.value.plannedCheckOutTime,
    },
    success: (res) => {
      const id = res.data?.data?.id
      message.value = id ? `寄养单创建成功，ID ${id}` : '创建完成'
      form.value.memberId = ''
      form.value.petId = ''
      form.value.cageNo = ''
      form.value.checkInTime = ''
      form.value.plannedCheckOutTime = ''
      loadOrders()
    },
    fail: () => {
      message.value = '创建失败'
    },
  })
}

function submitDailyRecord() {
  dailyMessage.value = ''
  uni.request({
    url: `${BASE_URL}/api/m-app/boarding/daily-records`,
    method: 'POST',
    header: { 'Content-Type': 'application/json' },
    data: {
      boardingOrderId: Number(dailyForm.value.boardingOrderId),
      recordDate: dailyForm.value.recordDate,
      healthNote: dailyForm.value.healthNote,
      exceptionNote: dailyForm.value.exceptionNote,
      staffId: dailyForm.value.staffId,
    },
    success: (res) => {
      const id = res.data?.data?.id
      dailyMessage.value = id ? `记录提交成功，ID ${id}` : '提交完成'
      dailyForm.value.boardingOrderId = ''
      dailyForm.value.recordDate = ''
      dailyForm.value.healthNote = ''
      dailyForm.value.exceptionNote = ''
    },
    fail: () => {
      dailyMessage.value = '提交失败'
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
  color: #4b5563;
  font-size: 26rpx;
}

.tag {
  font-size: 24rpx;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
}

.tag-active {
  background: #d1fae5;
  color: #065f46;
}

.tag-done {
  background: #f3f4f6;
  color: #6b7280;
}

.input {
  margin-top: 16rpx;
  padding: 20rpx;
  border-radius: 16rpx;
  background: #f5f7f8;
  display: block;
  width: 100%;
}

.btn {
  margin-top: 16rpx;
  background: #0f766e;
  color: #fff;
  border-radius: 16rpx;
}
</style>
