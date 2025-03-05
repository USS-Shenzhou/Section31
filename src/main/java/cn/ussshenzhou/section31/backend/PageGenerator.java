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
                        <summary class="group-title">%s</summary>
                        <div class="metrics-group-container">""", group));
            metrics.forEach((metric) -> content.append(String.format("""
                            <single-metric
                                    id="%1$s"
                                    name="%2$s"
                                    desc="%3$s"
                                    max-desc="%4$s"
                                    preferred-max=""
                                    format="%5$s">
                            </single-metric>""",
                    metric.id(),
                    metric.name(),
                    metric.desc(),
                    metric.maxDesc(),
                    metric.format())));
            content.append("""
                        </div>
                    </details>""");
        });
        page = HTML.replace("${{metrics}}", content.toString());
    }
}
