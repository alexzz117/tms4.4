/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  CompressUtils.java   
 * @Package cn.com.higinet.tms.common.util   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-8-21 11:29:10   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.jcraft.jzlib.DeflaterOutputStream;
import com.jcraft.jzlib.InflaterInputStream;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

public class CompressUtils {
	private static final LZ4Factory lz4Factory = LZ4Factory.fastestInstance();
	/***
	  * 压缩GZip
	  * 
	  * @param data
	  * @return
	  */
	public static byte[] gZip(byte[] data) {
		byte[] b = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			GZIPOutputStream gzip = new GZIPOutputStream(bos);
			gzip.write(data);
			gzip.finish();
			gzip.close();
			b = bos.toByteArray();
			bos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}

	/***
	  * 解压GZip
	  * 
	  * @param data
	  * @return
	  */
	public static byte[] unGZip(byte[] data) {
		byte[] b = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			GZIPInputStream gzip = new GZIPInputStream(bis);
			byte[] buf = new byte[1024];
			int num = -1;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ((num = gzip.read(buf, 0, buf.length)) != -1) {
				baos.write(buf, 0, num);
			}
			b = baos.toByteArray();
			baos.flush();
			baos.close();
			gzip.close();
			bis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}

	/***
	  * 压缩Zip
	  * 
	  * @param data
	  * @return
	  */
	public static byte[] zip(byte[] data) {
		byte[] b = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ZipOutputStream zip = new ZipOutputStream(bos);
			ZipEntry entry = new ZipEntry("zip");
			entry.setSize(data.length);
			zip.putNextEntry(entry);
			zip.write(data);
			zip.closeEntry();
			zip.close();
			b = bos.toByteArray();
			bos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}

	/***
	  * 解压Zip
	  * 
	  * @param data
	  * @return
	  */
	public static byte[] unZip(byte[] data) {
		byte[] b = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			ZipInputStream zip = new ZipInputStream(bis);
			while (zip.getNextEntry() != null) {
				byte[] buf = new byte[1024];
				int num = -1;
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				while ((num = zip.read(buf, 0, buf.length)) != -1) {
					baos.write(buf, 0, num);
				}
				b = baos.toByteArray();
				baos.flush();
				baos.close();
			}
			zip.close();
			bis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}

	//	/***
	//	  * 压缩BZip2
	//	  * 
	//	  * @param data
	//	  * @return
	//	  */
	//	 public static byte[] bZip2(byte[] data) {
	//	  byte[] b = null;
	//	  try {
	//	   ByteArrayOutputStream bos = new ByteArrayOutputStream();
	//	   CBZip2OutputStream bzip2 = new CBZip2OutputStream(bos);
	//	   bzip2.write(data);
	//	   bzip2.flush();
	//	   bzip2.close();
	//	   b = bos.toByteArray();
	//	   bos.close();
	//	  } catch (Exception ex) {
	//	   ex.printStackTrace();
	//	  }
	//	  return b;
	//	 }

	/***
	 * 解压BZip2
	 * 
	 * @param data
	 * @return
	 */
	//	 public static byte[] unBZip2(byte[] data) {
	//	  byte[] b = null;
	//	  try {
	//	   ByteArrayInputStream bis = new ByteArrayInputStream(data);
	//	   CBZip2InputStream bzip2 = new CBZip2InputStream(bis);
	//	   byte[] buf = new byte[1024];
	//	   int num = -1;
	//	   ByteArrayOutputStream baos = new ByteArrayOutputStream();
	//	   while ((num = bzip2.read(buf, 0, buf.length)) != -1) {
	//	    baos.write(buf, 0, num);
	//	   }
	//	   b = baos.toByteArray();
	//	   baos.flush();
	//	   baos.close();
	//	   bzip2.close();
	//	   bis.close();
	//	  } catch (Exception ex) {
	//	   ex.printStackTrace();
	//	  }
	//	  return b;
	//	 }

	/**
	  * 把字节数组转换成16进制字符串
	  * 
	  * @param bArray
	  * @return
	  */
	public static String bytesToHexString(byte[] bArray) {
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		for (int i = 0; i < bArray.length; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase());
		}
		return sb.toString();
	}

	/**
	  *jzlib 压缩数据
	  * 
	  * @param object
	  * @return
	  * @throws IOException
	  */
	public static byte[] jzlib(byte[] object) {
		byte[] data = null;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DeflaterOutputStream zOut = new DeflaterOutputStream(out);
			DataOutputStream objOut = new DataOutputStream(zOut);
			objOut.write(object);
			objOut.flush();
			zOut.close();
			data = out.toByteArray();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	/**
	 *jzLib压缩的数据
	 * 
	 * @param object
	 * @return
	 * @throws IOException
	 */
	public static byte[] unjzlib(byte[] object) {
		byte[] data = null;
		try {
			ByteArrayInputStream in = new ByteArrayInputStream(object);
			InflaterInputStream zIn = new InflaterInputStream(in);
			byte[] buf = new byte[1024];
			int num = -1;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ((num = zIn.read(buf, 0, buf.length)) != -1) {
				baos.write(buf, 0, num);
			}
			data = baos.toByteArray();
			baos.flush();
			baos.close();
			zIn.close();
			in.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	/**
	 * LZ4压缩算法压缩
	 * 参考样例：https://github.com/lz4/lz4-java
	* @param data
	* @return
	*/
	public static byte[] lz4(byte[] data) {
		final int decompressedLength = data.length;
		LZ4Compressor compressor = lz4Factory.fastCompressor();
		int maxCompressedLength = compressor.maxCompressedLength(decompressedLength);
		byte[] compressed = new byte[maxCompressedLength];
		compressor.compress(data, 0, decompressedLength, compressed, 0, maxCompressedLength);
		return compressed;
	}

	/**
	 * LZ4压缩算法解压
	* @param data
	* @param originalDataLength 压缩前数据大小，需要压缩前记录
	* @return
	*/
	public static byte[] unlz4(byte[] data, int originalDataLength) {
		LZ4FastDecompressor decompressor = lz4Factory.fastDecompressor();
		byte[] restored = new byte[originalDataLength];
		decompressor.decompress(data, 0, restored, 0, originalDataLength);
		return restored;
	}

	/**
	* 主函数.
	*
	* @param args the arguments
	*/
	public static void main(String[] args) {
		String s = "this is a test";

		byte[] b1 = zip(s.getBytes());
		System.out.println("zip:" + bytesToHexString(b1));
		byte[] b2 = unZip(b1);
		System.out.println("unZip:" + new String(b2));
		//	  byte[] b3 = bZip2(s.getBytes());
		//	  System.out.println("bZip2:" + bytesToHexString(b3));
		//	  byte[] b4 = unBZip2(b3);
		//	  System.out.println("unBZip2:" + new String(b4));
		//	  byte[] b5 = gZip(s.getBytes());
		//	  System.out.println("bZip2:" + bytesToHexString(b5));
		//	  byte[] b6 = unGZip(b5);
		//	  System.out.println("unBZip2:" + new String(b6));
		//	  byte[] b7 = jzlib(s.getBytes());
		//	  System.out.println("jzlib:" + bytesToHexString(b7));
		//	  byte[] b8 = unjzlib(b7);
		//	  System.out.println("unjzlib:" + new String(b8));
		//	 }

		byte[] b9 = lz4(s.getBytes());
		System.out.println("lz4:" + new String(b9));

		byte[] b10 = unlz4(b9,s.length());
		System.out.println("lz4:" + new String(b10));
	}
}
