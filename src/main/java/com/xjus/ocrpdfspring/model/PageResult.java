/**
  * 文件名[fileName]：PageResult.java
  * @author wuxiusong 
  * @version: v1.0.0.1
  * 日期：2020年4月8日 下午4:24:35
  * Copyright 【北京新桥技术有限公司版权所有】 2020 
  */
package com.xjus.ocrpdfspring.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
  *<p>类描述：分页查询结果。</p>
  * @author [wuxiusong]。
  * @version: v1.0.0.1。
 * @param <T>
  * @since JDK1.8。
  *<p>创建日期：2020年4月8日 下午4:24:35。</p>
  * Copyright 【北京新桥技术有限公司版权所有】 2020 
  */
@Data
@EqualsAndHashCode(callSuper = false)
public class PageResult<T> extends BaseResult<List<T>> {
	@JsonProperty("total")
	/**
	 * 总记录数
	 */
	private Long total; //总记录数 @JsonProperty("total")
	
	@JsonProperty("rows")
	/**
	 * 当前页数据
	 */
	private List<T> rows; //当前页数据 @JsonProperty("rows")

	public PageResult(Boolean success, String message, Long total , List<T> data) {
		super(success,message,null);
		this.total = total;	
		this.rows= data;
	}

}
