package com.xjus.ocrpdfspring.controller;

import com.xjus.ocrpdfspring.entity.FileInfo;

import com.xjus.ocrpdfspring.model.FileInfoVO;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


@RestController
@RequestMapping("/OCRToPDF")
public class OCRController {

    @Value("${file.upload-dir}")
    private String uploadDir;
    @Value("${file.pdf-dir}")
    private String pdfDir;
    @Value("${file.sever-name}")
    private String severName;

    private static final String PYTHON_SCRIPT_PATH = "static/image_to_pdf.py"; // Python 脚本路径

    @PostMapping("/imageToPDF")
    public FileInfo imageToPDF(@RequestBody List<FileInfoVO> files, HttpServletResponse response) {
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
            FileInfoVO fileInfo = new FileInfoVO();
            fileInfo.setName(files.get(0).getName() + "_converted.pdf");
            fileInfo.setSize(Files.size(filePath));
            fileInfo.setPath(severName + outputPdfName);
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

//            System.setProperty("jna.library.path", "/opt/homebrew/Cellar/tesseract/5.5.0/lib");
//            System.load("/opt/homebrew/Cellar/tesseract/5.5.0/lib/libtesseract.dylib");
            System.setProperty("jna.library.path", "/usr/lib/x86_64-linux-gnu"); // 示例路径
            System.load("/usr/lib/x86_64-linux-gnu/libtesseract.so.4"); // 使用找到的具体文件

            // 初始化Tesseract OCR
            Tesseract tesseract = new Tesseract();

            tesseract.setLanguage("chi_sim"); // 设置为简体中文
//            tesseract.setDatapath("/opt/homebrew/share/tessdata");  // 指定 tessdata 数据目录
            tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata"); // 已确认的 tessdata 路径
            tesseract.setOcrEngineMode(ITessAPI.TessOcrEngineMode.OEM_LSTM_ONLY); // 使用LSTM引擎
            tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_SINGLE_BLOCK);

            // 识别图片中的文字


            return tesseract.doOCR(image);

        } catch (IOException | TesseractException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            return ResultGenerator.fail("Failed to OCR image");
            return "Failed to OCR image";
        }
    }

    @PostMapping("/uploadImage")
    public ResponseEntity<FileInfoVO> uploadImage(@RequestParam("file") MultipartFile file) {
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
            String fileUrl = severName + fileName;
            FileInfoVO fileInfo = new FileInfoVO();
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