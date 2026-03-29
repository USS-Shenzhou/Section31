package cn.ussshenzhou.section31.backend.metric;

import java.util.function.Supplier;

/**
 * @author USS_Shenzhou
 */
public class BasicMetric {
    protected final String id;
    protected final String name;
    protected final String desc;
    protected final String maxDesc;
    protected final String format;
    protected final Supplier<?> provider;
    protected final Supplier<?> maxProvider;
    protected final int importance;

    public BasicMetric(String id,
                       String name,
                       String desc,
                       String maxDesc,
                       String format,
                       Supplier<?> provider,
                       Supplier<?> maxProvider,
                       int importance
    ) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.maxDesc = maxDesc;
        this.format = format;
        this.provider = provider;
        this.maxProvider = maxProvider;
        this.importance = importance;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String desc() {
        return desc;
    }

    public String maxDesc() {
        return maxDesc;
    }

    public String format() {
        return format;
    }

    public Supplier<?> provider() {
        return provider;
    }

    public Supplier<?> maxProvider() {
        return maxProvider;
    }

    public int importance() {
        return importance;
    }

    public void toHtml(StringBuilder body) {
        body.append(String.format("""
                        <single-metric
                                id="%1$s"
                                name="%2$s"
                                desc="%3$s"
                                max-desc="%4$s"
                                preferred-max=""
                                format="%5$s">
                        </single-metric>""",
                this.id(),
                this.name(),
                this.desc(),
                this.maxDesc(),
                this.format()));
    }

}
