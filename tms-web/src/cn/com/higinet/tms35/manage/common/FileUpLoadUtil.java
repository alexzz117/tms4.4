package cn.com.higinet.tms35.manage.common;

import java.io.*;

import org.springframework.web.multipart.MultipartFile;

public class FileUpLoadUtil {

	/**
	 * 文件的上传
	 * @param files 需要上传的文件
	 * @param fileName 上传的文件名
	 * @param path 上传的文件路径
	 * @throws IOException
	 */
	public static void fileUp(MultipartFile file,String fileName,String path) {

		
		File saveDir = new File(path, fileName);
		
		//判断目录是否存在
		if (!saveDir.getParentFile().exists())
            saveDir.getParentFile().mkdirs();
		
		// 转存文件
		try {
			file.transferTo(saveDir);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}
