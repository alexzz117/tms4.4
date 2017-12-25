package cn.com.higinet.tms.engine.comm;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

/**
 * 请求对象的默认实现
 * @author chenr, 2011-5-26
 * @version 2.1.0
 */
public class DefaultRequestImpl implements Request {

	private Message message;
	
	public DefaultRequestImpl(Message message){
		this.message = message;
	}
	
	public String getServiceCode() {
		return (String)message.getHead(Message.HEAD_SERVICE_CODE);
	}

	public String getTransactionCode() {
		return (String)message.getHead(Message.HEAD_TRANSACTION_CODE);
	}

	public String getContentType() {
		return (String)message.getHead(Message.HEAD_CONTENT_TAYPE);
	}

	public int getContentLength() {
		return Integer.parseInt((String)message.getHead(Message.HEAD_CONTENT_LENGTH));
	}

	public String getHead(String name) {
		return (String)message.getHead(name);
	}

	public Object getParameter(String name) {
		return message.getData(name);
	}

	public Map<String, Object> getParameterMap() {
		return Collections.unmodifiableMap(message.getData());
	}

	public Enumeration<String> getParameterNames() {
		Map<String, Object> data = message.getData();
		return new Vector<String>(data.keySet()).elements();
	}
	public Enumeration<String> getHeadNames(){
		Map<String, Object> head = message.getHead();
		return new Vector<String>(head.keySet()).elements();
	}
	public Map<String, Object> getHeadMap() {
		return Collections.unmodifiableMap(message.getHead());
	}

}
