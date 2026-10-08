import * as Util from './Util.js';
import {calculateFontSize} from './Util.js';
import {BaseMetric} from './BaseMetric.js';

export class SingleMetric extends BaseMetric {
    constructor() {
        super();
        this.historyData = [];
        this.timeLabels = [];
        this.latestValue = undefined;
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

    pushData(newValue, timeLabel) {
        this.latestValue = newValue;
        this.historyData.push(newValue);
        this.timeLabels.push(timeLabel);
    }

    trimHistory(capacity) {
        const overflow = this.historyData.length - capacity;
        if (overflow > 0) {
            this.historyData.splice(0, overflow);
            this.timeLabels.splice(0, overflow);
        }
    }

    render() {
        const maxPoints = this.getVisiblePointCount();
        if (maxPoints <= 0) {
            return;
        }
        if (this.latestValue !== undefined) {
            this.renderDonut(this.latestValue);
        }

        const shownData = this.historyData.slice(-maxPoints);
        const shownLabels = this.timeLabels.slice(-maxPoints);
        const paddingCount = maxPoints - shownData.length;
        this.lineOption.xAxis.data = Array(paddingCount).fill("").concat(shownLabels);
        this.lineOption.series[0].data = Array(paddingCount).fill(null).concat(shownData);

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

    renderDonut(newValue) {
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
    }
}

customElements.define('single-metric', SingleMetric);