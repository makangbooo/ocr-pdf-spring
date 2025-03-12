/**
  * 文件名[fileName]：FileInfoVO.java
  * @author wuxiusong 
  * @version: v1.0.0.1
  * 日期：2020年5月29日 上午10:17:44
  * Copyright 【北京新桥技术有限公司版权所有】 2020 
  */
package com.xjus.ocrpdfspring.model;

import com.xjus.ocrpdfspring.entity.FileInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.multipart.MultipartFile;

/**
  *<p>类描述: 文件信息。</p>
  * @author [wuxiusong]。
  * @version: v1.0.0.1。
  * @since JDK1.8。
  *<p>创建日期：2020年5月29日 上午10:17:44。</p>
  * Copyright 【北京新桥技术有限公司版权所有】 2020 
  */
@Data
@EqualsAndHashCode(callSuper = false)
public class FileInfoVO extends FileInfo {
	private static final long serialVersionUID = 1L;

	private String file; // Base64 编码的图片内容
	
	private Integer businessType;
}
