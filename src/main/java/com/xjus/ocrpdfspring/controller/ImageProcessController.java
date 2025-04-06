package com.xjus.ocrpdfspring.controller;

import com.xjus.ocrpdfspring.model.FileInfoVO;
import com.xjus.ocrpdfspring.utils.pcoressUtils.ImageProcessorApp;

import jakarta.servlet.http.HttpServletResponse;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.opencv.core.Core;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/ImageProcess")
public class ImageProcessController {

    //实例化
    ImageProcessorApp app = new ImageProcessorApp();
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.pdf-dir}")
    private String pdfDir;

    @Value("${file.ofd-dir}")
    private String ofdDir;
    static {
        Loader.load(opencv_java.class);
    }

    //透视校正 自动
    @PostMapping("/Perspective")
    public FileInfoVO perspective(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {
        if (file.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        //定义输入输出路径
        String uploadFullName =  UUID.randomUUID() + "_" + file.getOriginalFilename();
        String uploadOnlyName = uploadFullName.substring(0, uploadFullName.lastIndexOf(".")); //文件名（不带后缀）
        String uploadOnlyType = uploadFullName.substring(uploadFullName.lastIndexOf(".") + 1); //文件后缀

        String uploadPath = uploadDir + "/" + uploadFullName;

        String resultPath = uploadDir + "/" + uploadOnlyName + "_perspective." + uploadOnlyType;
        // 暂存上传的ofd文件
        Files.copy(file.getInputStream(), Path.of(uploadPath));

        app.processSingleStep(uploadPath, "perspective", uploadDir, true, null);

        // 文件生成并转换成base64字符串
        byte[] fileContent = Files.readAllBytes(Path.of(resultPath));
        String resultBase64 = Base64.getEncoder().encodeToString(fileContent);
        // 为base64字符串添加前缀
        String prefix = "data:image/" + uploadOnlyType + ";base64,";
        FileInfoVO result = new FileInfoVO();
        result.setFile(prefix + resultBase64);

        // 删除临时文件
        Files.deleteIfExists(Path.of(uploadPath));

        return result;

    }

    //降噪 手动
    @PostMapping("/Noise")
    public FileInfoVO noise(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {
        if (file.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        //定义输入输出路径
        String uploadFullName =  UUID.randomUUID() + "_" + file.getOriginalFilename();
        String uploadOnlyName = uploadFullName.substring(0, uploadFullName.lastIndexOf(".")); //文件名（不带后缀）
        String uploadOnlyType = uploadFullName.substring(uploadFullName.lastIndexOf(".") + 1); //文件后缀

        String uploadPath = uploadDir + "/" + uploadFullName;

        String resultPath = uploadDir + "/" + uploadOnlyName + "_noise." + uploadOnlyType;
        // 暂存上传的ofd文件
        Files.copy(file.getInputStream(), Path.of(uploadPath));

        Map<String, Object> noise_params = new HashMap<>();
        noise_params.put("method", "bilateral");
        noise_params.put("bilateral_d", 15);
        noise_params.put("bilateral_sigma_color", 80);
        app.processSingleStep(uploadPath, "noise", uploadDir, false, noise_params);

        // 文件生成并转换成base64字符串
        byte[] fileContent = Files.readAllBytes(Path.of(resultPath));
        String resultBase64 = Base64.getEncoder().encodeToString(fileContent);
        // 为base64字符串添加前缀
        String prefix = "data:image/" + uploadOnlyType + ";base64,";
        FileInfoVO result = new FileInfoVO();
        result.setFile(prefix + resultBase64);

        // 删除临时文件
        Files.deleteIfExists(Path.of(uploadPath));

        return result;
    }

    //白平衡 自动
    @PostMapping("/Balance")
    public FileInfoVO balance(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {
        if (file.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        //定义输入输出路径
        String uploadFullName =  UUID.randomUUID() + "_" + file.getOriginalFilename();
        String uploadOnlyName = uploadFullName.substring(0, uploadFullName.lastIndexOf(".")); //文件名（不带后缀）
        String uploadOnlyType = uploadFullName.substring(uploadFullName.lastIndexOf(".") + 1); //文件后缀

        String uploadPath = uploadDir + "/" + uploadFullName;

        String resultPath = uploadDir + "/" + uploadOnlyName + "_white_balance." + uploadOnlyType;
        // 暂存上传的ofd文件
        Files.copy(file.getInputStream(), Path.of(uploadPath));

        Map<String, Object> wb_params = new HashMap<>();
        wb_params.put("method", "grayworld");
        app.processSingleStep(uploadPath, "white_balance", uploadDir, true, wb_params);

        // 文件生成并转换成base64字符串
        byte[] fileContent = Files.readAllBytes(Path.of(resultPath));
        String resultBase64 = Base64.getEncoder().encodeToString(fileContent);
        // 为base64字符串添加前缀
        String prefix = "data:image/" + uploadOnlyType + ";base64,";
        FileInfoVO result = new FileInfoVO();
        result.setFile(prefix + resultBase64);

        // 删除临时文件
        Files.deleteIfExists(Path.of(uploadPath));

        return result;
    }

    //边缘抹白 手动
    @PostMapping("/Whitening")
    public FileInfoVO whitening(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {
        if (file.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        //定义输入输出路径
        String uploadFullName =  UUID.randomUUID() + "_" + file.getOriginalFilename();
        String uploadOnlyName = uploadFullName.substring(0, uploadFullName.lastIndexOf(".")); //文件名（不带后缀）
        String uploadOnlyType = uploadFullName.substring(uploadFullName.lastIndexOf(".") + 1); //文件后缀

        String uploadPath = uploadDir + "/" + uploadFullName;

        String resultPath = uploadDir + "/" + uploadOnlyName + "_edge_whitening." + uploadOnlyType;
        // 暂存上传的ofd文件
        Files.copy(file.getInputStream(), Path.of(uploadPath));


        Map<String, Object> whitening_params = new HashMap<>();
        whitening_params.put("border_size", 30);
        whitening_params.put("feather_size", 15);
        app.processSingleStep(uploadPath, "edge_whitening", uploadDir, false, whitening_params);

        // 文件生成并转换成base64字符串
        byte[] fileContent = Files.readAllBytes(Path.of(resultPath));
        String resultBase64 = Base64.getEncoder().encodeToString(fileContent);
        // 为base64字符串添加前缀
        String prefix = "data:image/" + uploadOnlyType + ";base64,";
        FileInfoVO result = new FileInfoVO();
        result.setFile(prefix + resultBase64);

        // 删除临时文件
        Files.deleteIfExists(Path.of(uploadPath));

        return result;
    }

    //二值化 自动
    @PostMapping("/Binary")
    public FileInfoVO binary(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {
        if (file.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        //定义输入输出路径
        String uploadFullName =  UUID.randomUUID() + "_" + file.getOriginalFilename();
        String uploadOnlyName = uploadFullName.substring(0, uploadFullName.lastIndexOf(".")); //文件名（不带后缀）
        String uploadOnlyType = uploadFullName.substring(uploadFullName.lastIndexOf(".") + 1); //文件后缀

        String uploadPath = uploadDir + "/" + uploadFullName;

        String resultPath = uploadDir + "/" + uploadOnlyName + "_binarize." + uploadOnlyType;
        // 暂存上传的ofd文件
        Files.copy(file.getInputStream(), Path.of(uploadPath));


        app.processSingleStep(uploadPath, "binarize", uploadDir, true, null);


        // 文件生成并转换成base64字符串
        byte[] fileContent = Files.readAllBytes(Path.of(resultPath));
        String resultBase64 = Base64.getEncoder().encodeToString(fileContent);
        // 为base64字符串添加前缀
        String prefix = "data:image/" + uploadOnlyType + ";base64,";
        FileInfoVO result = new FileInfoVO();
        result.setFile(prefix + resultBase64);

        // 删除临时文件
        Files.deleteIfExists(Path.of(uploadPath));

        return result;

    }
}
