 // src/main/java/com/example/ocr/controller/OCRController.java
package com.xjus.ocrpdfspring.controller;

import com.xjus.ocrpdfspring.entity.FileInfo;

import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.apache.pdfbox.pdmodel.graphics.state.RenderingMode.NEITHER;

@RestController
@RequestMapping("/OCRToPDF")
public class OCRController {

    @Value("${file.upload-dir}")
    private String uploadDir;
    @Value("${file.pdf-dir}")
    private String pdfDir;

    private static final String PYTHON_SCRIPT_PATH = "static/image_to_pdf.py"; // Python 脚本路径

    @PostMapping("/imageToPDF")
    public FileInfo imageToPDF(@RequestBody List<FileInfo> files, HttpServletResponse response) {
        try {
            if (files == null || files.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return null;
            }

            // 获取上传的图片文件
            List<String> imagePaths = new ArrayList<>();
            for (FileInfo file : files) {
                Path filePath = Paths.get(uploadDir, file.getName());
                imagePaths.add(filePath.toString());
            }

            // 生成输出 PDF 文件名
            String outputPdfName = UUID.randomUUID() + "_output.pdf";
            Path filePath = Paths.get(pdfDir, outputPdfName);
//            Path filePath = Paths.get(pdfDir);


            // 调用 Python 脚本
            String imagePathsStr = String.join(",", imagePaths); // 图片路径用逗号分隔
            ProcessBuilder pb = new ProcessBuilder(
                    "python3", PYTHON_SCRIPT_PATH, imagePathsStr, filePath.toString()
            );
            pb.redirectErrorStream(true); // 合并标准输出和错误输出
            Process process = pb.start();

            // 读取 Python 脚本输出
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line); // 打印 Python 输出，便于调试
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Python script execution failed with exit code: " + exitCode);
            }

            // 返回文件信息
            FileInfo fileInfo = new FileInfo();
            fileInfo.setName(files.get(0).getName() + "_converted.pdf");
            fileInfo.setSize(Files.size(filePath));
            fileInfo.setPath("http://localhost:8080/" + outputPdfName);
            return fileInfo;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }
    }
    @PostMapping("/ocrImage")

    public String ocrImage(@RequestParam("file") MultipartFile file, HttpServletResponse response) {
        try {
            // 读取上传的图片
            BufferedImage image = ImageIO.read(file.getInputStream());

            // 将图片保存到临时文件夹（./upload文件夹下）、
            // 生成唯一的文件名
            String fileName = UUID.randomUUID().toString() + "_111111_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);
            Files.copy(file.getInputStream(), filePath);

            System.setProperty("jna.library.path", "/opt/homebrew/Cellar/tesseract/5.5.0/lib");
            System.load("/opt/homebrew/Cellar/tesseract/5.5.0/lib/libtesseract.dylib");

            // 初始化Tesseract OCR
            Tesseract tesseract = new Tesseract();

            tesseract.setLanguage("chi_sim"); // 设置为简体中文
            tesseract.setDatapath("/opt/homebrew/share/tessdata");  // 指定 tessdata 数据目录
            tesseract.setOcrEngineMode(ITessAPI.TessOcrEngineMode.OEM_LSTM_ONLY); // 使用LSTM引擎
            tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_AUTO); // 自动页面分割

            // 识别图片中的文字
            String recognizedText = tesseract.doOCR(image);


            return recognizedText;

        } catch (IOException | TesseractException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            return ResultGenerator.fail("Failed to OCR image");
            return null;
        }
    }


    @PostMapping("/uploadImage")
    public ResponseEntity<FileInfo> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // 创建上传目录（如果不存在）
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成唯一的文件名
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);
            // 保存文件
            Files.copy(file.getInputStream(), filePath);

            // 返回文件的URL地址
            String fileUrl = "http://localhost:8080/" + fileName;
            FileInfo fileInfo = new FileInfo();
            fileInfo.setName(fileName);
            fileInfo.setSize(file.getSize());
            fileInfo.setPath(fileUrl);
            return ResponseEntity.ok(fileInfo);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
        }
    }
}