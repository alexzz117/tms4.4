package cn.com.higinet.tms.engine.core.concurrent;

import java.util.List;

public abstract class tms_worker<E>
{
	public abstract void start();

	public abstract void shutdown(boolean abort);

	public abstract void join();

	public abstract boolean request(E e);

	public abstract boolean request(List<E> el);

	public abstract boolean request(List<E> el, int b, int e);

	public abstract String name();

	public abstract void setup(String[] string);
}
