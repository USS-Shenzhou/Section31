import * as Util from './Util.js';
import {BaseMetric} from './BaseMetric.js';
import {calculateFontSize} from "./Util.js";

export class StackedMetric extends BaseMetric {
    constructor() {
        super();
        this.historyData = {};
        this.timeLabels = [];
        this.knownKeys = new Set();
        this.currentMouseX = 0;
        this.currentMouseY = 0;
        this.hoveredSeriesName = null;
    }

    renderCharts() {
        const donutContainer = this.shadowRoot.querySelector('.donut-container');
        const lineContainer = this.shadowRoot.querySelector('.line-container');

        this.donutChart = echarts.init(donutContainer, 'dark', {renderer: 'svg'});
        this.donutOption = {
            backgroundColor: 'transparent',
            series: [{
                type: 'pie',
                startAngle: 180,
                radius: ['50%', '70%'],
                data: [
                    {value: 0, name: 'Used', itemStyle: {color: '#3c91ff'}},
                    {value: 0, name: 'Remaining', itemStyle: {color: 'transparent'}}
                ],
                label: {show: true, position: 'center', fontSize: '1.75rem', fontWeight: 'bold'},
                emphasis: {scale: false}
            }]
        };
        this.donutChart.setOption(this.donutOption);

        this.lineChart = echarts.init(lineContainer, 'dark', {renderer: 'svg'});
        this.lineOption = {
            backgroundColor: 'transparent',
            tooltip: {
                trigger: 'axis',
                formatter: (params) => {
                    if (!params || params.length === 0) return '';
                    let validParams = params.filter(item => {
                        return typeof item.value === 'number' && item.value !== 0;
                    });
                    if (validParams.length === 0) return '';
                    if (this.currentMouseY < (window.innerHeight / 2)) {
                        validParams.sort((a, b) => b.value - a.value);
                    } else {
                        validParams.sort((a, b) => a.value - b.value);
                    }
                    let res = `<div>${params[0].axisValue}</div>`;
                    validParams.forEach(item => {
                        res += `
                                    <div style="display: flex; justify-content: space-between;">
                                        <span>${item.marker} ${item.seriesName}&nbsp;</span>
                                        <b style="margin-left: auto;">${Util.formatValue(item.value, this.format)}</b>
                                    </div>
                                `;
                    });
                    return res;
                },
                position: (point, params, dom, rect, size) => {
                    const [mouseX, mouseY] = point;
                    const [tooltipW, tooltipH] = size.contentSize;
                    const [containerW, containerH] = size.viewSize;
                    let x, y;
                    if (this.currentMouseX < (window.innerWidth / 2)) {
                        x = point[0];
                    } else {
                        x = point[0] - tooltipW
                    }
                    if (this.currentMouseY < (window.innerHeight / 2)) {
                        y = point[1] + 15;
                    } else {
                        y = point[1] - size.contentSize[1] - 15;
                    }
                    return [x, y];
                },
                transitionDuration: 0.15,
                hideDelay: 0,
            },
            grid: {left: '0', right: '0', bottom: '0', top: '0', containLabel: false},
            xAxis: {
                type: 'category',
                boundaryGap: false,
                data: [],
                axisLabel: {show: false},
                axisTick: {show: false}
            },
            yAxis: {
                type: 'value',
                axisLabel: {formatter: (value) => Util.formatValue(value, this.format)}
            },
            series: []
        };
        this.lineChart.setOption(this.lineOption);
    }

    bindEvents() {
        window.addEventListener('resize', () => {
            this.lineChart && this.lineChart.resize();
        });

        window.addEventListener('metricsUpdate', (event) => {
            const data = event.detail;
            if (data[this.id]) {
                this.updateData(data[this.id]);
            }
        });
        this.lineChart.getZr().on('mousemove', (params) => {
            this.currentMouseX = params.event.clientX;
            this.currentMouseY = params.event.clientY;
        });
    }

    updateData(newMap) {
        const currentTime = new Date().toLocaleTimeString();
        const lineContainerWidth = this.shadowRoot.querySelector('.line-container').clientWidth;
        const maxPoints = Math.floor(lineContainerWidth / 3);

        let totalValue = 0;
        for (let key in newMap) {
            totalValue += newMap[key];
            if (!this.knownKeys.has(key)) {
                this.knownKeys.add(key);
                this.historyData[key] = new Array(this.timeLabels.length).fill(0);
            }
        }

        const usedColor = this.getThresholdColor(totalValue, this.preferredMaxValue);
        const formattedTotal = Util.formatValue(totalValue, this.format, true);
        this.donutOption.series[0].data[0].value = totalValue;
        this.donutOption.series[0].data[0].itemStyle.color = (this.preferredMaxValue === 0) ? 'transparent' : usedColor;
        this.donutOption.series[0].label.formatter = () => formattedTotal;
        this.donutOption.series[0].label.textBorderColor = '#000000a0';
        this.donutOption.series[0].label.textBorderWidth = 3;
        this.donutOption.series[0].label.color = usedColor;
        this.donutOption.series[0].label.fontSize = calculateFontSize(formattedTotal, this.preferredMaxValue) + 'rem';
        this.donutOption.series[0].data[1].value = Math.max(0, (2 * this.preferredMaxValue) - totalValue);
        this.donutChart.setOption(this.donutOption);

        this.timeLabels.push(currentTime);
        if (this.timeLabels.length > maxPoints) this.timeLabels.shift();

        this.knownKeys.forEach(key => {
            const val = newMap[key] || null;
            this.historyData[key].push(val);
            if (this.historyData[key].length > maxPoints) {
                this.historyData[key].shift();
            }
        });

        const paddingCount = maxPoints - this.timeLabels.length;
        const paddedLabels = Array(paddingCount).fill("").concat(this.timeLabels);

        const newSeries = [];
        this.knownKeys.forEach(key => {
            const paddedData = Array(paddingCount).fill(null).concat(this.historyData[key]);
            newSeries.push({
                name: key,
                type: 'line',
                stack: 'total',
                areaStyle: {},
                emphasis: {focus: 'series'},
                symbol: 'none',
                data: paddedData,
                connectNulls: false,
                itemStyle: {color: Util.getColorFromKey(key)},
            });
        });

        this.lineOption.xAxis.data = paddedLabels;
        this.lineOption.series = newSeries;
        this.lineChart.setOption(this.lineOption);
    }
}

customElements.define('stacked-metric', StackedMetric);