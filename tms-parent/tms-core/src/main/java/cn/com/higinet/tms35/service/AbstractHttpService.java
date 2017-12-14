package cn.com.higinet.tms35.service;

import static cn.com.higinet.tms.common.StaticParams.ERR_APPKEYINCONSISTENT;
import static cn.com.higinet.tms.common.StaticParams.ERR_DECRY;
import static cn.com.higinet.tms.common.StaticParams.ERR_NOAUTH;
import static cn.com.higinet.tms.common.StaticParams.ERR_REDO;
import static cn.com.higinet.tms.common.StaticParams.ERR_TOKENINCONSISTENT;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.rapid.server.core.AsyncService;
import cn.com.higinet.rapid.server.core.Request;
import cn.com.higinet.rapid.server.core.Response;
import cn.com.higinet.tms.common.BUtil;
import cn.com.higinet.tms.common.SM4;
import cn.com.higinet.tms.common.StaticParams;
import cn.com.higinet.tms35.comm.IpUtil;
import cn.com.higinet.tms35.comm.MapUtil;
import cn.com.higinet.tms35.comm.StaticParameters;
import cn.com.higinet.tms35.comm.TmsHttpException;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.core.cache.db_cache;
import cn.com.higinet.tms35.core.cache.db_fwall_auth;
import cn.com.higinet.tms35.core.cache.db_token;
import cn.com.higinet.tms35.core.cond.date_tool;
import cn.com.higinet.tms35.core.dao.token_cache;
import cn.microdone.kb.Auth;
import cn.microdone.kb.Manage;


/**
 * @author zhangfg
 * @version 1.0,2014-09-20
 * @description 对外服务接口顶层接口
 *
 */
public abstract class AbstractHttpService extends AsyncService{
	
	private final static Logger logger = LoggerFactory.getLogger(AbstractHttpService.class);
	private final static boolean isDebug = logger.isDebugEnabled();
	public int timeout = 0;
	
	public void reponseResult(Response response, String callback,
			String resultJson) {
		
		if (!str_tool.is_empty(callback))
		{
			resultJson = callback + "({\"encry_result\":\"" + resultJson + "\"})";
		}
		response.setData(null,resultJson);
	}
	
	
	/**
	* 方法描述:补充交易数据
	* @param transaction
	* @param request
	*/
	
	public void addTxnFeature(Map<String, Object> transaction,
			Request request) {
		String app_id = tmsapp.get_config("microdone.param.appid","");
		String app_key = tmsapp.get_config("microdone.param.appkey","");
		String app_type = tmsapp.get_config("microdone.param.app_type","");
		String user_name = BUtil.getString(transaction, StaticParameters.Transaction.CSTNO);
		String random_num = BUtil.uuid();
		String user_finger = getParamValue(request,"devicefinger");
		String user_info = getParamValue(request,"keystroke");
		String collectType = getParamValue(request,"collectType");
		
		String user_ip = IpUtil.getIpAddr(request);
		
		if("0".equals(collectType)) {
			// 由js方式采集的设备信息
			// 补充设备指纹
			transaction.put(StaticParameters.Transaction.DEVICEFINGER, user_finger);
			// 补充设备标识
			transaction.put(StaticParameters.Transaction.DEVICETOKEN, user_info);

		}else {
			// 由微通新成控件的方式采集的设备信息
			String data_hash = str_tool.MD5(app_key+app_id+app_type+user_name+user_finger+user_info+user_ip);

			String serviceUrl = tmsapp.get_config(StaticParameters.MICRODONEURL,"");

			Map<String, String> params = new HashMap<String, String>();
			params.put("apps_id", app_id);
			params.put("app_type", app_key);
			params.put("user_name", user_name);
			params.put("random_num", random_num);
			params.put("user_finger",user_finger);
			params.put("user_info",user_info);
			params.put("user_ip", user_ip);
			params.put("data_hash", data_hash);
			Auth auth = Manage.PdAuth(serviceUrl, params);
			
			String keyStroke = auth.getAuth2();// 击键相似度

			// 补充设备ID
			transaction.put(StaticParameters.Transaction.DEVICETOKEN, auth.getPdDeviceId());
			
			if(isDebug) {
				logger.debug("***给微通新成上送的数据***");
				logger.debug("url："+serviceUrl);
				logger.debug("apps_id："+app_id);
				logger.debug("app_type："+app_key);
				logger.debug("user_name："+user_name);
				logger.debug("random_num："+random_num);
				logger.debug("user_finger："+user_finger);
				logger.debug("user_info："+user_info);
				logger.debug("user_ip："+user_ip);
				logger.debug("data_hash："+data_hash);
				logger.debug("微通新成返回击键行为相似度："+keyStroke);
			}
			// 补充击键相似度
			transaction.put(StaticParameters.Transaction.KEYSTROKESIMI, keyStroke == null || keyStroke.length() == 0 ? -1:keyStroke);
			
		}
		// IP
		if(str_tool.is_empty(MapUtil.getString(transaction, StaticParameters.Transaction.IPADDRESS))){
			transaction.put(StaticParameters.Transaction.IPADDRESS, user_ip);
		}
		
		//如果没有传送交易时间，进行补充
        if(str_tool.is_empty(MapUtil.getString(transaction, StaticParameters.Transaction.TRANSTIME))){
        	transaction.put(StaticParameters.Transaction.TRANSTIME,date_tool.now());
        }
	}
	
