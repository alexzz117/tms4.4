package cn.com.higinet.tms.test;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import cn.com.higinet.tms.loadrunner.KfkProducer;

public class KfkTest implements Runnable{

	private AtomicLong count = new AtomicLong();
	private static int time = 600000;
	private static int threads = 10;
	private CountDownLatch c = new CountDownLatch(threads);
	@Override
	public void run() {
		long curTime = System.currentTimeMillis();
		while(System.currentTimeMillis() - curTime < time){
			try{
				KfkProducer.send();
				count.getAndIncrement();
			}catch(Exception e){
				e.printStackTrace();
				
			}
		}
		c.countDown();
	}
	
	public static void main(String arg[]){
		 System.setProperty("bootstrap.servers", "192.168.10.190:9092,192.168.10.191:9092");
		 System.setProperty("topic", "tms");
		 System.setProperty("partions", "2");
		
		KfkTest test = new KfkTest();
		while(threads > 0){
			threads--;
			new Thread(test).start();
		}
		try {
			test.c.await();
			long total = test.count.get();
			System.out.println("总数：" + total);
			BigDecimal b = new BigDecimal(Double.toString(total));
			BigDecimal c = new BigDecimal(Double.toString(time/1000));
			System.out.println("TPS:" + b.divide(c,3,BigDecimal.ROUND_HALF_UP).doubleValue());
			System.out.println("RESPONSE_TIME:" + c.divide(b,6,BigDecimal.ROUND_HALF_UP).doubleValue());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
