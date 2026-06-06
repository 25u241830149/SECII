import { describe, expect, it } from 'vitest'

import { resolveAssetUrl } from '@/utils/asset'

describe('resolveAssetUrl', () => {
  it('returns empty string for missing values', () => {
    expect(resolveAssetUrl()).toBe('')
    expect(resolveAssetUrl(null)).toBe('')
  })

  it('keeps absolute http urls unchanged', () => {
    expect(resolveAssetUrl('https://example.com/a.png')).toBe('https://example.com/a.png')
  })

  it('prefixes relative asset paths with a slash', () => {
    expect(resolveAssetUrl('uploads/avatar.png')).toBe('/uploads/avatar.png')
    expect(resolveAssetUrl('/uploads/avatar.png')).toBe('/uploads/avatar.png')
  })
})
