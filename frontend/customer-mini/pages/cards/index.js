const { request } = require('../../utils/request')

function inferTone(name) {
  if ((name || '').includes('年')) return 'sunset'
  if ((name || '').includes('季')) return 'mint'
  return 'peach'
}

function inferBadge(meta) {
  if ((meta || '').includes('可购买')) return '热卖推荐'
  if ((meta || '').includes('有效')) return '权益生效中'
  return '精选卡种'
}

function mapCard(item) {
  return {
    id: item.id,
    name: item.name,
    desc: item.desc,
    meta: item.meta,
    price: item.price,
    priceText: `¥${item.price}`,
    tone: inferTone(item.name),
    badge: inferBadge(item.meta),
  }
}

Page({
  data: {
    cards: [],
    loading: true,
    summary: '挑一张更适合你和毛孩子出游节奏的卡。',
    decisionHint: '先理解权益差异，再决定是否购买长期卡。',
  },

  onLoad() {
    request('/api/c-app/cards')
      .then((data) => {
        const cards = data.map((item) =>
          mapCard({
            id: item.id,
            name: item.name,
            desc: item.desc,
            meta: `${item.status} | 到期 ${item.validTo}`,
            price: item.price,
          }),
        )
        this.setData({
          cards,
          loading: false,
          summary: cards.length ? `当前可查看 ${cards.length} 种入园卡方案` : '挑一张更适合你和毛孩子出游节奏的卡。',
          decisionHint: cards.length ? '从短期到长期方案依次浏览，减少跨卡种来回比较。' : '当前还没有可展示的卡种方案。',
        })
      })
      .catch(() => {
        const cards = [
          { id: 1, name: '月卡', desc: '30 天内多次入园', meta: '可购买', price: '399' },
          { id: 2, name: '季卡', desc: '90 天内多次入园', meta: '可购买', price: '999' },
          { id: 3, name: '年卡', desc: '365 天内多次入园', meta: '可购买', price: '1288' },
        ].map(mapCard)
        this.setData({
          cards,
          loading: false,
          summary: `当前可查看 ${cards.length} 种入园卡方案`,
          decisionHint: '从短期到长期方案依次浏览，减少跨卡种来回比较。',
        })
      })
  },
})
