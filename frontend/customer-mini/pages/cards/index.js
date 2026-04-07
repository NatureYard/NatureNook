var api = require('../../utils/api')
var fmt = require('../../utils/formatters')

Page({
  data: {
    cards: [],
    loading: true,
    error: '',
  },

  onLoad: function () {
    this.loadCards()
  },

  onPullDownRefresh: function () {
    this.loadCards()
  },

  loadCards: function () {
    this.setData({ loading: true, error: '' })
    var self = this

    api.fetchCards()
      .then(function (data) {
        self.setData({
          loading: false,
          cards: (data || []).map(function (item) {
            var progress = fmt.getValidityProgress(item.validFrom, item.validTo)
            var remaining = fmt.formatDaysRemaining(item.validTo)
            var isExpiringSoon = remaining.indexOf('剩余') >= 0 &&
              parseInt(remaining.replace(/[^0-9]/g, ''), 10) <= 30

            return {
              id: item.id,
              name: item.name,
              desc: item.desc,
              meta: '有效期至 ' + (item.validTo || '--'),
              status: fmt.formatCardStatus(item.status),
              statusClass: fmt.getCardStatusClass(item.status),
              price: fmt.formatPrice(item.price),
              remaining: remaining,
              progress: progress,
              isExpiringSoon: isExpiringSoon,
              benefits: item.benefits || [],
              validFrom: item.validFrom || '',
              validTo: item.validTo || '',
            }
          }),
        })
      })
      .catch(function (error) {
        self.setData({
          loading: false,
          cards: [],
          error: error.message || '卡种加载失败，请稍后重试',
        })
      })
      .then(function () {
        wx.stopPullDownRefresh()
      })
  },

  retry: function () {
    this.loadCards()
  },

  openTickets: function () {
    wx.switchTab({ url: '/pages/tickets/index' })
  },
})
