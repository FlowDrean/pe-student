/**
 * WebSocket 组合函数 — 基于 uni.connectSocket
 * 管理实时消息通信，兼容 Spring STOMP 后端
 * 支持自动重连、心跳维护、STOMP 帧解析
 */
import { ref, onUnmounted } from 'vue'
import { useUserStore } from '@stores/user'
import { useMessageStore } from '@stores/message'

const WS_URL = 'ws://localhost:8080/ws'

export function useSocket() {
  // ── 状态 ──
  const connected = ref(false)
  const socketTask = ref<UniApp.SocketTask | null>(null)
  const subscriptions = new Map<string, (data: any) => void>()

  let reconnectTimer: ReturnType<typeof setTimeout> | null = null
  let heartbeatTimer: ReturnType<typeof setInterval> | null = null
  let reconnectAttempts = 0
  const MAX_RECONNECT_ATTEMPTS = 10
  let isManualClose = false

  // ── 内部函数 ──

  /**
   * 解析 STOMP 帧 — 仅处理以 \x00 结束的完整帧，不完整数据留在缓冲区
   * 返回 { frames, remaining }，remaining 为不完整数据（等待后续 WebSocket 消息拼接）
   */
  function parseStompFrames(buffer: string): {
    frames: { command: string; headers: Record<string, string>; body: string }[]
    remaining: string
  } {
    const frames: { command: string; headers: Record<string, string>; body: string }[] = []

    // 找到最后一个完整帧边界（\x00 是 STOMP 帧终止符）
    const lastNullIdx = buffer.lastIndexOf('\x00')
    if (lastNullIdx < 0) {
      // 还没有完整帧，全部保留在缓冲区
      return { frames, remaining: buffer }
    }

    // 截取已完整接收的部分，剩余部分留在缓冲区等待后续数据
    const complete = buffer.substring(0, lastNullIdx)
    const remaining = buffer.substring(lastNullIdx + 1)

    const parts = complete.split('\x00')
    for (const part of parts) {
      const trimmed = part.trim()
      if (!trimmed) continue

      const lines = trimmed.split('\n')
      const command = lines[0]
      const headers: Record<string, string> = {}
      let i = 1

      // 解析 headers
      while (i < lines.length && lines[i].trim() !== '') {
        const colonIdx = lines[i].indexOf(':')
        if (colonIdx > 0) {
          const key = lines[i].substring(0, colonIdx).trim()
          const value = lines[i].substring(colonIdx + 1).trim()
          headers[key] = value
        }
        i++
      }

      // 跳过空行分隔符
      const bodyStart = i + 1
      const body = lines.slice(bodyStart).join('\n')

      frames.push({ command, headers, body })
    }

    return { frames, remaining }
  }

  /** 判断消息目标是否为当前登录用户 */
  function isForCurrentUser(destination: string): boolean {
    if (!destination) return false
    // Spring STOMP simple broker 会将 /user/queue/message 翻译为 /queue/message-user{sessionId}
    // 点对点消息已由 broker 精确路由，/topic/ 为广播消息
    return destination.includes('/queue/') || destination.startsWith('/topic/')
  }

  /** 开始心跳定时器 */
  function startHeartbeat() {
    stopHeartbeat()
    heartbeatTimer = setInterval(() => {
      if (socketTask.value && connected.value) {
        socketTask.value.send({
          data: '\n', // STOMP 心跳换行
        })
      }
    }, 10000)
  }

  /** 停止心跳定时器 */
  function stopHeartbeat() {
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer)
      heartbeatTimer = null
    }
  }

  /** 自动重连（指数退避） */
  function scheduleReconnect() {
    if (isManualClose) return
    if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
      console.warn('[WS] 重连次数已达上限，停止重连')
      return
    }

    const delay = Math.min(1000 * Math.pow(2, reconnectAttempts), 30000)
    reconnectAttempts++

    reconnectTimer = setTimeout(() => {
      console.log(`[WS] 第 ${reconnectAttempts} 次尝试重连...`)
      connect().catch(() => {
        // 失败后 onError 会再次触发 scheduleReconnect
      })
    }, delay)
  }

  /** 取消重连 */
  function cancelReconnect() {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    reconnectAttempts = 0
  }

  /** 重连后重新订阅所有已注册目标 */
  function reSubscribeAll() {
    subscriptions.forEach((_, destination) => {
      if (socketTask.value && connected.value && destination.startsWith('/')) {
        const id = 'sub-' + destination.replace(/[\/]/g, '-').replace(/^-/, '')
        socketTask.value.send({
          data: `SUBSCRIBE\nid:${id}\ndestination:${destination}\n\n\x00`,
        })
      }
    })
  }

  // ── 对外接口 ──

  /**
   * 连接 WebSocket
   * 发送 STOMP CONNECT 帧进行握手，包含 JWT 认证信息
   */
  function connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      const userStore = useUserStore()
      const token = userStore.token

      if (!token) {
        reject(new Error('未登录，无法建立 WebSocket 连接'))
        return
      }

      // 取消之前的重连定时器，防止产生重复连接
      cancelReconnect()

      // 关闭已有连接（发送 DISCONNECT 告知服务器清理订阅，避免残留订阅导致消息重复投递）
      if (socketTask.value) {
        try {
          socketTask.value.send({
            data: `DISCONNECT\nreceipt:cleanup-${Date.now()}\n\n\x00`,
          })
        } catch {
          // 旧连接可能已断开，忽略发送异常
        }
        socketTask.value.close({})
        socketTask.value = null
      }

      const task = uni.connectSocket({
        url: WS_URL,
        header: {
          Authorization: `Bearer ${token}`,
        },
        complete: () => {},
      })

      socketTask.value = task

      let messageBuffer = ''

      task.onOpen(() => {
        connected.value = true
        cancelReconnect()

        // 发送 STOMP CONNECT 帧（包含 JWT 认证信息）
        task.send({
          data: `CONNECT\nAuthorization:Bearer ${token}\naccept-version:1.1,1.2\nheart-beat:10000,10000\n\n\x00`,
        })

        startHeartbeat()
        resolve()
      })

      task.onMessage((res: any) => {
        // 仅处理当前活跃连接的消息，防止旧连接回调干扰
        if (socketTask.value !== task) return

        const rawData: string = typeof res.data === 'string' ? res.data : ''

        // 忽略心跳响应
        if (rawData === '\n' || rawData === '\r\n') return

        messageBuffer += rawData

        // 按 STOMP 帧解析（仅处理完整帧，不完整数据留在缓冲区）
        const { frames, remaining } = parseStompFrames(messageBuffer)
        messageBuffer = remaining

        for (const frame of frames) {
          if (frame.command === 'CONNECTED') {
            connected.value = true
            // 重连后恢复订阅
            reSubscribeAll()
          } else if (frame.command === 'MESSAGE') {
            const destination = frame.headers['destination'] || ''

            // 仅处理发给当前用户的消息
            if (isForCurrentUser(destination)) {
              try {
                const body = JSON.parse(frame.body)

                // 通知所有已注册回调（由各页面自行决定如何处理消息）
                subscriptions.forEach((cb) => {
                  try {
                    cb(body)
                  } catch {
                    // 单个回调异常不影响其他回调
                  }
                })

                // 注意：不在此处直接更新 messageStore
                // 由各页面的订阅回调负责按需更新 store，避免重复写入
              } catch {
                // body 不是 JSON，忽略
              }
            }
          }
        }

        // 如果 STOMP 解析无结果，尝试直接作为 JSON 处理（兼容非 STOMP 消息）
        if (frames.length === 0) {
          try {
            const data = JSON.parse(rawData)
            subscriptions.forEach((cb) => {
              try {
                cb(data)
              } catch {
                // 忽略回调异常
              }
            })
          } catch {
            // 不是有效 JSON，忽略
          }
        }
      })

      task.onClose(() => {
        // 仅当此 socket 仍是活跃连接时才重连，防止旧连接触发重复重连
        if (socketTask.value !== task) return
        connected.value = false
        stopHeartbeat()
        scheduleReconnect()
      })

      task.onError((err: any) => {
        console.error('[WS] 连接错误', err)
        // 仅当此 socket 仍是活跃连接时才重连
        if (socketTask.value !== task) return
        connected.value = false
        stopHeartbeat()
        scheduleReconnect()
        reject(err)
      })
    })
  }

  /**
   * 注册全局消息回调（所有 MESSAGE 帧都会通知）
   * @returns 取消订阅函数
   */
  function subscribe(callback: (data: any) => void): () => void {
    const id = `cb_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`
    subscriptions.set(id, callback)

    return () => {
      subscriptions.delete(id)
    }
  }

  /**
   * 订阅指定 STOMP 目标（如 /user/queue/message）
   * 会先取消同目标的旧订阅，防止重复订阅导致消息重复
   * @returns 取消订阅函数
   */
  function subscribeTo(destination: string, callback: (data: any) => void): () => void {
    // 避免重复订阅同一目标地址导致消息重复
    if (subscriptions.has(destination)) {
      // 发送 UNSUBSCRIBE 取消旧订阅
      if (socketTask.value && connected.value) {
        const oldId = 'sub-' + destination.replace(/[\/]/g, '-').replace(/^-/, '')
        try {
          socketTask.value.send({
            data: `UNSUBSCRIBE\nid:${oldId}\n\n\x00`,
          })
        } catch {
          // 忽略
        }
      }
    }

    subscriptions.set(destination, callback)

    // 已连接时立即发送 SUBSCRIBE 帧
    if (socketTask.value && connected.value) {
      const id = 'sub-' + destination.replace(/[\/]/g, '-').replace(/^-/, '')
      socketTask.value.send({
        data: `SUBSCRIBE\nid:${id}\ndestination:${destination}\n\n\x00`,
      })
    }

    return () => {
      subscriptions.delete(destination)
      // 发送 UNSUBSCRIBE 帧
      if (socketTask.value && connected.value) {
        const id = 'sub-' + destination.replace(/[\/]/g, '-').replace(/^-/, '')
        socketTask.value.send({
          data: `UNSUBSCRIBE\nid:${id}\n\n\x00`,
        })
      }
    }
  }

  /**
   * 发送 STOMP SEND 帧到指定目标
   */
  function send(destination: string, body: any): boolean {
    if (!socketTask.value || !connected.value) {
      console.warn('[WS] 尚未连接，无法发送')
      return false
    }

    const bodyStr = typeof body === 'string' ? body : JSON.stringify(body)

    socketTask.value.send({
      data: `SEND\ndestination:${destination}\ncontent-type:application/json\n\n${bodyStr}\x00`,
    })

    return true
  }

  /**
   * 主动断开连接（不自动重连）
   */
  function disconnect() {
    isManualClose = true
    cancelReconnect()
    stopHeartbeat()
    subscriptions.clear()

    if (socketTask.value) {
      try {
        socketTask.value.send({
          data: `DISCONNECT\nreceipt:disconnect-${Date.now()}\n\n\x00`,
        })
      } catch {
        // 忽略发送断开帧的异常
      }
      socketTask.value.close({})
      socketTask.value = null
    }

    connected.value = false
  }

  // 组件卸载时自动断开
  onUnmounted(() => {
    disconnect()
  })

  return {
    connected,
    connect,
    subscribe,
    subscribeTo,
    send,
    disconnect,
  }
}
