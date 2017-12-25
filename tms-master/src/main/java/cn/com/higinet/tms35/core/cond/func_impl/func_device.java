package cn.com.higinet.tms35.core.cond.func_impl;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import cn.com.higinet.rapid.base.util.IdUtil;
import cn.com.higinet.tms35.dataSyncLog;
import cn.com.higinet.tms35.comm.DeviceUtil;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.comm.tm_tool;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.core.bean;
import cn.com.higinet.tms35.core.cache.db_cache;
import cn.com.higinet.tms35.core.cache.db_device;
import cn.com.higinet.tms35.core.cache.db_device_user;
import cn.com.higinet.tms35.core.cache.db_dfp_application;
import cn.com.higinet.tms35.core.cache.linear;
import cn.com.higinet.tms35.core.cond.func;
import cn.com.higinet.tms35.core.dao.dao_base;
import cn.com.higinet.tms35.core.dao.dao_combin;
import cn.com.higinet.tms35.core.dao.dao_device_user;
import cn.com.higinet.tms35.core.dao.dao_seq;
import cn.com.higinet.tms35.run.run_env;

public class func_device implements func {
	private static boolean RANDOM_OUT = tmsapp.get_config("tms.dfp.random.isout", 0) == 1;

	private static boolean RANDOM_CHANGE = tmsapp.get_config("tms.dfp.random.change", 0) == 1;

	private static Logger log = LoggerFactory.getLogger(func_device.class);

	@Override
	public Object exec(Object[] p, int n) {
		try {
			if (!db_device.IS_DEVFINGER_ON) {
				return null;
			}
			run_env re = (run_env) p[n - 1];
			String dev_finger = str_tool.to_str(p[n]);
			String token = re.m_channel_data.get("DEVICETOKEN");
			if (RANDOM_OUT && (token == null || token.length() == 0 || token.length() > 100)) {
				// log.error("token异常：外部传入时token值不能为空且长度不能超过100");
				return setReturnValues(re, null, -1, -1, null);
			}
			// token 是否存在
			String deviceFinger = DeviceUtil.getDeviceInfo(dev_finger);
			String deviceType = DeviceUtil.getDeviceType(dev_finger);
			db_dfp_application app = null;
			String fingerToken = null;
			String tokenType = null;
			if (deviceType != null) {
				app = db_cache.get().application().get(deviceType);

				// /20170704
				if (app == null) {
					log.warn(String.format("Not found in table \"TMS_DFP_APPLICATION\" by device type \"%s\",received parameters is ", dev_finger));
					return null;
				}

				fingerToken = DeviceUtil.getDeviceToken(deviceFinger, app.token_type, app.app_id);
				tokenType = app.token_type;
			}
			String random = getRandomNum(token);
			String deviceToken = getDeviceToken(token, deviceFinger);
			String userId = re.get_user_id();
			// String userId = null;
			// //////////////////////////////////////
			// if(app != null){
			// app.token_type = DeviceUtil.TokenType.DEVICE_INFO_RANDOM;
			// }
			// System.out.println("交易状态:" + re.get_txn_status() + "认证状态："+
			// re.get_auth_status());
			// re.txnState = "1";
			// re.autoState = "0";
			// ////////////////////////////////////

			if (random != null) {
				db_device device = getDeviceByRandom(random, deviceFinger, app);
				db_device_user deviceUser;
				if (device != null) {
					deviceUser = getDeviceUser(re, device.device_id, userId);
					device.random_num = random;
					device = deviceExist(re, device, deviceUser, deviceFinger, app);
					return setReturnValues(re, device, device.match, deviceUser.status, tokenType);
				} else {
					if (RANDOM_OUT) {
						device = deviceNotExist(re, deviceFinger, random, app);
						return setReturnValues(re, device, -1, -1, tokenType);
					} else {
						return setReturnValues(re, null, -2, -1, tokenType);
					}
				}
			} else {
				if (deviceToken != null || fingerToken != null) {
					if (DeviceUtil.TokenType.NO_MATCH.equals(tokenType)) {
						db_device device = deviceNotExist(re, deviceFinger, random, app);
						return setReturnValues(re, device, -1, -1, tokenType);
					} else {
						db_device device;
						if (deviceToken != null || fingerToken != null) {
							device = getDeviceByToken(deviceToken == null ? fingerToken : deviceToken, deviceFinger, userId, app);
							if (device != null) {
								db_device_user deviceUser = getDeviceUser(re, device.device_id, userId);
								device = deviceExist(re, device, deviceUser, deviceFinger, app);
								return setReturnValues(re, device, device.match, deviceUser.status, tokenType);
							} else if (deviceToken == null) {
								device = deviceNotExist(re, deviceFinger, random, app);
								return setReturnValues(re, device, -1, -1, tokenType);
							} else if (deviceFinger == null) {
								return setReturnValues(re, null, -1, -1, null);
							} else {
								return setReturnValues(re, null, -2, -1, null);
							}
						} else {
							return setReturnValues(re, null, -1, -1, null);
						}
					}
				} else {
					return setReturnValues(re, null, -1, -1, null);
				}
			}
		} catch (Throwable e) {
			log.error("Execute func_device error.", e);
		}
		return null;
	}

