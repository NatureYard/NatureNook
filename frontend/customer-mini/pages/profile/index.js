const { request } = require('../../utils/request')

Page({
  data: {
    memberName: '当前会员',
    memberLevel: '',
    storeName: '',
    items: [],
  },

  onLoad() {
    request('/api/c-app/profile')
      .then((data) => {
        this.setData({
          memberName: data.memberName,
          memberLevel: data.memberLevel,
          storeName: data.storeName,
          items: data.items,
        })
      })
      .catch(() => {
        this.setData({
          memberName: '张三',
          memberLevel: 'SILVER',
          storeName: '上海萌宠乐园旗舰店',
          items: ['会员等级：SILVER', '活跃卡种：0', '累计订单：1', '人脸录入状态：已录入'],
        })
      })
  },
})
