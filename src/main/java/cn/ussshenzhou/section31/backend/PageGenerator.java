package cn.ussshenzhou.section31.backend;

import cn.ussshenzhou.section31.backend.metric.BasicMetric;
import cn.ussshenzhou.section31.backend.metric.MetricTab;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @author USS_Shenzhou
 */
public class PageGenerator {
    private final static String HTML;
    private static volatile int metricNumber = 0;
    private static String page = null;

    static {
        try (InputStream inputStream = PageGenerator.class.getResourceAsStream("/public/section31.html");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
        ) {
            HTML = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getPage() {
        if (page == null) {
            updatePage();
            return page;
        }
        int number = MetricsManager.getMetricCount();
        if (number != metricNumber) {
            metricNumber = number;
            updatePage();
        }
        return page;
    }

    private static void updatePage() {
        var tabs = new StringBuilder();
        var panels = new StringBuilder();
        int index = 0;
        for (var tab : MetricsManager.getMetrics().values()) {
            boolean selected = index == 0;
            tabs.append(String.format("""
                    <button class="sidebar-item tab-button%s" type="button" data-tab="%d" title="%s">
                        <span class="tab-icon">%s</span>
                        <span class="tab-label">%s</span>
                    </button>
                    """, selected ? " active" : "", index, tab.name(), getIconHtml(tab), tab.name()));
            panels.append(String.format("""
                    <section class="tab-panel" data-tab="%d"%s>
                    """, index, selected ? "" : " hidden"));
            tab.groups().forEach((group, metrics) -> appendGroup(panels, group, metrics));
            panels.append("""
                    </section>
                    """);
            index++;
        }
        page = HTML.replace("${{tabs}}", tabs.toString()).replace("${{panels}}", panels.toString());
    }

    private static void appendGroup(StringBuilder content, String group, List<BasicMetric> metrics) {
        content.append(String.format("""
                <details open class="metric-group">
                    <summary class="metric-group-title">%s</summary>""", group));
        int importance = 0;
        for (int i = 0; i < metrics.size(); i++) {
            var metric = metrics.get(i);
            if (i == 0) {
                importance = metric.importance();
                content.append(getContainerHtml(importance));
            }
            if (importance != metric.importance()) {
                content.append("</div>\n");
                importance = metric.importance();
                content.append(getContainerHtml(importance));
            }
            metric.toHtml(content);
        }
        content.append("""
                    </div>
                </details>""");
    }

    private static String getIconHtml(MetricTab tab) {
        if (tab.icon().isEmpty()) {
            var initial = tab.name().isEmpty() ? "?" : Character.toString(tab.name().codePointAt(0)).toUpperCase(Locale.ROOT);
            return String.format("""
                    <span class="tab-icon-letter">%s</span>""", initial);
        }
        return String.format("""
                <svg viewBox="0 0 24 24">%s</svg>""", tab.icon());
    }

    private static String getContainerHtml(int importance) {
        return switch (importance) {
            case -100 -> """
                    <div class="metrics-group-container" style="--metric-height: 20rem;">""";
            case -2 -> """
                    <div class="metrics-group-container-grid" style="--metric-height: 14rem;">""";
            case -1 -> """
                    <div class="metrics-group-container" style="--metric-height: 14rem;">""";
            case 1 -> """
                    <div class="metrics-group-container" style="--metric-height: 8rem;">""";
            default -> """
                    <div class="metrics-group-container-grid" style="--metric-height: 8rem;">""";
        };
    }
}
