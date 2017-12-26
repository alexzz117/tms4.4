package cn.com.higinet.tms.timer.core;

import java.util.Date;

public class TaskEntity {

	private String name;
	private String jobName;
	private String groupName;
	private String className;
	private String description;
	private String status;
	private String cronExpression;
	private Date createTime;
	private Date previousFireTime;
	private Date nextFireTime;

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

	public String getJobName() {
		return jobName;
	}

	public void setJobName( String jobName ) {
		this.jobName = jobName;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName( String groupName ) {
		this.groupName = groupName;
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

}
