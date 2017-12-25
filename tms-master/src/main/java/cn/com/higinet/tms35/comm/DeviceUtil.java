package cn.com.higinet.tms35.comm;

import java.math.BigDecimal;
import java.security.interfaces.RSAPrivateCrtKey;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ocx.RSAUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;

import cn.com.higinet.rapid.base.util.IdUtil;
import cn.com.higinet.tms35.core.cache.db_cache;
import cn.com.higinet.tms35.core.cache.db_dfp_app_properties;
import cn.com.higinet.tms35.core.cache.db_dfp_application;
import cn.com.higinet.tms35.core.cache.db_dfp_property;
import cn.com.higinet.tms35.core.cache.linear;

public class DeviceUtil {
	private static Logger log = LoggerFactory.getLogger(DeviceUtil.class);
	public final static String MD5_KEY = "8C2F5BE35845496D970BC14E9057DF50";
	
	public final static class TokenType{
		@Deprecated
		public static final String RANDOM = "0";
		@Deprecated
		public static final String OUT = "2";
		public static final String NO_MATCH = "3";
		public static final String DEVICE_INFO = "1";
		public static final String DEVICE_INFO_RANDOM = "4";
	}
	
	
	public static String getDeviceInfo(String dev_finger){
		if(dev_finger == null || dev_finger.length() <= 2){
			return null;
		}
		String device_type = getDeviceType(dev_finger);
		dev_finger = dev_finger.substring(dev_finger.indexOf("|")+1);
		if("2".equals(device_type) || "4".equals(device_type) || "6".equals(device_type) || "8".equals(device_type)){
			StringBuffer deviceInfoNew = new StringBuffer();
			String[] params = String.valueOf(dev_finger).split("\\|");
			for (int i = 0; i < params.length; i++) {
				String param = params[i];
				if(param.indexOf(":") != -1){
					int key = Integer.parseInt(param.substring(0, param.indexOf(":"))) + 4000;
					String value = param.substring(param.indexOf(":") + 1, param.length());
					if(i>0){
						deviceInfoNew.append("|");
					}
					deviceInfoNew.append(key).append(":").append(value); 
				}
			}
			dev_finger = deviceInfoNew.toString();
		}else if("1".equals(device_type) || "3".equals(device_type)){
		}else if("5".equals(device_type) || "7".equals(device_type)){
			String filepath = tmsapp.get_config("microdone.file.pfx", ""); 
		    String pwd =tmsapp.get_config("microdone.file.pfx.pwd", "");//证书的密码
		    RSAPrivateCrtKey prikey =RSAUtil.GetPrivateKey1(filepath,pwd);
		    String[] rsaDecrypt;
		    try{
		    	rsaDecrypt = RSAUtil.RSADecrypt(prikey,dev_finger);
		    	dev_finger = parseSdkDeviceInfo(rsaDecrypt[2], device_type);// 设备信息
//		    	sdkToken = rsaDecrypt[0]; // 设备唯一标识 
		    }catch(Exception e){
		    	e.printStackTrace();
		    	log.info(dev_finger);
		    	log.error("微通设备指纹解密出错.", new tms_exception("微通设备指纹解密出错[" + device_type + "]渠道"));
		    }
		}else{
			log.error("dev_finger eror:  " + dev_finger);
		}
		return dev_finger;
	}
	
//	public static void main(String[] args) {
//		String filepath = System.getProperty("user.dir").replaceAll("\\\\", "/") + "/src/resource/pfx/BOCD1234.pfx";
//	    String pwd ="BOCD1234";//证书的密码
//	    System.out.println(filepath);
//	    filepath = tmsapp.get_config("microdone.file.pfx", "");
//	    System.out.println(filepath);
//	}
	
