package com.xjus.ocrpdfspring.controller;

import com.xjus.ocrpdfspring.config.FontConfig;
import com.xjus.ocrpdfspring.model.FileInfoVO;
import com.xjus.ocrpdfspring.service.FileTypeConvertService;
import jakarta.servlet.http.HttpServletResponse;

import org.ofdrw.converter.ConvertHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Base64;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/FileTypeConvert")
public class FileTypeConvertController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.pdf-dir}")
    private String pdfDir;

    @Value("${file.ofd-dir}")
    private String ofdDir;

    @Autowired
    private FileTypeConvertService fileTypeConvertService;

    static {
        FontConfig.configureFontLoader();
    }

    // 接收Blob类型的ofd文件，返回FileInfo对象，属性path为转换后的pdf文件的URL
    // todo 转化为pdf后为单层，返回FileInfo
    @PostMapping("/ofd2pdf")
    public FileInfoVO ofd2pdf(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {
        if (file.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        // 去除文件后缀
        String originalFilename = file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + "_" + originalFilename + ".pdf";
        Path uploadPath = Paths.get(uploadDir, file.getOriginalFilename());
        Path pdfPath = Paths.get(pdfDir, fileName);

        // 保存上传的文件
        Files.copy(file.getInputStream(), uploadPath);

        // todo 测试ofd2pdf方法和toPdf的
        ConvertHelper.ofd2pdf(uploadPath, pdfPath);

        byte[] fileContent = Files.readAllBytes(pdfPath);
        String resultBase64 = Base64.getEncoder().encodeToString(fileContent);

        FileInfoVO result = new FileInfoVO();
        result.setFile(resultBase64);

        return result;
    }

    @PostMapping("/imageToPDF")
    public FileInfoVO imageToPDF(@RequestBody List<FileInfoVO> files, HttpServletResponse response) {
        if (files == null || files.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            throw new IllegalArgumentException("No files provided");
        }
        FileInfoVO result = this.fileTypeConvertService.imageToPDF(files);
        return result;
    }

    @PostMapping("/imageToOFD")
    public FileInfoVO imageToOFD(@RequestBody List<FileInfoVO> files, HttpServletResponse response) throws IOException, InterruptedException {
        if (files == null || files.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            throw new IllegalArgumentException("No files provided");
        }
        FileInfoVO result = this.fileTypeConvertService.imageToOFD(files);
        return result;
    }

    @PostMapping("/pdfToOFD")
    public FileInfoVO pdfToOFD(@RequestBody List<FileInfoVO> files, HttpServletResponse response) throws IOException, InterruptedException {
        if (files == null || files.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            throw new IllegalArgumentException("No files provided");
        }
        FileInfoVO result = this.fileTypeConvertService.pdfToOFD(files);
        return result;
    }

}