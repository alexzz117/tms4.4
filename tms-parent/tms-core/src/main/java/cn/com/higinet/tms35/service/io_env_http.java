package cn.com.higinet.tms35.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.rapid.server.core.Request;
import cn.com.higinet.rapid.server.core.Response;
import cn.com.higinet.tms.common.SM3;
import cn.com.higinet.tms.common.SM4;
import cn.com.higinet.tms.common.StaticParams;
import cn.com.higinet.tms35.comm.JacksonMapper;
import cn.com.higinet.tms35.comm.MapUtil;
import cn.com.higinet.tms35.comm.StaticParameters;
import cn.com.higinet.tms35.comm.TmsHttpException;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.core.cache.db_cache;
import cn.com.higinet.tms35.core.cache.db_fwall_auth;

/**
 * Http连接IO运行环境
 * @author lining
 */
public class io_env_http extends io_env {
	static Logger log = LoggerFactory.getLogger(io_env_http.class);
	static boolean isDebug = log.isDebugEnabled();
	Map<String, Object> transMap;
	boolean isConfirm;
	boolean isBatch;
	
	public io_env_http(Request req, Response res, 
			Map<String, Object> transMap,boolean isBatch)
	{
		this(req, res, transMap, false,isBatch);
	}
	
	public io_env_http(Request req, Response res, 
			Map<String, Object> transMap, boolean isConfirm,boolean isBatch)
	{
		super(req, res);
		this.transMap = transMap;
		this.isConfirm = isConfirm;
		this.isBatch = isBatch;
	}
	
	public Map<String, Object> getHeadMap()
	{
		return null;
	}

	public TreeMap<String, String> getParameterMap()
	{
		return map_cast(this.transMap);
	}

	public Map<String, Object> getParameterOriMap()
	{
		return Collections.unmodifiableMap(this.transMap);
	}
	
	public void done(Exception ex)
	{
		boolean is_returned = false;
		try
		{
			if (!returned.compareAndSet(false, true))
			{
				is_returned = true;
				return;
			}
			this.pin_time(INDEX_RETURN_RESULT_BG);
			done_handle();
			res.done(req, ex);
		}
		catch (Exception e)
		{
			log.error("excute done(e) method error.", e);
		}
		finally
		{
			if (!is_returned)
			{
				this.pin_time(INDEX_RETURN_RESULT_END);
				this.setResult(res.getReturnCode(), str_tool.to_str(res.getData("errorInfo")));
			}
		}
	}
	
	/**
	 * 返回结果前处理
	 */
	private void done_handle()
	{
		String resultJson = "";
		try
		{
			// SM3散列交易数据,为防篡改做准备
			String sm3_cry = "";
			
			if (!this.isConfirm)
			{
				List<Map<String, Object>> sub_req_list = null;

				String json = parseJsonString(req);
				String transaction_key = StaticParameters.Transaction.TRANSACTION;
				Map transMap = JacksonMapper.getInstance().readValue(json, TreeMap.class);
				if(isBatch)
				{
					transaction_key = StaticParameters.Batch.BATCH;
					
					sub_req_list = new ArrayList<Map<String, Object>>();
					Map mainTrans = MapUtil.getMap(transMap,StaticParameters.Transaction.TRANSACTIONS);
					if(mainTrans!=null && !mainTrans.isEmpty()){
						for(int i=0;i<mainTrans.size();i++){
							Map transaction = MapUtil.getMap(mainTrans,StaticParameters.Transaction.TRANSACTION+i);
							if(transaction!=null){
								sub_req_list.add(transaction);
							}
						}
					}
				}
				
				Map transaction =  MapUtil.getMap(transMap,transaction_key);
				
				try {
					sm3_cry = SM3.encry_fwall_txn(transaction,sub_req_list);
				} catch (UnsupportedEncodingException e) {
					throw new TmsHttpException(StaticParameters.TMS_HTTP_ENCRYERROR);
				}
			}
			
			HashMap<String, Object> resultMap = new HashMap<String, Object>(res.getDataMap());
			
			if(sm3_cry.length() > 0)
			{
				resultMap.put(StaticParams.RESULT_SIGNATURE,sm3_cry);// 原始的交易数据加密放到返回结果中
			}
			String ret_cod = res.getReturnCode();
			if(ret_cod != null && ret_cod.length()>0)
			{
				resultMap.put(StaticParams.RESULT_BACKCODE,ret_cod);// 返回码
			}
			
			Object backInfo = res.getData(StaticParameters.NODE_NAME_ERRORINFO);
			if(backInfo!=null) {
				resultMap.put(StaticParams.RESULT_BACKINFO,backInfo);// 返回信息
			}
			
			resultJson = JacksonMapper.getInstance().writeValueAsString(resultMap);
			if(isDebug) {
				log.debug("resultJson1:"+resultJson);
			}
			if (!this.isConfirm)
			{
				resultJson = "\"" + encry_result(req, resultJson)+"\"";
			}
			String callback = str_tool.to_str(req.getParameter("callback"));
			if(isDebug) {
				log.debug("callback:"+callback);
			}
			if (!str_tool.is_empty(callback))
			{
				resultJson = callback + "({\"encry_result\":" + resultJson + "})";
			}
			if(isDebug) {
				log.debug("resultJson2:"+resultJson);
			}
			res.setData(null, resultJson);
		}
		catch (Exception e)
		{
			log.error("Response Data process error.", e);
		}
	}
	
	/**
	* 方法描述:加密风险评估结果
	* @param request
	* @param resultJson
	* @return
	* @throws UnsupportedEncodingException
	 */
	String encry_result(Request request, String resultJson) throws UnsupportedEncodingException {
		String appid = str_tool.to_str(request.getParameter("appId"));
		String appkey_db = queryAppkey(appid);
		if(appkey_db.length()>0 && resultJson != null && resultJson.length() > 0) {
			resultJson = resultJson.substring(0,resultJson.length()-1) + ",\""+StaticParams.RESULT_MAC+"\":\""+appkey_db+"\"}" ;
			// 风险评估结果加密返回
			resultJson = SM4.encry_fwall(appkey_db, resultJson);
		}
		return resultJson;
	}
	
	/**
	 * 获取JSON数据
	 * @param jsonObject
	 * @return
	 */
	public String parseJsonString(Request request){
		StringBuffer json = new StringBuffer();
		String param = tmsapp.get_config("tms.http.jsonparam","");
		String requestJson = str_tool.to_str(request.getParameter(param));
		if(requestJson==null || "".equals(requestJson)){
			throw new TmsHttpException(StaticParameters.TMS_HTTP_TRANSACTIONNULL);
		}
		json.append(requestJson);
		
		return json.toString();
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
}