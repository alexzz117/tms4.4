package cn.com.higinet.tms.engine.run;

/**
 * 运行时间
 * @author lining
 *
 */
public class run_time {
	/**
	 * 开始时间, 毫秒数
	 */
	private Long startTime;
	/**
	 * 结束时间, 毫秒数
	 */
	private Long endTime;
	
	public run_time() {
		this.startTime = System.currentTimeMillis();
	}
	
	public run_time(long startTime) {
		this.startTime = startTime;
	}
	
	/**
	 * 重新开始计时
	 */
	public void reStart() {
		this.startTime = System.currentTimeMillis();
	}
	
	/**
	 * 停止计时
	 */
	public void stop() {
		this.endTime = System.currentTimeMillis();
	}

	/**
	 * 获取开始时间, 毫秒数
	 * @return
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * 设置开始时间, 毫秒数
	 * @param startTime
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	/**
	 * 获取结束时间, 毫秒数
	 * @return
	 */
	public long getEndTime() {
		if (endTime == null) {
			this.stop();
		}
		return endTime;
	}
	
	/**
	 * 设置结束时间, 毫秒数
	 * @param endTime
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	
	/**
	 * 获取耗费时间
	 * @return
	 */
	public int getUseTime() {
		return (int) (this.getEndTime() - this.getStartTime());
	}
}