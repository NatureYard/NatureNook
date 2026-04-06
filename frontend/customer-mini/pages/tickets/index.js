var api = require('../../utils/api')
var fmt = require('../../utils/formatters')
var req = require('../../utils/request')

var DEFAULT_TIME_SLOTS = ['09:00-12:00', '13:00-15:00', '15:00-18:00']

function getToday() {
  var now = new Date()
  var y = now.getFullYear()
  var m = String(now.getMonth() + 1).padStart(2, '0')
  var d = String(now.getDate()).padStart(2, '0')
  return y + '-' + m + '-' + d
}

Page({
  data: {
    loading: true,
    error: '',

    step: 1,

    products: [],
    pets: [],
    petNames: [],
    memberName: '当前会员',
    storeName: '未分配门店',

    selectedIndex: -1,
    selectedProductCode: '',
    selectedProductName: '',
    selectedProductDesc: '',
    selectedProductPrice: '',
    selectedProductTags: [],

    selectedPetIndex: 0,
    selectedPetName: '',
    reservationDate: getToday(),
    reservationDateLabel: '今天',
    today: getToday(),
    timeSlots: DEFAULT_TIME_SLOTS,
    selectedTimeSlot: '',

    submitting: false,
  },

  onLoad: function () {
    this.loadPage()
  },

  onPullDownRefresh: function () {
    this.loadPage()
  },

  loadPage: function () {
    this.setData({ loading: true, error: '', step: 1 })
    var self = this

    Promise.all([api.fetchTickets(), api.fetchContext()])
      .then(function (results) {
        self.applyPageData(results[0], results[1])
      })
      .catch(function (error) {
        self.setData({
          loading: false,
          error: error.message || '预约页加载失败，请稍后重试',
        })
      })
      .then(function () {
        wx.stopPullDownRefresh()
      })
  },

  applyPageData: function (products, context) {
    var safeProducts = products || []
    var safePets = (context && context.pets) ? context.pets : []

    this.setData({
      loading: false,
      products: safeProducts,
      pets: safePets,
      petNames: safePets.map(function (p) { return p.name }),
      memberName: (context && context.memberName) ? context.memberName : '当前会员',
      storeName: (context && context.storeName) ? context.storeName : '未分配门店',
      selectedIndex: -1,
      selectedProductCode: '',
      selectedProductName: '',
      selectedProductDesc: '',
      selectedProductPrice: '',
      selectedProductTags: [],
      selectedPetIndex: 0,
      selectedPetName: safePets[0] ? safePets[0].name : '',
      reservationDate: getToday(),
      reservationDateLabel: '今天',
      today: getToday(),
      selectedTimeSlot: '',
    })
  },

  /* ── Step 1: 选服务 ── */

  selectProduct: function (event) {
    var index = Number(event.currentTarget.dataset.index)
    var product = this.data.products[index]
    if (!product) return

    this.setData({
      selectedIndex: index,
      selectedProductCode: product.code,
      selectedProductName: product.name,
      selectedProductDesc: product.desc,
      selectedProductPrice: fmt.formatPrice(product.price),
      selectedProductTags: product.tags || [],
    })
  },

  nextStep: function () {
    if (!this.data.selectedProductCode) {
      wx.showToast({ title: '请先选择一个服务项目', icon: 'none' })
      return
    }
    this.setData({ step: 2 })
  },

  /* ── Step 2: 填信息 ── */

  prevStep: function () {
    this.setData({ step: this.data.step - 1 })
  },

  bindPetChange: function (event) {
    var idx = Number(event.detail.value)
    var pet = this.data.pets[idx]
    this.setData({
      selectedPetIndex: idx,
      selectedPetName: pet ? pet.name : '',
    })
  },

  bindDateChange: function (event) {
    var date = event.detail.value
    this.setData({
      reservationDate: date,
      reservationDateLabel: fmt.formatDateFriendly(date),
    })
  },

  selectTimeSlot: function (event) {
    var slot = event.currentTarget.dataset.slot
    this.setData({ selectedTimeSlot: slot })
  },

  goToConfirm: function () {
    if (!this.data.pets.length) {
      wx.showToast({ title: '请先添加宠物档案', icon: 'none' })
      return
    }
    if (!this.data.selectedTimeSlot) {
      wx.showToast({ title: '请选择预约时段', icon: 'none' })
      return
    }
    if (this.data.reservationDate < this.data.today) {
      wx.showToast({ title: '预约日期不能早于今天', icon: 'none' })
      return
    }
    this.setData({ step: 3 })
  },

  openPets: function () {
    wx.navigateTo({ url: '/pages/pets/index' })
  },

  /* ── Step 3: 确认提交并支付 ── */

  submitReservation: function () {
    if (this.data.submitting) return

    var selectedPet = this.data.pets[this.data.selectedPetIndex]
    if (!selectedPet) {
      wx.showToast({ title: '宠物档案异常，请返回重选', icon: 'none' })
      return
    }

    this.setData({ submitting: true })
    var self = this
    var orderNo = ''

    // Step 1: 创建预约订单（状态为 PENDING_PAY）
    api.createReservation({
      ticketCode: this.data.selectedProductCode,
      reservationDate: this.data.reservationDate,
      timeSlot: this.data.selectedTimeSlot,
      petId: selectedPet.id,
    })
      .then(function (result) {
        orderNo = (result && result.orderNo) ? result.orderNo : ''
        if (!orderNo) {
          throw new Error('订单创建异常')
        }
        // Step 2: 获取预支付参数
        return api.prepay(orderNo)
      })
      .then(function (prepayData) {
        // Step 3: 调用微信支付
        return self.callWxPayment(prepayData, orderNo)
      })
      .then(function () {
        // Step 4: 支付成功 → 确认支付（开发模式需要；正式环境由微信回调完成）
        return api.confirmPayment(orderNo)
      })
      .then(function () {
        wx.showModal({
          title: '支付成功',
          content: '订单号 ' + orderNo + '\n可前往订单页查看详情和入园凭证。',
          showCancel: false,
          confirmText: '查看订单',
          success: function () {
            wx.switchTab({ url: '/pages/orders/index' })
          },
        })
      })
      .catch(function (error) {
        var msg = error.message || ''
        if (msg.indexOf('cancel') >= 0 || msg.indexOf('取消') >= 0) {
          wx.showToast({ title: '已取消支付', icon: 'none' })
          // 取消支付后用户可重新支付，跳到订单页
          if (orderNo) {
            wx.showModal({
              title: '订单已创建',
              content: '您可以稍后在订单页继续支付。',
              showCancel: false,
              confirmText: '查看订单',
              success: function () {
                wx.switchTab({ url: '/pages/orders/index' })
              },
            })
          }
        } else {
          req.showRequestError(error, '预约或支付失败，请稍后重试')
        }
      })
      .then(function () {
        self.setData({ submitting: false })
      })
  },

  /**
   * 调用 wx.requestPayment 发起微信支付。
   * 开发模式下微信开发者工具不支持真实支付，会自动模拟成功。
   */
  callWxPayment: function (prepayData, orderNo) {
    var config = require('../../utils/config')

    // 开发环境跳过真实支付（微信开发者工具不支持）
    if (config.shouldUseMockFallback()) {
      console.log('[payment] 开发模式模拟支付成功: orderNo=' + orderNo)
      return Promise.resolve()
    }

    return new Promise(function (resolve, reject) {
      wx.requestPayment({
        timeStamp: prepayData.timeStamp,
        nonceStr: prepayData.nonceStr,
        package: prepayData.packageValue,
        signType: prepayData.signType || 'RSA',
        paySign: prepayData.paySign,
        success: function () {
          resolve()
        },
        fail: function (err) {
          reject(new Error(err.errMsg || '支付失败'))
        },
      })
    })
  },

  retry: function () {
    this.loadPage()
  },
})
