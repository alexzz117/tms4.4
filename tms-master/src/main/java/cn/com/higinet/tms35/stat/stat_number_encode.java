package cn.com.higinet.tms35.stat;

import cn.com.higinet.tms35.comm.clock;
import cn.com.higinet.tms35.comm.tmsapp;

@SuppressWarnings("unused")
public class stat_number_encode
{
	static private codec m_codec = null;

	static
	{
		int use_base64 = tmsapp.get_config("tms.stat.base64", 0);
		m_codec = use_base64 == 1 ? new b64() : new b10();
	}

	final static public void setM_codec(int i)
	{
		m_codec = i == 1 ? new b64() : new b10();;
	}
	
	final static public String encode(int i)
	{
		return m_codec.encode(i);
	}

	final static public String encode(long i)
	{
		return m_codec.encode(i);
	}

	final static public int decode_int(String s)
	{
		return m_codec.decode_int(s);
	}

	final static public long decode_long(String s)
	{
		return m_codec.decode_long(s);
	}

	static private interface codec
	{
		public String encode(int i);

		public String encode(long i);

		public int decode_int(String s);

		public long decode_long(String s);
	}

	static private class b64 implements codec
	{
		
		static char[] encode = new char[64];
		static int[] decode = new int[128];
		static
		{
			int i = 0;
			for (int j = 'A'; j <= 'Z'; j++)
			{
				encode[i] = (char) j;
				decode[j] = i++;
			}

			for (int j = 'a'; j <= 'z'; j++)
			{
				encode[i] = (char) j;
				decode[j] = i++;
			}

			for (int j = '0'; j <= '9'; j++)
			{
				encode[i] = (char) j;
				decode[j] = i++;
			}
			{
				encode[i] = '+';
				decode['+'] = i++;
				encode[i] = '-';
				decode['-'] = i++;
			}
		}

		final public String encode(int i)
		{
			char cc[] = new char[8];
			int k = 0;
			do
			{
				cc[k++] = encode[i & 0x3f];
				i >>>= 6;
			} while (i != 0);
			return String.copyValueOf(cc, 0, k);
		}

		final public String encode(long i)
		{
			char cc[] = new char[16];
			int k = 0;
			do
			{
				cc[k++] = encode[(int) (i & 0x3f)];
				i >>>= 6;
			} while (i != 0);
			return String.copyValueOf(cc, 0, k);
		}

		final public int decode_int(String s)
		{
			int ret = 0;
			for (int i = 0, len = s.length(); i < len; i++)
				ret |= decode[s.charAt(i)] << (i * 6);

			return ret;
		}

		final public long decode_long(String s)
		{
			long ret = 0;
			for (int i = 0, len = s.length(); i < len; i++)
				ret |= ((long) decode[s.charAt(i)]) << (i * 6);

			return ret;
		}
	}

	static private class b16 implements codec
	{
		static char[] encode = new char[16];
		static int[] decode = new int[128];
		static
		{
			int i = 0;
			for (int j = '0'; j <= '9'; j++)
			{
				encode[i] = (char) j;
				decode[j] = i++;
			}
			for (int j = 'A'; j <= 'F'; j++)
			{
				encode[i] = (char) j;
				decode[j] = i++;
			}
		}

		final public String encode(int i)
		{
			char cc[] = new char[8];
			int k = 0;
			do
			{
				cc[k++] = encode[i & 0xf];
				i >>>= 4;
			} while (i != 0);
			return String.copyValueOf(cc, 0, k);
		}

		final public int decode_int(String s)
		{
			int ret = 0;
			for (int i = 0, len = s.length(); i < len; i++)
				ret |= decode[s.charAt(i)] << (i << 2);

			return ret;
		}

		final public String encode(long i)
		{
			char cc[] = new char[16];
			int k = 0;
			do
			{
				cc[k++] = encode[(int) (i & 0xf)];
				i >>>= 4;
			} while (i != 0);
			return String.copyValueOf(cc, 0, k);
		}

		final public long decode_long(String s)
		{
			long ret = 0;
			for (int i = 0, len = s.length(); i < len; i++)
				ret |= ((long) decode[s.charAt(i)]) << (i << 2);

			return ret;
		}
	}

	static private class b10 implements codec
	{
		final public String encode(int i)
		{
			return Integer.toString(i);
		}

		final public int decode_int(String s)
		{
			return (int) Long.parseLong(s);
		}

		final public String encode(long i)
		{
			return Long.toString(i);
		}

		final public long decode_long(String s)
		{
			return Long.parseLong(s);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		int CC = 1 << 25;
		clock c = new clock();
		{
			c.reset();
			for (int i = 0; i < CC; i++)
			{
				if (i != decode_int(encode(i)))
					System.out.println("64ERR." + i);
			}
			c.pin("base64[]");
		}
		{
			c.reset();
			for (int i = 0; i < CC; i++)
			{
				if (i != Integer.parseInt(Integer.toHexString(i), 16))
					System.out.println("ERR." + i);
			}
			c.pin("base16");
		}

	}
}
