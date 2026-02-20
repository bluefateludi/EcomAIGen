/**
 * User avatar utility functions
 */

// Import default avatar
import defaultAvatar from '@/assets/defaultAvatar.png'

/**
 * Get user avatar URL with fallback to default avatar
 * @param avatarUrl - User's custom avatar URL
 * @returns Avatar URL to display
 */
export function getUserAvatar(avatarUrl?: string): string {
  if (avatarUrl && avatarUrl.trim() !== '') {
    return avatarUrl
  }
  // Return default avatar
  return defaultAvatar
}

/**
 * Get user initial letter for avatar fallback
 * @param userName - User's name
 * @returns Initial letter
 */
export function getUserInitial(userName?: string): string {
  if (!userName || userName.trim() === '') {
    return 'U'
  }
  // Get first character that is a letter
  const match = userName.trim().match(/[a-zA-Z\u4e00-\u9fa5]/)
  return match ? match[0] : 'U'
}
