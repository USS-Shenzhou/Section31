package cn.ussshenzhou.section31.backend.metric;

import java.util.function.Supplier;

/**
 * @author USS_Shenzhou
 */
public class StackedMetric extends BasicMetric{
    public StackedMetric(String id, String name, String desc, String maxDesc, String format, Supplier<?> provider, Supplier<?> maxProvider, int important) {
        super(id, name, desc, maxDesc, format, provider, maxProvider, important);
    }

    @Override
    public void toHtml(StringBuilder body) {
        body.append(String.format("""
                        <stacked-metric
                                id="%1$s"
                                name="%2$s"
                                desc="%3$s"
                                max-desc="%4$s"
                                preferred-max=""
                                format="%5$s">
                        </stacked-metric>""",
                this.id(),
                this.name(),
                this.desc(),
                this.maxDesc(),
                this.format()));
    }
}
