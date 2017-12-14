package cn.com.higinet.tms.core.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 服务器实体类
 * @author sunhk 2016-12-22
 *
 */
public class Server {
	private static Logger logger = LoggerFactory.getLogger(Server.class);
	public static final String DELIMITER=":";
	private String ip;
	private int port;
//	private AtomicBoolean isUsable=new AtomicBoolean();
//	private AtomicInteger restartCount = new AtomicInteger(TmsConfigVo.minServSucCount()-1);
//	private AtomicLong errorCount = new AtomicLong(0);
	public Server(String ip, String port){
		this.port = Integer.parseInt(port);
		this.ip = ip;
	}
	public Server(String[] ipport){
		this(ipport[0], ipport[1]);
	}
	public Server(String ipportString){
		this(ipportString.split(DELIMITER));
	}
	public void setServer(String ipportString ) {
		String[] ipport = ipportString.split(DELIMITER);
		this.ip = ipport[0];
		this.port = Integer.parseInt(ipport[1]);
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
//	public boolean isUsable() {
//		return isUsable.get();
//	}
//	/**
//	 * 将当前错误次数加1，如果满足最大错误次数，自动不可用
//	 * @return
//	 */
//	public boolean incErrorCount(){
//		isUsable.compareAndSet(true, errorCount.incrementAndGet()<TmsConfigVo.maxServErrorCount());
//		logger.warn("服务器[" + this.toString() + "]请求失败，失败计数：" + errorCount.get());
//		if(!isUsable()){
//			logger.error("服务器[" + this.toString() + "]请求连续失败，已标记为不可用！");
//		}
//		return true;
//	}
//	/**
//	 * 设置可用
//	 */
//	public void setUsable(){
//		isUsable.set(true);
//		
//	}
//	public void resetErrorCount(){
//		errorCount.set(0);
//		//成功即打断错误计数用上面，否则用下面，成功需比失败多
////		if(errorCount.get()>0)
////			errorCount.decrementAndGet();
//	}
	@Override
	public boolean equals(Object object) {
		if(!(object instanceof Server)){
			return false;
		}else{
			Server server=(Server)object;
			if(port==server.port&&ip.equals(server.ip)){
				return true;
			}
		}
		return false;
	}
	public boolean equals(String ip,String port){
		return this.ip.equals(ip) && String.valueOf(this.port).equals(port);
	}
	public boolean equals(String[] ipport){
		return equals(ipport[0], ipport[1]);
	}
	public boolean equals(String ipStr){
		String[] ipport = ipStr.split(DELIMITER);
		return equals(ipport);
	}
	@Override
	public String toString() {
		return ip+DELIMITER+port;
	}
	
	
}
