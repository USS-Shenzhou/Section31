import * as Util from './Util.js';
import {calculateFontSize} from './Util.js';
import {BaseMetric} from './BaseMetric.js';

export class SingleMetric extends BaseMetric {
    constructor() {
        super();
        this.historyData = [];
        this.timeLabels = [];
    }

    renderCharts() {
        const donutContainer = this.shadowRoot.querySelector('.donut-container');
        const lineContainer = this.shadowRoot.querySelector('.line-container');

        const currentValue = 0;
        const fullValue = 2 * this.preferredMaxValue;

        let usedColor = this.getThresholdColor(currentValue, this.preferredMaxValue);

        this.donutChart = echarts.init(donutContainer, 'dark', {renderer: 'svg'});
        const innerRadius = 50;
        const outerRadius = 70;

        this.donutOption = {
            backgroundColor: 'transparent',
            series: [{
                type: 'pie',
                startAngle: 180,
                radius: [innerRadius + '%', outerRadius + '%'],
                data: [
                    {
                        value: currentValue,
                        name: 'Used',
                        itemStyle: {color: this.preferredMaxValue === 0 ? 'transparent' : usedColor}
                    },
                    {
                        value: Math.max(0, fullValue - Math.min(currentValue, fullValue)),
                        name: 'Remaining',
                        itemStyle: {color: 'transparent'}
                    }
                ],
                label: {
                    show: true,
                    position: 'center',
                    fontSize: '1.75rem',
                    fontWeight: 'bold',
                    color: usedColor,
                    textBorderColor: '#000000a0',
                    textBorderWidth: 3
                },
                emphasis: {
                    scale: false
                },
                tooltip: {
                    show: false
                }
            }]
        };
        this.donutChart.setOption(this.donutOption);

        this.lineChart = echarts.init(lineContainer, 'dark', {renderer: 'svg'});
        this.lineOption = {
            backgroundColor: 'transparent',
            xAxis: {
                type: 'category',
                data: this.timeLabels,
                boundaryGap: false,
                axisLine: {lineStyle: {color: '#888'}},
                axisLabel: {show: false},
                axisTick: {show: false}
            },
            yAxis: {
                type: 'value',
                axisLine: {lineStyle: {color: '#888'}},
                axisLabel: {formatter: (value) => Util.formatValue(value, this.format)}
            },
            visualMap: {
                show: false,
                dimension: 1,
                pieces: [
                    {max: 0.75 * this.preferredMaxValue, color: '#3c91ff'},
                    {min: 0.75 * this.preferredMaxValue, max: 0.9 * this.preferredMaxValue, color: '#B8E635'},
                    {min: 0.9 * this.preferredMaxValue, color: '#E64B35'}
                ]
            },
            series: [{
                data: this.historyData,
                type: 'line',
                symbol: 'none',
                lineStyle: {width: 2},
                areaStyle: {color: 'rgba(53,120,229,0.2)'}
            }],
            tooltip: {
                trigger: 'axis',
                formatter: (params) => {
                    const item = params[0];
                    if (item.value === null || item.value === undefined) {
                        return '';
                    }
                    const formattedValue = Util.formatValue(item.value, this.format);
                    return `
                                <div>${params[0].axisValue}</div>
                                <div style="display: flex; align-items: center;">
                                    ${item.marker}
                                    <b>${formattedValue}</b>
                                </div>
                            `;
                },
                transitionDuration: 0.15,
                hideDelay: 0,
            },
            grid: {
                left: '0',
                right: '0',
                bottom: '0',
                top: '0',
                containLabel: false
            },
        };
        this.lineChart.setOption(this.lineOption);
    }

    bindEvents() {
        window.addEventListener('resize', () => {
            if (!this.lineChart) return;

            const lineContainer = this.shadowRoot.querySelector('.line-container');
            const containerWidth = lineContainer.clientWidth;
            const pointWidth = 5;
            const maxPoints = Math.floor(containerWidth / pointWidth);

            let displayedLabels = [];
            let displayedData = [];

            if (this.historyData.length >= maxPoints) {
                displayedLabels = this.timeLabels.slice(-maxPoints);
                displayedData = this.historyData.slice(-maxPoints);
            } else {
                const paddingCount = maxPoints - this.historyData.length;
                displayedLabels = Array(paddingCount).fill("").concat(this.timeLabels);
                displayedData = Array(paddingCount).fill(null).concat(this.historyData);
            }

            this.lineOption.xAxis.data = displayedLabels;
            this.lineOption.series[0].data = displayedData;
            this.lineChart.resize();
            this.lineChart.setOption(this.lineOption);
        });

        window.addEventListener('metricsUpdate', (event) => {
            const data = event.detail;
            if (data[this.id] !== undefined) {
                this.updateData(data[this.id]);
            }
        });
    }

    updateData(newValue) {
        let currentTime = new Date().toLocaleTimeString();
        const fullValue = 2 * this.preferredMaxValue;

        let usedColor = this.getThresholdColor(newValue, this.preferredMaxValue);

        let formattedValue = Util.formatValue(newValue, this.format);
        let donutValue = Util.formatValue(newValue, this.format, true);

        this.donutOption.series[0].label.formatter = function () {
            return donutValue;
        };
        this.donutOption.series[0].label.fontSize = calculateFontSize(formattedValue, this.preferredMaxValue) + 'rem';
        this.donutOption.series[0].label.textBorderColor = '#000000a0';
        this.donutOption.series[0].label.textBorderWidth = 3;
        this.donutOption.series[0].data[0].value = newValue;
        this.donutOption.series[0].data[0].itemStyle.color = (this.preferredMaxValue === 0) ? 'transparent' : usedColor;
        this.donutOption.series[0].label.color = usedColor;
        this.donutOption.series[0].data[1].value = Math.max(0, fullValue - Math.min(newValue, fullValue));
        this.donutChart.setOption(this.donutOption);

        const lineContainerWidth = this.shadowRoot.querySelector('.line-container').clientWidth;
        const pointDensity = 3;
        const maxPoints = Math.floor(lineContainerWidth / pointDensity);

        this.historyData.push(newValue);
        this.timeLabels.push(currentTime);

        if (this.historyData.length > maxPoints) {
            this.historyData.shift();
            this.timeLabels.shift();
        }

        let paddingCount = maxPoints - this.historyData.length;
        let paddedLabels = Array(paddingCount).fill("").concat(this.timeLabels);
        let paddedData = Array(paddingCount).fill(null).concat(this.historyData);

        this.lineOption.xAxis.data = paddedLabels;
        this.lineOption.series[0].data = paddedData;

        this.lineOption.visualMap = {
            show: false,
            dimension: 1,
            pieces: [
                {max: 0.75 * this.preferredMaxValue, color: '#3c91ff'},
                {min: 0.75 * this.preferredMaxValue, max: 0.9 * this.preferredMaxValue, color: '#B8E635'},
                {min: 0.9 * this.preferredMaxValue, color: '#E64B35'}
            ]
        };

        this.lineChart.setOption(this.lineOption);
    }
}

customElements.define('single-metric', SingleMetric);