	public static String getDeviceType(String dev_finger){
		if(dev_finger == null || dev_finger.length() < 3){
			return null;
		}
		return dev_finger.substring(0, dev_finger.indexOf("|"));
	}
	/**
	 * 
	 * @param deviceInfo
	 * @param tokenType  app.token_type
	 * @param device_type  app.app_id
	 * @return
	 */
	public static String getDeviceToken(String deviceInfo, Object tokenType,String device_type){
		if(TokenType.RANDOM.equals(tokenType)){
			return IdUtil.uuid();
		}
		if(deviceInfo == null || deviceInfo.length() < 3){
			return null;
		}
		
//		if(TokenType.OUT.equals(tokenType)){
//			return device_token;
//		}
		Map<String,String> paramMap = new HashMap<String, String>();
		String[] params = String.valueOf(deviceInfo).split("\\|");
		//List list=Arrays.asList(params1);
		//Set set =new HashSet(list);
		//String[] params =(String[])set.toArray(new String[0]);
		for(String param : params){
			 try{
				if(param.indexOf(":") != -1){
					String key = param.substring(0, param.indexOf(":"));
					String value = param.substring(param.indexOf(":") + 1, param.length());
					paramMap.put(key, value);
				}
			 }catch(Exception e){
				 log.warn(String.format("Exception occurred when parsing device parameters \"%s\".",param),e);
			 }
		}
//		String[] keys = "4002,4003,4005,4007,4008,4014,4015,4101,4102,4106,4107".split(",");
		linear<db_dfp_app_properties> linear = db_cache.get().app_properties().get(device_type);
		StringBuffer token = new StringBuffer();
		for(db_dfp_app_properties ap : linear){
			if("1".equals(ap.isToken)){
				token.append(getPropFirstValue(paramMap.get(ap.prop_id + ""))).append("|");
			}
		}
		if(token.length() == 0){
			return null;
		}else{
			return SysUtil.md5(token.toString() + MD5_KEY);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static String parseSdkDeviceInfo(String deviceInfo,String appId) throws Exception{
	    //解码
	    JSONObject jo = null;
		jo = new JSONObject(deviceInfo);
//	    String[] params = deviceInfo.split(",");
	    String result = "";
	    Iterator keys = jo.keys();
	    linear<db_dfp_app_properties> linear = db_cache.get().app_properties().get(appId);
	    while(keys.hasNext()){
	    	String key = String.valueOf(keys.next());
	    	String value = String.valueOf(jo.get(key));
	    	String prop_key = getPropIdByName(key,linear);
	    	if(prop_key != null){
	    		result += prop_key +":" + value +"|";
	    	}
	    }
	    if(result.length()>1){
	    	result = result.substring(0, result.length() -1 );
	    }
	    return result;
	}
	
	private static String getPropIdByName(Object name, linear<db_dfp_app_properties> linear){
		for (int a = 0, alen = linear.size(); a < alen; a++) {
			 db_dfp_app_properties properties = linear.get(a);
			db_dfp_property prop = db_cache.get().property()
					.get(properties.prop_id);
			if (name != null && prop != null && prop.prop_name != null 
					&& prop.prop_name.trim().replaceAll(" ", "").replaceAll("_", "")
					.equalsIgnoreCase(String.valueOf(name).replaceAll("_", "").replaceAll(" ", ""))) {
				return String.valueOf(properties.prop_id);
			}
		}
		return null;
	}
	
	/**
	 * 比较两个指纹参数的相似度
	 * @param chann_finger	渠道传递的指纹
	 * @param prop_finger	数据库中指纹
	 * @param app			渠道信息
	 * @return
	 */
	public static double fingerSimilarity(String chann_finger, String prop_finger, db_dfp_application app)
	{
		BigDecimal d = new BigDecimal(0.0);//分母
		BigDecimal m = new BigDecimal(0.0);//分子
		linear<db_dfp_app_properties> app_props = db_cache.get().app_properties().get(app.app_id);
		for (int a = 0, alen = app_props.size(); a < alen; a++)
		{
			db_dfp_app_properties app_prop = app_props.get(a);
			db_dfp_property prop = db_cache.get().property().get(app_prop.prop_id);
			if (prop != null) {
				String real_value = getPropertyValue(prop, chann_finger);//渠道传递的参数值
				String db_value = getPropertyValue(prop, prop_finger);//数据库中的参数值
				BigDecimal w = new BigDecimal(app_prop.weight);
				d = d.add(w);
				if(equals(real_value, db_value)){
					m = m.add(w);
				}
				
			}
		}
		if (d.doubleValue() <= 0 || m.doubleValue() <= 0)
			return 0d;
		return m.divide(d, 2,BigDecimal.ROUND_HALF_EVEN).doubleValue();
	}
	
	private static boolean equals(String a, String b){
		if(a == null && b == null){
			return true;
		}else if(a != null){
			return a.equals(b);
		}else{
			return false;
		}
	}
	
	/**
	 * 在设备指纹中获取某个参数的值
	 * @param prop			要获取的参数
	 * @param prop_values	设备指纹
	 * @return
	 */
	public static String getPropertyValue(db_dfp_property prop, String prop_values) {
		return getPropById(String.valueOf(prop.prop_id), prop_values);
	}
	
	public static String getPropById(String propId, String prop_values){
		if (str_tool.is_empty(prop_values))
		{
			return "";
		}
		if (!prop_values.startsWith("|"))
		{
			prop_values = "|" + prop_values;
		}
		if (!prop_values.endsWith("|"))
		{
			prop_values += "|";
		}
		if (prop_values.indexOf("|" + propId + ":") == -1){
			return null;
		}
		int s = prop_values.indexOf("|" + propId + ":") + propId.length() + 2;
		int e = prop_values.indexOf("|", s);
		return getPropFirstValue(prop_values.substring(s, e));
	}
	public static String getPropFirstValue(String propValue){
		if(propValue == null || propValue.length()<4){
			return propValue;
		}
		if(propValue.startsWith("[\"") && propValue.endsWith("\"]")){
			String[] props = propValue.substring(2,propValue.length()-2).split("\",\"");
			for(String prop : props){
				if(!"0.0.0.0".equals(prop)){
					return prop;
				}
			}
			return "";
		}else{
			return propValue;
		}
	}
//	public static void main(String[] args) {
////		String str = "[\"ca-2a-14-81-3d-00\",\"c8-2a-14-18-c2-11\",\"e0-f8-47-37-fa-f6\",\"d2-00-1b-bc-8b-20\",\"02-f8-47-37-fa-f6\"]";
////		String str = "[\"0.0.0.0\",\"10.8.5.24\",\"0.0.0.0\",\"192.168.190.1\",\"192.168.80.1\"]";
//		String str = "[]";
//		
//		System.out.println(getPropFirstValue(str));
//	}
}