	public db_device_user getDeviceUser(run_env re, long deviceId, String userId) {
		try {
			db_device_user deviceUser = re.get_dao_combin().stmt_device_user.read(deviceId, userId);
			deviceUser.device_id = deviceId;
			deviceUser.user_id = userId;
			return deviceUser;
		} catch (SQLException e) {
			log.error("", e);
			e.printStackTrace();
			return null;
		}
	}

	private db_device getDeviceByRandom(String randomNum, String finger, db_dfp_application app) {
		SqlRowSet rs = bean.get(dao_base.class).query("select DEVICE_ID, APP_ID, PROP_VALUES from TMS_DFP_DEVICE where RANDOM_NUM = ?", new Object[] { randomNum });
		if (rs.next()) {
			db_device device = new db_device();
			device.set_indb(true);
			device.device_id = rs.getLong("DEVICE_ID");
			device.app_id = rs.getString("APP_ID");
			device.prop_values = rs.getString("PROP_VALUES");
			;
			if (finger != null && finger.length() > 0) {
				device.match = DeviceUtil.fingerSimilarity(finger, device.prop_values, app);
			} else {
				device.match = -1;
			}
			return device;
		}
		return null;
	}

	public Object setReturnValues(run_env re, db_device device, double deviceState, double deviceUserState, String tokenType) {
		re.set_device_status(deviceState);
		re.set_user_device_status(deviceUserState);
		if (device != null) {
			if (RANDOM_OUT) {
				re.set_device_token(null);
			} else if (DeviceUtil.TokenType.DEVICE_INFO.equals(tokenType)) {
				re.set_device_token(device.channel_deviceid);
			} else if (DeviceUtil.TokenType.DEVICE_INFO_RANDOM.equals(tokenType)) {
				re.set_device_token(device.channel_deviceid + device.random_num);
			} else {
				re.set_device_token(device.channel_deviceid + device.random_num);
			}
		}
		return device == null ? device : device.device_id;
	}

	/**
	 * 
	 * @param token
	 * @param finger
	 * @param userId
	 * @param app
	 * @return
	 */
	private db_device getDeviceByToken(String token, String finger, String userId, db_dfp_application app) {
		SqlRowSet rs = null;
		if (!str_tool.is_empty(userId)) {
			rs = bean.get(dao_base.class).query("select d.DEVICE_ID, d.APP_ID, d.PROP_VALUES FROM tms_dfp_user_device u JOIN TMS_DFP_DEVICE d ON u.device_id = d.device_id WHERE user_id = ? AND channel_deviceid = ? ORDER BY u.lastmodified DESC", new Object[] { userId, token });
			if (rs.next()) {
				db_device device = new db_device();
				device.set_indb(true);
				device.device_id = rs.getLong("DEVICE_ID");
				device.app_id = rs.getString("APP_ID");
				device.prop_values = rs.getString("PROP_VALUES");
				;
				if (finger != null && finger.length() > 0) {
					device.match = DeviceUtil.fingerSimilarity(finger, device.prop_values, app);
				} else {
					device.match = -1;
				}
				return device;
			}
		}
		if (finger != null) {
			rs = bean.get(dao_base.class).query("select d.DEVICE_ID, d.APP_ID, d.PROP_VALUES FROM TMS_DFP_DEVICE d WHERE channel_deviceid = ? ORDER BY lastmodified DESC", new Object[] { token });
			db_device device = null;
			double simi = app.threshold;
			while (rs.next()) {
				String dbfinger = rs.getString("PROP_VALUES");
				double dsimi = DeviceUtil.fingerSimilarity(dbfinger, dbfinger, app);
				if (dsimi >= simi) {
					device = new db_device();
					device.set_indb(true);
					device.device_id = rs.getLong("DEVICE_ID");
					device.app_id = rs.getString("APP_ID");
					device.prop_values = rs.getString("PROP_VALUES");
					;
					simi = dsimi;
					if (dsimi == 1) {
						break;
					}
				}
			}
			if (device != null) {
				if (finger != null && finger.length() > 0) {
					device.match = DeviceUtil.fingerSimilarity(finger, device.prop_values, app);
				} else {
					device.match = -1;
				}
			}
			return device;
		} else {
			return null;
		}
	}

	/**
	 * 判断随机数是否存在
	 */
	private String getRandomNum(String token) {
		if (RANDOM_OUT) {
			if (!str_tool.is_empty(token)) {
				return token;
			} else {
				return null;
			}
		} else {
			if (!str_tool.is_empty(token) && token.length() == 64) {
				return token.substring(32, 64);
			} else {
				return null;
			}
		}
	}

