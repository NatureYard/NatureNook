const { request } = require('../../utils/request')

const DEFAULT_TIME_SLOTS = ['09:00-12:00', '13:00-15:00', '15:00-18:00']

const productToneMap = {
  TICKET: { badge: '入园热门', toneClass: 'tone-sun', accent: '当日多次出入' },
  GROOMING: { badge: '颜值焕新', toneClass: 'tone-rose', accent: '美容护理更省心' },
  BOARDING: { badge: '安心托管', toneClass: 'tone-mint', accent: '按天预约，灵活到店' },
}

function getToday() {
  const now = new Date()
  const year = now.getFullYear()
  const month = `${now.getMonth() + 1}`.padStart(2, '0')
  const day = `${now.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

function mapProduct(product) {
  const meta = productToneMap[product.type] || { badge: '精选服务', toneClass: 'tone-sun', accent: '立即安排今日行程' }
  return {
    ...product,
    badge: meta.badge,
    toneClass: meta.toneClass,
    accent: meta.accent,
  }
}

function buildProgressNote(hasPets, selectedProductName) {
  if (!selectedProductName) return '先选择服务，再补充宠物、日期与时段。'
  if (!hasPets) return '当前还没有可预约的宠物档案，请先完善宠物信息。'
  return `已选择 ${selectedProductName}，继续确认宠物、日期和时段即可提交。`
}

Page({
  data: {
    products: [],
    pets: [],
    petNames: [],
    memberName: '当前会员',
    storeName: '未分配门店',
    selectedProductCode: '',
    selectedProductName: '',
    selectedProductDesc: '',
    selectedProductPrice: '',
    selectedProductBadge: '',
    selectedProductAccent: '',
    selectedProductTone: '',
    selectedPetIndex: 0,
    selectedPetName: '',
    reservationDate: getToday(),
    today: getToday(),
    timeSlots: DEFAULT_TIME_SLOTS,
    timeSlotIndex: 0,
    selectedTimeSlot: DEFAULT_TIME_SLOTS[0],
    progressNote: '先选择服务，再补充宠物、日期与时段。',
    loading: true,
    submitting: false,
  },

  onLoad() {
    this.loadPage()
  },

  loadPage() {
    Promise.all([request('/api/c-app/tickets'), request('/api/c-app/context')])
      .then(([products, context]) => {
        this.applyPageData(products, context)
      })
      .catch(() => {
        this.applyPageData(
          [
            { code: 'DAY_TICKET', name: '单次门票', desc: '支持当日多次出入', price: '68.00', type: 'TICKET' },
            { code: 'GROOMING_PACKAGE', name: '洗护套餐', desc: '含基础洗护与护理', price: '128.00', type: 'GROOMING' },
            { code: 'BOARDING_DAY', name: '寄养预约', desc: '按天预约，可追加喂养服务', price: '188.00', type: 'BOARDING' },
          ],
          {
            memberName: '张三',
            storeName: '上海萌宠乐园旗舰店',
            pets: [
              { id: 1, name: '奶球' },
              { id: 3, name: '可乐' },
            ],
          },
        )
      })
  },

  applyPageData(products, context) {
    const safeProducts = (products || []).map(mapProduct)
    const safePets = context?.pets || []
    const firstProduct = safeProducts[0] || {}
    this.setData({
      products: safeProducts,
      pets: safePets,
      petNames: safePets.map((item) => item.name),
      memberName: context?.memberName || '当前会员',
      storeName: context?.storeName || '未分配门店',
      selectedProductCode: firstProduct.code || '',
      selectedProductName: firstProduct.name || '',
      selectedProductDesc: firstProduct.desc || '',
      selectedProductPrice: firstProduct.price || '',
      selectedProductBadge: firstProduct.badge || '',
      selectedProductAccent: firstProduct.accent || '',
      selectedProductTone: firstProduct.toneClass || '',
      selectedPetIndex: 0,
      selectedPetName: safePets[0]?.name || '暂无宠物档案',
      reservationDate: getToday(),
      today: getToday(),
      timeSlots: DEFAULT_TIME_SLOTS,
      timeSlotIndex: 0,
      selectedTimeSlot: DEFAULT_TIME_SLOTS[0],
      progressNote: buildProgressNote(safePets.length > 0, firstProduct.name || ''),
      loading: false,
    })
  },

  selectProduct(event) {
    const index = Number(event.currentTarget.dataset.index)
    const product = this.data.products[index]
    if (!product) return
    this.setData({
      selectedProductCode: product.code,
      selectedProductName: product.name,
      selectedProductDesc: product.desc,
      selectedProductPrice: product.price,
      selectedProductBadge: product.badge,
      selectedProductAccent: product.accent,
      selectedProductTone: product.toneClass,
      progressNote: buildProgressNote(this.data.pets.length > 0, product.name),
    })
  },

  bindPetChange(event) {
    const selectedPetIndex = Number(event.detail.value)
    this.setData({
      selectedPetIndex,
      selectedPetName: this.data.pets[selectedPetIndex]?.name || '暂无宠物档案',
    })
  },

  bindDateChange(event) {
    this.setData({
      reservationDate: event.detail.value,
    })
  },

  bindTimeSlotChange(event) {
    const timeSlotIndex = Number(event.detail.value)
    this.setData({
      timeSlotIndex,
      selectedTimeSlot: this.data.timeSlots[timeSlotIndex],
    })
  },

  submitReservation() {
    if (this.data.submitting) return

    const selectedPet = this.data.pets[this.data.selectedPetIndex]
    if (!this.data.selectedProductCode) {
      wx.showToast({ title: '请选择预约项目', icon: 'none' })
      return
    }
    if (!selectedPet) {
      wx.showToast({ title: '请先完善宠物档案', icon: 'none' })
      return
    }

    this.setData({ submitting: true })
    request('/api/c-app/reservations', {
      method: 'POST',
      data: {
        ticketCode: this.data.selectedProductCode,
        reservationDate: this.data.reservationDate,
        timeSlot: this.data.selectedTimeSlot,
        petId: selectedPet.id,
      },
    })
      .then(() => {
        wx.showToast({ title: '预约成功', icon: 'success' })
        setTimeout(() => {
          wx.switchTab({
            url: '/pages/orders/index',
          })
        }, 300)
      })
      .catch((error) => {
        wx.showToast({
          title: error.message || '预约失败',
          icon: 'none',
        })
      })
      .finally(() => {
        this.setData({ submitting: false })
      })
  },
})
