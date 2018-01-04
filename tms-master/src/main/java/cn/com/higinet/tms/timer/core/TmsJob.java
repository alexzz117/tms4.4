package cn.com.higinet.tms.timer.core;

import org.quartz.Job;

/**
 * TMS定时任务接口
 * 系统将会以此接口进行Job实现类扫描，加入至可选任务
 * @author zhang.lei
 * */

public interface TmsJob extends Job {
	
	/**
	 * 返回任务名称
	 * 只将任务动作简单描述，切勿带入执行时间
	 * */
	public String getJobName();
	
}
