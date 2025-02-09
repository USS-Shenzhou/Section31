package cn.ussshenzhou.section31;

import cn.ussshenzhou.section31.backend.OshiHelper;
import cn.ussshenzhou.section31.web.Server;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

@Mod(Section31.MODID)
public class Section31 {
    public static final String MODID = "section31";
    private static final Logger LOGGER = LogUtils.getLogger();

    static {
        Server.init();
    }

    public Section31(IEventBus modEventBus, ModContainer modContainer) {
    }
}
