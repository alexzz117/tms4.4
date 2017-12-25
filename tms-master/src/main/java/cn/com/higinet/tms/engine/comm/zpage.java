package cn.com.higinet.tms.engine.comm;


@SuppressWarnings("unchecked")
public final class zpage<E>
{
	public static interface comparator<E>
	{
		int compare(E e, Object o);
	}

	comparator<E> _compare;
	Object[] _buf;
	int _begin, _end, _page_size, _padding;

	public String toString()
	{
		StringBuffer sb = new StringBuffer(128);
		sb.append(size()).append(",").append(_begin).append(",").append(_end).append('[');
		for (int i = _begin; i < _end; i++)
			sb.append(_buf[i]).append(',');
		sb.append(']');
		return sb.toString();
	}

	public zpage<E> Clone()
	{
		zpage<E> ret = new zpage<E>(this._compare, this._page_size, this._padding);
		ret._begin = _begin;
		ret._end = _end;
		ret._padding = _padding;
		ret._buf = array_tool.copyOf(_buf, _buf.length);
		return ret;
	}

	public zpage(comparator<E> c, int page_size)
	{
		this(c, page_size, 90);
	}

	public zpage(comparator<E> c, int page_size, int padding)
	{
		_compare = c;
		this._page_size = page_size;
		this._padding = padding > 100 || padding < 50 ? 90 : padding;
		this._buf = new Object[page_size];
		_begin = _end = bal_min(0);
	}

	public int bal_min(int count)
	{
		return (_page_size - count) / 2;
	}

	public int bal_max(int count)
	{
		return bal_min(count) + count;
	}

	public int size()
	{
		return _end - _begin;
	}

	public boolean isempty()
	{
		return size() == 0;
	}

	public boolean isfull()
	{
		return size() >= max_size();
	}

	public int max_size()
	{
		return _page_size * _padding / 100;
	}

	public zpage<E> split()
	{
		return split(size() / 2);
	}

	public zpage<E> split(int count)
	{
		zpage<E> p = new zpage<E>(_compare, _page_size, _padding);
		p._begin = p._end = bal_max(count);
		System.arraycopy(_buf, _end - count, p._buf, p._begin -= count, count);
		fill_null(_end - count, _end);
		_end -= count;
		return p;
	}

	public zpage<E> merge(zpage<E> p)
	{
		if (p.isempty())
			return this;

		if (isempty())
			return p;

		return size() < p.size() ? p.copy(this) : copy(p);
	}

	public zpage<E> copy(zpage<E> p)
	{
		int count = p.size();
		if (this.isempty() || _compare.compare(get(0), p.get(0)) > 0)
		{
			if (_begin < count)
			{
				int c = size();
				int e = bal_max(c + count);
				mv(_begin, _end, e - c);
				_end = e;
				_begin = e - c;
			}

			System.arraycopy(p._buf, p._begin, _buf, _begin -= count, count);
		}
		else
		{
			if (_page_size - _end < count)
			{
				int c = size();
				int b = bal_min(c + count);
				mv(_begin, _end, _begin = b);
				_end = _begin + c;
			}

			System.arraycopy(p._buf, p._begin, _buf, _end, count);
			_end += count;
		}

		p.clear();
		return this;
	}

	public int find(Object e)
	{
		int i = index(e);
		return i > 0 ? i - _begin : i + _begin;
	}

	public E get(int index)
	{
		if (index < 0 || index >= size())
			return null;

		return (E) _buf[_begin + index];
	}

	public E get(E e)
	{
		int i = index(e);
		if (i < 0)
			return null;
		return (E) _buf[i];
	}

	public E remove(E e)
	{
		int i = index(e);
		if (i < 0)
			return null;

		return remove(i - _begin);
	}

	public E remove(int index)
	{
		if (index < 0 || index >= size())
			return null;
		index += _begin;

		E ret = (E) _buf[index];
		if (index - _begin < _end - index)
		{
			mv(_begin, index, _begin + 1);
			_buf[_begin++] = null;
		}
		else
		{
			mv(index + 1, _end, index);
			_buf[--_end] = null;
		}

		return ret;
	}

	public E set(int i, E e)
	{
		i += _begin;
		E ret = (E) _buf[i];
		_buf[i] = e;
		return ret;
	}

