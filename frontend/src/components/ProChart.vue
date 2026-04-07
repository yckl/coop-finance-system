<template>
  <div class="pro-chart-sandbox" ref="chartRef" style="width: 100%; min-height: 300px;"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import * as echarts from 'echarts';
import { useDark, useWindowSize, useDebounceFn } from '@vueuse/core';

const props = defineProps({
  option: { type: Object, required: true }
});

const chartRef = ref(null);
let eInstance = null;
const isDark = useDark(); 

const renderEngine = () => {
  if (!chartRef.value) return;
  
  if (eInstance) eInstance.dispose();
  
  eInstance = echarts.init(chartRef.value, isDark.value ? 'dark' : undefined);
  
  const advancedPayload = {
    ...props.option,
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow', shadowStyle: { color: 'rgba(0,82,204,0.1)' } } },
    dataZoom: [
       { type: 'inside' }, 
       { type: 'slider', height: 8, bottom: 5, borderColor: 'transparent', handleSize: '150%' }
    ]
  };
  
  eInstance.setOption(advancedPayload);
};

watch(isDark, renderEngine);
watch(() => props.option, () => eInstance?.setOption({
   ...props.option,
   backgroundColor: 'transparent',
   tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
   dataZoom: [{ type: 'inside' }, { type: 'slider', height: 8, bottom: 5 }]
}), { deep: true });

const { width } = useWindowSize();
watch(width, useDebounceFn(() => {
   eInstance?.resize();
}, 200));

onMounted(renderEngine);
onBeforeUnmount(() => eInstance?.dispose());
</script>
