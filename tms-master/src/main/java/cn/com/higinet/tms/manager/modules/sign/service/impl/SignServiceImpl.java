package cn.com.higinet.tms.manager.modules.sign.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.util.MapWrap;
import cn.com.higinet.tms.manager.modules.common.StaticParameters.ActionCode;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;
import cn.com.higinet.tms.manager.modules.exception.TmsMgrServiceException;
import cn.com.higinet.tms.manager.modules.mgr.service.ServerService;
import cn.com.higinet.tms.manager.modules.sign.common.TmsMessageUtil;
import cn.com.higinet.tms.manager.modules.sign.common.jaxb.JaxbObjectAndXmlUtil;
import cn.com.higinet.tms.manager.modules.sign.model.MerchantRiskInfo;
import cn.com.higinet.tms.manager.modules.sign.service.SignService;

/**
 * @author zhang.lei
 */
@Service("signService")
public class SignServiceImpl implements SignService {
	private static final int SEND_TIMES = 3;// 发送次数

	@Autowired
	@Qualifier("tmsSimpleDao")
	private SimpleDao tmsSimpleDao;
	@Autowired
	private ServerService ServerService;

	@Override
	public List<Map<String, Object>> getUserAndBankMobileList(String userId) {
		String sql = "select distinct u.USERID, u.MOBILE, case when length(u.mobile) > 0 then (" + "select nvl(co.countryname, '未知') || '-' || nvl(r.regionname, '未知') || '-' || " + "nvl(c.cityname, '未知') from tms_mgr_mobile m left join TMS_MGR_CITY c on m.citycode "
				+ "= c.citycode left join TMS_MGR_REGION r on c.regioncode = r.regioncode left join " + "TMS_MGR_COUNTRY co on c.countrycode = co.countrycode where m.mobile = substr(" + "u.mobile, 0, 7)) else '' end moble_location, a.BANK_MOBILE, case when length("
				+ "a.bank_mobile) > 0 then (select nvl(co.countryname, '未知') || '-' || nvl(r.regionname, " + "'未知') || '-' || nvl(c.cityname, '未知') from tms_mgr_mobile m left join TMS_MGR_CITY c " + "on m.citycode = c.citycode left join TMS_MGR_REGION r on c.regioncode = r.regioncode "
				+ "left join TMS_MGR_COUNTRY co on c.countrycode = co.countrycode where m.mobile = substr(" + "a.bank_mobile, 0, 7)) else '' end bank_moble_location from TMS_RUN_USER u left join " + "TMS_RUN_ACCOUNT a on u.USERID = a.USERID where u.USERID = ? and length(a.BANK_MOBILE) > 0";
		return tmsSimpleDao.queryForList(sql, userId);
	}

	@Override
	public void refreshRiskServerSignCacheForMerchantRiskInfo(MerchantRiskInfo merchantRiskInfo) {
		if (merchantRiskInfo == null) {
			throw new TmsMgrServiceException("参数merchantRiskInfo[商户风险信息]不能为null");
		}

		if (StringUtil.isBlank(merchantRiskInfo.getMrchNo())) {
			throw new TmsMgrServiceException("参数merchantRiskInfo[商户风险信息]中的mrchNo[商户号]属性的值不能为空");
		}

		// 构造0006请求报文
		String message = TmsMessageUtil.composeMessage(ActionCode.UPDATE_SIGN_DATA, JaxbObjectAndXmlUtil.objectToXml(merchantRiskInfo));
		// 获取可用的风险评估服务列表
		List<Map<String, Object>> riskList = ServerService.listServer(MapWrap.map("SERVTYPE", "1").getMap());
		List<Map<String, Object>> copyList = copy(riskList);
		List<Map<String, Object>> errorList = copyList;

		// 最多向服务器发送3次,过程中只要全部发送成功则退出,如果3次都未成功,则认为失败服务器已不可用,忽略
		for (int i = 0, len = SEND_TIMES; i < len; i++) {
			errorList = TmsMessageUtil.callServer(message, errorList);// 向服务端发送报文,并返回报错信息
			if (errorList == null || errorList.isEmpty()) {
				break;
			}
		}
	}

	private List<Map<String, Object>> copy(List<Map<String, Object>> list) {
		List<Map<String, Object>> cpList = new ArrayList<Map<String, Object>>();
		if (list != null) {
			for (Map<String, Object> map : list) {
				Map<String, Object> cpMap = new HashMap<String, Object>();
				cpMap.putAll(map);
				cpList.add(cpMap);
			}
		}

		return cpList;
	}
}