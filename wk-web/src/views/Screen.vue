<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { fetchStats } from '@/api/items'
import { menus } from '@/menus'
import { useClock } from '@/composables/useClock'
import { formatTime } from '@/utils/format'
import ScreenChart from '@/components/ScreenChart.vue'

// 设计稿尺寸，整体等比缩放适配各种屏幕
const DESIGN_W = 1920
const DESIGN_H = 1080
const scale = ref(1)

const stats = ref([])
const loaded = ref(false)
const nowText = useClock()

// 按宽高比取较小缩放值：保证 1920x1080 舞台完整可见，居中后四边可能留白
function fit() {
  scale.value = Math.min(window.innerWidth / DESIGN_W, window.innerHeight / DESIGN_H)
}

// 聚合接口一次拉回全部大屏数据，按菜单顺序对齐展示结构
async function load() {
  try {
    const data = await fetchStats()
    stats.value = menus.map((m) => {
      const s = data.find((d) => d.category === m.category)
      return { key: m.key, title: m.title, total: s?.total ?? 0, items: s?.items ?? [] }
    })
    loaded.value = true
  } catch {
    // 轮询刷新失败时保留旧数据，等待下一轮
  }
}

const totalAll = computed(() => stats.value.reduce((sum, s) => sum + s.total, 0))

// 柱状图：各分类记录数对比
const barOption = computed(() => ({
  grid: { left: 50, right: 24, top: 36, bottom: 32 },
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    data: stats.value.map((s) => s.title),
    axisLine: { lineStyle: { color: 'rgba(64, 196, 255, 0.3)' } },
    axisTick: { show: false },
    axisLabel: { color: '#9fd8ff', fontSize: 14 }
  },
  yAxis: {
    type: 'value',
    minInterval: 1,
    splitLine: { lineStyle: { color: 'rgba(64, 196, 255, 0.12)' } },
    axisLabel: { color: '#5b7ba6' }
  },
  series: [
    {
      type: 'bar',
      data: stats.value.map((s) => s.total),
      barWidth: 36,
      itemStyle: {
        borderRadius: [4, 4, 0, 0],
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: '#4de3ff' },
            { offset: 1, color: '#1f6feb' }
          ]
        }
      },
      label: { show: true, position: 'top', color: '#4de3ff', fontSize: 16 }
    }
  ]
}))

// 环形图：各分类占比
const pieOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, icon: 'circle', textStyle: { color: '#9fd8ff' } },
  color: ['#4de3ff', '#ffd666', '#7bffb2', '#ff8fa3'],
  series: [
    {
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '44%'],
      itemStyle: { borderColor: '#06132b', borderWidth: 2 },
      label: { color: '#c9e6ff', formatter: '{b}\n{c} 条' },
      labelLine: { lineStyle: { color: 'rgba(64, 196, 255, 0.4)' } },
      data: stats.value.map((s) => ({ name: s.title, value: s.total }))
    }
  ]
}))

// 折线图：合并各分类最近记录，按日期统计条数展示新增趋势
const lineOption = computed(() => {
  const counts = {}
  stats.value.forEach((s) =>
    s.items.forEach((it) => {
      const day = String(it.updatedAt).slice(5, 10) // 取 MM-DD
      counts[day] = (counts[day] || 0) + 1
    })
  )
  const days = Object.keys(counts).sort()
  return {
    grid: { left: 50, right: 24, top: 36, bottom: 32 },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: days,
      axisLine: { lineStyle: { color: 'rgba(64, 196, 255, 0.3)' } },
      axisTick: { show: false },
      axisLabel: { color: '#9fd8ff', fontSize: 14 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: 'rgba(64, 196, 255, 0.12)' } },
      axisLabel: { color: '#5b7ba6' }
    },
    series: [
      {
        type: 'line',
        data: days.map((d) => counts[d]),
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { width: 3, color: '#7bffb2' },
        itemStyle: { color: '#7bffb2' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(123, 255, 178, 0.35)' },
              { offset: 1, color: 'rgba(123, 255, 178, 0)' }
            ]
          }
        }
      }
    ]
  }
})

// 合并各分类记录，按更新时间倒序取前 10 条
const recent = computed(() =>
  stats.value
    .flatMap((s) => s.items.map((it) => ({ ...it, categoryTitle: s.title })))
    .sort((a, b) => String(b.updatedAt).localeCompare(String(a.updatedAt)))
    .slice(0, 10)
)

// 内容超过可视区时复制两份实现无缝滚动
const needScroll = computed(() => recent.value.length > 5)
const displayList = computed(() => (needScroll.value ? [...recent.value, ...recent.value] : recent.value))

let refreshTimer = 0

onMounted(() => {
  fit()
  window.addEventListener('resize', fit)
  load()
  refreshTimer = setInterval(load, 30000) // 每 30 秒轮询刷新大屏数据
})
onUnmounted(() => {
  window.removeEventListener('resize', fit)
  clearInterval(refreshTimer)
})
</script>

