package cn.com.higinet.tms.timer.core;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.entity.common.SelectItem;
import cn.com.higinet.tms.base.util.Classz;
import cn.com.higinet.tms.base.util.Stringz;

@RestController
@RequestMapping(value = "/timer")
public class JobController {

	private static final Logger logger = LoggerFactory.getLogger( JobController.class );

	@Autowired
	@Qualifier("Scheduler")
	private Scheduler scheduler;

	/**
	 * 新增任务
	 * */
	@PostMapping("/add")
	public Model add( @RequestBody TaskEntity task ) throws Exception {
		Model model = new Model();
		String name = task.getName();
		String taskId = Stringz.isNotEmpty( task.getTaskId() ) ? task.getTaskId() : Stringz.randomUUID();
		String className = task.getClassName();
		String group = task.getGroup();
		String cron = task.getCron();
		String description = task.getDescription();

		if( Stringz.isEmpty( name ) ) model.addError( "name为空" );
		if( Stringz.isEmpty( className ) ) model.addError( "className为空" );
		if( Stringz.isEmpty( cron ) ) model.addError( "cron为空" );
		if( Stringz.isEmpty( CronExpression.isValidExpression( cron ) ) ) model.addError( "cron表达式错误" );
		if( checkExists( taskId ) ) model.addError( "任务已存在" );
		if( model.hasError() ) return model;

		// 启动调度器  
		if( !scheduler.isStarted() ) scheduler.start();

		//构建job信息
		JobBuilder jobBuilder = JobBuilder.newJob( getClass( className ).getClass() );
		jobBuilder = jobBuilder.withIdentity( taskId );
		JobDetail jobDetail = jobBuilder.build();

		//表达式调度构建器(即任务执行的时间)
		CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule( cron );

		//taskName与简介信息以JSON形式存放在trigger Description中
		JSONObject json = new JSONObject();
		json.put( "name", name );
		json.put( "description", description );
		json.put( "group", group );

		//按新的cronExpression表达式构建一个新的trigger
		TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
		triggerBuilder = triggerBuilder.withIdentity( taskId );
		triggerBuilder = triggerBuilder.withDescription( json.toString() );
		CronTrigger trigger = triggerBuilder.withSchedule( scheduleBuilder ).build();

		//创建任务
		Date result = scheduler.scheduleJob( jobDetail, trigger );
		return model.setRow( result );
	}

