package nc.itf.saas;

import java.util.Map;

import nc.vo.pub.BusinessException;

/**
 * 人员信息
 * @author nijb@yonyou.com
 * 
 */
public interface ITBMQueryService {
	
	//查询我的申请
	public String getMyApplication(Map<String,Object> param) throws BusinessException;//queryBills
	//查询我的审批
	public String getMyApprove(Map<String,Object> param) throws BusinessException;
	//获取请假类型
	public String queryLeaveType(Map<String,Object> param) throws BusinessException;
	//查询请假单
	public String queryLeaveByPk(Map<String,Object> param) throws BusinessException;
	//保存请假单
	public String saveLeave(Map<String,Object> param) throws BusinessException;
	//提交表单
	public String submitLeave(Map<String,Object> param) throws BusinessException;
	//保存并提交表单
	public String saveAndSubmitLeave(Map<String,Object> param) throws BusinessException;
	//跳转到请假新增页面
	public String getNewLeave(Map<String,Object> param) throws BusinessException;
	//计算请假时长
	public String calculateLeaveLength(Map<String,Object> param) throws BusinessException;
	//计算请假类型剩余
	public String getLeaveBalance(Map<String, Object> param) throws BusinessException;
	//删除一愕请假单
	public String deleteLeave(Map<String, Object> param) throws BusinessException;
	//考勤日历中心
	public String queryCalendar(Map<String,Object> param) throws BusinessException;
	//获取考勤详细数据
	public String queryCalendarDayDetails(Map<String,Object> param) throws BusinessException;
	//获取流程节点列表
	public String queryProcessNodeList(Map<String,Object> param) throws BusinessException;
	//获取审批人列表
	public String queryApproverList(Map<String,Object> param) throws BusinessException;
	
	public String submitBill(Map<String,Object> params) throws BusinessException;
	//获取流程类型
	public String queryTranstype(Map<String, Object> param) throws BusinessException;
	//获取审批情况
	public int queryDirectApprove(Map<String, Object> param) throws BusinessException;
	//审批操作
	public String doApprove(Map<String, Object> param) throws Exception;
	//批量审批
	public String doBatchApprove(Map<String, Object> param)throws Exception;
	//请假打印信息查询
	public String leavePrintTemplate(Map<String, Object> param)throws Exception;
	
	//校验是否为产假 产检假 和病假
	public String checkBillType(Map<String,Object> param )throws Exception;

	public Map<String, Object> checkTimeBrokenLeave(Map<String, Object> param)throws BusinessException;
	
	/**
	 * 获取用户参照
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public String queryCpUser(Map<String, Object> param)throws Exception;
	/**
	 * 获取人员参照
	 * @param param
	 * @return
	 * @throws Exception
	 * @author wangtian1
	 */
	public String queryPsnodc(Map<String, Object> param)throws Exception;
	/**
	 * 发送抄送消息
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public String sendMessage(Map<String, Object> param)throws Exception;
	
	
}