package nc.impl.saas.hi;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.NCLocator;
import nc.hr.utils.ResHelper;
import nc.itf.om.IDeptQueryService;
import nc.itf.om.IOrgInfoQueryService;
import nc.itf.saas.ITBMOffService;
import nc.itf.saas.pub.PageResult;
import nc.itf.saas.pub.SaasMapUtils;
import nc.itf.ta.IAwayOffApplyQueryMaintain;
import nc.itf.ta.IAwayOffManageMaintain;
import nc.itf.ta.ILeaveApplyApproveManageMaintain;
import nc.itf.ta.ILeaveApplyQueryMaintain;
import nc.itf.ta.ILeaveOffApplyQueryMaintain;
import nc.itf.ta.ILeaveOffManageMaintain;
import nc.itf.ta.ILeaveRegisterInfoDisplayer;
import nc.itf.ta.ITBMPsndocQueryMaintain;
import nc.itf.ta.ITimeItemQueryService;
import nc.itf.ta.ITimeRuleQueryService;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.pubitf.rbac.IUserPubService;
import nc.vo.hi.psndoc.PsnJobVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.ta.PublicLangRes;
import nc.vo.ta.awayoff.AggAwayOffVO;
import nc.vo.ta.awayoff.AwayOffVO;
import nc.vo.ta.leave.AggLeaveVO;
import nc.vo.ta.leave.LeaveRegVO;
import nc.vo.ta.leave.LeavebVO;
import nc.vo.ta.leave.LeavehVO;
import nc.vo.ta.leaveoff.AggLeaveoffVO;
import nc.vo.ta.leaveoff.LeaveoffVO;
import nc.vo.ta.psndoc.TBMPsndocVO;
import nc.vo.ta.timeitem.AwayTypeCopyVO;
import nc.vo.ta.timeitem.LeaveTypeCopyVO;
import nc.vo.ta.timerule.TimeRuleVO;

public class TBMOffServiceImpl implements ITBMOffService {

	@Override
	public String queryLeavereg4off(Map<String, Object> param)
			throws BusinessException {
		String userId = param.get("userId").toString();
		String pk_psndoc = NCLocator.getInstance().lookup(IUserPubService.class).queryPsndocByUserid(userId);
		if (StringUtils.isBlank(pk_psndoc))
			throw new BusinessException(ResHelper.getString("6017mobile","06017mytime000001"));
		UFLiteralDate apply_date = new UFLiteralDate();
		TBMPsndocVO latestVO = NCLocator.getInstance().lookup(ITBMPsndocQueryMaintain.class).queryByPsndocAndDate(pk_psndoc, apply_date);
		if (latestVO == null) {
			throw new BusinessException(ResHelper.getString("6017mobile","06017mytime000002"));
		}
		String pk_org = latestVO.getPk_org();
		Map<String, Object> data = new TBMOffDao().queryLeavereg4off(pk_psndoc, pk_org);
		PageResult result = new PageResult();
		result.setData(data);
		return result.toJson();
	}

	@Override
	public String queryLeaveregByPk(Map<String, Object> param)
			throws BusinessException {
		String pk_leavereg = param.get("pk").toString();
		Map<String, Object> leavereg = new TBMOffDao()
				.queryLeaveregByPk(pk_leavereg);
		PageResult result = new PageResult();
		result.setData(leavereg);
		result.pushDevInfo("param", param);
		return result.toJson();
	}

