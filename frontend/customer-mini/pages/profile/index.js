var api = require('../../utils/api')
var fmt = require('../../utils/formatters')

Page({
  data: {
    loading: true,
    error: '',
    avatar: '',
    memberName: '当前会员',
    memberLevel: '',
    storeName: '',
    phone: '',
    faceStatus: '',
    petCount: 0,
    cardCount: 0,
    orderCount: 0,
  },

  onShow: function () {
    this.loadProfile()
  },

  onPullDownRefresh: function () {
    this.loadProfile()
  },

  loadProfile: function () {
    this.setData({ loading: true, error: '' })
    var self = this

    Promise.all([api.fetchProfile(), api.fetchContext(), api.fetchOrders()])
      .then(function (results) {
        var profile = results[0] || {}
        var context = results[1] || {}
        var orders = results[2] || []

        self.setData({
          loading: false,
          avatar: profile.memberName ? profile.memberName.substring(0, 1) : '?',
          memberName: profile.memberName || '当前会员',
          memberLevel: fmt.formatMemberLevel(profile.memberLevel),
          storeName: profile.storeName || '未分配门店',
          phone: profile.phone || '未绑定',
          faceStatus: profile.faceStatus === 'REGISTERED' ? '已录入' : '未录入',
          petCount: (context.pets || []).length,
          cardCount: (context.cards || []).length,
          orderCount: orders.length,
        })
      })
      .catch(function (error) {
        self.setData({
          loading: false,
          error: error.message || '会员信息加载失败，请稍后重试',
        })
      })
      .then(function () {
        wx.stopPullDownRefresh()
      })
  },

  retry: function () {
    this.loadProfile()
  },

  openOrders: function () {
    wx.switchTab({ url: '/pages/orders/index' })
  },

  openCards: function () {
    wx.navigateTo({ url: '/pages/cards/index' })
  },

  openPets: function () {
    wx.navigateTo({ url: '/pages/pets/index' })
  },

  openTickets: function () {
    wx.switchTab({ url: '/pages/tickets/index' })
  },
})
