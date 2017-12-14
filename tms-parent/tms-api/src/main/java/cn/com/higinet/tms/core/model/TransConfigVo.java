package cn.com.higinet.tms.core.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 交易配置信息对象
 * @author Administrator
 *
 */
public class TransConfigVo implements Serializable{
	
	private String id;
	
	private String transName;
	
	private String riskType;
	
	private List<Map<String, String>> params;
	
	private boolean startFlag = true;
	
	private String conversionClass ;

	private String httpHead;
	
	private String httpSession;
	
	private String httpCookie;
	
	private String httpParam;
	
	private String httpAttribute;
	
	private Map<String, String> httpHeadMap;
	
	private Map<String, String> httpSessionMap;
	
	private Map<String, String> httpCookieMap;
	
	private Map<String, String> httpParamMap;
	
	private Map<String, String> httpAttributeMap;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRiskType() {
		return riskType;
	}

	public String getTransName() {
		return transName;
	}

	public void setTransName(String transName) {
		this.transName = transName;
	}

	public void setRiskType(String riskType) {
		this.riskType = riskType;
	}

	public List<Map<String, String>> getParams() {
		return params;
	}

	public void setParams(List<Map<String, String>> params) {
		this.params = params;
	}

	public boolean getStartFlag() {
		return startFlag;
	}

	public synchronized void setStartFlag(boolean startFlag) {
		this.startFlag = startFlag;
	}

	public String getConversionClass() {
		return conversionClass;
	}

	public void setConversionClass(String conversionClass) {
		this.conversionClass = conversionClass;
	}

	public String getHttpHead() {
		return httpHead;
	}

	public void setHttpHead(String httpHead) {
		this.httpHead = httpHead;
	}

	public String getHttpSession() {
		return httpSession;
	}

	public void setHttpSession(String httpSession) {
		this.httpSession = httpSession;
	}

	public String getHttpCookie() {
		return httpCookie;
	}

	public void setHttpCookie(String httpCookie) {
		this.httpCookie = httpCookie;
	}

	public String getHttpParam() {
		return httpParam;
	}

	public void setHttpParam(String httpParam) {
		this.httpParam = httpParam;
	}

	public String getHttpAttribute() {
		return httpAttribute;
	}

	public void setHttpAttribute(String httpAttribute) {
		this.httpAttribute = httpAttribute;
	}

	public Map<String, String> getHttpHeadMap() {
		return httpHeadMap;
	}

	public void setHttpHeadMap(Map<String, String> httpHeadMap) {
		this.httpHeadMap = httpHeadMap;
	}

	public Map<String, String> getHttpSessionMap() {
		return httpSessionMap;
	}

	public void setHttpSessionMap(Map<String, String> httpSessionMap) {
		this.httpSessionMap = httpSessionMap;
	}

	public Map<String, String> getHttpCookieMap() {
		return httpCookieMap;
	}

	public void setHttpCookieMap(Map<String, String> httpCookieMap) {
		this.httpCookieMap = httpCookieMap;
	}

	public Map<String, String> getHttpParamMap() {
		return httpParamMap;
	}

	public void setHttpParamMap(Map<String, String> httpParamMap) {
		this.httpParamMap = httpParamMap;
	}

	public Map<String, String> getHttpAttributeMap() {
		return httpAttributeMap;
	}

	public void setHttpAttributeMap(Map<String, String> httpAttributeMap) {
		this.httpAttributeMap = httpAttributeMap;
	}
	
}