	/**
	 * 修改任务
	 * */
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public Model update( @RequestBody TaskEntity task ) throws Exception {
		Model model = new Model();
		String name = task.getName();
		String taskId = task.getTaskId();
		String group = task.getGroup();
		String cron = task.getCron();
		String className = task.getClassName();
		String description = task.getDescription();
		if( Stringz.isEmpty( name, taskId, cron, className ) ) model.addError( "param is empty" );
		if( Stringz.isEmpty( CronExpression.isValidExpression( cron ) ) ) model.addError( "cron表达式错误" );
		if( scheduler.checkExists( TriggerKey.triggerKey( taskId ) ) ) model.addError( "任务不存在" );
		if( model.hasError() ) return model;

		JSONObject descJson = new JSONObject();
		descJson.put( "name", name );
		descJson.put( "description", description );
		descJson.put( "group", group );

		TriggerKey triggerKey = TriggerKey.triggerKey( taskId );
		// 表达式调度构建器
		CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule( cron );
		CronTrigger trigger = (CronTrigger) scheduler.getTrigger( triggerKey );
		//获得当前定时任务的执行className
		String oldClassName = scheduler.getJobDetail( trigger.getJobKey() ).getJobClass().getName();

		//假如class没有变动，那只做trigger重新设定
		if( className.equals( oldClassName ) ) {
			//按新的cronExpression表达式重新构建trigger
			trigger = trigger.getTriggerBuilder().withIdentity( triggerKey ).withDescription( descJson.toString() ).withSchedule( scheduleBuilder ).build();
			//按新的trigger重新设置job执行
			scheduler.rescheduleJob( triggerKey, trigger );
		}
		//否则删除任务，重新添加
		else {
			scheduler.pauseTrigger( triggerKey ); //停止任务
			scheduler.unscheduleJob( triggerKey ); //停用任务
			scheduler.deleteJob( JobKey.jobKey( taskId ) ); //删除任务

			//构建新Job信息
			JobBuilder jobBuilder = JobBuilder.newJob( getClass( className ).getClass() );
			jobBuilder = jobBuilder.withIdentity( taskId );
			JobDetail jobDetail = jobBuilder.build();
			//构建一个新的trigger
			TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
			triggerBuilder = triggerBuilder.withIdentity( taskId );
			triggerBuilder = triggerBuilder.withDescription( descJson.toString() );
			CronTrigger newTrigger = triggerBuilder.withSchedule( scheduleBuilder ).build();
			//创建任务
			scheduler.scheduleJob( jobDetail, newTrigger );
		}

		return model;
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Model list() throws Exception {
		List<TaskEntity> taskList = Lists.newArrayList();

		List<Trigger> list = this.getTriggerList();
		for( Trigger trigger : list ) {
			//taskName与简介信息以JSON形式存放在trigger Description中
			JSONObject json = new JSONObject();
			String jsonMap = trigger.getDescription();
			if( Stringz.isNotEmpty( jsonMap ) ) {
				try {
					json = JSON.parseObject( jsonMap );
				}
				catch( Exception e ) {
				}
			}

			String name = json.getString( "name" );
			String taskId = trigger.getJobKey().getName();
			String group = json.getString( "group" );
			String description = json.getString( "description" );
			String status = scheduler.getTriggerState( trigger.getKey() ).name();
			String className = scheduler.getJobDetail( trigger.getJobKey() ).getJobClass().getName();
			String cron = "";
			Date previousFireTime = null;
			Date nextFireTime = null;

			if( trigger instanceof CronTrigger ) {
				CronTrigger cronTrigger = (CronTrigger) trigger;
				cron = cronTrigger.getCronExpression();
				previousFireTime = cronTrigger.getPreviousFireTime();
				nextFireTime = cronTrigger.getNextFireTime();
			}

			TaskEntity task = new TaskEntity();
			task.setName( name ); //执行名称
			task.setTaskId( taskId ); //任务名称
			task.setGroup( group ); //任务分组名
			task.setClassName( className ); //任务Class名
			task.setDescription( description ); //任务说明
			task.setCron( cron ); //执行表达式
			task.setStatus( status ); //任务状态
			task.setPreviousFireTime( previousFireTime ); //上一次执行时间
			task.setNextFireTime( nextFireTime ); //下一次执行时间
			taskList.add( task );
		}

		return new Model().setList( taskList );
	}

	@RequestMapping(value = "/check-name", method = RequestMethod.POST)
	public Model checkName( @RequestBody TaskEntity task ) throws Exception {
		if( Stringz.isEmpty( task.getName() ) ) return new Model().addError( "name is empty" );

		List<Trigger> list = this.getTriggerList();
		for( Trigger trigger : list ) {
			try {
				String description = trigger.getDescription();
				if( Stringz.isEmpty( description ) ) continue;
				JSONObject json = JSON.parseObject( description );
				if( task.getName().equals( json.getString( "name" ) ) ) {
					return new Model().setRow( false );
				}
			}
			catch( Exception e ) {
				logger.error( e.getMessage(), e );
			}
		}
		return new Model().setRow( true );
	}

	@RequestMapping(value = "/check-cron", method = RequestMethod.POST)
	public Model checkCron( @RequestBody TaskEntity task ) throws Exception {
		if( Stringz.isEmpty( task.getCron() ) ) return new Model().addError( "cron为空" );
		return new Model().setRow( CronExpression.isValidExpression( task.getCron() ) );
	}

	@RequestMapping(value = "/groups", method = RequestMethod.GET)
	public Model jobGroupNames() {
		List<SelectItem> list = Lists.newArrayList();
		list.add( new SelectItem( "1", "业务处理" ) );
		list.add( new SelectItem( "2", "数据抽取" ) );
		list.add( new SelectItem( "3", "数据处理" ) );
		list.add( new SelectItem( "4", "其他" ) );
		return new Model().setList( list );
	}

	/**
	 * 暂停任务
	 * */
	@RequestMapping(value = "/pause", method = RequestMethod.POST)
	public Model pause( @RequestBody TaskEntity task ) throws Exception {
		String taskId = task.getTaskId();
		if( Stringz.isEmpty( taskId ) ) new Model().addError( "param is empty" );

		scheduler.pauseJob( JobKey.jobKey( taskId ) );
		return new Model();
	}

	/**
	 * 恢复启动任务
	 * */
	@RequestMapping(value = "/resume", method = RequestMethod.POST)
	public Model resume( @RequestBody TaskEntity task ) throws Exception {
		String taskId = task.getTaskId();
		if( Stringz.isEmpty( taskId ) ) new Model().addError( "param is empty" );
		scheduler.resumeJob( JobKey.jobKey( taskId ) );
		return new Model();
	}

	/**
	 * 删除任务
	 * */
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public Model delete( @RequestBody TaskEntity task ) throws Exception {
		String taskId = task.getTaskId();
		if( Stringz.isEmpty( taskId ) ) new Model().addError( "param is empty" );

		TriggerKey triggerKey = TriggerKey.triggerKey( taskId );
		if( scheduler.checkExists( triggerKey ) ) {
			scheduler.pauseTrigger( triggerKey );
			scheduler.unscheduleJob( triggerKey );
			scheduler.deleteJob( JobKey.jobKey( taskId ) );
		}
		return new Model();
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/classes", method = RequestMethod.GET)
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
	 * @param taskId 
	 */
	private boolean checkExists( String taskId ) throws Exception {
		TriggerKey triggerKey = TriggerKey.triggerKey( taskId );
		return scheduler.checkExists( triggerKey );
	}

	/**
	 * 取得Class实例
	 * */
	private Job getClass( String classname ) throws Exception {
		Class<?> class1 = Class.forName( classname );
		return (Job) class1.newInstance();
	}

	/**
	 * 获取系统当前所有Trigger
	 * */
	public List<Trigger> getTriggerList() throws Exception {
		List<Trigger> list = Lists.newArrayList();
		for( String groupJob : scheduler.getJobGroupNames() ) {
			for( JobKey jobKey : scheduler.getJobKeys( GroupMatcher.<JobKey> groupEquals( groupJob ) ) ) {
				List<? extends Trigger> triggers = scheduler.getTriggersOfJob( jobKey );
				for( Trigger trigger : triggers ) {
					list.add( trigger );
				}
			}
		}
		return list;
	}
}
