package cn.com.higinet.tms.common;

import org.junit.Test;

import cn.com.higinet.tms.common.util.ClockUtils;
import cn.com.higinet.tms.common.util.ClockUtils.Clock;
import cn.com.higinet.tms.common.util.CompressUtils;

public class CompressTest extends BaseConcurrentTest {
	private static final int size = 100;

	@Test
	public void jzTest() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(i);
		}
		byte[] b = sb.toString().getBytes();
		Clock c = ClockUtils.createClock();
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			CompressUtils.jzlib(b);
		}
		long t = c.countMillis();
		System.out.println("j:" + DEFAULT_TIMES / t * 1000);
		System.out.println("j:" + t);
	}

	@Test
	public void gzTest() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(i);
		}
		byte[] b = sb.toString().getBytes();
		Clock c = ClockUtils.createClock();
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			CompressUtils.gZip(b);
		}
		long t = c.countMillis();
		System.out.println("g:" + DEFAULT_TIMES / t * 1000);
		System.out.println("g:" + t);
	}

	@Test
	public void zzTest() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(i);
		}
		byte[] b = sb.toString().getBytes();
		Clock c = ClockUtils.createClock();
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			CompressUtils.zip(b);
		}
		long t = c.countMillis();
		System.out.println("z:" + DEFAULT_TIMES / t * 1000);
		System.out.println("z:" + t);
	}

	@Test
	public void lz4Test() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(i);
		}
		byte[] b = sb.toString().getBytes();
		Clock c = ClockUtils.createClock();
		byte[] a = null;
		for (int i = 0; i < DEFAULT_TIMES; i++) {
			a = CompressUtils.lz4(b);
		}
		long t = c.countMillis();
		System.out.println("lz4:" + DEFAULT_TIMES / t * 1000);
		System.out.println("lz4:" + t);
	}

}
