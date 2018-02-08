package cn.com.higinet.tms.manager.modules.zookeeper.controller;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.zookeeper.config.ZookeeperConfigProperties;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.google.common.collect.Lists;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.util.Stringz;
import cn.com.higinet.tms.manager.common.ManagerConstants;
import cn.com.higinet.tms.manager.modules.zookeeper.model.ZkNode;

@RestController
@RequestMapping(ManagerConstants.URI_PREFIX + "/zookeeper")
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
		boolean isRoot = Stringz.isEmpty( indata.getPath());
		if( isRoot ) path = this.getRootPath();
		else path = indata.getPath();

		List<ZkNode> nodeList = Lists.newArrayList();
		for( String name : curator.getChildren().forPath( path ) ) {
			ZkNode node = new ZkNode();
			String currPath = path + "/" + name;
			List<String> childrenPaths = curator.getChildren().forPath( currPath );

			node.setLeaf(CollectionUtils.isEmpty(childrenPaths));
			node.setLabel( name );
			node.setPath( currPath );
			node.setValue( new String( curator.getData().forPath( node.getPath() ) ) );
			node.setRoot(isRoot);
			nodeList.add( node );
		}
		return new Model().setRow( nodeList );
	}

	private String getRootPath() {
		String path = "/" + zookeeperConfigProperties.getRoot();
		return path.replace( "//", "/" );
	}

	@RequestMapping(value = "/get", method = RequestMethod.POST)
	public Model get( @RequestBody ZkNode indata ) throws Exception {
		if( Stringz.isEmpty( indata.getPath() ) ) new Model().addError( "path is empty" );
		if( curator.checkExists().forPath( indata.getPath() ) == null ) return new Model().addError( "节点不存在" );

		indata.setLabel( indata.getPath().substring( indata.getPath().lastIndexOf( "/" ) + 1, indata.getPath().length() ) );
		byte[] bytes = curator.getData().forPath( indata.getPath() );
		if( bytes != null ) {
			indata.setValue( new String( bytes ) );
		}
		return new Model().setRow( indata );
	}

	@RequestMapping(value = "/set", method = RequestMethod.POST)
	public Model set( @RequestBody ZkNode indata ) throws Exception {
		if( curator.checkExists().forPath( indata.getPath() ) == null ) {
//			curator.create().forPath( indata.getPath() );
			curator.createContainers(indata.getPath());
		}
		if(StringUtils.equals(indata.getType(), "node")) {
			String subPath = indata.getPath() + "/foo";
			curator.create().forPath( subPath );
			curator.setData().forPath( subPath, "bar".getBytes() );
		} else {
			if( Stringz.isNotEmpty( indata.getValue() ) ) {
				curator.setData().forPath( indata.getPath(), indata.getValue().getBytes() );
			}
		}

		return new Model();
	}

	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public Model delete( @RequestBody List<ZkNode> indata ) throws Exception {
		for(ZkNode zkNode : indata) {
			if( Stringz.isEmpty( zkNode.getPath() ) ){
				new Model().addError( "path is empty" );
			}
			if( curator.checkExists().forPath( zkNode.getPath() ) == null ){
				return new Model().addError( "节点不存在" );
			}
		}
		for(ZkNode zkNode : indata) {
			Stat stat = new Stat();
			curator.delete().deletingChildrenIfNeeded().forPath(zkNode.getPath());
		}
		return new Model();
	}
	
	@RequestMapping(value = "/import", method = RequestMethod.POST)
	public Model importThread( String rootPath, MultipartFile zkFile, HttpServletRequest request ) {
		String fileName = zkFile.getOriginalFilename();

		Properties pps = new Properties();
		try{
			pps.load(zkFile.getInputStream());

			Set<String> propNameSet = pps.stringPropertyNames();
			for(String propName : propNameSet) {
				String value = pps.getProperty(propName);
				String path = rootPath + "/" + propName.replaceAll("\\.", "/");
				ZkNode zkNode = new ZkNode();
				zkNode.setPath(path);
				zkNode.setType("prop");
				zkNode.setValue(value);

				Model model = set(zkNode);
				System.out.println(model);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}


		return new Model();
	}

}
