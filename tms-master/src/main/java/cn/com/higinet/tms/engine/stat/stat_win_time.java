package cn.com.higinet.tms.engine.stat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import cn.com.higinet.tms.engine.core.cond.date_tool;

@SuppressWarnings("deprecation")
public abstract class stat_win_time
{
	private static final int Base_time = (int) (date_tool.parse("2013-1-1 0:0:0").getTime() / 60000);

	public static final int base_time()
	{
		return Base_time;
	}

	public static int dec_win_when_set(int tm_unit)
	{
		if (tm_unit >= unit.length)
			return -1;

		return unit[tm_unit].dec_win_when_set();
	}

	public static int cur_win_time(int tm_unit, int txn_minute)
	{
		if (tm_unit >= unit.length)
			return -1;
		return unit[tm_unit].cur_win_time(txn_minute) - base_time();
	}

	public static int min_win_time(int tm_unit, int num_unit, int txn_minute)
	{
		if (tm_unit >= unit.length)
			return -1;
		return unit[tm_unit].min_win_time(num_unit, txn_minute) - base_time();
	}

	public static int st2txntime(int tm_unit, int stat_minute)
	{
		return stat_minute + base_time();
	}

	private static win_time_base unit[] = { //
			new fixed(1), // 0 分钟
			new fixed(5), // 1 5分钟
			new fixed(60), // 2 小时
			new day(), // 3 天
			new week(), // 4 星期
			new month(), // 5 月
			new year(), // 6 年
			new eternal(), // 7 永久
			new txn(), // 8 交易
			new session() // 9 每次登录
	};

	public static int minute_per_win(int tm_unit)
	{
		return unit[tm_unit].num_minute();
	}

	private static int tm_offset = TimeZone.getDefault().getRawOffset() / 1000 / 60;

	public static abstract class win_time_base
	{
		public int dec_win_when_set()
		{
			return 0;
		}

		abstract public int cur_win_time(int txn_minute);

		abstract public int min_win_time(int num_unitint, int txn_minute);

		abstract public int num_minute();
	}

	public final static class fixed extends win_time_base
	{
		int unit_minute;

		public fixed(int unit_minute)
		{
			this.unit_minute = unit_minute;
		}

		public int cur_win_time(int txnMinute)
		{
			return txnMinute - txnMinute % unit_minute;
		}

		public int min_win_time(int numUnit, int txnMinute)
		{
			return cur_win_time(txnMinute) - numUnit * unit_minute;
		}

		@Override
		public int num_minute()
		{
			return unit_minute;
		}

	}

	public final static class day extends win_time_base
	{
		@Override
		public int num_minute()
		{
			return 24 * 60;
		}

		public int cur_win_time(int txnMinute)
		{
			return (txnMinute + tm_offset) / (24 * 60) * (24 * 60) - tm_offset;
		}

		public int min_win_time(int numUnit, int txnMinute)
		{
			return (cur_win_time(txnMinute) - numUnit * 24 * 60);
		}
	}

	public final static class week extends win_time_base
	{
		@Override
		public int num_minute()
		{
			return 7 * 24 * 60;
		}

		public int cur_win_time(int txnMinute)
		{
			return (txnMinute + tm_offset - 4 * 24 * 60) / (7 * 24 * 60) * (7 * 24 * 60) - tm_offset + 4 * 24 * 60;
		}

		public int min_win_time(int numUnit, int txnMinute)
		{
			return (cur_win_time(txnMinute) - numUnit * 7 * 24 * 60);
		}
	}

	public final static class month extends win_time_base
	{
		@Override
		public int num_minute()
		{
			return 30 * 24 * 60;
		}

		public int cur_win_time(int txnMinute)
		{
			Date d = new Date((long) txnMinute * 60 * 1000);
			return (int) (Date.UTC(d.getYear(), d.getMonth(), 1, 0, -tm_offset, 0) / 1000 / 60);
		}

		public int min_win_time(int numUnit, int txnMinute)
		{
			Date d = new Date((long) txnMinute * 60 * 1000);
			return (int) (Date.UTC(d.getYear(), d.getMonth() - numUnit, 1, 0, -tm_offset, 0) / 1000 / 60);
		}
	}

	public final static class year extends win_time_base
	{
		@Override
		public int num_minute()
		{
			return 365 * 24 * 60;
		}

