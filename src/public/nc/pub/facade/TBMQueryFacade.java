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
 * 象屿接口我的申请请假facade
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
			ITBMOffService.class);//销假/销差接口类

	/**
	 * 获取我的申请列表
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
	 * 获取我的审批列表
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
	 * 获取请假单据实体
	 * 
	 * @param param
	 * @return String
	 */
	public static String queryLeaveByPk(Map<String, Object> param)
			throws Exception {
		String retInfo = service.queryLeaveByPk(param);
		return retInfo;
	}
	
	/** 获取单条请假登记数据
	 * 
	 * @param param
	 * @return String
	 */
	public static String queryLeaveregByPk(Map<String, Object> param)
			throws Exception {
		String retInfo = offservice.queryLeaveregByPk(param);
		return retInfo;
	}
	
	/** 获取请假记录数据（销假使用）
	 * 
	 * @param param
	 * @return String
	 */
	public static String queryLeavereg4off(Map<String, Object> param)
			throws Exception {
		String retInfo = offservice.queryLeavereg4off(param);
		return retInfo;
	}
	
	/** 获取单条出差登记数据
	 * 
	 * @param param
	 * @return String
	 */
	public static String queryAwayregByPk(Map<String, Object> param)
			throws Exception {
		String retInfo = offservice.queryAwayregByPk(param);
		return retInfo;
	}
	
	/** 获取出差记录数据（销差使用）
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
	 * 保存请假单据实体
	 * 
	 * @param param
	 * @return String
	 */
	public static String saveLeave(Map<String, Object> param) throws Exception {
		String retInfo = service.saveLeave(param);
		return retInfo;
	}

	/**
	 * 提交请假单据实体
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
	 * 提交销假单据实体
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
	 * 提交销差单据实体
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
	 * 保存并提交请假单据实体
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
	 * 查询请假类型
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
	 * 查询流程类型
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
	 * 获取新的请假实体
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
	 * 计算请假时长
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
	 * 计算销假时长
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
	 * 计算销差时长
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
	 * 获取请假剩余时长
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
	 * 删除请假单
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
	 * 考勤中心日历
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
	 * 获取一天考勤具体详情
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
	 * 获取审批人列表
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
	 * 获取流程节点列表
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
	// --------------------------出差---------------
	/**
	 * 出差参照
	 * 
	 * @param pk_org
	 * @return
	 * @throws DAOException
	 */
	public static List queryAwayRef(String pk_org) throws Exception {
		return awayService.queryAwayRef(pk_org);
	}

	/**
	 * 保存出差单
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
	 * 提交出差审批单
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static String submitAway(Map<String, Object> param) throws Exception {
		return awayService.submitAway(param);
	}

	/**
	 * 查询出差审批单参照
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
	 * 保存并提交出差审批单
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
	 * 收回出差审批单
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
	 * 刪除出差审批单
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static String deleteAway(Map<String, Object> param) throws Exception {
		return awayService.deleteAway(param);
	}

	/**
	 * 计算出差天数
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
	 * 出差打印模板信息查询
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static String awayPrintTemplate(Map<String, Object> param)
			throws Exception {
		return awayService.awayPrintTemplate(param);
	}

	// ---------------------加班---------------
	/**
	 * 加班参照
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
	 * 获取加班申请单据
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
	 * 保存加班申请
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
	 * 提交加班申请
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
	 * 保存并提交
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
	 * 刪除加班申請
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
	 * 计算加班时长
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
	 * 获取加班待遇参照
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
	 * 加班申请单打印信息查询
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static String overtimePrintTemplate(Map<String, Object> param)
			throws Exception {
		return overtimeService.overtimePrintTemplate(param);
	}

	// --------------签卡------------
	/**
	 * 查询签卡申请单详情
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
	 * 保存签卡申请
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
	 * 提交签卡申请
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
	 * 保存并提交签卡申请
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
	 * 收回签卡审批单
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
	 * 删除签卡
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
	 * 签卡申请单打印信息查询
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
	 * 查询直批情况
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
	 * 单条审批单据
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
	 * 批量审批
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
	 * 校验单据时间冲突
	 */
	public static Map<String, Object> checkTimeBrokenAway(Map<String, Object> param) throws Exception{
		Logger.error("checkTimeBroken interface");
		return awayService.checkTimeBrokenAway(param);
	}
	
	/**
	 * 校验单据时间冲突
	 */
	public static Map<String, Object> checkTimeBrokenLeave(Map<String, Object> param) throws Exception{
		Logger.error("checkTimeBroken interface");
		return service.checkTimeBrokenLeave(param);
	}
	
	/**
	 * 校验单据时间冲突
	 */
	public static Map<String, Object> checkTimeBrokenOvertime(Map<String, Object> param) throws Exception{
		Logger.error("checkTimeBroken interface");
		return overtimeService.checkTimeBrokenOvertime(param);
	}
	/**
	 * 获取用户参照
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static String getUserInfo(Map<String, Object> param)throws Exception{
		return service.queryCpUser(param);
	}
	/**
	 * 获取人员参照
	 * @param param
	 * @return
	 * @throws Exception
	 * @author wangtian1
	 */
	public static String getPsndocInfo(Map<String, Object> param)throws Exception{
		return service.queryPsnodc(param);
	}
	/**
	 * 发送抄送消息
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static String sendMessage(Map<String, Object> param)throws Exception{
		return service.sendMessage(param);
	}
	
	/**
	 * 保存销假单据实体
	 * 
	 * @param param
	 * @return String
	 */
	public static String saveLeaveoff(Map<String, Object> param) throws Exception {
		String retInfo = offservice.saveLeaveoff(param);
		return retInfo;
	}
	
	/**
	 * 保存销差体
	 * 
	 * @param param
	 * @return String
	 */
	public static String saveAwayoff(Map<String, Object> param) throws Exception {
		String retInfo = offservice.saveAwayoff(param);
		return retInfo;
	}
	
	/**
	 * 获取销假单据实体
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
	 * 获取销差单据实体
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
	 * 删除销假单
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
	 * 删除销差单
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