	@Override
	public String calculateLeaveoffLength(Map<String, Object> param)
			throws BusinessException {
		Map<String, Object> leaveoffMap = (Map<String, Object>) param.get("leaveoffMap");
		String userid = param.get("userId").toString();

		AggLeaveoffVO aggVO = getLeaveoffFromMap(leaveoffMap, userid, null);
		ILeaveOffManageMaintain leaveoffM = NCLocator
				.getInstance().lookup(ILeaveOffManageMaintain.class);
		aggVO = leaveoffM.calculate(aggVO);
		LeaveoffVO offvo = aggVO.getLeaveoffVO();
		TimeRuleVO timeRulevo = NCLocator
				.getInstance().lookup(ITimeRuleQueryService.class)
				.queryByOrg(offvo.getPk_org());
		DecimalFormat dcmFmt = TBMHelper.getDecimalFormat(timeRulevo
				.getTimedecimal());

		LeaveTypeCopyVO[] leaveCopyTypes = NCLocator
				.getInstance().lookup(ITimeItemQueryService.class)
				.queryLeaveCopyTypesByOrg(offvo.getPk_org(),
						"pk_timeitemcopy = '" + offvo.getPk_leavetypecopy()
								+ "' ");
		LeaveTypeCopyVO typeVO = leaveCopyTypes[0];
		String unit = (0 == typeVO.getTimeItemUnit() ? PublicLangRes.DAY()
				: PublicLangRes.HOUR());
		leaveoffMap.put("reallyleavehour",new UFDouble(dcmFmt.format(offvo.getReallyleavehour())).toString());
		leaveoffMap.put("unit", unit);
		leaveoffMap.put("differencehour", new UFDouble(dcmFmt.format(offvo.getDifferencehour())).toString());
		PageResult pageResult = new PageResult();
		pageResult.setData(leaveoffMap);
		return pageResult.toJson();
	}


	@Override
	public String saveLeaveoff(Map<String, Object> param)
			throws BusinessException {
		Map<String, Object> leaveoffMap = (Map<String, Object>) param
				.get("leaveoffMap");
		String userId = param.get("userId").toString();
		AggLeaveoffVO aggVO = getLeaveoffFromMap(leaveoffMap, userId, null);
		AggLeaveoffVO newLeave = new TBMOffDao().saveLeaveoff(userId, false, aggVO);
		LeaveoffVO parentVO = (LeaveoffVO) newLeave.getParentVO();
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("pk_leaveoff", parentVO.getPk_leaveoff());
		hashMap.put("bill_code", parentVO.getBill_code());
		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(hashMap);
		return result.toJson();
	}
	
