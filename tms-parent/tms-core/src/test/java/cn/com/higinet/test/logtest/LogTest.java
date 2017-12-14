package cn.com.higinet.test.logtest;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogTest {
//	@Test
	public void slf4jLog4jTest() {
		Logger log = LoggerFactory.getLogger(LogTest.class);
		Object[] a = new Object[] { new Object(), new Object() };
		log.info("{}", a);
		List<Object> l = new ArrayList<Object>();
		l.add(new Object());
		l.add(new Object());
		log.info("{}", l);
	}

//	@Test
	public void log4jTest() {
		org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LogTest.class);
		Object[] a = new Object[] { new Object(), new Object() };
		log.info(a);
		List<Object> l = new ArrayList<Object>();
		l.add(new Object());
		l.add(new Object());
		log.info(l);
	}
}
