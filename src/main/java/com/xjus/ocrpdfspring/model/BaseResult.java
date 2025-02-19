/**
  * 文件名[fileName]：Result.java
  * @author wuxiusong 
  * @version: v1.0.0.1
  * 日期：2020年4月3日 下午12:31:10
  * Copyright 【北京新桥技术有限公司版权所有】 2020 
  */
package com.xjus.ocrpdfspring.model;

import lombok.Data;

/**
 * <p>
 * 类描述：通用返回结果。
 * </p>
 * 
 * @author [wuxiusong]。
 * @version: v1.0.0.1。
 * @since JDK1.8。
 *        <p>
 *        创建日期：2020年4月3日 下午12:31:10。
 *        </p>
 *        Copyright 【北京新桥技术有限公司版权所有】 2020
 */
@Data
public class BaseResult<T> {
	/**
	 * 是否成功
	 */
	private Boolean success; // 是否成功
	
	/**
	 * 提示信息
	 */
	private String message; // 提示信息
	
	/**
	 * 错误码
	 */
	private String errCode; // 错误码
	
	/**
	 * 返回数据实体
	 */
	private T data; // 返回数据实体

	public BaseResult() {
	}

	public BaseResult(Boolean success, String message, T data) {
		this.success = success;
		this.message = message;
		this.data = data;
	}
}
