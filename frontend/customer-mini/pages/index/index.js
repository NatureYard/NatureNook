const { request } = require('../../utils/request')

Page({
  data: {
    banners: [],
    quickEntries: [],
    activeCardTip: '',
  },

  onLoad() {
    request('/api/c-app/home')
      .then((data) => {
        this.setData({
          banners: data.banners,
          quickEntries: data.quickEntries,
          activeCardTip: data.activeCardTip,
        })
      })
      .catch(() => {
        this.setData({
          banners: ['周末寄养优惠', '年卡限时折扣', '洗护套餐立减'],
          quickEntries: ['购票预约', '卡种中心', '我的宠物', '入园凭证'],
          activeCardTip: '年卡会员可在有效期内多次入园',
        })
      })
  },
})
