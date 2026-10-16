<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { fetchStats } from '@/api/items'
import { menus } from '@/menus'
import { formatTime } from '@/utils/format'
import ScreenChart from '@/components/ScreenChart.vue'

const stats = ref([])
const loaded = ref(false)

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

// 图表共用坐标轴样式：浅灰网格 + 灰字，贴合 antd 浅色系
const axisCommon = {
  axisLine: { lineStyle: { color: '#d9d9d9' } },
  axisTick: { show: false },
  axisLabel: { color: '#8c8c8c', fontSize: 13 }
}

// 柱状图：各分类记录数对比
const barOption = computed(() => ({
  grid: { left: 50, right: 24, top: 36, bottom: 32 },
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: stats.value.map((s) => s.title), ...axisCommon },
  yAxis: {
    type: 'value',
    minInterval: 1,
    splitLine: { lineStyle: { color: '#f0f0f0' } },
    axisLabel: { color: '#8c8c8c' }
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
            { offset: 0, color: '#69b1ff' },
            { offset: 1, color: '#1677ff' }
          ]
        }
      },
      label: { show: true, position: 'top', color: '#1677ff', fontSize: 14 }
    }
  ]
}))

// 环形图：各分类占比
const pieOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, icon: 'circle', textStyle: { color: '#595959' } },
  color: ['#1677ff', '#faad14', '#52c41a', '#eb2f96'],
  series: [
    {
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '44%'],
      itemStyle: { borderColor: '#fff', borderWidth: 2 },
      label: { color: '#595959', formatter: '{b}\n{c} 条' },
      labelLine: { lineStyle: { color: '#d9d9d9' } },
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
    xAxis: { type: 'category', data: days, ...axisCommon },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#8c8c8c' }
    },
    series: [
      {
        type: 'line',
        data: days.map((d) => counts[d]),
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { width: 3, color: '#52c41a' },
        itemStyle: { color: '#52c41a' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(82, 196, 26, 0.2)' },
              { offset: 1, color: 'rgba(82, 196, 26, 0)' }
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
  load()
  refreshTimer = setInterval(load, 30000) // 每 30 秒轮询刷新大屏数据
})
onUnmounted(() => {
  clearInterval(refreshTimer)
})
</script>

<template>
  <div class="screen">
    <!-- 顶部统计卡：4 分类 + 总数 -->
    <div class="stat-row">
      <div v-for="s in stats" :key="s.key" class="card stat">
        <div class="stat-title">{{ s.title }}</div>
        <div class="stat-num">{{ s.total }}</div>
        <div class="stat-unit">条记录</div>
      </div>
      <div class="card stat">
        <div class="stat-title">记录总数</div>
        <div class="stat-num total">{{ totalAll }}</div>
        <div class="stat-unit">条记录</div>
      </div>
    </div>

    <!-- 中部：柱状图 + 环形图 -->
    <div class="row">
      <div class="card chart-card grow">
        <div class="card-title">分类统计</div>
        <ScreenChart v-if="loaded" :option="barOption" />
      </div>
      <div class="card chart-card">
        <div class="card-title">分类占比</div>
        <ScreenChart v-if="loaded" :option="pieOption" />
      </div>
    </div>

    <!-- 底部：趋势图 + 最新动态 -->
    <div class="row">
      <div class="card chart-card grow">
        <div class="card-title">新增趋势</div>
        <ScreenChart v-if="loaded" :option="lineOption" />
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
    </div>
  </div>
</template>

<style scoped lang="scss">
// 与列表页一致的浅色后台风：灰底白卡、antd 主色
.screen {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: calc(100vh - 136px); // 100vh - 头部64 - 页签栏40 - 内容区上下padding 32
  min-height: 0;

  @media (max-width: 768px) {
    height: auto; // 小屏允许纵向滚动
    min-height: calc(100vh - 112px); // 100vh - 头部56 - 页签栏40 - 内容区上下padding 16
  }
}

.row {
  flex: 1;
  display: flex;
  gap: 16px;
  min-height: 0;

  @media (max-width: 768px) {
    flex-direction: column;
  }
}

.card {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 16px 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
  padding-left: 10px;
  border-left: 3px solid #1677ff;
}

/* 统计卡：自适应均分一行 */
.stat-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 16px;
}

.stat {
  .stat-title {
    font-size: 14px;
    color: #8c8c8c;
  }

  .stat-num {
    margin-top: 8px;
    font-size: 36px;
    font-weight: 700;
    line-height: 1;
    color: #1677ff;
    font-family: 'Bahnschrift', 'DIN Alternate', monospace;
    font-variant-numeric: tabular-nums;

    &.total {
      color: #fa8c16;
    }
  }

  .stat-unit {
    margin-top: 8px;
    font-size: 12px;
    color: #bfbfbf;
  }
}

/* 图表卡片：标题固定，图表占满剩余空间；grow 让柱/折线图比环形图宽 */
.chart-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;

  &.grow {
    flex: 2;
  }

  @media (max-width: 768px) {
    height: 320px;
    flex: none;
  }
}

/* 最新动态滚动列表 */
.recent-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;

  @media (max-width: 768px) {
    height: 320px;
    flex: none;
  }
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
    border-bottom: 1px dashed #f0f0f0;
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
  color: #1677ff;
  background: #e6f4ff;
  border: 1px solid #91caff;
  border-radius: 3px;
}

.txt {
  flex: 1;
  font-size: 14px;
  color: #1f2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dt {
  flex-shrink: 0;
  font-size: 13px;
  color: #8c8c8c;
  font-variant-numeric: tabular-nums;
}

.empty {
  padding-top: 40px;
  text-align: center;
  color: #bfbfbf;
}
</style>
