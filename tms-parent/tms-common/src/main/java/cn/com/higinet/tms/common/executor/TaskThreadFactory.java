/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.com.higinet.tms.common.executor;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * tomcat封装的TaskThread对象的工厂类.
 *
 * @ClassName:  TaskThreadFactory
 * @author: michael
 * @date:   2017-5-8 18:22:51
 * @since:  v4.3
 */
public class TaskThreadFactory implements ThreadFactory {

	/** group. */
	private final ThreadGroup group;

	/** thread number. */
	private final AtomicInteger threadNumber = new AtomicInteger(1);

	/** name prefix. */
	private final String namePrefix;

	/** daemon. */
	private final boolean daemon;

	/** thread priority. */
	private final int threadPriority;

	/**
	 * 构造一个新的对象.
	 *
	 * @param namePrefix the name prefix
	 * @param daemon the daemon
	 * @param priority the priority
	 */
	public TaskThreadFactory(String namePrefix, boolean daemon, int priority) {
		SecurityManager s = System.getSecurityManager();
		group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
		this.namePrefix = namePrefix;
		this.daemon = daemon;
		this.threadPriority = priority;
	}

	/**
	 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
	 */
	@Override
	public Thread newThread(Runnable r) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			// Threads should not be created by the webapp classloader
			if (System.getSecurityManager() != null) {
				PrivilegedAction<Void> pa = new PrivilegedSetTccl(getClass().getClassLoader());
				AccessController.doPrivileged(pa);
			} else {
				Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			}
			TaskThread t = new TaskThread(group, r, namePrefix + threadNumber.getAndIncrement());
			t.setDaemon(daemon);
			t.setPriority(threadPriority);
			return t;
		} finally {
			if (System.getSecurityManager() != null) {
				PrivilegedAction<Void> pa = new PrivilegedSetTccl(loader);
				AccessController.doPrivileged(pa);
			} else {
				Thread.currentThread().setContextClassLoader(loader);
			}
		}
	}
}
