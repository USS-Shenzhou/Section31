package cn.ussshenzhou.section31.util;

import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author USS_Shenzhou
 */
public class SimpleSlidingWindowCollector {
    private record DataNode(long expireNanos, int value) {
    }

    private final ConcurrentLinkedQueue<DataNode> queue = new ConcurrentLinkedQueue<>();
    private final ReentrantLock cleanLock = new ReentrantLock();

    private static final long WINDOW_NANOS = 1_000_000_000L;

    public void put(Identifier id, int value) {
        long expireTime = System.nanoTime() + WINDOW_NANOS;
        queue.add(new DataNode(expireTime, value));
    }

    public int read() {
        long now = System.nanoTime();
        if (cleanLock.tryLock()) {
            try {
                DataNode head;
                while ((head = queue.peek()) != null && head.expireNanos() <= now) {
                    queue.poll();
                }
            } finally {
                cleanLock.unlock();
            }
        }
        int result = 0;

        for (DataNode node : queue) {
            if (node.expireNanos() > now) {
                result += node.value;
            }
        }

        return result;
    }
}
