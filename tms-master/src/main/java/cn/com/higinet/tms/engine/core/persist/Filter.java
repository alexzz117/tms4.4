package cn.com.higinet.tms.engine.core.persist;

public interface Filter<T> {
	public boolean filter(T t);
}
