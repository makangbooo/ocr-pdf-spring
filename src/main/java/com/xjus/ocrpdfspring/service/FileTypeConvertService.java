package com.xjus.ocrpdfspring.service;
import com.xjus.ocrpdfspring.model.FileInfoVO;

import java.util.List;

public interface FileTypeConvertService {

    FileInfoVO imageToPDF(List<FileInfoVO> files);

    FileInfoVO imageToOFD(List<FileInfoVO> files) throws InterruptedException;

    FileInfoVO pdfToOFD(List<FileInfoVO> files);

}
