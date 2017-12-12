package cn.com.higinet.tms35.manage.sign.common;

public final class clock {
	long _init;

	public clock() {
		reset();
	}

	public void reset() {
		_init = System.nanoTime();
	}

	public long count() {
		return (System.nanoTime() - _init) / 1000;
	}

	public long count_ms() {
		return (System.nanoTime() - _init) / 1000 / 1000;
	}

	public long count_sec() {
		return (System.nanoTime() - _init) / 1000 / 1000 / 1000;
	}

	public long count_ms_reset() {
		long now = System.nanoTime();
		long ret = (now - _init) / 1000 / 1000;
		_init = now;
		return ret;
	}

	public long count_sec_reset() {
		long now = System.nanoTime();
		long ret = (now - _init) / 1000 / 1000 / 1000;
		_init = now;
		return ret;
	}

	public long count_us(long nowNs) {
		return (nowNs - _init) / 1000;
	}

	public long count_ms(long nowNs) {
		return (nowNs - _init) / 1000 / 1000;
	}

	public long count_sec(long nowNs) {
		return (nowNs - _init) / 1000 / 1000 / 1000;
	}

	public int left(int time_out_ms) {
		long left = (int) (time_out_ms - count_ms());
		return (int) (left >= 0 ? left : 0);
	}

	public int left(long time_out_ms) {
		long left = (time_out_ms - count_ms());
		return (int) (left >= 0 ? left : 0);
	}
}