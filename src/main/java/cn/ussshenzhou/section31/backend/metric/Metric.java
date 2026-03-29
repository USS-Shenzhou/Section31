package cn.ussshenzhou.section31.backend.metric;

import java.util.function.Supplier;

public record Metric(String id,
                     String name,
                     String desc,
                     String maxDesc,
                     String format,
                     Supplier<?> provider,
                     Supplier<?> maxProvider,
                     boolean important
) {
}
