import zhCN from './src/locales/zh-CN'
import enUS from './src/locales/en-US'

function getKeys(obj: any, prefix = ''): string[] {
  const keys: string[] = []
  for (const key in obj) {
    if (obj.hasOwnProperty(key)) {
      const fullKey = prefix ? `${prefix}.${key}` : key
      if (typeof obj[key] === 'object' && obj[key] !== null && !Array.isArray(obj[key])) {
        keys.push(...getKeys(obj[key], fullKey))
      } else {
        keys.push(fullKey)
      }
    }
  }
  return keys
}

const zhKeys = getKeys(zhCN)
const enKeys = getKeys(enUS)

const zhSet = new Set(zhKeys)
const enSet = new Set(enKeys)

const missingInEN = zhKeys.filter(k => !enSet.has(k))
const missingInZH = enKeys.filter(k => !zhSet.has(k))

console.log('\n=== 语言包一致性检查报告 ===\n')
console.log(`中文包总键数: ${zhKeys.length}`)
console.log(`英文包总键数: ${enKeys.length}\n`)

if (missingInEN.length > 0) {
  console.log(`⚠️  英文包缺失的键 (${missingInEN.length}个):`)
  missingInEN.forEach(k => console.log(`   - ${k}`))
} else {
  console.log('✓ 英文包包含所有中文键')
}

if (missingInZH.length > 0) {
  console.log(`\n⚠️  中文包缺失的键 (${missingInZH.length}个):`)
  missingInZH.forEach(k => console.log(`   - ${k}`))
} else {
  console.log('\n✓ 中文包包含所有英文键')
}

if (missingInEN.length === 0 && missingInZH.length === 0) {
  console.log('\n✅ 两个语言包完全一致！')
}
