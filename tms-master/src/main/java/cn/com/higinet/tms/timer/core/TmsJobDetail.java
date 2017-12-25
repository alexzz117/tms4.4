package cn.com.higinet.tms.timer.core;

public class TmsJobDetail {
	String jobName;
	String jobClassName;
	String jobGroupName;
	String cronExpression;

	public String getJobName() {
		return jobName;
	}

	public void setJobName( String jobName ) {
		this.jobName = jobName;
	}

	public String getJobClassName() {
		return jobClassName;
	}

	public void setJobClassName( String jobClassName ) {
		this.jobClassName = jobClassName;
	}

	public String getJobGroupName() {
		return jobGroupName;
	}

	public void setJobGroupName( String jobGroupName ) {
		this.jobGroupName = jobGroupName;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression( String cronExpression ) {
		this.cronExpression = cronExpression;
	}
}
