package cn.com.higinet.tms.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cn.com.higinet.tms.common.util.ClockUtils;
import cn.com.higinet.tms.common.util.ClockUtils.Clock;

public class ListTest {
	private static final int size = 10000000;

	@Test
	public void itorTest() {
		List<Integer> il = new ArrayList<Integer>(size);
		fillList(il);
		Clock c = ClockUtils.createClock();
		for (Integer i : il) {
			
		}
		System.out.println("itr:" + c.countMicros());
	}

	@Test
	public void listTest() {
		List<Integer> il = new ArrayList<Integer>(size);
		fillList(il);
		Clock c = ClockUtils.createClock();
		for (int i = 0; i < size; i++) {
			il.get(i);
		}
		System.out.println("old:" + c.countMicros());
	}

	public void fillList(List<Integer> l) {
		for (int i = 0; i < size; i++) {
			l.add(i);
		}
	}
}
