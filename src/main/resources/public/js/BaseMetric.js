import * as Util from './Util.js';

const POINT_DENSITY = 3;
const RESIZE_SETTLE_DELAY = 150;

let provisionalCapacity = Math.floor(window.innerWidth / POINT_DENSITY);

window.addEventListener('resize', () => {
    provisionalCapacity = Math.max(provisionalCapacity, Math.floor(window.innerWidth / POINT_DENSITY));
});

const RENDER_BUDGET_PER_FRAME = 8;
const pendingRenders = new Set();
let renderLoopScheduled = false;

function scheduleRender(metric) {
    pendingRenders.add(metric);
    if (!renderLoopScheduled) {
        renderLoopScheduled = true;
        requestAnimationFrame(runPendingRenders);
    }
}

function runPendingRenders() {
    const frameStart = performance.now();
    for (const metric of pendingRenders) {
        pendingRenders.delete(metric);
        metric.flushRender();
        if (performance.now() - frameStart > RENDER_BUDGET_PER_FRAME) {
            break;
        }
    }
    if (pendingRenders.size > 0) {
        requestAnimationFrame(runPendingRenders);
    } else {
        renderLoopScheduled = false;
    }
}

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

export class BaseMetric extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({mode: 'open'});
        this.shadowRoot.appendChild(template.content.cloneNode(true));
        this.stylesheetLoaded = false;
        this.chartsCreated = false;
        this.visible = false;
        this.showPending = false;
        this.resizeSettleTimer = null;
        this.historyCapacity = 0;
        this.capacityMeasured = false;
    }

    connectedCallback() {
        this.metricName = this.getAttribute('name') || 'Default Metric';
        this.description = this.getAttribute('desc') || '';
        this.maxDescription = this.getAttribute('max-desc') || '';
        this.id = this.getAttribute('id') || '';
        this.preferredMaxValue = Number(this.getAttribute('preferred-max')) || 0;
        this.format = this.getAttribute('format') || 'float';

        this.shadowRoot.querySelector('.name').textContent = this.metricName;
        this.shadowRoot.querySelector('.desc').textContent = this.description;
        this.shadowRoot.querySelector('.max-desc').textContent = this.maxDescription;
        this.lineContainer = this.shadowRoot.querySelector('.line-container');

        const linkElem = document.createElement('link');
        linkElem.setAttribute('rel', 'stylesheet');
        linkElem.setAttribute('href', '/css/style.css');
        linkElem.onload = () => {
            this.stylesheetLoaded = true;
            this.requestShow();
        };
        this.shadowRoot.prepend(linkElem);

        window.addEventListener('metricsInit', (event) => {
            const data = event.detail;
            if (data[this.id]) {
                this.preferredMaxValue = Number(data[this.id]);
                let maxDesc = this.shadowRoot.querySelector('.max-desc').textContent;
                if (!maxDesc) {
                    return;
                }
                this.shadowRoot.querySelector('.max-desc').textContent = Util.formatValue(data[this.id], this.format) + maxDesc;
            }
        });

        window.addEventListener('metricsUpdate', (event) => {
            const value = event.detail[this.id];
            if (value === undefined) {
                return;
            }
            this.pushData(value, new Date().toLocaleTimeString());
            this.trimHistory(this.getHistoryCapacity());
            if (this.visible) {
                scheduleRender(this);
            }
        });

        new ResizeObserver(entries => {
            this.onContainerResize(entries[entries.length - 1].contentRect.width);
        }).observe(this.lineContainer);
    }

    onContainerResize(width) {
        const wasVisible = this.visible;
        this.visible = width > 0;
        clearTimeout(this.resizeSettleTimer);
        if (!this.visible) {
            return;
        }
        if (wasVisible) {
            this.resizeSettleTimer = setTimeout(() => this.requestShow(), RESIZE_SETTLE_DELAY);
        } else {
            this.requestShow();
        }
    }

    requestShow() {
        this.showPending = true;
        scheduleRender(this);
    }

    flushRender() {
        if (!this.stylesheetLoaded || !this.visible) {
            return;
        }
        if (this.showPending || !this.chartsCreated) {
            this.showPending = false;
            this.showCharts();
        } else {
            this.render();
        }
    }

    showCharts() {
        if (!this.stylesheetLoaded || !this.visible) {
            return;
        }
        this.updateCapacity();
        if (this.chartsCreated) {
            this.donutChart.resize();
            this.lineChart.resize();
        } else {
            this.renderCharts();
            this.bindChartEvents();
            this.chartsCreated = true;
        }
        this.render();
    }

    updateCapacity() {
        const visiblePoints = this.getVisiblePointCount();
        if (visiblePoints <= 0) {
            return;
        }
        if (!this.capacityMeasured) {
            this.capacityMeasured = true;
            this.historyCapacity = visiblePoints;
            this.trimHistory(visiblePoints);
        } else if (visiblePoints > this.historyCapacity) {
            this.historyCapacity = visiblePoints;
        }
    }

    getHistoryCapacity() {
        return this.capacityMeasured ? this.historyCapacity : provisionalCapacity;
    }

    getVisiblePointCount() {
        return Math.floor(this.lineContainer.clientWidth / POINT_DENSITY);
    }

    getThresholdColor(currentValue, maxValue) {
        if (maxValue === 0) return '#3c91ff';
        if (currentValue >= 0.9 * maxValue) return '#E64B35';
        if (currentValue >= 0.75 * maxValue) return '#B8E635';
        return '#3c91ff';
    }

    renderCharts() {
        console.warn('renderCharts() must be implemented by subclass');
    }

    bindChartEvents() {
    }

    pushData(value, timeLabel) {
        console.warn('pushData() must be implemented by subclass');
    }

    trimHistory(capacity) {
        console.warn('trimHistory() must be implemented by subclass');
    }

    render() {
        console.warn('render() must be implemented by subclass');
    }
}