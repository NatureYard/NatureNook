var api = require('../../utils/api')
var fmt = require('../../utils/formatters')
var config = require('../../utils/config')
var req = require('../../utils/request')

var TABS = [
  { key: 'all', label: '全部' },
  { key: 'pending_pay', label: '待支付' },
  { key: 'active', label: '待使用' },
  { key: 'done', label: '已完成' },
]

Page({
  data: {
    allOrders: [],
    orders: [],
    loading: true,
    error: '',
    tabs: TABS,
    activeTab: 'all',
  },

  onShow: function () {
    this.loadOrders()
  },

  onPullDownRefresh: function () {
    this.loadOrders()
  },

  loadOrders: function () {
    this.setData({ loading: true, error: '' })
    var self = this

    api.fetchOrders()
      .then(function (data) {
        var allOrders = (data || []).map(function (item) {
          var isToday = fmt.isToday(item.reservationDate)
          return {
            no: item.orderNo,
            type: item.type,
            rawStatus: item.status,
            status: fmt.formatOrderStatus(item.status),
            amount: fmt.formatPrice(item.amount),
            statusClass: fmt.getOrderStatusClass(item.status),
            storeName: item.storeName || '未分配门店',
            reservationInfo: fmt.formatReservationInfo(item),
            dateFriendly: item.reservationDate ? fmt.formatDateFriendly(item.reservationDate) : '',
            createdAt: item.createdAt || '',
            isToday: isToday,
            canUse: (item.status === 'PAID' || item.status === 'BOOKED') && isToday,
            needPay: item.status === 'PENDING_PAY',
            passEntitlementId: item.passEntitlementId || null,
          }
        })

        self.setData({
          loading: false,
          allOrders: allOrders,
        })
        self.filterOrders(self.data.activeTab)
      })
      .catch(function (error) {
        self.setData({
          loading: false,
          allOrders: [],
          orders: [],
          error: error.message || '订单加载失败，请稍后重试',
        })
      })
      .then(function () {
        wx.stopPullDownRefresh()
      })
  },

  switchTab: function (event) {
    var key = event.currentTarget.dataset.key
    this.setData({ activeTab: key })
    this.filterOrders(key)
  },

  filterOrders: function (tab) {
    var all = this.data.allOrders
    var filtered

    if (tab === 'pending_pay') {
      filtered = all.filter(function (o) {
        return o.rawStatus === 'PENDING_PAY'
      })
    } else if (tab === 'active') {
      filtered = all.filter(function (o) {
        return o.rawStatus === 'PAID' || o.rawStatus === 'BOOKED'
      })
    } else if (tab === 'done') {
      filtered = all.filter(function (o) {
        return o.rawStatus === 'USED' || o.rawStatus === 'CANCELLED' || o.rawStatus === 'REFUNDED'
      })
    } else {
      filtered = all
    }

    this.setData({ orders: filtered })
  },

  retry: function () {
    this.loadOrders()
  },

  openTickets: function () {
    wx.switchTab({ url: '/pages/tickets/index' })
  },

  openPassQr: function (event) {
    var passId = event.currentTarget.dataset.passid
    if (!passId) {
      wx.showToast({ title: '凭证信息异常', icon: 'none' })
      return
    }
    wx.navigateTo({ url: '/pages/pass-qr/index?passEntitlementId=' + passId })
  },

  payOrder: function (event) {
    var orderNo = event.currentTarget.dataset.orderno
    if (!orderNo) return

    var self = this
    wx.showLoading({ title: '发起支付...' })

    api.prepay(orderNo)
      .then(function (prepayData) {
        wx.hideLoading()
        // 开发模式跳过真实支付
        if (config.shouldUseMockFallback()) {
          return Promise.resolve()
        }
        return new Promise(function (resolve, reject) {
          wx.requestPayment({
            timeStamp: prepayData.timeStamp,
            nonceStr: prepayData.nonceStr,
            package: prepayData.packageValue,
            signType: prepayData.signType || 'RSA',
            paySign: prepayData.paySign,
            success: function () { resolve() },
            fail: function (err) { reject(new Error(err.errMsg || '支付失败')) },
          })
        })
      })
      .then(function () {
        return api.confirmPayment(orderNo)
      })
      .then(function () {
        wx.showToast({ title: '支付成功', icon: 'success' })
        self.loadOrders()
      })
      .catch(function (error) {
        wx.hideLoading()
        var msg = error.message || ''
        if (msg.indexOf('cancel') >= 0 || msg.indexOf('取消') >= 0) {
          wx.showToast({ title: '已取消支付', icon: 'none' })
        } else {
          req.showRequestError(error, '支付失败，请稍后重试')
        }
      })
  },
})
