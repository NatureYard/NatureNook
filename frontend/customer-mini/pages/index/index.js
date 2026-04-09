var api = require('../../utils/api')
var fmt = require('../../utils/formatters')

var entryPathMap = {
  '购票预约': '/pages/tickets/index',
  '卡种中心': '/pages/cards/index',
  '我的宠物': '/pages/pets/index',
  '入园凭证': '/pages/orders/index',
}

Page({
  data: {
    loading: true,
    error: '',
    banners: [],
    quickEntries: [],
    activeCardTip: '',
    memberName: '当前会员',
    memberLevel: '',
    storeName: '',
    orderCount: 0,
    petCount: 0,
    cardCount: 0,
    todayPass: '',
    swiperCurrent: 0,
  },

  onLoad: function () {
    this.loadPage()
  },

  onPullDownRefresh: function () {
    this.loadPage()
  },

  onShow: function () {
    if (!this.data.loading && !this.data.error) {
      this.refreshCounts()
    }
  },

  loadPage: function () {
    this.setData({ loading: true, error: '' })

    var self = this
    Promise.all([
      api.fetchHome(),
      api.fetchProfile(),
      api.fetchOrders(),
      api.fetchContext(),
    ])
      .then(function (results) {
        var home = results[0] || {}
        var profile = results[1] || {}
        var orders = results[2] || []
        var context = results[3] || {}
        var pets = context.pets || []
        var cards = context.cards || []

        var todayOrders = orders.filter(function (o) {
          return fmt.isToday(o.reservationDate) && (o.status === 'PAID' || o.status === 'BOOKED')
        })
        var todayPass = ''
        var todayPassId = ''
        if (todayOrders.length > 0) {
          todayPass = '您有今日可用入园凭证（' + todayOrders.length + '张）'
          todayPassId = todayOrders[0].passEntitlementId || ''
        }

        self.setData({
          banners: home.banners || [],
          quickEntries: (home.quickEntries || []).map(function (label) {
            return {
              label: label,
              desc: self.describeEntry(label),
              path: entryPathMap[label] || '/pages/index/index',
              icon: fmt.getEntryIcon(label),
            }
          }),
          activeCardTip: home.activeCardTip || '',
          memberName: profile.memberName || '当前会员',
          memberLevel: fmt.formatMemberLevel(profile.memberLevel),
          storeName: profile.storeName || '未分配门店',
          orderCount: orders.length,
          petCount: pets.length,
          cardCount: cards.length,
          todayPass: todayPass,
          todayPassId: todayPassId,
          loading: false,
        })
      })
      .catch(function (error) {
        self.setData({
          loading: false,
          error: error.message || '首页加载失败，请下拉重试',
        })
      })
      .then(function () {
        wx.stopPullDownRefresh()
      })
  },

  refreshCounts: function () {
    var self = this
    Promise.all([api.fetchOrders(), api.fetchContext()])
      .then(function (results) {
        var orders = results[0] || []
        var context = results[1] || {}
        var todayOrders = orders.filter(function (o) {
          return fmt.isToday(o.reservationDate) && (o.status === 'PAID' || o.status === 'BOOKED')
        })
        self.setData({
          orderCount: orders.length,
          petCount: (context.pets || []).length,
          cardCount: (context.cards || []).length,
          todayPass: todayOrders.length > 0 ? '您有今日可用入园凭证（' + todayOrders.length + '张）' : '',
          todayPassId: todayOrders.length > 0 ? (todayOrders[0].passEntitlementId || '') : '',
        })
      })
      .catch(function () {})
  },

  describeEntry: function (label) {
    if (label === '购票预约') return '门票 / 洗护 / 寄养'
    if (label === '卡种中心') return '月卡 / 季卡 / 年卡'
    if (label === '我的宠物') return '宠物档案管理'
    if (label === '入园凭证') return '订单与凭证状态'
    return '进入详情'
  },

  onSwiperChange: function (e) {
    this.setData({ swiperCurrent: e.detail.current })
  },

  openEntry: function (event) {
    var path = event.currentTarget.dataset.path
    if (!path) return
    wx.switchTab({
      url: path,
      fail: function () {
        wx.navigateTo({ url: path })
      },
    })
  },

  openOrders: function () {
    wx.switchTab({ url: '/pages/orders/index' })
  },

  openPass: function () {
    if (this.data.todayPassId) {
      wx.navigateTo({ url: '/pages/pass-qr/index?id=' + this.data.todayPassId })
    } else {
      wx.switchTab({ url: '/pages/orders/index' })
    }
  },

  openPets: function () {
    wx.navigateTo({ url: '/pages/pets/index' })
  },

  openCards: function () {
    wx.navigateTo({ url: '/pages/cards/index' })
  },

  openProfile: function () {
    wx.switchTab({ url: '/pages/profile/index' })
  },

  openTickets: function () {
    wx.switchTab({ url: '/pages/tickets/index' })
  },

  retry: function () {
    this.loadPage()
  },
})
