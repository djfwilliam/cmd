package nc.impl.saas.hi;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.borland.dx.sql.metadata.MetaDataException;

import uap.distribution.util.StringUtil;
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.hr.utils.MultiLangHelper;
import nc.impl.saas.pub.SaasCommonHelper;
import nc.itf.hr.pf.IHrPf;
import nc.itf.saas.pub.PageResult;
import nc.itf.ta.ILeaveApplyApproveManageMaintain;
import nc.itf.ta.ILeaveApplyQueryMaintain;
import nc.itf.ta.ILeaveBalanceManageService;
import nc.itf.ta.ILeaveRegisterManageMaintain;
import nc.itf.ta.ILeaveRegisterQueryMaintain;
import nc.itf.ta.ITBMPsndocQueryMaintain;
import nc.itf.ta.ITimeItemQueryService;
import nc.itf.ta.ITimeRuleQueryService;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.jdbc.framework.processor.MapProcessor;
import nc.md.persist.framework.IMDPersistenceQueryService;
import nc.md.persist.framework.MDPersistenceService;
import nc.pubitf.para.SysInitQuery;
import nc.pubitf.rbac.IUserPubService;
import nc.vo.iufo.hr.PubEnv;
import nc.vo.pmpub.project.ProjectHeadVO;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.billtype.BilltypeVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.pub.pf.IPfRetCheckInfo;
import nc.vo.sm.UserVO;
import nc.vo.ta.PublicLangRes;
import nc.vo.ta.leave.AggLeaveVO;
import nc.vo.ta.leave.LeaveCheckResult;
import nc.vo.ta.leave.LeaveRegVO;
import nc.vo.ta.leave.LeavebVO;
import nc.vo.ta.leave.LeavehVO;
import nc.vo.ta.leave.SplitBillResult;
import nc.vo.ta.psndoc.TBMPsndocVO;
import nc.vo.ta.timeitem.LeaveTypeCopyVO;
import nc.vo.ta.timerule.TimeRuleVO;
import nc.vo.uap.pf.PfProcessBatchRetObject;

public class TBMQueryDao {
	
	public static final String KQDA = new String("leave,overtime,signcard,away,awayoff,leaveoff");// 需要校验考勤档案
	
	public <T> T queryByPk(Class<T> clazz, String pk, boolean lazyLoad2)
			throws BusinessException {
		try {
			IMDPersistenceQueryService lookupPersistenceQueryService = MDPersistenceService
					.lookupPersistenceQueryService();
			return lookupPersistenceQueryService.queryBillOfVOByPK(clazz, pk,lazyLoad2);
		} catch (MetaDataException e) {
			throw new BusinessException(e.getMessage());
		}
	}
	public Map<String, Object> queryLeaveByPK(String pk_leaveh)
			throws BusinessException {
		Map<String, Object> result = new HashMap<String, Object>();

		String whereSql = " pk_leaveh = '" + pk_leaveh + "' ";
		AggLeaveVO[] aggVOs = ((ILeaveApplyQueryMaintain) NCLocator
				.getInstance().lookup(ILeaveApplyQueryMaintain.class))
				.queryByWhereSQL(null, whereSql);
		if (ArrayUtils.isEmpty(aggVOs)){//针对从考勤信息“员工日历”登记单数据跳转，需要查询对应登记单数据  by tianxx5
			LeaveRegVO regVO = ((ILeaveRegisterQueryMaintain) NCLocator
					.getInstance().lookup(ILeaveRegisterQueryMaintain.class)).queryByPk(pk_leaveh);
			LeaveTypeCopyVO[] leaveCopyTypes = ((ITimeItemQueryService) NCLocator
					.getInstance().lookup(ITimeItemQueryService.class))
					.queryLeaveCopyTypesByOrg(regVO.getPk_org(),
							"pk_timeitemcopy = '" + regVO.getPk_leavetypecopy()
									+ "' ");
			String typename = "";
			LeaveTypeCopyVO typeVO = leaveCopyTypes[0];
			DecimalFormat dcmFmt = getDecimalFormat(regVO.getPk_org());

			result.put("pk_leaveh", regVO.getPk_leavereg());
			result.put("billcode", regVO.getBill_code());
			result.put("transtypeid", null);
			result.put("transtype", null);
			result.put("pk_timeitemcopy", regVO.getPk_leavetypecopy());
			result.put("leaveremark", regVO.getLeaveremark());
			result.put("billmaker", regVO.getCreator());
			result.put("sumhour", new UFDouble(dcmFmt.format(regVO.getLeavehour())).toString());
			result.put("year", regVO.getLeaveyear());
			result.put("month", regVO.getLeavemonth());
			result.put("ts", regVO.getTs().toString());
			result.put("creationtime", regVO.getCreationtime().toString());
			result.put("approve_state", "10");
			result.put("islactation", regVO.getIslactation().booleanValue());
			if(!"1002Z710000000021ZM3".equals(regVO.getPk_leavetype())){
				result.put("useful",
						new UFDouble(dcmFmt.format(regVO.getUsefuldayorhour()))
				.toString());
				result.put("freeze",
						new UFDouble(dcmFmt.format(regVO.getFreezedayorhour()))
				.toString());
			}else{
				result.put("lactationhour", regVO.getLactationhour().doubleValue());
			}
			result.put("requestid",regVO.getAttributeValue("requestid"));

			if (!ArrayUtils.isEmpty(leaveCopyTypes)) {
				typename = typeVO.getMultilangName();
			}
			result.put("leavetypename", typename);
			String unit =( 0 == typeVO.getTimeItemUnit() ? PublicLangRes.DAY() : PublicLangRes.HOUR());
			result.put("unit", unit);
			List<Map<String, Object>> blist = new ArrayList<Map<String, Object>>();
			result.put("leavebs", blist);
			Map<String, Object> binfo = new HashMap<String, Object>();
			blist.add(binfo);
			binfo.put("pk_leaveb", "virtualleavePK");
			binfo.put("unit", unit);
			binfo.put("leaveremark", regVO.getLeaveremark());
			binfo.put("ts", regVO.getTs().toString());
			binfo.put("islactation", regVO.getIslactation().booleanValue());
			binfo.put("begintime", regVO.getLeavebegintime().toString());
			binfo.put("endtime", regVO.getLeaveendtime().toString());
			binfo.put("begindate", regVO.getLeavebegindate().toString());
			binfo.put("enddate", regVO.getLeaveenddate().toString());
			binfo.put("length",
					new UFDouble(dcmFmt.format(regVO.getLeavehour())).toString());
			String lactationtypeshow = null;
			if(regVO.getIslactation().booleanValue()){
				String type = regVO.getLactationholidaytype().toString();
				lactationtypeshow = Integer.parseInt(type) == 0? "单一作息时间段" : Integer.parseInt(type) == 1 ? "上班时段" : Integer.parseInt(type) == 2 ? "下班时段":"任意时段";
			}
			binfo.put("lactationtypeshow", lactationtypeshow);
			binfo.put("lactationtype", regVO.getLactationholidaytype() == null ? null : regVO.getLactationholidaytype().toString());
			return result;
		}
		AggLeaveVO aggVO = aggVOs[0];
		LeavehVO headVO = aggVO.getHeadVO();
		LeavebVO[] bodyVOs = aggVO.getBodyVOs();

		LeaveTypeCopyVO[] leaveCopyTypes = ((ITimeItemQueryService) NCLocator
				.getInstance().lookup(ITimeItemQueryService.class))
				.queryLeaveCopyTypesByOrg(headVO.getPk_org(),
						"pk_timeitemcopy = '" + headVO.getPk_leavetypecopy()
								+ "' ");
		String typename = "";
		LeaveTypeCopyVO typeVO = leaveCopyTypes[0];

		DecimalFormat dcmFmt = getDecimalFormat(headVO.getPk_org());
		//add by wt 20190821
//		if (StringUtils.isNotBlank(headVO.getPk_project())) {
//			ProjectHeadVO projectVO = (ProjectHeadVO) new BaseDAO().retrieveByPK(
//					ProjectHeadVO.class, headVO.getPk_project());
//			result.put("project_name",MultiLangHelper.getName(projectVO, "project_name"));
//		}
		result.put("pk_leaveh", headVO.getPk_leaveh());
		result.put("billcode", headVO.getBill_code());
		result.put("transtypeid", headVO.getTranstypeid());
		result.put("transtype", headVO.getTranstype());
		result.put("pk_timeitemcopy", headVO.getPk_leavetypecopy());
		result.put("leaveremark", headVO.getLeaveremark());
		result.put("billmaker", headVO.getBillmaker());
//TODO
//		result.put("leavereason", headVO.getLeavereason());
		result.put("sumhour", new UFDouble(dcmFmt.format(headVO.getSumhour())).toString());
		result.put("year", headVO.getLeaveyear());
		result.put("month", headVO.getLeavemonth());
		result.put("ts", headVO.getTs().toString());
		result.put("creationtime", headVO.getCreationtime().toString());
		result.put("approve_state", headVO.getApprove_state().toString());
		result.put("islactation", headVO.getIslactation().booleanValue());
		if (StringUtils.isNotBlank(headVO.getTranstypeid())) {
			BilltypeVO billType = (BilltypeVO) new BaseDAO().retrieveByPK(
					BilltypeVO.class, headVO.getTranstypeid());
			result.put("transtypename",
					MultiLangHelper.getName(billType, "billtypename"));
		}
		if(!"1002Z710000000021ZM3".equals(headVO.getPk_leavetype())){
			result.put("useful",
					new UFDouble(dcmFmt.format(headVO.getUsefuldayorhour()))
			.toString());
			result.put("freeze",
					new UFDouble(dcmFmt.format(headVO.getFreezedayorhour()))
			.toString());
		}else{
			result.put("lactationhour", headVO.getLactationhour().doubleValue());
		}
		
		result.put("requestid",headVO.getAttributeValue("requestid"));

		if (!ArrayUtils.isEmpty(leaveCopyTypes)) {
			typename = typeVO.getMultilangName();
		}
		result.put("leavetypename", typename);
		String unit =( 0 == typeVO.getTimeItemUnit() ? PublicLangRes.DAY() : PublicLangRes.HOUR());
		result.put("unit", unit);

		List<Map<String, Object>> blist = new ArrayList<Map<String, Object>>();
		result.put("leavebs", blist);
		if (ArrayUtils.isEmpty(bodyVOs)) {
			return result;
		}
		ArrayList<Map<String,Object>> workFlowNote = new TBMAwayDao().queryWorkFlowNote(headVO.getTranstype(),headVO.getBill_code(),headVO.getPk_leaveh());
		if(workFlowNote!=null && workFlowNote.size()>0){
			result.put("workflownote", workFlowNote);
		}

		Arrays.sort(bodyVOs, new Comparator<LeavebVO>() {
			@Override
			public int compare(LeavebVO vo1, LeavebVO vo2) {
				UFDateTime leavebegintime1 = vo1.getLeavebegintime();
				UFDateTime leavebegintime2 = vo2.getLeavebegintime();
				int compareTo = leavebegintime1.compareTo(leavebegintime2);
				return compareTo;
			}
		});
		for (LeavebVO bvo : bodyVOs) {
			Map<String, Object> binfo = new HashMap<String, Object>();
			blist.add(binfo);
			binfo.put("begintime", bvo.getLeavebegintime().toString());
			binfo.put("endtime", bvo.getLeaveendtime().toString());
			binfo.put("begindate", bvo.getLeavebegindate().toString());
			binfo.put("enddate", bvo.getLeaveenddate().toString());
			String lactationtypeshow = null;
			if(headVO.getIslactation().booleanValue()){
				String type = bvo.getLactationholidaytype().toString();
				lactationtypeshow = Integer.parseInt(type) == 0? "单一作息时间段" : Integer.parseInt(type) == 1 ? "上班时段" : Integer.parseInt(type) == 2 ? "下班时段":"任意时段";
			}
			binfo.put("lactationtypeshow", lactationtypeshow);
			binfo.put("lactationtype", bvo.getLactationholidaytype() == null ? null : bvo.getLactationholidaytype().toString());
			binfo.put("length",
					new UFDouble(dcmFmt.format(bvo.getLeavehour())).toString());
			binfo.put("pk_leaveb", bvo.getPk_leaveb());
			binfo.put("unit", unit);
			binfo.put("leaveremark", bvo.getLeaveremark());
			binfo.put("ts", bvo.getTs().toString());
		}
		return result;
	}

