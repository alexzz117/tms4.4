package cn.com.higinet.tms.timer.core;

import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import cn.com.higinet.tms.manager.dao.SimpleDao;

@Component
public class TimerRunner implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger( TimerRunner.class );

	@Autowired
	@Qualifier("offlineSimpleDao")
	SimpleDao offlineSimpleDao;

	@Autowired
	@Qualifier("Scheduler")
	Scheduler scheduler;

	@Override
	public void run( String... arg0 ) throws Exception {
		//offlineSimpleDao.queryForList( "select * from ", params )
		/*List<TaskEntity> taskList = Lists.newArrayList();
		
		for( String groupJob : scheduler.getJobGroupNames() ) {
			for( JobKey jobKey : scheduler.getJobKeys( GroupMatcher.<JobKey> groupEquals( groupJob ) ) ) {
				List<? extends Trigger> triggers = scheduler.getTriggersOfJob( jobKey );
				for( Trigger trigger : triggers ) {
					Trigger.TriggerState triggerState = scheduler.getTriggerState( trigger.getKey() );
					JobDetail jobDetail = scheduler.getJobDetail( jobKey );

					String cronExpression = "";
					String createTime = "";

					if( trigger instanceof CronTrigger ) {
						CronTrigger cronTrigger = (CronTrigger) trigger;
						cronExpression = cronTrigger.getCronExpression();
						createTime = cronTrigger.getDescription();
					}
					
					TaskEntity task = new TaskEntity();
					task.setName( jobKey.getName() );
					task.setGroup( jobKey.getGroup() );
					task.setDescription( jobDetail.getDescription() );
					task.setCronExpression( cronExpression );
					task.setCreateTime( createTime );
					taskList.add( task );
				}
			}
		}*/
	}

}
