package cn.com.higinet.tms.base.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Classz {
	/**
	 * @Description: 根据一个接口返回该接口的所有类 
	 * @param c 接口 
	 * @return List<Class> 实现接口的所有类 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	*/
	@SuppressWarnings("rawtypes")
	public static List<Class<?>> getAllClassByInterface( Class<?> c, String packageName, String... exception ) throws Exception {
		List<Class<?>> returnClassList = new ArrayList<Class<?>>();
		//判断是不是接口,不是接口不作处理  
		if( c.isInterface() ) {
			List<Class> allClass = getClasses( packageName, exception );//获得当前包以及子包下的所有类  
			//判断是否是一个接口  
			for( int i = 0; i < allClass.size(); i++ ) {
				if( c.isAssignableFrom( allClass.get( i ) ) ) {
					if( !c.equals( allClass.get( i ) ) ) {
						returnClassList.add( allClass.get( i ) );
					}
				}
			}
		}
		return returnClassList;
	}

	/** 
	 *  
	* @Description: 根据包名获得该包以及子包下的所有类不查找jar包中的
	* @param pageName 包名 
	* @return List<Class> 包下所有类 
	 */
	@SuppressWarnings("rawtypes")
	private static List<Class> getClasses( String packageName, String... exception ) throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		String path = packageName.replace( ".", "/" );
		Enumeration<URL> resources = classLoader.getResources( path );
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			String newPath = resource.getFile().replace( "%20", " " );
			dirs.add( new File( newPath ) );
		}
		ArrayList<Class> classes = new ArrayList<Class>();
		for( File directory : dirs ) {
			classes.addAll( findClass( directory, packageName, exception ) );
		}
		return classes;
	}

	@SuppressWarnings("rawtypes")
	private static List<Class> findClass( File directory, String packageName, String... exception ) throws ClassNotFoundException {
		List<Class> classes = new ArrayList<Class>();
		

		if( exception != null ) {
			for( String ex : exception ) {
				if( packageName.startsWith( ex ) ) return classes;
			}
		}

		if( !directory.exists() ) {
			return classes;
		}

		File[] files = directory.listFiles();
		for( File file : files ) {
			if( file.isDirectory() ) {
				assert !file.getName().contains( "." );
				classes.addAll( findClass( file, packageName + "." + file.getName(), exception ) );
			}
			else if( file.getName().endsWith( ".class" ) ) {
				String className = packageName + "." + file.getName().substring( 0, file.getName().length() - 6 );
				classes.add( Class.forName( className ) );
			}
		}
		return classes;
	}
}
