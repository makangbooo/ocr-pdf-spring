package com.xjus.ocrpdfspring.controller;

import com.xjus.ocrpdfspring.config.FontConfig;
import com.xjus.ocrpdfspring.model.FileInfoVO;
import com.xjus.ocrpdfspring.service.FileTypeConvertService;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

import java.util.List;


@RestController
@RequestMapping("/FileTypeConvert")
public class FileTypeConvertController {

    @Autowired
    private FileTypeConvertService fileTypeConvertService;

    static {
        FontConfig.configureFontLoader();
    }

    // 接收Blob类型的ofd文件，返回FileInfo对象，属性path为转换后的pdf文件的URL
    @PostMapping("/ofd2pdf")
    public FileInfoVO ofd2pdf(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {
        if (file.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        FileInfoVO result = this.fileTypeConvertService.ofd2pdf(file);
        return result;
    }

    @PostMapping("/imageToPDF")
    public FileInfoVO imageToPDF(@RequestBody List<FileInfoVO> files, HttpServletResponse response) throws IOException {
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

    @PostMapping("/pdf2pdf")
    public FileInfoVO pdf2pdf(@RequestBody List<FileInfoVO> files, HttpServletResponse response) throws Exception {
        if (files == null || files.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            throw new IllegalArgumentException("No files provided");
        }
        FileInfoVO result = this.fileTypeConvertService.pdf2pdf(files);
        return result;
    }


    @PostMapping("/ofd2ofd")
    public FileInfoVO ofd2ofd(@RequestBody List<FileInfoVO> files, HttpServletResponse response) throws Exception {
        if (files == null || files.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            throw new IllegalArgumentException("No files provided");
        }
        FileInfoVO result = this.fileTypeConvertService.ofd2ofd(files);
        return result;
    }
}