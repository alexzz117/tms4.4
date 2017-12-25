package cn.com.higinet.tms35.comm;

import java.util.Date;

import org.slf4j.Logger;

import cn.com.higinet.tms35.core.cond.date_tool;

public final class clock
{
	long _init;

	public clock()
	{
		reset();
	}

	public void reset()
	{
		_init = System.nanoTime();
	}

	public long count()
	{
		return (System.nanoTime() - _init) / 1000;
	}

	public long count_ms()
	{
		return (System.nanoTime() - _init) / 1000 / 1000;
	}

	public long count_sec()
	{
		return (System.nanoTime() - _init) / 1000 / 1000 / 1000;
	}

	public long count_ms_reset()
	{
		long now = System.nanoTime();
		long ret = (now - _init) / 1000 / 1000;
		_init = now;
		return ret;
	}

	public long count_sec_reset()
	{
		long now = System.nanoTime();
		long ret = (now - _init) / 1000 / 1000 / 1000;
		_init = now;
		return ret;
	}

	public long count_us(long nowNs)
	{
		return (nowNs - _init) / 1000;
	}

	public long count_ms(long nowNs)
	{
		return (nowNs - _init) / 1000 / 1000;
	}

	public long count_sec(long nowNs)
	{
		return (nowNs - _init) / 1000 / 1000 / 1000;
	}

	public void pin(String dbg_info)
	{
		pin(dbg_info, System.out);
	}

	public void pin_us(String dbg_info)
	{
		pin_us(dbg_info, System.out);
	}

	public void pin(Logger log,String dbg_info)
	{
		pin(dbg_info, log);
	}

	public void pin(String dbg_info, java.io.PrintStream out)
	{
		long count=count_ms();
		out.println(date_tool.format(new Date(tm_tool.lctm_ms())) + " " + dbg_info + ":" + count);
		reset();
	}

	public void pin_us(String dbg_info, java.io.PrintStream out)
	{
		long count=this.count_us(System.nanoTime());
		out.println(date_tool.format(new Date(tm_tool.lctm_ms())) + " " + dbg_info + ":" + count);
		reset();
	}
	public void pin(String dbg_info, Logger log)
	{
		log.info(date_tool.format(new Date(tm_tool.lctm_ms())) + " " + dbg_info + ":" + count_ms());
		reset();
	}
	public int left(int time_out_ms)
	{
		long left = (int) (time_out_ms - count_ms());
		return (int)(left >= 0 ? left : 0);
	}
	public int left(long time_out_ms)
	{
		long left = (time_out_ms - count_ms());
		return (int)(left >= 0 ? left : 0);
	}
}
