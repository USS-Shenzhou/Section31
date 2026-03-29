package cn.ussshenzhou.section31.provider.network;

import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author USS_Shenzhou
 */
public class SlidingWindowCollector {
    private record DataNode(long expireNanos, Identifier id, int size) {}

    private final ConcurrentLinkedQueue<DataNode> queue = new ConcurrentLinkedQueue<>();
    private final ReentrantLock cleanLock = new ReentrantLock();

    private static final long WINDOW_NANOS = 1_000_000_000L;

    public void put(Identifier id, int size) {
        long expireTime = System.nanoTime() + WINDOW_NANOS;
        queue.add(new DataNode(expireTime, id, size));
    }

    public Map<String, Integer> read() {
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
        Map<String, Integer> result = new HashMap<>();

        for (DataNode node : queue) {
            if (node.expireNanos() > now) {
                result.merge(node.id().toString(), node.size(), Integer::sum);
            }
        }

        return result;
    }
}
