package cn.ussshenzhou.section31.backend;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
        int number = 0;
        for (var list : MetricsManager.getMetrics().values()) {
            number += list.size();
        }
        if (number != metricNumber) {
            metricNumber = number;
            updatePage();
        }
        return page;
    }

    private static void updatePage() {
        var content = new StringBuilder();
        MetricsManager.getMetrics().forEach((group, metrics) -> {
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
        });
        page = HTML.replace("${{metrics}}", content.toString());
    }

    private static String getContainerHtml(int importance) {
        return switch (importance) {
            case -1 -> """
                    <div class="metrics-group-container-grid" style="--metric-height: 14rem;">""";
            case 0 -> """
                    <div class="metrics-group-container" style="--metric-height: 14rem;">""";
            case 1 -> """
                    <div class="metrics-group-container" style="--metric-height: 8rem;">""";
            default -> """
                    <div class="metrics-group-container-grid" style="--metric-height: 8rem;">""";
        };
    }
}
