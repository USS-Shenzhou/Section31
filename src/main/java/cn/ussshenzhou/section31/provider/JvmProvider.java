package cn.ussshenzhou.section31.provider;

import cn.ussshenzhou.section31.backend.metric.BasicMetric;
import cn.ussshenzhou.section31.backend.metric.StackedMetric;
import cn.ussshenzhou.section31.util.Kernel32Helper;
import cn.ussshenzhou.section31.util.NtHelper;
import cn.ussshenzhou.section31.util.WindowsHelper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;
import net.minecraft.util.Util;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static cn.ussshenzhou.section31.util.Util.NO_MAX;

/**
 * @author USS_Shenzhou
 */
public class JvmProvider {

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
        list.add(new StackedMetric("threads", "Threads", (percent ? "Active Threads and Usage." : "Active Threads.") + " Threads lasted too short may not get captured", "", percent ? "percent" : "int", () -> getThreadCpuUsage(), NO_MAX, -100));
    }

    private record ThreadCpuRecord(long cpuTimeNs, long uptimeNs) {
    }

    private static final HashMap<Long, ThreadCpuRecord> THREAD_CPU_RECORD = new HashMap<>();
    private static volatile HashMap<String, Float> threadCpuUsage = new HashMap<>();
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
            .setNameFormat("Section31-Threads-Usage-Count-Thread-%d")
            .setDaemon(true)
            .build());

    static {
        SCHEDULER.scheduleAtFixedRate(x64WindowsPreciseMode() ? JvmProvider::updateThreadCpuUsageWindows : JvmProvider::updateThreadCpuUsage, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private static Map<String, Float> getThreadCpuUsage() {
        return threadCpuUsage;
    }

    private static boolean x64WindowsPreciseMode() {
        try {
            var nativeMachine = new ShortByReference();
            return Platform.isWindows() &&
                    Kernel32Helper.INSTANCE.IsWow64Process2(Kernel32.INSTANCE.GetCurrentProcess(), new ShortByReference(), nativeMachine) &&
                    (nativeMachine.getValue() & 0xffff) == 0x8664 &&
                    ((String) ManagementFactory.getPlatformMBeanServer().invoke(new ObjectName("com.sun.management:type=DiagnosticCommand"), "vmInfo", null, null))
                            .lines().filter(line -> line.startsWith("CPU:")).anyMatch(line -> line.contains(", tscinvbit"));
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static void updateThreadCpuUsage() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        HashMap<String, Float> resultMap = new HashMap<>();
        if (!threadMXBean.isThreadCpuTimeSupported()) {
            for (var info : threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds())) {
                if (info != null && (info.getThreadState() == Thread.State.RUNNABLE || info.getThreadState() == Thread.State.NEW)) {
                    resultMap.put(info.getThreadName(), 1f);
                }
            }
            threadCpuUsage = resultMap;
            return;
        }
        Set<Long> existIds = new HashSet<>();
        for (var info : threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds())) {
            if (info != null) {
                long threadId = info.getThreadId();
                existIds.add(threadId);
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
                        var name = info.getThreadName();
                        if (resultMap.containsKey(name)) {
                            name += (" (" + threadId + ')');
                        }
                        resultMap.put(name, usage);
                    }
                    return new ThreadCpuRecord(currentCpuTimeNs, currentUptimeNs);
                });
            }
        }
        THREAD_CPU_RECORD.keySet().removeIf(id -> !existIds.contains(id));
        threadCpuUsage = resultMap;
    }

    private static final class ThreadCpuRecordWindows {
        private final int threadId;
        private final WinNT.HANDLE handle;
        private long lastCycles;

        private ThreadCpuRecordWindows(int threadId, WinNT.HANDLE handle, long lastCycles) {
            this.threadId = threadId;
            this.handle = handle;
            this.lastCycles = lastCycles;
        }

        private String updateAndGetName() {
            var description = new PointerByReference();
            String name = "";
            if (Kernel32Helper.INSTANCE.GetThreadDescription(handle, description) >= 0 && description.getValue() != null) {
                name = description.getValue().getWideString(0);
            }
            Kernel32.INSTANCE.LocalFree(description.getValue());
            return name.isEmpty() ? "[unknown]" : name;
        }
    }

    private static final int THREAD_ACCESS = WinNT.SYNCHRONIZE | WinNT.THREAD_QUERY_LIMITED_INFORMATION;
    private static final LinkedHashMap<Integer, ThreadCpuRecordWindows> THREAD_CPU_RECORD_WINDOWS = new LinkedHashMap<>();
    private static long lastUpdateNs = -1;

    private static void updateThreadCpuUsageWindows() {
        if (WindowsHelper.CYCLES_NS == 0) {
            return;
        }
        checkIfNewThreads();
        long now = System.nanoTime();
        if (lastUpdateNs == -1) {
            lastUpdateNs = now;
            return;
        }
        HashMap<String, Float> resultMap = new HashMap<>();
        double deltaTotalCycle = WindowsHelper.CYCLES_NS * (now - lastUpdateNs);
        var cycles = new LongByReference();
        for (var thread : THREAD_CPU_RECORD_WINDOWS.values()) {
            if (Kernel32Helper.INSTANCE.QueryThreadCycleTime(thread.handle, cycles)) {
                var name = thread.updateAndGetName();
                if (resultMap.containsKey(name)) {
                    name += (" (" + thread.threadId + ')');
                }
                float usage = (cycles.getValue() - thread.lastCycles) / (float) deltaTotalCycle;
                if (usage > 0.005){
                    resultMap.put(name, usage);
                }
                thread.lastCycles = cycles.getValue();
            }
        }
        THREAD_CPU_RECORD_WINDOWS.values().removeIf(t -> {
            if (Kernel32.INSTANCE.WaitForSingleObject(t.handle, 0) == WinBase.WAIT_OBJECT_0) {
                Kernel32.INSTANCE.CloseHandle(t.handle);
                return true;
            }
            return false;
        });
        lastUpdateNs = now;
        threadCpuUsage = resultMap;
    }

    private static void checkIfNewThreads() {
        var process = Kernel32.INSTANCE.GetCurrentProcess();
        WinNT.HANDLE nextThread = null;
        boolean remove = false;
        boolean firstTime = THREAD_CPU_RECORD_WINDOWS.isEmpty();
        while (true) {
            var next = new WinNT.HANDLEByReference();
            int status = NtHelper.INSTANCE.NtGetNextThread(process, nextThread, THREAD_ACCESS, 0, 0, next);
            if (nextThread != null && remove) {
                Kernel32.INSTANCE.CloseHandle(nextThread);
            }
            if (status != 0) {
                return;
            }
            nextThread = next.getValue();
            int threadId = Kernel32Helper.INSTANCE.GetThreadId(nextThread);
            if (THREAD_CPU_RECORD_WINDOWS.containsKey(threadId) || Kernel32.INSTANCE.WaitForSingleObject(nextThread, 0) == WinBase.WAIT_OBJECT_0) {
                remove = true;
            } else {
                long baselineCycles = 0;
                if (firstTime) {
                    var cycles = new LongByReference();
                    if (Kernel32Helper.INSTANCE.QueryThreadCycleTime(nextThread, cycles)) {
                        baselineCycles = cycles.getValue();
                    }
                }

                THREAD_CPU_RECORD_WINDOWS.put(threadId, new ThreadCpuRecordWindows(threadId, nextThread, baselineCycles));
                remove = false;
            }
        }
    }
}
