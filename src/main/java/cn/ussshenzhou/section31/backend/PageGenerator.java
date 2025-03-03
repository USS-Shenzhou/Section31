package cn.ussshenzhou.section31.backend;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * @author USS_Shenzhou
 */
public class PageGenerator {
    private static String page;

    static {
        try (InputStream inputStream = PageGenerator.class.getResourceAsStream("/public/section31.html");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
        ) {
            page = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getPage() {
        return page;
    }
}
