package cn.com.higinet.tms.engine.comm;

import java.util.ArrayList;
import java.util.List;

public class set_tool {
	
	/*
	 * l1 ,l2 内元素必须唯一
	 * 结果为 A*B
	 * */
	public static <E>  void set_intersection(List<E> ret,List<E> l1,List<E> l2,java.util.Comparator<E> c)
	{
		int t;
		int i1=0,i2=0,len1=l1.size(),len2=l2.size();
		for(;i1<len1 && i2<len2;)
		{
			t=c.compare(l1.get(i1),l2.get(i2));
			if(t<0)
			{
				i1++;
			}
			else if(t>0)
			{
				i2++;
			}
			else
			{
				ret.add(l1.get(i1++));
				i2++;
			}
		}
	}

	/*
	 * l1 ,l2 内元素必须唯一
	 * 结果为 A-B
	 * */
	public static <E>  void set_difference(List<E> ret,List<E> l1,List<E> l2,java.util.Comparator<E> c)
	{
		int t;
		int i1=0,i2=0,len1=l1.size(),len2=l2.size();
		for(;i1<len1 && i2<len2;)
		{
			t=c.compare(l1.get(i1),l2.get(i2));
			if(t<0)
			{
				ret.add(l1.get(i1++));
			}
			else if(t>0)
			{
				i2++;
			}
			else
			{
				i1++;
				i2++;
			}
		}
		if(i1<len1)
			ret.addAll(l1.subList(i1, len1));
	}

	/*
	 * l1 ,l2 内元素必须唯一
	 * 结果为并集A+B
	 * */
	public static <E>  void set_union(List<E> ret,List<E> l1,List<E> l2,java.util.Comparator<E> c)
	{
		int t;
		int i1=0,i2=0,len1=l1.size(),len2=l2.size();
		for(;i1<len1 && i2<len2;)
		{
			t=c.compare(l1.get(i1),l2.get(i2));
			if(t<0)
			{
				ret.add(l1.get(i1++));
			}
			else if(t>0)
			{
				ret.add(l2.get(i2++));
			}
			else
			{
				ret.add(l1.get(i1++));
				i2++;
			}
		}
		if(i1<len1)
			ret.addAll(l1.subList(i1, len1));
		
		if(i2<len2)
			ret.addAll(l2.subList(i2, len2));
	}
	public static <E>  List<E> set_union(List<E> l1,List<E> l2,java.util.Comparator<E> c)
	{
		List<E> ret=new ArrayList<E>(l1.size()+l2.size());
		set_union(ret,l1,l2,c);
		return ret;
	}
	/*
	 * l1 ,l2 内元素必须唯一
	 * 结果为并集A+B-2(A*B),即两个集合同时存在的元素没有了
	 * */
	public static <E>  void set_symmetric_difference (List<E> ret,List<E> l1,List<E> l2,java.util.Comparator<E> c)
	{
		int t;
		int i1=0,i2=0,len1=l1.size(),len2=l2.size();
		for(;i1<len1 && i2<len2;)
		{
			t=c.compare(l1.get(i1),l2.get(i2));
			if(t<0)
			{
				ret.add(l1.get(i1++));
			}
			else if(t>0)
			{
				ret.add(l2.get(i2++));
			}
			else
			{
				i1++;
				i2++;
			}
		}
		if(i1<len1)
			ret.addAll(l1.subList(i1, len1));
		
		if(i2<len2)
			ret.addAll(l2.subList(i2, len2));
	}

	/*
	 * 结果是排序的
	 * l1 ,l2内的元素全部出现
	 * */

	public static <E>  void merge(List<E> ret,List<E> l1,List<E> l2,java.util.Comparator<E> c)
	{
		int t;
		int i1=0,i2=0,len1=l1.size(),len2=l2.size();
		for(;i1<len1 && i2<len2;)
		{
			t=c.compare(l1.get(i1),l2.get(i2));
			if(t<0)
			{
				ret.add(l1.get(i1++));
			}
			else if(t>0)
			{
				ret.add(l2.get(i2++));
			}
			else
			{
				ret.add(l1.get(i1++));
				ret.add(l2.get(i2++));
			}
		}
		if(i1<len1)
			ret.addAll(l1.subList(i1, len1));
		
		if(i2<len2)
			ret.addAll(l2.subList(i2, len2));
	}

	public static <E> void unique(List<E> list,java.util.Comparator<E> c)
	{
		if(list.size()<=1)
			return;
		
		int last=list.size();
		for(int i=0,ib;(ib=i)!=last && ++i!=last;)
		{
			if(0==c.compare(list.get(ib),list.get(i)))
			{
				for(;++i!=last;)
					if(0!=c.compare(list.get(ib),list.get(i)))
						list.set(++ib, list.get(i));
				
				for(int j=last-1;j!=ib;j--)
					list.remove(j);
			}
		}
	}
	
	public static <E> void unique(List<E> list)
	{
		if(list.size()<=1)
			return;

		int last=list.size()-1;
		for(int i=0,ib;(ib=i)!=last && ++i!=last;)
		{
			if(list.get(ib).equals(list.get(i)))
			{
				for(;++i!=last;)
					if(!list.get(ib).equals(list.get(i)))
						list.set(++ib, list.get(i));
				
				for(int j=last;j!=ib;j--)
					list.remove(j);
			}
		}
	}
	
	public static void main(String[] args) {

		List<Integer> list=new ArrayList<Integer>();
		List<Integer> list1=new ArrayList<Integer>();
			list1.add(0);
		list1.add(1);
		list1.add(1);
		list1.add(2);
		list1.add(2);
		list1.add(3);
		list1.add(4);
		list1.add(5);
		list1.add(5);

		List<Integer> list2=new ArrayList<Integer>();
		list2.add(0);
		list2.add(1);
		list2.add(1);
		list2.add(2);
		list2.add(2);
		list2.add(3);
		list2.add(4);
		list2.add(5);
		list2.add(5);
		list2.add(6);
		list2.add(6);
		list2.add(6);
		list2.add(6);
		list2.add(6);
		
		System.out.println(list);
	}
}
