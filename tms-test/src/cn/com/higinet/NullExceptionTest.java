package cn.com.higinet;

public class NullExceptionTest {
	public static void main(String[] args) {
		int i = 1;
		while (i < 2000000) {
			
			try {
				Long l = null;
				l.toString();
			} catch (Exception e) {
				//System.out.println("getStackTrace:" + e.getStackTrace());
				if (e.getStackTrace().length == 0) {
					System.out.println("--------------count is:" + i + "----------------------------");
					break;
				}
			}
			i++;
		}
	}
}
