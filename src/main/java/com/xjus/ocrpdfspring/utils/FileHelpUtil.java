package com.xjus.ocrpdfspring.utils;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Base64;

public class FileHelpUtil {

    /**
     * 将Base64字符串转换为文件并保存到指定路径
     *
     * @param base64String Base64字符串
     * @param filePath     保存的文件路径
     */
    public static void convertBase64ToFile(String base64String, String filePath) {
        try {
            // 去掉前缀
            if (base64String.contains(",")) {
                base64String = base64String.split(",")[1];
            }
            // 解码
            byte[] imageBytes = Base64.getDecoder().decode(base64String);
            // 写入文件
            try (OutputStream out = new FileOutputStream(filePath)) {
                out.write(imageBytes);
                out.flush();
            }
        } catch (Exception e) {
            System.out.println("转换失败：" + e.getMessage());
        }
    }
}
