package nc.impl.saas.hi;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.NCLocator;
import nc.hr.utils.MultiLangHelper;
import nc.itf.hr.pf.IHrPf;
import nc.itf.ta.IAwayOffApplyQueryMaintain;
import nc.itf.ta.IAwayOffManageMaintain;
import nc.itf.ta.IAwayRegisterQueryMaintain;
import nc.itf.ta.ILeaveOffApplyQueryMaintain;
import nc.itf.ta.ILeaveOffManageMaintain;
import nc.itf.ta.ILeaveRegisterQueryMaintain;
import nc.itf.ta.ITimeItemQueryService;
import nc.itf.ta.ITimeRuleQueryService;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.jdbc.framework.processor.MapProcessor;
import nc.pubitf.para.SysInitQuery;
import nc.vo.pmpub.project.ProjectHeadVO;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.billtype.BilltypeVO;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.pf.IPfRetCheckInfo;
import nc.vo.ta.PublicLangRes;
import nc.vo.ta.away.AwayRegVO;
import nc.vo.ta.awayoff.AggAwayOffVO;
import nc.vo.ta.awayoff.AwayOffVO;
import nc.vo.ta.leave.LeaveRegVO;
import nc.vo.ta.leaveoff.AggLeaveoffVO;
import nc.vo.ta.leaveoff.LeaveoffVO;
import nc.vo.ta.timeitem.AwayTypeCopyVO;
import nc.vo.ta.timeitem.LeaveTypeCopyVO;
import nc.vo.ta.timerule.TimeRuleVO;
import nc.vo.uap.pf.PfProcessBatchRetObject;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class TBMOffDao {

	public Map<String, Object> queryLeavereg4off(String pk_psndoc, String pk_org) throws BusinessException {
		Map<String, Object> result = new HashMap<String, Object>();
		String sql = "select a.pk_leavereg as id, b.timeitemname as type, a.leavebegindate as begindate,a.leavebegintime as begintime" +
				", a.leaveendtime as endtime, leaveenddate as enddate, a.leavehour as leavehour" +
				", a.lactationhour as lactationhour, case when a.lactationholidaytype=0 then '单一作息时间段' " +
				"when a.lactationholidaytype=1 then '上班时段' when a.lactationholidaytype=2 then '下班时段' " +
				"when a.lactationholidaytype=3 then '任意时段' else '' end lactationtype, a.islactation as islactation" +
				", a.leaveremark as leaveremark" + 
				", case when c.timeitemunit=0 then '天' else '小时' end as unit " +
				"from tbm_leavereg a " +
				"inner join tbm_timeitem b on a.pk_leavetype = b.pk_timeitem " +
				"inner join tbm_timeitemcopy c on a.pk_leavetypecopy = c.pk_timeitemcopy " +
				"where pk_psndoc =? " +
				"and pk_leavereg  not in (" +
				"select pk_leavereg from TBM_LEAVEOFF where APPROVE_STATE  not in (0,1)" +
				") and a.leaveenddate> ( " +
				"select nvl(MAX(enddate),'1971') from tbm_period where sealflag='Y' and pk_org = ? " +
				") and billsource = 0";
		SQLParameter param = new SQLParameter();
		param.addParam(pk_psndoc);
		param.addParam(pk_org);
		List<Map<String, Object>> data = (List<Map<String, Object>>) new BaseDAO()
		.executeQuery(sql.toString(), param, new MapListProcessor());
		if(data != null && data.size()>0){
			result.put("data", data);
		}else {
			result.put("data", null); 
		}
		return result;
	}
	
	public Map<String, Object> queryAwayreg4off(String pk_psndoc, String pk_org) throws BusinessException {
		Map<String, Object> result = new HashMap<String, Object>();
		String sql = "select a.pk_awayreg as id, b.timeitemname as type, a.awaybegindate as begindate,a.awaybegintime as begintime" +
				", a.awayendtime as endtime, awayenddate as enddate, a.awayhour as awayhour, a.awayaddress as awayaddress, a.workprocess as workprocess" +
				", case when c.timeitemunit=0 then '天' else '小时' end as unit " +
				"from tbm_awayreg a " +
				"inner join tbm_timeitem b on a.pk_awaytype = b.pk_timeitem " +
				"inner join tbm_timeitemcopy c on a.pk_awaytypecopy = c.pk_timeitemcopy " +
				"where pk_psndoc =? " +
				"and pk_awayreg  not in (" +
				"select pk_awayreg from TBM_AWAYOFF where APPROVE_STATE  not in (0,1)" +
				") and a.awayenddate> ( " +
				"select nvl(MAX(enddate),'1971') from tbm_period where sealflag='Y' and pk_org = ? " +
				") and billsource = 0";
		SQLParameter param = new SQLParameter();
		param.addParam(pk_psndoc);
		param.addParam(pk_org);
		List<Map<String, Object>> data = (List<Map<String, Object>>) new BaseDAO()
		.executeQuery(sql.toString(), param, new MapListProcessor());
		if(data != null && data.size()>0){
			result.put("data", data);
		}else {
			result.put("data", null); 
		}
		return result;
	}
	
	public Map<String, Object> queryLeaveregByPk(String pk_leavereg)
			throws BusinessException {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> offdata = new HashMap<String, Object>();
		LeaveRegVO regVO = ((ILeaveRegisterQueryMaintain) NCLocator
				.getInstance().lookup(ILeaveRegisterQueryMaintain.class)).queryByPk(pk_leavereg);
		LeaveTypeCopyVO[] leaveCopyTypes = ((ITimeItemQueryService) NCLocator
				.getInstance().lookup(ITimeItemQueryService.class))
				.queryLeaveCopyTypesByOrg(regVO.getPk_org(),
						"pk_timeitemcopy = '" + regVO.getPk_leavetypecopy()
								+ "' ");
		String typename = "";
		LeaveTypeCopyVO typeVO = leaveCopyTypes[0];
		DecimalFormat dcmFmt = getDecimalFormat(regVO.getPk_org());
		
		offdata.put("pk_leaveoff", null);
		offdata.put("pk_leavereg", regVO.getPk_leavereg());
		offdata.put("pk_timeitemcopy", regVO.getPk_leavetypecopy());
		offdata.put("leaveremark", regVO.getLeaveremark());
		offdata.put("regleavehourcopy", new UFDouble(dcmFmt.format(regVO.getLeavehour())).toString());
		offdata.put("year", regVO.getLeaveyear());
		offdata.put("month", regVO.getLeavemonth());
		offdata.put("ts", regVO.getTs().toString());
		offdata.put("islactation", regVO.getIslactation().booleanValue());
		if(!"1002Z710000000021ZM3".equals(regVO.getPk_leavetype())){
			offdata.put("useful",
					new UFDouble(dcmFmt.format(regVO.getUsefuldayorhour()))
			.toString());
			offdata.put("freeze",
					new UFDouble(dcmFmt.format(regVO.getFreezedayorhour()))
			.toString());
		}else{
			offdata.put("lactationhour", regVO.getLactationhour().doubleValue());
		}
		offdata.put("requestid",regVO.getAttributeValue("requestid"));

		if (!ArrayUtils.isEmpty(leaveCopyTypes)) {
			typename = typeVO.getMultilangName();
		}
		offdata.put("leavetypename", typename);
		String unit =( 0 == typeVO.getTimeItemUnit() ? PublicLangRes.DAY() : PublicLangRes.HOUR());
		offdata.put("unit", unit);
		offdata.put("regbegintimecopy", regVO.getLeavebegintime().toString());
		offdata.put("regendtimecopy", regVO.getLeaveendtime().toString());
		offdata.put("regbegindatecopy", regVO.getLeavebegindate().toString());
		offdata.put("regenddatecopy", regVO.getLeaveenddate().toString());
		offdata.put("leavebegintime", regVO.getLeavebegintime().toString());
		offdata.put("leaveendtime", regVO.getLeaveendtime().toString());
		offdata.put("leavebegindate", regVO.getLeavebegindate().toString());
		offdata.put("leaveenddate", regVO.getLeaveenddate().toString());
		offdata.put("reallyleavehour",
				new UFDouble(dcmFmt.format(regVO.getLeavehour())).toString());
		String lactationtypeshow = null;
		offdata.put("differencehour", new UFDouble(dcmFmt.format(0)).toString());
		if(regVO.getIslactation().booleanValue()){
			String type = regVO.getLactationholidaytype().toString();
			lactationtypeshow = Integer.parseInt(type) == 0? "单一作息时间段" : Integer.parseInt(type) == 1 ? "上班时段" : Integer.parseInt(type) == 2 ? "下班时段":"任意时段";
		}
		offdata.put("lactationtypeshow", lactationtypeshow);
		offdata.put("lactationtype", regVO.getLactationholidaytype() == null ? null : regVO.getLactationholidaytype().toString());
		result.put("offdata", offdata);
		return result;
	}
	
	public Map<String, Object> queryAwayregByPk(String pk_awayreg)
			throws BusinessException {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> offdata = new HashMap<String, Object>();
		AwayRegVO regVO = ((IAwayRegisterQueryMaintain) NCLocator
				.getInstance().lookup(IAwayRegisterQueryMaintain.class)).queryByPk(pk_awayreg);
		AwayTypeCopyVO[] awayCopyTypes = ((ITimeItemQueryService) NCLocator
				.getInstance().lookup(ITimeItemQueryService.class))
				.queryAwayCopyTypesByOrg(regVO.getPk_org(),
						"pk_timeitemcopy = '" + regVO.getPk_awaytypecopy()
								+ "' ");
		String typename = "";
		AwayTypeCopyVO typeVO = awayCopyTypes[0];
		DecimalFormat dcmFmt = getDecimalFormat(regVO.getPk_org());
		
		offdata.put("pk_awayoff", null);
		offdata.put("pk_awayreg", regVO.getPk_awayreg());
		offdata.put("pk_timeitemcopy", regVO.getPk_awaytypecopy());
		offdata.put("awayaddress", regVO.getAwayaddress());
		offdata.put("workprocess", regVO.getWorkprocess());
		offdata.put("regawayhourcopy", new UFDouble(dcmFmt.format(regVO.getAwayhour())).toString());
		offdata.put("ts", regVO.getTs().toString());
		offdata.put("requestid",regVO.getAttributeValue("requestid"));

		if (!ArrayUtils.isEmpty(awayCopyTypes)) {
			typename = typeVO.getMultilangName();
		}
		offdata.put("awaytypename", typename);
		String unit =( 0 == typeVO.getTimeItemUnit() ? PublicLangRes.DAY() : PublicLangRes.HOUR());
		offdata.put("unit", unit);
		offdata.put("regbegintimecopy", regVO.getAwaybegintime().toString());
		offdata.put("regendtimecopy", regVO.getAwayendtime().toString());
		offdata.put("regbegindatecopy", regVO.getAwaybegindate().toString());
		offdata.put("regenddatecopy", regVO.getAwayenddate().toString());
		offdata.put("awaybegintime", regVO.getAwaybegintime().toString());
		offdata.put("awayendtime", regVO.getAwayendtime().toString());
		offdata.put("awaybegindate", regVO.getAwaybegindate().toString());
		offdata.put("awayenddate", regVO.getAwayenddate().toString());
		offdata.put("reallyawayhour",
				new UFDouble(dcmFmt.format(regVO.getAwayhour())).toString());
		offdata.put("differencehour", new UFDouble(dcmFmt.format(0)).toString());
		result.put("offdata", offdata);
		return result;
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
	 * 
	 * @param leaveoffMap
	 * @param userid
	 * @param needCheck
	 * @param aggVO
	 * @return
	 * @throws BusinessException
	 */
	public AggLeaveoffVO saveLeaveoff(String userid, boolean needCheck, AggLeaveoffVO aggVO) throws BusinessException {
		ILeaveOffManageMaintain leaveoffM = (ILeaveOffManageMaintain) NCLocator
				.getInstance().lookup(ILeaveOffManageMaintain.class);
		String pk_leaveoff = aggVO.getLeaveoffVO().getPk_leaveoff();
		AggLeaveoffVO newAggVO = null;
		if (StringUtils.isBlank(pk_leaveoff)) {
			String newBill_code = TBMHelper.getBillCode("6406",aggVO.getLeaveoffVO().getPk_group(),aggVO.getLeaveoffVO().getPk_org());
			aggVO.getLeaveoffVO().setBill_code(newBill_code);
			try{
				newAggVO = leaveoffM.insertData(aggVO);
			} catch(BusinessException e){
				TBMHelper.rollbackBillCode("6406",aggVO.getLeaveoffVO().getPk_group(),aggVO.getLeaveoffVO().getPk_org(),newBill_code);
				throw new BusinessException(e.getMessage());
			}
		} else {
			newAggVO = leaveoffM.updateData(aggVO);
		}
		return newAggVO;
	}
	
	/**
	 * 
	 * @param awayoffMap
	 * @param userid
	 * @param needCheck
	 * @param aggVO
	 * @return
	 * @throws BusinessException
	 */
	public AggAwayOffVO saveAwayoff(String userid, boolean needCheck, AggAwayOffVO aggVO) throws BusinessException {
		IAwayOffManageMaintain awayoffM = (IAwayOffManageMaintain) NCLocator
				.getInstance().lookup(IAwayOffManageMaintain.class);
		String pk_awayoff = aggVO.getAwayOffVO().getPk_awayoff();
		AggAwayOffVO newAggVO = null;
		if (StringUtils.isBlank(pk_awayoff)) {
			String newBill_code = TBMHelper.getBillCode("6407",aggVO.getAwayOffVO().getPk_group(),aggVO.getAwayOffVO().getPk_org());
			aggVO.getAwayOffVO().setBill_code(newBill_code);
			try{
				newAggVO = awayoffM.insertData(aggVO);
			} catch(BusinessException e){
				TBMHelper.rollbackBillCode("6407",aggVO.getAwayOffVO().getPk_group(),aggVO.getAwayOffVO().getPk_org(),newBill_code);
				throw new BusinessException(e.getMessage());
			}
		} else {
			newAggVO = awayoffM.updateData(aggVO);
		}
		return newAggVO;
	}
	
	public Map<String, Object> queryLeaveoffByPK(String pk_leaveoff) throws BusinessException {
		Map<String, Object> result = new HashMap<String, Object>();
		AggLeaveoffVO aggVO = ((ILeaveOffApplyQueryMaintain) NCLocator
				.getInstance().lookup(ILeaveOffApplyQueryMaintain.class)).queryByPk(pk_leaveoff);
		if (aggVO == null){
			return null;
		}
		LeaveoffVO leaveoffVO = aggVO.getLeaveoffVO();
		LeaveRegVO regVO = ((ILeaveRegisterQueryMaintain) NCLocator
				.getInstance().lookup(ILeaveRegisterQueryMaintain.class)).queryByPk(leaveoffVO.getPk_leavereg());

		LeaveTypeCopyVO[] leaveCopyTypes = ((ITimeItemQueryService) NCLocator
				.getInstance().lookup(ITimeItemQueryService.class))
				.queryLeaveCopyTypesByOrg(leaveoffVO.getPk_org(),
						"pk_timeitemcopy = '" + leaveoffVO.getPk_leavetypecopy()
								+ "' ");
		String typename = "";
		LeaveTypeCopyVO typeVO = leaveCopyTypes[0];

		DecimalFormat dcmFmt = getDecimalFormat(leaveoffVO.getPk_org());

		result.put("pk_leaveoff", leaveoffVO.getPk_leaveoff());
		result.put("pk_leavereg", leaveoffVO.getPk_leavereg());
		result.put("billcode", leaveoffVO.getBill_code());
		result.put("transtypeid", leaveoffVO.getTranstypeid());
		result.put("transtype", leaveoffVO.getTranstype());
		result.put("pk_timeitemcopy", leaveoffVO.getPk_leavetypecopy());
		result.put("leaveremark", regVO.getLeaveremark());
		result.put("billmaker", leaveoffVO.getBillmaker());
		result.put("reallyleavehour", new UFDouble(dcmFmt.format(leaveoffVO.getReallyleavehour())).toString());
		result.put("differencehour", new UFDouble(dcmFmt.format(leaveoffVO.getDifferencehour())).toString());
		result.put("year", regVO.getLeaveyear());
		result.put("month", regVO.getLeavemonth());
		result.put("ts", leaveoffVO.getTs().toString());
		result.put("creationtime", leaveoffVO.getCreationtime().toString());
		result.put("approve_state", leaveoffVO.getApprove_state().toString());
		result.put("islactation", leaveoffVO.getIslactation().booleanValue());
		if (StringUtils.isNotBlank(leaveoffVO.getTranstypeid())) {
			BilltypeVO billType = (BilltypeVO) new BaseDAO().retrieveByPK(
					BilltypeVO.class, leaveoffVO.getTranstypeid());
			result.put("transtypename",
					MultiLangHelper.getName(billType, "billtypename"));
		}
		String lactationtypeshow = null;
		if("1002Z710000000021ZM3".equals(leaveoffVO.getPk_leavetype())){
			result.put("lactationhour", regVO.getLactationhour().doubleValue());
			String type = regVO.getLactationholidaytype().toString();
			lactationtypeshow = Integer.parseInt(type) == 0? "单一作息时间段" : Integer.parseInt(type) == 1 ? "上班时段" : Integer.parseInt(type) == 2 ? "下班时段":"任意时段";
		}
		result.put("lactationtypeshow", lactationtypeshow);
		result.put("lactationtype", regVO.getLactationholidaytype() == null ? null : regVO.getLactationholidaytype().toString());
		result.put("length",
				new UFDouble(dcmFmt.format(leaveoffVO.getLength())).toString());
		result.put("requestid",leaveoffVO.getAttributeValue("requestid"));

		if (!ArrayUtils.isEmpty(leaveCopyTypes)) {
			typename = typeVO.getMultilangName();
		}
		result.put("leavetypename", typename);
		String unit =( 0 == typeVO.getTimeItemUnit() ? PublicLangRes.DAY() : PublicLangRes.HOUR());
		result.put("unit", unit);

		ArrayList<Map<String,Object>> workFlowNote = new TBMAwayDao().queryWorkFlowNote(leaveoffVO.getTranstype(),leaveoffVO.getBill_code(),leaveoffVO.getPk_leaveoff());
		if(workFlowNote!=null && workFlowNote.size()>0){
			result.put("workflownote", workFlowNote);
		}
		result.put("leavebegintime", leaveoffVO.getLeavebegintime().toString());
		result.put("leavebegindate", leaveoffVO.getLeavebegindate().toString());
		result.put("leaveendtime", leaveoffVO.getLeaveendtime().toString());
		result.put("leaveenddate", leaveoffVO.getLeaveenddate().toString());
		result.put("regbegintimecopy", leaveoffVO.getRegbegintimecopy().toString());
		result.put("regbegindatecopy", leaveoffVO.getRegbegindatecopy().toString());
		result.put("regendtimecopy", leaveoffVO.getRegendtimecopy().toString());
		result.put("regenddatecopy", leaveoffVO.getRegenddatecopy().toString());
		
		result.put("regleavehourcopy", new UFDouble(dcmFmt.format(leaveoffVO.getRegleavehourcopy())).toString());
		//add by wt 20190821
//		if (StringUtils.isNotBlank(leaveoffVO.getPk_project())) {
//			ProjectHeadVO projectVO = (ProjectHeadVO) new BaseDAO().retrieveByPK(
//					ProjectHeadVO.class, leaveoffVO.getPk_project());
//			result.put("project_name",MultiLangHelper.getName(projectVO, "project_name"));
//		}
		return result;
	}
	
	public Map<String, Object> queryAwayoffByPK(String pk_awayoff) throws BusinessException {
		Map<String, Object> result = new HashMap<String, Object>();
		AggAwayOffVO aggVO = ((IAwayOffApplyQueryMaintain) NCLocator
				.getInstance().lookup(IAwayOffApplyQueryMaintain.class)).queryByPk(pk_awayoff);
		if (aggVO == null){
			return null;
		}
		AwayOffVO awayoffVO = aggVO.getAwayOffVO();
		AwayRegVO regVO = ((IAwayRegisterQueryMaintain) NCLocator
				.getInstance().lookup(IAwayRegisterQueryMaintain.class)).queryByPk(awayoffVO.getPk_awayreg());
		
		AwayTypeCopyVO[] awayCopyTypes = ((ITimeItemQueryService) NCLocator
				.getInstance().lookup(ITimeItemQueryService.class))
				.queryAwayCopyTypesByOrg(awayoffVO.getPk_org(),
						"pk_timeitemcopy = '" + awayoffVO.getPk_awaytypecopy()
								+ "' ");
		String typename = "";
		AwayTypeCopyVO typeVO = awayCopyTypes[0];

		DecimalFormat dcmFmt = getDecimalFormat(awayoffVO.getPk_org());

		result.put("pk_awayoff", awayoffVO.getPk_awayoff());
		result.put("pk_awayreg", awayoffVO.getPk_awayreg());
		result.put("billcode", awayoffVO.getBill_code());
		result.put("transtypeid", awayoffVO.getTranstypeid());
		result.put("transtype", awayoffVO.getTranstype());
		result.put("pk_timeitemcopy", awayoffVO.getPk_awaytypecopy());
		result.put("awayaddress", regVO.getAwayaddress());
		result.put("workprocess", regVO.getWorkprocess());
		result.put("billmaker", awayoffVO.getBillmaker());
		result.put("reallyawayhour", new UFDouble(dcmFmt.format(awayoffVO.getReallyawayhour())).toString());
		result.put("ts", awayoffVO.getTs().toString());
		result.put("creationtime", awayoffVO.getCreationtime().toString());
		result.put("approve_state", awayoffVO.getApprove_state().toString());
		if (StringUtils.isNotBlank(awayoffVO.getTranstypeid())) {
			BilltypeVO billType = (BilltypeVO) new BaseDAO().retrieveByPK(
					BilltypeVO.class, awayoffVO.getTranstypeid());
			result.put("transtypename",
					MultiLangHelper.getName(billType, "billtypename"));
		}
		result.put("requestid",awayoffVO.getAttributeValue("requestid"));

		if (awayoffVO != null) {
			typename = typeVO.getMultilangName();
		}
		result.put("awaytypename", typename);
		String unit =( 0 == typeVO.getTimeItemUnit() ? PublicLangRes.DAY() : PublicLangRes.HOUR());
		result.put("unit", unit);

		ArrayList<Map<String,Object>> workFlowNote = new TBMAwayDao().queryWorkFlowNote(awayoffVO.getTranstype(),awayoffVO.getBill_code(),awayoffVO.getPk_awayoff());
		if(workFlowNote!=null && workFlowNote.size()>0){
			result.put("workflownote", workFlowNote);
		}
		result.put("awaybegintime", awayoffVO.getAwaybegintime().toString());
		result.put("awaybegindate", awayoffVO.getAwaybegindate().toString());
		result.put("awayendtime", awayoffVO.getAwayendtime().toString());
		result.put("awayenddate", awayoffVO.getAwayenddate().toString());
		result.put("regbegintimecopy", awayoffVO.getRegbegintimecopy().toString());
		result.put("regbegindatecopy", awayoffVO.getRegbegindatecopy().toString());
		result.put("regendtimecopy", awayoffVO.getRegendtimecopy().toString());
		result.put("regenddatecopy", awayoffVO.getRegenddatecopy().toString());
		result.put("differencehour", new UFDouble(dcmFmt.format(awayoffVO.getDifferencehour())).toString());
		
		result.put("regawayhourcopy", new UFDouble(dcmFmt.format(awayoffVO.getRegawayhourcopy())).toString());
		//add by wt 20190821
//		if (StringUtils.isNotBlank(awayoffVO.getPk_project())) {
//			ProjectHeadVO projectVO = (ProjectHeadVO) new BaseDAO().retrieveByPK(
//					ProjectHeadVO.class, awayoffVO.getPk_project());
//			result.put("project_name",MultiLangHelper.getName(projectVO, "project_name"));
//		}
		return result;
	}
	
	public Map<String, Object> submitLeaveoff(String userid, AggLeaveoffVO newVO)
			throws BusinessException {
		HashMap<String, String> eParam = new HashMap<String, String>();
		LeaveoffVO offvo = newVO.getLeaveoffVO();
		String pk_org = offvo.getPk_org();
		if (isDirectApprove(pk_org, "6406")) {
			String regsql = "update tbm_leavereg set isleaveoff='Y' where pk_leavereg='" + offvo.getPk_leavereg() + "'";
			String sql = "update tbm_leaveoff set approve_state = "+IPfRetCheckInfo.PASSING+" where pk_leaveoff='" + offvo.getPk_leaveoff()+"'";
			new BaseDAO().executeUpdate(sql);
			new BaseDAO().executeUpdate(regsql);
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("flag", "2");
			return result;
		}
 		PfProcessBatchRetObject validateRetObj = ((IHrPf) NCLocator.getInstance().lookup(IHrPf.class)).submitValidation("Commit","Commit",null,SysInitQuery.getParaInt(pk_org,(String) IHrPf.hashBillTypePara.get("6406")).intValue(),new AggregatedValueObject[] { newVO });
		if ((validateRetObj.getRetObj() == null)
				|| (validateRetObj.getRetObj().length == 0)) {
			String errStr = validateRetObj.getExceptionMsg();
			if (StringUtils.isNotBlank(errStr))
				throw new BusinessException(errStr);
		}
		LfwPfUtil.runAction("SAVE", "6406", newVO, null, null, null, eParam,
				new String[] { userid }, null);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("flag", "2");
		return result;
	}
	
	public Map<String, Object> rollBackLeaveoff(String userid, AggLeaveoffVO newVO)
			throws BusinessException {
		LeaveoffVO offvo = newVO.getLeaveoffVO();
		String pk_org = offvo.getPk_org();
		HashMap<String, String> eParam = new HashMap<String, String>();
		if (isDirectApprove(pk_org, "6406")) {
			eParam.put("nosendmessage", "nosendmessage");
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
		LfwPfUtil.runAction("SAVE", "6406", newVO, null, null, null, eParam,
				new String[] { userid }, null);

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("flag", "2");
		return result;
	}
	
	public Map<String, Object> submitAwayoff(String userid, AggAwayOffVO newVO)
			throws BusinessException {
		HashMap<String, String> eParam = new HashMap<String, String>();
		AwayOffVO offvo = newVO.getAwayOffVO();
		String pk_org = offvo.getPk_org();
		if (isDirectApprove(pk_org, "6407")) {
			String regsql = "update tbm_awayreg set isawayoff='Y' where pk_awayreg='" + offvo.getPk_awayreg() + "'";
			String sql = "update tbm_awayoff set approve_state = "+IPfRetCheckInfo.PASSING+" where pk_awayoff='" + offvo.getPk_awayoff()+"'";
			new BaseDAO().executeUpdate(sql);
			new BaseDAO().executeUpdate(regsql);
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("flag", "2");
			return result;
		}
 		PfProcessBatchRetObject validateRetObj = ((IHrPf) NCLocator.getInstance().lookup(IHrPf.class)).submitValidation("Commit","Commit",null,SysInitQuery.getParaInt(pk_org,(String) IHrPf.hashBillTypePara.get("6407")).intValue(),new AggregatedValueObject[] { newVO });
		if ((validateRetObj.getRetObj() == null)
				|| (validateRetObj.getRetObj().length == 0)) {
			String errStr = validateRetObj.getExceptionMsg();
			if (StringUtils.isNotBlank(errStr))
				throw new BusinessException(errStr);
		}
		LfwPfUtil.runAction("SAVE", "6407", newVO, null, null, null, eParam,
				new String[] { userid }, null);

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("flag", "2");
		return result;
	}
	
	public Map<String, Object> rollBackAwayoff(String userid, AggAwayOffVO newVO) throws BusinessException {

		AwayOffVO offvo = newVO.getAwayOffVO();
		String pk_org = offvo.getPk_org();
		HashMap<String, String> eParam = new HashMap<String, String>();
		if (isDirectApprove(pk_org, "6407")) {
			eParam.put("nosendmessage", "nosendmessage");
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
		LfwPfUtil.runAction("RECALL", "6407", newVO, null, null, null, eParam,
				new String[] { userid }, null);

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("flag", "2");
		return result;
	
	}
	
	public boolean isDirectApprove(String pk_org, String billtype)
			throws BusinessException {
//		Integer type = SysInitQuery.getParaInt(pk_org,
//				(String) IHrPf.hashBillTypePara.get(billtype));
		String sql = "select value from pub_sysinit where pk_org='" + pk_org+"' and initcode ='" + (String) IHrPf.hashBillTypePara.get(billtype) +"'";
		String types = (String) new BaseDAO().executeQuery(sql, new ColumnProcessor());
		Integer type =Integer.valueOf(types);
		return (type != null) && (type.intValue() == 0);
	}
	
	/**
	 * 销假打印信息查询
	 * @param pk_leaveh
	 * @return
	 */
	public Map<String, Object> leaveoffPrintTemplate(String pk_leaveoff)throws BusinessException{
		Map<String, Object> map = new HashMap<>();
		StringBuilder builder = new StringBuilder();
		builder.append("select a.bill_code as billcode, b.name as orgname, d.name as deptname, e.timeitemname as type, a.approve_state as status, ");
		builder.append("f.user_name as applyer, a.apply_date as applydate, a.approve_time as approvedate, g.postname as postname ");
		builder.append("from tbm_leaveoff a ");
		builder.append("inner join org_hrorg b on a.pk_org = b.pk_hrorg ");
		builder.append("inner join hi_psnjob c on c.pk_psnjob = a.pk_psnjob ");
		builder.append("inner join org_dept d on d.pk_dept = c.pk_dept ");
		builder.append("inner join tbm_timeitem e on e. pk_timeitem = a.pk_leavetype ");
		builder.append("inner join sm_user f on f.cuserid = a.billmaker ");
		builder.append("inner join om_post g on g.pk_post = c.pk_post ");
		builder.append("where a.pk_leaveoff = '" + pk_leaveoff + "' ");
		HashMap<String, Object> dataMap = (HashMap<String, Object>)new BaseDAO().executeQuery(builder.toString(), new MapProcessor());
		
		StringBuilder sb = new StringBuilder();
		sb.append("select a.leavebegintime as leavebegintime, a.leaveendtime as leaveendtime, a.reallyleavehour as reallyleavehour, b.leaveremark as leaveremark," +
				"a.regbegintimecopy as regbegintimecopy, a.regendtimecopy as regendtimecopy, a.regleavehourcopy as regleavehourcopy, a.differencehour as differencehour ");
		sb.append("from tbm_leaveoff a ");
		sb.append("inner join tbm_leavereg b on b.pk_leavereg = a.pk_leavereg ");
		sb.append("where a.pk_leaveoff = '" + pk_leaveoff + "'");
		sb.append("order by a.leavebegintime asc ");
		List list = (List) new BaseDAO().executeQuery(sb.toString(),
				new MapListProcessor());
		
		StringBuilder sba = new StringBuilder();
		sba.append("select b.user_name as sender, a.senddate as senddate, c.user_name as approver, a.dealdate as approdate, a.checknote as approveidea ");
		sba.append("from pub_workflownote a ");
		sba.append("inner join sm_user b on b.cuserid = a.senderman ");
		sba.append("inner join sm_user c on c.cuserid = a.checkman ");
		sba.append("where  billid = '" + pk_leaveoff + "' and actiontype <> 'BIZ' ");
		sba.append("order by a.senddate asc ");
		List notelist = (List) new BaseDAO().executeQuery(sba.toString(),
				new MapListProcessor());
		map.put("data", dataMap);
		map.put("tableData", list);
		map.put("approveData", notelist);
		return map;
	}
	
	/**
	 * 销差打印信息查询
	 * @param pk_leaveh
	 * @return
	 */
	public Map<String, Object> awayoffPrintTemplate(String pk_awayoff)throws BusinessException{
		Map<String, Object> map = new HashMap<>();
		StringBuilder builder = new StringBuilder();
		builder.append("select a.bill_code as billcode, b.name as orgname, d.name as deptname, e.timeitemname as type, a.approve_state as status, ");
		builder.append("f.user_name as applyer, a.apply_date as applydate, a.approve_time as approvedate, g.postname as postname ");
		builder.append("from tbm_awayoff a ");
		builder.append("inner join org_hrorg b on a.pk_org = b.pk_hrorg ");
		builder.append("inner join hi_psnjob c on c.pk_psnjob = a.pk_psnjob ");
		builder.append("inner join org_dept d on d.pk_dept = c.pk_dept ");
		builder.append("inner join tbm_timeitem e on e. pk_timeitem = a.pk_awaytype ");
		builder.append("inner join sm_user f on f.cuserid = a.billmaker ");
		builder.append("inner join om_post g on g.pk_post = c.pk_post ");
		builder.append("where a.pk_awayoff = '" + pk_awayoff + "' ");
		HashMap<String, Object> dataMap = (HashMap<String, Object>)new BaseDAO().executeQuery(builder.toString(), new MapProcessor());
		
		StringBuilder sb = new StringBuilder();
		sb.append("select a.awaybegintime as awaybegintime, a.awayendtime as awayendtime, a.reallyawayhour as reallyawayhour, b.awayaddress as awayaddress, b.workprocess as workprocess, " +
				"a.regbegintimecopy as regbegintimecopy, a.regendtimecopy as regendtimecopy, a.regawayhourcopy as regawayhourcopy, a.differencehour as differencehour ");
		sb.append("from tbm_awayoff a ");
		sb.append("inner join tbm_awayreg b on b.pk_awayreg = a.pk_awayreg ");
		sb.append("where a.pk_awayoff = '" + pk_awayoff + "'");
		sb.append("order by a.awaybegintime asc ");
		List list = (List) new BaseDAO().executeQuery(sb.toString(),
				new MapListProcessor());
		
		StringBuilder sba = new StringBuilder();
		sba.append("select b.user_name as sender, a.senddate as senddate, c.user_name as approver, a.dealdate as approdate, a.checknote as approveidea ");
		sba.append("from pub_workflownote a ");
		sba.append("inner join sm_user b on b.cuserid = a.senderman ");
		sba.append("inner join sm_user c on c.cuserid = a.checkman ");
		sba.append("where  billid = '" + pk_awayoff + "' and actiontype <> 'BIZ' ");
		sba.append("order by a.senddate asc ");
		List notelist = (List) new BaseDAO().executeQuery(sba.toString(),
				new MapListProcessor());
		map.put("data", dataMap);
		map.put("tableData", list);
		map.put("approveData", notelist);
		return map;
	}
	
}
