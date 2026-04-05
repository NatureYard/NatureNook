<template>
  <view class="page">
    <view class="panel-head">
      <view class="section-title">任务看板</view>
      <button class="btn-refresh" @click="fetchTasks">刷新</button>
    </view>
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
import { ref, onMounted } from 'vue'
import { BASE_URL } from '../../config.js'

const defaultTasks = [
  { title: '待核销到店', count: 12, desc: '优先处理到店预约和闸机异常' },
  { title: '待补货物资', count: 3, desc: '低库存物资需尽快补货' },
  { title: '待复核异常', count: 2, desc: '人工放行和报损异常待店长确认' },
]

const tasks = ref([...defaultTasks])

function fetchTasks() {
  uni.request({
    url: `${BASE_URL}/api/m-app/task-board`,
    method: 'GET',
    success: (res) => {
      const data = res.data?.data
      if (Array.isArray(data) && data.length > 0) {
        tasks.value = data.map((item) => ({
          title: item.title,
          count: item.count,
          desc: item.desc,
        }))
      } else {
        tasks.value = [...defaultTasks]
      }
    },
    fail: () => {
      tasks.value = [...defaultTasks]
    },
  })
}

onMounted(fetchTasks)
</script>

<style scoped>
.page {
  padding: 24rpx;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 700;
}

.btn-refresh {
  font-size: 24rpx;
  padding: 8rpx 24rpx;
  background: var(--brand-secondary);
  color: #fff;
  border-radius: 12rpx;
}

.card {
  background: var(--surface-card);
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
  color: var(--brand-secondary);
  font-size: 34rpx;
  font-weight: 700;
}

.desc {
  margin-top: 10rpx;
  color: var(--text-secondary);
}
</style>
