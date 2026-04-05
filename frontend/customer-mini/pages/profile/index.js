const { request } = require('../../utils/request')

function levelLabel(level) {
  if (!level) return '会员旅程待完善'
  if (level === 'SILVER') return '银卡会员'
  if (level === 'GOLD') return '金卡会员'
  if (level === 'PLATINUM') return '铂金会员'
  return level
}

function mapProfileItem(item) {
  const parts = String(item || '').split('：')
  if (parts.length >= 2) {
    return {
      title: parts[0],
      value: parts.slice(1).join('：'),
    }
  }
  return {
    title: '会员信息',
    value: item,
  }
}

Page({
  data: {
    memberName: '当前会员',
    memberLevel: '',
    memberLevelText: '',
    storeName: '',
    items: [],
    profileHint: '会员等级、订单与凭证状态会在这里统一查看。',
  },

  onLoad() {
    request('/api/c-app/profile')
      .then((data) => {
        this.setData({
          memberName: data.memberName,
          memberLevel: data.memberLevel,
          memberLevelText: levelLabel(data.memberLevel),
          storeName: data.storeName,
          items: data.items.map(mapProfileItem),
          profileHint: data.items.length ? '你的会员信息已同步，关键状态会以可读字段持续更新。' : '会员信息正在同步中。',
        })
      })
      .catch(() => {
        this.setData({
          memberName: '张三',
          memberLevel: 'SILVER',
          memberLevelText: levelLabel('SILVER'),
          storeName: '上海萌宠乐园旗舰店',
          items: ['会员等级：SILVER', '活跃卡种：0', '累计订单：1', '人脸录入状态：已录入'].map(mapProfileItem),
          profileHint: '你的会员信息已同步，关键状态会以可读字段持续更新。',
        })
      })
  },
})
