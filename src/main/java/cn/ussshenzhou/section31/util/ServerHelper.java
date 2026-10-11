package cn.ussshenzhou.section31.util;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModList;

import java.io.IOException;
import java.util.Optional;

public class ServerHelper {

    public static byte[] readIconFromJar(Identifier identifier) {
        var modFile = ModList.get().getModFileById(identifier.getNamespace());
        try {
            return modFile.getFile().getContents().readFile(identifier.getPath());
        } catch (IOException e) {
            LogUtils.getLogger().error("Failed to read resource {}.", identifier);
            LogUtils.getLogger().error(e.getMessage());
        }
        return new byte[0];
    }
}
