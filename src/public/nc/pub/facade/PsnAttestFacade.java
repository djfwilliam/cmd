package nc.pub.facade;

import org.apache.commons.lang.StringUtils;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.saas.IPsnAttestService;
import nc.itf.saas.pub.MessageResult;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class PsnAttestFacade {


	/**
	 * 获取参照
	 * 包括证明类型和证明处理人
	 * @return
	 */
	public static String getDocRef(String paramJson){
		
		MessageResult result = new MessageResult(MessageResult.STATUS_SUCCESS,"操作成功！");
		JSONObject jo = new JSONObject() ;
		try {
			IPsnAttestService service = NCLocator.getInstance().lookup(IPsnAttestService.class);
			JSONObject param = JSONObject.fromObject(paramJson);
			String docList = param.getString("pkDocList");
//			String applyRole = param.getString("applyRoleCode");
			JSONArray docArr = service.getAttestType(docList/**"1002ZZ10000000001VXU"**/);
			JSONArray applyerArr = service.getApplyer(param.toString());
			
			if(docArr.size() == 0){
				result.setMessage("没有找到定义的证明类型。");
				result.setStatusCode(MessageResult.STATUS_ERROR);
			}
			jo.put("attestType", docArr);
			jo.put("applyer", applyerArr);
			result.setData(jo);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			result.setMessage(e.getMessage());
			result.setStatusCode(MessageResult.STATUS_ERROR);
		}
		
		return JSONObject.fromObject(result).toString();
	}
	
	
	/**
	 * 获取证明类型
	 * @param paramJson
	 * @return
	 */
	public static String getAttestType(String paramJson) {
		
		MessageResult result = new MessageResult(MessageResult.STATUS_SUCCESS,"操作成功！");
		JSONObject jo = new JSONObject() ;
		try {
			IPsnAttestService service = NCLocator.getInstance().lookup(IPsnAttestService.class);
			JSONObject param = JSONObject.fromObject(paramJson);
			String docList = param.getString("pkDocList");
			JSONArray docArr = service.getAttestType(docList/**"1002ZZ10000000001VXU"**/);
			String message = "";
			if(docArr.size() == 0){
				message += "没有找到定义的证明类型。" ;
			}
			if(StringUtils.isNotEmpty(message)){
				result.setMessage(message);
				result.setStatusCode(MessageResult.STATUS_ERROR);
			}
			jo.put("attestType", docArr);
			result.setData(jo);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			result.setMessage(e.getMessage());
			result.setStatusCode(MessageResult.STATUS_ERROR);
		}
		
		return JSONObject.fromObject(result).toString();
	}

	/**
	 * 获取证明处理人
	 * @param paramJson
	 * @return
	 */
	public static String getApplyer(String paramJson) {
	
	MessageResult result = new MessageResult(MessageResult.STATUS_SUCCESS,"操作成功！");
	try {
		IPsnAttestService service = NCLocator.getInstance().lookup(IPsnAttestService.class);
		
		JSONObject applyerJson = service.searchApplyer(paramJson);
		result.setData(applyerJson);
	} catch (Exception e) {
		Logger.error(e.getMessage());
		result.setMessage(e.getMessage());
		result.setStatusCode(MessageResult.STATUS_ERROR);
	}
	
	return JSONObject.fromObject(result).toString();
}
	/**
	 * 提交开证明
	 * @param attestInfo
	 * @return
	 */
	public static String commit(String attestInfo){
		MessageResult result = new MessageResult(MessageResult.STATUS_SUCCESS,"操作成功！");
		try {
			IPsnAttestService service = NCLocator.getInstance().lookup(IPsnAttestService.class);
			service.commit(attestInfo);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			result.setMessage(e.getMessage());
			result.setStatusCode(MessageResult.STATUS_ERROR);
		}
		
		return JSONObject.fromObject(result).toString();
	}
	/**
	 * 证明预览
	 * @param attestType
	 * @return
	 */
	public static String getPreview(String attestType){
		IPsnAttestService service = NCLocator.getInstance().lookup(IPsnAttestService.class);
		MessageResult result = new MessageResult(MessageResult.STATUS_SUCCESS, "操作成功！");
		String res = "" ;
		try {
			res = service.getPreview(attestType);
			result.setData(res);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			result.setMessage(e.getMessage());
			result.setStatusCode(MessageResult.STATUS_ERROR);
		}
		return JSONObject.fromObject(result).toString();
	}
	/**
	 * 获取证明处理人
	 * @param paramJson
	 * @return
	 */
	public static String queryProject(String paramJson) {
	
	MessageResult result = new MessageResult(MessageResult.STATUS_SUCCESS,"操作成功！");
	try {
		IPsnAttestService service = NCLocator.getInstance().lookup(IPsnAttestService.class);
		
		JSONObject applyerJson = service.searchProject(paramJson);
		result.setData(applyerJson);
	} catch (Exception e) {
		Logger.error(e.getMessage());
		result.setMessage(e.getMessage());
		result.setStatusCode(MessageResult.STATUS_ERROR);
	}
	
	return JSONObject.fromObject(result).toString();
}
		
}
