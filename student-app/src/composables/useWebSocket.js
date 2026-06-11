/* ============================================
   STOMP WebSocket 组合函数
   使用原生 WebSocket + STOMP 协议（无需 SockJS）
   服务端 Spring STOMP 直接支持原生 WebSocket 连接
   ============================================ */

import { ref, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import storage from '@utils/storage'

const WS_URL = import.meta.env.VITE_WS_URL || '/ws'

export function useWebSocket() {
  const connected = ref(false)
  const client = ref(null)
  const subscriptions = new Map()

  function connect() {
    return new Promise((resolve, reject) => {
      const token = storage.get('token')

      const stompClient = new Client({
        // 原生 WebSocket —— 服务端 withSockJS() 同时也接受原生 WS 升级
        brokerURL: WS_URL,
        // STOMP 连接头 —— 携带 JWT 给服务端鉴权
        connectHeaders: token
          ? { Authorization: `Bearer ${token}` }
          : {},
        // 心跳间隔
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,
        // 自动重连
        reconnectDelay: 5000,
        // 连接超时
        connectionTimeout: 10000,
        // 调试（设为 console.log 可开启）
        debug: (msg) => { /* console.log('[STOMP]', msg) */ },

        onConnect: () => {
          connected.value = true
          resolve()
        },

        onStompError: (frame) => {
          console.warn('[STOMP] 连接错误:', frame.headers?.['message'] || '未知错误')
          connected.value = false
          reject(new Error(frame.headers?.['message'] || 'STOMP connection failed'))
        },

        onWebSocketClose: () => {
          connected.value = false
        },
      })

      stompClient.activate()
      client.value = stompClient
    })
  }

  /**
   * 订阅目标地址
   * @param {string} destination — 如 '/user/queue/message'
   * @param {Function} callback  — 收到消息时回调
   * @returns {object} subscription 对象
   */
  function subscribe(destination, callback) {
    if (!client.value || !connected.value) {
      console.warn('[STOMP] 尚未连接，无法订阅:', destination)
      return null
    }

    // 避免重复订阅同一地址
    if (subscriptions.has(destination)) {
      subscriptions.get(destination).unsubscribe()
    }

    const sub = client.value.subscribe(destination, (message) => {
      try {
        const body = JSON.parse(message.body)
        callback(body)
      } catch {
        callback(message.body)
      }
    })

    subscriptions.set(destination, sub)
    return sub
  }

  /** 取消订阅 */
  function unsubscribe(destination) {
    if (subscriptions.has(destination)) {
      subscriptions.get(destination).unsubscribe()
      subscriptions.delete(destination)
    }
  }

  /** 断开连接 + 清理所有订阅 */
  function disconnect() {
    subscriptions.forEach((sub) => sub.unsubscribe())
    subscriptions.clear()
    if (client.value) {
      client.value.deactivate()
      client.value = null
    }
    connected.value = false
  }

  onUnmounted(() => {
    disconnect()
  })

  return { connected, client, connect, subscribe, unsubscribe, disconnect }
}
