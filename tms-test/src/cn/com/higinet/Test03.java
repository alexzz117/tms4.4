package cn.com.higinet;

import java.util.HashMap;
import java.util.Map;

public class Test03 {

	public static void main(String[] args) {
		Map<String, Long> map = new HashMap();

		for (int i = 0; i < 10000; i++) {
			map.put("USER" + i, System.currentTimeMillis());
		}

	}

}
