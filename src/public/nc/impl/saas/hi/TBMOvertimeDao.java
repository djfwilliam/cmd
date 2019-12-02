package nc.impl.saas.hi;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.bs.uif2.validation.DefaultValidationService;
import nc.hr.utils.MultiLangHelper;
import nc.hr.utils.ResHelper;
import nc.itf.hr.frame.IHrBillCode;
import nc.itf.hr.pf.IHrPf;
import nc.itf.ta.IOvertimeApplyApproveManageMaintain;
import nc.itf.ta.IOvertimeApplyQueryMaintain;
import nc.itf.ta.IOvertimeRegisterQueryMaintain;
import nc.itf.ta.ITBMPsndocQueryMaintain;
import nc.itf.ta.ITimeItemQueryService;
import nc.itf.ta.ITimeRuleQueryService;
import nc.itf.ta.algorithm.ITimeScopeWithBillInfo;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.jdbc.framework.processor.MapProcessor;
import nc.pub.billcode.itf.IBillcodeManage;
import nc.pub.billcode.vo.BillCodeContext;
import nc.pubitf.para.SysInitQuery;
import nc.pubitf.rbac.IUserPubService;
import nc.vo.hi.psndoc.PsndocVO;
import nc.vo.pmpub.project.ProjectHeadVO;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.billtype.BilltypeVO;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.sm.UserVO;
import nc.vo.ta.PublicLangRes;
import nc.vo.ta.bill.BillMutexException;
import nc.vo.ta.overtime.AggOvertimeVO;
import nc.vo.ta.overtime.OvertimeCheckResult;
import nc.vo.ta.overtime.OvertimeCommonVO;
import nc.vo.ta.overtime.OvertimeRegVO;
import nc.vo.ta.overtime.OvertimebVO;
import nc.vo.ta.overtime.OvertimehVO;
import nc.vo.ta.overtime.pf.validator.PFSaveOvertimeValidator;
import nc.vo.ta.psndoc.TBMPsndocVO;
import nc.vo.ta.timeitem.OverTimeTypeCopyVO;
import nc.vo.ta.timerule.TimeRuleVO;
import nc.vo.trade.pub.HYBillVO;
import nc.vo.uap.pf.PfProcessBatchRetObject;
import nc.vo.util.remotecallcombination.IRemoteCallCombinatorService;
import nc.vo.util.remotecallcombination.RemoteCallInfo;
import nc.vo.util.remotecallcombination.RemoteCallResult;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.ibm.db2.jcc.am.un;

public class TBMOvertimeDao {

	/**
	 * 加班参照
	 * 
	 * @param pk_org
	 * @return
	 * @throws DAOException
	 */
	public List<Map<String, Object>> getOvertimeRef(String pk_org)
			throws DAOException {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT ");
		sb.append(" 	tbm_timeitem.timeitemcode, ");
		sb.append(" 	timeitemname, ");
		sb.append(" 	tbm_timeitem.pk_timeitem, ");
		sb.append(" 	tbm_timeitem.itemtype, ");
		sb.append(" 	tbm_timeitemcopy.pk_timeitemcopy, ");
		sb.append(" 	tbm_timeitemcopy.pk_org, ");
		sb.append(" 	tbm_timeitemcopy.timeitemunit, ");
		sb.append(" 	tbm_timeitemcopy.leavesetperiod ");
		sb.append(" FROM ");
		sb.append(" 	tbm_timeitem ");
		sb.append(" INNER JOIN tbm_timeitemcopy ON ");
		sb.append(" 	tbm_timeitem.pk_timeitem = tbm_timeitemcopy.pk_timeitem ");
		sb.append(" 	AND tbm_timeitemcopy.pk_org = '" + pk_org + "' ");
		sb.append(" WHERE ");
		sb.append(" 	11 = 11 ");
		sb.append(" 	AND( ");
		sb.append(" 		tbm_timeitem.itemtype = 1 ");
		sb.append(" 		AND tbm_timeitemcopy.enablestate = 2 ");
		sb.append(" 	) ");
		sb.append(" ORDER BY tbm_timeitem.timeitemcode");
		return (List<Map<String, Object>>) new BaseDAO().executeQuery(
				sb.toString(), new MapListProcessor());
	}

