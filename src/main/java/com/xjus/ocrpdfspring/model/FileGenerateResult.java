/**
  * 文件名[fileName]：FileGenerateResult.java
  * @author wuxiusong 
  * @version: v1.0.0.1
  * 日期：2020年4月26日 下午11:17:22
  * Copyright 【北京新桥技术有限公司版权所有】 2020 
  */
package com.xjus.ocrpdfspring.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 类描述：生成（导出数据）文件结果。
 * </p>
 * 
 * @author [wuxiusong]。
 * @version: v1.0.0.1。
 * @since JDK1.8。
 *        <p>
 *        创建日期：2020年4月26日 下午11:17:22。
 *        </p>
 *        Copyright 【北京新桥技术有限公司版权所有】 2020
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FileGenerateResult extends BaseResult<String> {
	/**
	 * 文件id，仅在文件需要持久化保存时使用
	 */
	private String fileId; // 文件id，仅在文件需要持久化保存时使用
	
	/**
	 * 文件url
	 */
	private String fileUrl; // 文件url
	
	/**
	 * 文件名称
	 */
	private String fileName; // 文件名称
	
	/**
	 * 文件路径
	 */
	private String filePath; // 文件路径

	public FileGenerateResult(Boolean success, String message, String fileName, String fileUrl, String filePath) {
		super(success, message, null);
		this.fileName = fileName;
		this.fileUrl = fileUrl;
		this.filePath = filePath;
	}

}