	/**
	 * 
	 * @param token
	 * @param finger
	 * @param app
	 * @return
	 */
	private String getDeviceToken(String token, String finger) {
		if (str_tool.is_empty(token)) {
			return null;
		} else if (token.length() == 32) {
			return token;
		} else if (token.length() == 64) {
			return token.substring(0, 32);
		} else {
			return null;
		}
	}

	public db_device deviceNotExist(run_env re, String finger, String random, db_dfp_application app) {
		String txnStatus = re.m_channel_data.get("TXNSTATUS");
		if (finger != null && finger.length() > 0) {
			db_device device = createDevice(app, random, finger, re);
			db_device_user deviceUser = new db_device_user();
			deviceUser.device_id = device.device_id;
			deviceUser.user_id = re.get_user_id();
			if (txnStatus != null && txnStatus.length() > 0) {
				if ("1".equals(re.get_auth_status())) {
					deviceUser.status = 1;
				} else if ("0".equals(re.get_auth_status())) {
					if (deviceUser.status != 1) {
						deviceUser.status = 0;
					}
				} else if ("1".equals(txnStatus)) {
					deviceUser.status = 1;
				} else {
					if (deviceUser.status != 1) {
						deviceUser.status = 0;
					}
				}
			} else {
				if (deviceUser.status != 1) {
					deviceUser.status = 0;
				}
			}
			dbUpdate(re, device, deviceUser, finger, app);
			return device;
		} else {
			return null;
		}
	}

	public db_device deviceExist(run_env re, db_device device, db_device_user deviceUser, String finger, db_dfp_application app) {
		String txnStatus = re.m_channel_data.get("TXNSTATUS");
		if (txnStatus != null && txnStatus.length() > 0) {
			if ("1".equals(re.get_auth_status())) {
				deviceUser.status = 1;
			} else if ("0".equals(re.get_auth_status())) {
				if (deviceUser.status != 1) {
					deviceUser.status = 0;
				}
			} else if ("1".equals(txnStatus)) {
				deviceUser.status = 1;
			} else {
				if (deviceUser.status != 1) {
					deviceUser.status = 0;
				}
			}
			dbUpdate(re, device, deviceUser, finger, app);
			return device;
		} else {
			return device;
		}
	}

	public void dbUpdate(run_env re, db_device device, db_device_user deviceUser, String finger, db_dfp_application app) {
		dao_combin m_dc = re.get_dao_combin();
		String batch_code = null;
		if (db_device.isDevSyncLog) {
			batch_code = dataSyncLog.get_batch_code(re.get_thread_name(), tm_tool.lctm_ms());
			dataSyncLog.batch_print_begin(batch_code);
		}
		int updateNum = 0;
		String error = null;
		try {
			if (!deviceUser.is_indb() && db_device_user.maxDeviceOn) {
				dao_device_user daoDeviceUser = (dao_device_user) m_dc.stmt_device_user;
				linear<db_device_user> list = daoDeviceUser.readListByUser(deviceUser.user_id);
				if (app != null && list.size() >= app.max_devices) {
					deviceUser.old_device_id = list.get(list.size() - 1).device_id;
					deviceUser.set_indb(true);
				}
			}
			deviceUser.lastmodified = System.currentTimeMillis();
			// System.out.println(deviceUser.user_id +
			// "--------------------------" + deviceUser.device_id);
			if (deviceUser.user_id != null) {
				m_dc.stmt_device_user.update(batch_code, deviceUser);
				m_dc.stmt_device_user.flush();
				updateNum++;
			}
			if (!device.is_indb() || (RANDOM_CHANGE && app != null && finger != null) || (device.match < 1 && app != null && finger != null)) {
				if (!RANDOM_OUT && RANDOM_CHANGE) {
					device.random_num = IdUtil.uuid();
				}
				device.prop_values = finger;
				device.channel_deviceid = DeviceUtil.getDeviceToken(finger, app.token_type, app.app_id);
				m_dc.stmt_device.update(batch_code, device);
				m_dc.stmt_device.flush();
				updateNum++;
			}
			m_dc.stmt_rule_hit.commit();
		} catch (SQLException e) {
			log.error("dbUpdate异常", e);
			m_dc.stmt_device.reset_update_pos();
			error = e.getLocalizedMessage();
		} finally {
			if (db_device.isDevSyncLog) {
				dataSyncLog.batch_print_end(batch_code, updateNum, (error == null ? dataSyncLog.Action.COMMIT : dataSyncLog.Action.ROLLBACK));
			}
		}
	}

	private db_device createDevice(db_dfp_application app, String random, String finger, run_env re) {
		db_device device = new db_device();
		device.app_id = app.app_id;
		device.device_id = dao_seq.get("SEQ_TMS_DFP_DEVICE").next();
		device.channel_deviceid = DeviceUtil.getDeviceToken(finger, app.token_type, app.app_id);
		if (!RANDOM_OUT) {
			device.random_num = IdUtil.uuid();
		} else {
			device.random_num = random;
		}
		device.prop_values = finger;
		device.match = -1;
		return device;
	}

}