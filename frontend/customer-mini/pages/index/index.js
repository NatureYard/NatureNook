const { request } = require('../../utils/request')

const entryPathMap = {
  '购票 / 预约': '/pages/tickets/index',
  '卡种中心': '/pages/cards/index',
  '我的宠物': '/pages/pets/index',
  '入园凭证': '/pages/orders/index',
}

const entryMetaMap = {
  '购票 / 预约': { icon: '🎟️', kicker: '本周热卖', toneClass: 'tone-sun' },
  '卡种中心': { icon: '💳', kicker: '省心入园', toneClass: 'tone-rose' },
  '我的宠物': { icon: '🐾', kicker: '档案管理', toneClass: 'tone-mint' },
  '入园凭证': { icon: '✨', kicker: '随时核验', toneClass: 'tone-sky' },
}

const bannerMeta = [
  { tag: '限时推荐', desc: '周末带宠出游更划算', toneClass: 'banner-sun' },
  { tag: '会员礼遇', desc: '高频到店更适合长期权益', toneClass: 'banner-mint' },
  { tag: '热门服务', desc: '美容洗护和寄养可一站预约', toneClass: 'banner-rose' },
]

function mapEntry(label) {
  const meta = entryMetaMap[label] || { icon: '✨', kicker: '乐园服务', toneClass: 'tone-sun' }
  return {
    label,
    path: entryPathMap[label] || '/pages/index/index',
    desc: describeEntry(label),
    icon: meta.icon,
    kicker: meta.kicker,
    toneClass: meta.toneClass,
  }
}

function mapBanner(title, index) {
  const meta = bannerMeta[index % bannerMeta.length]
  return {
    title,
    tag: meta.tag,
    desc: meta.desc,
    toneClass: meta.toneClass,
  }
}

function describeEntry(label) {
  if (label === '购票 / 预约') return '门票、洗护、寄养预约统一入口'
  if (label === '卡种中心') return '查看月卡、季卡、年卡权益'
  if (label === '我的宠物') return '查看宠物档案和基础信息'
  if (label === '入园凭证') return '查看订单与当日凭证状态'
  return '进入服务详情'
}

function formatMemberMeta(profile) {
  const level = profile?.memberLevel ? `会员等级 ${profile.memberLevel}` : '会员权益待同步'
  const store = profile?.storeName || '门店待分配'
  return `${level} · ${store}`
}

function formatNextStep(orderCount) {
  if (orderCount > 0) return '已同步最近订单，你可以直接查看凭证或继续预约新的服务。'
  return '先从预约入园开始，支付完成后会自动生成订单与入园凭证。'
}

Page({
  data: {
    banners: [],
    quickEntries: [],
    activeCardTip: '',
    memberName: '当前会员',
    memberMeta: '查看权益、凭证和售后记录',
    orderCount: 0,
    nextStepHint: '先预约，再到店核验，流程会在订单页持续可见。',
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
          banners: home.banners.map(mapBanner),
          quickEntries: home.quickEntries.map(mapEntry),
          activeCardTip: home.activeCardTip,
          memberName: profile.memberName,
          memberMeta: formatMemberMeta(profile),
          orderCount: orders.length,
          nextStepHint: formatNextStep(orders.length),
          loading: false,
        })
      })
      .catch(() => {
        this.setData({
          banners: ['周末寄养优惠', '年卡限时折扣', '洗护套餐立减'].map(mapBanner),
          quickEntries: ['购票 / 预约', '卡种中心', '我的宠物', '入园凭证'].map(mapEntry),
          activeCardTip: '年卡会员可在有效期内多次入园，现场只需亮出凭证即可快速核验。',
          memberName: '张三',
          memberMeta: '会员等级 SILVER · 上海萌宠乐园旗舰店',
          orderCount: 2,
          nextStepHint: formatNextStep(2),
          loading: false,
        })
      })
  },

  openTickets() {
    wx.switchTab({
      url: '/pages/tickets/index',
    })
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
