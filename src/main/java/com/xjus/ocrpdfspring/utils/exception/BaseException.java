/**
  * 文件名[fileName]：BaseRuntimeException.java
  * @author wuxiusong 
  * @version: v1.0.0.1
  * 日期：2020年4月13日 下午1:56:31
  * Copyright 【北京新桥技术有限公司版权所有】 2020 
  */
package com.xjus.ocrpdfspring.utils.exception;

/**
 * <p>
 * 类描述：业务系统自定义异常基类。
 * </p>
 * 
 * @author [wuxiusong]。
 * @version: v1.0.0.1。
 * @since JDK1.8。
 *        <p>
 *        创建日期：2020年4月13日 下午1:56:31。
 *        </p>
 *        Copyright 【北京新桥技术有限公司版权所有】 2020
 */
public class BaseException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * 异常信息
	 */
	private String message; // 异常信息

	private Throwable cause; // 深层异常（引发问题的原异常）

	public BaseException(String message) {
		this.message = message;
	}

	public BaseException(String message, Throwable cause) {
		this.message = message;
		this.cause = cause;
	}

	public String getMessage() {
		return message;
	}

	public Throwable getCause() {
		return cause;
	}

	protected void setMessage(String message) {
		this.message = message;
	}

	protected void setCause(Throwable cause) {
		this.cause = cause;
	}
}
