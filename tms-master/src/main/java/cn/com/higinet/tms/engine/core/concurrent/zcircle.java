package cn.com.higinet.tms.engine.core.concurrent;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("unchecked")
public final class zcircle<E>
{
	java.util.Comparator<E> _compare;
	Object[] buf;
	int begin, end, size;

	public zcircle(int maxsize, Comparator<E> compare)
	{
		_compare = compare;
		begin = end = 0;
		buf = new Object[maxsize];
	}
	
	public void clear()
	{
		begin = end = 0;
		buf = new Object[buf.length];
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer(128);
		sb.append(buf.length).append(",").append(size()).append(",").append(begin).append(",")
				.append(end).append('[');
		for (int i = begin, c = 0; c < size; i++, c++)
		{
			if (i >= buf.length)
			{
				i = 0;
				sb.append("||,");
			}
			sb.append(buf[i]).append(',');
		}
		sb.append(']');
		return sb.toString();

	}

	private int upper_bound(E e)
	{
		if (_compare == null)
			return end;

		Object[] b = buf;
		for (int i = index_dec(end - 1), c = 0; c < size; c++)
		{
			if (_compare.compare((E) b[i], e) <= 0)
				return index_inc(i + 1);
			i = index_dec(i - 1);
		}

		return begin;
	}

	private void move(int from, int to)
	{
		int c = index_dec(end - from);
		if (c > 0)
		{
			int f = index(from + c - 1), t = index(to + c - 1);
			for (int i = 0; i < c; i++)
			{
				buf[t] = buf[f];
				t = index_dec(t - 1);
				f = index_dec(f - 1);
			}
		}
		end = index(end + to - from);
		size += index(to - from);
	}

	private void copy(int i, List<E> c, int b, int e)
	{
		for (; b < e; b++)
		{
			buf[i] = c.get(b);
			i = index_inc(i + 1);
		}
	}

	private int copy_to(Collection<E> c, int count)
	{
		count = count < size ? count : size;
		for (int f = begin, t = 0; t < count; t++)
		{
			c.add((E) buf[f]);
			f = index_inc(f + 1);
		}
		return count;
	}

	private void fill_null(int b, int count)
	{
		b = index(b);
		for (int i = 0; i < count; i++)
		{
			buf[b] = null;
			b = index_inc(b + 1);
		}
	}

	private int index_inc(int i)
	{
		return i >= buf.length ? i - buf.length : i;
	}

	private int index_dec(int i)
	{
		return (i < 0) ? i + buf.length : i;
	}

	private int index(int i)
	{
		return (i < 0) ? i + buf.length : i >= buf.length ? i - buf.length : i;
	}

	public void add_uncheck(E e)
	{
		int i = upper_bound(e);
		move(i, index_inc(i + 1));
		buf[i] = e;
	}

	public void add_first_uncheck(E e)
	{
		begin = index_dec(begin - 1);
		buf[begin] = e;
		size++;
	}

	public void add_uncheck(List<E> c, int b, int e)
	{
		int i = upper_bound(c.get(b));
		move(i, index_inc(i + (e - b)));
		copy(i, c, b, e);
	}

	public int add(E e)
	{
		if (isfull())
			return -1;
		add_uncheck(e);
		return 0;
	}

	public int add_first(E e)
	{
		if (isfull())
			return -1;
		add_first_uncheck(e);
		return 0;
	}

	public int add(List<E> c, int b, int e)
	{
		if (c.isEmpty())
			return -1;
		if (size() + (e - b) > max_size())
			return -1;

		add_uncheck(c, b, e);
		return 0;
	}

	public E get()
	{
		if (size <= 0)
			return null;

		E e = (E) buf[begin];
		buf[begin] = null;
		begin = index_inc(begin + 1);
		size--;
		return e;
	}

	public int get(Collection<E> c, int count)
	{
		if (count <= 0 || size == 0)
			return 0;

		count = copy_to(c, count);
		fill_null(begin, count);
		begin = index_inc(begin + count);
		size = index_dec(end - begin);

		return count;
	}

	public int max_size()
	{
		return buf.length;
	}

	public boolean isfull()
	{
		return size == buf.length;
	}

	public boolean isempty()
	{
		return size == 0;
	}

	public int size()
	{
		return size;
	}
}
