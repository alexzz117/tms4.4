package cn.com.higinet.tms.timer.core;

public class TaskEntity {
	
	String name;
	String group;
	String description;
	String status;
	String cronExpression;
	String createTime;
	
	public String getName() {
		return name;
	}
	public void setName( String name ) {
		this.name = name;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup( String group ) {
		this.group = group;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription( String description ) {
		this.description = description;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus( String status ) {
		this.status = status;
	}
	public String getCronExpression() {
		return cronExpression;
	}
	public void setCronExpression( String cronExpression ) {
		this.cronExpression = cronExpression;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime( String createTime ) {
		this.createTime = createTime;
	}

}
