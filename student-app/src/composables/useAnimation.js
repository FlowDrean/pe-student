/* ============================================
   GSAP 动画封装 — Vue 3 Composables
   使用 gsap.context() 管理生命周期
   ============================================ */

import gsap from 'gsap'
import { onUnmounted } from 'vue'

/**
 * 创建 GSAP 上下文并绑定到组件生命周期
 * @returns {{ ctx: gsap.Context, gsap: typeof gsap }}
 */
export function useGsap() {
  const ctx = gsap.context(() => {})

  onUnmounted(() => {
    ctx.revert()
  })

  return {
    ctx,
    gsap,
    add(fn) {
      ctx.add(fn)
    },
  }
}

/**
 * 页面进入动画 — 从下往上淡入
 */
export function pageEnter(el) {
  return gsap.fromTo(
    el,
    { opacity: 0, y: 24 },
    {
      opacity: 1,
      y: 0,
      duration: 0.4,
      ease: 'power2.out',
      clearProps: 'transform',
    }
  )
}

/**
 * 页面离开动画 — 向上淡出
 */
export function pageLeave(el, done) {
  gsap.to(el, {
    opacity: 0,
    y: -16,
    duration: 0.25,
    ease: 'power2.in',
    onComplete: done,
  })
}

/**
 * 列表交错入场动画
 */
export function listStagger(elements, options = {}) {
  const {
    fromY = 30,
    duration = 0.5,
    stagger = 0.06,
    ease = 'power3.out',
    delay = 0,
    from = 'start',
  } = options

  return gsap.fromTo(
    elements,
    { opacity: 0, y: fromY },
    {
      opacity: 1,
      y: 0,
      duration,
      stagger: { each: stagger, from },
      ease,
      delay,
      clearProps: 'transform',
    }
  )
}

/**
 * 淡入缩放动画
 */
export function scaleIn(el, options = {}) {
  const { duration = 0.35, ease = 'back.out(1.7)', delay = 0 } = options

  return gsap.fromTo(
    el,
    { opacity: 0, scale: 0.85 },
    {
      opacity: 1,
      scale: 1,
      duration,
      ease,
      delay,
      clearProps: 'transform',
    }
  )
}

/**
 * 淡入动画
 */
export function fadeIn(el, options = {}) {
  const { duration = 0.35, ease = 'power2.out', delay = 0, y = 0 } = options

  const vars = {
    opacity: 1,
    duration,
    ease,
    delay,
  }
  if (y) vars.y = 0

  return gsap.fromTo(el, { opacity: 0, y }, vars)
}

/**
 * 点赞按钮动画 — 弹性缩放 + 粒子效果
 */
export function likeAnimation(el) {
  const tl = gsap.timeline()

  // 缩放弹跳
  tl.to(el, {
    scale: 1.35,
    duration: 0.12,
    ease: 'power2.out',
  })
    .to(el, {
      scale: 1,
      duration: 0.25,
      ease: 'elastic.out(1, 0.4)',
    })

  // 生成粒子
  const rect = el.getBoundingClientRect()
  const cx = rect.left + rect.width / 2
  const cy = rect.top + rect.height / 2

  for (let i = 0; i < 8; i++) {
    const particle = document.createElement('div')
    particle.style.cssText = `
      position: fixed;
      left: ${cx}px;
      top: ${cy}px;
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: #f05a28;
      pointer-events: none;
      z-index: 9999;
    `
    document.body.appendChild(particle)

    gsap.to(particle, {
      x: gsap.utils.random(-40, 40),
      y: gsap.utils.random(-50, -20),
      opacity: 0,
      scale: 0,
      duration: 0.6,
      ease: 'power2.out',
      onComplete: () => particle.remove(),
    })
  }

  return tl
}

/**
 * 收藏动画 — 心形弹跳
 */
export function favoriteAnimation(el) {
  const tl = gsap.timeline()

  tl.to(el, {
    scale: 0.6,
    duration: 0.1,
    ease: 'power2.in',
  })
    .to(el, {
      scale: 1.2,
      duration: 0.2,
      ease: 'back.out(2)',
    })
    .to(el, {
      scale: 1,
      duration: 0.15,
      ease: 'power2.out',
    })

  return tl
}

/**
 * 骨架屏闪烁动画
 * @param {Element} el — 骨架屏容器
 * @returns {gsap.core.Tween}
 */
export function shimmer(el) {
  return gsap.fromTo(
    el,
    { backgroundPosition: '-200% 0' },
    {
      backgroundPosition: '200% 0',
      duration: 1.5,
      repeat: -1,
      ease: 'linear',
    }
  )
}

/**
 * 下拉刷新旋转动画
 * @param {Element} el — 旋转图标
 * @param {number} progress — 0-1 进度
 */
export function pullRefresh(el, progress) {
  gsap.to(el, {
    rotation: progress * 360,
    duration: 0.05,
    ease: 'none',
    overwrite: 'auto',
  })
}

/**
 * 消息弹入动画
 */
export function messagePopIn(el, options = {}) {
  const { duration = 0.3, ease = 'back.out(1.7)' } = options

  return gsap.fromTo(
    el,
    { opacity: 0, scale: 0.8, y: 10 },
    {
      opacity: 1,
      scale: 1,
      y: 0,
      duration,
      ease,
      clearProps: 'transform',
    }
  )
}

/**
 * 数字滚动动画
 */
export function countUp(el, options = {}) {
  const {
    endVal = 0,
    duration = 0.8,
    ease = 'power2.out',
    prefix = '',
    suffix = '',
  } = options

  const obj = { val: 0 }
  return gsap.to(obj, {
    val: endVal,
    duration,
    ease,
    onUpdate: () => {
      el.textContent = `${prefix}${Math.round(obj.val)}${suffix}`
    },
  })
}

/**
 * 滤镜面板滑入
 */
export function panelSlideIn(el, options = {}) {
  const { duration = 0.35, ease = 'power3.out' } = options

  return gsap.fromTo(
    el,
    { y: '100%' },
    {
      y: 0,
      duration,
      ease,
      clearProps: 'transform',
    }
  )
}

/**
 * 浮动动画（持续）
 */
export function float(el, options = {}) {
  const { distance = 6, duration = 2, ease = 'sine.inOut' } = options

  return gsap.to(el, {
    y: distance,
    duration,
    ease,
    yoyo: true,
    repeat: -1,
  })
}

/**
 * 按压反馈
 */
export function pressFeedback(el) {
  gsap.to(el, {
    scale: 0.95,
    duration: 0.08,
    ease: 'power2.in',
    onComplete: () => {
      gsap.to(el, {
        scale: 1,
        duration: 0.2,
        ease: 'elastic.out(1, 0.4)',
      })
    },
  })
}
