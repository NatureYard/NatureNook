var api = require('../../utils/api')
var fmt = require('../../utils/formatters')

var TABS = [
  { key: 'all', label: '全部' },
  { key: 'active', label: '待使用' },
  { key: 'done', label: '已完成' },
  { key: 'other', label: '其他' },
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

    if (tab === 'active') {
      filtered = all.filter(function (o) {
        return o.rawStatus === 'PAID' || o.rawStatus === 'BOOKED'
      })
    } else if (tab === 'done') {
      filtered = all.filter(function (o) {
        return o.rawStatus === 'USED'
      })
    } else if (tab === 'other') {
      filtered = all.filter(function (o) {
        return o.rawStatus === 'CANCELLED' || o.rawStatus === 'REFUNDED'
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
})