	private AggLeaveoffVO getLeaveoffFromMap(Map<String, Object> leaveoffMap,
			String userid, LeaveTypeCopyVO typeVO) throws BusinessException {
		if (MapUtils.isEmpty(leaveoffMap))
			return null;
		String pk_psndoc = NCLocator.getInstance().lookup(IUserPubService.class).queryPsndocByUserid(userid);
		if (StringUtils.isBlank(pk_psndoc))
			throw new BusinessException(ResHelper.getString("6017mobile","06017mytime000001"));
		AggLeaveoffVO aggVO = new AggLeaveoffVO();
		String pk_leaveoff = null;
		if (leaveoffMap.get("pk_leaveoff") != null && !"".equals(leaveoffMap.get("pk_leaveoff"))) {
			pk_leaveoff = leaveoffMap.get("pk_leaveoff").toString();
		}
		AggLeaveoffVO oldAggVO = null;
		LeaveoffVO leaveoffvo = null;
		UFLiteralDate apply_date = new UFLiteralDate();
		if (pk_leaveoff != null) {
			oldAggVO = NCLocator.getInstance()
					.lookup(ILeaveOffApplyQueryMaintain.class).queryByPk(pk_leaveoff);
			leaveoffvo = oldAggVO.getLeaveoffVO();
			leaveoffvo.setTs(leaveoffMap.get("ts") == null ? new UFDateTime(): new UFDateTime((String) leaveoffMap.get("ts")));
			apply_date = leaveoffvo.getApply_date();
			leaveoffvo.setModifiedtime(new UFDateTime());
			leaveoffvo.setModifier(userid);
		} else {
			leaveoffvo = new LeaveoffVO();
			UFLiteralDate newdate = new UFLiteralDate();
			leaveoffvo.setApply_date(newdate);
			leaveoffvo.setApprove_state(Integer.valueOf(-1));
			leaveoffvo.setTs(leaveoffMap.get("ts") == null ? new UFDateTime()
					: new UFDateTime((String) leaveoffMap.get("ts")));
			leaveoffvo.setCreationtime(leaveoffMap.get("creationtime") == null ? new UFDateTime()
					: new UFDateTime((String) leaveoffMap.get("creationtime")));
			leaveoffvo.setBillmaker(userid);
			leaveoffvo.setCreator(userid);
		}
		TBMPsndocVO latestVO = NCLocator.getInstance().lookup(ITBMPsndocQueryMaintain.class).queryByPsndocAndDate(pk_psndoc, apply_date);
		if (latestVO == null) {
			throw new BusinessException(ResHelper.getString("6017mobile","06017mytime000002"));
		}
		String pk_timeitemcopy = (String) leaveoffMap.get("pk_timeitemcopy");
		String pk_org = latestVO.getPk_org();
		String pk_group = latestVO.getPk_group();
		if (typeVO == null) {
			LeaveTypeCopyVO[] leaveCopyTypes = NCLocator
					.getInstance().lookup(ITimeItemQueryService.class)
					.queryLeaveCopyTypesByOrg(pk_org, "pk_timeitemcopy = '"+ pk_timeitemcopy + "' ");

			if (ArrayUtils.isEmpty(leaveCopyTypes)) {
				throw new BusinessException(ResHelper.getString("6017mobile","06017mytime000019"));
			}
			typeVO = leaveCopyTypes[0];
		}
		aggVO.setParentVO(leaveoffvo);
//		leaveoffvo.setLeaveremark((String)leaveoffMap.get("leaveremark"));//数据库表中无此字段
		leaveoffvo.setPk_org(pk_org);
		leaveoffvo.setPk_group(pk_group);
		String pk_psnorg = latestVO.getPk_psnorg();
		leaveoffvo.setPk_psnorg(pk_psnorg);
		leaveoffvo.setPk_psndoc(pk_psndoc);
		String pk_psnjob = latestVO.getPk_psnjob();
		leaveoffvo.setPk_psnjob(pk_psnjob);
		String pk_timeitem = typeVO.getPk_timeitem();
		leaveoffvo.setPk_leavetype(pk_timeitem);
		leaveoffvo.setPk_leavetypecopy(pk_timeitemcopy);
		leaveoffvo.setTranstypeid(leaveoffMap.get("transtypeid") == null ? "" : leaveoffMap.get("transtypeid").toString());
		if(leaveoffMap.get("transtypeid") != null){
			String transtypesql = "select pk_billtypecode from bd_billtype where pk_billtypeid = '" + leaveoffMap.get("transtypeid").toString() + "'";
			leaveoffvo.setTranstype((String)(new BaseDAO().executeQuery(transtypesql, new ColumnProcessor())));
		}
		boolean isLactation = "1002Z710000000021ZM3".equals(typeVO.getPk_timeitem());
		leaveoffvo.setIslactation(UFBoolean.valueOf(isLactation));
//		if(isLactation){//数据库表中无此字段
//			leaveoffvo.setLactationhour(new UFDouble(leaveoffMap.get("lactationhour").toString()));
//			leaveoffvo.setLactationholidaytype(Integer.parseInt((String)leaveoffMap.get("lactationtype")));
//		}
		leaveoffvo.setRegbegindatecopy(SaasMapUtils.getDateValue(leaveoffMap, "regbegindatecopy"));
		leaveoffvo.setRegbegintimecopy(SaasMapUtils.getDateTimeValue(leaveoffMap, "regbegintimecopy"));
		leaveoffvo.setRegenddatecopy(SaasMapUtils.getDateValue(leaveoffMap, "regenddatecopy"));
		leaveoffvo.setRegendtimecopy(SaasMapUtils.getDateTimeValue(leaveoffMap, "regendtimecopy"));
		leaveoffvo.setRegleavehourcopy(new UFDouble((String)leaveoffMap.get("regleavehourcopy")));
		leaveoffvo.setLeavebegintime(SaasMapUtils.getDateTimeValue(leaveoffMap, "leavebegintime"));
		leaveoffvo.setLeavebegindate(SaasMapUtils.getDateValue(leaveoffMap, "leavebegintime"));
		leaveoffvo.setLeaveendtime(SaasMapUtils.getDateTimeValue(leaveoffMap, "leaveendtime"));
		leaveoffvo.setLeaveenddate(SaasMapUtils.getDateValue(leaveoffMap, "leaveendtime"));
		leaveoffvo.setPk_leavereg((String)leaveoffMap.get("pk_leavereg"));
		leaveoffvo.setReallyleavehour(new UFDouble((String)leaveoffMap.get("reallyleavehour")));
		leaveoffvo.setDifferencehour(new UFDouble((String)leaveoffMap.get("differencehour")));
		leaveoffvo.setPk_billtype("6406");
		PsnJobVO jobVO = (PsnJobVO) new BaseDAO().retrieveByPK(PsnJobVO.class, leaveoffvo.getPk_psnjob());
		String pk_org_v = NCLocator.getInstance().lookup(IOrgInfoQueryService.class).getOrgVid(jobVO.getPk_org(), new UFDate());
		leaveoffvo.setPk_org_v(pk_org_v);
		String pk_dept_v = NCLocator.getInstance().lookup(
				IDeptQueryService.class).getDeptVid(jobVO.getPk_dept(),new UFDate());
		leaveoffvo.setPk_dept_v(pk_dept_v);
		String creatordept = new TBMQueryDao().getDeptnameByPsndocAndDateTime(pk_psndoc,new UFDateTime());
		leaveoffvo.setAttributeValue("creatordept", creatordept);
		//add by wt 20190821 begin 
//		leaveoffvo.setPk_project(leaveoffMap.get("pk_project")==null ? "":leaveoffMap.get("pk_project").toString());
		return aggVO;
	}
	
