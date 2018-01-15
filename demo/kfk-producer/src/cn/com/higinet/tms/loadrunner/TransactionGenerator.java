/** 
 * @file 
 * @brief brief description 
 * @author 王兴 
 * [@author-desc 宏基恒信深圳研发中心] 
 * @date 2017-4-29
 * @version v1.0
 * @note 
 * detailed description 
 */
package cn.com.higinet.tms.loadrunner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import cn.com.higinet.tms.core.model.Transaction;
import cn.com.higinet.tms.core.util.StringUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class TransactionGenerator.
 *
 * @author 王兴
 * @since v1.0
 * @date 2017-4-27
 */
public class TransactionGenerator {
	
	/** The Constant random. */
	public static final Random random = new Random();

	/** The Constant deviceId. */
	private static final String deviceId = "55-5R-FC-FD-EQ-A72";

	//	private static final String deviceToken = "6f3f2882786c4e2e90e80e32c879da1q";

	//	private static final String deviceFingerPrint = "1|2001:868047021163169|2004:88AKBME22ZJ8|2006:4910367dc88df82d|2009:0|2010:true|2013:4.589389937671455|2014:720*1280|2015:m2|2016:m2|2018:android|2019:5.1|2020:zh|2021:com.zte.smartpay|2022:1.0.5|2023:322d4985062b16|2024:22|2025:1.55 GB|2026:1.88 GB|2027:49|2028:MOLY.LR9.W1444.MD.LWTG.CMCC.MP.V10.P44, 2015/08/24 10:49|2029:3.10.65+|2030:CN|2031:TimeZone   GMT+08:00Timezon id :: Asia/Shanghai|2032:10.113.175.229|2033:0.0.0.0|2034:0|2035:0|2036:0|2051:11.59 GB|2052:AArch64 Processor rev 3 (aarch64) 0|2053:BOARD: m2, BOOTLOADER: unknown|2054:m2|2055:4g";

	/** The Constant custNo. */
	private static final String custNo = "20148999891962";

	/** The Constant ipAddress. */
	private static final String ipAddress = "10.118.201.156";

	/** The Constant channelType. */
	private static final String channelType = "CH01";

	/**
	 * 指定固定的交易类型，其他属性全部随机.
	 *
	 * @param transType the trans type
	 * @return the transaction
	 */
	public static Transaction generateRandomTransaction(String transType) {
		return generateRandomTransaction(transType, channelType, null, true, true, true);
	}

	/**
	 * 指定固定的交易类型，其他属性全部随机.
	 *
	 * @param transType the trans type
	 * @param extInfo the ext info
	 * @return the transaction
	 */
	public static Transaction generateRandomTransaction(String transType, Map<String, Object> extInfo) {
		return generateRandomTransaction(transType, channelType, extInfo, true, true, true);
	}

	/**
	 * 一个简单的交易对象生成代码，只设置部分值随机.
	 *
	 * @param transType the trans type
	 * @param channelType the channel type
	 * @param extInfo the ext info
	 * @param randomCustNo the random cust no
	 * @param randomIP the random ip
	 * @param randomDeviceId the random device id
	 * @return the transaction
	 */
	public static Transaction generateRandomTransaction(String transType, String channelType, Map<String, Object> extInfo, boolean randomCustNo, boolean randomIP, boolean randomDeviceId) {
		Transaction transaction = null;
		transaction = new Transaction();
		transaction.setTransCode(StringUtil.randomUUID());
		if (randomIP) {
			transaction.setIpAddress(randomString(255, ".", 4));
		} else {
			transaction.setIpAddress(ipAddress);
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (randomCustNo) {
			transaction.setCstNo(String.valueOf(System.currentTimeMillis() % 1000000000));//重复无所谓
		} else {
			transaction.setCstNo(custNo);
		}
		if (randomDeviceId) {
			transaction.setDeviceId(randomString(99, "-", 6));
		} else {
			transaction.setDeviceId(deviceId);
		}
		//		transaction.setDeviceToken(deviceToken);
		//		transaction.setDeviceFinger(deviceFingerPrint);
		transaction.setTransType(transType);
		transaction.setChannelType(channelType);
		if (extInfo != null) {
			transaction.setExtInfo(extInfo);
		}
		transaction.setTransTime(sdf.format(new Date()));
		return transaction;
	}

	/**
	 * Random string.
	 *
	 * @param range the range
	 * @param separator the separator
	 * @param tokens the tokens
	 * @return the string
	 */
	public static String randomString(int range, String separator, int tokens) {
		StringBuilder sb = new StringBuilder(String.valueOf(getRandomNumber(range)));
		for (int i = 1; i < tokens; i++) {
			sb.append(separator).append(getRandomNumber(range));
		}
		return sb.toString();
	}

	/**
	 * Gets the random number.
	 *
	 * @param range the range
	 * @return the random number
	 */
	public static int getRandomNumber(int range) {
		return random.nextInt(range - 1) + 1;
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		Transaction t = generateRandomTransaction("NPP101");
		System.out.println(t);
	}
}
