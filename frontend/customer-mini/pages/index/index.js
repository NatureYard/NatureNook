const { request } = require('../../utils/request')

const entryPathMap = {
  '购票预约': '/pages/tickets/index',
  '卡种中心': '/pages/cards/index',
  '我的宠物': '/pages/pets/index',
  '入园凭证': '/pages/orders/index',
}

Page({
  data: {
    banners: [],
    quickEntries: [],
    activeCardTip: '',
    memberName: '当前会员',
    memberMeta: '查看权益、凭证和售后记录',
    orderCount: 0,
    loading: true,
  },

  onLoad() {
    Promise.all([
      request('/api/c-app/home'),
      request('/api/c-app/profile'),
      request('/api/c-app/orders'),
    ])
      .then(([home, profile, orders]) => {
        this.setData({
          banners: home.banners,
          quickEntries: home.quickEntries.map((label) => ({
            label,
            desc: this.describeEntry(label),
            path: entryPathMap[label] || '/pages/index/index',
          })),
          activeCardTip: home.activeCardTip,
          memberName: profile.memberName,
          memberMeta: `${profile.memberLevel} · ${profile.storeName}`,
          orderCount: orders.length,
          loading: false,
        })
      })
      .catch(() => {
        this.setData({
          banners: ['周末寄养优惠', '年卡限时折扣', '洗护套餐立减'],
          quickEntries: [
            { label: '购票预约', desc: '门票、洗护、寄养预约统一入口', path: '/pages/tickets/index' },
            { label: '卡种中心', desc: '查看月卡、季卡、年卡权益', path: '/pages/cards/index' },
            { label: '我的宠物', desc: '查看宠物档案和基础信息', path: '/pages/pets/index' },
            { label: '入园凭证', desc: '查看订单与当日凭证状态', path: '/pages/orders/index' },
          ],
          activeCardTip: '年卡会员可在有效期内多次入园',
          memberName: '张三',
          memberMeta: 'SILVER · 上海萌宠乐园旗舰店',
          orderCount: 2,
          loading: false,
        })
      })
  },

  describeEntry(label) {
    if (label === '购票预约') return '门票、洗护、寄养预约统一入口'
    if (label === '卡种中心') return '查看月卡、季卡、年卡权益'
    if (label === '我的宠物') return '查看宠物档案和基础信息'
    if (label === '入园凭证') return '查看订单与当日凭证状态'
    return '进入服务详情'
  },

  openEntry(event) {
    const { path } = event.currentTarget.dataset
    if (!path) return
    wx.switchTab({
      url: path,
      fail: () => {
        wx.navigateTo({ url: path })
      },
    })
  },

  openOrders() {
    wx.switchTab({
      url: '/pages/orders/index',
    })
  },

  openProfile() {
    wx.switchTab({
      url: '/pages/profile/index',
    })
  },
})