	private AggAwayOffVO getAwayoffFromMap(Map<String, Object> awayoffMap,
			String userid, AwayTypeCopyVO typeVO) throws BusinessException {
		if (MapUtils.isEmpty(awayoffMap))
			return null;
		String pk_psndoc = NCLocator.getInstance().lookup(IUserPubService.class).queryPsndocByUserid(userid);
		if (StringUtils.isBlank(pk_psndoc))
			throw new BusinessException(ResHelper.getString("6017mobile","06017mytime000001"));
		AggAwayOffVO aggVO = new AggAwayOffVO();
		String pk_awayoff = null;
		if (awayoffMap.get("pk_awayoff") != null && !"".equals(awayoffMap.get("pk_awayoff"))) {
			pk_awayoff = awayoffMap.get("pk_awayoff").toString();
		}
		AggAwayOffVO oldAggVO = null;
		AwayOffVO awayoffvo = null;
		UFLiteralDate apply_date = new UFLiteralDate();
		if (pk_awayoff != null) {
			oldAggVO = NCLocator.getInstance()
					.lookup(IAwayOffApplyQueryMaintain.class).queryByPk(pk_awayoff);
			awayoffvo = oldAggVO.getAwayOffVO();
			awayoffvo.setTs(awayoffMap.get("ts") == null ? new UFDateTime(): new UFDateTime((String) awayoffMap.get("ts")));
			apply_date = awayoffvo.getApply_date();
			awayoffvo.setModifiedtime(new UFDateTime());
			awayoffvo.setModifier(userid);
		} else {
			awayoffvo = new AwayOffVO();
			UFLiteralDate newdate = new UFLiteralDate();
			awayoffvo.setApply_date(newdate);
			awayoffvo.setApprove_state(Integer.valueOf(-1));
			awayoffvo.setTs(awayoffMap.get("ts") == null ? new UFDateTime()
					: new UFDateTime((String) awayoffMap.get("ts")));
			awayoffvo.setCreationtime(awayoffMap.get("creationtime") == null ? new UFDateTime()
					: new UFDateTime((String) awayoffMap.get("creationtime")));
			awayoffvo.setBillmaker(userid);
			awayoffvo.setCreator(userid);
		}
		TBMPsndocVO latestVO = NCLocator.getInstance().lookup(ITBMPsndocQueryMaintain.class).queryByPsndocAndDate(pk_psndoc, apply_date);
		if (latestVO == null) {
			throw new BusinessException(ResHelper.getString("6017mobile","06017mytime000002"));
		}
		String pk_timeitemcopy = (String) awayoffMap.get("pk_timeitemcopy");
		String pk_org = latestVO.getPk_org();
		String pk_group = latestVO.getPk_group();
		if (typeVO == null) {
			AwayTypeCopyVO[] awayCopyTypes = NCLocator
					.getInstance().lookup(ITimeItemQueryService.class)
					.queryAwayCopyTypesByOrg(pk_org, "pk_timeitemcopy = '"+ pk_timeitemcopy + "' ");

			if (ArrayUtils.isEmpty(awayCopyTypes)) {
				throw new BusinessException(ResHelper.getString("6017mobile","06017mytime000019"));
			}
			typeVO = awayCopyTypes[0];
		}
		aggVO.setParentVO(awayoffvo);
		awayoffvo.setPk_org(pk_org);
		awayoffvo.setPk_group(pk_group);
		String pk_psnorg = latestVO.getPk_psnorg();
		awayoffvo.setPk_psnorg(pk_psnorg);
		awayoffvo.setPk_psndoc(pk_psndoc);
		String pk_psnjob = latestVO.getPk_psnjob();
		awayoffvo.setPk_psnjob(pk_psnjob);
		String pk_timeitem = typeVO.getPk_timeitem();
		awayoffvo.setPk_awaytype(pk_timeitem);
		awayoffvo.setPk_awaytypecopy(pk_timeitemcopy);
		awayoffvo.setTranstypeid(awayoffMap.get("transtypeid") == null ? "" : awayoffMap.get("transtypeid").toString());
		if(awayoffMap.get("transtypeid") != null){
			String transtypesql = "select pk_billtypecode from bd_billtype where pk_billtypeid = '" + awayoffMap.get("transtypeid").toString() + "'";
			awayoffvo.setTranstype((String)(new BaseDAO().executeQuery(transtypesql, new ColumnProcessor())));
		}
		awayoffvo.setRegbegindatecopy(SaasMapUtils.getDateValue(awayoffMap, "regbegintimecopy"));
		awayoffvo.setRegbegintimecopy(SaasMapUtils.getDateTimeValue(awayoffMap, "regbegintimecopy"));
		awayoffvo.setRegenddatecopy(SaasMapUtils.getDateValue(awayoffMap, "regendtimecopy"));
		awayoffvo.setRegendtimecopy(SaasMapUtils.getDateTimeValue(awayoffMap, "regendtimecopy"));
		awayoffvo.setRegawayhourcopy(new UFDouble((String)awayoffMap.get("regawayhourcopy")));
		awayoffvo.setAwaybegintime(SaasMapUtils.getDateTimeValue(awayoffMap, "awaybegintime"));
		awayoffvo.setAwaybegindate(SaasMapUtils.getDateValue(awayoffMap, "awaybegintime"));
		awayoffvo.setAwayendtime(SaasMapUtils.getDateTimeValue(awayoffMap, "awayendtime"));
		awayoffvo.setAwayenddate(SaasMapUtils.getDateValue(awayoffMap, "awayendtime"));
		awayoffvo.setPk_awayreg((String)awayoffMap.get("pk_awayreg"));
		awayoffvo.setReallyawayhour(new UFDouble((String)awayoffMap.get("reallyawayhour")));
		awayoffvo.setDifferencehour(new UFDouble((String)awayoffMap.get("differencehour")));
		awayoffvo.setPk_billtype("6407");
		PsnJobVO jobVO = (PsnJobVO) new BaseDAO().retrieveByPK(PsnJobVO.class, awayoffvo.getPk_psnjob());
		String pk_org_v = NCLocator.getInstance().lookup(IOrgInfoQueryService.class).getOrgVid(jobVO.getPk_org(), new UFDate());
		awayoffvo.setPk_org_v(pk_org_v);
		String pk_dept_v = NCLocator.getInstance().lookup(
				IDeptQueryService.class).getDeptVid(jobVO.getPk_dept(),new UFDate());
		awayoffvo.setPk_dept_v(pk_dept_v);
		String creatordept = new TBMQueryDao().getDeptnameByPsndocAndDateTime(pk_psndoc,new UFDateTime());
		awayoffvo.setAttributeValue("creatordept", creatordept);
		//add by wt 20190821 begin 
//		awayoffvo.setPk_project(awayoffMap.get("pk_project") == null ? "": awayoffMap.get("pk_project").toString());
		return aggVO;
	}

