package com.travel.shop.tools.commitphoto;

import java.io.File;
import java.util.ArrayList;

/**
 * 
 * <p>
 * Title:UploadFilePostRequest
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Company: yosuntech
 * </p>
 * 
 * @author: HuangZuQing
 * @version: [1.0]
 * @date: 2015-10-10 下午12:02:52
 * 
 */
public class UploadFilePostRequest{


	/**
	 * 上传文件
	 * 
	 * @param serverUrl
	 * @param path
	 * @param key
	 * @param value
	 * @return
	 */
	public static String uploadFile(String serverUrl, String path, String key,
			String value, String[] files) {
		// 设定要上传的普通Form Field及其对应的value
		// 类FormFieldKeyValuePair的定义见后面的代码
		ArrayList<FormFieldKeyValuePair> ffkvp = new ArrayList<FormFieldKeyValuePair>();
		ffkvp.add(new FormFieldKeyValuePair(key, value));

		// 设定要上传的文件。UploadFileItem见后面的代码
		ArrayList<UploadFileItem> ufi = new ArrayList<UploadFileItem>();
		// 传目录
		if (!"".equals(path.trim())) {
			File f = new File(path);
			if (f.isDirectory()) {// 读取目录下的文件
				String[] fileS = f.list();
				for (int i = 0; i < fileS.length; i++) {
					ufi.add(new UploadFileItem("upload" + i, fileS[i]));
				}
			}
		}
		// 传文件数组
		for (int i = 0; i < files.length; i++) {
			ufi.add(new UploadFileItem("uploadfile" + i, files[i]));
		}

		// 类HttpPostEmulator的定义，见后面的代码
		HttpPostEmulator hpe = new HttpPostEmulator();
		String response = null;
		try {
			response = hpe.sendHttpPostRequest(serverUrl, path, ffkvp, ufi);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Responsefrom server is: " + response);
		return response;
	}


}
