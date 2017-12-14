package cn.com.higinet.tms.core.common;

public class hash
{
	static final public int clac(String s)
	{
		return clac(s,16777619);
	}

	static final public int clac(String s,int prime)
	{
    	int nr=0;
    	for(int i=0,len=s.length();i<len;i++)
    	{
        	nr*=prime;
    		nr^=0xff&s.charAt(i);
    	}

    	return nr; 		
	}

	static final public int clac(int id)
	{
    	int nr=0;
    	
    	nr =(id&0xff)^nr;
    	nr*=16777619;
    	id>>=8;

    	nr =(id&0xff)^nr;
    	nr*=16777619;
    	id>>=8;

    	nr =(id&0xff)^nr;
    	nr*=16777619;
    	id>>=8;

    	nr =(id&0xff)^nr;
        
        return nr;
	}
}
