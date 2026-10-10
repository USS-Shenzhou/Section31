package cn.ussshenzhou.section31.util;

import com.mojang.logging.LogUtils;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.ptr.LongByReference;

public class WindowsHelper {
    public static final double CYCLES_NS = tryCyclesNs();

    private static double tryCyclesNs() {
        var currentThread = Kernel32.INSTANCE.GetCurrentThread();
        var cycles = new LongByReference();
        double bestCyclesPerNs = 0;
        for (int i = 0; i < 5 || bestCyclesPerNs == 0; i++) {
            Kernel32Helper.INSTANCE.QueryThreadCycleTime(currentThread, cycles);
            long startC = cycles.getValue();
            long endC = startC;
            long startNs = System.nanoTime();
            long nowNs = startNs;
            long prevNs;
            while (nowNs - startNs < 100_000_000) {
                prevNs = nowNs;
                Kernel32Helper.INSTANCE.QueryThreadCycleTime(currentThread, cycles);
                nowNs = System.nanoTime();
                if (nowNs - prevNs > 100_000) {
                    nowNs = prevNs;
                    break;
                }
                endC = cycles.getValue();
            }
            if (nowNs - startNs >= 1_000_000) {
                bestCyclesPerNs = Math.max(bestCyclesPerNs, (double) (endC - startC) / (nowNs - startNs));
            }
        }
        LogUtils.getLogger().info("Section 31 cycles per ns: {}", bestCyclesPerNs);
        return bestCyclesPerNs;
    }
}
