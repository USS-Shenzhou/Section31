package cn.ussshenzhou.section31.backend.provider;

import cn.ussshenzhou.section31.backend.Metric;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * @author USS_Shenzhou
 */
public class JvmHelper {

    public static ArrayList<Metric> getMemoryPoolMetrics() {
        var list = new ArrayList<Metric>();
        var mem = ManagementFactory.getMemoryMXBean();
        list.add(new Metric("heap", "Heap", "", " ", "byte", () -> mem.getHeapMemoryUsage().getUsed(), () -> mem.getHeapMemoryUsage().getMax(), true));
        ManagementFactory.getMemoryPoolMXBeans()
                .stream()
                .filter(MemoryPoolMXBean::isValid)
                .sorted(Comparator.comparing(MemoryPoolMXBean::getName))
                .forEach(pool -> {
                    var name = pool.getName();
                    list.add(new Metric(name, name, "", " ", "byte", () -> pool.getUsage().getUsed(), () -> {
                        var max = pool.getUsage().getMax();
                        return max == -1 ? 0 : max;
                    }, false));
                });
        return list;
    }
}
