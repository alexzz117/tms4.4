package cn.com.higinet.tms35.comm;

public class hash
{
	// static int[] PRIME = new int[] { 2087, 2089, 2137, 2153, 2221, 2281,
	// 2297, 2311, 2381, 2393,
	// 4153, 4153, 4201, 4201, 4217, 4217, 4231, 4231, 4327, 4327, 4391, 4391,
	// 4397, 4397,
	// 4409, 4409, 4423, 4423, 4441, 4441, 4457, 4457, 4493, 4493, 8231, 8233,
	// 8237, 8263,
	// 8269, 8297, 8311, 8377, 8423, 8429, 8461, 8537, 8599, 8647, 8663, 16487,
	// 16493, 16519,
	// 16553, 16567, 16631, 16633, 16729, 16759, 16823, 16871, 16889, 16903,
	// 32839, 32887,
	// 32909, 32941, 32983, 32999, 33037, 33049, 33113, 33191, 33223, 65543,
	// 65581, 65657,
	// 65677, 65687, 65719, 65831, 65837, 65881, 65927, 66029, 131111, 131113,
	// 131129, 131143,
	// 131149, 131213, 131303, 131321, 131431, 131437, 131447, 131449, 131479,
	// 131497, 131501,
	// 131543, 131561, 131591, 131639, 131641, 131671, 131687, 262151, 262231,
	// 262253, 262313,
	// 262349, 262391, 262489, 262519, 262541, 262553, 262567, 262583, 262649,
	// 262681, 262697,
	// 262733, 524429, 524519, 524521, 524599, 524633, 524743, 524857, 524921,
	// 524941, 524969,
	// 1048583, 1048589, 1048601, 1048633, 1048681, 1048717, 1048759, 1048793,
	// 1048807,
	// 1048877, 1048889, 1048909, 1048919, 1049063, 1049101, 1049129, 1049143,
	// 1049177,
	// 2097223, 2097229, 2097257, 2097287, 2097383, 2097401, 2097421, 2097449,
	// 2097479,
	// 2097517, 2097559, 2097593, 2097671, 2097709, 2097769, 2097833, 4194329,
	// 4194409,
	// 4194439, 4194503, 4194583, 4194599, 4194601, 4194637, 4194679, 4194713,
	// 4194793,
	// 4194823, 4194857, 4194871, 4194887, 4194919, 4194989, 4195001, 4195021,
	// 8388697,
	// 8388761, 8388791, 8388841, 8388857, 8388953, 8389063, 8389079, 8389081,
	// 8389111,
	// 8389159, 8389261, 16777337, 16777421, 16777447, 16777577, 16777639,
	// 16777721, 16777751,
	// 16777837, 16777991, 16778009, 16778023, 16778071, 33554473, 33554503,
	// 33554509,
	// 33554519, 33554743, 33554839, 33554903, 33554951, 33555079, 33555149,
	// 33555191,
	// 33555241, 67108919, 67109033, 67109177, 67109191, 67109209, 67109417,
	// 67109431,
	// 67109479, 67109543, 67109593, 67109671, 67109687, 67109737, 134217773,
	// 134217799,
	// 134217869, 134217943, 134217977, 134218039, 134218103, 134218153,
	// 134218169, 134218199,
	// 134218327, 134218391, 134218423, 134218537, 268435463, 268435561,
	// 268435577, 268435597,
	// 268435639, 268435751, 268435757, 268435879, 268435961, 268436071,
	// 268436087, 268436089,
	// 268436141, 268436167, 268436249, 268436263, 268436269, 268436279,
	// 268436281, 268436327,
	// 268436407, 268436429, 536870951, 536871001, 536871017, 536871191,
	// 536871319, 536871337,
	// 536871367, 536871449, 536871463, 536871481, 536871527, 536871703,
	// 536871719, 536871721,
	// 536871757, 1073741831, 1073741993, 1073742073, 1073742169, 1073742233,
	// 1073742343,1073742361, 1073742391, 1073742583, 1073742713 };
	
	final int[] a={536871757, 1073741831, 1073741993, 1073742073, 1073742169, 1073742233,1073742343,1073742361, 1073742391, 1073742583, 1073742713};
	
	int id(int h,int i)
	{
		return h*a[i]%10;
	}

	static final public int clac(final String s)
	{
		return clac(s, 16777337);
	}
	
	static final public int hash_id(String name, int m)
	{
		return hash.clac(name, 134218327) % 2297 % m;
	}

	static final public int clac(final String s, int prime)
	{
		int h = 0;
		for (int i = 0, len = s.length(); i < len; i++)
		{
			h *= prime;
			h ^= 0xff & s.charAt(i);
		}

		return h & 0x7FFFFFFF;
	}

	static final public int clac(int id)
	{
		int h = 0;

		h = (id & 0xff) ^ h;
		h *= 16777337;
		id >>= 8;

		h = (id & 0xff) ^ h;
		h *= 16777337;
		id >>= 8;

		h = (id & 0xff) ^ h;
		h *= 16777337;
		id >>= 8;

		h = (id & 0xff) ^ h;

		return h & 0x7FFFFFFF;
	}

	static double norm(int[] d)
	{
		long sum = 0;
		for (int i : d)
			sum += i;

		double avg = 1. * sum / d.length;

		double x = 0;
		for (int i : d)
			x += (i - avg) * (i - avg) / d.length / d.length;

		x = Math.sqrt((x));

		// System.out.print(String.format("%.3f,", x));
		return x;

	}

	static interface cc
	{
		String go(int i);
	}

	public static double test(int pp, int c, cc cc)
	{
		int[] pr = new int[c];
		for (int i = 0; i < 100 * c; i++)
			pr[hash.clac(cc.go(i), pp) % c]++;
		int sum = 0;
		for (int i : pr)
			sum += i;

		// for(int i:pr)
		// {
		// System.out.print(String.format("%.4f ", 1.*i/sum));
		// }
		// System.out.println();
		return norm(pr);
	}

	static final String prefix = "USER0000000000000";

	public static void main(String[] argv)
	{
		// for (int i = 0; i < PRIME.length; i++)
		// {
		// System.out.print(String.format("%d-%X\t", PRIME[i], PRIME[i]));
		// if ((i + 1) % 8 == 0)
		// System.out.println();
		// }

		// System.out.println("\n平均源");
		double pp = 0;
		int prime1 = 0x1000;
		// for (int e = 10; e < 31; e++, prime1 = (1 << e))

		int[] P = new int[] { 134218199, 134218327, 1048919, 134217943 };
		for (int j = 0; j < P.length; j++)
		{
			// prime1 = prime.get(prime1 + 1);
			prime1 = P[j];
			pp = 0;
			for (int i = 1; i <= 6; i++)
				pp += test(prime1, 1 << i, new cc()
				{
					final public String go(int i)
					{
						return i + "";
					}
				});

			// // System.out.println("\n平均源00000000000000000");
			// for (int i = 1; i <= 6; i++)
			// pp += test(prime1, 1 << i, new cc()
			// {
			// final public String go(int i)
			// {
			// return i + prefix;
			// }
			// });
			// System.out.println("\n00000000000000000平均源");
			for (int i = 1; i <= 6; i++)
				pp += test(prime1, 1 << i, new cc()
				{
					final public String go(int i)
					{
						return prefix + i;
					}
				});

			System.out.println(String.format("%d,%d,%X", (int) (pp * 1000), prime1, prime1));
		}
	}
}