<template>
  <div class="screen">
    <div class="stage" :style="{ transform: `translate(-50%, -50%) scale(${scale})` }">
      <header class="topbar">
        <h1 class="title">数据可视化大屏</h1>
        <span class="time">{{ nowText }}</span>
      </header>
      <main class="body">
        <section class="col side">
          <div v-for="s in stats" :key="s.key" class="card stat">
            <div class="stat-title">{{ s.title }}</div>
            <div class="stat-num">{{ s.total }}</div>
            <div class="stat-unit">条记录</div>
          </div>
        </section>

        <section class="col center">
          <div class="card total-card">
            <div class="total-label">记录总数</div>
            <div class="total-num">{{ totalAll }}</div>
          </div>
          <div class="card chart-card">
            <div class="card-title">分类统计</div>
            <ScreenChart v-if="loaded" :option="barOption" />
          </div>
          <div class="card chart-card">
            <div class="card-title">新增趋势</div>
            <ScreenChart v-if="loaded" :option="lineOption" />
          </div>
        </section>

        <section class="col side">
          <div class="card pie-card">
            <div class="card-title">分类占比</div>
            <ScreenChart v-if="loaded" :option="pieOption" />
          </div>
          <div class="card recent-card">
            <div class="card-title">最新动态</div>
            <div class="scroll-box">
              <ul class="scroll-list" :class="{ scrolling: needScroll }">
                <li v-for="(it, i) in displayList" :key="i">
                  <span class="tag">{{ it.categoryTitle }}</span>
                  <span class="txt">{{ it.title }}</span>
                  <span class="dt">{{ formatTime(it.updatedAt).slice(5, 16) }}</span>
                </li>
              </ul>
              <div v-if="loaded && !recent.length" class="empty">暂无数据</div>
            </div>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<style scoped lang="scss">
// 大屏配色
$blue: #4de3ff;
$text-main: #9fd8ff;
$text-dim: #5b7ba6;

// 发光大数字：统计数值共用样式
@mixin glow-num($size, $color, $glow, $glow-size: 16px) {
  font-size: $size;
  font-weight: 700;
  line-height: 1;
  color: $color;
  font-family: 'Bahnschrift', 'DIN Alternate', monospace;
  text-shadow: 0 0 $glow-size $glow;
}

.screen {
  position: fixed;
  inset: 0;
  overflow: hidden;
  background: radial-gradient(ellipse at center, #0a1f44 0%, #06132b 70%);
}

/* 设计稿舞台：居中后等比缩放 */
.stage {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 1920px;
  height: 1080px;
  display: flex;
  flex-direction: column;
  transform-origin: center;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 80px;
  padding: 0 32px;
  border-bottom: 1px solid rgba(64, 196, 255, 0.2);
}

.title {
  margin: 0;
  font-size: 34px;
  letter-spacing: 4px;
  background: linear-gradient(180deg, #fff, $blue);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.time {
  font-size: 18px;
  color: #7ecfff;
  font-variant-numeric: tabular-nums;
}

.body {
  flex: 1;
  display: flex;
  gap: 24px;
  padding: 24px;
  min-height: 0;
}

.col {
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-height: 0;
}

.side {
  width: 460px;
}

.center {
  flex: 1;
}

.card {
  background: rgba(13, 38, 76, 0.6);
  border: 1px solid rgba(64, 196, 255, 0.25);
  border-radius: 8px;
  padding: 20px 24px;
  box-shadow: inset 0 0 24px rgba(64, 196, 255, 0.06);
}

.card-title {
  font-size: 18px;
  color: $text-main;
  margin-bottom: 16px;
  padding-left: 10px;
  border-left: 3px solid $blue;
}

/* 左侧统计卡 */
.stat {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;

  .stat-title {
    font-size: 16px;
    color: $text-main;
  }

  .stat-num {
    margin-top: 8px;
    @include glow-num(52px, $blue, rgba(77, 227, 255, 0.4));
  }

  .stat-unit {
    margin-top: 8px;
    font-size: 13px;
    color: $text-dim;
  }
}

/* 中间总数与柱状图 */
.total-card {
  text-align: center;
  padding: 24px 0;

  .total-label {
    font-size: 18px;
    color: $text-main;
  }

  .total-num {
    margin-top: 8px;
    @include glow-num(64px, #ffd666, rgba(255, 214, 102, 0.35), 24px);
  }
}

/* 图表卡片：标题固定，图表占满剩余空间 */
.chart-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.pie-card {
  height: 360px;
  display: flex;
  flex-direction: column;
}

/* 右侧滚动列表 */
.recent-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.scroll-box {
  position: relative;
  flex: 1;
  overflow: hidden;
}

.scroll-list {
  margin: 0;
  padding: 0;
  list-style: none;

  /* 无缝滚动：内容复制两份，向上平移一半后循环 */
  &.scrolling {
    animation: scrollUp 18s linear infinite;

    &:hover {
      animation-play-state: paused;
    }
  }

  li {
    display: flex;
    align-items: center;
    gap: 10px;
    height: 44px;
    border-bottom: 1px dashed rgba(64, 196, 255, 0.15);
  }
}

@keyframes scrollUp {
  to {
    transform: translateY(-50%);
  }
}

.tag {
  flex-shrink: 0;
  padding: 1px 8px;
  font-size: 12px;
  color: $blue;
  border: 1px solid rgba(77, 227, 255, 0.4);
  border-radius: 3px;
}

.txt {
  flex: 1;
  font-size: 15px;
  color: #d9ecff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dt {
  flex-shrink: 0;
  font-size: 13px;
  color: $text-dim;
  font-variant-numeric: tabular-nums;
}

.empty {
  padding-top: 40px;
  text-align: center;
  color: $text-dim;
}
</style>
