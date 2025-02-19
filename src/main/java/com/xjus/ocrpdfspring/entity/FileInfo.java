package com.xjus.ocrpdfspring.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * <p>
 * 类描述:文件信息。
 * </p>
 * 
 * @author [wuxiusong]。
 * @version: v1.0.0.1。
 * @since JDK1.8。
 *        <p>
 *        创建日期：2020年12月2日 下午5:26:26。
 *        </p>
 *        Copyright 【北京新桥技术有限公司版权所有】 2020
 */
@Data
@EqualsAndHashCode(callSuper = false)
//@TableName()
// todo 建表
public class FileInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	@TableId
	/**
	 * id
	 */
	private String id; // id

	/**
	 * 原始文件名
	 */
	private String originalName; // 原始文件名

	/**
	 * 保存后重命名的文件名
	 */
	private String name; // 保存后重命名的文件名

	/**
	 * 文件存储文件名路径
	 */
	private String path; // 文件存储文件名路径

	/**
	 * 缩略图文件存储文件名路径
	 */
	private String thumbPath; // 缩略图文件存储文件名路径

	/**
	 * 文件大小
	 */
	private Long size; // 文件大小

	@TableField(updateStrategy = FieldStrategy.IGNORED)
	/**
	 * 文件过期时间
	 */
	private Date expireTime; // 文件过期时间

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@TableField(fill = FieldFill.INSERT)
	private Integer status;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@TableField(fill = FieldFill.INSERT, select = false)
	private String createUserId;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@TableField(fill = FieldFill.INSERT, select = false)
	private Date createTime;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@TableField(fill = FieldFill.INSERT_UPDATE, select = false)
	private String modifyUserId;

	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private Date modifyTime;
}
