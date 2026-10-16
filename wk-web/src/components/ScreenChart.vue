<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue'
import * as echarts from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

// 按需注册，控制打包体积
echarts.use([BarChart, LineChart, PieChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

const props = defineProps({
  option: { type: Object, required: true }
})

const el = ref(null)
let chart = null
let resizeObserver = null

onMounted(() => {
  chart = echarts.init(el.value)
  chart.setOption(props.option)
  // 容器尺寸随窗口/侧边栏折叠变化，触发 ECharts 重绘
  resizeObserver = new ResizeObserver(() => chart && chart.resize())
  resizeObserver.observe(el.value)
})

// option 由父组件 computed 每次生成新对象，引用变化即可触发，无需 deep 遍历
watch(
  () => props.option,
  (val) => chart && chart.setOption(val)
)

onUnmounted(() => {
  resizeObserver?.disconnect()
  chart && chart.dispose()
  chart = null
})
</script>

<template>
  <div ref="el" class="chart"></div>
</template>

<style scoped lang="scss">
.chart {
  flex: 1;
  min-height: 0;
  width: 100%;
}
</style>
