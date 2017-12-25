package cn.com.higinet.tms.timer.core;

import org.quartz.Job;

/**
 * TMS定时任务接口
 * @author zhang.lei
 * */

public interface TmsJob extends Job {
	
	public String getJobName();
	
}
