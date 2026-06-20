<template>
  <div class="dashboard">
    <h2 class="page-title">仪表盘</h2>

    <!-- 统计卡片 -->
    <div class="stat-grid">
      <StatCard
        label="用户总数"
        :value="stats.totalUsers"
        icon="User"
        icon-bg="#e8f8ef"
        icon-color="#07C160"
        :trend="computeTrend(stats.todayNewUsers, stats.yesterdayNewUsers)"
      />
      <StatCard
        label="帖子总数"
        :value="stats.totalPosts"
        icon="Document"
        icon-bg="#fff5f0"
        icon-color="#f97316"
        :trend="computeTrend(stats.todayNewPosts, stats.yesterdayNewPosts)"
      />
      <StatCard
        label="商品总数"
        :value="stats.totalTrades"
        icon="ShoppingBag"
        icon-bg="#e8f8ef"
        icon-color="#059669"
        :trend="computeTrend(stats.todayNewTrades, stats.yesterdayNewTrades)"
      />
      <StatCard
        label="评论总数"
        :value="stats.totalComments"
        icon="ChatLineSquare"
        icon-bg="#fffbeb"
        icon-color="#f59e0b"
      />
    </div>

    <!-- 图表区域 -->
    <div class="chart-grid">
      <div class="admin-card chart-box">
        <h3 class="chart-title">近7日新增趋势</h3>
        <v-chart :option="trendOption" style="height: 300px" autoresize />
      </div>
      <div class="admin-card chart-box">
        <h3 class="chart-title">内容概览</h3>
        <div class="overview-grid">
          <div class="overview-item">
            <span class="ov-label">活跃用户</span>
            <span class="ov-value">{{ stats.activeUsers }}</span>
          </div>
          <div class="overview-item">
            <span class="ov-label">禁用用户</span>
            <span class="ov-value danger">{{ stats.disabledUsers }}</span>
          </div>
          <div class="overview-item">
            <span class="ov-label">正常帖子</span>
            <span class="ov-value">{{ stats.normalPosts }}</span>
          </div>
          <div class="overview-item">
            <span class="ov-label">在售商品</span>
            <span class="ov-value">{{ stats.sellingTrades }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 最新用户 -->
    <div class="admin-card" style="margin-top: 24px; padding: 24px;">
      <h3 class="chart-title">快速入口</h3>
      <div class="quick-links">
        <el-button type="primary" @click="$router.push('/users')">
          <el-icon><User /></el-icon> 用户管理
        </el-button>
        <el-button type="success" @click="$router.push('/posts')">
          <el-icon><Document /></el-icon> 帖子管理
        </el-button>
        <el-button type="warning" @click="$router.push('/trades')">
          <el-icon><ShoppingBag /></el-icon> 商品管理
        </el-button>
        <el-button type="danger" @click="$router.push('/comments')">
          <el-icon><ChatLineSquare /></el-icon> 评论管理
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { dashboardApi } from '@/api/dashboard'
import StatCard from '@/components/StatCard.vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent])

const stats = ref({})
const trends = ref({ dates: [], userTrend: [], postTrend: [], tradeTrend: [] })

const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: {
    data: ['新增用户', '新增帖子', '新增商品'],
    bottom: 0,
    textStyle: { color: '#6e7080', fontSize: 12 },
  },
  grid: { left: '3%', right: '4%', bottom: '12%', top: '8%', containLabel: true },
  xAxis: {
    type: 'category',
    data: trends.value.dates,
    axisLine: { lineStyle: { color: '#e4e4e8' } },
    axisLabel: { color: '#9496a4', fontSize: 11 },
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: '#f4f4f6' } },
    axisLabel: { color: '#9496a4', fontSize: 11 },
  },
  series: [
    {
      name: '新增用户',
      type: 'line',
      data: trends.value.userTrend,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      itemStyle: { color: '#07C160' },
      lineStyle: { width: 2 },
    },
    {
      name: '新增帖子',
      type: 'line',
      data: trends.value.postTrend,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      itemStyle: { color: '#f97316' },
      lineStyle: { width: 2 },
    },
    {
      name: '新增商品',
      type: 'line',
      data: trends.value.tradeTrend,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      itemStyle: { color: '#f59e0b' },
      lineStyle: { width: 2 },
    },
  ],
}))

function computeTrend(today, yesterday) {
  if (!yesterday || yesterday === 0) return today > 0 ? 100 : 0
  return Math.round(((today - yesterday) / yesterday) * 100)
}

onMounted(async () => {
  const [statsRes, trendsRes] = await Promise.all([
    dashboardApi.getStats(),
    dashboardApi.getTrends(7),
  ])
  stats.value = statsRes.data
  trends.value = trendsRes.data
})
</script>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}
@media (max-width: 1200px) {
  .stat-grid { grid-template-columns: repeat(2, 1fr); }
}

.chart-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}
@media (max-width: 1200px) {
  .chart-grid { grid-template-columns: 1fr; }
}

.chart-box {
  padding: 24px;
}
.chart-title {
  font-family: var(--font-display);
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20px;
}

.overview-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
.overview-item {
  background: var(--bg-page);
  border-radius: var(--radius-md);
  padding: 20px;
  text-align: center;
}
.ov-label {
  display: block;
  font-size: 0.8125rem;
  color: var(--text-muted);
  margin-bottom: 8px;
}
.ov-value {
  font-family: var(--font-display);
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
}
.ov-value.danger {
  color: var(--error);
}

.quick-links {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.quick-links .el-button {
  font-weight: 500;
  box-shadow: var(--shadow-button);
}
</style>
