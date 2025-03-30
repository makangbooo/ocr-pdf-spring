package com.xjus.ocrpdfspring.service;
import com.xjus.ocrpdfspring.model.FileInfoVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileTypeConvertService {

    FileInfoVO imageToPDF(List<FileInfoVO> files) throws IOException;

    FileInfoVO imageToOFD(List<FileInfoVO> files) throws InterruptedException, IOException;

    FileInfoVO pdfToOFD(List<FileInfoVO> files);

    FileInfoVO pdf2pdf(List<FileInfoVO> files) throws Exception;

    FileInfoVO ofd2pdf(MultipartFile file) throws IOException;

    FileInfoVO ofd2ofd(List<FileInfoVO> files) throws Exception;

}