	/**
	 * 保存加班申请
	 * @param overtimehMap
	 * @param bList
	 * @param userid
	 * @param needCheck
	 * @param aggvo
	 * @return
	 * @throws BusinessException
	 */
	public AggOvertimeVO saveOvertime(Map<String, Object> overtimehMap,
			List<Map<String, Object>> bList, String userid, String needCheck,
			AggOvertimeVO aggvo) throws BusinessException {
		String pk_psndoc = ((IUserPubService) NCLocator.getInstance().lookup(
				IUserPubService.class)).queryPsndocByUserid(userid);
		TBMPsndocVO latestVO = ((ITBMPsndocQueryMaintain) NCLocator
				.getInstance().lookup(ITBMPsndocQueryMaintain.class))
				.queryByPsndocAndDateTime(pk_psndoc, new UFDateTime());
		if (latestVO == null) {
			throw new BusinessException(ResHelper.getString("6017mobile",
					"06017mytime000002"));
		}
		String pk_org = latestVO.getPk_org();
		TimeRuleVO timeRule = ((ITimeRuleQueryService) NCLocator.getInstance()
				.lookup(ITimeRuleQueryService.class)).queryByOrg(pk_org);
		String isrestrictctrlot = timeRule.getIsrestrictctrlot().toString();

		OvertimebVO[] overtimebVOs = aggvo.getOvertimebVOs();
		if (ArrayUtils.isEmpty(overtimebVOs)) {
			throw new BusinessException(ResHelper.getString("6017hrta",
					"06017hrta0096"));
		}

		for (OvertimebVO bvo : overtimebVOs) {
			if (bvo.getActhour() == null) {
				bvo.setActhour(bvo.getLength());
			}
		}

		overtimehMap.put("blist", bList);
		String warningMessage = "";
		IOvertimeApplyApproveManageMaintain overtimeM = (IOvertimeApplyApproveManageMaintain) NCLocator
				.getInstance()
				.lookup(IOvertimeApplyApproveManageMaintain.class);
		if (StringUtils.isBlank(aggvo.getOvertimehVO().getPk_overtimeh())) {
			// 在此生成表单bill_code
			String newBill_code = TBMHelper.getBillCode("6405",aggvo.getHeadVO().getPk_group(),aggvo.getHeadVO().getPk_org());
			aggvo.getHeadVO().setBill_code(newBill_code);
			try{
				aggvo = overtimeM.insertDataDirect(aggvo);
			} catch(BusinessException e){
				TBMHelper.rollbackBillCode("6405",aggvo.getHeadVO().getPk_group(),aggvo.getHeadVO().getPk_org(),newBill_code);
				throw new BusinessException(e.getMessage());
			}
		} else {
			aggvo = (AggOvertimeVO) overtimeM.updateData(aggvo);
		}
		return aggvo;
	}

	/**
	 * 提交加班申请
	 * @param userid
	 * @param aggvo
	 * @return
	 * @throws BusinessException
	 */
	public Map<String, Object> submitOvertime(String userid, AggOvertimeVO aggvo)
			throws BusinessException {
		HashMap<String, String> eParam = new HashMap();
		if (isDirectApprove(aggvo.getOvertimehVO().getPk_org(), "6405")) {
			eParam.put("nosendmessage", "nosendmessage");
		}
		PfProcessBatchRetObject validateRetObj = ((IHrPf) NCLocator
				.getInstance().lookup(IHrPf.class))
				.submitValidation("Commit","Commit",null,SysInitQuery.getParaInt(
								aggvo.getOvertimehVO().getPk_org(),
								(String) IHrPf.hashBillTypePara.get("6405"))
								.intValue(),
						new AggregatedValueObject[] { aggvo });
		if ((validateRetObj.getRetObj() == null)
				|| (validateRetObj.getRetObj().length == 0)) {
			String errStr = validateRetObj.getExceptionMsg();
			if (StringUtils.isNotBlank(errStr))
				throw new BusinessException(errStr);
		}
		LfwPfUtil.runAction("SAVE", "6405", aggvo, null, null, null, eParam,
				new String[] { userid }, null);
		Map<String, Object> result = new HashMap();
		result.put("flag", "2");
		return result;
	}

