/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  ServerKeeper.java   
 * @Package cn.com.higinet.tms.common.balancer   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2018-1-19 17:38:25   
 * @version V1.0 
 * @Copyright: 2018 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.balancer;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * 保管ServerInfo的类，有读写锁控制整个serverinfo
 *
 * @ClassName:  ServerKeeper
 * @author: 王兴
 * @date:   2018-1-19 17:38:25
 * @since:  v4.4
 */
public class TargetKeeper<T> {
	private T[] targets ;

	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	private ReadLock readLock = lock.readLock();

	private WriteLock writeLock = lock.writeLock();

	public boolean isEmpty() {
		return targets == null || targets.length == 0;
	}

	public int availableTargetCount() {
		return getAvailableTargets().length;
	}

	public T[] getAvailableTargets() {
		readLock.lock();
		try {
			return targets;
		} finally {
			readLock.unlock();
		}
	}

	public T getAvailableTarget(int i) {
		readLock.lock();
		try {
			if (i > targets.length) {
				return null;
			}
			return targets[i];
		} finally {
			readLock.unlock();
		}
	}

	public void setAvailableTargets(T[] targets) {
		writeLock.lock();
		try {
			this.targets = targets;
		} finally {
			writeLock.unlock();
		}
	}

	public void clear() {
		targets = null;
	}

}
