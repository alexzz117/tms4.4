package cn.com.higinet.tms.engine.comm;


public class prime
{
	static int get(int m)
	{
		for (;; m++)
		{
			if (is_prime(m))
				return m;
		}

	}

	static private boolean is_prime(int m)
	{
		double sqrt=Math.sqrt(m);
		for(int i=2;i<sqrt;i++)
		{
			if(m%i==0)
				return false;
		}
		return true;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
//		db_user u=new db_user();
			
		int p=1;
		for(int i=0;i<100;i++)
		{
			System.out.print(p=get(p));
			System.out.print(String.format("-%o",p));
			p+=1;
			System.out.print("\t");
			if((i+1)%8==0)
				System.out.println();
		}
	}

}
