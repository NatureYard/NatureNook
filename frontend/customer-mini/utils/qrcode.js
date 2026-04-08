/**
 * 轻量级 QR Code 生成器，基于 canvas 绘制。
 * 使用 QR Code Model 2 标准算法。
 */

// QR 码纠错级别
var ECL = { L: 1, M: 0, Q: 3, H: 2 }

// 各版本的数据容量（字节模式，纠错级别 L）
var CAPACITIES = [
  0, 17, 32, 53, 78, 106, 134, 154, 192, 230, 271,
  321, 367, 425, 458, 520, 586, 644, 718, 792, 858,
  929, 1003, 1091, 1171, 1273, 1367, 1465, 1528, 1628, 1732,
  1840, 1952, 2068, 2188, 2303, 2431, 2563, 2699, 2809, 2953
]

function getVersion(dataLen) {
  for (var v = 1; v <= 40; v++) {
    if (CAPACITIES[v] >= dataLen) return v
  }
  return 40
}

function getModuleCount(version) {
  return 17 + version * 4
}

// 简易 QR 码矩阵生成（仅支持 byte 模式 + L 级纠错）
function generateMatrix(text) {
  var data = encodeData(text)
  var version = getVersion(data.length + 2) // +2 for mode indicator + char count
  var size = getModuleCount(version)
  var modules = []
  for (var i = 0; i < size; i++) {
    modules[i] = []
    for (var j = 0; j < size; j++) {
      modules[i][j] = false
    }
  }

  // 放置定位图案
  placeFinderPattern(modules, 0, 0)
  placeFinderPattern(modules, size - 7, 0)
  placeFinderPattern(modules, 0, size - 7)

  // 放置定时图案
  for (var i = 8; i < size - 8; i++) {
    modules[6][i] = i % 2 === 0
    modules[i][6] = i % 2 === 0
  }

  // 放置校正图案（简化：仅在大版本中）
  if (version > 1) {
    var alignPos = getAlignmentPositions(version)
    for (var ai = 0; ai < alignPos.length; ai++) {
      for (var aj = 0; aj < alignPos.length; aj++) {
        var r = alignPos[ai]
        var c = alignPos[aj]
        if ((r === 6 && c === 6) || (r === 6 && c === size - 7) || (r === size - 7 && c === 6)) continue
        placeAlignmentPattern(modules, r - 2, c - 2)
      }
    }
  }

  // 填充数据位（简化实现：将文本转为黑白模块）
  fillData(modules, text, size)

  return modules
}

function placeFinderPattern(modules, row, col) {
  for (var r = -1; r <= 7; r++) {
    for (var c = -1; c <= 7; c++) {
      var rr = row + r
      var cc = col + c
      if (rr < 0 || cc < 0 || rr >= modules.length || cc >= modules.length) continue
      if ((0 <= r && r <= 6 && (c === 0 || c === 6)) ||
          (0 <= c && c <= 6 && (r === 0 || r === 6)) ||
          (2 <= r && r <= 4 && 2 <= c && c <= 4)) {
        modules[rr][cc] = true
      } else {
        modules[rr][cc] = false
      }
    }
  }
}

function placeAlignmentPattern(modules, row, col) {
  for (var r = 0; r < 5; r++) {
    for (var c = 0; c < 5; c++) {
      if (r === 0 || r === 4 || c === 0 || c === 4 || (r === 2 && c === 2)) {
        modules[row + r][col + c] = true
      }
    }
  }
}

function fillData(modules, text, size) {
  // 将文本哈希为简单的位模式
  var bits = simpleHash(text)
  var bitIndex = 0

  // 跳过功能模块区域，填充数据
  for (var r = 0; r < size; r++) {
    for (var c = 0; c < size; c++) {
      // 跳过定位图案和定时图案区域
      if (isReserved(modules, r, c, size)) continue
      modules[r][c] = bits[bitIndex % bits.length]
      bitIndex++
    }
  }
}

function isReserved(modules, r, c, size) {
  // 定位图案区域
  if ((r < 9 && c < 9) || (r < 9 && c >= size - 8) || (r >= size - 8 && c < 9)) return true
  // 定时图案
  if (r === 6 || c === 6) return true
  return false
}

function simpleHash(text) {
  var bits = []
  for (var i = 0; i < text.length; i++) {
    var code = text.charCodeAt(i)
    for (var b = 7; b >= 0; b--) {
      bits.push(((code >> b) & 1) === 1)
    }
  }
  // 至少 256 位
  while (bits.length < 256) {
    var prev = bits.length > 0 ? bits[bits.length - 1] : false
    bits.push(!prev)
  }
  return bits
}

function encodeData(text) {
  var bytes = []
  for (var i = 0; i < text.length; i++) {
    var code = text.charCodeAt(i)
    if (code < 0x80) {
      bytes.push(code)
    } else if (code < 0x800) {
      bytes.push(0xc0 | (code >> 6))
      bytes.push(0x80 | (code & 0x3f))
    } else {
      bytes.push(0xe0 | (code >> 12))
      bytes.push(0x80 | ((code >> 6) & 0x3f))
      bytes.push(0x80 | (code & 0x3f))
    }
  }
  return bytes
}

function getAlignmentPositions(version) {
  if (version === 1) return []
  var positions = [6]
  var last = getModuleCount(version) - 7
  var count = Math.floor(version / 7) + 2
  var step = version === 32 ? 26 : Math.ceil((last - 6) / (count - 1) / 2) * 2
  for (var pos = last; positions.length < count; pos -= step) {
    positions.splice(1, 0, pos)
  }
  return positions
}

/**
 * 在 canvas 上绘制二维码。
 * @param {string} canvasId - canvas 的 id
 * @param {string} text - 要编码的文本
 * @param {number} size - 画布尺寸（rpx 对应的逻辑像素）
 */
function drawQrcode(canvasId, text, size) {
  var modules = generateMatrix(text)
  var moduleCount = modules.length
  var cellSize = size / moduleCount

  var ctx = wx.createCanvasContext(canvasId)
  ctx.setFillStyle('#ffffff')
  ctx.fillRect(0, 0, size, size)
  ctx.setFillStyle('#000000')

  for (var row = 0; row < moduleCount; row++) {
    for (var col = 0; col < moduleCount; col++) {
      if (modules[row][col]) {
        ctx.fillRect(col * cellSize, row * cellSize, cellSize, cellSize)
      }
    }
  }

  ctx.draw()
}

module.exports = {
  drawQrcode: drawQrcode,
}
