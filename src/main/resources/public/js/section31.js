import {SingleMetric} from "./SingleMetric.js";
import {StackedMetric} from "./StackedMetric.js";

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
