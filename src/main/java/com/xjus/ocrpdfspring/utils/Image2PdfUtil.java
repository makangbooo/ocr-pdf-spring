package com.xjus.ocrpdfspring.utils;


import java.io.*;
import java.util.Base64;

public class Image2PdfUtil {
    private static final String PYTHON_SCRIPT_PATH = "static/image_to_pdf_old.py"; // Python 脚本路径

    public static void image2PdfByPython(String imagePathsStr, String outputPdfPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder("python3", PYTHON_SCRIPT_PATH, imagePathsStr, outputPdfPath);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Python script execution failed with exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("Python 脚本执行失败：" + e.getMessage());
        }
    }

}
