/**
  * 文件名[fileName]：ResultGenerator.java
  * @author wuxiusong 
  * @version: v1.0.0.1
  * 日期：2020年4月3日 下午12:44:41
  * Copyright 【北京新桥技术有限公司版权所有】 2020 
  */
package com.xjus.ocrpdfspring.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xjus.ocrpdfspring.model.BaseResult;
import com.xjus.ocrpdfspring.model.FileGenerateResult;
import com.xjus.ocrpdfspring.model.PageResult;

import java.util.List;

/**
 * <p>
 * 类描述：通用返回结果生成工具。
 * </p>
 * 
 * @author [wuxiusong]。
 * @version: v1.0.0.1。
 * @since JDK1.8。
 *        <p>
 *        创建日期：2020年4月3日 下午12:44:41。
 *        </p>
 *        Copyright 【北京新桥技术有限公司版权所有】 2020
 */
public abstract class ResultGenerator {
	/**
	 * 
	 * <p>
	 * 功能描述：返回失败结果并加入错误信息。
	 * </p>
	 * 
	 * @param message 错误信息
	 * @return
	 * @since JDK1.8。
	 *        <p>
	 *        创建日期:2020年12月3日 上午11:09:17。
	 *        </p>
	 *        <p>
	 *        更新日期:[日期YYYY-MM-DD][wuxiusong][变更描述]。
	 *        </p>
	 */
	public static <T> BaseResult<T> fail(String message) {
		return new BaseResult<T>(false, message, null);
	}

	/**
	 * 
	 * <p>
	 * 功能描述：返回成功结果并加入实体对象。
	 * </p>
	 * 
	 * @param <T>
	 * @param data
	 * @return
	 * @since JDK1.8。
	 *        <p>
	 *        创建日期:2020年12月3日 上午11:09:50。
	 *        </p>
	 *        <p>
	 *        更新日期:[日期YYYY-MM-DD][wuxiusong][变更描述]。
	 *        </p>
	 */
	public static <T> BaseResult<T> succeed(T data) {
		return new BaseResult<T>(true, null, data);
	}

	/**
	 * 
	 * <p>
	 * 功能描述：仅返回成功结果无实体对象信息。
	 * </p>
	 * 
	 * @return
	 * @since JDK1.8。
	 *        <p>
	 *        创建日期:2020年12月3日 上午11:10:31。
	 *        </p>
	 *        <p>
	 *        更新日期:[日期YYYY-MM-DD][wuxiusong][变更描述]。
	 *        </p>
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static BaseResult succeed() {
		return new BaseResult(true, null, null);
	}

	/**
	 * 
	 * <p>
	 * 功能描述：将IPage<T>对象转换为通用分页结果并返回。
	 * </p>
	 * 
	 * @param <T>
	 * @param result
	 * @return
	 * @since JDK1.8。
	 *        <p>
	 *        创建日期:2020年12月3日 上午11:11:07。
	 *        </p>
	 *        <p>
	 *        更新日期:[日期YYYY-MM-DD][wuxiusong][变更描述]。
	 *        </p>
	 */
	public static <T> PageResult<T> pageSucceed(IPage<T> result) {
		return new PageResult<T>(true, null, result.getTotal(), result.getRecords());
	}

	/**
	 * 
	 * <p>
	 * 功能描述：根据当前页列表数据和总记录数生成通用分页结果并返回。
	 * </p>
	 * 
	 * @param <T>
	 * @param total 总记录数
	 * @param data  当前页列表数据
	 * @return
	 * @since JDK1.8。
	 *        <p>
	 *        创建日期:2020年12月3日 上午11:11:48。
	 *        </p>
	 *        <p>
	 *        更新日期:[日期YYYY-MM-DD][wuxiusong][变更描述]。
	 *        </p>
	 */
	public static <T> PageResult<T> pageSucceed(int total, List<T> data) {
		return new PageResult<T>(true, null, new Long(total), data);
	}

	/**
	 * 
	 * <p>
	 * 功能描述：返回分页失败结果并加入错误信息。
	 * </p>
	 * 
	 * @param message 错误信息
	 * @return
	 * @since JDK1.8。
	 *        <p>
	 *        创建日期:2020年12月3日 上午11:12:46。
	 *        </p>
	 *        <p>
	 *        更新日期:[日期YYYY-MM-DD][wuxiusong][变更描述]。
	 *        </p>
	 */
	public static <T> PageResult<T> pageFail(String message) {
		return new PageResult<T>(false, message, null, null);
	}



}
