<template>
  <view class="page">
    <view class="panel">
      <view class="title">商户移动端工作台</view>
      <view class="desc">门店现场作业入口，覆盖核销、寄养、美容、物资和风险处理。</view>
    </view>
    <view class="grid">
      <view v-for="item in items" :key="item.title" class="item">
        <view class="item-title">{{ item.title }}</view>
        <view class="item-desc">{{ item.desc }}</view>
        <view class="item-count">{{ item.count }}</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onMounted, ref } from 'vue'

const items = ref([
  { title: '现场核销', desc: '订单核验、闸机异常、人工放行', count: 0 },
  { title: '寄养履约', desc: '入住、喂养记录、异常上报', count: 0 },
  { title: '物资管理', desc: '领用、盘点、报损、补货提醒', count: 0 },
  { title: '待复核异常', desc: '风险事件与人工放行复核', count: 0 },
])

onMounted(() => {
  uni.request({
    url: 'http://localhost:8080/api/m-app/task-board',
    success: (res) => {
      const rows = res.data?.data || []
      items.value = [
        { title: '现场核销', desc: '订单核验、闸机异常、人工放行', count: rows.find((x) => x.type === 'CHECKIN')?.count || 0 },
        { title: '寄养履约', desc: '入住、喂养记录、异常上报', count: rows.find((x) => x.type === 'BOARDING')?.count || 0 },
        { title: '物资管理', desc: '领用、盘点、报损、补货提醒', count: rows.find((x) => x.type === 'MATERIAL')?.count || 0 },
        { title: '待复核异常', desc: '风险事件与人工放行复核', count: rows.find((x) => x.type === 'RISK')?.count || 0 },
      ]
    },
  })
})
</script>

<style scoped>
.page {
  padding: 24rpx;
}

.panel,
.item {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
}

.title {
  font-size: 36rpx;
  font-weight: 700;
}

.desc {
  margin-top: 12rpx;
  color: #4b5563;
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
  margin-top: 16rpx;
}

.item-title {
  font-size: 30rpx;
  font-weight: 700;
}

.item-desc {
  margin-top: 10rpx;
  color: #4b5563;
  line-height: 1.6;
}

.item-count {
  margin-top: 12rpx;
  color: #0f766e;
  font-size: 34rpx;
  font-weight: 700;
}
</style>
