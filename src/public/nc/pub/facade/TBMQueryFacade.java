package nc.pub.facade;

import java.util.List;
import java.util.Map;

import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.saas.ITBMAwayService;
import nc.itf.saas.ITBMOffService;
import nc.itf.saas.ITBMOvertimeService;
import nc.itf.saas.ITBMQueryService;
import nc.itf.saas.ITBMSigncardService;
import nc.vo.pub.BusinessException;

/**
 * ����ӿ��ҵ��������facade
 * 
 * @author nijb@yonyou.com
 * 
 */
public class TBMQueryFacade {

	static ITBMQueryService service = NCLocator.getInstance().lookup(
			ITBMQueryService.class);

	static ITBMAwayService awayService = NCLocator.getInstance().lookup(
			ITBMAwayService.class);

	static ITBMOvertimeService overtimeService = NCLocator.getInstance()
			.lookup(ITBMOvertimeService.class);

	static ITBMSigncardService sinSigncardService = NCLocator.getInstance()
			.lookup(ITBMSigncardService.class);
	
	static ITBMOffService offservice = NCLocator.getInstance().lookup(
			ITBMOffService.class);//����/����ӿ���

	/**
	 * ��ȡ�ҵ������б�
	 * 
	 * @param param
	 * @return String
	 */
	public static String getMyApplication(Map<String, Object> param)
			throws Exception {
		String retInfo = service.getMyApplication(param);
		return retInfo;
	}

	/**
	 * ��ȡ�ҵ������б�
	 * 
	 * @param param
	 * @return String
	 */
	public static String getMyApprove(Map<String, Object> param)
			throws Exception {
		String retInfo = service.getMyApprove(param);
		return retInfo;
	}

	/**
	 * ��ȡ��ٵ���ʵ��
	 * 
	 * @param param
	 * @return String
	 */
	public static String queryLeaveByPk(Map<String, Object> param)
			throws Exception {
		String retInfo = service.queryLeaveByPk(param);
		return retInfo;
	}
	
	/** ��ȡ������ٵǼ�����
	 * 
	 * @param param
	 * @return String
	 */
	public static String queryLeaveregByPk(Map<String, Object> param)
			throws Exception {
		String retInfo = offservice.queryLeaveregByPk(param);
		return retInfo;
	}
	
	/** ��ȡ��ټ�¼���ݣ�����ʹ�ã�
	 * 
	 * @param param
	 * @return String
	 */
	public static String queryLeavereg4off(Map<String, Object> param)
			throws Exception {
		String retInfo = offservice.queryLeavereg4off(param);
		return retInfo;
	}
	
	/** ��ȡ��������Ǽ�����
	 * 
	 * @param param
	 * @return String
	 */
	public static String queryAwayregByPk(Map<String, Object> param)
			throws Exception {
		String retInfo = offservice.queryAwayregByPk(param);
		return retInfo;
	}
	
	/** ��ȡ�����¼���ݣ�����ʹ�ã�
	 * 
	 * @param param
	 * @return String
	 */
	public static String queryAwayreg4off(Map<String, Object> param)
			throws Exception {
		String retInfo = offservice.queryAwayreg4off(param);
		return retInfo;
	}
	

	/**
	 * ������ٵ���ʵ��
	 * 
	 * @param param
	 * @return String
	 */
	public static String saveLeave(Map<String, Object> param) throws Exception {
		String retInfo = service.saveLeave(param);
		return retInfo;
	}

	/**
	 * �ύ��ٵ���ʵ��
	 * 
	 * @param param
	 * @return String
	 */
	public static String submitLeave(Map<String, Object> param)
			throws Exception {
		String retInfo = service.submitLeave(param);
		return retInfo;
	}
	
	/**
	 * �ύ���ٵ���ʵ��
	 * 
	 * @param param
	 * @return String
	 */
	public static String submitLeaveoff(Map<String, Object> param)
			throws Exception {
		String retInfo = offservice.submitLeaveoff(param);
		return retInfo;
	}
	
	/**
	 * �ύ�����ʵ��
	 * 
	 * @param param
	 * @return String
	 */
	public static String submitAwayoff(Map<String, Object> param)
			throws Exception {
		String retInfo = offservice.submitAwayoff(param);
		return retInfo;
	}

	/**
	 * submit all bills
	 * 
	 * @param param
	 * @return String
	 */
	public static String submitBill(Map<String, Object> param) throws Exception {
		// TODO
		String retInfo = null;
		retInfo = service.submitBill(param);
		return retInfo;
	}

	/**
	 * ���沢�ύ��ٵ���ʵ��
	 * 
	 * @param param
	 * @return String
	 */
	public static String saveAndSubmitLeave(Map<String, Object> param)
			throws Exception {
		String retInfo = service.saveAndSubmitLeave(param);
		return retInfo;
	}

