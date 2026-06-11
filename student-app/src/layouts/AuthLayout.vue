<template>
  <div class="auth-layout">
    <!-- 背景装饰：墨水纹理 + 几何图形 -->
    <div class="auth-layout__bg">
      <!-- 大圆 -->
      <div class="auth-bg__orb auth-bg__orb--1"></div>
      <div class="auth-bg__orb auth-bg__orb--2"></div>
      <div class="auth-bg__orb auth-bg__orb--3"></div>
      <!-- 网格点阵 -->
      <div class="auth-bg__dots"></div>
      <!-- 噪点纹理 -->
      <div class="auth-bg__grain"></div>
    </div>

    <!-- 内容区 -->
    <div class="auth-layout__content">
      <router-view v-slot="{ Component }">
        <transition name="auth-page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>

    <!-- 底部品牌文字 -->
    <p class="auth-layout__brand">校园互助 · Campus Mutual Aid</p>
  </div>
</template>

<script setup>
</script>

<style scoped>
.auth-layout {
  position: relative;
  min-height: 100vh;
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--bg-ink);
  overflow: hidden;
}

/* ── 背景装饰 ── */
.auth-layout__bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

/* 光晕球体 */
.auth-bg__orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.12;
}

.auth-bg__orb--1 {
  width: 320px;
  height: 320px;
  background: var(--accent-500);
  top: -100px;
  right: -80px;
  animation: orbFloat1 8s ease-in-out infinite;
}

.auth-bg__orb--2 {
  width: 240px;
  height: 240px;
  background: var(--amber-500);
  bottom: 100px;
  left: -80px;
  animation: orbFloat2 10s ease-in-out infinite;
  opacity: 0.08;
}

.auth-bg__orb--3 {
  width: 180px;
  height: 180px;
  background: var(--coral-400);
  top: 45%;
  right: -60px;
  animation: orbFloat3 7s ease-in-out infinite;
  opacity: 0.06;
}

@keyframes orbFloat1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(-20px, 30px) scale(1.1); }
  66% { transform: translate(10px, -20px) scale(0.95); }
}

@keyframes orbFloat2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(20px, -30px) scale(1.15); }
}

@keyframes orbFloat3 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-15px, -20px) scale(1.2); }
}

/* 网格点阵 */
.auth-bg__dots {
  position: absolute;
  inset: 0;
  background-image:
    radial-gradient(circle, rgba(255,255,255,0.06) 1px, transparent 1px);
  background-size: 28px 28px;
}

/* 噪点纹理 */
.auth-bg__grain {
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.7' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.04'/%3E%3C/svg%3E");
  background-repeat: repeat;
  opacity: 0.5;
}

/* ── 内容区 ── */
.auth-layout__content {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 380px;
  padding: 0 var(--space-6);
}

/* ── 底部品牌 ── */
.auth-layout__brand {
  position: absolute;
  bottom: 40px;
  left: 0;
  right: 0;
  text-align: center;
  font-family: var(--font-mono);
  font-size: var(--text-2xs);
  color: rgba(255, 255, 255, 0.25);
  letter-spacing: 0.12em;
  text-transform: uppercase;
  pointer-events: none;
}

/* ── 页面转场 ── */
.auth-page-enter-active {
  animation: slideUp 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.auth-page-leave-active {
  animation: fadeIn 0.2s ease-in reverse;
}
</style>
