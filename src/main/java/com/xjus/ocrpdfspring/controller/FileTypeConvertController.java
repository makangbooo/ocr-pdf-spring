package com.xjus.ocrpdfspring.controller;

import com.xjus.ocrpdfspring.model.FileInfoVO;
import com.xjus.ocrpdfspring.utils.Image2PdfUtil;
import com.xjus.ocrpdfspring.utils.ofdRender.utils.OfdPdfUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/FileTypeConvert")
public class FileTypeConvertController {

    @Value("${file.upload-dir}")
    private String uploadDir;
    @Value("${file.pdf-dir}")
    private String pdfDir;

    @Value("${file.ofd-dir}")
    private String ofdDir;

    private final WebClient webClient;
    private final String umiOcrUrl = "http://1.95.55.32:1224"; // 替换为你的 Umi-OCR 服务地址

    private static final String PYTHON_SCRIPT_PATH = "static/image_to_pdf.py"; // Python 脚本路径

    public FileTypeConvertController() {
        this.webClient = WebClient.builder().baseUrl(umiOcrUrl).build();
    }

    // 接收Blob类型的ofd文件，返回FileInfo对象，属性path为转换后的pdf文件的URL
    // todo 转化为pdf后为单层，返回FileInfo
    @PostMapping("/ofd2pdf")
    public void ofd2pdf(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {
        if (file.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 去除文件后缀
        String originalFilename = file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + "_" + originalFilename + ".pdf";
        Path uploadPath = Paths.get(uploadDir, fileName);
        Path pdfPath = Paths.get(pdfDir, fileName);

        // 保存上传的文件
        Files.copy(file.getInputStream(), uploadPath);

        // 转换为 PDF
        Path ofdSrc = Paths.get(uploadPath.toString());
        Path pdfdst = Paths.get(pdfPath.toString());
        org.ofdrw.converter.ConvertHelper.toPdf(ofdSrc, pdfdst);







//
//
//
//
//        OfdPdfUtil.ofdToPdf(uploadPath.toString(), pdfPath.toString());
        // 设置响应头
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\""); // inline 让浏览器直接显示

        // 将 PDF 文件写入响应流
        Files.copy(pdfPath, response.getOutputStream());
        response.getOutputStream().flush();
    }

    @PostMapping("/imageToPDF")
    public FileInfoVO imageToPDF(@RequestBody List<FileInfoVO> files, HttpServletResponse response) {
        if (files == null || files.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            throw new IllegalArgumentException("No files provided");
        }
        String outputPdfPath = UUID.randomUUID() + "output123.pdf";
        List<String> imagePaths = new ArrayList<>();

        // 遍历files，将每个对象中的base64字符串转换为图片文件
        for (FileInfoVO file : files) {
            String filePath = uploadDir + "/" + file.getName();
            // 将 base64 字符串转换为图片
            Image2PdfUtil.convertBase64ToImage(file.getFile(), filePath);
            imagePaths.add(filePath.toString());
        }
        String imagePathsStr = String.join(",", imagePaths); // 图片路径用逗号分隔
        // 调用工具类方法，将图片转换为 PDF
        Image2PdfUtil.image2PdfByPython(imagePathsStr, pdfDir + "/" + outputPdfPath);

        FileInfoVO result = new FileInfoVO();
        result.setName(outputPdfPath);
        result.setPath(pdfDir + "/" + outputPdfPath);
        return result;
    }

    @PostMapping("/imageToOFD")
    public FileInfoVO imageToOFD(@RequestBody List<FileInfoVO> files, HttpServletResponse response) throws IOException, InterruptedException {
        if (files == null || files.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            throw new IllegalArgumentException("No files provided");
        }
        String outputPdfPath = pdfDir + "/" + UUID.randomUUID() + "output123.pdf";
        String outputOfdPath = UUID.randomUUID() + "output123.ofd";
        List<String> imagePaths = new ArrayList<>();

        // 遍历files，将每个对象中的base64字符串转换为图片文件
        for (FileInfoVO file : files) {
            String filePath = uploadDir + "/" + file.getName();
            // 将 base64 字符串转换为图片
            Image2PdfUtil.convertBase64ToImage(file.getFile(), filePath);
            imagePaths.add(filePath.toString());
        }
        String imagePathsStr = String.join(",", imagePaths); // 图片路径用逗号分隔
        // 调用工具类方法，将图片转换为 PDF
        Image2PdfUtil.image2PdfByPython(imagePathsStr, outputPdfPath);

        // 等待文件生成
        Path pdfPath = Paths.get(outputPdfPath);
        int maxAttempts = 50; // 最大尝试次数
        int attempt = 0;
        long delayMs = 500; // 每次等待500毫秒

        while (!Files.exists(Path.of(outputPdfPath)) && attempt < maxAttempts) {
            System.out.println("等待 PDF 文件生成...");
            TimeUnit.MILLISECONDS.sleep(delayMs);
            attempt++;
        }

        // 检查文件是否最终生成
        if (!Files.exists(Path.of(outputPdfPath))) {
            throw new RuntimeException("PDF 文件未能在指定时间内生成: " + outputPdfPath);
        }
        OfdPdfUtil.convertToOfd(outputPdfPath, ofdDir + "/" + outputOfdPath);
        FileInfoVO result = new FileInfoVO();
        result.setName(outputOfdPath);
        result.setPath(ofdDir + "/" + outputOfdPath);
        return result;

    }

}