	/*public BillCodeContext getBillCodeContext(String billType, String pk_group,
			String pk_org) throws BusinessException {
		return ((IBillcodeManage) NCLocator.getInstance().lookup(
				IBillcodeManage.class)).getBillCodeContext(billType, pk_group,
				pk_org);
	}*/

	public boolean isDirectApprove(String pk_org, String billtype)
			throws BusinessException {
//		Integer type = SysInitQuery.getParaInt(pk_org,
//				(String) IHrPf.hashBillTypePara.get(billtype));
		String sql = "select value from pub_sysinit where pk_org='" + pk_org+"' and initcode ='" + (String) IHrPf.hashBillTypePara.get(billtype) +"'";
		String types = (String) new BaseDAO().executeQuery(sql, new ColumnProcessor());
		if(types == null || types.equals("null") || types.equals("")){
			types = "0";
		}
		Integer type =Integer.valueOf(types);
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

	/**
	 * 获取我的申请列表
	 * 
	 * @param userId
	 * @param begindate
	 * @param billstateCond
	 * @param billtype
	 * @param pageno
	 * @param pagesize
	 * @return
	 * @throws BusinessException
	 */
	public PageResult queryBills(String userId, UFLiteralDate begindate,
			String billstateCond, String billtype, Integer pageno,
			Integer pagesize) throws BusinessException {
		PageResult result = new PageResult();

		String pk_psndoc = ((IUserPubService) NCLocator.getInstance().lookup(
				IUserPubService.class)).queryPsndocByUserid(userId);
		if (StringUtil.isBlank(pk_psndoc)) {
			result.setStatusCode(PageResult.STATUS_ERROR);
			result.setMessage("未找到该用户，请确认用户是否是员工。");
			return result;
		}
		TBMPsndocVO latestVO = ((ITBMPsndocQueryMaintain) NCLocator
				.getInstance().lookup(ITBMPsndocQueryMaintain.class))
				.queryByPsndocAndDateTime(pk_psndoc, new UFDateTime());
		//由于人事单据的加入，需要把考勤档案放后判断
//		if (latestVO == null) {
//			result.setStatusCode(PageResult.STATUS_ERROR);
//			result.setMessage("当前用户没有考勤档案。");
//			return result;
//		}
		//String condition = " where pk_psndoc= '" + pk_psndoc + "' and billmaker" + " = '" + userId + "' and " + "approve_state" + billstateCond;
		String condition = " where billmaker" + " = '" + userId + "' and ishrssbill = 'Y' and " + "approve_state" + billstateCond;
		String condition4off = " where billmaker" + " = '" + userId + "' and " + "approve_state" + billstateCond;
		if (begindate != null) {
			condition = condition + " and apply_date > '"
					+ begindate.toString() + "' ";
			condition4off = condition4off + " and apply_date > '"
					+ begindate.toString() + "' ";
		}

		String commonsql = "select apply_date apply_date, approve_note approve_note, approve_state approve_state,bill_code bill_code,fun_code fun_code, pk_billtype pk_billtype,transtype transtype ,transtypeid transtypeid,billtypename transtype_name ";
		String commonsql4psn = "select apply_date apply_date, approve_note approve_note, approve_state approve_state,bill_code bill_code,'' fun_code, pk_billtype pk_billtype,transtype transtype ,transtypeid transtypeid,billtypename transtype_name ";
		String approvesql = ",case when approve_state = -1 then '自由' when approve_state = 0 then '审批未通过' when approve_state = 1 then '审批通过' when approve_state = 2 then '审批进行中' when approve_state = 3 then '已提交' end approve_state_name ";

		StringBuffer buffer = new StringBuffer();
		if("all".equals(billtype)||"psnreg".equals(billtype)){
			buffer.append(commonsql4psn+approvesql);
			buffer.append(",pk_hi_regapply pk_h,memo tbm_remark ,case when probation_type=1 then '1' when probation_type=2 then '2' end pk_tbmtype, 1 sumhour, '转正' tbm_h_name ,'psnreg' billtype ");
			buffer.append(",case when probation_type=1 then '入职试用' when probation_type=2 then '转岗试用' end tbmtype_name ,hi_regapply.ishrssbill ishrssbill ");
			buffer.append(",begin_date tbm_begindate ");
			buffer.append(",end_date tbm_enddate ");
			buffer.append(" FROM hi_regapply ");
			buffer.append(" left join bd_billtype on bd_billtype.pk_billtypeid = hi_regapply.TRANSTYPEID");
			buffer.append(condition);	
		}
		if("all".equals(billtype)||"dimission".equals(billtype)){
			if("all".equals(billtype)){
				buffer.append(" union ");	
			}
			buffer.append(commonsql+approvesql);
			buffer.append(", pk_hi_stapply pk_h, hi_stapply.memo tbm_remark, hi_stapply.pk_trnstype pk_tbmtype, 0 sumhour, '离职' tbm_h_name , 'dimission' billtype ");
			buffer.append(", hr_trnstype.trnstypename tbmtype_name , hi_stapply.ishrssbill ishrssbill,hi_stapply.creationtime tbm_begindate,hi_stapply.effectdate tbm_enddate ");
			buffer.append(" FROM hi_stapply LEFT JOIN bd_billtype ON hi_stapply.transtypeid = bd_billtype. pk_billtypeid LEFT JOIN hr_trnstype ON hi_stapply.pk_trnstype = hr_trnstype.pk_trnstype ");
			buffer.append(condition);
			buffer.append(" and fun_code = '60090dimissionapply' ");
		}
		if("all".equals(billtype)||"trns".equals(billtype)){
			if("all".equals(billtype)){
				buffer.append(" union ");	
			}
			buffer.append(commonsql+approvesql);
			buffer.append(", pk_hi_stapply pk_h, hi_stapply.memo tbm_remark, hi_stapply.pk_trnstype pk_tbmtype, 0 sumhour, '调配' tbm_h_name , 'trns' billtype ");
			buffer.append(", hr_trnstype.trnstypename tbmtype_name , hi_stapply.ishrssbill ishrssbill,hi_stapply.creationtime tbm_begindate,hi_stapply.effectdate tbm_enddate ");
			buffer.append(" FROM hi_stapply LEFT JOIN bd_billtype ON hi_stapply.transtypeid = bd_billtype. pk_billtypeid LEFT JOIN hr_trnstype ON hi_stapply.pk_trnstype = hr_trnstype.pk_trnstype ");
			buffer.append(condition);	
			buffer.append(" and fun_code = '60090transapply' ");
		}
		if (latestVO == null) {//用户没有考勤档案时只查询人事单据
			String exesql = "select * from (" + buffer.toString()
					+ ") tbm_temp order by tbm_temp.apply_date desc  ";
			if (pageno == null || pagesize == null) {
				List<Map<String, Object>> data = (List<Map<String, Object>>) new BaseDAO()
						.executeQuery(exesql, new MapListProcessor());
				result.setData(data);
			} else {
				int queryTotalCount = SaasCommonHelper.queryTotalCount(exesql);
				String querystrSQL = SaasCommonHelper.processPageSql(exesql,
						pageno, pagesize);
				List<Map<String, Object>> data = (List<Map<String, Object>>) new BaseDAO()
						.executeQuery(querystrSQL, new MapListProcessor());
				result.setData(data);
				result.setTotalCount(queryTotalCount);
				result.setPageno(pageno);
			}
			return result;
		}
		if("all".equals(billtype)||"leave".equals(billtype)){
			if("all".equals(billtype)){
				buffer.append(" union ");	
			}
			buffer.append(commonsql+approvesql);
			buffer.append(",pk_leaveh pk_h,leaveremark tbm_remark ,pk_leavetype pk_tbmtype, sumhour sumhour, '请假' tbm_h_name ,'leave' billtype ");
			buffer.append(",tbm_timeitem.timeitemname tbmtype_name ,tbm_leaveh.ishrssbill ishrssbill ");
			buffer.append(",(SELECT min(leavebegindate) FROM tbm_leaveb WHERE tbm_leaveb.pk_leaveh = tbm_leaveh.pk_leaveh) tbm_begindate ");
			buffer.append(",(SELECT max(leaveenddate) FROM tbm_leaveb WHERE tbm_leaveb.pk_leaveh = tbm_leaveh.pk_leaveh)  tbm_enddate ");
			buffer.append(" FROM tbm_leaveh inner join tbm_timeitem on tbm_leaveh.pk_leavetype = tbm_timeitem.pk_timeitem ");
			buffer.append(" left join bd_billtype on bd_billtype.pk_billtypeid = tbm_leaveh.TRANSTYPEID");
			buffer.append(condition);	
		}
		
		if("all".equals(billtype)||"signcard".equals(billtype)){
			if("all".equals(billtype)){
				buffer.append(" union ");	
			}
			buffer.append(commonsql+approvesql);
			buffer.append(", pk_signh pk_h,signremark tbm_remark ,'' pk_tbmtype,1 sumhour , '签卡' tbm_h_name ,'signcard' billtype");
			buffer.append(",'' tbmtype_name ,tbm_signh.ishrssbill ishrssbill ");
			buffer.append(",(SELECT min(signdate) FROM tbm_signb WHERE tbm_signb.pk_signh = tbm_signh.pk_signh) tbm_begindate ");
			buffer.append(",(SELECT max(signdate) FROM tbm_signb WHERE tbm_signb.pk_signh = tbm_signh.pk_signh)  tbm_enddate ");
			buffer.append(" FROM tbm_signh ");
			buffer.append(" left join bd_billtype on bd_billtype.pk_billtypeid = tbm_signh.TRANSTYPEID");
			buffer.append(condition);
		}

		if("all".equals(billtype)||"overtime".equals(billtype)){
			if("all".equals(billtype)){
				buffer.append(" union ");	
			}
			buffer.append(commonsql+approvesql);
			buffer.append(" , pk_overtimeh  pk_h, '' tbm_remark ,pk_overtimetype pk_tbmtype,sumhour sumhour , '加班' tbm_h_name,'overtime' billtype ");
			buffer.append(",tbm_timeitem.timeitemname tbmtype_name ,tbm_overtimeh.ishrssbill ishrssbill ");
			buffer.append(",(SELECT min(overtimebegindate) FROM tbm_overtimeb WHERE tbm_overtimeb.pk_overtimeh = tbm_overtimeh.pk_overtimeh) tbm_begindate ");
			buffer.append(",(SELECT max(overtimeenddate) FROM tbm_overtimeb WHERE tbm_overtimeb.pk_overtimeh = tbm_overtimeh.pk_overtimeh)  tbm_enddate ");
			buffer.append(" FROM tbm_overtimeh inner join tbm_timeitem on tbm_overtimeh.pk_overtimetype = tbm_timeitem.pk_timeitem ");
			buffer.append(" left join bd_billtype on bd_billtype.pk_billtypeid = tbm_overtimeh.TRANSTYPEID");
			buffer.append(condition);
		}

		if("all".equals(billtype)||"away".equals(billtype)){
			if("all".equals(billtype)){
				buffer.append(" union ");	
			}
			buffer.append(commonsql+approvesql);
			buffer.append(" ,pk_awayh pk_h, awayremark tbm_remark ,pk_awaytype pk_tbmtype,sumhour sumhour, '出差' tbm_h_name ,'away' billtype  ");
			buffer.append(",tbm_timeitem.timeitemname tbmtype_name ,tbm_awayh.ishrssbill ishrssbill ");
			buffer.append(",(SELECT min(awaybegintime) FROM tbm_awayb WHERE tbm_awayb.pk_awayh = tbm_awayh.pk_awayh) tbm_begindate ");
			buffer.append(",(SELECT max(awayendtime) FROM tbm_awayb WHERE tbm_awayb.pk_awayh = tbm_awayh.pk_awayh)  tbm_enddate ");
			buffer.append(" FROM tbm_awayh inner join tbm_timeitem on tbm_awayh.pk_awaytype = tbm_timeitem.pk_timeitem ");
			buffer.append(" left join bd_billtype on bd_billtype.pk_billtypeid = tbm_awayh.TRANSTYPEID");
			buffer.append(condition);
		}
		
		if("all".equals(billtype)||"leaveoff".equals(billtype)){
			if("all".equals(billtype)){
				buffer.append(" union ");	
			}
			buffer.append(commonsql+approvesql);
			buffer.append(",pk_leaveoff pk_h,'' tbm_remark ,pk_leavetype pk_tbmtype, reallyleavehour sumhour, '销假' tbm_h_name ,'leaveoff' billtype ");
			buffer.append(",tbm_timeitem.timeitemname tbmtype_name ,'Y' ishrssbill ");
			buffer.append(",leavebegindate tbm_begindate, leaveenddate tbm_enddate ");
			buffer.append(" FROM tbm_leaveoff inner join tbm_timeitem on tbm_leaveoff.pk_leavetype = tbm_timeitem.pk_timeitem ");
			buffer.append(" left join bd_billtype on bd_billtype.pk_billtypeid = tbm_leaveoff.TRANSTYPEID");
			buffer.append(condition4off);	
		}
		
		if("all".equals(billtype)||"awayoff".equals(billtype)){
			if("all".equals(billtype)){
				buffer.append(" union ");	
			}
			buffer.append(commonsql+approvesql);
			buffer.append(",pk_awayoff pk_h,'' tbm_remark ,pk_awaytype pk_tbmtype, reallyawayhour sumhour, '销差' tbm_h_name ,'awayoff' billtype ");
			buffer.append(",tbm_timeitem.timeitemname tbmtype_name ,'Y' ishrssbill ");
			buffer.append(",awaybegindate tbm_begindate, awayenddate tbm_enddate ");
			buffer.append(" FROM tbm_awayoff inner join tbm_timeitem on tbm_awayoff.pk_awaytype = tbm_timeitem.pk_timeitem ");
			buffer.append(" left join bd_billtype on bd_billtype.pk_billtypeid = tbm_awayoff.TRANSTYPEID");
			buffer.append(condition4off);	
		}

		String exesql = "select * from (" + buffer.toString()
				+ ") tbm_temp order by tbm_temp.apply_date desc  ";
		if (pageno == null || pagesize == null) {
			List<Map<String, Object>> data = (List<Map<String, Object>>) new BaseDAO()
					.executeQuery(exesql, new MapListProcessor());
			result.setData(data);
		} else {
			int queryTotalCount = SaasCommonHelper.queryTotalCount(exesql);
			String querystrSQL = SaasCommonHelper.processPageSql(exesql,
					pageno, pagesize);
			List<Map<String, Object>> data = (List<Map<String, Object>>) new BaseDAO()
					.executeQuery(querystrSQL, new MapListProcessor());
			result.setData(data);
			result.setTotalCount(queryTotalCount);
			result.setPageno(pageno);
		}
		return result;
	}
	@SuppressWarnings("unchecked")
	public PageResult queryApprove(String pk_psndoc, String inCond, String isFinish) throws BusinessException {
		PageResult result = new PageResult();
		UserVO userVO = ((IUserPubService) NCLocator.getInstance().lookup(
				IUserPubService.class)).queryUserVOByPsnDocID(pk_psndoc);
		if (userVO == null) {
			result.setStatusCode(PageResult.STATUS_ERROR);
			result.setMessage("未找到该用户，请确认用户是否是员工。");
			return result;
		}
		StringBuffer sql = new StringBuffer();
		sql.append("select pk_leaveh as billpk, a.apply_date, '请假单' as titlename, 0 as type, b.messagenote ");
		sql.append("from tbm_leaveh a inner join pub_workflownote b on a.pk_leaveh = b.billid ");
		sql.append("where a.approve_state in ").append(inCond).append(" and b.checkman = ? and b.ischeck = '" + isFinish + "' ");
		sql.append("union ");
		sql.append("select pk_signh as billpk, c.apply_date, '签卡单' as titlename, 1 as type, d.messagenote ");
		sql.append("from tbm_signh c inner join pub_workflownote d on c.pk_signh = d.billid ");
		sql.append("where c.approve_state in ").append(inCond).append(" and d.checkman = ? and d.ischeck = '" + isFinish + "' ");
		sql.append("union ");
		sql.append("select pk_overtimeh as billpk, e.apply_date, '加班单' as titlename, 2 as type, f.messagenote ");
		sql.append("from tbm_overtimeh e inner join pub_workflownote f on e.pk_overtimeh = f.billid ");
		sql.append("where e.approve_state in ").append(inCond).append(" and f.checkman = ? and f.ischeck = '" + isFinish + "' ");
		sql.append("union ");
		sql.append("select pk_awayh as billpk, g.apply_date, '出差单' as titlename, 3 as type, h.messagenote ");
		sql.append("from tbm_awayh g inner join pub_workflownote h on g.pk_awayh = h.billid ");
		sql.append("where g.approve_state in ").append(inCond).append(" and h.checkman = ? and h.ischeck = '" + isFinish + "' ");
		
		sql.append("union ");
		sql.append("select pk_leaveoff as billpk, g.apply_date, '销假单' as titlename, 4 as type, h.messagenote ");
		sql.append("from tbm_leaveoff g inner join pub_workflownote h on g.pk_leaveoff = h.billid ");
		sql.append("where g.approve_state in ").append(inCond).append(" and h.checkman = ? and h.ischeck = '" + isFinish + "' ");
		
		sql.append("union ");
		sql.append("select pk_awayoff as billpk, g.apply_date, '销差单' as titlename, 5 as type, h.messagenote ");
		sql.append("from tbm_awayoff g inner join pub_workflownote h on g.pk_awayoff = h.billid ");
		sql.append("where g.approve_state in ").append(inCond).append(" and h.checkman = ? and h.ischeck = '" + isFinish + "' ");
		//转正
		sql.append("union ");
		sql.append("select pk_hi_regapply as billpk, g.apply_date, '转正单' as titlename, 6 as type, h.messagenote ");
		sql.append("from hi_regapply g inner join pub_workflownote h on g.pk_hi_regapply = h.billid ");
		sql.append("where g.approve_state in ").append(inCond).append(" and h.checkman = ? and h.ischeck = '" + isFinish + "' ");
		//调配
		sql.append("union ");
		sql.append("select pk_hi_stapply as billpk, g.apply_date, '调配单' as titlename, 7 as type, h.messagenote ");
		sql.append("from hi_stapply g inner join pub_workflownote h on g.pk_hi_stapply = h.billid ");
		sql.append("where g.approve_state in ").append(inCond).append(" and h.checkman = ? and h.ischeck = '" + isFinish + "' and fun_code ='60090transapply'");
		sql.append("union ");
		sql.append("select pk_hi_stapply as billpk, g.apply_date, '离职单' as titlename, 8 as type, h.messagenote ");
		sql.append("from hi_stapply g inner join pub_workflownote h on g.pk_hi_stapply = h.billid ");
		sql.append("where g.approve_state in ").append(inCond).append(" and h.checkman = ? and h.ischeck = '" + isFinish + "' and fun_code ='60090dimissionapply' ");
		
		sql.append("order by apply_date desc ");
		SQLParameter param = new SQLParameter();
		param.addParam(userVO.getCuserid());
		param.addParam(userVO.getCuserid());
		param.addParam(userVO.getCuserid());
		param.addParam(userVO.getCuserid());
		param.addParam(userVO.getCuserid());
		param.addParam(userVO.getCuserid());
		param.addParam(userVO.getCuserid());
		param.addParam(userVO.getCuserid());
		param.addParam(userVO.getCuserid());
		List<Map<String, Object>> data = (List<Map<String, Object>>) new BaseDAO()
				.executeQuery(sql.toString(), param, new MapListProcessor());
		result.setData(data);
		return result;
	}
	
	

	public List<Map<String, Object>> queryLeaveType(String userId,String billtype)
			throws BusinessException {
		PageResult result = new PageResult();
		String pk_psndoc = ((IUserPubService) NCLocator.getInstance().lookup(
				IUserPubService.class)).queryPsndocByUserid(userId);
		if (StringUtil.isBlank(pk_psndoc)) {
			throw new BusinessException("未找到该用户，请确认用户是否是员工");
		}
		TBMPsndocVO latestVO = ((ITBMPsndocQueryMaintain) NCLocator
				.getInstance().lookup(ITBMPsndocQueryMaintain.class))
				.queryByPsndocAndDateTime(pk_psndoc, new UFDateTime());
		if (latestVO == null) {
			throw new BusinessException("当前用户没有考勤档案。");
		}
		Integer billcode = 0;
		if("leave".equals(billtype)){
			billcode = 0;
		} else if("overtime".equals(billtype)){
			billcode = 1;
		}else if("signcard".equals(billtype)){
			billcode = 3;
		}else if("away".equals(billtype)){
			billcode = 2;
		}
		String pk_org = latestVO.getPk_org();
		String exesql = "select tbm_timeitem.timeitemcode, timeitemname, tbm_timeitem.pk_timeitem, tbm_timeitem.itemtype, tbm_timeitemcopy.pk_timeitemcopy, "
				+ "tbm_timeitemcopy.pk_org, tbm_timeitemcopy.timeitemunit, tbm_timeitemcopy.leavesetperiod  "
				+ "from tbm_timeitem inner join tbm_timeitemcopy on tbm_timeitem.pk_timeitem = tbm_timeitemcopy.pk_timeitem  "
				+ "and tbm_timeitemcopy.pk_org = '"
				+ pk_org
				+ "'  "
				+ "where ( tbm_timeitem.itemtype = "+billcode+" and tbm_timeitemcopy.enablestate = 2 ) order by tbm_timeitem.timeitemcode ";
		List<Map<String, Object>> data = (List<Map<String, Object>>) new BaseDAO()
				.executeQuery(exesql, new MapListProcessor());
		return data;
	}
	
	public List<Map<String, Object>> queryTranstype(String userId,String billtype)
			throws BusinessException {
		String pk_psndoc = ((IUserPubService) NCLocator.getInstance().lookup(
				IUserPubService.class)).queryPsndocByUserid(userId);
		if (StringUtil.isBlank(pk_psndoc)) {
			throw new BusinessException("未找到该用户，请确认用户是否是员工");
		}
		TBMPsndocVO latestVO = ((ITBMPsndocQueryMaintain) NCLocator
				.getInstance().lookup(ITBMPsndocQueryMaintain.class))
				.queryByPsndocAndDateTime(pk_psndoc, new UFDateTime());
		String pk_group = "";
		if (latestVO == null && KQDA.contains(billtype)) {
			throw new BusinessException("当前用户没有考勤档案。");
		}else if(KQDA.contains(billtype)){
			pk_group = latestVO.getPk_group();
		} else {
			pk_group = PubEnv.getPk_group();
		}
		Integer billcode = 6404;
		if("leave".equals(billtype)){
			billcode = 6404;
		} else if("overtime".equals(billtype)){
			billcode = 6405;
		}else if("signcard".equals(billtype)){
			billcode = 6402;
		}else if("away".equals(billtype)){
			billcode = 6403;
		}else if("leaveoff".equals(billtype)){
			billcode = 6406;
		}else if("awayoff".equals(billtype)){
			billcode = 6407;
		}else if("psnreg".equals(billtype)){
			billcode = 6111;
		}else if("trns".equals(billtype)) {
			billcode = 6113;
		}else if("dimission".equals(billtype)) {
			billcode = 6115;
		}
		
		String exesql = "select pk_billtypecode transtype, billtypename transtypename, pk_billtypeid transtypeid from bd_billtype " 
				+ "where ( istransaction = 'Y' and pk_group = '" 
				+ pk_group
				+ "' and isnull ( islock, 'N' ) = 'N' and 1 = 1 ) and ( ( parentbilltype = '"
				+ billcode
				+ "' and pk_group = '" 
				+ pk_group
				+ "' ) or pk_billtypecode = '" + billcode + "' ) order by pk_billtypecode";
		List<Map<String, Object>> data = (List<Map<String, Object>>) new BaseDAO()
				.executeQuery(exesql, new MapListProcessor());
		return data;
	}

	public String getPk_leavetypecopy(String pk_leavetype, String pk_org)
			throws BusinessException {
		String exesql = "SELECT pk_timeitemcopy FROM tbm_timeitemcopy WHERE pk_timeitem = ? AND pk_org = ? ";
		SQLParameter para = new SQLParameter();
		para.addParam(pk_leavetype);
		para.addParam(pk_org);
		String pk_leavetypecody = (String) new BaseDAO().executeQuery(exesql,
				para, new ColumnProcessor());
		return pk_leavetypecody;
	}

	public Map<String, Object> getTranstypes(String userid, String billtype) {
		Map<String, Object> result = new HashMap();
		try {
			String pk_psndoc = ((IUserPubService) NCLocator.getInstance()
					.lookup(IUserPubService.class)).queryPsndocByUserid(userid);
			TBMPsndocVO latestVO = ((ITBMPsndocQueryMaintain) NCLocator
					.getInstance().lookup(ITBMPsndocQueryMaintain.class))
					.queryByPsndocAndDateTime(pk_psndoc, new UFDateTime());
			if (latestVO == null)
				return result;
			String condition = "SELECT * FROM bd_Billtype WHERE ( parentbilltype = '"
					+ TBMHelper.getBillTypeCode(billtype)
					+ "' and pk_group = '"
					+ latestVO.getPk_group()
					+ "') or pk_billtypecode = '"
					+ TBMHelper.getBillTypeCode(billtype) + "' ";
			Map<String, Object> executeQuery = (Map<String, Object>) new BaseDAO()
					.executeQuery(condition, new MapProcessor());
			result.put("transtypeid", executeQuery.get("pk_billtypeid"));
			result.put("transtypename", executeQuery.get("billtypename"));
			result.put("transtypecode", executeQuery.get("pk_billtypecode"));
		} catch (BusinessException e) {
			Logger.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * 
	 * @param leavehMap
	 * @param bList
	 * @param userid
	 * @param needCheck
	 * @param aggVO
	 * @return
	 * @throws BusinessException
	 */
	public AggLeaveVO saveLeave(String userid, boolean needCheck, AggLeaveVO aggVO) throws BusinessException {
		String warningMessage = "";
		String errMessage = "";
		ILeaveApplyApproveManageMaintain leaveM = (ILeaveApplyApproveManageMaintain) NCLocator
				.getInstance().lookup(ILeaveApplyApproveManageMaintain.class);
		String pk_leaveh = aggVO.getLeavehVO().getPk_leaveh();
		ILeaveApplyQueryMaintain maintain = (ILeaveApplyQueryMaintain)NCLocator.getInstance().lookup(ILeaveApplyQueryMaintain.class);
		LeaveCheckResult<AggLeaveVO> checkResult = maintain.checkWhenSave(aggVO);
		SplitBillResult<AggLeaveVO> splitResult = checkResult.getSplitResult();
		LeavehVO newleaveVO = ((AggLeaveVO)splitResult.getOriginalBill()).getLeavehVO();
		LeavebVO[] newleaveVOs = ((AggLeaveVO)splitResult.getOriginalBill()).getLeavebVOs();
		if (newleaveVO.getIslactation().booleanValue()) {
			((AggLeaveVO)splitResult.getOriginalBill()).getLeavehVO().setSumhour(new UFDouble(0));
			((AggLeaveVO)splitResult.getOriginalBill()).getLeavehVO().setLeaveindex(1);
			for (int i = 0; i < newleaveVOs.length; i++) {
				((AggLeaveVO)splitResult.getOriginalBill()).getLeavebVOs()[i].setLeavehour(new UFDouble(0));
			}
		}
		AggLeaveVO[] newAggVOs = null;
		if (StringUtils.isBlank(pk_leaveh)) {
			String newBill_code = TBMHelper.getBillCode("6404",aggVO.getHeadVO().getPk_group(),aggVO.getHeadVO().getPk_org());
			aggVO.getHeadVO().setBill_code(newBill_code);
			try{
				newAggVOs = leaveM.insertData(splitResult);
			} catch(BusinessException e){
				TBMHelper.rollbackBillCode("6404",aggVO.getHeadVO().getPk_group(),aggVO.getHeadVO().getPk_org(),newBill_code);
				throw new BusinessException(e.getMessage());
			}
		} else {
			newAggVOs = leaveM.updateData(splitResult);
		}
		return newAggVOs[0];
	}
	
	public Map<String, Object> rollBackLeave(String userid, AggLeaveVO newVO)
			throws BusinessException {
		String pk_org = newVO.getLeavehVO().getPk_org();
		HashMap<String, String> eParam = new HashMap<String, String>();
		if (isDirectApprove(pk_org, "6404")) {
			eParam.put("nosendmessage", "nosendmessage");
		}
		LeavebVO[] leavebVOs = newVO.getLeavebVOs();
		LeavehVO hvo = newVO.getLeavehVO();
		for (LeavebVO bvo : leavebVOs) {
			bvo.setPk_psnjob(hvo.getPk_psnjob());
			bvo.setPk_psndoc(hvo.getPk_psndoc());
			bvo.setPk_psnorg(hvo.getPk_psnorg());
		}

		PfProcessBatchRetObject validateRetObj = ((IHrPf) NCLocator
				.getInstance().lookup(IHrPf.class))
				.callbackValidate(
						"CallBack",
						"CallBack",
						null,
						true,
						new AggregatedValueObject[] { newVO });
		if ((validateRetObj.getRetObj() == null)
				|| (validateRetObj.getRetObj().length == 0)) {
			String errStr = validateRetObj.getExceptionMsg();
			if (StringUtils.isNotBlank(errStr))
				throw new BusinessException(errStr);
		}
		LfwPfUtil.runAction("RECALL", "6404", newVO, null, null, null, eParam,
				new String[] { userid }, null);

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("flag", "2");
		return result;
	}

	public Map<String, Object> submitLeave(String userid, AggLeaveVO newVO)
			throws BusinessException {
		String pk_org = newVO.getLeavehVO().getPk_org();
		HashMap<String, String> eParam = new HashMap<String, String>();
		LeavebVO[] leavebVOs = newVO.getLeavebVOs();
		LeavehVO hvo = newVO.getLeavehVO();
		for (LeavebVO bvo : leavebVOs) {
			bvo.setPk_psnjob(hvo.getPk_psnjob());
			bvo.setPk_psndoc(hvo.getPk_psndoc());
			bvo.setPk_psnorg(hvo.getPk_psnorg());
		}
		AggLeaveVO[] voss = new AggLeaveVO[1];
		voss[0] = newVO;
		if (isDirectApprove(pk_org, "6404")) {
			genRegAndCalculate(voss);
			String sql = "update tbm_leaveh set approve_state = "+IPfRetCheckInfo.PASSING+" where pk_leaveh='" + voss[0].getLeavehVO().getPk_leaveh()+"'";
			new BaseDAO().executeUpdate(sql);
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("flag", "2");
			return result;
		}
 		PfProcessBatchRetObject validateRetObj = ((IHrPf) NCLocator.getInstance().lookup(IHrPf.class)).submitValidation("Commit","Commit",null,SysInitQuery.getParaInt(pk_org,(String) IHrPf.hashBillTypePara.get("6404")).intValue(),new AggregatedValueObject[] { newVO });
		if ((validateRetObj.getRetObj() == null)
				|| (validateRetObj.getRetObj().length == 0)) {
			String errStr = validateRetObj.getExceptionMsg();
			if (StringUtils.isNotBlank(errStr))
				throw new BusinessException(errStr);
		}
		LfwPfUtil.runAction("SAVE", "6404", newVO, null, null, null, eParam,
				new String[] { userid }, null);

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("flag", "2");
		return result;
	}
	
	private void genRegAndCalculate(AggLeaveVO[] aggVOs)
			throws BusinessException {
		ILeaveRegisterManageMaintain regService = (ILeaveRegisterManageMaintain) NCLocator
				.getInstance().lookup(ILeaveRegisterManageMaintain.class);
		List<LeaveRegVO> insertList = new ArrayList();
		for (AggLeaveVO aggVO : aggVOs) {
			LeavehVO leavehVO = aggVO.getLeavehVO();
			for (LeavebVO leavebVO : aggVO.getBodyVOs()) {
				LeaveRegVO regVO = new LeaveRegVO();
				regVO.setPk_group(leavehVO.getPk_group());
				regVO.setPk_org(leavehVO.getPk_org());
				regVO.setBillsource(Integer.valueOf(0));
				regVO.setPk_billsourceh(leavehVO.getPk_leaveh());
				regVO.setBill_code(leavehVO.getBill_code());

				regVO.setIslactation(leavehVO.getIslactation());
				regVO.setLactationholidaytype(leavebVO
						.getLactationholidaytype());
				regVO.setLactationhour(leavehVO.getLactationhour());

				regVO.setIsleaveoff(UFBoolean.FALSE);
				regVO.setPk_leavetype(leavehVO.getPk_leavetype());
				regVO.setPk_leavetypecopy(leavehVO.getPk_leavetypecopy());
				regVO.setPk_psnjob(leavehVO.getPk_psnjob());
				regVO.setPk_psnorg(leavehVO.getPk_psnorg());
				regVO.setPk_psndoc(leavehVO.getPk_psndoc());
				regVO.setPk_timeitem(leavehVO.getPk_timeitem());
				regVO.setLeaveyear(leavehVO.getLeaveyear());
				regVO.setLeavemonth(leavehVO.getLeavemonth());
				regVO.setLeaveindex(leavehVO.getLeaveindex());

				regVO.setPk_billsourceb(leavebVO.getPk_leaveb());
				regVO.setLeavebegindate(leavebVO.getLeavebegindate());
				regVO.setLeaveenddate(leavebVO.getLeaveenddate());
				regVO.setLeavebegintime(leavebVO.getLeavebegintime());
				regVO.setLeaveendtime(leavebVO.getLeaveendtime());

				regVO.setLeaveremark(leavebVO.getLeaveremark());
				regVO.setLeavehour(leavebVO.getLeavehour());

				regVO.setRealdayorhour(leavehVO.getRealdayorhour());
				regVO.setResteddayorhour(leavehVO.getResteddayorhour());
				regVO.setRestdayorhour(leavehVO.getRestdayorhour());
				regVO.setFreezedayorhour(leavehVO.getFreezedayorhour());
				regVO.setUsefuldayorhour(leavehVO.getUsefuldayorhour());

				regVO.setPk_org_v(leavehVO.getPk_org_v());
				regVO.setPk_dept_v(leavehVO.getPk_dept_v());

				insertList.add(regVO);
			}
		}
		regService.insertData(
				(LeaveRegVO[]) insertList.toArray(new LeaveRegVO[0]), false);
		((ILeaveBalanceManageService) NCLocator.getInstance().lookup(
				ILeaveBalanceManageService.class)).queryAndCalLeaveBalanceVO(
				aggVOs[0].getLeavehVO().getPk_org(), (Object[]) aggVOs);
	}
	public String getDeptnameByPsndocAndDateTime(String pk_psndoc,
			UFDateTime ufDateTime) throws BusinessException {
		String exesql = "select org_dept.name from tbm_psndoc inner join hi_psnjob on tbm_psndoc.pk_psnjob = hi_psnjob.pk_psnjob "+
				" left join org_dept on hi_psnjob.pk_dept = org_dept.pk_dept "+
				" where tbm_psndoc.pk_psndoc = '"+pk_psndoc+"' and '"+ufDateTime.toString()+"' between tbm_psndoc.begindate and tbm_psndoc.enddate ";
		String deptname = (String) new BaseDAO().executeQuery(exesql, new ColumnProcessor());
		return deptname;
	}
	/**
	 * 请假打印信息查询
	 * @param pk_leaveh
	 * @return
	 */
	public Map<String, Object> leavePrintTemplate(String pk_leaveh)throws BusinessException{
		Map<String, Object> map = new HashMap<>();
		StringBuilder builder = new StringBuilder();
		builder.append("select a.bill_code as billcode, b.name as orgname, d.name as deptname, e.timeitemname as type, a.approve_state as status, ");
		builder.append("f.user_name as applyer, a.apply_date as applydate, a.approve_time as approvedate, g.postname as postname ");
		builder.append("from tbm_leaveh a ");
		builder.append("inner join org_hrorg b on a.pk_org = b.pk_hrorg ");
		builder.append("inner join hi_psnjob c on c.pk_psnjob = a.pk_psnjob ");
		builder.append("inner join org_dept d on d.pk_dept = c.pk_dept ");
		builder.append("inner join tbm_timeitem e on e. pk_timeitem = a.pk_leavetype ");
		builder.append("inner join sm_user f on f.cuserid = a.billmaker ");
		builder.append("inner join om_post g on g.pk_post = c.pk_post ");
		builder.append("where a.pk_leaveh = '" + pk_leaveh + "' ");
		HashMap<String, Object> dataMap = (HashMap<String, Object>)new BaseDAO().executeQuery(builder.toString(), new MapProcessor());
		
		StringBuilder sb = new StringBuilder();
		sb.append("select a.leavebegintime as leavebegintime, a.leaveendtime as leaveendtime, a.leavehour as leavehour, a.leaveremark as leaveremark ");
		sb.append("from tbm_leaveb a ");
		sb.append("where a.pk_leaveh = '" + pk_leaveh + "'");
		sb.append("order by a.leavebegintime asc ");
		List list = (List) new BaseDAO().executeQuery(sb.toString(),
				new MapListProcessor());
		
		StringBuilder sba = new StringBuilder();
		sba.append("select b.user_name as sender, a.senddate as senddate, c.user_name as approver, a.dealdate as approdate, a.checknote as approveidea ");
		sba.append("from pub_workflownote a ");
		sba.append("inner join sm_user b on b.cuserid = a.senderman ");
		sba.append("inner join sm_user c on c.cuserid = a.checkman ");
		sba.append("where  billid = '" + pk_leaveh + "' and actiontype <> 'BIZ' ");
		sba.append("order by a.senddate asc ");
		List notelist = (List) new BaseDAO().executeQuery(sba.toString(),
				new MapListProcessor());
		map.put("data", dataMap);
		map.put("tableData", list);
		map.put("approveData", notelist);
		return map;
	}
	
	/**
	 * 获取审批列表
	 * @param userId
	 * @param begindate
	 * @param billtype
	 * @param pageno
	 * @param pagesize
	 * @param inCond
	 * @param isFinish
	 * @return
	 * @throws BusinessException
	 */
	public PageResult queryBills(String userId, UFLiteralDate begindate,
			String billtype, Integer pageno, Integer pagesize, String inCond,
			String isFinish) throws BusinessException {
		
		PageResult result = new PageResult();

		String pk_psndoc = ((IUserPubService) NCLocator.getInstance().lookup(
				IUserPubService.class)).queryPsndocByUserid(userId);
		if (StringUtil.isBlank(pk_psndoc)) {
			result.setStatusCode(PageResult.STATUS_ERROR);
			result.setMessage("未找到该用户，请确认用户是否是员工。");
			return result;
		}
		String condition = " where pub_workflownote.CHECKMAN='"+userId+"' and ishrssbill = 'Y' and " + "approve_state" + inCond +" and pub_workflownote.ischeck='" + isFinish +"' ";
		String DimissionCondition = " where pub_workflownote.CHECKMAN='"+userId+"' and ishrssbill = 'Y' and " + "approve_state" + inCond +" and pub_workflownote.ischeck in ('N','Y') and hi_stapply.fun_code='60090dimissionapply'";
		String trnsCondition = " where pub_workflownote.CHECKMAN='"+userId+"' and ishrssbill = 'Y' and " + "approve_state" + inCond +" and pub_workflownote.ischeck in ('N','Y') and hi_stapply.fun_code='60090transapply'";
		String condition4off = " where pub_workflownote.CHECKMAN='"+userId+"' and " + "approve_state" + inCond +" and pub_workflownote.ischeck='" + isFinish +"' ";
		if (begindate != null) {
			condition = condition + " and apply_date > '"
					+ begindate.toString() + "' ";
			condition4off = condition4off + " and apply_date > '"
					+ begindate.toString() + "' ";
		}
		String approvesql = ",case when approve_state = -1 then '自由' when approve_state = 0 then '审批未通过' when approve_state = 1 then '审批通过' when approve_state = 2 then '审批进行中' when approve_state = 3 then '已提交' when approve_state = 102 then '已执行' end approve_state_name ";
		StringBuffer buffer = new StringBuffer();
		if("all".equals(billtype)||"leave".equals(billtype)){
			buffer.append("select apply_date apply_date, approve_note approve_note, approve_state approve_state,bill_code bill_code,fun_code fun_code, tbm_leaveh.pk_billtype pk_billtype,transtype transtype ,transtypeid transtypeid, bd_billtype.billtypename transtype_name,bd_psndoc.name psnname  ");
			buffer.append(approvesql);
			buffer.append(",pk_leaveh pk_h,leaveremark tbm_remark ,pk_leavetype pk_tbmtype, sumhour sumhour, '请假' tbm_h_name ,'leave' billtype ");
			buffer.append(",tbm_timeitem.timeitemname tbmtype_name ,tbm_leaveh.ishrssbill ishrssbill ");
			buffer.append(",(SELECT min(leavebegindate) FROM tbm_leaveb WHERE tbm_leaveb.pk_leaveh = tbm_leaveh.pk_leaveh) tbm_begindate ");
			buffer.append(",(SELECT max(leaveenddate) FROM tbm_leaveb WHERE tbm_leaveb.pk_leaveh = tbm_leaveh.pk_leaveh)  tbm_enddate ");
			buffer.append(" FROM tbm_leaveh inner join tbm_timeitem on tbm_leaveh.pk_leavetype = tbm_timeitem.pk_timeitem inner join bd_billtype on bd_billtype.pk_billtypeid=tbm_leaveh.TRANSTYPEID ");
			buffer.append(" inner join pub_workflownote on tbm_leaveh.PK_LEAVEH = pub_workflownote.BILLVERSIONPK left join bd_psndoc on tbm_leaveh.pk_psndoc = bd_psndoc.pk_psndoc ");
			buffer.append(condition);
			buffer.append(" union ");
			buffer.append("select apply_date apply_date, approve_note approve_note, approve_state approve_state,bill_code bill_code,fun_code fun_code, tbm_leaveh.pk_billtype pk_billtype,transtype transtype ,transtypeid transtypeid, case when transtype='~' then '' end transtype_name,bd_psndoc.name psnname ");
			buffer.append(approvesql);
			buffer.append(",pk_leaveh pk_h,leaveremark tbm_remark ,pk_leavetype pk_tbmtype, sumhour sumhour, '请假' tbm_h_name ,'leave' billtype ");
			buffer.append(",tbm_timeitem.timeitemname tbmtype_name ,tbm_leaveh.ishrssbill ishrssbill ");
			buffer.append(",(SELECT min(leavebegindate) FROM tbm_leaveb WHERE tbm_leaveb.pk_leaveh = tbm_leaveh.pk_leaveh) tbm_begindate ");
			buffer.append(",(SELECT max(leaveenddate) FROM tbm_leaveb WHERE tbm_leaveb.pk_leaveh = tbm_leaveh.pk_leaveh)  tbm_enddate ");
			buffer.append(" FROM tbm_leaveh inner join tbm_timeitem on tbm_leaveh.pk_leavetype = tbm_timeitem.pk_timeitem ");
			buffer.append(" inner join pub_workflownote on tbm_leaveh.PK_LEAVEH = pub_workflownote.BILLVERSIONPK left join bd_psndoc on tbm_leaveh.pk_psndoc = bd_psndoc.pk_psndoc ");
			buffer.append(condition + " and  tbm_leaveh.TRANSTYPEID='~' ");
		}
		
		if("all".equals(billtype)||"signcard".equals(billtype)){
			if("all".equals(billtype)){
				buffer.append(" union ");	
			}
			buffer.append("select apply_date apply_date, approve_note approve_note, approve_state approve_state,bill_code bill_code,fun_code fun_code, tbm_signh.pk_billtype pk_billtype,transtype transtype ,transtypeid transtypeid, bd_billtype.billtypename transtype_name,bd_psndoc.name psnname ");
			buffer.append(approvesql);
			buffer.append(", pk_signh pk_h,signremark tbm_remark ,'' pk_tbmtype,1 sumhour , '签卡' tbm_h_name ,'signcard' billtype");
			buffer.append(",'' tbmtype_name ,tbm_signh.ishrssbill ishrssbill ");
			buffer.append(",(SELECT min(signdate) FROM tbm_signb WHERE tbm_signb.pk_signh = tbm_signh.pk_signh) tbm_begindate ");
			buffer.append(",(SELECT max(signdate) FROM tbm_signb WHERE tbm_signb.pk_signh = tbm_signh.pk_signh)  tbm_enddate ");
			buffer.append(" FROM tbm_signh inner join bd_billtype on bd_billtype.pk_billtypeid=tbm_signh.TRANSTYPEID ");
			buffer.append(" inner join pub_workflownote on tbm_signh.pk_signh = pub_workflownote.BILLVERSIONPK  left join bd_psndoc on tbm_signh.pk_psndoc = bd_psndoc.pk_psndoc ");
			buffer.append(condition);
			buffer.append(" union ");
			buffer.append("select apply_date apply_date, approve_note approve_note, approve_state approve_state,bill_code bill_code,fun_code fun_code, tbm_signh.pk_billtype pk_billtype,transtype transtype ,transtypeid transtypeid, case when transtype='~' then '' end transtype_name,bd_psndoc.name psnname ");
			buffer.append(approvesql);
			buffer.append(", pk_signh pk_h,signremark tbm_remark ,'' pk_tbmtype,1 sumhour , '签卡' tbm_h_name ,'signcard' billtype");
			buffer.append(",'' tbmtype_name ,tbm_signh.ishrssbill ishrssbill ");
			buffer.append(",(SELECT min(signdate) FROM tbm_signb WHERE tbm_signb.pk_signh = tbm_signh.pk_signh) tbm_begindate ");
			buffer.append(",(SELECT max(signdate) FROM tbm_signb WHERE tbm_signb.pk_signh = tbm_signh.pk_signh)  tbm_enddate ");
			buffer.append(" FROM tbm_signh ");
			buffer.append(" inner join pub_workflownote on tbm_signh.pk_signh = pub_workflownote.BILLVERSIONPK  left join bd_psndoc on tbm_signh.pk_psndoc = bd_psndoc.pk_psndoc ");
			buffer.append(condition + " and  tbm_signh.TRANSTYPEID = '~' ");
		}

		if("all".equals(billtype)||"overtime".equals(billtype)){
			if("all".equals(billtype)){
				buffer.append(" union ");	
			}
			buffer.append("select apply_date apply_date, approve_note approve_note, approve_state approve_state,bill_code bill_code,fun_code fun_code, tbm_overtimeh.pk_billtype pk_billtype,transtype transtype ,transtypeid transtypeid, bd_billtype.billtypename transtype_name,bd_psndoc.name psnname ");

			buffer.append(approvesql);
			buffer.append(" , pk_overtimeh  pk_h, '' tbm_remark ,pk_overtimetype pk_tbmtype,sumhour sumhour , '加班' tbm_h_name,'overtime' billtype ");
			buffer.append(",tbm_timeitem.timeitemname tbmtype_name ,tbm_overtimeh.ishrssbill ishrssbill ");
			buffer.append(",(SELECT min(overtimebegindate) FROM tbm_overtimeb WHERE tbm_overtimeb.pk_overtimeh = tbm_overtimeh.pk_overtimeh) tbm_begindate ");
			buffer.append(",(SELECT max(overtimeenddate) FROM tbm_overtimeb WHERE tbm_overtimeb.pk_overtimeh = tbm_overtimeh.pk_overtimeh)  tbm_enddate ");
			buffer.append(" FROM tbm_overtimeh inner join tbm_timeitem on tbm_overtimeh.pk_overtimetype = tbm_timeitem.pk_timeitem inner join bd_billtype on bd_billtype.pk_billtypeid=tbm_overtimeh.TRANSTYPEID ");
			buffer.append(" inner join pub_workflownote on tbm_overtimeh.pk_overtimeh  = pub_workflownote.BILLVERSIONPK left join bd_psndoc on tbm_overtimeh.pk_psndoc = bd_psndoc.pk_psndoc");
			buffer.append(condition);
			buffer.append(" union ");
			buffer.append("select apply_date apply_date, approve_note approve_note, approve_state approve_state,bill_code bill_code,fun_code fun_code, tbm_overtimeh.pk_billtype pk_billtype,transtype transtype ,transtypeid transtypeid, case when transtype='~' then '' end transtype_name,bd_psndoc.name psnname ");

			buffer.append(approvesql);
			buffer.append(" , pk_overtimeh  pk_h, '' tbm_remark ,pk_overtimetype pk_tbmtype,sumhour sumhour , '加班' tbm_h_name,'overtime' billtype ");
			buffer.append(",tbm_timeitem.timeitemname tbmtype_name ,tbm_overtimeh.ishrssbill ishrssbill ");
			buffer.append(",(SELECT min(overtimebegindate) FROM tbm_overtimeb WHERE tbm_overtimeb.pk_overtimeh = tbm_overtimeh.pk_overtimeh) tbm_begindate ");
			buffer.append(",(SELECT max(overtimeenddate) FROM tbm_overtimeb WHERE tbm_overtimeb.pk_overtimeh = tbm_overtimeh.pk_overtimeh)  tbm_enddate ");
			buffer.append(" FROM tbm_overtimeh inner join tbm_timeitem on tbm_overtimeh.pk_overtimetype = tbm_timeitem.pk_timeitem ");
			buffer.append(" inner join pub_workflownote on tbm_overtimeh.pk_overtimeh = pub_workflownote.BILLVERSIONPK left join bd_psndoc on tbm_overtimeh.pk_psndoc = bd_psndoc.pk_psndoc");
			buffer.append(condition + " and tbm_overtimeh.TRANSTYPEID='~' ");
		}

		if("all".equals(billtype)||"away".equals(billtype)){
			if("all".equals(billtype)){
				buffer.append(" union ");	
			}
			buffer.append("select apply_date apply_date, approve_note approve_note, approve_state approve_state,bill_code bill_code,fun_code fun_code, tbm_awayh.pk_billtype pk_billtype,transtype transtype ,transtypeid transtypeid, bd_billtype.billtypename transtype_name,bd_psndoc.name psnname ");

			buffer.append(approvesql);
			buffer.append(" ,pk_awayh pk_h, awayremark tbm_remark ,pk_awaytype pk_tbmtype,sumhour sumhour, '出差' tbm_h_name ,'away' billtype  ");
			buffer.append(",tbm_timeitem.timeitemname tbmtype_name ,tbm_awayh.ishrssbill ishrssbill ");
			buffer.append(",(SELECT min(awaybegintime) FROM tbm_awayb WHERE tbm_awayb.pk_awayh = tbm_awayh.pk_awayh) tbm_begindate ");
			buffer.append(",(SELECT max(awayendtime) FROM tbm_awayb WHERE tbm_awayb.pk_awayh = tbm_awayh.pk_awayh)  tbm_enddate ");
			buffer.append(" FROM tbm_awayh inner join tbm_timeitem on tbm_awayh.pk_awaytype = tbm_timeitem.pk_timeitem inner join bd_billtype on bd_billtype.pk_billtypeid=tbm_awayh.TRANSTYPEID ");
			buffer.append(" inner join pub_workflownote on tbm_awayh.pk_awayh= pub_workflownote.BILLVERSIONPK left join bd_psndoc on tbm_awayh.pk_psndoc = bd_psndoc.pk_psndoc ");
			buffer.append(condition);
			buffer.append(" union ");
			buffer.append(" select apply_date apply_date, approve_note approve_note, approve_state approve_state,bill_code bill_code,fun_code fun_code, tbm_awayh.pk_billtype pk_billtype,transtype transtype ,transtypeid transtypeid, case when transtype='~' then '' end transtype_name,bd_psndoc.name psnname ");
			buffer.append(approvesql);
			buffer.append(" ,pk_awayh pk_h, awayremark tbm_remark ,pk_awaytype pk_tbmtype,sumhour sumhour, '出差' tbm_h_name ,'away' billtype  ");
			buffer.append(",tbm_timeitem.timeitemname tbmtype_name ,tbm_awayh.ishrssbill ishrssbill ");
			buffer.append(",(SELECT min(awaybegintime) FROM tbm_awayb WHERE tbm_awayb.pk_awayh = tbm_awayh.pk_awayh) tbm_begindate ");
			buffer.append(",(SELECT max(awayendtime) FROM tbm_awayb WHERE tbm_awayb.pk_awayh = tbm_awayh.pk_awayh)  tbm_enddate ");
			buffer.append(" FROM tbm_awayh inner join tbm_timeitem on tbm_awayh.pk_awaytype = tbm_timeitem.pk_timeitem ");
			buffer.append(" inner join pub_workflownote on tbm_awayh.pk_awayh = pub_workflownote.BILLVERSIONPK left join bd_psndoc on tbm_awayh.pk_psndoc = bd_psndoc.pk_psndoc ");
			//修改于2019/11/22
		    //buffer.append(condition + " and tbm_awayh.TRANSTYPEID='~' ");
			buffer.append(condition);
		}
		
		if("all".equals(billtype)||"leaveoff".equals(billtype)){
			if("all".equals(billtype)){
				buffer.append(" union ");	
			}
			buffer.append("select apply_date apply_date, approve_note approve_note, approve_state approve_state,bill_code bill_code,fun_code fun_code, tbm_leaveoff.pk_billtype pk_billtype,transtype transtype ,transtypeid transtypeid, bd_billtype.billtypename transtype_name,bd_psndoc.name psnname ");

			buffer.append(approvesql);
			buffer.append(" ,pk_leaveoff pk_h, '' tbm_remark ,pk_leavetype pk_tbmtype,reallyleavehour sumhour, '销假' tbm_h_name ,'leaveoff' billtype  ");
			buffer.append(",tbm_timeitem.timeitemname tbmtype_name ,'Y' ishrssbill ");
			buffer.append(",leavebegintime tbm_begindate ,leaveendtime  tbm_enddate ");
			buffer.append(" FROM tbm_leaveoff inner join tbm_timeitem on tbm_leaveoff.pk_leavetype = tbm_timeitem.pk_timeitem inner join bd_billtype on bd_billtype.pk_billtypeid=tbm_leaveoff.TRANSTYPEID ");
			buffer.append(" inner join pub_workflownote on tbm_leaveoff.pk_leaveoff= pub_workflownote.BILLVERSIONPK left join bd_psndoc on tbm_leaveoff.pk_psndoc = bd_psndoc.pk_psndoc ");
			buffer.append(condition4off);
			buffer.append(" union ");
			buffer.append(" select apply_date apply_date, approve_note approve_note, approve_state approve_state,bill_code bill_code,fun_code fun_code, tbm_leaveoff.pk_billtype pk_billtype,transtype transtype ,transtypeid transtypeid, case when transtype='~' then '' end transtype_name,bd_psndoc.name psnname ");
			buffer.append(approvesql);
			buffer.append(" ,pk_leaveoff pk_h, '' tbm_remark ,pk_leavetype pk_tbmtype,reallyleavehour sumhour, '销假' tbm_h_name ,'leaveoff' billtype  ");
			buffer.append(",tbm_timeitem.timeitemname tbmtype_name ,'Y' ishrssbill ");
			buffer.append(",leavebegintime tbm_begindate ,leaveendtime tbm_enddate ");
			buffer.append(" FROM tbm_leaveoff inner join tbm_timeitem on tbm_leaveoff.pk_leavetype = tbm_timeitem.pk_timeitem ");
			buffer.append(" inner join pub_workflownote on tbm_leaveoff.pk_leaveoff = pub_workflownote.BILLVERSIONPK left join bd_psndoc on tbm_leaveoff.pk_psndoc = bd_psndoc.pk_psndoc ");
			buffer.append(condition4off + " and tbm_leaveoff.TRANSTYPEID='~' ");
		}
		
		if("all".equals(billtype)||"awayoff".equals(billtype)){
			if("all".equals(billtype)){
				buffer.append(" union ");	
			}
			buffer.append("select apply_date apply_date, approve_note approve_note, approve_state approve_state,bill_code bill_code,fun_code fun_code, tbm_awayoff.pk_billtype pk_billtype,transtype transtype ,transtypeid transtypeid, bd_billtype.billtypename transtype_name,bd_psndoc.name psnname ");

			buffer.append(approvesql);
			buffer.append(" ,pk_awayoff pk_h, '' tbm_remark ,pk_awaytype pk_tbmtype,reallyawayhour sumhour, '销差' tbm_h_name ,'awayoff' billtype  ");
			buffer.append(",tbm_timeitem.timeitemname tbmtype_name ,'Y' ishrssbill ");
			buffer.append(",awaybegintime tbm_begindate ,awayendtime  tbm_enddate ");
			buffer.append(" FROM tbm_awayoff inner join tbm_timeitem on tbm_awayoff.pk_awaytype = tbm_timeitem.pk_timeitem inner join bd_billtype on bd_billtype.pk_billtypeid=tbm_awayoff.TRANSTYPEID ");
			buffer.append(" inner join pub_workflownote on tbm_awayoff.pk_awayoff= pub_workflownote.BILLVERSIONPK  left join bd_psndoc on tbm_awayoff.pk_psndoc = bd_psndoc.pk_psndoc ");
			buffer.append(condition4off);
			buffer.append(" union ");
			buffer.append(" select apply_date apply_date, approve_note approve_note, approve_state approve_state,bill_code bill_code,fun_code fun_code, tbm_awayoff.pk_billtype pk_billtype,transtype transtype ,transtypeid transtypeid, case when transtype='~' then '' end transtype_name,bd_psndoc.name psnname ");
			buffer.append(approvesql);
			buffer.append(" ,pk_awayoff pk_h, '' tbm_remark ,pk_awaytype pk_tbmtype,reallyawayhour sumhour, '销差' tbm_h_name ,'awayoff' billtype  ");
			buffer.append(",tbm_timeitem.timeitemname tbmtype_name ,'Y' ishrssbill ");
			buffer.append(",awaybegintime tbm_begindate ,awayendtime tbm_enddate ");
			buffer.append(" FROM tbm_awayoff inner join tbm_timeitem on tbm_awayoff.pk_awaytype = tbm_timeitem.pk_timeitem ");
			buffer.append(" inner join pub_workflownote on tbm_awayoff.pk_awayoff = pub_workflownote.BILLVERSIONPK  left join bd_psndoc on tbm_awayoff.pk_psndoc = bd_psndoc.pk_psndoc ");
			buffer.append(condition4off + " and tbm_awayoff.TRANSTYPEID='~' ");
		}
		
		if("all".equals(billtype)||"dimission".equals(billtype)){
			if("all".equals(billtype)){
				buffer.append(" union ");	
			}
			buffer.append("SELECT apply_date apply_date,approve_note approve_note,approve_state approve_state,bill_code bill_code,fun_code fun_code,hi_stapply.pk_billtype pk_billtype,transtype transtype,transtypeid transtypeid,bd_billtype.billtypename transtype_name,bd_psndoc.name psnname");
			buffer.append(approvesql);
			buffer.append(",pk_hi_stapply pk_h,hi_stapply.memo tbm_remark,hi_stapply.pk_trnstype pk_tbmtype,1 sumhour,'离职' tbm_h_name ,'dimission' billtype,hr_trnstype.trnstypename  tbmtype_name ,hi_stapply.ishrssbill ishrssbill,hi_stapply.effectdate tbm_begindate,hi_stapply.effectdate tbm_enddate ");
			buffer.append("FROM hi_stapply inner JOIN bd_billtype ON bd_billtype.pk_billtypeid=hi_stapply.TRANSTYPEID inner JOIN hr_trnstype ON hi_stapply.pk_trnstype=hr_trnstype.pk_trnstype inner JOIN pub_workflownote ON hi_stapply.pk_hi_stapply = pub_workflownote.BILLVERSIONPK inner JOIN bd_psndoc ON hi_stapply.pk_psndoc = bd_psndoc.pk_psndoc");
			buffer.append(DimissionCondition);
		}
		if("all".equals(billtype)||"trns".equals(billtype)){
			if("all".equals(billtype)){
				buffer.append(" union ");	
			}
			buffer.append("SELECT apply_date apply_date,approve_note approve_note,approve_state approve_state,bill_code bill_code,fun_code fun_code,hi_stapply.pk_billtype pk_billtype,transtype transtype,transtypeid transtypeid,bd_billtype.billtypename transtype_name,bd_psndoc.name psnname");
			buffer.append(approvesql);
			buffer.append(",pk_hi_stapply pk_h,hi_stapply.memo tbm_remark,hi_stapply.pk_trnstype pk_tbmtype,1 sumhour,'调配' tbm_h_name ,'trns' billtype,hr_trnstype.trnstypename  tbmtype_name ,hi_stapply.ishrssbill ishrssbill,hi_stapply.effectdate tbm_begindate,hi_stapply.effectdate tbm_enddate ");
			buffer.append(" FROM hi_stapply inner JOIN bd_billtype ON bd_billtype.pk_billtypeid=hi_stapply.TRANSTYPEID inner JOIN hr_trnstype ON hi_stapply.pk_trnstype=hr_trnstype.pk_trnstype inner JOIN pub_workflownote ON hi_stapply.pk_hi_stapply = pub_workflownote.BILLVERSIONPK inner JOIN bd_psndoc ON hi_stapply.pk_psndoc = bd_psndoc.pk_psndoc ");
			buffer.append(trnsCondition);
			buffer.append(" union ");
			buffer.append("SELECT apply_date apply_date,approve_note approve_note,approve_state approve_state,bill_code bill_code,fun_code fun_code,hi_stapply.pk_billtype pk_billtype,transtype transtype,transtypeid transtypeid,case when transtype='~' then '' end transtype_name,bd_psndoc.name psnname");
			buffer.append(approvesql);
			buffer.append(",pk_hi_stapply pk_h,hi_stapply.memo tbm_remark,hi_stapply.pk_trnstype pk_tbmtype,1 sumhour,'调配' tbm_h_name ,'trns' billtype,hr_trnstype.trnstypename  tbmtype_name ,hi_stapply.ishrssbill ishrssbill,hi_stapply.effectdate tbm_begindate,hi_stapply.effectdate tbm_enddate ");
			buffer.append(" FROM hi_stapply inner JOIN bd_billtype ON bd_billtype.pk_billtypeid=hi_stapply.TRANSTYPEID inner JOIN hr_trnstype ON hi_stapply.pk_trnstype=hr_trnstype.pk_trnstype inner JOIN pub_workflownote ON hi_stapply.pk_hi_stapply = pub_workflownote.BILLVERSIONPK inner JOIN bd_psndoc ON hi_stapply.pk_psndoc = bd_psndoc.pk_psndoc ");
			buffer.append(trnsCondition + " and hi_stapply.transtypeid='~'");
		}
		if("all".equals(billtype)||"psnreg".equals(billtype)){
			if("all".equals(billtype)){
				buffer.append(" union ");	
			}
			buffer.append("SELECT apply_date apply_date,approve_note approve_note,approve_state approve_state,bill_code bill_code,bill_code fun_code,hi_regapply.pk_billtype pk_billtype,transtype transtype,transtypeid transtypeid,bd_billtype.billtypename transtype_name,bd_psndoc.name psnname");
			buffer.append(approvesql);
			buffer.append(",hi_regapply.pk_hi_regapply  pk_h,hi_regapply.memo tbm_remark,'1' pk_tbmtype,1 sumhour,'转正' tbm_h_name ,'psnreg' billtype,case hi_regapply.probation_type when 1 then '入职试用' when 2 then '转岗试用' end tbmtype_name ,hi_regapply.ishrssbill ishrssbill,hi_regapply.begin_date tbm_begindate,hi_regapply.regulardate tbm_enddate ");
			buffer.append(" FROM hi_regapply inner JOIN bd_billtype ON bd_billtype.pk_billtypeid=hi_regapply.TRANSTYPEID inner JOIN pub_workflownote ON hi_regapply.pk_hi_regapply = pub_workflownote.BILLVERSIONPK inner JOIN bd_psndoc ON hi_regapply.pk_psndoc = bd_psndoc.pk_psndoc ");
			buffer.append(condition);
			buffer.append(" union ");
			buffer.append("SELECT apply_date apply_date,approve_note approve_note,approve_state approve_state,bill_code bill_code,bill_code fun_code,hi_regapply.pk_billtype pk_billtype,transtype transtype,transtypeid transtypeid,case when transtype='~' then '' end transtype_name,bd_psndoc.name psnname");
			buffer.append(approvesql);
			buffer.append(",hi_regapply.pk_hi_regapply  pk_h,hi_regapply.memo tbm_remark,'1' pk_tbmtype,1 sumhour,'转正' tbm_h_name ,'psnreg' billtype,case hi_regapply.probation_type when 1 then '入职试用' when 2 then '转岗试用' end tbmtype_name ,hi_regapply.ishrssbill ishrssbill,hi_regapply.begin_date tbm_begindate,hi_regapply.regulardate tbm_enddate ");
			buffer.append(" FROM hi_regapply inner JOIN bd_billtype ON bd_billtype.pk_billtypeid=hi_regapply.TRANSTYPEID inner JOIN pub_workflownote ON hi_regapply.pk_hi_regapply = pub_workflownote.BILLVERSIONPK inner JOIN bd_psndoc ON hi_regapply.pk_psndoc = bd_psndoc.pk_psndoc ");
			buffer.append(condition + " and hi_regapply.transtypeid='~'");
		}
 		String exesql = "select * from (" + buffer.toString()
				+ ") tbm_temp order by tbm_temp.apply_date desc  ";
		if (pageno == null || pagesize == null) {
			List<Map<String, Object>> data = (List<Map<String, Object>>) new BaseDAO()
					.executeQuery(exesql, new MapListProcessor());
			result.setData(data);
		} else {
			int queryTotalCount = SaasCommonHelper.queryTotalCount(exesql);
			String querystrSQL = SaasCommonHelper.processPageSql(exesql,
					pageno, pagesize);
			List<Map<String, Object>> data = (List<Map<String, Object>>) new BaseDAO()
					.executeQuery(querystrSQL, new MapListProcessor());
			result.setData(data);
			result.setTotalCount(queryTotalCount);
			result.setPageno(pageno);
		}
		return result;
	}
}