	@Override
	public String queryLeaveoffByPk(Map<String, Object> param)
			throws BusinessException {
		String pk_leaveoff = param.get("pk_leaveoff").toString();
		Map<String, Object> leaveoff = new TBMOffDao()
				.queryLeaveoffByPK(pk_leaveoff);
		PageResult result = new PageResult();
		result.setData(leaveoff);
		result.pushDevInfo("param", param);
		return result.toJson();
	}

	@Override
	public String queryAwayoffByPk(Map<String, Object> param)
			throws BusinessException {
		String pk_awayoff = param.get("pk_awayoff").toString();
		Map<String, Object> awayoff = new TBMOffDao()
				.queryAwayoffByPK(pk_awayoff);
		PageResult result = new PageResult();
		result.setData(awayoff);
		result.pushDevInfo("param", param);
		return result.toJson();
	}

	@Override
	public String deleteLeaveoff(Map<String, Object> param)
			throws BusinessException {
		String pk_leaveoff = param.get("pk_leaveoff").toString();
		Map<String, Object> result = new HashMap<String, Object>();
		AggLeaveoffVO aggVO = (AggLeaveoffVO) NCLocator
				.getInstance().lookup(ILeaveOffApplyQueryMaintain.class)
				.queryByPk(pk_leaveoff);
		deleteBeforcheckState(aggVO.getLeaveoffVO().getApprove_state());
		NCLocator.getInstance().lookup(
				ILeaveOffManageMaintain.class).deleteData(aggVO);
		result.put("flag", "2");
		PageResult pageResult = new PageResult();
		pageResult.setData(result);
		return pageResult.toJson();
	}

