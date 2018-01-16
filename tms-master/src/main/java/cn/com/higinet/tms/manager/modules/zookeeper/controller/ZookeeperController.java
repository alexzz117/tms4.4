package cn.com.higinet.tms.manager.modules.zookeeper.controller;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.zookeeper.config.ZookeeperConfigProperties;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.util.Stringz;
import cn.com.higinet.tms.manager.modules.zookeeper.model.ZkNode;

@RestController
@RequestMapping("/zookeeper")
@RefreshScope
public class ZookeeperController {

	@Value("${spring.application.name}")
	String appName;

	@Value("${spring.profiles.active:unkown}")
	String active;

	@Autowired
	ZookeeperConfigProperties zookeeperConfigProperties;

	@Autowired
	CuratorFramework curator;

	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public Model list( @RequestBody ZkNode indata ) throws Exception {
		String path = "";
		if( Stringz.isEmpty( indata.getPath() ) ) path = this.getZookeeperPath();
		else path = indata.getPath();

		List<String> list = curator.getChildren().forPath( path );

		List<ZkNode> nodeList = Lists.newArrayList();
		for( String name : list ) {
			ZkNode node = new ZkNode();
			node.setLabel( name );
			node.setPath( (path + "/" + name) );
			node.setValue( new String( curator.getData().forPath( node.getPath() ) ) );
			nodeList.add( node );
		}

		return new Model().setRow( nodeList );
	}

	private String getZookeeperPath() {
		String path = "/" + zookeeperConfigProperties.getRoot();
		return path.replace( "//", "/" );
	}

	@RequestMapping(value = "/get", method = RequestMethod.POST)
	public Model get( @RequestBody ZkNode indata ) throws Exception {
		if( Stringz.isEmpty( indata.getPath() ) ) new Model().addError( "path is empty" );

		byte[] bytes = curator.getData().forPath( indata.getPath() );
		if( bytes != null ) {
			indata.setValue( new String( bytes ) );
		}
		return new Model().setRow( indata );
	}

	@RequestMapping(value = "/set", method = RequestMethod.POST)
	public Model set( @RequestBody ZkNode indata ) throws Exception {
		if( curator.checkExists().forPath( indata.getPath() ) == null ) {
			curator.create().forPath( indata.getPath() );
		}
		if( Stringz.isNotEmpty( indata.getValue() ) ) {
			curator.setData().forPath( indata.getPath(), indata.getValue().getBytes() );
		}
		return new Model();
	}

	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public Model delete( @RequestBody ZkNode indata ) throws Exception {
		if( Stringz.isEmpty( indata.getPath() ) ) new Model().addError( "path is empty" );
		if( curator.checkExists().forPath( indata.getPath() ) != null ) {
			curator.delete().forPath( indata.getPath() );
			return new Model();
		}
		else {
			return new Model().addError( "节点不存在" );
		}
	}
}
