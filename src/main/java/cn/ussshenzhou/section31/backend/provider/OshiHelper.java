package cn.ussshenzhou.section31.backend.provider;

import com.google.common.util.concurrent.AtomicDouble;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author USS_Shenzhou
 */
public class OshiHelper {
    private static final SystemInfo SYSTEM = new SystemInfo();
    private static final HardwareAbstractionLayer HARDWARE = SYSTEM.getHardware();
    private static final CentralProcessor PROCESSOR = HARDWARE.getProcessor();
    private static final GlobalMemory RAM = HARDWARE.getMemory();

    private static final ScheduledExecutorService TIMER = Executors.newSingleThreadScheduledExecutor();
    private static final AtomicDouble CPULOAD = new AtomicDouble();
    private static long[] prevTicks;
    private static final AtomicLong RX_DELTA = new AtomicLong();
    private static final AtomicLong TX_DELTA = new AtomicLong();
    private static final AtomicLong RX_PREV = new AtomicLong();
    private static final AtomicLong TX_PREV = new AtomicLong();
    private static final long MAX_NET_SPEED = HARDWARE.getNetworkIFs().stream()
            .filter(NetworkIF::isConnectorPresent)
            .filter(n -> n.getBytesSent() != 0)
            .mapToLong(n -> (int) n.getSpeed())
            .max().orElse(0) / 8;
    public static final Runnable UPDATE = () -> {
        long[] currTicks = PROCESSOR.getSystemCpuLoadTicks();
        if (prevTicks != null) {
            CPULOAD.set(PROCESSOR.getSystemCpuLoadBetweenTicks(prevTicks));
        }
        prevTicks = currTicks;

        long rx = 0, tx = 0;
        for (NetworkIF net : HARDWARE.getNetworkIFs()) {
            rx += net.getBytesRecv();
            tx += net.getBytesSent();
        }

        if (RX_PREV.get() != 0) {
            RX_DELTA.set(rx - RX_PREV.get());
            TX_DELTA.set(tx - TX_PREV.get());
        }
        RX_PREV.set(rx);
        TX_PREV.set(tx);
    };

    static {
        TIMER.scheduleAtFixedRate(UPDATE, 0, 1, TimeUnit.SECONDS);
    }

    public static float getCpu() {
        return CPULOAD.floatValue();
    }

    public static float getCpuMax() {
        return 1;
    }

    public static float getRam() {
        return RAM.getTotal() - RAM.getAvailable();
    }

    public static float getRamMax() {
        return RAM.getTotal();
    }

    public static long getNetRx() {
        return RX_DELTA.longValue();
    }

    public static long getNetTx() {
        return TX_DELTA.longValue();
    }

    public static long getNetMax() {
        return MAX_NET_SPEED;
    }
}
