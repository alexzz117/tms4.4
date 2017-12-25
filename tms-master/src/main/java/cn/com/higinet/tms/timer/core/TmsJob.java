package cn.com.higinet.tms.timer.core;

import org.quartz.Job;

/**
 * TMS定时任务接口
 * 系统将会以此接口进行Job实现类扫描，加入至可选任务
 * @author zhang.lei
 * */

public interface TmsJob extends Job {
	
	public String getJobName();
	
}