	/**
	 * ��ѯ�������
	 * 
	 * @param param
	 * @return String
	 */
	public static String queryLeaveType(Map<String, Object> param)
			throws Exception {
		String retInfo = service.queryLeaveType(param);
		return retInfo;
	}

	/**
	 * ��ѯ��������
	 * 
	 * @param param
	 * @return String
	 */
	public static String queryTranstype(Map<String, Object> param)
			throws Exception {
		String retInfo = service.queryTranstype(param);
		return retInfo;
	}

	/**
	 * ��ȡ�µ����ʵ��
	 * 
	 * @param param
	 * @return String
	 */
	public static String getNewLeave(Map<String, Object> param)
			throws Exception {
		String retInfo = service.getNewLeave(param);
		return retInfo;
	}

	/**
	 * �������ʱ��
	 * 
	 * @param param
	 * @return String
	 */
	public static String calculateLeaveLength(Map<String, Object> param)
			throws Exception {
		String retInfo = service.calculateLeaveLength(param);
		return retInfo;
	}
	
	/**
	 * ��������ʱ��
	 * 
	 * @param param
	 * @return String
	 */
	public static String calculateLeaveoffLength(Map<String, Object> param)
			throws Exception {
		String retInfo = offservice.calculateLeaveoffLength(param);
		return retInfo;
	}
	
	/**
	 * ��������ʱ��
	 * 
	 * @param param
	 * @return String
	 */
	public static String calculateAwayoffLength(Map<String, Object> param)
			throws Exception {
		String retInfo = offservice.calculateAwayoffLength(param);
		return retInfo;
	}

	/**
	 * ��ȡ���ʣ��ʱ��
	 * 
	 * @param param
	 * @return String
	 */
	public static String getLeaveBalance(Map<String, Object> param)
			throws Exception {
		String retInfo = service.getLeaveBalance(param);
		return retInfo;
	}

	/**
	 * ɾ����ٵ�
	 * 
	 * @param param
	 * @return String
	 */
	public static String deleteLeave(Map<String, Object> param)
			throws Exception {
		String retInfo = service.deleteLeave(param);
		return retInfo;
	}
   public static String	leavePrintTemplate(Map<String, Object> param)throws Exception{
	   return service.leavePrintTemplate(param);
   }
   
   public static String	leaveoffPrintTemplate(Map<String, Object> param)throws Exception{
	   return offservice.leaveoffPrintTemplate(param);
   }
   
   public static String	awayoffPrintTemplate(Map<String, Object> param)throws Exception{
	   return offservice.awayoffPrintTemplate(param);
   }

	/**
	 * ������������
	 * 
	 * @param param
	 * @return String
	 */
	public static String queryCalendar(Map<String, Object> param)
			throws Exception {
		String retInfo = service.queryCalendar(param);
		return retInfo;
	}

	/**
	 * ��ȡһ�쿼�ھ�������
	 * 
	 * @param param
	 * @return String
	 */
	public static String queryCalendarDayDetails(Map<String, Object> param)
			throws Exception {
		String retInfo = service.queryCalendarDayDetails(param);
		return retInfo;
	}

	/**
	 * ��ȡ�������б�
	 * 
	 * @param param
	 * @return String
	 */
	public static String queryApproverList(Map<String, Object> param)
			throws Exception {
		String retInfo = service.queryApproverList(param);
		return retInfo;
	}

	/**
	 * ��ȡ���̽ڵ��б�
	 * 
	 * @param param
	 * @return String
	 */
	public static String queryProcessNodeList(Map<String, Object> param)
			throws Exception {
		String retInfo = service.queryProcessNodeList(param);
		return retInfo;
	}

	// private static JSONObject getErrorObject(){
	// return getErrorObject(500,null,null);
	// }
	// private static JSONObject getErrorObject(Integer statuscode){
	// return getErrorObject(statuscode,null,null);
	// }
	// private static JSONObject getErrorObject(Integer statuscode,String
	// message,Map devInfo){
	// JSONObject jsonObject = new JSONObject();
	// jsonObject.element("statusCode", statuscode);
	// jsonObject.element("message", "NC error:"+message);
	// jsonObject.element("devInfo", devInfo);
	// jsonObject.element("data", new ArrayList());
	// return jsonObject;
	// }
	// --------------------------����---------------
	/**
	 * �������
	 * 
	 * @param pk_org
	 * @return
	 * @throws DAOException
	 */
	public static List queryAwayRef(String pk_org) throws Exception {
		return awayService.queryAwayRef(pk_org);
	}

