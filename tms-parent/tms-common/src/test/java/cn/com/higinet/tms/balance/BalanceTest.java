package cn.com.higinet.tms.balance;

import org.junit.Test;

import cn.com.higinet.tms.common.balancer.BalancerContext;
import cn.com.higinet.tms.common.balancer.server.ServerInfo;
import cn.com.higinet.tms.common.balancer.server.ServerUtils;
import cn.com.higinet.tms.common.balancer.Balancer;

public class BalanceTest {
	@Test
	public void balance() {
		Balancer<ServerInfo> m = new Balancer<>();
		String servers = "127.0.0.1:1,127.0.0.1:2,1.1.1.1:3";
		m.setAvailableTargets(ServerUtils.stringToServerArray(servers));
		m.setCalculateThreshold(32);
		m.start();
		BalancerContext<ServerInfo> bctx = new BalancerContext<>();
		bctx.setBalanceID(String.valueOf(Math.random() * 100000000));
		ServerInfo s = m.chooseTarget(bctx);
		System.out.println(s);
		s = m.chooseTarget(bctx);
		System.out.println(s);
		s = m.chooseTarget(bctx);
		System.out.println(s);
		System.out.println(bctx.getCurrentCalculateCount());
	}
}