	/**
	 * 获取加班单据
	 * 
	 * @param pk_overtimeh
	 * @return
	 * @throws BusinessException
	 */
	public Map<String, Object> queryOvertimeByPK(String pk_overtimeh)
			throws BusinessException {
		Map<String, Object> result = new HashMap();

		String whereSql = "pk_overtimeh = '" + pk_overtimeh + "' ";

		AggOvertimeVO[] aggVOs = ((IOvertimeApplyQueryMaintain) NCLocator
				.getInstance().lookup(IOvertimeApplyQueryMaintain.class))
				.queryByCond(whereSql);
		if (ArrayUtils.isEmpty(aggVOs)){//针对从考勤信息“员工日历”登记单数据跳转，需要查询对应登记单数据  by tianxx5
			OvertimeRegVO regVO = ((IOvertimeRegisterQueryMaintain) NCLocator
					.getInstance().lookup(IOvertimeRegisterQueryMaintain.class)).queryByPk(pk_overtimeh);
			initGroup(regVO.getPk_group());
			OverTimeTypeCopyVO[] Types = ((ITimeItemQueryService) NCLocator
					.getInstance().lookup(ITimeItemQueryService.class))
					.queryOvertimeCopyTypesByOrg(regVO.getPk_org(),
							"pk_timeitemcopy = '" + regVO.getPk_overtimetypecopy()
									+ "' ");
			String typename = "";
			OverTimeTypeCopyVO typeVO = Types[0];
			DecimalFormat dcmFmt = getDecimalFormat(regVO.getPk_org());
			String unit = 0 == typeVO.getTimeItemUnit() ? PublicLangRes.DAY()
					: PublicLangRes.HOUR();
			result.put("pk_overtimeh", regVO.getPk_overtimereg());
			result.put("billcode", regVO.getBill_code());
			result.put("transtypeid", null);
			result.put("transtype", null);
			result.put("pk_timeitemcopy", regVO.getPk_overtimetypecopy());
			result.put("ts", regVO.getTs().toString());
			result.put("creationtime", regVO.getCreationtime().toString());
			result.put("approve_state", "10");
			result.put("sumhour", new UFDouble(dcmFmt.format(regVO.getOvertimehour())).toString());
			result.put("requestid",regVO.getAttributeValue("requestid"));
			
			String billmaker = regVO.getCreator();
			List<PsndocVO> psndoc = (List<PsndocVO>) new BaseDAO().retrieveByClause(PsndocVO.class, "pk_psndoc in(select pk_base_doc from sm_user where cuserid='"+billmaker+"')");
			result.put("applyer",psndoc.get(0).getName());
			result.put("apply_time", regVO.getCreationtime().toStdString());
			result.put("unit", unit);
			typename = typeVO.getMultilangName();
			result.put("typename", typename);
			List<Map<String, Object>> blist = new ArrayList();
			result.put("blist", blist);
			Map<String, Object> binfo = new HashMap<String, Object>();
			blist.add(binfo);
			binfo.put("begintime", regVO.getOvertimebegintime().toString());
			binfo.put("endtime", regVO.getOvertimeendtime().toString());
			binfo.put("overtimeremark", regVO.getOvertimeremark());
			binfo.put("length",
					new UFDouble(dcmFmt.format(regVO.getOvertimehour()))
							.toString());
			binfo.put("pk_overtimeb", "virtualovertimePK");
			binfo.put("ts", regVO.getTs().toString());
			binfo.put("unit", unit);
			return result;
		}
		AggOvertimeVO aggVO = aggVOs[0];
		OvertimehVO headVO = aggVO.getHeadVO();
		OvertimebVO[] bodyVOs = aggVO.getBodyVOs();

		initGroup(headVO.getPk_group());
		OverTimeTypeCopyVO[] Types = ((ITimeItemQueryService) NCLocator
				.getInstance().lookup(ITimeItemQueryService.class))
				.queryOvertimeCopyTypesByOrg(headVO.getPk_org(),
						"pk_timeitemcopy = '" + headVO.getPk_overtimetypecopy()
								+ "' ");
		String typename = "";
		OverTimeTypeCopyVO typeVO = Types[0];

		DecimalFormat dcmFmt = getDecimalFormat(headVO.getPk_org());
		String unit = 0 == typeVO.getTimeItemUnit() ? PublicLangRes.DAY()
				: PublicLangRes.HOUR();
		result.put("pk_overtimeh", headVO.getPk_overtimeh());
		result.put("billcode", headVO.getBill_code());
		result.put("transtypeid", headVO.getTranstypeid());
		result.put("transtype", headVO.getTranstype());
		result.put("pk_timeitemcopy", headVO.getPk_overtimetypecopy());
		result.put("ts", headVO.getTs().toString());
		result.put("creationtime", headVO.getCreationtime().toString());
		result.put("approve_state", headVO.getApprove_state().toString());
		result.put("sumhour", new UFDouble(dcmFmt.format(headVO.getSumhour())).toString());
		result.put("requestid",headVO.getAttributeValue("requestid"));
		
		String billmaker = headVO.getCreator();
		List<PsndocVO> psndoc = (List<PsndocVO>) new BaseDAO().retrieveByClause(PsndocVO.class, "pk_psndoc in(select pk_base_doc from sm_user where cuserid='"+billmaker+"')");
		result.put("applyer",psndoc.get(0).getName());
		result.put("apply_time", headVO.getCreationtime().toStdString());
//TODO		
//		result.put("overtimereason",headVO.getOvertimereason());
		
		result.put("unit", unit);
		if (StringUtils.isNotBlank(headVO.getTranstypeid())) {
			BilltypeVO billType = (BilltypeVO) new BaseDAO().retrieveByPK(
					BilltypeVO.class, headVO.getTranstypeid());
			result.put("transtypename",
					MultiLangHelper.getName(billType, "billtypename"));
		}
		
		typename = typeVO.getMultilangName();
		result.put("typename", typename);
		// add by wt 20190822 begin
//		if (StringUtils.isNotBlank(headVO.getPk_project())) {
//			ProjectHeadVO projectVO = (ProjectHeadVO) new BaseDAO().retrieveByPK(
//					ProjectHeadVO.class, headVO.getPk_project());
//			result.put("project_name",MultiLangHelper.getName(projectVO, "project_name"));
//		}
		// add by wt 20190822 end
		List<Map<String, Object>> blist = new ArrayList();
		result.put("blist", blist);
		if (ArrayUtils.isEmpty(bodyVOs)) {
			return result;
		}
		
		Arrays.sort(bodyVOs, new Comparator<OvertimebVO>(){
			public int compare(OvertimebVO param1,OvertimebVO param2){
				UFDateTime overtimebegintime1 = param1.getOvertimebegintime();
				UFDateTime overtimeendtime2 = param2.getOvertimeendtime();
				return overtimebegintime1.compareTo(overtimeendtime2);
			}
		});

		for (OvertimebVO bvo : bodyVOs) {
			Map<String, Object> binfo = new HashMap<String, Object>();
			blist.add(binfo);
			binfo.put("begintime", bvo.getOvertimebegintime().toString());
			binfo.put("endtime", bvo.getOvertimeendtime().toString());
			binfo.put("overtimeremark", bvo.getOvertimeremark());
//			TODO
//			binfo.put("overtimesubsidy", bvo.getOvertimesubsidy());
			binfo.put("length",
					new UFDouble(dcmFmt.format(bvo.getOvertimehour()))
							.toString());
			binfo.put("pk_overtimeb", bvo.getPk_overtimeb());
			binfo.put("ts", bvo.getTs().toString());
			binfo.put("unit", unit);
		}
		ArrayList<Map<String,Object>> workFlowNote =new TBMAwayDao().queryWorkFlowNote(headVO.getTranstype(),headVO.getBill_code(),headVO.getPk_overtimeh());
		if(workFlowNote!=null && workFlowNote.size()>0){
			result.put("workflownote", workFlowNote);
		}
		return result;
	}
    /**
     * 加班打印查询
     * @param pk_overtimeh
     * @return
     * @throws BusinessException
     */
	public Map<String, Object> awayPrintTemplate(String pk_overtimeh) throws BusinessException {
		Map<String, Object> map = new HashMap<>();
		StringBuilder builder = new StringBuilder();
		builder.append("select a.bill_code as billcode, b.name as orgname, d.name as deptname, e.timeitemname as type, a.approve_state as status, ");
		builder.append("f.user_name as applyer, a.apply_date as applydate, a.approve_time as approvedate, g.postname as postname ");
		builder.append("from tbm_overtimeh a ");
		builder.append("inner join org_hrorg b on a.pk_org = b.pk_hrorg ");
		builder.append("inner join hi_psnjob c on c.pk_psnjob = a.pk_psnjob ");
		builder.append("inner join org_dept d on d.pk_dept = c.pk_dept ");
		builder.append("inner join tbm_timeitem e on e. pk_timeitem = a.pk_overtimetype ");
		builder.append("inner join sm_user f on f.cuserid = a.billmaker ");
		builder.append("inner join om_post g on g.pk_post = c.pk_post ");
		builder.append("where a.pk_overtimeh = '" + pk_overtimeh + "' ");
		HashMap<String, Object> dataMap = (HashMap<String, Object>)new BaseDAO().executeQuery(builder.toString(), new MapProcessor());
		
		StringBuilder sb = new StringBuilder();
		sb.append("select a.overtimebegintime as overtimebegintime, a.overtimeendtime as overtimeendtime, a.overtimehour as overtimehour, a.overtimeremark as overtimeremark ");
		sb.append("from tbm_overtimeb a ");
		sb.append("where a.pk_overtimeh  = '" + pk_overtimeh + "'");
		sb.append("order by a.overtimebegindate asc ");
		List list = (List) new BaseDAO().executeQuery(sb.toString(),
				new MapListProcessor());
		
		StringBuilder sba = new StringBuilder();
		sba.append("select b.user_name as sender, a.senddate as senddate, c.user_name as approver, a.dealdate as approdate, a.checknote as approveidea ");
		sba.append("from pub_workflownote a ");
		sba.append("inner join sm_user b on b.cuserid = a.senderman ");
		sba.append("inner join sm_user c on c.cuserid = a.checkman ");
		sba.append("where  billid = '" + pk_overtimeh + "' and actiontype <> 'BIZ' ");
		sba.append("order by a.senddate asc ");
		List notelist = (List) new BaseDAO().executeQuery(sba.toString(),
				new MapListProcessor());
		map.put("data", dataMap);
		map.put("tableData", list);
		map.put("approveData", notelist);
		return map;
	}
	
	
	