	/**
	 * ������
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> saveAway(Map<String, Object> param)
			throws Exception {
		return awayService.saveAway(param);
	}

	/**
	 * �ύ����������
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static String submitAway(Map<String, Object> param) throws Exception {
		return awayService.submitAway(param);
	}

	/**
	 * ��ѯ��������������
	 * 
	 * @param pk_awayh
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> queryAwayByPK(String pk_awayh)
			throws Exception {
		return awayService.queryAwayByPK(pk_awayh);
	}

	/**
	 * ���沢�ύ����������
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static String saveAndSubmitAway(Map<String, Object> param)
			throws Exception {
		return awayService.saveAndSubmitAway(param);
	}

	/**
	 * �ջس���������
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static String callbackAway(Map<String, Object> param)
			throws Exception {
		return awayService.callbackAway(param);
	}

	/**
	 * �h������������
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static String deleteAway(Map<String, Object> param) throws Exception {
		return awayService.deleteAway(param);
	}

	/**
	 * �����������
	 * 
	 * @param awayhMap
	 * @param bList
	 * @param userid
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> calculateAway(Map<String, Object> param)
			throws Exception {
		return awayService.calculateAway(param);
	}

	/**
	 * �����ӡģ����Ϣ��ѯ
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static String awayPrintTemplate(Map<String, Object> param)
			throws Exception {
		return awayService.awayPrintTemplate(param);
	}

	// ---------------------�Ӱ�---------------
	/**
	 * �Ӱ����
	 * 
	 * @param pk_org
	 * @return
	 * @throws DAOException
	 */
	public static List<Map<String, Object>> getOvertimeRef(String pk_org)
			throws Exception {
		return overtimeService.getOvertimeRef(pk_org);
	}

	/**
	 * ��ȡ�Ӱ����뵥��
	 * 
	 * @param pk_overtimeh
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> queryOvertimeByPK(String pk_overtimeh)
			throws Exception {
		return overtimeService.queryOvertimeByPK(pk_overtimeh);
	}

	/**
	 * ����Ӱ�����
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static String saveOvertime(Map<String, Object> param)
			throws Exception {
		return overtimeService.saveOvertime(param);
	}

	/**
	 * �ύ�Ӱ�����
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static String submitOvertime(Map<String, Object> param)
			throws Exception {
		return overtimeService.submitOvertime(param);
	}

	/**
	 * ���沢�ύ
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static String saveAndSubmitOvertime(Map<String, Object> param)
			throws Exception {
		return overtimeService.saveAndSubmitOvertime(param);
	}

	/**
	 * �h���Ӱ���Ո
	 * 
	 * @param param
	 * @return
	 * @throws BusinessException
	 */
	public static String deleteOvertime(Map<String, Object> param)
			throws Exception {
		return overtimeService.deleteOvertime(param);
	}

	/**
	 * ����Ӱ�ʱ��
	 * 
	 * @param param
	 * @return
	 * @throws BusinessException
	 */
	public static Map<String, Object> calculateOvertime(
			Map<String, Object> param) throws Exception {
		return overtimeService.calculateOvertime(param);
	}

	/**
	 * ��ȡ�Ӱ��������
	 * 
	 * @param param
	 * @return
	 * @throws BusinessException
	 */
	public static String getOvertimesubsidy(Map<String, Object> param)
			throws Exception {
		return overtimeService.getOvertimesubsidy(param);
	}

	/**
	 * �Ӱ����뵥��ӡ��Ϣ��ѯ
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static String overtimePrintTemplate(Map<String, Object> param)
			throws Exception {
		return overtimeService.overtimePrintTemplate(param);
	}

	// --------------ǩ��------------
	/**
	 * ��ѯǩ�����뵥����
	 * 
	 * @param pk_signh
	 * @return
	 * @throws BusinessException
	 */
	public static Map<String, Object> querySigncardByPK(String pk_signh)
			throws Exception {
		return sinSigncardService.querySigncardByPK(pk_signh);
	}

	/**
	 * ����ǩ������
	 * 
	 * @param param
	 * @return
	 * @throws BusinessException
	 */
	public static Map<String, Object> saveSigncard(Map<String, Object> param)
			throws Exception {
		return sinSigncardService.saveSigncard(param);
	}

	/**
	 * �ύǩ������
	 * 
	 * @param param
	 * @return
	 * @throws BusinessException
	 */
	public static String submitSigncard(Map<String, Object> param)
			throws Exception {
		return sinSigncardService.submitSigncard(param);
	}

	/**
	 * ���沢�ύǩ������
	 * 
	 * @param param
	 * @return
	 * @throws BusinessException
	 */
	public static String saveAndSubmitSigncard(Map<String, Object> param)
			throws Exception {
		return sinSigncardService.saveAndSubmitSigncard(param);
	}

