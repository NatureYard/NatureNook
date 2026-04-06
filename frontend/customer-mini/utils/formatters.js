function joinText(parts) {
  return parts.filter(Boolean).join(' | ')
}

function formatPrice(value) {
  const amount = Number(value)
  if (Number.isNaN(amount)) {
    return value || '0.00'
  }
  return amount.toFixed(2)
}

function formatMemberLevel(level) {
  if (level === 'NORMAL') return '普通会员'
  if (level === 'SILVER') return '银卡会员'
  if (level === 'GOLD') return '金卡会员'
  return level || '未分级'
}

function formatCardStatus(status) {
  if (status === 'ACTIVE') return '生效中'
  if (status === 'FROZEN') return '已冻结'
  if (status === 'INVALID') return '已作废'
  if (status === 'EXPIRED') return '已过期'
  return status || '未知状态'
}

function getCardStatusClass(status) {
  if (status === 'ACTIVE') return 'active'
  if (status === 'FROZEN' || status === 'EXPIRED' || status === 'INVALID') return 'inactive'
  return 'pending'
}

function formatOrderStatus(status) {
  if (status === 'PAID') return '已支付'
  if (status === 'BOOKED') return '已预约'
  if (status === 'USED') return '已使用'
  if (status === 'CANCELLED') return '已取消'
  if (status === 'REFUNDED') return '已退款'
  return status || '处理中'
}

function getOrderStatusClass(status) {
  if (status === 'PAID') return 'paid'
  if (status === 'BOOKED') return 'pending'
  if (status === 'USED') return 'active'
  return 'inactive'
}

function formatPetMeta(item) {
  return joinText([
    item.species,
    item.breed,
    item.weight ? item.weight + 'kg' : '',
  ]) || '待补充宠物档案'
}

function formatPetGender(gender) {
  if (gender === 'MALE') return '公'
  if (gender === 'FEMALE') return '母'
  return '未知'
}

function formatPetSpecies(species) {
  if (species === 'DOG') return '犬'
  if (species === 'CAT') return '猫'
  if (species === 'BIRD') return '鸟'
  if (species === 'RABBIT') return '兔'
  return species || '其他'
}

function formatReservationInfo(item) {
  return joinText([item.reservationDate, item.timeSlot]) || '待安排服务时间'
}

function formatDaysRemaining(validTo) {
  if (!validTo) return '有效期未知'
  var now = new Date()
  var todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  var parts = validTo.split('-')
  var end = new Date(Number(parts[0]), Number(parts[1]) - 1, Number(parts[2]))
  var diff = Math.ceil((end - todayStart) / (1000 * 60 * 60 * 24))
  if (diff < 0) return '已过期'
  if (diff === 0) return '今日到期'
  if (diff <= 7) return '剩余 ' + diff + ' 天'
  if (diff <= 30) return '剩余 ' + diff + ' 天'
  return '剩余 ' + diff + ' 天'
}

function getValidityProgress(validFrom, validTo) {
  if (!validFrom || !validTo) return 100
  var now = new Date()
  var parts1 = validFrom.split('-')
  var parts2 = validTo.split('-')
  var start = new Date(Number(parts1[0]), Number(parts1[1]) - 1, Number(parts1[2]))
  var end = new Date(Number(parts2[0]), Number(parts2[1]) - 1, Number(parts2[2]))
  var total = end - start
  var elapsed = now - start
  if (total <= 0) return 100
  var pct = Math.round((elapsed / total) * 100)
  if (pct < 0) return 0
  if (pct > 100) return 100
  return pct
}

function formatDateFriendly(dateStr) {
  if (!dateStr) return ''
  var now = new Date()
  var todayStr = now.getFullYear() + '-' +
    String(now.getMonth() + 1).padStart(2, '0') + '-' +
    String(now.getDate()).padStart(2, '0')
  if (dateStr === todayStr) return '今天'

  var tomorrow = new Date(now)
  tomorrow.setDate(tomorrow.getDate() + 1)
  var tomorrowStr = tomorrow.getFullYear() + '-' +
    String(tomorrow.getMonth() + 1).padStart(2, '0') + '-' +
    String(tomorrow.getDate()).padStart(2, '0')
  if (dateStr === tomorrowStr) return '明天'

  var weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  var parts = dateStr.split('-')
  var target = new Date(Number(parts[0]), Number(parts[1]) - 1, Number(parts[2]))
  return dateStr.substring(5) + ' ' + weekdays[target.getDay()]
}

function getEntryIcon(label) {
  var map = {
    '购票预约': 'ticket',
    '卡种中心': 'card',
    '我的宠物': 'paw',
    '入园凭证': 'pass',
  }
  return map[label] || 'dot'
}

function isToday(dateStr) {
  if (!dateStr) return false
  var now = new Date()
  var todayStr = now.getFullYear() + '-' +
    String(now.getMonth() + 1).padStart(2, '0') + '-' +
    String(now.getDate()).padStart(2, '0')
  return dateStr === todayStr
}

module.exports = {
  formatCardStatus,
  formatDateFriendly,
  formatDaysRemaining,
  formatMemberLevel,
  formatOrderStatus,
  formatPetGender,
  formatPetMeta,
  formatPetSpecies,
  formatPrice,
  formatReservationInfo,
  getCardStatusClass,
  getEntryIcon,
  getOrderStatusClass,
  getValidityProgress,
  isToday,
  joinText,
}