	@Override
	public String deleteAwayoff(Map<String, Object> param)
			throws BusinessException {
		String pk_awayoff = param.get("pk_awayoff").toString();
		Map<String, Object> result = new HashMap<String, Object>();
		AggAwayOffVO aggVO = (AggAwayOffVO) NCLocator
				.getInstance().lookup(IAwayOffApplyQueryMaintain.class)
				.queryByPk(pk_awayoff);
		deleteBeforcheckState(aggVO.getAwayOffVO().getApprove_state());
		NCLocator.getInstance().lookup(
				IAwayOffManageMaintain.class).deleteData(aggVO);
		result.put("flag", "2");
		PageResult pageResult = new PageResult();
		pageResult.setData(result);
		return pageResult.toJson();
	}
	
	private void deleteBeforcheckState(Integer pfsate) throws BusinessException {
		if (-1 != pfsate.intValue()) {
			throw new BusinessException(ResHelper.getString("6017hrta",
					"06017hrta0089"));
		}
	}
	
	/**
	 * 提交销假单
	 */
	@Override
	public String submitLeaveoff(Map<String, Object> param)
			throws BusinessException {
		String userId = param.get("userId").toString();
		String pk_leaveoff = param.get("billKey").toString();
		//但强 收回和提交操作判断  rollback 收回 commit 提交 
		String opration = param.get("oprationtype").toString();
		AggLeaveoffVO aggVO = NCLocator
				.getInstance().lookup(ILeaveOffApplyQueryMaintain.class).queryByPk(pk_leaveoff);
		if(opration.equals("rollBack")){
			//收回操作
			Map<String, Object> submitLeave = new TBMOffDao().rollBackLeaveoff(userId,
					aggVO);
		}else{
			//提交操作
			Map<String, Object> submitLeave = new TBMOffDao().submitLeaveoff(userId,
					aggVO);
		}
		AggLeaveoffVO aggVO2 = NCLocator.getInstance().lookup(ILeaveOffApplyQueryMaintain.class).queryByPk(pk_leaveoff);
		LeaveoffVO parentVO = (LeaveoffVO) aggVO2.getParentVO();
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("pk_leaveoff", parentVO.getPk_leaveoff());
		hashMap.put("pk_h", parentVO.getPk_leaveoff());
		hashMap.put("pk_leavereg", parentVO.getPk_leavereg());
		hashMap.put("bill_code", parentVO.getBill_code());
		hashMap.put("approve_state", parentVO.getApprove_state());
		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(hashMap);
		return result.toJson();
	}
	
