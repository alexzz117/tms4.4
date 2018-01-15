package cn.com.higinet.tms.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FindInFile {

	private static final String PATH = "E:\\loadrunner_results\\res5\\log";

	private static final String TARGET = "02009999";

	public static boolean isStringInFile(File file, String str) throws IOException {
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		try {
			String line = null;
			while ((line = br.readLine()) != null)
				if (line.contains(str)) {
					return true;
				}
		} finally {
			try {
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return false;
	}

	public static void main(String[] args) throws Exception {
		File f = new File(PATH);
		if (f.exists()) {
			File[] fs = f.listFiles();
			for (File _f : fs) {
				if (isStringInFile(_f, TARGET)) {
					System.out.println(_f.getName());
				}
			}
		}
	}
}
