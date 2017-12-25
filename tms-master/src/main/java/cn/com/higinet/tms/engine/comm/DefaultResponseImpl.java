package cn.com.higinet.tms.engine.comm;

import java.util.Collections;
import java.util.Map;

/**
 * 响应对象的默认实现
 * 
 * @author chenr, 2011-5-26
 * @version 2.1.0
 *
 */
public class DefaultResponseImpl implements Response {
	
	private Message message;
	
	public DefaultResponseImpl(Message message){
		this.message = new Message();
		this.message.setHead(Message.HEAD_CONTENT_TAYPE, message.getHead(Message.HEAD_CONTENT_TAYPE));
		this.message.setHead(Message.HEAD_SERVICE_CODE, message.getHead(Message.HEAD_SERVICE_CODE));
		this.message.setHead(Message.HEAD_TRANSACTION_CODE, message.getHead(Message.HEAD_TRANSACTION_CODE));
	}
	
	public String getReturnCode() {
		return (String) message.getHead(Message.HEAD_RETURN_CODE);
	}

	public void setContentType(String type) {
		message.setHead(Message.HEAD_CONTENT_TAYPE, type);
	}

	public String getContentType() {
		return (String) message.getHead(Message.HEAD_CONTENT_TAYPE);
	}

	public void setData(String name, Object value) {
		message.setData(name, value);
	}
	
	public void setReturnCode(String code) {
		message.setHead(Message.HEAD_RETURN_CODE, code);
	}

	public Object getData(String name) {
		return message.getData(name);
	}

	public void removeData(String name) {
		message.removeData(name);
		
	}
	public Message getMessage(){
		return message;
	}

	public Map<String, Object> getDataMap() {
		return Collections.unmodifiableMap(message.getData());
	}

	public Map<String, Object> getHeadMap() {
		return Collections.unmodifiableMap(message.getHead());
	}
}