		public int cur_win_time(int txnMinute)
		{
			Date d = new Date((long) txnMinute * 60 * 1000);
			return (int) (Date.UTC(d.getYear(), 0, 1, 0, -tm_offset, 0) / 1000 / 60);
		}

		public int min_win_time(int numUnit, int txnMinute)
		{
			Date d = new Date((long) txnMinute * 60 * 1000);
			return (int) (Date.UTC(d.getYear() - numUnit, 0, 1, 0, -tm_offset, 0) / 1000 / 60);
		}
	}

	public final static class session extends win_time_base
	{
		@Override
		public int num_minute()
		{
			return 1;
		}

		public int cur_win_time(int txnMinute)
		{
			return base_time();
		}

		public int min_win_time(int numUnit, int txnMinute)
		{
			return base_time();
		}
	}

	public final static class eternal extends win_time_base
	{
		@Override
		public int num_minute()
		{
			return 100 * 365 * 24 * 60;
		}

		public int cur_win_time(int txnMinute)
		{
			return base_time();
		}

		public int min_win_time(int numUnit, int txnMinute)
		{
			return base_time();
		}
	}

	public final static class txn extends win_time_base
	{
		@Override
		public int num_minute()
		{
			return 1;
		}

		@Override
		public int dec_win_when_set()
		{
			return 1;
		}

		public int cur_win_time(int txnMinute)
		{
			return base_time();
		}

		public int min_win_time(int numUnit, int txnMinute)
		{
			return base_time() - numUnit;
		}
	}

	static void debug(int minu)
	{
		System.out.print(minu);
		System.out.print(' ');
		System.out.println(new SimpleDateFormat("yyyy-M-d HH:mm:ss").format(new Date((long) minu * 60 * 1000)));

		// System.out.println("--"+new Date((long)minu*60*1000-8*3600*1000));
		// System.out.println(DateTool.format(new Date((long)minu*60*1000)));
	}

	public static void main(String[] args)
	{
		// int dm = 24 * 60;
		// System.out.print(new Date(abstime).getTime());
		// debug((int)(abstime/60/1000));
		// debug((int)((abstime)/60/1000-20));
		// System.out.println(abstime/60/1000%(24*60));
		// debug((int) (new Date().getTime() / 1000 / 60));
		// debug(new day().cur_win_time((int) ((abstime / 1000 / 60)-20)));
		// debug(new day().cur_win_time((int) (abstime / 1000 / 60)));
		// debug(new day().min_win_time(2, (int) (new Date().getTime() / 1000 /
		// 60)));
		//
		long abstime = 1368029730842l; // 2013-5-9 0:15
		debug(new week().cur_win_time((int) ((abstime / 1000 / 60) - 24 * 60 * 1)));
		debug(new week().cur_win_time((int) ((abstime / 1000 / 60) - 24 * 60 * 2)));
		debug(new week().cur_win_time((int) ((abstime / 1000 / 60) - 24 * 60 * 3)));
		debug(new week().cur_win_time((int) ((abstime / 1000 / 60) - 24 * 60 * 4)));
		debug(new week().cur_win_time((int) ((abstime / 1000 / 60) - 24 * 60 * 5)));

		debug(new day().cur_win_time((int) ((abstime / 1000 / 60) - 24 * 60 * 1)));
		debug(new day().cur_win_time((int) ((abstime / 1000 / 60) - 24 * 60 * 2)));
		debug(new day().cur_win_time((int) ((abstime / 1000 / 60) - 24 * 60 * 3)));
		debug(new day().cur_win_time((int) ((abstime / 1000 / 60) - 24 * 60 * 4)));
		debug(new day().cur_win_time((int) ((abstime / 1000 / 60) - 24 * 60 * 5)));

		debug(new fixed(1).cur_win_time((int) ((abstime / 1000 / 60))));

		debug(new month().cur_win_time((int) ((abstime / 1000 / 60))));
		debug(new month().min_win_time(1, (int) ((abstime / 1000 / 60))));

		debug(new year().cur_win_time((int) ((abstime / 1000 / 60))));
		debug(new year().min_win_time(1, (int) ((abstime / 1000 / 60))));
		debug(new session().cur_win_time((int) ((abstime / 1000 / 60))));
		debug(new session().min_win_time(1, (int) ((abstime / 1000 / 60))));
	}
}
