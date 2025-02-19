package com.xjus.ocrpdfspring.controller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.util.ImageHelper;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class Test {

    public static void main(String[] args) {
        try {
            // 1. 读取图片
            File imageFile = new File("./src/main/resources/中文实例.png");
            BufferedImage image = ImageIO.read(imageFile);
            int width = image.getWidth();
            int height = image.getHeight();

            // 2. 创建PDF文档
            PDDocument doc = new PDDocument();
            PDPage page = new PDPage(new PDRectangle(width, height));
            doc.addPage(page);

            // 3. 将图片添加为PDF背景层
            PDImageXObject pdImage = PDImageXObject.createFromFile("./src/main/resources/中文实例.png", doc);
            try (PDPageContentStream imgStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true)) {
                imgStream.drawImage(pdImage, 0, 0, width, height);
            }

            // 4. OCR识别文本和位置
            System.setProperty("jna.library.path", "/opt/homebrew/Cellar/tesseract/5.5.0/lib");
            System.load("/opt/homebrew/Cellar/tesseract/5.5.0/lib/libtesseract.dylib");
            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath("/opt/homebrew/share/tessdata");  // 指定 tessdata 数据目录
            tesseract.setLanguage("chi_sim"); // 设置语言

            // 提高识别精度：转换为灰度图
            BufferedImage grayImage = ImageHelper.convertImageToGrayscale(image);
            grayImage = ImageHelper.getScaledInstance(grayImage, grayImage.getWidth() * 2, grayImage.getHeight() * 2);

            // 获取文本及其边界框
            java.util.List<net.sourceforge.tess4j.Word> words = tesseract.getWords(grayImage, net.sourceforge.tess4j.ITessAPI.TessPageIteratorLevel.RIL_WORD);

            // 5. 添加透明文本层
            PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
            graphicsState.setNonStrokingAlphaConstant(0f); // 设置文本透明
//            PDType1Font font = PDType1Font.HELVETICA; // 使用Helvetica字体
            PDType0Font font = PDType0Font.load(doc, new File("/Library/Fonts/Arial Unicode.ttf"));


            try (PDPageContentStream textStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true)) {
                textStream.setGraphicsStateParameters(graphicsState);
                textStream.setFont(font, 12); // 字体大小需根据实际调整

                for (net.sourceforge.tess4j.Word word : words) {
                    java.awt.Rectangle rect = word.getBoundingBox();
                    // 坐标转换：图片左上角原点转PDF左下角原点
                    float pdfY = height - rect.y - rect.height;

                    textStream.beginText();
                    textStream.newLineAtOffset(rect.x, pdfY);
                    textStream.showText(word.getText());
                    textStream.endText();
                }
            }

            // 6. 保存PDF
            doc.save("output.pdf");
            doc.close();
            System.out.println("双层PDF生成成功！");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}