	/**
	 * 提交销假单
	 */
	@Override
	public String submitAwayoff(Map<String, Object> param)
			throws BusinessException {
		String userId = param.get("userId").toString();
		String pk_awayoff = param.get("billKey").toString();
		//但强 收回和提交操作判断  rollback 收回 commit 提交 
		String opration = param.get("oprationtype").toString();
		AggAwayOffVO aggVO = NCLocator
				.getInstance().lookup(IAwayOffApplyQueryMaintain.class).queryByPk(pk_awayoff);
		if(opration.equals("rollBack")){
			//收回操作
			Map<String, Object> submitLeave = new TBMOffDao().rollBackAwayoff(userId,
					aggVO);
		}else{
			//提交操作
			Map<String, Object> submitLeave = new TBMOffDao().submitAwayoff(userId,
					aggVO);
		}
		AggAwayOffVO aggVO2 = NCLocator.getInstance().lookup(IAwayOffApplyQueryMaintain.class).queryByPk(pk_awayoff);
		AwayOffVO parentVO = (AwayOffVO) aggVO2.getParentVO();
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("pk_awayoff", parentVO.getPk_awayoff());
		hashMap.put("pk_h", parentVO.getPk_awayoff());
		hashMap.put("pk_awayreg", parentVO.getPk_awayreg());
		hashMap.put("bill_code", parentVO.getBill_code());
		hashMap.put("approve_state", parentVO.getApprove_state());
		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(hashMap);
		return result.toJson();
	}
	
	/**
	 * 销假打印信息查询
	 */
	@Override
	public String leaveoffPrintTemplate(Map<String, Object> param)
			throws Exception {
		String pk_leaveoff = (String)param.get("id");
		Map<String, Object> leaveTemp = new TBMOffDao().leaveoffPrintTemplate(pk_leaveoff);
		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(leaveTemp);
		return result.toJson();
	}

