package com.xjus.ocrpdfspring.service.impl;

import com.xjus.ocrpdfspring.model.FileInfoVO;
import com.xjus.ocrpdfspring.service.FileTypeConvertService;
import com.xjus.ocrpdfspring.utils.Image2PdfUtil;
import com.xjus.ocrpdfspring.utils.FileHelpUtil;
import com.xjus.ocrpdfspring.utils.PDF2PDF;
import com.xjus.ocrpdfspring.utils.ofdRender.utils.OfdPdfUtil;
import org.ofdrw.converter.ConvertHelper;
import org.ofdrw.converter.export.OFDExporter;
import org.ofdrw.converter.export.PDFExporterPDFBox;
import org.ofdrw.converter.ofdconverter.PDFConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class FileTypeConvertServiceImpl implements FileTypeConvertService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.pdf-dir}")
    private String pdfDir;

    @Value("${file.ofd-dir}")
    private String ofdDir;


    /**
     * 双层OFD 转 双层PDF
     *
     * @param file ofd文件
     * @return FileInfoVO
     */
    @Override
    public FileInfoVO ofd2pdf(MultipartFile file) throws IOException {
        //定义输入输出路径
        String originalName = file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf(".")); //文件名（不带后缀）
        Path uploadPath = Paths.get(uploadDir, UUID.randomUUID() + "_" + file.getOriginalFilename());
        Path pdfPath = Paths.get(pdfDir, UUID.randomUUID() + "_" + originalName + ".pdf");
        // 暂存上传的ofd文件
        Files.copy(file.getInputStream(), uploadPath);

        // 调用工具类方法，将ofd转换为pdf todo 拆包重写？？？，将字体颜色设置为透明？？？
        ConvertHelper.toPdf(uploadPath, pdfPath);

        // 文件生成并转换成base64字符串
        byte[] fileContent = Files.readAllBytes(pdfPath);
        String resultBase64 = Base64.getEncoder().encodeToString(fileContent);
        FileInfoVO result = new FileInfoVO();
        result.setFile(resultBase64);

        // 删除临时文件
        Files.deleteIfExists(uploadPath);
        Files.deleteIfExists(pdfPath);

        return result;
    }

    /**
     * 将图片转换为 PDF （已完结）
     *
     * @param files 图片文件列表
     * @return FileInfoVO
     */
    @Override
    public FileInfoVO imageToPDF(List<FileInfoVO> files) throws IOException {

        String resultName = UUID.randomUUID() + ".pdf";
        Path resultPath = Paths.get(pdfDir, resultName);

        // 遍历files，将每个对象中的base64字符串转换为图片文件
        List<String> imagePaths = new ArrayList<>();
        for (FileInfoVO file : files) {
            String filePath = uploadDir + "/" + file.getName();
            // 将 base64 字符串转换为图片
            FileHelpUtil.convertBase64ToFile(file.getFile(), filePath);
            imagePaths.add(filePath);
        }

        // 调用工具类方法，将图片转换为 PDF
        String imagePathsStr = String.join(",", imagePaths); // 图片路径用逗号分隔
        Image2PdfUtil.image2PdfByPython(imagePathsStr, pdfDir + "/" + resultName);

        // 文件生成并转换成base64字符串
        byte[] fileContent = Files.readAllBytes(resultPath);
        String resultBase64 = Base64.getEncoder().encodeToString(fileContent);

        FileInfoVO result = new FileInfoVO();
        result.setName(resultName);
        result.setPath(pdfDir + "/" + resultName);
        result.setFile(resultBase64);

        // 删除临时图片文件
        imagePaths.forEach(imagePath -> {
            try {
                Files.deleteIfExists(Paths.get(imagePath));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return result;
    }

    /**
     * 将图片转换为 双层OFD
     *      先将图片转换为双层pdf，再将双层pdf转换为双层ofd
     * @param files 图片文件列表
     * @return FileInfoVO
     * @throws InterruptedException
     */
    @Override
    public FileInfoVO imageToOFD(List<FileInfoVO> files) throws InterruptedException, IOException {

        String resultName = UUID.randomUUID() + ".ofd";
        String resultPathStr = ofdDir + "/" + resultName;
        Path resultPath = Path.of(resultPathStr);

        String tempPdfPathStr = pdfDir + "/" + UUID.randomUUID() + ".pdf";
        Path tempPdfPath = Path.of(tempPdfPathStr);
        List<String> imagePaths = new ArrayList<>();

        // 遍历files，将每个对象中的base64字符串转换为图片文件
        for (FileInfoVO file : files) {
            String filePath = uploadDir + "/" + file.getName();
            // 将 base64 字符串转换为图片
            FileHelpUtil.convertBase64ToFile(file.getFile(), filePath);
            imagePaths.add(filePath);
        }

        // 直接转换ofd，效果为单层的
//        try (ImageConverter converter = new ImageConverter(Path.of(resultPath))) {
//            // 遍历图片路径列表，将每个图片添加到转换器中
//            for (String imagePath : imagePaths) {
//                Path imgPath = Paths.get(imagePath);
//                converter.append(imgPath, 210,297);
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }


        String imagePathsStr = String.join(",", imagePaths); // 图片路径用逗号分隔
        // 调用工具类方法，将图片转换为 PDF
        Image2PdfUtil.image2PdfByPython(imagePathsStr, tempPdfPathStr);

        // 等待文件生成
        int maxAttempts = 50; // 最大尝试次数
        int attempt = 0;
        long delayMs = 500; // 每次等待500毫秒

        while (!Files.exists(tempPdfPath) && attempt < maxAttempts) {
            System.out.println("等待 PDF 文件生成...");
            TimeUnit.MILLISECONDS.sleep(delayMs);
            attempt++;
        }

        // 检查文件是否最终生成
        if (!Files.exists(tempPdfPath)) {
            throw new RuntimeException("PDF 文件未能在指定时间内生成: " + tempPdfPathStr);
        }

        // 调用工具类方法，将pdf转换为ofd。使用官方方法找不到字体
        OfdPdfUtil.convertToOfd(tempPdfPathStr, resultPathStr);
//        try (PDFConverter converter = new PDFConverter(resultPath)) {
//            converter.convert(tempPdfPath);
//        }
//        System.out.println(">> " + resultPath.toAbsolutePath());


        FileInfoVO result = new FileInfoVO();
        result.setName(resultName);
        result.setPath(resultPathStr);

        // 删除临时图片文件
        imagePaths.forEach(imagePath -> {
            try {
                Files.deleteIfExists(Paths.get(imagePath));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return result;
    }

    @Override
    public FileInfoVO pdfToOFD(List<FileInfoVO> files) {
        // 取files中的第一个元素
        FileInfoVO file = files.get(0);
        String filePath = uploadDir + "/" +UUID.randomUUID() + file.getName();
        // 将 base64 字符串转换为pdf
        FileHelpUtil.convertBase64ToFile(file.getFile(), filePath);
        String outputOfdPath = UUID.randomUUID() + "output123.ofd";

        OfdPdfUtil.convertToOfd(filePath, ofdDir + "/" + outputOfdPath);

        FileInfoVO result = new FileInfoVO();
        result.setName(outputOfdPath);
        result.setPath(ofdDir + "/" + outputOfdPath);
        return result;
    }
    @Override
    public FileInfoVO pdf2pdf(List<FileInfoVO> files) throws Exception {
        // 取files中的第一个元素
        FileInfoVO file = files.get(0);
//        String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));//文件名（不带后缀）

        String filePathStr = uploadDir + "/" +UUID.randomUUID() + ".pdf";
        String resultName = UUID.randomUUID() + ".pdf";
        String resultPathStr = ofdDir + "/" + resultName;

        FileHelpUtil.convertBase64ToFile(file.getFile(), filePathStr);

        //单层PDF 转化为 双层PDF
        PDF2PDF.pdf2PdfConverter(filePathStr, resultPathStr);

        FileInfoVO result = new FileInfoVO();
        result.setName(resultName);
        result.setPath(resultPathStr);
//        result.setFile(resultBase64);
        return result;

    }

    /**
     * 将 单层ofd 转换为 双层OFD
     *      先将ofd转换为双层pdf，再将双层pdf转换为双层ofd
     * @param files 图片文件列表
     * @return FileInfoVO
     * @throws InterruptedException
     */
    @Override
    public FileInfoVO ofd2ofd(List<FileInfoVO> files) throws Exception {

        // 取files中的第一个元素
        FileInfoVO file = files.get(0);
//        String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));//文件名（不带后缀）

        String filePathStr = uploadDir + "/" +UUID.randomUUID() + ".ofd";
        String tempFilePathStr = pdfDir + "/" +UUID.randomUUID() + ".pdf";
        String resultName = UUID.randomUUID() + ".ofd";
        String resultPathStr = ofdDir + "/" + resultName;

        FileHelpUtil.convertBase64ToFile(file.getFile(), filePathStr);


        // 单层OFD 转化为 单层PDF
        Path ofdPath = Path.of(filePathStr);
        Path pdfPath = Path.of(tempFilePathStr);
        try (OFDExporter exporter = new PDFExporterPDFBox(ofdPath, pdfPath)) {
            exporter.export();
        }
        System.out.println(tempFilePathStr);
        //单层PDF 转化为 双层PDF
        PDF2PDF.pdf2PdfConverter(tempFilePathStr, tempFilePathStr);

        //双层PDF 转化为 双层OFD
        OfdPdfUtil.convertToOfdByStream(tempFilePathStr, resultPathStr);

        FileInfoVO result = new FileInfoVO();
        result.setName(resultName);
        result.setPath(resultPathStr);
//        result.setFile(resultBase64);
        return result;

    }
}
