const BASE_URL = 'http://localhost:8080'

Page({
  data: {
    products: [
      { name: '单次门票', desc: '支持当日多次出入', price: '68', type: 'TICKET' },
      { name: '洗护套餐', desc: '含基础洗护与护理', price: '128', type: 'GROOMING' },
      { name: '寄养预约', desc: '按天预约，可追加喂养服务', price: '188', type: 'BOARDING' },
    ],
  },

  onLoad() {
    // products are static; reservations are fetched at booking time
  },

  makeReservation(e) {
    const product = e.currentTarget.dataset.product
    const today = new Date().toISOString().slice(0, 10)
    wx.request({
      url: `${BASE_URL}/api/c-app/reservations`,
      method: 'POST',
      header: { 'Content-Type': 'application/json' },
      data: {
        memberId: 1,
        storeId: 1,
        reservationType: product.type,
        reservationDate: today,
        timeSlot: '09:00-12:00',
        amount: Number(product.price),
      },
      success: (res) => {
        const id = res.data && res.data.data && res.data.data.id
        wx.showToast({ title: id ? `预约成功 #${id}` : '预约已提交', icon: 'success' })
      },
      fail: () => {
        wx.showToast({ title: '预约失败，请稍后重试', icon: 'none' })
      },
    })
  },
})

