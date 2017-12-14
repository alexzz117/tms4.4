package cn.com.higinet.tms35.core.persist;

public interface Filter<T> {
	public boolean filter(T t);
}
