package cn.com.higinet.tms35.service;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.comm.Request;
import cn.com.higinet.tms35.comm.Response;
import cn.com.higinet.tms35.comm.clock;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.comm.trans_pin;

public class io_env extends trans_pin {
	static Logger log = LoggerFactory.getLogger( io_env.class );
	static AtomicLong g_counter = new AtomicLong( 0 );
	AtomicBoolean returned;
	clock m_clock = new clock();
	Request req;
	Object res;

	public io_env( Request req, Response res ) {
		super();
		this.req = req;
		this.res = res;
		returned = new AtomicBoolean( false );
		this.pin_thread( INDEX_IO_THREADID );
		this.pin_time( INDEX_IO_ENV_INIT_END );
	}

	public Map<String, Object> getHeadMap() {
		return req.getHeadMap();
	}

	public TreeMap<String, String> getParameterMap() {
		return map_cast( req.getParameterMap() );
	}

	public Map<String, Object> getParameterOriMap() {
		return req.getParameterMap();
	}

	public void done() {
		done( null );
	}

	public void done( Exception ex ) {
		boolean is_returned = false;
		try {
			if( !returned.compareAndSet( false, true ) ) {
				is_returned = true;
				return;
			}
			if( res == null ) {
				is_returned = true;
				return;
			}
			this.pin_time( INDEX_RETURN_RESULT_BG );
		}
		catch( Exception e ) {
			log.error( "", e );
		}
		finally {
			if( !is_returned ) {
			}

		}

		// g_pool.execute(new Runnable()
		// {
		// public void run()
		// {
		// try
		// {
		// res.done(req, e);
		// }
		// catch (Exception e)
		// {
		// e.printStackTrace();
		// log.error(e);
		// }
		// }
		// });
	}

	public void setData( String name, Object value ) {
		if( returned.get() ) return;

	}

	public void setReturnCode( String code ) {
		if( returned.get() ) return;

	}

	protected TreeMap<String, String> map_cast( Map<String, Object> s ) {
		TreeMap<String, String> ret = new TreeMap<String, String>();
		map_cast( ret, s, "" );
		return ret;
	}

	@SuppressWarnings("unchecked")
	private void map_cast( Map<String, String> d, Map<String, Object> s, String prefix ) {
		if( !str_tool.is_empty( prefix ) ) prefix = prefix + '.';
		else prefix = "";

		for( Map.Entry<String, Object> e : s.entrySet() ) {
			String k = e.getKey();
			Object v = e.getValue();

			if( v instanceof String ) {
				d.put( prefix + k, str_tool.to_str( v ) );
			}
			else if( v instanceof Map ) {
				map_cast( d, (Map<String, Object>) v, k );
			}
		}
	}

	public TreeMap<String, Object> getResultData() {
		return null;
	}

	public String getResultCode() {
		return null;
	}
}
