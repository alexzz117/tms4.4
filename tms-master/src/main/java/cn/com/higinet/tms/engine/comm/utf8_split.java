package cn.com.higinet.tms.engine.comm;

public final class utf8_split
{
	static byte[] g_plen = new byte[0xFFFF];
	static
	{
		int i = 0;
		for (; i <= 0x7F; i++)
			g_plen[i] = 1;
		for (; i <= 0x7FF; i++)
			g_plen[i] = 2;
		for (; i < 0xFFFF; i++)
			g_plen[i] = 3;
	}
	public String s1, s2, s3;

	public utf8_split(String s, int max_len)
	{
		do_split(s, max_len);
	}

	static final int utf8_len(int ch)
	{
		if (ch < 0xFFFF)
			return g_plen[ch];
		else if (ch == 0xFFFF)
			return 3;
		else if (ch <= 0x1FFFFF)
			return 4;
		else if (ch <= 0x3FFFFFF)
			return 5;
		else
			return 6;
	}

	public void do_split(String s, int len)
	{
		if (s.length() < len / 3)
		{
			s1 = s;
			return;
		}

		int pn = 0, n = 0;
		n = pos(s, pn, len);
		if (n == s.length())
		{
			s1 = s;
			return;
		}

		s1 = s.substring(pn, n);
		pn = n;

		n = pos(s, pn, len);
		s2 = s.substring(pn, n);
		if (n == s.length())
			return;

		s3 = s.substring(n);
	}

	private int pos(String s, int i, int max_len)
	{
		int blen = 0;
		for (int len = s.length(); i < len; i++)
		{
//			blen += utf8_len(s.charAt(i));
			blen += utf8_len(s.codePointAt(i));
			if (blen > max_len)
				return i;
		}

		return s.length();
	}

	public static void main(String[] args)
	{
		utf8_split us=new utf8_split("我是一个中国人，我热爱自己的祖国", 20);
		System.out.println(us.s1+us.s2+us.s3);
	}
}
