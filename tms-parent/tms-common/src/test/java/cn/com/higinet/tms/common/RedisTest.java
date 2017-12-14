package cn.com.higinet.tms.common;

import org.junit.Test;

import cn.com.higinet.tms.common.util.ClockUtils;
import cn.com.higinet.tms.common.util.ClockUtils.Clock;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class RedisTest {
	@Test
	public void pipelineTest() {
		Jedis jedis = new Jedis("192.168.10.189", 6379);
		jedis.auth("hjhx.123456");
		Clock c = ClockUtils.createClock();
		Pipeline p = jedis.pipelined();
		for (int i = 0; i < 1000; i++) {
			p.set("p" + i, String.valueOf(i));
		}
		p.sync();
		jedis.close();
		System.out.println("p:" + c.countMillis());
	}

	@Test
	public void addTest() {
		Jedis jedis = new Jedis("192.168.10.189", 6379);
		jedis.auth("hjhx.123456");
		Clock c = ClockUtils.createClock();
		for (int i = 0; i < 1000; i++) {
			jedis.set("t" + i, String.valueOf(i));
		}
		jedis.close();
		System.out.println("t:" + c.countMillis());
	}
}
