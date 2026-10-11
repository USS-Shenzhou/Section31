package cn.ussshenzhou.section31.backend.metric;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @author USS_Shenzhou
 */
public class Tab {
    protected final String name;
    protected final String icon;
    protected final LinkedHashMap<String, ArrayList<BasicMetric>> groups = new LinkedHashMap<>();

    /**
     * @param icon Inner markup of a 24x24 SVG, drawn with stroke only. Empty string to use the first letter of name.
     */
    public Tab(String name, String icon) {
        this.name = name;
        this.icon = icon;
    }

    public String name() {
        return name;
    }

    public String icon() {
        return icon;
    }

    public LinkedHashMap<String, ArrayList<BasicMetric>> groups() {
        return groups;
    }

    public ArrayList<BasicMetric> group(String groupName) {
        return groups.computeIfAbsent(groupName, newGroupName -> new ArrayList<>());
    }
}
