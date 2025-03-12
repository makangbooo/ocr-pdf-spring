package com.xjus.ocrpdfspring.utils.ofdRender.dir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 页面容器
 *
 * @author gblfy
 * @since 2021-12-06
 */
public class PagesDir implements com.xjus.ocrpdfspring.utils.ofdRender.dir.DirCollect {

    /**
     * 容器
     */
    private List<PageDir> container;

    public PagesDir() {
        this.container = new ArrayList<>(5);
    }

    /**
     * 增加页面
     *
     * @param page 页面容器
     * @return this
     */
    public PagesDir add(PageDir page) {
        if (container == null) {
            container = new ArrayList<>(5);
        }
        this.container.add(page);
        return this;
    }

    /**
     * 获取指定页面容器
     *
     * @param index 页码（从1开始）
     * @return this
     */
    public PageDir get(Integer index) {
        if (container == null) {
            return null;
        }
        for (PageDir page : container) {
            if (page.getIndex().equals(index)) {
                return page;
            }
        }
        return null;
    }

    /**
     * 创建目录并复制文件
     *
     * @param base 基础路径
     * @return 创建的目录路径
     * @throws IOException IO异常
     */
    @Override
    public Path collect(String base) throws IOException {
        if (container == null || container.isEmpty()) {
            throw new IllegalArgumentException("缺少页面");
        }
        Path path = Paths.get(base, "Pages");
        path = Files.createDirectories(path);
        String dir = path.toAbsolutePath().toString();
        for (PageDir p :container) {
            p.collect(dir);
        }
        return path;
    }

    @Override
    public Map<String, byte[]> collect(String base, Map<String, byte[]> virtualFileMap) throws IOException {
        if (container == null || container.isEmpty()) {
            throw new IllegalArgumentException("缺少页面");
        }
        Path path = Paths.get(base, "Pages");
        String dir = path.toString();
        for (PageDir p :container) {
            p.collect(dir, virtualFileMap);
        }
        return virtualFileMap;
    }
}
