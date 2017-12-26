package cn.com.higinet.tms.timer.core;

import java.util.Date;

public class TaskEntity {

	private String name;
	private String taskId;
	private String group;
	private String className;
	private String description;
	private String status;
	private String cron;
	private Date createTime;
	private Date previousFireTime;
	private Date nextFireTime;

	public String getGroup() {
		return group;
	}

	public void setGroup( String group ) {
		this.group = group;
	}

	public String getCron() {
		return cron;
	}

	public void setCron( String cron ) {
		this.cron = cron;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId( String taskId ) {
		this.taskId = taskId;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName( String className ) {
		this.className = className;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime( Date createTime ) {
		this.createTime = createTime;
	}

	public Date getPreviousFireTime() {
		return previousFireTime;
	}

	public void setPreviousFireTime( Date previousFireTime ) {
		this.previousFireTime = previousFireTime;
	}

	public Date getNextFireTime() {
		return nextFireTime;
	}

	public void setNextFireTime( Date nextFireTime ) {
		this.nextFireTime = nextFireTime;
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
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

}
