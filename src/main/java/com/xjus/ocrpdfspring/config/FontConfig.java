package com.xjus.ocrpdfspring.config;
import org.ofdrw.converter.FontLoader;

public class FontConfig {
    public static void configureFontLoader() {
        // 设置字体加载路径
        FontLoader.getInstance().addSystemFontMapping("楷体", "./Fonts/楷体.ttf");
        FontLoader.getInstance().addSystemFontMapping("宋体", "./Fonts/宋体.ttf");
        FontLoader.getInstance().addSystemFontMapping("Droid Sans Fallback Regular", "./Fonts/DroidSansFallback__.ttf");
    }
}