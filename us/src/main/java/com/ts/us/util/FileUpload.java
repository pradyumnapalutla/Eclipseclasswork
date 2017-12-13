package com.ts.us.util;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

public class FileUpload {

	public static void uploadImage(String imagePath,CommonsMultipartFile commonsMultipartFile,String ImageName) {
		try {
			byte[] bytes = commonsMultipartFile.getBytes();
			
			File dir = new File(imagePath);
			FileOutputStream f = new FileOutputStream(dir);
			if(!dir.exists()) {
				dir.mkdirs();
			}
			BufferedOutputStream stream = new BufferedOutputStream(f);
			stream.write(bytes);
			stream.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
