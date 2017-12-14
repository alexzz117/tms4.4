package cn.com.higinet.tms.core.survivor;

import cn.com.higinet.tms.core.model.OperatCycle;

public class EventContext {

	private boolean isSurviver = false;
	private OperatCycle model;
	private String runId;
	private String head;
	private String body;
	private int currentFailureTimes = -1; // 当前失败次数
	private long beginTime; // 事故发生时间，如果要按时间来计算本消息失效的话，可以用这个参数
	private int retryTimes = 5; // 重试次数，目前没有开放配置
	private int timeout = 5000;// 重发时候的超时时间，这个可以设置长一点，提高重发准确性
	private int connTimeOut = 2000;// 重发时候的超时时间，这个可以设置长一点，提高重发准确性

	public boolean isSurviver() {
		return isSurviver;
	}

	public void setSurviver(boolean isSurviver) {
		this.isSurviver = isSurviver;
	}

	public OperatCycle getModel() {
		return model;
	}

	public void setModel(OperatCycle model) {
		this.model = model;
	}

	public String getRunId() {
		return runId;
	}

	public void setRunId(String runId) {
		this.runId = runId;
	}

	public String getHead() {
		return head;
	}

	public void setHead(String head) {
		this.head = head;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public int getCurrentFailureTimes() {
		return currentFailureTimes;
	}

	public void setCurrentFailureTimes(int currentFailureTimes) {
		this.currentFailureTimes = currentFailureTimes;
	}

	public long getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}

	public int getRetryTimes() {
		return retryTimes;
	}

	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getConnTimeOut() {
		return connTimeOut;
	}

	public void setConnTimeOut(int connTimeOut) {
		this.connTimeOut = connTimeOut;
	}

}
