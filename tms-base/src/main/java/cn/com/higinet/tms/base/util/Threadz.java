package cn.com.higinet.tms.base.util;

public class Threadz {
	public static Thread[] getAllThread(){
		ThreadGroup group = Thread.currentThread().getThreadGroup();
		ThreadGroup topGroup = group;

		/* 遍历线程组树，获取根线程组 */
		while (group != null) {
			topGroup = group;
			group = group.getParent();
		}
		/* 激活的线程数加倍 */
		int estimatedSize = topGroup.activeCount() * 2;
		Thread[] slackList = new Thread[estimatedSize];

		/* 获取根线程组的所有线程 */
		int actualSize = topGroup.enumerate( slackList );
		/* copy into a list that is the exact size */
		Thread[] threadList = new Thread[actualSize];
		System.arraycopy( slackList, 0, threadList, 0, actualSize );
		
		return threadList;
	}
}
