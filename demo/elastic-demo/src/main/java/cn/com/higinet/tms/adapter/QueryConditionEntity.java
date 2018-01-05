package cn.com.higinet.tms.adapter;

import org.springframework.stereotype.Component;

@Component
public class QueryConditionEntity {
	
	/**
	 * 要查询的属性名字
	 */
	private String name;
	
	/**
	 * 起始值 说明：queryType=OTHER时，字段值放到startValue中,当queryType=ORDERBY时，字段值为ASC 或者 DESC
	 */
	private Object startValue;
	
	/**
	 * 结束值
	 */
	private Object endValue;
	
	/**
	 * 起始查询符号 说明：LIKE模糊匹配 LIKE_前缀查询，EQ精确查询，GTE大于等于，GT大于
	 */
	private ConditionMarkEnum startMark;
	
	/**
	 * 结束查询符号说明：LTE小于等于，LT小于
	 */
	private ConditionMarkEnum endMark;
	
	/**
	 * queryType OTHER:代表条件查询非区间查询(例如=或者like) ,BETWEEN:代表区间查询 ,ORDERBY:代表排序
	 */
	private ConditionMarkEnum queryType;
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getStartValue() {
		return startValue;
	}

	public void setStartValue(Object startValue) {
		this.startValue = startValue;
	}

	public Object getEndValue() {
		return endValue;
	}

	public void setEndValue(Object endValue) {
		this.endValue = endValue;
	}

	public ConditionMarkEnum getStartMark() {
		return startMark;
	}

	public void setStartMark(ConditionMarkEnum startMark) {
		this.startMark = startMark;
	}

	public ConditionMarkEnum getEndMark() {
		return endMark;
	}

	public void setEndMark(ConditionMarkEnum endMark) {
		this.endMark = endMark;
	}

	public ConditionMarkEnum getQueryType() {
		return queryType;
	}

	public void setQueryType(ConditionMarkEnum queryType) {
		this.queryType = queryType;
	}

	
	
	
	
}
