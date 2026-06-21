package cn.ussshenzhou.section31.provider;

import cn.ussshenzhou.section31.backend.metric.BasicMetric;
import cn.ussshenzhou.section31.backend.metric.StackedMetric;
import com.mojang.logging.LogUtils;
import net.minecraft.util.Util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static cn.ussshenzhou.section31.util.Util.NO_MAX;

/**
 * @author USS_Shenzhou
 */
public class JvmHelper {

    public static void getMemoryPoolMetrics(ArrayList<BasicMetric> list) {
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
    }

    public static void getThreadsMetrics(ArrayList<BasicMetric> list) {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        if (!threadMXBean.isThreadCpuTimeEnabled()) {
            threadMXBean.setThreadCpuTimeEnabled(true);
        }
        var percent = threadMXBean.isThreadCpuTimeSupported();
        list.add(new StackedMetric("threads", "Threads", (percent ? "Active Threads and Usage." : "Active Threads.") + " Threads lasting under 1 second may not be captured", "", percent ? "percent" : "int", () -> getThreadCpuUsage(threadMXBean), NO_MAX, 0));
    }

    private record threadCpuRecord(long cpuTimeNs, long uptimeNs) {
    }

    private static final ConcurrentHashMap<Long, threadCpuRecord> THREAD_CPU_RECORD = new ConcurrentHashMap<>();

    private static Map<String, Float> getThreadCpuUsage(ThreadMXBean threadMXBean) {
        long now = Util.getNanos();
        Map<String, Float> resultMap = new HashMap<>();
        if (!threadMXBean.isThreadCpuTimeSupported()) {
            forEveryActiveThread(threadMXBean, i -> resultMap.put(i.getThreadName(), 1f));
            return resultMap;
        }
        Set<Long> aliveIds = new HashSet<>();

        forEveryThread(threadMXBean, i -> {
            long threadId = i.getThreadId();
            aliveIds.add(threadId);
            long currentUptimeNs = Util.getNanos();
            long currentCpuTimeNs = threadMXBean.getThreadCpuTime(threadId);
            if (currentCpuTimeNs == -1) {
                return;
            }
            THREAD_CPU_RECORD.compute(threadId, (_, r) -> {
                if (r != null) {
                    long cpuTimeDelta = currentCpuTimeNs - r.cpuTimeNs;
                    long timeDelta = currentUptimeNs - r.uptimeNs;
                    float usage = 0f;
                    if (timeDelta > 0) {
                        usage = (float) ((double) cpuTimeDelta / timeDelta);
                    }
                    if (isActiveState(i.getThreadState())) {
                        resultMap.put(i.getThreadName(), usage);
                    }
                }
                return new threadCpuRecord(currentCpuTimeNs, currentUptimeNs);
            });
        });

        THREAD_CPU_RECORD.keySet().removeIf(id -> !aliveIds.contains(id));
        LogUtils.getLogger().warn("{} ms", (Util.getNanos() - now) / 1000_000f);
        return resultMap;
    }

    private static void forEveryThread(ThreadMXBean threadMXBean, Consumer<ThreadInfo> consumer) {
        for (var info : threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds())) {
            if (info != null) {
                consumer.accept(info);
            }
        }
    }

    private static void forEveryActiveThread(ThreadMXBean threadMXBean, Consumer<ThreadInfo> consumer) {
        for (var info : threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds())) {
            if (info != null && isActiveState(info.getThreadState())) {
                consumer.accept(info);
            }
        }
    }

    private static boolean isActiveState(Thread.State state) {
        return state == Thread.State.RUNNABLE || state == Thread.State.NEW;
    }
}
