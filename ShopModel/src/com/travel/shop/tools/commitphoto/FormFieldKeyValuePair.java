package com.travel.shop.tools.commitphoto;

import java.io.Serializable;

/**
 * 
 * <p>
 * Title:FormFieldKeyValuePair
 * </p>
 * <p>
 * Description: 用于处理普通表单域形如key = value对的数据
 * </p>
 * <p>
 * Company: yosuntech
 * </p>
 * 
 * @author: HuangZuQing
 * @version: [1.0]
 * @date: 2015-10-10 上午11:02:41
 * 
 */
public class FormFieldKeyValuePair implements Serializable {
	private static final long serialVersionUID = 1L;

	// The form field used for receivinguser's input,
	// such as "username" in "<inputtype="text" name="username"/>"
	private String key;
	// The value entered by user in thecorresponding form field,
	// such as "Patrick" the abovementioned formfield "username"
	private String value;

	public FormFieldKeyValuePair(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
