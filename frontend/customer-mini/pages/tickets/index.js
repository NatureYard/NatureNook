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

  /* ── Step 3: 确认提交 ── */

  submitReservation: function () {
    if (this.data.submitting) return

    var selectedPet = this.data.pets[this.data.selectedPetIndex]
    if (!selectedPet) {
      wx.showToast({ title: '宠物档案异常，请返回重选', icon: 'none' })
      return
    }

    this.setData({ submitting: true })
    var self = this

    api.createReservation({
      ticketCode: this.data.selectedProductCode,
      reservationDate: this.data.reservationDate,
      timeSlot: this.data.selectedTimeSlot,
      petId: selectedPet.id,
    })
      .then(function (result) {
        wx.showModal({
          title: '预约成功',
          content: '订单号 ' + ((result && result.orderNo) ? result.orderNo : '--') + '\n可前往订单页查看详情和入园凭证。',
          showCancel: false,
          confirmText: '查看订单',
          success: function () {
            wx.switchTab({ url: '/pages/orders/index' })
          },
        })
      })
      .catch(function (error) {
        req.showRequestError(error, '预约失败，请稍后重试')
      })
      .then(function () {
        self.setData({ submitting: false })
      })
  },

  retry: function () {
    this.loadPage()
  },
})
