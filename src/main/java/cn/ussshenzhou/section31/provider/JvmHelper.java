package cn.ussshenzhou.section31.provider;

import cn.ussshenzhou.section31.backend.metric.BasicMetric;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * @author USS_Shenzhou
 */
public class JvmHelper {

    public static ArrayList<BasicMetric> getMemoryPoolMetrics() {
        var list = new ArrayList<BasicMetric>();
        var mem = ManagementFactory.getMemoryMXBean();
        list.add(new BasicMetric("heap", "Heap", "", " ", "byte", () -> mem.getHeapMemoryUsage().getUsed(), () -> mem.getHeapMemoryUsage().getMax(), 1));
        ManagementFactory.getMemoryPoolMXBeans()
                .stream()
                .filter(MemoryPoolMXBean::isValid)
                .sorted(Comparator.comparing(MemoryPoolMXBean::getName))
                .forEach(pool -> {
                    var name = pool.getName();
                    list.add(new BasicMetric(name, name, "", " ", "byte", () -> pool.getUsage().getUsed(), () -> {
                        var max = pool.getUsage().getMax();
                        return max == -1 ? 0 : max;
                    }, 2));
                });
        return list;
    }
}
