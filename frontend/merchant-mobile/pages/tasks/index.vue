<template>
  <view class="page">
    <view class="card" v-for="item in tasks" :key="item.title">
      <view class="row">
        <view class="title">{{ item.title }}</view>
        <view class="count">{{ item.count }}</view>
      </view>
      <view class="desc">{{ item.desc }}</view>
    </view>
  </view>
</template>

<script setup>
const tasks = [
  { title: '待核销到店', count: 12, desc: '优先处理到店预约和闸机异常' },
  { title: '待补货物资', count: 3, desc: '低库存物资需尽快补货' },
  { title: '待复核异常', count: 2, desc: '人工放行和报损异常待店长确认' },
]
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
  font-size: 32rpx;
  font-weight: 700;
}

.count {
  color: #0f766e;
  font-size: 34rpx;
  font-weight: 700;
}

.desc {
  margin-top: 10rpx;
  color: #4b5563;
}
</style>
