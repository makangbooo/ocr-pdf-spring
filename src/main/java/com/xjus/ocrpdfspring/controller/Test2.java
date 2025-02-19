package com.xjus.ocrpdfspring.controller;

import net.sourceforge.tess4j.ITesseract;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Test2 {
    public static void main(String[] args) throws IOException, TesseractException {
        // 加载图像
        File imageFile = new File("./src/main/resources/实例2.png");
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        // 添加图像层
        PDImageXObject pdImage = PDImageXObject.createFromFileByContent(imageFile, document);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.drawImage(pdImage, 0, 0, pdImage.getWidth(), pdImage.getHeight());
        contentStream.close();

        // Tesseract OCR识别

        System.setProperty("jna.library.path", "/opt/homebrew/Cellar/tesseract/5.5.0/lib");
        System.load("/opt/homebrew/Cellar/tesseract/5.5.0/lib/libtesseract.dylib");

        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("/opt/homebrew/share/tessdata");  // 指定 tessdata 数据目录
        tesseract.setLanguage("chi_sim"); // 设置语言
        tesseract.setTessVariable("user_defined_dpi", "300"); // 设置DPI
        String result = tesseract.doOCR(imageFile);




        // 添加文字层
        contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
        contentStream.setFont(PDType1Font.HELVETICA, 12); // 设置字体和大小
        contentStream.setNonStrokingColor(Color.BLACK);

        // 假设Tesseract返回的坐标和大小
        float tesseractX = 100; // 示例坐标
        float tesseractY = 200;
        float tesseractWidth = 50;
        float tesseractHeight = 20;

        // 转换坐标
        float scale = 72.0f / 300.0f; // 假设图像DPI为300
        float pdfX = tesseractX * scale;
        float pdfY = (pdImage.getHeight() - tesseractY - tesseractHeight) * scale;

        // 绘制文字
        contentStream.beginText();
        contentStream.newLineAtOffset(pdfX, pdfY);
        contentStream.showText("Recognized Text");
        contentStream.endText();

        contentStream.close();

        // 保存PDF
        document.save("output.pdf");
        document.close();
    }
}