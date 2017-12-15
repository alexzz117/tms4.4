package cn.com.higinet.tms35.comm;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.core.concurrent.tms_worker;
import cn.com.higinet.tms35.core.concurrent.tms_worker_base;

// TODO: Auto-generated Javadoc
/**
 * 专门用于打印transpin日志的队列.
 */
public class translog_worker extends tms_worker_base<TransPinLog> {

	/** The log. */
	private static Logger log = LoggerFactory.getLogger(translog_worker.class);

	/** The Constant isLogOn. */
	private final static boolean isLogOn = tmsapp.get_config("tms.trans.log.isOn", 0) == 1;

	/** The Constant deque_size. */
	private final static int deque_size = tmsapp.get_config("tms.trans.log.dequesize", 8192);

	/** The inst. */
	private static tms_worker<TransPinLog> inst;

	/**
	 * Worker.
	 *
	 * @return the tms_worker
	 */
	public static tms_worker<TransPinLog> worker() {
		if (inst != null)
			return inst;
		synchronized (translog_worker.class) {
			if (inst != null)
				return inst;
			return inst = new translog_worker("translog", deque_size);
		}
	}

	/**
	 * Instantiates a new translog_worker.
	 *
	 * @param name the name
	 * @param requst_max_size the requst_max_size
	 */
	public translog_worker(String name, int requst_max_size) {
		super(name, requst_max_size);
	}

	/** The list. */
	private List<TransPinLog> list = new ArrayList<TransPinLog>(1024);

	/** The line. */
	private int line = 0;

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.core.concurrent.tms_worker_base#run_once()
	 */
	@Override
	protected void run_once() {
		list.clear();
		this.drainTo(list, Integer.MAX_VALUE, 100);
		if (list.isEmpty())
			return;
		if (!isLogOn)
			return;
		for (TransPinLog pin : list) {
			if (line++ % 10000 == 0) {
				log.info(pin.helpString());
			}
			log.info(pin.toString());
		}
	}
}