	/**
	 * 获取JSON数据
	 * @param jsonObject
	 * @return
	 */
	public String parseJsonString(Request request){
		StringBuffer json = new StringBuffer();
		String param = tmsapp.get_config("tms.http.jsonparam","");
		String requestJson = getParamValue(request,param);
		if(requestJson==null || "".equals(requestJson)){
			throw new TmsHttpException(StaticParameters.TMS_HTTP_TRANSACTIONNULL);
		}
		json.append(requestJson);
		
		logger.info("Receive Transaction Info:"+json.toString());
		return json.toString();
	}
	
	
	
	/**
	 * 出现异常时组装返回的异常信息JSON
	 * @param backCode
	 * @param exceptionInfo
	 * @return
	 */
	public String getExceptionJson(String backCode,String exceptionInfo){
		StringBuffer exceptionJson = new StringBuffer();
		exceptionJson.append("{");
		exceptionJson.append("\"backCode\":\""+backCode+"\",");
		exceptionJson.append("\"backInfo\":\""+exceptionInfo+"\"");
		exceptionJson.append("}");
		
		logger.error("{}",exceptionJson);
		
		return exceptionJson.toString();
	}
	
	/**
	* 方法描述:1，校验交易是否授权 2，校验交易是否重做交易 3，校验开关是否关闭
	* @param request
	* @return
	 */
	public String validate(Request request) {
		String appid = getParamValue(request,"appId");
		String token = getParamValue(request,"token");
		String signature = getParamValue(request,"signature");
		
		// 开关的校验应该在客户端判断，如果关闭，不与云服务通讯
		
		/*String json = parseJsonString(request);
		boolean useFlag = true;
		Map jsonObject = null;
		try {
			jsonObject = JacksonMapper.getInstance().readValue(json, HashMap.class);
			useFlag = MapUtil.getBoolean(jsonObject,StaticParameters.USEFLAG);
		} catch (Exception e1) {
			return getExceptionJson(StaticParameters.TMS_JSON_ERROR,"JSON格式不合法");
		}
		
		if(!useFlag) {
			return getExceptionJson(StaticParameters.TMS_API_ERROR,"The param 'useFlag' is FALSE or the TMS api is configed close");
		}*/
		
		String appkey_db = queryAppkey(appid);
		
		if(appkey_db.length()==0) {
			return getExceptionJson(ERR_NOAUTH,StaticParams.ERR_INFO.get(ERR_NOAUTH));
		}
		
		// 通过APP_KEY解密
		String decry_s = "";
		try {
			decry_s = SM4.decry_fwall_risk(appkey_db, signature);
		} catch (UnsupportedEncodingException e) {
			return getExceptionJson(ERR_DECRY,StaticParams.ERR_INFO.get(ERR_DECRY));
		}
		
		if(decry_s.length()<16) {
			return getExceptionJson(ERR_DECRY,StaticParams.ERR_INFO.get(ERR_DECRY));
		}
		
		String appkey_decry = decry_s.substring(0, 16);// 解密后的APPKEY
		String token_decry = decry_s.substring(16);// 解密后的token
		
		// appkey不一致
		if(!appkey_decry.equals(appkey_db)) {
			return getExceptionJson(ERR_APPKEYINCONSISTENT,StaticParams.ERR_INFO.get(ERR_APPKEYINCONSISTENT));
		}
		// token不一致
		if(!token_decry.equals(token)) {
			return getExceptionJson(ERR_TOKENINCONSISTENT,StaticParams.ERR_INFO.get(ERR_TOKENINCONSISTENT));
		}
		// 数据被重做
		db_token tokenCache = token_cache.read(token);
		if(tokenCache != null) {
			return getExceptionJson(ERR_REDO,StaticParams.ERR_INFO.get(ERR_REDO));
		}
		return null;
	}
	
	
	private String getParamValue(Request request,String key) {
		String appid = str_tool.to_str(request.getParameter(key));
		appid = (appid == null || appid.length() == 0) ? str_tool.to_str(request.getParameterMap().get(key)): appid;
		return appid;
	}
	
	/**
	* 方法描述:通过APPID查询缓存中的appkey
	* @param appid
	* @return
	*/
	String queryAppkey(String appid) {
		db_fwall_auth db_fwall_auth = db_cache.get().cloud_cache().get(appid);
		if(db_fwall_auth == null)
			return "";
		return db_fwall_auth.APP_KEY;
	}
	/**
	* 方法描述:加密风险评估结果
	* @param request
	* @param resultJson
	* @return
	* @throws UnsupportedEncodingException
	 */
	String encry_result(Request request,String resultJson) throws UnsupportedEncodingException {
		
		if(isDebug) {
			logger.debug("风险评估结果："+resultJson);
		}
		String appid = str_tool.to_str(request.getParameter("appId"));
		String appkey_db = queryAppkey(appid);
		if(appkey_db.length()>0 && resultJson != null && resultJson.length() > 0) {
			resultJson = resultJson.substring(0,resultJson.length()-1) + ",\""+StaticParams.RESULT_MAC+"\":\""+appkey_db+"\"}" ;
			// 风险评估结果加密返回
			resultJson = SM4.encry_fwall(appkey_db, resultJson);
		}
		return resultJson;
	}
}