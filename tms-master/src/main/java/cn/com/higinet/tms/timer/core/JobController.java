package cn.com.higinet.tms.timer.core;

import java.util.List;
import java.util.Map;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.util.Classz;
import cn.com.higinet.tms.base.util.Stringz;

@RestController
@RequestMapping(value = "/demo/quartz")
public class JobController {

	private static final Logger logger = LoggerFactory.getLogger( JobController.class );

	@Autowired
	@Qualifier("Scheduler")
	private Scheduler scheduler;

	/**
	 * 新增任务
	 * */
	@PostMapping("/add")
	public Model add( @RequestBody JobEntity detail ) throws Exception {
		String jobName = detail.getJobName();
		String jobClassName = detail.getJobClassName();
		String jobGroupName = detail.getJobGroupName();
		String cronExpression = detail.getCronExpression();
		if( Stringz.isEmpty( jobName, jobClassName, jobGroupName, cronExpression ) ) return new Model().addError( "param is empty" );

		if( checkExists( jobName, jobGroupName ) ) {
			return new Model().addError( "任务已存在" );
		}

		// 启动调度器  
		scheduler.start();
		//构建job信息
		JobDetail jobDetail = JobBuilder.newJob( getClass( jobClassName ).getClass() ).withIdentity( jobName, jobGroupName ).build();
		//表达式调度构建器(即任务执行的时间)
		CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule( cronExpression );
		//按新的cronExpression表达式构建一个新的trigger
		CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity( jobName, jobGroupName ).withSchedule( scheduleBuilder ).build();
		//创建任务
		scheduler.scheduleJob( jobDetail, trigger );
		return new Model();
	}

	/**
	 * 暂停任务
	 * */
	@RequestMapping(value = "/pause", method = RequestMethod.POST)
	public Model pause( @RequestBody JobEntity detail ) throws Exception {
		String jobName = detail.getJobName();
		String jobGroupName = detail.getJobGroupName();
		if( Stringz.isEmpty( jobName, jobGroupName ) ) new Model().addError( "param is empty" );

		scheduler.pauseJob( JobKey.jobKey( jobName, jobGroupName ) );
		return new Model();
	}

	@RequestMapping(value = "/resume", method = RequestMethod.POST)
	public Model resume( @RequestBody JobEntity detail ) throws Exception {
		String jobName = detail.getJobName();
		String jobGroupName = detail.getJobGroupName();
		if( Stringz.isEmpty( jobName, jobGroupName ) ) new Model().addError( "param is empty" );

		scheduler.resumeJob( JobKey.jobKey( jobName, jobGroupName ) );
		return new Model();
	}

	/**
	 * 修改任务
	 * */
	@RequestMapping(value = "/reschedule", method = RequestMethod.POST)
	public Model reschedule( @RequestBody JobEntity detail ) throws Exception {
		String jobName = detail.getJobName();
		String jobGroupName = detail.getJobGroupName();
		String cronExpression = detail.getCronExpression();
		if( Stringz.isEmpty( jobName, jobGroupName, cronExpression ) ) throw new Exception( "param is empty" );

		TriggerKey triggerKey = TriggerKey.triggerKey( jobName, jobGroupName );
		// 表达式调度构建器
		CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule( cronExpression );
		CronTrigger trigger = (CronTrigger) scheduler.getTrigger( triggerKey );
		// 按新的cronExpression表达式重新构建trigger
		trigger = trigger.getTriggerBuilder().withIdentity( triggerKey ).withSchedule( scheduleBuilder ).build();
		// 按新的trigger重新设置job执行
		scheduler.rescheduleJob( triggerKey, trigger );
		return new Model();
	}

	/**
	 * 删除任务
	 * */
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public Model delete( @RequestBody JobEntity detail ) throws Exception {
		String jobName = detail.getJobName();
		String jobGroupName = detail.getJobGroupName();
		if( Stringz.isEmpty( jobName, jobGroupName ) ) new Model().addError( "param is empty" );

		TriggerKey triggerKey = TriggerKey.triggerKey( jobName, jobGroupName );
		if( scheduler.checkExists( triggerKey ) ) {
			scheduler.pauseTrigger( triggerKey );
			scheduler.unscheduleJob( triggerKey );
			scheduler.deleteJob( JobKey.jobKey( jobName, jobGroupName ) );
		}
		return new Model();
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/job_class_list", method = RequestMethod.GET)
	public Model jobclasslist() throws Exception {
		List<Map<String, String>> resultList = Lists.newArrayList();
		List<Class<?>> list = Classz.getAllClassByInterface( TmsJob.class, "cn.com.higinet.tms", "cn.com.higinet.tms.engine", "cn.com.higinet.tms.base" );
		if( list != null ) {
			for( Class<?> _class : list ) {
				Map<String, String> map = Maps.newHashMap();
				TmsJob tmsjob = ((Class<TmsJob>) _class).newInstance();
				map.put( "label", tmsjob.getJobName() );
				map.put( "value", tmsjob.getClass().getName() );
				resultList.add( map );
			}
		}
		return new Model().setList( resultList );
	}

	/** 
	 * 验证是否存在 
	 * @param jobName 
	 * @param jobGroupName 
	 */
	private boolean checkExists( String jobName, String jobGroupName ) throws Exception {
		TriggerKey triggerKey = TriggerKey.triggerKey( jobName, jobGroupName );
		return scheduler.checkExists( triggerKey );
	}

	private Job getClass( String classname ) throws Exception {
		Class<?> class1 = Class.forName( classname );
		return (Job) class1.newInstance();
	}
}
