import {SingleMetric} from "./SingleMetric.js";
import {StackedMetric} from "./StackedMetric.js";

let pollingIntervalId = null;
let isPolling = false;
const glowOverlay = document.getElementById('pause-glow-overlay');

const content = document.getElementById('content');
const sidebar = document.getElementById('sidebar');
const sidebarToggle = document.getElementById('sidebar-toggle');
const tabButtons = Array.from(document.querySelectorAll('.tab-button'));
const tabPanels = Array.from(document.querySelectorAll('.tab-panel'));

function selectTab(tabId) {
    tabButtons.forEach(button => {
        button.classList.toggle('active', button.dataset.tab === tabId);
    });
    tabPanels.forEach(panel => {
        panel.hidden = panel.dataset.tab !== tabId;
    });
    content.scrollTop = 0;
}

tabButtons.forEach(button => {
    button.addEventListener('click', () => {
        if (!button.classList.contains('active')) {
            selectTab(button.dataset.tab);
        }
        button.blur();
    });
});

sidebarToggle.addEventListener('click', () => {
    sidebar.classList.toggle('collapsed');
    sidebarToggle.blur();
});

function fetchMetrics() {
    fetch('/api/all')
        .then(response => response.json())
        .then(aggregatedData => {
            window.dispatchEvent(new CustomEvent('metricsUpdate', {detail: aggregatedData}));
        })
        .catch(error => {
            console.error('Error fetching aggregated metrics:', error);
        });
}

function startPolling() {
    if (!isPolling) {
        isPolling = true;
        if (glowOverlay) {
            glowOverlay.classList.remove('active');
        }
        fetchMetrics();
        pollingIntervalId = setInterval(fetchMetrics, 1000);
    }
}

function stopPolling() {
    if (isPolling) {
        isPolling = false;
        if (glowOverlay) {
            glowOverlay.classList.add('active');
        }
        clearInterval(pollingIntervalId);
        pollingIntervalId = null;
    }
}

startPolling();
document.addEventListener('keydown', (event) => {
    const activeTagName = document.activeElement ? document.activeElement.tagName : '';
    const isInputFocused = activeTagName === 'INPUT' || activeTagName === 'TEXTAREA';
    if (!isInputFocused && event.code === 'Space') {
        event.preventDefault();
        if (isPolling) {
            stopPolling();
        } else {
            startPolling();
        }
    }
});

const hintToast = document.getElementById('space-hint-toast');

window.addEventListener('load', () => {
    if (hintToast) {
        hintToast.addEventListener('animationend', () => {
            hintToast.classList.remove('show');
        }, {once: true});
        hintToast.classList.add('show');
    }
});

fetch('/api/init')
    .then(response => response.json())
    .then(aggregatedData => {
        window.dispatchEvent(new CustomEvent('metricsInit', {detail: aggregatedData}));
    })
    .catch(error => {
        console.error('Error init metrics:', error);
    });
