/**
 * 服务器地址集中配置
 *
 * 优先读取环境变量 VITE_SERVER_URL（可在项目根目录 .env / .env.production 中配置），
 * 未配置时回退到本机开发地址。
 *
 * 示例 .env.production：
 *   VITE_SERVER_URL=https://your-server.com
 *
 * 注意：微信小程序正式版要求 https + 已配置合法域名。
 */

export const SERVER_BASE_URL: string = (import.meta.env.VITE_SERVER_URL as string) || 'http://localhost:8080'

/** REST API 基础地址 */
export const API_BASE_URL: string = `${SERVER_BASE_URL}/api/v1`

/** WebSocket 地址（由服务器地址推导，http/https 对应 ws/wss） */
export const WS_URL: string = `${SERVER_BASE_URL.replace(/^http/, 'ws')}/ws`