	/**
	 * �ջ�ǩ��������
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static String callbackSigncard(Map<String, Object> param)
			throws Exception {
		return sinSigncardService.callbackSigncard(param);
	}

	/**
	 * ɾ��ǩ��
	 * 
	 * @param pk_signh
	 * @return
	 * @throws BusinessException
	 */
	public static Map<String, Object> deleteSignCard(String pk_signh)
			throws Exception {
		return sinSigncardService.deleteSignCard(pk_signh);
	}

	/**
	 * ǩ�����뵥��ӡ��Ϣ��ѯ
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static String signcardPrintTemplate(Map<String, Object> param)
			throws Exception {
		return sinSigncardService.signcardPrintTemplate(param);
	}

	/**
	 * ��ѯֱ�����
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static Integer queryDirectApprove(Map<String, Object> param)
			throws Exception {
		int approvestyle = service.queryDirectApprove(param);
		return approvestyle;
	}

	/**
	 * ������������
	 * 
	 * @param param
	 * @return String
	 */
	public static String doApprove(Map<String, Object> param) throws Exception {
		Logger.error("doApproveTest interface");
		String queryByPk = null;
		try {
			String retInfo = service.doApprove(param);
			return retInfo;
		} catch (Exception e) {
			Logger.error(e.getMessage(), e.getCause());
			throw e;
		}
	}

	/**
	 * ��������
	 */
	public static String doBatchApprove(Map<String, Object> param)
			throws Exception {
		Logger.error("doApproveTest interface");
		String queryByPk = null;
		try {
			String retInfo = service.doBatchApprove(param);
			return retInfo;
		} catch (Exception e) {
			Logger.error(e.getMessage(), e.getCause());
			throw e;
		}
	}
	
	public static String checkBillType(Map<String, Object> param) throws Exception{
		Logger.error("checkBillTypeTest interface");
		String result = service.checkBillType(param);
		return result;
	}
	/**
	 * У�鵥��ʱ���ͻ
	 */
	public static Map<String, Object> checkTimeBrokenAway(Map<String, Object> param) throws Exception{
		Logger.error("checkTimeBroken interface");
		return awayService.checkTimeBrokenAway(param);
	}
	
	/**
	 * У�鵥��ʱ���ͻ
	 */
	public static Map<String, Object> checkTimeBrokenLeave(Map<String, Object> param) throws Exception{
		Logger.error("checkTimeBroken interface");
		return service.checkTimeBrokenLeave(param);
	}
	
	/**
	 * У�鵥��ʱ���ͻ
	 */
	public static Map<String, Object> checkTimeBrokenOvertime(Map<String, Object> param) throws Exception{
		Logger.error("checkTimeBroken interface");
		return overtimeService.checkTimeBrokenOvertime(param);
	}
	/**
	 * ��ȡ�û�����
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static String getUserInfo(Map<String, Object> param)throws Exception{
		return service.queryCpUser(param);
	}
	/**
	 * ��ȡ��Ա����
	 * @param param
	 * @return
	 * @throws Exception
	 * @author wangtian1
	 */
	public static String getPsndocInfo(Map<String, Object> param)throws Exception{
		return service.queryPsnodc(param);
	}
	/**
	 * ���ͳ�����Ϣ
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static String sendMessage(Map<String, Object> param)throws Exception{
		return service.sendMessage(param);
	}
	
	/**
	 * �������ٵ���ʵ��
	 * 
	 * @param param
	 * @return String
	 */
	public static String saveLeaveoff(Map<String, Object> param) throws Exception {
		String retInfo = offservice.saveLeaveoff(param);
		return retInfo;
	}
	
	/**
	 * ����������
	 * 
	 * @param param
	 * @return String
	 */
	public static String saveAwayoff(Map<String, Object> param) throws Exception {
		String retInfo = offservice.saveAwayoff(param);
		return retInfo;
	}
	
	/**
	 * ��ȡ���ٵ���ʵ��
	 * 
	 * @param param
	 * @return String
	 */
	public static String queryLeaveoffByPk(Map<String, Object> param)
			throws Exception {
		String retInfo = offservice.queryLeaveoffByPk(param);
		return retInfo;
	}
	
	/**
	 * ��ȡ�����ʵ��
	 * 
	 * @param param
	 * @return String
	 */
	public static String queryAwayoffByPk(Map<String, Object> param)
			throws Exception {
		String retInfo = offservice.queryAwayoffByPk(param);
		return retInfo;
	}
	
	/**
	 * ɾ�����ٵ�
	 * 
	 * @param param
	 * @return String
	 */
	public static String deleteLeaveoff(Map<String, Object> param)
			throws Exception {
		String retInfo = offservice.deleteLeaveoff(param);
		return retInfo;
	}
	
	/**
	 * ɾ�����
	 * 
	 * @param param
	 * @return String
	 */
	public static String deleteAwayoff(Map<String, Object> param)
			throws Exception {
		String retInfo = offservice.deleteAwayoff(param);
		return retInfo;
	}
	
}
