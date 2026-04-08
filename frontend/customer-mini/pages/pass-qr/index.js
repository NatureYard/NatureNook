var api = require('../../utils/api')
var qrcode = require('../../utils/qrcode')

var REFRESH_INTERVAL = 25 // 秒

Page({
  data: {
    passEntitlementId: null,
    passName: '',
    storeName: '',
    expiresAt: '',
    qrContent: '',
    loading: true,
    error: '',
    countdown: 0,
    reporting: false,
  },

  _timer: null,
  _countdownTimer: null,

  onLoad: function (options) {
    if (!options.passEntitlementId) {
      this.setData({ loading: false, error: '缺少凭证信息' })
      return
    }
    this.setData({ passEntitlementId: options.passEntitlementId })
    this.generateQrCode()
  },

  onUnload: function () {
    if (this._timer) clearInterval(this._timer)
    if (this._countdownTimer) clearInterval(this._countdownTimer)
  },

  generateQrCode: function () {
    var self = this
    this.setData({ loading: true, error: '' })

    api.generateQrCode(this.data.passEntitlementId)
      .then(function (data) {
        self.setData({
          loading: false,
          qrContent: data.qrContent,
          expiresAt: data.expiresAt,
          passName: data.passName,
          storeName: data.storeName,
          countdown: REFRESH_INTERVAL,
        })

        qrcode.drawQrcode('qrCanvas', data.qrContent, 200)

        self.startAutoRefresh()
      })
      .catch(function (err) {
        self.setData({
          loading: false,
          error: err.message || '二维码生成失败，请重试',
        })
      })
  },

  startAutoRefresh: function () {
    var self = this
    if (this._timer) clearInterval(this._timer)
    if (this._countdownTimer) clearInterval(this._countdownTimer)

    this.setData({ countdown: REFRESH_INTERVAL })

    this._countdownTimer = setInterval(function () {
      var remaining = self.data.countdown - 1
      if (remaining <= 0) remaining = 0
      self.setData({ countdown: remaining })
    }, 1000)

    this._timer = setInterval(function () {
      self.generateQrCode()
    }, REFRESH_INTERVAL * 1000)
  },

  retry: function () {
    this.generateQrCode()
  },

  reportUnauthorized: function () {
    var self = this
    if (this.data.reporting) return

    wx.showModal({
      title: '举报非本人操作',
      content: '如果这张入园凭证不是您本人使用，点击确认后我们将通知门店工作人员核实处理。',
      confirmText: '确认举报',
      confirmColor: '#dc2626',
      success: function (res) {
        if (!res.confirm) return

        self.setData({ reporting: true })
        api.reportUnauthorizedEntry(self.data.passEntitlementId, '非本人操作')
          .then(function (data) {
            wx.showToast({ title: '已收到举报', icon: 'success' })
          })
          .catch(function (err) {
            wx.showToast({ title: err.message || '举报失败', icon: 'none' })
          })
          .then(function () {
            self.setData({ reporting: false })
          })
      }
    })
  },
})
