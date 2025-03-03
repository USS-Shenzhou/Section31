const template = document.createElement('template');
template.innerHTML = `
  <div class="single-metric">
    <div class="metric-header">
      <div class="metric-title">
        <span class="name"></span>
        <span class="desc"></span>
      </div>
      <div class="metric-max">
        <span class="max-desc"></span>
      </div>
    </div>
    <div class="metric-body">
      <div class="donut-container"></div>
      <div class="line-container"></div>
    </div>
  </div>
`;

setInterval(() => {
    fetch('/api/all')
        .then(response => response.json())
        .then(aggregatedData => {
            window.dispatchEvent(new CustomEvent('metricsUpdate', {detail: aggregatedData}));
        })
        .catch(error => {
            console.error('Error fetching aggregated metrics:', error);
        });
}, 1000);
fetch('/api/init')
    .then(response => response.json())
    .then(aggregatedData => {
        window.dispatchEvent(new CustomEvent('metricsInit', {detail: aggregatedData}));
    })
    .catch(error => {
        console.error('Error init metrics:', error);
    });

class SingleMetric extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({mode: 'open'});
        this.shadowRoot.appendChild(template.content.cloneNode(true));
    }

    connectedCallback() {
        const linkElem = document.createElement('link');
        linkElem.setAttribute('rel', 'stylesheet');
        linkElem.setAttribute('href', '/css/style.css');
        linkElem.onload = () => {
            const metricName = this.getAttribute('name') || 'Default Metric';
            const description = this.getAttribute('desc') || '';
            const maxDescription = this.getAttribute('max-desc') || '';
            this.id = this.getAttribute('id') || '';
            this.preferredMaxValue = Number(this.getAttribute('preferred-max')) || 0;
            this.format = this.getAttribute('format') || 'float';
            this.shadowRoot.querySelector('.name').textContent = metricName;
            this.shadowRoot.querySelector('.desc').textContent = description;
            this.shadowRoot.querySelector('.max-desc').textContent = maxDescription;
            this.renderCharts();
        };
        this.shadowRoot.prepend(linkElem);
        window.addEventListener('resize', () => {
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
            if (data[this.id]) {
                const metricData = data[this.id];
                this.updateData(metricData);
            }
        });
        window.addEventListener('metricsInit', (event) => {
            const data = event.detail;
            if (data[this.id]) {
                this.preferredMaxValue = Number(data[this.id]);
                let maxDesc = this.shadowRoot.querySelector('.max-desc').textContent;
                if (maxDesc === null || maxDesc === undefined || maxDesc === "") {
                    return;
                }
                this.shadowRoot.querySelector('.max-desc').textContent = this.formatValue(data[this.id]) + maxDesc;
            }
        });
    }

    renderCharts() {
        const donutContainer = this.shadowRoot.querySelector('.donut-container');
        const lineContainer = this.shadowRoot.querySelector('.line-container');

        const currentValue = 0;
        const preferredMaxValue = this.preferredMaxValue;
        const fullValue = 2 * preferredMaxValue;
        this.historyData = [];
        this.timeLabels = [];

        let usedColor;
        if (preferredMaxValue === 0) {
            usedColor = '#3578e5'
        } else if (currentValue >= 0.9 * preferredMaxValue) {
            usedColor = '#E64B35';
        } else if (currentValue >= 0.75 * preferredMaxValue) {
            usedColor = '#B8E635';
        } else {
            usedColor = '#3578e5';
        }

        this.donutChart = echarts.init(donutContainer, 'dark');
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
                        itemStyle: {color: usedColor}
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
                    fontSize: '2rem',
                    fontWeight: 'bold',
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

        this.lineChart = echarts.init(lineContainer, 'dark');
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
                axisLabel: {formatter: (value) => this.formatValue(value)}
            },
            visualMap: {
                show: false,
                dimension: 1,
                pieces: [
                    {max: 0.75 * this.preferredMaxValue, color: '#3578e5'},
                    {min: 0.75 * this.preferredMaxValue, max: 0.9 * this.preferredMaxValue, color: '#B8E635'},
                    {min: 0.9 * this.preferredMaxValue, color: '#E64B35'}
                ]
            },
            series: [{
                data: this.timeLabels,
                type: 'line',
                symbol: 'none',
                lineStyle: {width: 2},
                areaStyle: {color: 'rgba(53,120,229,0.2)'}
            }],
            tooltip: {trigger: 'axis'},
            grid: {
                left: '0.5%',
                right: '2%',
                bottom: '2.5%',
                top: '5%',
                containLabel: true
            },
        };
        this.lineChart.setOption(this.lineOption);
    }

    formatValue(value, netLine = false) {
        if (value === null || value === undefined || value === "") {
            return "N/A";
        }
        if (typeof value !== "number" || isNaN(value)) {
            return String(value);
        }
        switch (this.format) {
            case 'int':
                return Math.round(value) + `\u200B`;
            case 'percent':
                return (value * 100).toFixed(0) + '%';
            case 'float':
                return value.toFixed(2);
            case 'net':
                return this.formatBytes(value, netLine);
            default:
                return value;
        }
    }

    formatBytes(bytes, netLine = false) {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['Bps', 'KiBps', 'MiBps', 'GiBps', 'TiBps', 'PiBps'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        const value = parseFloat((bytes / Math.pow(k, i)).toFixed(2));
        return netLine ? `${value}\n${sizes[i]}` : `${value} ${sizes[i]}`;
    }

    updateData(newValue) {
        let currentTime = new Date().toLocaleTimeString();
        const preferredMax = this.preferredMaxValue;
        const fullValue = 2 * preferredMax;

        let usedColor;
        if (preferredMax === 0) {
            usedColor = '#3578e5'
        } else if (newValue >= 0.9 * preferredMax) {
            usedColor = '#E64B35';
        } else if (newValue >= 0.75 * preferredMax) {
            usedColor = '#B8E635';
        } else {
            usedColor = '#3578e5';
        }

        let formattedValue = this.formatValue(newValue);
        let baseFontSize = 2;
        let extraChars = Math.max(0, formattedValue.length - 5);
        let fontSize = Math.max(1, baseFontSize - extraChars * 0.2);
        let donutValue = this.formatValue(newValue, true)
        this.donutOption.series[0].label.formatter = function () {
            return donutValue;
        };
        this.donutOption.series[0].label.fontSize = fontSize + 'rem';
        this.donutOption.series[0].data[0].value = newValue;
        this.donutOption.series[0].data[0].itemStyle.color = usedColor;
        this.donutOption.series[0].label.color = usedColor;
        this.donutOption.series[0].data[1].value = Math.max(0, fullValue - Math.min(newValue, fullValue));
        this.donutChart.setOption(this.donutOption);

        const lineContainerWidth = this.shadowRoot.querySelector('.line-container').clientWidth;
        const pointWidth = 5;
        const maxPoints = Math.floor(lineContainerWidth / pointWidth);

        this.historyData.push(newValue);
        this.timeLabels.push(currentTime);
        if (this.historyData.length > maxPoints) {
            this.historyData.shift();
            this.timeLabels.shift();
        }
        let paddingCount = maxPoints - this.historyData.length;
        let paddedLabels = Array(paddingCount).fill("");
        let paddedData = Array(paddingCount).fill(null);
        paddedLabels = paddedLabels.concat(this.timeLabels);
        paddedData = paddedData.concat(this.historyData);
        this.lineOption.xAxis.data = paddedLabels;
        this.lineOption.series[0].data = paddedData;
        this.lineOption.visualMap = {
            show: false,
            dimension: 1,
            pieces: [
                {max: 0.75 * this.preferredMaxValue, color: '#3578e5'},
                {min: 0.75 * this.preferredMaxValue, max: 0.9 * this.preferredMaxValue, color: '#B8E635'},
                {min: 0.9 * this.preferredMaxValue, color: '#E64B35'}
            ]
        };
        this.lineChart.setOption(this.lineOption);
    }
}

customElements.define('single-metric', SingleMetric);
