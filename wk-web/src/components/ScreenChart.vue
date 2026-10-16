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

onMounted(() => {
  // 容器尺寸由父布局 flex 决定，大屏用 transform 缩放无需监听 resize
  chart = echarts.init(el.value)
  chart.setOption(props.option)
})

// option 由父组件 computed 每次生成新对象，引用变化即可触发，无需 deep 遍历
watch(
  () => props.option,
  (val) => chart && chart.setOption(val)
)

onUnmounted(() => {
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
