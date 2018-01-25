package cn.com.higinet;

public class Test1 {

	public static String retString(String x) {
		int j = 0;
		int i = 0;
		StringBuffer y = new StringBuffer(x);

		try {

			while (y.charAt(i) != '\0') {
				if (y.charAt(i) != ' ') {
					y.setCharAt(j, y.charAt(i));
					i++;
					j++;
				} else {
					y.setCharAt(j, y.charAt(i));
					i++;
					j++;
					while (y.charAt(i) == ' ')
						i++;
				}
			}
			y.setCharAt(j, '\0');

		} finally {

			System.out.println("lalalalololo ");
		}

		return y.toString();

	}

	public static void main(String[] args) {
		long start = System.currentTimeMillis();

		
		System.out.println("------->1000--------:" + start);

		//Test1.retString("000");
		
		String ss="12345";
		System.out.println("-----------:" + ss.substring(0,6));
		
		long end = System.currentTimeMillis();
		long constime = end - start;
		if (constime > 100) {
			System.out.println("------->1000--------:" + end);
		}
	}

}