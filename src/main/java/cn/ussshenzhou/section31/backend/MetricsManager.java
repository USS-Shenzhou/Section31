package cn.ussshenzhou.section31.backend;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author USS_Shenzhou
 */
public class MetricsManager {
    private static final ConcurrentHashMap<String, ArrayList<Metrics>> METRICS = new ConcurrentHashMap<>();


    public record Metrics(String id,
                          String name,
                          String desc,
                          String maxDesc,
                          String format,
                          Supplier<?> maxProvider,
                          Supplier<?> provider
                          ) {
    }
}
