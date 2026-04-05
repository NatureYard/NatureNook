<template>
  <view class="page">
    <view class="card" v-for="item in stocks" :key="item.name">
      <view class="title">{{ item.name }}</view>
      <view class="desc">{{ item.category }} | 当前库存 {{ item.qty }}</view>
    </view>

    <view class="card">
      <view class="title">物资领用</view>
      <input v-model="form.materialItemId" class="input" placeholder="物资ID" />
      <input v-model="form.quantity" class="input" placeholder="领用数量" />
      <input v-model="form.bizType" class="input" placeholder="业务类型，如 BOARDING" />
      <button class="btn" @click="submit">提交领用</button>
      <view v-if="message" class="desc">{{ message }}</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { BASE_URL } from '../../config.js'

const stocks = [
  { name: '犬用基础粮', category: '饲料', qty: '32kg' },
  { name: '地面消毒液', category: '清洁用品', qty: '16瓶' },
  { name: '低敏沐浴露', category: '洗护耗材', qty: '6瓶' },
]

const form = ref({
  storeId: 1,
  warehouseId: 1,
  staffId: 2,
  materialItemId: '',
  quantity: '',
  unit: '瓶',
  bizType: '',
  bizId: null,
  remark: '商户移动端领用',
})

const message = ref('')

function submit() {
  uni.request({
    url: `${BASE_URL}/api/admin/materials/issues`,
    method: 'POST',
    header: {
      'Content-Type': 'application/json',
    },
    data: {
      ...form.value,
      materialItemId: Number(form.value.materialItemId),
      quantity: Number(form.value.quantity),
      bizId: form.value.bizId,
    },
    success: (res) => {
      const id = res.data?.data?.id
      message.value = id ? `领用成功，单号 ${id}` : '领用已提交'
      form.value.materialItemId = ''
      form.value.quantity = ''
      form.value.bizType = ''
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
