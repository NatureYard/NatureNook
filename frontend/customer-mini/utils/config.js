const STORAGE_KEY = 'mcly_api_base'

const ENV_API_BASES = {
  develop: 'http://localhost:8080',
  trial: 'http://localhost:8080',
  release: 'http://localhost:8080',
}

function getEnvVersion() {
  try {
    return wx.getAccountInfoSync().miniProgram.envVersion || 'develop'
  } catch (error) {
    return 'develop'
  }
}

function getStoredApiBase() {
  try {
    return wx.getStorageSync(STORAGE_KEY) || ''
  } catch (error) {
    return ''
  }
}

function resolveApiBase() {
  const storedApiBase = getStoredApiBase()
  if (storedApiBase) {
    return storedApiBase
  }

  const envVersion = getEnvVersion()
  return ENV_API_BASES[envVersion] || ENV_API_BASES.develop
}

function shouldUseMockFallback() {
  return getEnvVersion() === 'develop'
}

module.exports = {
  STORAGE_KEY,
  getEnvVersion,
  resolveApiBase,
  shouldUseMockFallback,
}
