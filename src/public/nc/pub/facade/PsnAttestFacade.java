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
	 * ��ȡ����
	 * ����֤�����ͺ�֤��������
	 * @return
	 */
	public static String getDocRef(String paramJson){
		
		MessageResult result = new MessageResult(MessageResult.STATUS_SUCCESS,"�����ɹ���");
		JSONObject jo = new JSONObject() ;
		try {
			IPsnAttestService service = NCLocator.getInstance().lookup(IPsnAttestService.class);
			JSONObject param = JSONObject.fromObject(paramJson);
			String docList = param.getString("pkDocList");
//			String applyRole = param.getString("applyRoleCode");
			JSONArray docArr = service.getAttestType(docList/**"1002ZZ10000000001VXU"**/);
			JSONArray applyerArr = service.getApplyer(param.toString());
			
			if(docArr.size() == 0){
				result.setMessage("û���ҵ������֤�����͡�");
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
	 * ��ȡ֤������
	 * @param paramJson
	 * @return
	 */
	public static String getAttestType(String paramJson) {
		
		MessageResult result = new MessageResult(MessageResult.STATUS_SUCCESS,"�����ɹ���");
		JSONObject jo = new JSONObject() ;
		try {
			IPsnAttestService service = NCLocator.getInstance().lookup(IPsnAttestService.class);
			JSONObject param = JSONObject.fromObject(paramJson);
			String docList = param.getString("pkDocList");
			JSONArray docArr = service.getAttestType(docList/**"1002ZZ10000000001VXU"**/);
			String message = "";
			if(docArr.size() == 0){
				message += "û���ҵ������֤�����͡�" ;
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
	 * ��ȡ֤��������
	 * @param paramJson
	 * @return
	 */
	public static String getApplyer(String paramJson) {
	
	MessageResult result = new MessageResult(MessageResult.STATUS_SUCCESS,"�����ɹ���");
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
	 * �ύ��֤��
	 * @param attestInfo
	 * @return
	 */
	public static String commit(String attestInfo){
		MessageResult result = new MessageResult(MessageResult.STATUS_SUCCESS,"�����ɹ���");
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
	 * ֤��Ԥ��
	 * @param attestType
	 * @return
	 */
	public static String getPreview(String attestType){
		IPsnAttestService service = NCLocator.getInstance().lookup(IPsnAttestService.class);
		MessageResult result = new MessageResult(MessageResult.STATUS_SUCCESS, "�����ɹ���");
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
	 * ��ȡ֤��������
	 * @param paramJson
	 * @return
	 */
	public static String queryProject(String paramJson) {
	
	MessageResult result = new MessageResult(MessageResult.STATUS_SUCCESS,"�����ɹ���");
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
