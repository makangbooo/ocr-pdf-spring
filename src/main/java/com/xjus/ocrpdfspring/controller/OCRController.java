// src/main/java/com/example/ocr/controller/OCRController.java
package com.xjus.ocrpdfspring.controller;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;

@RestController
@CrossOrigin(origins = "*") // 允许前端访问
public class OCRController {

    @PostMapping("/upload")
    public void uploadImage(@RequestParam("file") MultipartFile file, HttpServletResponse response) {
        try {
            // 读取上传的图片
            BufferedImage image = ImageIO.read(file.getInputStream());

            System.setProperty("jna.library.path", "/opt/homebrew/Cellar/tesseract/5.5.0/lib");
            System.load("/opt/homebrew/Cellar/tesseract/5.5.0/lib/libtesseract.dylib");
            // 使用 Tesseract 进行 OCR 识别
            Tesseract tesseract = new Tesseract();
            tesseract.setLanguage("chi_sim"); // 设置为简体中文
            tesseract.setDatapath("/opt/homebrew/share/tessdata");  // 指定 tessdata 数据目录

            String recognizedText = tesseract.doOCR(image);

            // 创建 PDF 文档
            PDDocument document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            // 写入识别的文字到 PDF
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(50, 700); // 设置文本起始位置

            // 处理多行文本
            for (String line : recognizedText.split("\n")) {
                contentStream.showText(line);
                contentStream.newLineAtOffset(0, -15); // 每行之间的间距
            }

            contentStream.endText();
            contentStream.close();

            // 设置响应类型为 PDF
            response.setContentType("application/pdf");
            document.save(response.getOutputStream());
            document.close();

        } catch (IOException | TesseractException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}