	@Override
	public String calculateAwayoffLength(Map<String, Object> param)
			throws BusinessException {
		Map<String, Object> awayoffMap = (Map<String, Object>) param.get("awayoffMap");
		String userid = param.get("userId").toString();

		AggAwayOffVO aggVO = getAwayoffFromMap(awayoffMap, userid, null);
		IAwayOffManageMaintain awayoffM = NCLocator
				.getInstance().lookup(IAwayOffManageMaintain.class);
		aggVO = awayoffM.calculate(aggVO);
		AwayOffVO offvo = aggVO.getAwayOffVO();
		TimeRuleVO timeRulevo = NCLocator
				.getInstance().lookup(ITimeRuleQueryService.class)
				.queryByOrg(offvo.getPk_org());
		DecimalFormat dcmFmt = TBMHelper.getDecimalFormat(timeRulevo
				.getTimedecimal());

		AwayTypeCopyVO[] awayCopyTypes = NCLocator
				.getInstance().lookup(ITimeItemQueryService.class)
				.queryAwayCopyTypesByOrg(offvo.getPk_org(),
						"pk_timeitemcopy = '" + offvo.getPk_awaytypecopy()
								+ "' ");
		AwayTypeCopyVO typeVO = awayCopyTypes[0];
		String unit = (0 == typeVO.getTimeItemUnit() ? PublicLangRes.DAY()
				: PublicLangRes.HOUR());
		awayoffMap.put("reallyawayhour",new UFDouble(dcmFmt.format(offvo.getReallyawayhour())).toString());
		awayoffMap.put("unit", unit);
		awayoffMap.put("differencehour", new UFDouble(dcmFmt.format(offvo.getDifferencehour())).toString());
		PageResult pageResult = new PageResult();
		pageResult.setData(awayoffMap);
		return pageResult.toJson();
	}

	@Override
	public String saveAwayoff(Map<String, Object> param)
			throws BusinessException {
		Map<String, Object> awayoffMap = (Map<String, Object>) param
				.get("awayoffMap");
		String userId = param.get("userId").toString();
		AggAwayOffVO aggVO = getAwayoffFromMap(awayoffMap, userId, null);
		AggAwayOffVO newAway = new TBMOffDao().saveAwayoff(userId, false, aggVO);
		AwayOffVO parentVO = (AwayOffVO) newAway.getParentVO();
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("pk_awayoff", parentVO.getPk_awayoff());
		hashMap.put("bill_code", parentVO.getBill_code());
		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(hashMap);
		return result.toJson();
	}

	@Override
	public String awayoffPrintTemplate(Map<String, Object> param)
			throws Exception {
		String pk_awayoff = (String)param.get("id");
		Map<String, Object> awayoffTemp = new TBMOffDao().awayoffPrintTemplate(pk_awayoff);
		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(awayoffTemp);
		return result.toJson();
	}
	
	@Override
	public String queryAwayreg4off(Map<String, Object> param)
			throws BusinessException {
		String userId = param.get("userId").toString();
		String pk_psndoc = NCLocator.getInstance().lookup(IUserPubService.class).queryPsndocByUserid(userId);
		if (StringUtils.isBlank(pk_psndoc))
			throw new BusinessException(ResHelper.getString("6017mobile","06017mytime000001"));
		UFLiteralDate apply_date = new UFLiteralDate();
		TBMPsndocVO latestVO = NCLocator.getInstance().lookup(ITBMPsndocQueryMaintain.class).queryByPsndocAndDate(pk_psndoc, apply_date);
		if (latestVO == null) {
			throw new BusinessException(ResHelper.getString("6017mobile","06017mytime000002"));
		}
		String pk_org = latestVO.getPk_org();
		Map<String, Object> data = new TBMOffDao().queryAwayreg4off(pk_psndoc, pk_org);
		PageResult result = new PageResult();
		result.setData(data);
		return result.toJson();
	}

	@Override
	public String queryAwayregByPk(Map<String, Object> param)
			throws BusinessException {
		String pk_awayreg = param.get("pk").toString();
		Map<String, Object> awayreg = new TBMOffDao()
				.queryAwayregByPk(pk_awayreg);
		PageResult result = new PageResult();
		result.setData(awayreg);
		result.pushDevInfo("param", param);
		return result.toJson();
	}

}
