package cn.ussshenzhou.section31;

import cn.ussshenzhou.section31.web.Server;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * @author USS_Shenzhou
 */
@Mod(Section31.MODID)
public class Section31 {
    public static final String MODID = "section31";
    private static final Logger LOGGER = LogUtils.getLogger();

    static {
        Server.init();
        LogUtils.getLogger().info("Welcome to Section 31!");
    }

    public Section31(IEventBus modEventBus, ModContainer modContainer) {
    }
}
