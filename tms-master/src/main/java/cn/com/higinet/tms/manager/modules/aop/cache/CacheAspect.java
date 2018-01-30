package cn.com.higinet.tms.manager.modules.aop.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.com.higinet.tms.manager.modules.auth.exception.TmsMgrAuthDepException;
import cn.com.higinet.tms.manager.modules.auth.service.AuthService;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;

@Aspect
@Component("cacheAspect")
public class CacheAspect {

	@Autowired
	private AuthService authService;

	@Pointcut("(execution(* cn.com.higinet.tms.manager.modules.*.service.*.importIpLocation(..))) || (execution(* cn.com.higinet.tms.manager.modules.*.service.*.importRoster(..))) || (execution(* cn.com.higinet.tms.manager.modules.*.service.*.save*(..))) || (execution(* cn.com.higinet.tms.manager.modules.auth.service.AuthServiceImpl.batchUpadteAuth(..))) || (execution(* cn.com.higinet.tms.manager.modules.*.service.*.update*(..))) || (execution(* cn.com.higinet.tms.manager.modules.*.service.*.create*(..))) || (execution(* cn.com.higinet.tms.manager.modules.*.service.*.delete*(..)))")
	public void executeService() {

	}

	/**
	 * 在目标service方法执行前执行
	 * @param point
	 */
	@SuppressWarnings("unchecked")
	@Before("executeService()")
	public void runOnBefor( JoinPoint point ) {
		String methodName = point.getSignature().getName(); //获取方法名

		//获取参数
		Object[] args = point.getArgs();
		Object[] args2 = new Object[args.length];
		for( int i = 0; i < args.length; i++ ) {
			args2[i] = keyIgnoreCase( args[i] );
		}

		//判断是当前操作是否为授权中心中的授权操作
		if( methodName.equals( "batchUpadteAuth" ) ) { //是
			//获取参数
			Map<String, String> authArg = (Map<String, String>) args2[0];
			String authIds = MapUtil.getString( authArg, "AUTH_IDS" );

			if( !StringUtil.isEmpty( authIds ) ) {
				String[] authIdArr = authIds.split( "," );

				//检查交易配置/其他模块的依赖
				for( String authId : authIdArr ) {
					String depInfo = authService.getDependencyInfo( authId );
					if( !StringUtil.isEmpty( depInfo ) ) {
						throw new TmsMgrAuthDepException( depInfo );
					}
				}
			}

		}
	}

	/**
	 * 在目标service方法执行后执行
	 * @param point
	 */
	@After("executeService()")
	public void runOnAfter( JoinPoint point ) {
		String methodName = point.getSignature().getName(); //获取方法名

		//获取参数
		Object[] args = point.getArgs();
		Object[] args2 = new Object[args.length];
		for( int i = 0; i < args.length; i++ ) {
			args2[i] = keyIgnoreCase( args[i] );
		}

		//判断是当前操作是否为授权中心中的授权操作
		if( methodName.equals( "batchUpadteAuth" ) ) { //是
			authService.doAuth( args2 );
		}
		else { //不是
			authService.doApply( args2, methodName );
		}
	}

	/**
	 * 忽略Map参数key的大小写
	 * @param args
	 * @return
	 */
	@SuppressWarnings("all")
	private Object keyIgnoreCase( Object arg ) {
		if( arg instanceof Map ) {
			Map mapArg = (Map) arg;
			Object add = mapArg.get( "add" );
			Object mod = mapArg.get( "mod" );
			Object del = mapArg.get( "del" );
			Object copy = mapArg.get( "copy" );
			Object valid_y = mapArg.get( "valid-y" );
			Object valid_n = mapArg.get( "valid-n" );

			Map mapArg2 = new CaseInsensitiveMap( mapArg );
			if( add instanceof List || mod instanceof List || del instanceof List || copy instanceof List ) {
				if( add != null ) {
					List<Map<String, Object>> addList = (List<Map<String, Object>>) add;
					List<Map<String, Object>> addList2 = new ArrayList<Map<String, Object>>();
					for( int i = 0; i < addList.size(); i++ ) {
						addList2.add( new CaseInsensitiveMap( addList.get( i ) ) );
					}
					mapArg2.put( "add", addList2 );
				}
				if( copy != null ) {
					List<Map<String, Object>> copyList = (List<Map<String, Object>>) copy;
					List<Map<String, Object>> copyList2 = new ArrayList<Map<String, Object>>();
					for( int i = 0; i < copyList.size(); i++ ) {
						copyList2.add( new CaseInsensitiveMap( copyList.get( i ) ) );
					}
					mapArg2.put( "copy", copyList2 );
				}
				if( mod != null ) {
					List<Map<String, Object>> modList = (List<Map<String, Object>>) mod;
					List<Map<String, Object>> modList2 = new ArrayList<Map<String, Object>>();
					for( int i = 0; i < modList.size(); i++ ) {
						modList2.add( new CaseInsensitiveMap( modList.get( i ) ) );
					}
					mapArg2.put( "mod", modList2 );
				}
				if( del != null ) {
					List<Map<String, Object>> delList = (List<Map<String, Object>>) del;
					List<Map<String, Object>> delList2 = new ArrayList<Map<String, Object>>();
					for( int i = 0; i < delList.size(); i++ ) {
						delList2.add( new CaseInsensitiveMap( delList.get( i ) ) );
					}
					mapArg2.put( "del", delList2 );
				}
				if( valid_y != null ) {
					List<Map<String, Object>> valid_yList = (List<Map<String, Object>>) valid_y;
					List<Map<String, Object>> valid_yList2 = new ArrayList<Map<String, Object>>();
					for( int i = 0; i < valid_yList.size(); i++ ) {
						valid_yList2.add( new CaseInsensitiveMap( valid_yList.get( i ) ) );
					}
					mapArg2.put( "valid-y", valid_yList2 );
				}
				if( valid_n != null ) {
					List<Map<String, Object>> valid_nList = (List<Map<String, Object>>) valid_n;
					List<Map<String, Object>> valid_nList2 = new ArrayList<Map<String, Object>>();
					for( int i = 0; i < valid_nList.size(); i++ ) {
						valid_nList2.add( new CaseInsensitiveMap( valid_nList.get( i ) ) );
					}
					mapArg2.put( "valid-n", valid_nList2 );
				}
			}
			return mapArg2;
		}

		return arg;
	}

	public int getOrder() {
		return 200;
	}
}