	private void initGroup(String pk_group) {
		String groupId = InvocationInfoProxy.getInstance().getGroupId();
		if (StringUtils.isBlank(groupId)) {
			InvocationInfoProxy.getInstance().setGroupId(pk_group);
		}
	}


	public boolean isDirectApprove(String pk_org, String billtype)
			throws BusinessException {
		Integer type = SysInitQuery.getParaInt(pk_org,
				(String) IHrPf.hashBillTypePara.get(billtype));
		return (type != null) && (type.intValue() == 0);
	}

	private DecimalFormat getDecimalFormat(String pk_org)
			throws BusinessException {
		TimeRuleVO timeRulevo = ((ITimeRuleQueryService) NCLocator
				.getInstance().lookup(ITimeRuleQueryService.class))
				.queryByOrg(pk_org);
		DecimalFormat dcmFmt = new DecimalFormat("0.00");
		if (timeRulevo == null)
			return dcmFmt;
		return getDecimalFormat(timeRulevo.getTimedecimal());
	}

	private DecimalFormat getDecimalFormat(Integer dec) {
		DecimalFormat dcmFmt = new DecimalFormat("0.00");
		if (dec != null) {
			if (dec.intValue() == 0) {
				dcmFmt = new DecimalFormat("0");
			} else if (dec.intValue() == 1) {
				dcmFmt = new DecimalFormat("0.0");
			}
		}
		return dcmFmt;
	}
}