	public void add(int i, E e)
	{
		if (_begin == 0 || _end == _page_size)
			balance();
		i += _begin;
		if (i - _begin < _end - i && _begin > 0)
		{
			mv(_begin, i, _begin - 1);
			--_begin;
			_buf[i - 1] = e;
		}
		else
		{
			mv(i, _end, i + 1);
			++_end;
			_buf[i] = e;
		}
	}

	public int add(E e)
	{
		if (size() == _page_size)
			return -1;

		int i = index(e);
		if (i >= 0)
		{
			_buf[i] = e;
			return 0;
		}

		i = -1 - i;

		add(i - _begin, e);
		return 1;
	}

	public void clear()
	{
		fill_null(_begin, _end);
		_begin = _end = bal_min(0);
	}

	void balance()
	{
		int c = _end - _begin;
		mv(_begin, _end, _begin = bal_min(c));
		_end = _begin + c;
	}

	static final int min(int i1, int i2)
	{
		return i1 > i2 ? i2 : i1;
	}

	static final int max(int i1, int i2)
	{
		return i1 > i2 ? i1 : i2;
	}

	private void fill_null(int b, int e)
	{
		fill_null(_buf, b, e);
	}

	private void fill_null(Object[] buff, int b, int e)
	{
		for (; b < e;)
			buff[b++] = null;
	}

	private void mv(int b, int e, int to)
	{
		if (b == to || b == e)
			return;
		System.arraycopy(this._buf, b, _buf, to, e - b);
		if (b > to)
			fill_null(max(b, to + (e - b)), e);
		else
			fill_null(b, min(to, e));
	}

	private int index(Object key)
	{
		int low = _begin;
		int high = _end - 1;

		while (low <= high)
		{
			int mid = (low + high) >>> 1;
			E midVal = (E) _buf[mid];
			int cmp = _compare.compare((E) midVal, key);

			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1); // key not found.
	}

	public void remove_if(func_if<E> func)
	{
		if (func == null)
			return;
		for (int i = _begin; i < _end;i++)
		{
			if (func._if((E)this._buf[i]))
			{
				if (i - _begin < _end - i)
				{
					mv(_begin, i, _begin + 1);
					_buf[_begin++] = null;
				}
				else
				{
					mv(i + 1, _end, i);
					_buf[--_end] = null;
				}
			}
		}

	}

	static void page_test()
	{
		zpage<Integer> p = new zpage<Integer>(new comparator<Integer>()
		{
			public int compare(Integer e, Object o)
			{
				if (e == null)
					return -1;
				if (o == null)
					return 1;
				return e - (Integer) o;
			}
		}, 20, 75);

		// for(int xx=0;xx<10000;)
		{
			for (int i = 0; i < 20; i++)
				p.add(i);

			for (int i = 4; i < 20; i++)
			{
				Integer o = p.remove(new Integer(i));
				if (!o.equals(i))
				{
					System.out.print(".......");
				}

			}
			p.clear();
		}

		{
			for (int i = 0; i < 50; i++)
				p.add(i);

			zpage<Integer> x;
			{
				x = p.split();
				p.copy(x);
			}
			{
				x = p.split(10);
				p.copy(x);
			}
			{
				x = p.split(10);
				x.copy(p);
				p.copy(x);
			}
			{
				x = p.split();
				p.copy(x);
			}

			p.remove(0);
			p.remove(6);
			p.remove(10);
			p.remove(14);
			p.remove(19);

			p.clear();
		}

		{
			for (int i = 0; i < 20; i++)
				p.add(i);

			zpage<Integer> x = p.split();
			p.copy(x);

			p.clear();
		}

		for (int i = 0; i < 100; i += 10)
			p.add(i);

		@SuppressWarnings("unused")
		int pos = p.find(69);
		pos = p.find(-1);

		p.add(22);
		p.add(21);
		p.add(2);
		p.add(88);
		for (int i = 0; i < 10; i++)
			p.add(2 + i);

		for (int i = 40; i < 70; i++)
			p.remove(i);
		for (int i = 0; i < 10; i++)
			p.remove(i);

		p.remove(80);
		p.remove(88);
		p.remove(90);

		p.remove(0);
		p.remove(10);
	}

	public static void main(String args[])
	{
		page_test();
	}
}
