package cn.com.higinet.tms35.core.cache;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public final class linear<V> implements Iterable<V>
{
	public ArrayList<V> m_list;
	public java.util.Comparator<V> m_comp = null;

	public String toString()
	{
		return m_list.toString();
	}

	public linear(java.util.Comparator<V> comp, int cap)
	{
		m_list = new ArrayList<V>(cap);
		m_comp = comp;

	}

	public linear(java.util.Comparator<V> comp)
	{
		m_comp = comp;
		m_list = new ArrayList<V>();
	}

	public linear()
	{
		m_list = new ArrayList<V>();
	}

	public linear(linear<V> list)
	{
		m_comp = list.m_comp;
		m_list = new ArrayList<V>(list.m_list);
	}

	public linear(Comparator<V> comp, List<V> list)
	{
		m_comp = comp;
		m_list = new ArrayList<V>(list);
	}

	public void add(V v)
	{
		m_list.add(v);
	}

	public int lower_bound(V v)
	{
		int _First = 0;
		int _Count = this.size();
		for (; 0 < _Count;)
		{ // divide and conquer, find half that contains answer
			int _Count2 = _Count / 2;
			int _Mid = _First + _Count2;

			if (m_comp.compare(this.get_uncheck(_Mid), v) < 0)
			{
				_First = ++_Mid;
				_Count -= _Count2 + 1;
			}
			else
				_Count = _Count2;
		}
		return (_First);

	}

	public int upper_bound(V v)
	{
		int _First = 0;
		int _Count = this.size();

		for (; 0 < _Count;)
		{ // divide and conquer, find half that contains answer
			int _Count2 = _Count / 2;
			int _Mid = _First + _Count2;

			if (m_comp.compare(get(_Mid), v) <= 0)
			{
				_First = ++_Mid;
				_Count -= _Count2 + 1;
			}
			else
				_Count = _Count2;
		}
		return (_First);
	}

	public int index(V v)
	{
		return java.util.Collections.binarySearch(m_list, v, m_comp);
	}

	public void sort()
	{
		java.util.Collections.sort(m_list, m_comp);
	}

	public V get(int index)
	{
		if (index < 0 || index >= m_list.size())
			return null;

		return m_list.get(index);
	}

	public V get_uncheck(int index)
	{
		return m_list.get(index);
	}

	public V get(V v)
	{
		return get(index(v));
	}

	public void clear()
	{
		m_list.clear();
	}

	public void set(int index, V v)
	{
		for (; index >= m_list.size();)
			m_list.add(null);

		m_list.set(index, v);
	}

	public int size()
	{
		return m_list.size();
	}

	public void addAll(java.util.Collection<V> linear)
	{
		if (linear != null)
			m_list.addAll(linear);
	}

	public linear<V> sub(int first, int last)
	{
		linear<V> lin = new linear<V>(this.m_comp, last - first);
		for (; first < last; first++)
			lin.add(this.get_uncheck(first));
		return lin;
	}

	public Iterator<V> iterator()
	{
		return m_list.iterator();
	}

	public void addAll(linear<V> linear)
	{
		if (linear != null)
			m_list.addAll(linear.m_list);
	}

	public void insert(V vv)
	{
		int index = this.lower_bound(vv);
		m_list.add(index, vv);
	}

	static public void main(String[] v)
	{
		linear<Integer> li = new linear<Integer>(new Comparator<Integer>()
		{
			public int compare(Integer o1, Integer o2)
			{
				return o1 - o2;
			}
		});

		li.add(0);
		li.add(1);
		li.add(2);

		for (int i = -1; i < 4; i++)
		{
			System.out.println(i + ":" + li.lower_bound(i));
			System.out.println(i + ":" + li.upper_bound(i));
		}

	}
}
