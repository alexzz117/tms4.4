package cn.com.higinet.tms35.core.cond;


public interface func
{
	Object exec(Object[] p, int n);
}

/*
class func_impl
{
	static func[] c_func = new func[] { new func()
	{
		// 0
		public Object exec(Object[] p, int n)
		{
			return ((Long) p[n + 0]) + ((Long) p[n + 1]);
		}
	}, new func()
	{// 1
				public Object exec(Object[] p, int n)
				{
					return ((Long) p[n + 0]) + ((Double) p[n + 1]);
				}
			}, new func()
			{// 2
				public Object exec(Object[] p, int n)
				{
					return ((Long) p[n + 0]) + ((String) p[n + 1]);
				}
			}, new func()
			{// 3
				public Object exec(Object[] p, int n)
				{
					return ((Double) p[n + 0]) + ((Long) p[n + 1]);
				}
			}, new func()
			{// 4
				public Object exec(Object[] p, int n)
				{
					return ((Double) p[n + 0]) + ((Double) p[n + 1]);
				}
			}, new func()
			{// 5
				public Object exec(Object[] p, int n)
				{
					return ((Double) p[n + 0]) + ((String) p[n + 1]);
				}
			}, new func()
			{// 6
				public Object exec(Object[] p, int n)
				{
					return ((String) p[n + 0]) + ((Long) p[n + 1]);
				}
			}, new func()
			{// 7
				public Object exec(Object[] p, int n)
				{
					return ((String) p[n + 0]) + ((Double) p[n + 1]);
				}
			}, new func()
			{// 8
				public Object exec(Object[] p, int n)
				{
					return ((String) p[n + 0]) + ((String) p[n + 1]);
				}
			}, new func()
			{// 9
				public Object exec(Object[] p, int n)
				{
					return ((String) p[n + 0])
							+ date_tool.format(new java.util.Date((Long) p[n + 1]));
				}
			}, new func()
			{// 10
				public Object exec(Object[] p, int n)
				{
					return date_tool.format(new java.util.Date((Long) p[n + 0]))
							+ ((String) p[n + 1]);
				}
			}, new func()
			{// 11
				public Object exec(Object[] p, int n)
				{
					return (((Long) p[n + 0]) + ((Long) p[n + 1])) % (24 * 60 * 60 * 1000);
				}
			}, new func()
			{// 12
				public Object exec(Object[] p, int n)
				{
					return "[span:" + ((Long) p[n + 0]) + "]" + ((String) p[n + 1]);
				}
			}, new func()
			{// 13
				public Object exec(Object[] p, int n)
				{
					return -((Long) p[n + 0]);
				}
			}, new func()
			{// 14
				public Object exec(Object[] p, int n)
				{
					return -((Double) p[n + 0]);
				}
			}, new func()
			{// 15
				public Object exec(Object[] p, int n)
				{
					return ((Long) p[n + 0]) - ((Long) p[n + 1]);
				}
			}, new func()
			{// 16
				public Object exec(Object[] p, int n)
				{
					return ((Long) p[n + 0]) - ((Double) p[n + 1]);
				}
			}, new func()
			{// 17
				public Object exec(Object[] p, int n)
				{
					return ((Double) p[n + 0]) - ((Long) p[n + 1]);
				}
			}, new func()
			{// 18
				public Object exec(Object[] p, int n)
				{
					return ((Double) p[n + 0]) - ((Double) p[n + 1]);
				}
			}, new func()
			{// 19
				public Object exec(Object[] p, int n)
				{
					return (((Long) p[n + 0]) - ((Long) p[n + 1])) % (24 * 60 * 60 * 1000);
				}
			}, new func()
			{// 20
				public Object exec(Object[] p, int n)
				{
					return ((Long) p[n + 0]) * ((Long) p[n + 1]);
				}
			}, new func()
			{// 21
				public Object exec(Object[] p, int n)
				{
					return ((Long) p[n + 0]) * ((Double) p[n + 1]);
				}
			}, new func()
			{// 22
				public Object exec(Object[] p, int n)
				{
					return ((Double) p[n + 0]) * ((Long) p[n + 1]);
				}
			}, new func()
			{// 23
				public Object exec(Object[] p, int n)
				{
					return ((Double) p[n + 0]) * ((Double) p[n + 1]);
				}
			}, new func()
			{// 24
				public Object exec(Object[] p, int n)
				{
					return ((Long) p[n + 0]) / ((Long) p[n + 1]);
				}
			}, new func()
			{// 25
				public Object exec(Object[] p, int n)
				{
					return ((Long) p[n + 0]) / ((Double) p[n + 1]);
				}
			}, new func()
			{// 26
				public Object exec(Object[] p, int n)
				{
					return ((Double) p[n + 0]) / ((Long) p[n + 1]);
				}
			}, new func()
			{// 27
				public Object exec(Object[] p, int n)
				{
					return ((Double) p[n + 0]) / ((Double) p[n + 1]);
				}
			}

			, new func()
			{// 28-33 EQ
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return ((Number) p[n + 0]).longValue() - ((Number) p[n + 1]).longValue() == 0 ? TRUE
							: FALSE;
				}
			}, new func()
			{// 29
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return Math.abs(((Number) p[n + 0]).longValue()
							- ((Number) p[n + 1]).doubleValue()) < 1e-4 ? TRUE : FALSE;
				}
			}, new func()
			{// 30
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return Math.abs(((Number) p[n + 0]).doubleValue()
							- ((Number) p[n + 1]).longValue()) < 1e-4 ? TRUE : FALSE;
				}
			}, new func()
			{// 31
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return Math.abs(((Number) p[n + 0]).doubleValue()
							- ((Number) p[n + 1]).doubleValue()) < 1e-4 ? TRUE : FALSE;
				}
			}, new func()
			{// 32
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return p[n + 0].equals(p[n + 1]) ? TRUE : FALSE;
				}
			}, new func()
			{// 33
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return ((Number) p[n + 0]).longValue() % (24 * 3600 * 1000)
							- ((Number) p[n + 1]).longValue() == 0 ? TRUE : FALSE;
				}
			}

			, new func()
			{// 34-38 GT
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return ((Number) p[n + 0]).longValue() - ((Number) p[n + 1]).longValue() > 0 ? TRUE
							: FALSE;
				}
			}, new func()
			{// 35
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return ((Number) p[n + 0]).longValue() - ((Number) p[n + 1]).doubleValue() > 0 ? TRUE
							: FALSE;
				}
			}, new func()
			{// 36
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;

					return ((Number) p[n + 0]).doubleValue() - ((Number) p[n + 1]).longValue() > 0 ? TRUE
							: FALSE;
				}
			}, new func()
			{// 37
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return ((Number) p[n + 0]).doubleValue() - ((Number) p[n + 1]).doubleValue() > 0 ? TRUE
							: FALSE;
				}
			}, new func()
			{// 38
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return ((Number) p[n + 0]).longValue() % (24 * 3600 * 1000)
							- ((Number) p[n + 1]).longValue() > 0 ? TRUE : FALSE;
				}
			}

			, new func()
			{// 39-43 LT
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return ((Number) p[n + 0]).longValue() - ((Number) p[n + 1]).longValue() < 0 ? TRUE
							: FALSE;
				}
			}, new func()
			{// 40
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return ((Number) p[n + 0]).longValue() - ((Number) p[n + 1]).doubleValue() < 0 ? TRUE
							: FALSE;
				}
			}, new func()
			{// 41
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return ((Number) p[n + 0]).doubleValue() - ((Number) p[n + 1]).longValue() < 0 ? TRUE
							: FALSE;
				}
			}, new func()
			{// 42
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return ((Number) p[n + 0]).doubleValue() - ((Number) p[n + 1]).doubleValue() < 0 ? TRUE
							: FALSE;
				}
			}, new func()
			{// 43
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return ((Number) p[n + 0]).longValue() % (24 * 3600 * 1000)
							- ((Number) p[n + 1]).longValue() < 0 ? TRUE : FALSE;
				}
			}

			, new func()
			{// 44-48 GT
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return ((Number) p[n + 0]).longValue() - ((Number) p[n + 1]).longValue() >= 0 ? TRUE
							: FALSE;
				}
			}, new func()
			{// 45
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return ((Number) p[n + 0]).longValue() - ((Number) p[n + 1]).doubleValue() >= 0 ? TRUE
							: FALSE;
				}
			}, new func()
			{// 46
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return ((Number) p[n + 0]).doubleValue() - ((Number) p[n + 1]).longValue() >= 0 ? TRUE
							: FALSE;
				}
			}, new func()
			{// 47
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return ((Number) p[n + 0]).doubleValue() - ((Number) p[n + 1]).doubleValue() >= 0 ? TRUE
							: FALSE;
				}
			}, new func()
			{// 48
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return ((Number) p[n + 0]).longValue() % (24 * 3600 * 1000)
							- ((Number) p[n + 1]).longValue() >= 0 ? TRUE : FALSE;
				}
			}

			, new func()
			{// 49-53 LE
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return ((Number) p[n + 0]).longValue() - ((Number) p[n + 1]).longValue() <= 0 ? TRUE
							: FALSE;
				}
			}, new func()
			{// 50
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return ((Number) p[n + 0]).longValue() - ((Number) p[n + 1]).doubleValue() <= 0 ? TRUE
							: FALSE;
				}
			}, new func()
			{// 51
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return ((Number) p[n + 0]).doubleValue() - ((Number) p[n + 1]).longValue() <= 0 ? TRUE
							: FALSE;
				}
			}, new func()
			{// 52
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return ((Number) p[n + 0]).doubleValue() - ((Number) p[n + 1]).doubleValue() <= 0 ? TRUE
							: FALSE;
				}
			}, new func()
			{// 53
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return ((Number) p[n + 0]).longValue() % (24 * 3600 * 1000)
							- ((Number) p[n + 1]).longValue() <= 0 ? TRUE : FALSE;
				}
			}

			, new func()
			{// 54-59 NE
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return ((Number) p[n + 0]).longValue() - ((Number) p[n + 1]).longValue() != 0 ? TRUE
							: FALSE;
				}
			}, new func()
			{// 55
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return Math.abs(((Number) p[n + 0]).longValue()
							- ((Number) p[n + 1]).doubleValue()) > 1e-4 ? TRUE : FALSE;
				}
			}, new func()
			{// 56
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return Math.abs(((Number) p[n + 0]).doubleValue()
							- ((Number) p[n + 1]).longValue()) > 1e-4 ? TRUE : FALSE;
				}
			}, new func()
			{// 57
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return Math.abs(((Number) p[n + 0]).doubleValue()
							- ((Number) p[n + 1]).doubleValue()) > 1e-4 ? TRUE : FALSE;
				}
			}, new func()
			{// 58
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return !p[n + 0].equals(p[n + 1]) ? TRUE : FALSE;
				}
			}, new func()
			{// 59
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;
					return ((Number) p[n + 0]).longValue() % (24 * 3600 * 1000)
							- ((Number) p[n + 1]).longValue() != 0 ? TRUE : FALSE;
				}
			}

			, new func()
			{// 60
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null)
						return null;
					return Math.abs(((Number) p[n + 0]).longValue());
				}
			}, new func()
			{// 61
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null)
						return null;
					return Math.abs(((Number) p[n + 0]).doubleValue());
				}
			}

			, new func()
			{// 62
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null)
						return null;
					return (long) new Date(((Number) p[n + 0]).longValue()).getYear() + 1900;
				}
			}, new func()
			{// 63
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null)
						return null;
					return (long) new Date(((Number) p[n + 0]).longValue()).getMonth() + 1;
				}
			}, new func()
			{// 64
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null)
						return null;
					return new Date(((Number) p[n + 0]).longValue()).getDate();
				}
			}, new func()
			{// 65
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null)
						return null;
					return new Date(((Number) p[n + 0]).longValue()).getDay();
				}
			}, new func()
			{// 66
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null)
						return null;
					return new Date(((Number) p[n + 0]).longValue()).getHours();
				}
			}, new func()
			{// 67
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null)
						return null;
					return new Date(((Number) p[n + 0]).longValue()).getMinutes();
				}
			}, new func()
			{// 68
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null)
						return null;
					return new Date(((Number) p[n + 0]).longValue()).getSeconds();
				}
			}, new func()
			{// 69
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null)
						return null;
					Long ret = (((Number) p[n + 0]).longValue()) / (24 * 60 * 60 * 1000)
							* (24 * 60 * 60 * 1000) - date_tool.raw_offset;
					return ret;
				}
			}, new func()
			{// 70
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null)
						return null;
					return ((Number) p[n + 0]).longValue() % (24 * 60 * 60 * 1000);
				}
			}, new func()
			{// 71
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return FALSE;

					return ((Long) p[n + 0]) * ((Long) p[n + 1]) != 0 ? TRUE : FALSE;
				}
			}, new func()
			{// 72
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null && p[n + 1] == null)
						return FALSE;
					if (p[n] == null || p[n + 1] == null)
						return TRUE;

					return ((Long) p[n + 0]) + ((Long) p[n + 1]) != 0 ? TRUE : FALSE;
				}
			}, new func_session_diff()// 73
			, new func()
			{// 74 ip2isp

				public Object exec(Object[] p, int n)
				{
					return "";
				}

			}, new func()
			{// 75 ip2city

				public Object exec(Object[] p, int n)
				{
					return ip_cache.Instance().get_city(str_tool.to_str(p[n]));
				}
			}, new func()
			{// 76 ip2region

				public Object exec(Object[] p, int n)
				{
					return ip_cache.Instance().get_region(str_tool.to_str(p[n]));
				}
			}, new func()
			{// 77 ip2country

				public Object exec(Object[] p, int n)
				{
					return ip_cache.Instance().get_country(str_tool.to_str(p[n]));
				}
			}, new func()
			{// 78 long in
				public Object exec(Object[] p, int n)
				{
					run_env re = (run_env) p[n - 1];
					db_roster.cache drc = re.get_txn().g_dc.roster();
					return drc.val_in((Number) p[n + 1], p[n]);
				}
			}, new func()
			{// 79 string in

				public Object exec(Object[] p, int n)
				{
					run_env re = (run_env) p[n - 1];
					// db_roster.cache drc = re.get_txn().g_dc.roster();
					db_roster.cache drc = db_cache.get().roster();

					return drc.val_in((Number) p[n + 1], p[n]);
				}
			}, new func()
			{// 80 long notin

				public Object exec(Object[] p, int n)
				{
					run_env re = (run_env) p[n - 1];
					db_roster.cache drc = re.get_txn().g_dc.roster();
					return func_map.is_true(drc.val_in((Number) p[n + 1], p[n])) ? FALSE : TRUE;
				}
			}, new func()
			{// 81 string notin

				public Object exec(Object[] p, int n)
				{
					run_env re = (run_env) p[n - 1];
					db_roster.cache drc = re.get_txn().g_dc.roster();
					return func_map.is_true(drc.val_in((Number) p[n + 1], p[n])) ? FALSE : TRUE;
				}
			}, new func_last_impl() // 82
			, new func()
			{// 83 null

				public Object exec(Object[] p, int n)
				{
					return p[n] == null ? TRUE : FALSE;
				}
			}, new func()
			{// 84 is_empty(str)
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null)
						return TRUE;

					if (p[n] instanceof String && ((String) p[n]).length() == 0)
						return TRUE;

					return FALSE;
				}
			}, new func()
			{// 85 not(bool)
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null)
						return TRUE;

					if (p[n] instanceof String && ((String) p[n]).length() == 0)
						return TRUE;

					if (p[n] instanceof Number && ((Number) p[n]).longValue() != 0)
						return TRUE;

					return FALSE;
				}
			}, new func()
			{// 86 truncate

				public Object exec(Object[] p, int n)
				{
					if (p[n] == null)
						return null;
					String s = str_tool.to_str(p[n]);
					int s1 = ((Number) p[n + 1]).intValue();

					return s.substring(0, s1);
				}
			}, new func()
			{// 87 trim

				public Object exec(Object[] p, int n)
				{
					if (p[n] == null)
						return null;
					String s = str_tool.to_str(p[n]);
					return s.trim();
				}
			}, new func()
			{// 88 len

				public Object exec(Object[] p, int n)
				{
					if (p[n] == null)
						return null;
					String s = str_tool.to_str(p[n]);
					return s.length();
				}
			}, new func()
			{// 89 substr
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null)
						return null;
					String s = str_tool.to_str(p[n]);
					int s1 = ((Number) p[n + 1]).intValue();
					int s2 = ((Number) p[n + 2]).intValue();
					return s.substring(s1, s2);
				}
			}, new func()
			{// 90 now
				public Object exec(Object[] p, int n)
				{
					return new Long(tm_tool.lctm_ms());
				}
			}, new func()
			{// 91 upper
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null)
						return null;

					return upper_case(str_tool.to_str(p[n]));
				}
			}, new func()
			{// 92 lower
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null)
						return null;

					return str_tool.lower_case(str_tool.to_str(p[n]));
				}
			}, new func()
			{// 93 index
				public Object exec(Object[] p, int n)
				{
					if (p[n] == null || p[n + 1] == null)
						return new Long(-1);

					String s1 = to_str(p[n]);
					String s2 = to_str(p[n + 1]);

					return new Long(s1.indexOf(s2));
				}
			}, new func()
			{// 94
				public Object exec(Object[] p, int n)
				{
					return ((Long) p[n + 0]) % ((Long) p[n + 1]);
				}
			}, new func()
			{// 95
				public Object exec(Object[] p, int n)
				{
					return ((Long) p[n + 0]) % ((Double) p[n + 1]).longValue();
				}
			}, new func()
			{// 96
				public Object exec(Object[] p, int n)
				{
					return ((Double) p[n + 0]).longValue() % ((Long) p[n + 1]);
				}
			}, new func()
			{// 97
				public Object exec(Object[] p, int n)
				{
					return ((Double) p[n + 0]).longValue() % ((Double) p[n + 1]).longValue();
				}
			}

	};
}
*/