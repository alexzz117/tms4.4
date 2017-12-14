package cn.com.higinet.tms35.comm;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.security.Key;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import gnu.crypto.util.Base64;


/**
 * 加密解密工具类
 * @author lining
 *
 */
public class DESEncryptUtil {
	/**
	 * 对称加解密方式
	 */
	private static final String Algorithm = "DESede";
	/**
	 * 默认密钥
	 */
	private static final byte[] defaultKeyByte = {
		0x11, 0x22, 0x4F, 0x58, (byte) 0x88, 0x10, 0x40, 0x38, 0x28, 0x25, 0x79, 0x51, (byte) 0xCB,
		(byte) 0xDD, 0x55, 0x66, 0x77, 0x29, 0x74, (byte) 0x98, 0x30, 0x40, 0x36, (byte) 0xE2
	};
	
	/**
	 * 创建随机密钥
	 * @return
	 */
	public static Key createKey() {
		try {
			Security.insertProviderAt(new com.sun.crypto.provider.SunJCE(), 1);
			KeyGenerator generator = KeyGenerator.getInstance(Algorithm);
			generator.init(new SecureRandom());
			Key key = generator.generateKey();
			return key;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 通过流加载密钥
	 * @param is
	 * @return
	 */
	public static Key getKey(InputStream is) {
		try {
			ObjectInputStream ios = new ObjectInputStream(is);
			return (Key) ios.readObject();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 获取默认密钥
	 * @return
	 */
	public static Key getDefaultKey() {
		SecretKey decKey = new SecretKeySpec(defaultKeyByte, Algorithm);
		return decKey;
	}
	
	/**
	 * 对数据进行加密
	 * @param key	密钥串
	 * @param data	需要加密的数据
	 * @return
	 */
	public static String doEncrypt(byte[] keybuf, String data) {
		try {
			if (keybuf == null || keybuf.length == 0) {
				keybuf = defaultKeyByte;
			}
			SecretKey decKey = new SecretKeySpec(keybuf, Algorithm);
			return doEncrypt(decKey, data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 对数据进行加密
	 * @param key	密钥
	 * @param data	需要加密的数据
	 * @return
	 */
	public static String doEncrypt(Key key, String data) {
		try {
			if (key == null) {
				return doEncrypt(new byte[] {}, data);
			}
			Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] raw = cipher.doFinal(data.getBytes());
			return byte2Base64(raw);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 对数据进行解密
	 * @param keybuf	密钥串byte数组
	 * @param data		需要解密的数据
	 * @return
	 */
	public static String doDecrypt(byte[] keybuf, String data) {
		try {
			if (keybuf == null || keybuf.length == 0) {
				keybuf = defaultKeyByte;
			}
			SecretKey desKey = new SecretKeySpec(keybuf, Algorithm);
			return doDecrypt(desKey, data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 对数据进行解密
	 * @param key	密钥
	 * @param data	需要解密的数据
	 * @return
	 */
	public static String doDecrypt(Key key, String data) {
		try {
			if (key == null) {
				return doDecrypt(new byte[] {}, data);
			}
			Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] raw = cipher.doFinal(Base64.decode(data));
			return new String(raw);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 转换成base64编码  
	 * @param b
	 * @return
	 */

	public static String byte2Base64(byte[] b) {
        return Base64.encode(b);
    }
	
	public static void main(String[] args) {
		try {
			/*Key key = DESEncryptUtil.createKey();
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File("D:\\cipher.key")));
			out.writeObject(key);
			out.close();*/
			FileInputStream fis = new FileInputStream(new File("D:\\cipher.key"));
			Key key1 = DESEncryptUtil.getKey(fis);
			System.out.println(DESEncryptUtil.doEncrypt(key1, "111111"));
			System.out.println(DESEncryptUtil.doDecrypt(key1, "sf02jsAkf60="));
			System.out.println(DESEncryptUtil.doEncrypt(new byte[] {}, "abc1234"));
			System.out.println(DESEncryptUtil.doDecrypt(new byte[] {}, "zYGLEm3CMRY="));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}