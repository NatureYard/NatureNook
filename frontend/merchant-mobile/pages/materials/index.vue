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

    <view class="card">
      <view class="title">报损上报</view>
      <input v-model="lossForm.materialItemId" class="input" placeholder="物资ID" />
      <input v-model="lossForm.quantity" class="input" placeholder="报损数量" />
      <input v-model="lossForm.unit" class="input" placeholder="单位，如 瓶、kg" />
      <input v-model="lossForm.reason" class="input" placeholder="报损原因" />
      <button class="btn" @click="submitLoss">提交报损</button>
      <view v-if="lossMessage" class="desc">{{ lossMessage }}</view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { BASE_URL } from '../../config.js'

const defaultStocks = [
  { name: '犬用基础粮', category: '饲料', qty: '32kg' },
  { name: '地面消毒液', category: '清洁用品', qty: '16瓶' },
  { name: '低敏沐浴露', category: '洗护耗材', qty: '6瓶' },
]

const stocks = ref([...defaultStocks])

function fetchStocks() {
  uni.request({
    url: `${BASE_URL}/api/m-app/materials/stocks`,
    method: 'GET',
    success: (res) => {
      const data = res.data?.data
      if (Array.isArray(data) && data.length > 0) {
        stocks.value = data.map((item) => ({
          name: item.name,
          category: item.category,
          qty: `${item.quantity}${item.unit}`,
        }))
      } else {
        stocks.value = [...defaultStocks]
      }
    },
    fail: () => {
      stocks.value = [...defaultStocks]
    },
  })
}

onMounted(fetchStocks)

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

const lossForm = ref({
  storeId: 1,
  warehouseId: 1,
  staffId: 2,
  materialItemId: '',
  quantity: '',
  unit: '瓶',
  reason: '',
  remark: '商户移动端报损',
})

const lossMessage = ref('')

function submitLoss() {
  uni.request({
    url: `${BASE_URL}/api/m-app/materials/loss-report`,
    method: 'POST',
    header: {
      'Content-Type': 'application/json',
    },
    data: {
      ...lossForm.value,
      materialItemId: Number(lossForm.value.materialItemId),
      quantity: Number(lossForm.value.quantity),
    },
    success: (res) => {
      const id = res.data?.data?.id
      lossMessage.value = id ? `报损成功，单号 ${id}` : '报损已提交'
      lossForm.value.materialItemId = ''
      lossForm.value.quantity = ''
      lossForm.value.reason = ''
    },
    fail: () => {
      lossMessage.value = '提交失败'
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
