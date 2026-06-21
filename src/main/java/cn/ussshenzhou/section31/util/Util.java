package cn.ussshenzhou.section31.util;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.util.thread.EffectiveSide;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author USS_Shenzhou
 */
public class Util {
    public static final Supplier<Integer> NO_MAX = () -> 0;
    public static final Map<String, Integer> NONE = new HashMap<>();

    public static final boolean IS_SERVER = EffectiveSide.get() == LogicalSide.SERVER || FMLEnvironment.getDist() == Dist.DEDICATED_SERVER;
}
