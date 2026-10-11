package cn.ussshenzhou.section31.util;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.io.IOException;

public class ClientHelper {

    public static byte[] readIcon(Identifier identifier) {
        return Minecraft.getInstance().getResourceManager().getResource(identifier).map(resource -> {
            try (var inputStream = resource.open()) {
                return inputStream.readAllBytes();
            } catch (IOException e) {
                LogUtils.getLogger().error("Failed to read resource {}.", identifier, e);
                return null;
            }
        }).orElse(new byte[0]);
    }
}
