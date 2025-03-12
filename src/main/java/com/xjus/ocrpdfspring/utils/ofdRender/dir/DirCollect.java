package com.xjus.ocrpdfspring.utils.ofdRender.dir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * 创建目录
 *
 * @author gblfy
 * @since 2021-12-06
 */
public interface DirCollect {
    /**
     * 创建目录并复制文件
     *
     * @param base 基础路径
     * @return 创建的目录路径
     * @throws IOException IO异常
     */
    Path collect(String base) throws IOException;

    /**
     * 创建虚拟目录并推入文件数据
     *
     * @Author: Lianglx
     *
     * @param base
     * @param virtualFileMap
     * @return
     * @throws IOException
     */
    Map<String, byte[]> collect(String base, Map<String, byte[]> virtualFileMap) throws IOException;
}
