package cn.com.higinet.tms;

public class ShutdownHook {

	public static void main( String args[] ) {
		Runtime.getRuntime().addShutdownHook( new Thread( new Runnable() {

			@Override
			public void run() {
				System.out.println( "ShutdownHook" );
			}
		} ) );
		
		System.out.println( "11111" );
	}

}
