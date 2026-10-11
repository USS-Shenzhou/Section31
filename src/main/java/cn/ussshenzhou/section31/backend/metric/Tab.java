package cn.ussshenzhou.section31.backend.metric;

import cn.ussshenzhou.section31.Section31;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @author USS_Shenzhou
 */
public class Tab {
    public static final Identifier DEFAULT_ICON = Identifier.fromNamespaceAndPath(Section31.MODID, "textures/structure_void.png");
    protected final String name;
    protected Identifier icon;
    protected final LinkedHashMap<String, ArrayList<BasicMetric>> groups = new LinkedHashMap<>();

    public Tab(String name, @Nullable Identifier icon) {
        this.name = name;
        this.icon = icon == null ? DEFAULT_ICON : icon;
    }

    public String name() {
        return name;
    }

    public Identifier icon() {
        return icon;
    }

    public LinkedHashMap<String, ArrayList<BasicMetric>> groups() {
        return groups;
    }

    public ArrayList<BasicMetric> group(String groupName) {
        return groups.computeIfAbsent(groupName, newGroupName -> new ArrayList<>());
    }
}
