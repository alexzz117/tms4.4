package cn.com.higinet.tms.common;

public class BaseConcurrentTest {
	/** 常量 TOPIC. */
	protected static final String TOPIC = "test";

	private static final String RANDOM_STR = "1qaz2wsx3edc4rfv5tgb6yhn7ujm8ik,9ol.0p;/!@#$%^&*()";

	/** 常量 DEFAULT_TIMES. */
	protected static final int DEFAULT_TIMES = 5000000;

	protected static final int DEFAULT_DATA_SIZE = 100;

	/** 常量 THREAD_COUNT. */
	protected static final int THREAD_COUNT = 20;

	/**
	 * Creates the threads.
	 *
	 * @param size the size
	 * @param run the run
	 * @return the thread[]
	 */
	protected Thread[] createThreads(int size, Runnable run) {
		Thread[] threads = new Thread[size];
		for (int i = 0; i < size; i++) {
			threads[i] = new Thread(run, "Test-Thread-" + i);
			threads[i].start();
		}
		return threads;
	}

	protected byte[] getData(int size) {
		StringBuilder sb = new StringBuilder(size * 2);
		for (int i = 0; i < size; i++) {
			sb.append(getSalt());
		}
		return sb.toString().getBytes();
	}

	private static char getSalt() {
		byte[] b = RANDOM_STR.getBytes();
		int len = b.length;
		int x = (int) (Math.random() * len);
		return (char) b[x];
	}
}
