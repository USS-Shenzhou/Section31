import * as Util from './Util.js';

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
    }

    connectedCallback() {
        const linkElem = document.createElement('link');
        linkElem.setAttribute('rel', 'stylesheet');
        linkElem.setAttribute('href', '/css/style.css');

        linkElem.onload = () => {
            this.metricName = this.getAttribute('name') || 'Default Metric';
            this.description = this.getAttribute('desc') || '';
            this.maxDescription = this.getAttribute('max-desc') || '';
            this.id = this.getAttribute('id') || '';
            this.preferredMaxValue = Number(this.getAttribute('preferred-max')) || 0;
            this.format = this.getAttribute('format') || 'float';

            this.shadowRoot.querySelector('.name').textContent = this.metricName;
            this.shadowRoot.querySelector('.desc').textContent = this.description;
            this.shadowRoot.querySelector('.max-desc').textContent = this.maxDescription;

            this.renderCharts();
            this.bindEvents();
        };

        this.shadowRoot.prepend(linkElem);

        window.addEventListener('metricsInit', (event) => {
            const data = event.detail;
            if (data[this.id]) {
                this.preferredMaxValue = Number(data[this.id]);
                let maxDesc = this.shadowRoot.querySelector('.max-desc').textContent;
                if (!maxDesc) return;
                this.shadowRoot.querySelector('.max-desc').textContent = Util.formatValue(data[this.id], this.format) + maxDesc;
            }
        });
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

    bindEvents() {
        console.warn('bindEvents() must be implemented by subclass');
    }
}