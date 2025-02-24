 // src/main/java/com/example/ocr/controller/OCRController.java
package com.xjus.ocrpdfspring.controller;

import com.xjus.ocrpdfspring.entity.FileInfo;
import com.xjus.ocrpdfspring.model.BaseResult;
import com.xjus.ocrpdfspring.utils.ResultGenerator;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @PostMapping("/imageToPDF")
    public FileInfo imageToPDF(@RequestParam("file") List<MultipartFile> files, HttpServletResponse response) {
        try {
            if (files == null || files.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return null;
            }

            // 创建 PDF 文档
            PDDocument document = new PDDocument();

            // 处理多个图片文件
            for (MultipartFile file : files) {
                // 读取上传的图片
                BufferedImage image = ImageIO.read(file.getInputStream());

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

                // 为每个图片创建新页面
                PDPage page = new PDPage(new PDRectangle(image.getWidth(), image.getHeight()));
                document.addPage(page);

                // 将图片嵌入PDF
                PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, file.getBytes(), file.getOriginalFilename());
                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                contentStream.drawImage(pdImage, 0, 0, image.getWidth(), image.getHeight());

                // 设置字体
                PDType0Font font = PDType0Font.load(document, new File("/Library/Fonts/Arial Unicode.ttf"));

                // 写入识别的文字到 PDF
                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.setNonStrokingColor(255, 255, 255, 0); // 设置透明，使文本不可见
                contentStream.setRenderingMode(NEITHER);
                contentStream.newLineAtOffset(10, image.getHeight() - 20);

                // 逐行写入OCR识别的文本
                for (String line : recognizedText.split("\n")) {
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -15);
                }

                contentStream.endText();
                contentStream.close();
            }

            // 生成唯一的文件名（使用第一个文件的名称作为基础）
            String fileName = UUID.randomUUID().toString() + "_" + files.get(0).getOriginalFilename() + ".pdf";
            Path filePath = Paths.get(pdfDir, fileName);
            document.save(filePath.toFile());
            document.close();

            // 返回文件信息（使用第一个文件的信息）
            FileInfo fileInfo = new FileInfo();
            fileInfo.setName(files.get(0).getOriginalFilename());
            fileInfo.setSize(files.stream().mapToLong(MultipartFile::getSize).sum()); // 所有文件总大小
            fileInfo.setPath("http://localhost:8080/" + fileName);

            return fileInfo;

        } catch (IOException | TesseractException e) {
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
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
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
            return ResponseEntity.ok(fileUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to upload file");
        }
    }
}