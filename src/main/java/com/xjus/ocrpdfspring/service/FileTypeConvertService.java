package com.xjus.ocrpdfspring.service;
import com.xjus.ocrpdfspring.model.FileInfoVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileTypeConvertService {

    FileInfoVO imageToPDF(List<FileInfoVO> files) throws IOException;

    FileInfoVO imageToOFD(List<FileInfoVO> files) throws InterruptedException;

    FileInfoVO pdfToOFD(List<FileInfoVO> files);

    FileInfoVO ofd2pdf(MultipartFile file) throws IOException;

}
