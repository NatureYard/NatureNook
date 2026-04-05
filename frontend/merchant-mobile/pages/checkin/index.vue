<template>
  <view class="page">
    <view class="card" v-for="item in rows" :key="item.title">
      <view class="title">{{ item.title }}</view>
      <view class="desc">{{ item.desc }}</view>
    </view>

    <view class="card">
      <view class="title">人工放行登记</view>
      <input v-model="form.memberId" class="input" placeholder="会员ID" />
      <input v-model="form.orderId" class="input" placeholder="订单ID" />
      <input v-model="form.reason" class="input" placeholder="放行原因" />
      <button class="btn" @click="submit">提交</button>
      <view v-if="message" class="desc">{{ message }}</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const rows = [
  { title: '待核销到店', desc: '处理预约到店和门票核销' },
  { title: '人工放行', desc: '登记原因、留痕并触发风控' },
  { title: '异常识别', desc: '人脸不匹配、退款后通行、跨店通行' },
]

const form = ref({
  storeId: 1,
  staffId: 2,
  memberId: '',
  orderId: '',
  reason: '',
})

const message = ref('')

function submit() {
  uni.request({
    url: 'http://localhost:8080/api/m-app/manual-releases',
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
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.input {
  margin-top: 16rpx;
  padding: 20rpx;
  border-radius: 16rpx;
  background: #f5f7f8;
}

.btn {
  margin-top: 16rpx;
  background: #0f766e;
  color: #fff;
  border-radius: 16rpx;
}

.title {
  font-size: 32rpx;
  font-weight: 700;
}

.desc {
  margin-top: 10rpx;
  color: #4b5563;
}
</style>
