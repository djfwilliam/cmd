package nc.impl.saas.hi;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.bs.uif2.validation.DefaultValidationService;
import nc.hr.utils.CommonUtils;
import nc.hr.utils.PubEnv;
import nc.hr.utils.ResHelper;
import nc.hr.utils.StringPiecer;
import nc.impl.hrtrn.PsnDimissionRefServiceImpl;
import nc.impl.saas.pub.SaasCommonHelper;
import nc.itf.hi.oa.FlowNodeVO;
import nc.itf.hi.oa.OperatorVO;
import nc.itf.hr.pf.IHrPf;
import nc.itf.hrtrn.IPsnDimissionRefService;
import nc.itf.om.IDeptQueryService;
import nc.itf.om.IOrgInfoQueryService;
import nc.itf.saas.ITBMOffService;
import nc.itf.saas.ITBMQueryService;
import nc.itf.saas.pub.PageResult;
import nc.itf.saas.pub.SaasMapUtils;
import nc.itf.saas.trn.IPsnRegService;
import nc.itf.ta.IAwayApplyQueryMaintain;
import nc.itf.ta.IAwayOffApplyQueryMaintain;
import nc.itf.ta.ILeaveAppInfoDisplayer;
import nc.itf.ta.ILeaveApplyApproveManageMaintain;
import nc.itf.ta.ILeaveApplyQueryMaintain;
import nc.itf.ta.ILeaveOffApplyQueryMaintain;
import nc.itf.ta.IOvertimeApplyQueryMaintain;
import nc.itf.ta.IPeriodQueryService;
import nc.itf.ta.ISignCardApplyQueryMaintain;
import nc.itf.ta.ITBMPsndocQueryMaintain;
import nc.itf.ta.ITimeItemQueryService;
import nc.itf.ta.ITimeRuleQueryService;
import nc.itf.ta.algorithm.ITimeScopeWithBillInfo;
import nc.itf.trn.regmng.IRegmngQueryService;
import nc.itf.xyjt.webservice.NC_Itf_SelfWebService;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.message.reconstruction.MessageCenterUIConst;
import nc.message.util.MessageCenter;
import nc.message.vo.MessageVO;
import nc.message.vo.NCMessage;
import nc.pubitf.para.SysInitQuery;
import nc.pubitf.rbac.IUserPubService;
import nc.vo.hi.psndoc.PsnJobVO;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.ta.PublicLangRes;
import nc.vo.ta.away.AggAwayVO;
import nc.vo.ta.away.AwayhVO;
import nc.vo.ta.awayoff.AggAwayOffVO;
import nc.vo.ta.awayoff.AwayOffVO;
import nc.vo.ta.bill.BillMutexException;
import nc.vo.ta.leave.AggLeaveVO;
import nc.vo.ta.leave.LeaveCheckLengthResult;
import nc.vo.ta.leave.LeaveCheckResult;
import nc.vo.ta.leave.LeavebVO;
import nc.vo.ta.leave.LeavehVO;
import nc.vo.ta.leave.pf.validator.PFSaveLeaveValidator;
import nc.vo.ta.leavebalance.LeaveBalanceVO;
import nc.vo.ta.leaveoff.AggLeaveoffVO;
import nc.vo.ta.leaveoff.LeaveoffVO;
import nc.vo.ta.overtime.AggOvertimeVO;
import nc.vo.ta.overtime.OvertimehVO;
import nc.vo.ta.period.PeriodVO;
import nc.vo.ta.psndoc.TBMPsndocVO;
import nc.vo.ta.signcard.AggSignVO;
import nc.vo.ta.signcard.SignhVO;
import nc.vo.ta.timeitem.LeaveTypeCopyVO;
import nc.vo.ta.timerule.TimeRuleVO;
import nc.vo.trn.regmng.AggRegapplyVO;
import nc.vo.trn.regmng.RegapplyVO;
import nc.vo.trn.transmng.AggStapply;
import nc.vo.trn.transmng.StapplyVO;
import nc.vo.uap.pf.PfProcessBatchRetObject;
import nc.vo.util.BDVersionValidationUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 员工假勤
 * 
 * @author nijb@yonyou.com
 * 
 */
public class TBMQueryServiceImpl implements ITBMQueryService {

	/**
	 * 获取我的申请列表
	 */
	@Override
	public String getMyApplication(Map<String, Object> param)
			throws BusinessException {
		Integer pageno = null;
		if (param.get("pageno") != null && !"".equals(param.get("pageno"))) {
			pageno = Integer.parseInt(param.get("pageno").toString());
		}
		Integer pagesize = null;
		if (param.get("pagesize") != null && !"".equals(param.get("pagesize"))) {
			pagesize = Integer.parseInt(param.get("pagesize").toString());
		}
		String userId = param.get("userId").toString();

		String state = "all";//
		if (param.get("state") != null && !"".equals(param.get("state"))) {
			state = param.get("state").toString();
		}
		String billstateCond = null;
		String inCond = "";
		String isFinish = "";
		if(param.get("activeName") != null && !"".equals(param.get("activeName"))){
			if(param.get("activeName").toString().equals("first")){
				//查询待办
				inCond = " in (2,3)";
				isFinish = "N";
			}else if(param.get("activeName").toString().equals("second")){
				inCond = " in (0,1,2,3,102)";
				isFinish = "Y";
			}else{
			}
		}else{
			if (("allState".equalsIgnoreCase(state))
					|| ("all".equalsIgnoreCase(state))) {
				billstateCond = " in (-1,0,1,2,3) ";
			} else if ("free".equals(state)) {
				billstateCond = " in (-1) ";
			} else if ("going".equals(state)) {
				billstateCond = " in (2,3) ";
			} else if ("approved".equals(state)) {
				billstateCond = " in (0,1) ";
			} else {
				billstateCond = " in (-1,2,3) ";
			}
		}
		String billtype = null; // all全部（默认），leave请假，overtime加班，signcard补考勤，away出差 /leaveoff销假，awayoff销差
		if (param.get("billtype") != null && !"".equals(param.get("billtype"))) {
			billtype = param.get("billtype").toString();
		} else {
			billtype = "all";
		}

		String timescope = null;
		UFLiteralDate curDate = new UFLiteralDate();
		UFLiteralDate begindate = null;
		if ("one".equals(timescope)) {
			begindate = curDate.getDateBefore(30);
		} else if ("three".equals(timescope)) {
			begindate = curDate.getDateBefore(90);
		} else if ("half".equals(timescope)) {
			begindate = curDate.getDateBefore(180);
		} else {

		}

		PageResult result = null;
		if(param.get("activeName") != null && !"".equals(param.get("activeName"))){
			result = new TBMQueryDao().queryBills(userId, begindate, billtype, pageno, pagesize, inCond, isFinish);
		}else{
			result = new TBMQueryDao().queryBills(userId, begindate,
					billstateCond, billtype, pageno, pagesize);
		}

		result.pushDevInfo("params", param);
		return result.toJson();
	}
	
	/**
	 * 获取我的审批列表
	 * @param param
	 * @return String
	 */
	@Override
	public String getMyApprove(Map<String, Object> param)
			throws BusinessException {
		String pk_psndoc = param.get("userId").toString();
		int type = Integer.parseInt(param.get("type").toString());
		String inCond = "";
		String isFinish = "";
		if (type == 0) {
			inCond = "(2,3)";
			isFinish = "N";
		} else if (type == 1) {
			inCond = "(0,1,2,3,102)";
			isFinish = "Y";
		}
		PageResult result = new TBMQueryDao().queryApprove(pk_psndoc, inCond, isFinish);
		return result.toJson();
	}

	/**
	 * 获取请假类型 年假事假。。。
	 */
	@Override
	public String queryLeaveType(Map<String, Object> param)
			throws BusinessException {
		String userId = param.get("userId").toString();
		String billtype = null;
		if (param.get("billtype") != null && !"".equals(param.get("billtype"))) {
			billtype = param.get("billtype").toString();
		} else {
			billtype = "leave";
		}
		List<Map<String, Object>> data = new TBMQueryDao().queryLeaveType(
				userId, billtype);
		PageResult result = new PageResult();
		result.setData(data);
		result.pushDevInfo("param", param);
		return result.toJson();
	}

	/**
	 * 获取请假详情
	 */
	@Override
	public String queryLeaveByPk(Map<String, Object> param)
			throws BusinessException {
		String pk_leaveh = param.get("pk_leaveh").toString();
		Map<String, Object> leaveh = new TBMQueryDao()
				.queryLeaveByPK(pk_leaveh);
		PageResult result = new PageResult();
		result.setData(leaveh);
		result.pushDevInfo("param", param);
		return result.toJson();
	}

	/**
	 * 保存请假单
	 */
	@Override
	public String saveLeave(Map<String, Object> param) throws BusinessException {
		Map<String, Object> leavehMap = (Map<String, Object>) param
				.get("leavehMap");
		List<Map<String, Object>> bList = (List<Map<String, Object>>) param
				.get("bList");
		String userId = param.get("userId").toString();
		AggLeaveVO aggVO = getLeaveFromMap(leavehMap, bList, userId, null);
		AggLeaveVO newLeave = new TBMQueryDao().saveLeave(userId, false, aggVO);
		LeavehVO parentVO = (LeavehVO) newLeave.getParentVO();
//		parentVO.SETPK_PROJECT(aggVO.getParentVO().getAttributeValue("pk_project").toString());
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("pk_leaveh", parentVO.getPk_leaveh());
		hashMap.put("bill_code", parentVO.getBill_code());
		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(hashMap);
		return result.toJson();
	}

	/**
	 * 提交请假单
	 */
	@Override
	public String submitLeave(Map<String, Object> param)
			throws BusinessException {
		String userId = param.get("userId").toString();
		String pk_leaveh = param.get("billKey").toString();
		//但强 收回和提交操作判断  rollback 收回 commit 提交 
		String opration = param.get("oprationtype").toString();
		String whereSql = " pk_leaveh = '" + pk_leaveh + "' ";
		AggLeaveVO[] aggVOs = NCLocator
				.getInstance().lookup(ILeaveApplyQueryMaintain.class)
				.queryByWhereSQL(null, whereSql);
		AggLeaveVO aggLeaveVO = aggVOs[0];
		if(opration.equals("rollBack")){
			//收回操作
			Map<String, Object> submitLeave = new TBMQueryDao().rollBackLeave(userId,
					aggLeaveVO);
		}else{
			//提交操作
			Map<String, Object> submitLeave = new TBMQueryDao().submitLeave(userId,
					aggLeaveVO);
		}
		AggLeaveVO[] aggVOs2 = NCLocator.getInstance().lookup(ILeaveApplyQueryMaintain.class).queryByWhereSQL(null, whereSql);
		AggLeaveVO aggLeaveVO2 = aggVOs2[0];
		LeavehVO parentVO = (LeavehVO) aggLeaveVO2.getParentVO();
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("pk_leaveh", parentVO.getPk_leaveh());
		hashMap.put("pk_h", parentVO.getPk_leaveh());
		hashMap.put("bill_code", parentVO.getBill_code());
		hashMap.put("approve_state", parentVO.getApprove_state());
		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(hashMap);
		return result.toJson();
	}
	 
	/**
	 * 保存和提交请假单
	 */
	@Override
	public String saveAndSubmitLeave(Map<String, Object> param)
			throws BusinessException {
		Map<String, Object> leavehMap = (Map<String, Object>) param
				.get("leavehMap");
		List<Map<String, Object>> bList = (List<Map<String, Object>>) param
				.get("bList");
		String userId = param.get("userId").toString();
		AggLeaveVO aggVO = getLeaveFromMap(leavehMap, bList, userId, null);
		TBMQueryDao queryDao = new TBMQueryDao();
		AggLeaveVO newLeave = queryDao.saveLeave(userId, false, aggVO);
		Map<String, Object> submitLeave = queryDao
				.submitLeave(userId, newLeave);

		LeavehVO parentVO = (LeavehVO) newLeave.getParentVO();
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("pk_leaveh", parentVO.getPk_leaveh());
		hashMap.put("bill_code", parentVO.getBill_code());

		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(hashMap);
		return result.toJson();
	}

	@Override
	public String getNewLeave(Map<String, Object> param)
			throws BusinessException {
		String userid = param.get("userId").toString();
		String pk_psndoc = NCLocator.getInstance().lookup(
				IUserPubService.class).queryPsndocByUserid(userid);
		TBMPsndocVO latestVO = NCLocator
				.getInstance().lookup(ITBMPsndocQueryMaintain.class)
				.queryByPsndocAndDateTime(pk_psndoc, new UFDateTime());
		if (latestVO == null) {
			throw new BusinessException(ResHelper.getString("6017mobile",
					"06017mytime000002"));
		}
		// Map<String, Object> transtypes = new
		// TBMQueryDao().getTranstypes(userid,"leave");
		String pk_leavetypecopy = null;
		if (param.get("pk_leavetypecopy") != null
				&& !"".equals(param.get("pk_leavetypecopy"))) {
			pk_leavetypecopy = param.get("pk_leavetypecopy").toString();
		} else {
			String pk_leavetype = param.get("pk_leavetype").toString();
			pk_leavetypecopy = new TBMQueryDao().getPk_leavetypecopy(
					pk_leavetype, latestVO.getPk_org());
		}

		Map<String, Object> result = new HashMap<String, Object>();
		// result.put("pk_timeitemcopy", pk_leavetypecopy);

		String pk_org = latestVO.getPk_org();
		String pk_group = latestVO.getPk_group();
		// initGroup(pk_group);
		LeaveTypeCopyVO[] leaveCopyTypes = NCLocator
				.getInstance().lookup(ITimeItemQueryService.class)
				.queryLeaveCopyTypesByOrg(pk_org, "pk_timeitemcopy = '"
						+ pk_leavetypecopy + "' ");
		LeaveTypeCopyVO typeVO = null;

		if (!ArrayUtils.isEmpty(leaveCopyTypes)) {
			typeVO = leaveCopyTypes[0];
		}
		String typename = typeVO == null ? "" : typeVO.getMultilangName();
		result.put("leavetypename", typename);

		TimeRuleVO timeRulevo = NCLocator
				.getInstance().lookup(ITimeRuleQueryService.class)
				.queryByOrg(pk_org);

		if ((timeRulevo == null) || (timeRulevo.isPreHolidayFirst())) {
			result.put("isyearedit", "N");
			result.put("ismonthedit", "N");
		} else {
			result.put("isyearedit", "Y");
			if ((typeVO == null)
					|| (typeVO.getLeavesetperiod().intValue() != 0)) {
				result.put("ismonthedit", "N");
			} else {
				result.put("ismonthedit", "Y");
			}
		}

		AggLeaveVO aggVO = new AggLeaveVO();
		LeavehVO leavevo = new LeavehVO();
		aggVO.setParentVO(leavevo);
		leavevo.setPk_org(pk_org);
		leavevo.setPk_group(pk_group);
		leavevo.setApply_date(new UFLiteralDate());
		leavevo.setBillmaker(userid);

		leavevo.setPk_psndoc(pk_psndoc);
		leavevo.setPk_psnjob(latestVO.getPk_psnjob());
		leavevo.setPk_leavetype(typeVO == null ? "" : typeVO.getPk_timeitem());
		leavevo.setPk_leavetypecopy(typeVO == null ? "" : typeVO
				.getPk_timeitemcopy());
		boolean isLactation = typeVO == null ? false : "1002Z710000000021ZM3"
				.equals(typeVO.getPk_timeitem());
		leavevo.setIslactation(UFBoolean.valueOf(isLactation));

		LeavebVO bvo = new LeavebVO();
		bvo.setPk_group(pk_group);
		bvo.setPk_org(pk_org);
		LeavebVO[] bvos = { bvo };
		aggVO.setChildrenVO(bvos);
		if (!isLactation) {
			ILeaveAppInfoDisplayer displayer = NCLocator
					.getInstance().lookup(ILeaveAppInfoDisplayer.class);
			aggVO = displayer.calculate(aggVO,
					TimeZone.getDefault());
		}

		List<Map<String, Object>> blist = new ArrayList();
		result.put("leavebs", blist);

		DecimalFormat dcmFmt = TBMHelper.getDecimalFormat(pk_org);

		Map<String, Object> b1 = new HashMap<String, Object>();
		b1.put("begintime", bvo.getLeavebegintime() == null ? "" : bvo
				.getLeavebegintime().toString());
		b1.put("endtime", bvo.getLeaveendtime() == null ? "" : bvo
				.getLeaveendtime().toString());

		b1.put("length",
				new UFDouble(
						dcmFmt.format(bvo.getLeavehour() == null ? UFDouble.ZERO_DBL
								: bvo.getLeavehour())).toString());
		blist.add(b1);

		// return result;
		return null;
	}

	/**
	 * 计算请假 时长
	 */
	@Override
	public String calculateLeaveLength(Map<String, Object> param)
			throws BusinessException {
		Map<String, Object> leavehMap = (Map<String, Object>) param.get("leavehMap");
		List<Map<String, Object>> bList = (List<Map<String, Object>>) param.get("bList");
		String userid = param.get("userId").toString();

		AggLeaveVO aggVO = getLeaveFromMap(leavehMap, bList, userid, null);
		LeavebVO[] calbvos = aggVO.getLeavebVOs();
		ILeaveAppInfoDisplayer displayer = NCLocator
				.getInstance().lookup(ILeaveAppInfoDisplayer.class);
		aggVO = displayer.calculate(aggVO, TimeZone.getDefault());
		LeavehVO headVO = aggVO.getHeadVO();
		TimeRuleVO timeRulevo = NCLocator
				.getInstance().lookup(ITimeRuleQueryService.class)
				.queryByOrg(headVO.getPk_org());
		DecimalFormat dcmFmt = TBMHelper.getDecimalFormat(timeRulevo
				.getTimedecimal());

		LeaveTypeCopyVO[] leaveCopyTypes = NCLocator
				.getInstance().lookup(ITimeItemQueryService.class)
				.queryLeaveCopyTypesByOrg(headVO.getPk_org(),
						"pk_timeitemcopy = '" + headVO.getPk_leavetypecopy()
								+ "' ");
		LeaveTypeCopyVO typeVO = leaveCopyTypes[0];
		String unit = (0 == typeVO.getTimeItemUnit() ? PublicLangRes.DAY()
				: PublicLangRes.HOUR());

		int j = 0;
		for (Map<String, Object> bmap : bList) {
			LeavebVO bvo = calbvos[j];
			bmap.put("length",new UFDouble(dcmFmt.format(bvo.getLeavehour())).toString());
			bmap.put("begintime",bvo.getLeavebegintime().toString());
			bmap.put("endtime",bvo.getLeaveendtime().toString());
			bmap.put("unit", unit);
			j++;
		}
		leavehMap.put("sumhour",new UFDouble(dcmFmt.format(headVO.getSumhour())).toString());
		leavehMap.put("leavebs", bList);
		leavehMap.put("unit", unit);
		PageResult pageResult = new PageResult();
		pageResult.setData(leavehMap);
		return pageResult.toJson();
	}

	/**
	 * 删除一个请假单
	 * 
	 * @param pk_leaveh
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public String deleteLeave(Map<String, Object> param)
			throws BusinessException {
		String pk_leaveh = param.get("pk_leaveh").toString();
		Map<String, Object> result = new HashMap<String, Object>();
		AggLeaveVO aggVO = (AggLeaveVO) NCLocator
				.getInstance().lookup(ILeaveApplyQueryMaintain.class)
				.queryByPk(pk_leaveh);
		deleteBeforcheckState(aggVO.getLeavehVO().getApprove_state());
		NCLocator.getInstance().lookup(
				ILeaveApplyApproveManageMaintain.class).deleteData(aggVO);
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

	private AggLeaveVO getLeaveFromMap(Map<String, Object> leavehMap,
			List<Map<String, Object>> bList, String userid,
			LeaveTypeCopyVO typeVO) throws BusinessException {
		if (MapUtils.isEmpty(leavehMap))
			return null;
		String pk_psndoc = NCLocator.getInstance().lookup(IUserPubService.class).queryPsndocByUserid(userid);
		if (StringUtils.isBlank(pk_psndoc))
			throw new BusinessException(ResHelper.getString("6017mobile","06017mytime000001"));
		AggLeaveVO aggVO = new AggLeaveVO();
		String pk_leaveh = null;
		if (leavehMap.get("pk_leaveh") != null && !"".equals(leavehMap.get("pk_leaveh"))) {
			pk_leaveh = leavehMap.get("pk_leaveh").toString();
		}
		AggLeaveVO oldAggVO = null;
		LeavehVO leavevo = null;
		UFLiteralDate apply_date = new UFLiteralDate();
		if (pk_leaveh != null) {
			String whereSql = " pk_leaveh = '" + pk_leaveh + "' ";
			oldAggVO = NCLocator.getInstance()
					.lookup(ILeaveApplyQueryMaintain.class).queryByWhereSQL(
					null, whereSql)[0];
			leavevo = oldAggVO.getHeadVO();
			leavevo.setTs(leavehMap.get("ts") == null ? new UFDateTime(): new UFDateTime((String) leavehMap.get("ts")));
			apply_date = leavevo.getApply_date();
		} else {
			leavevo = new LeavehVO();
			UFLiteralDate newdate = new UFLiteralDate();
			leavevo.setApply_date(newdate);
			leavevo.setBillmaker(userid);
			leavevo.setIshrssbill(UFBoolean.TRUE);
			leavevo.setApprove_state(Integer.valueOf(-1));
			leavevo.setTs(leavehMap.get("ts") == null ? new UFDateTime()
					: new UFDateTime((String) leavehMap.get("ts")));
			leavevo.setCreationtime(leavehMap.get("creationtime") == null ? new UFDateTime()
					: new UFDateTime((String) leavehMap.get("creationtime")));

		}
		TBMPsndocVO latestVO = NCLocator.getInstance().lookup(ITBMPsndocQueryMaintain.class).queryByPsndocAndDate(pk_psndoc, apply_date);
		if (latestVO == null) {
			throw new BusinessException(ResHelper.getString("6017mobile","06017mytime000002"));
		}
		String pk_timeitemcopy = (String) leavehMap.get("pk_timeitemcopy");
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
		leavevo.setLeaveremark((String)leavehMap.get("leaveremark"));
//		leavevo.setLeavereason((String)leavehMap.get("leavereason"));
		aggVO.setParentVO(leavevo);
		leavevo.setPk_org(pk_org);
		leavevo.setPk_group(pk_group);
		leavevo.setBillmaker(userid);
		String pk_psnorg = latestVO.getPk_psnorg();
		leavevo.setPk_psnorg(pk_psnorg);
		leavevo.setPk_psndoc(pk_psndoc);
		String pk_psnjob = latestVO.getPk_psnjob();
		leavevo.setPk_psnjob(pk_psnjob);
		String pk_timeitem = typeVO.getPk_timeitem();
		leavevo.setPk_leavetype(pk_timeitem);
		leavevo.setPk_leavetypecopy(pk_timeitemcopy);
		//add by wt 20190821 begin 
//		leavevo.setPk_project(leavehMap.get("pk_project") == null ? "" : leavehMap.get("pk_project").toString());
		leavevo.setTranstypeid(leavehMap.get("transtypeid") == null ? "" : leavehMap.get("transtypeid").toString());
		if(leavehMap.get("transtypeid") != null){
			String transtypesql = "select pk_billtypecode from bd_billtype where pk_billtypeid = '" + leavehMap.get("transtypeid").toString() + "'";
			leavevo.setTranstype((String)(new BaseDAO().executeQuery(transtypesql, new ColumnProcessor())));
		}
		boolean isLactation = "1002Z710000000021ZM3".equals(typeVO.getPk_timeitem());
		leavevo.setIslactation(UFBoolean.valueOf(isLactation));
		if(isLactation){
			leavevo.setLactationhour(new UFDouble(leavehMap.get("lactationhour").toString()));
			leavevo.setSumhour(new UFDouble());
		}
		leavevo.setPk_billtype("6404");
		leavevo.setCreator(userid);
		PsnJobVO jobVO = (PsnJobVO) new BaseDAO().retrieveByPK(PsnJobVO.class, leavevo.getPk_psnjob());
		String pk_org_v = NCLocator.getInstance().lookup(IOrgInfoQueryService.class).getOrgVid(jobVO.getPk_org(), new UFDate());
		leavevo.setPk_org_v(pk_org_v);
		String pk_dept_v = NCLocator.getInstance().lookup(
				IDeptQueryService.class).getDeptVid(jobVO.getPk_dept(),new UFDate());
		leavevo.setPk_dept_v(pk_dept_v);
		String creatordept = new TBMQueryDao().getDeptnameByPsndocAndDateTime(pk_psndoc,new UFDateTime());
		leavevo.setAttributeValue("creatordept", creatordept);
//		Boolean undertakingdept = (Boolean)leavehMap.get("undertakingdept");
		//是否是事业部员工
//		if(undertakingdept !=null && undertakingdept){
//			leavevo.setAttributeValue("undertakingdept", new UFBoolean(true));
//		} else {
//			leavevo.setAttributeValue("undertakingdept", new UFBoolean(false));
//		}
		if (CollectionUtils.isEmpty(bList)) {
			return aggVO;
		}
		boolean flag = true;
		ArrayList<LeavebVO> bvolist = new ArrayList<LeavebVO>();
		for (Map<String, Object> bmap : bList) {
			LeavebVO bvo = new LeavebVO();
			String pk_leaveb = (String) bmap.get("pk_leaveb");
			if (StringUtils.isBlank(pk_leaveb)) {
				bvo.setStatus(VOStatus.NEW);
			} else {
				bvo.setStatus(VOStatus.UPDATED);
				bvo.setTs(bmap.get("ts") == null ? new UFDateTime(): new UFDateTime((String) bmap.get("ts")));
			}
			bvo.setPk_leaveb(pk_leaveb);
			bvo.setPk_leaveh(pk_leaveh);
			bvolist.add(bvo);
			bvo.setPk_psnorg(pk_psnorg);
			bvo.setPk_org(pk_org);
			bvo.setPk_group(pk_group);
			bvo.setPk_psndoc(pk_psndoc);
			bvo.setPk_psnjob(pk_psnjob);
			bvo.setPk_leavetype(pk_timeitem);
			bvo.setPk_leavetypecopy(pk_timeitemcopy);
			if (bmap.get("leaveremark") != null) {
				bvo.setLeaveremark(bmap.get("leaveremark").toString());
			}
			bvo.setLeavehour(new UFDouble((String) bmap.get("length")));
			bvo.setLeavebegintime(SaasMapUtils.getDateTimeValue(bmap, "begintime"));
			bvo.setLeavebegindate(SaasMapUtils.getDateValue(bmap, "begintime"));
			bvo.setLeaveendtime(SaasMapUtils.getDateTimeValue(bmap, "endtime"));
			bvo.setLeaveenddate(SaasMapUtils.getDateValue(bmap, "endtime"));
			if(isLactation){
				bvo.setLactationholidaytype(Integer.parseInt((String) bmap.get("lactationtype")));
				bvo.setLactationhour(leavevo.getLactationhour());
				bvo.setIslactation(new UFBoolean(true));
				bvo.setLeavehour(new UFDouble());
			}
			if(latestVO.getBegindate().after(new UFLiteralDate(SaasMapUtils.getDateTimeValue(bmap, "begintime").toString()))){
				flag = false;
			}
		}
		if(!flag){
			throw new BusinessException("不允许申请考勤档案开始日期"+latestVO.getBegindate()+"之前的请假记录");
		}
		if (oldAggVO != null && oldAggVO.getBodyVOs() != null) {// 计算被删除的子集
			LeavebVO[] oldbodyVOs = oldAggVO.getBodyVOs();
			for (LeavebVO oldbvo : oldbodyVOs) {
				boolean isdelete = true;
				for (LeavebVO newvo : bvolist) {
					if (StringUtils.equals(oldbvo.getPk_leaveb(), newvo.getPk_leaveb())) {
						isdelete = false;
						break;
					}
				}
				if (isdelete) {
					oldbvo.setStatus(VOStatus.DELETED);
					bvolist.add(oldbvo);
				}
			}
		}
		aggVO.setChildrenVO(bvolist.toArray(new LeavebVO[0]));
		return aggVO;
	}

	/**
	 * 获取请假剩余
	 */
	@Override
	public String getLeaveBalance(Map<String, Object> param)
			throws BusinessException {
		String userid = param.get("userId").toString();
		String pk_timeitemcopy = param.get("pk_timeitemcopy").toString();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String pk_psndoc = NCLocator.getInstance().lookup(
				IUserPubService.class).queryPsndocByUserid(userid);
		TBMPsndocVO latestVO = NCLocator
				.getInstance().lookup(ITBMPsndocQueryMaintain.class)
				.queryByPsndocAndDateTime(pk_psndoc, new UFDateTime());
		if (latestVO == null) {
			return null;
		}
		String pk_org = latestVO.getPk_org();
		LeaveTypeCopyVO[] leaveTypeCopyVOs = NCLocator
				.getInstance().lookup(ITimeItemQueryService.class)
				.queryLeaveCopyTypesByOrg(pk_org, "pk_timeitemcopy = '"
						+ pk_timeitemcopy + "' ");

		IPeriodQueryService periodQuery = NCLocator
				.getInstance().lookup(IPeriodQueryService.class);
		PeriodVO curPeriod = periodQuery.queryByDate(pk_org,
				new UFLiteralDate());
		if (null == curPeriod) {
			curPeriod = periodQuery.queryCurPeriod(pk_org);
		}
		resultMap.put("enddate",
				"" + ResHelper.getString("6017mobile", "06017mytime000004")
						+ curPeriod.getEnddate().toString());
		PeriodVO previousPeriod = periodQuery.queryPreviousPeriod(pk_org,
				new UFLiteralDate());
		Collection<LeaveBalanceVO> leavec = queryCurrentVOs(pk_org, pk_psndoc,
				curPeriod, leaveTypeCopyVOs);
		Map<String, LeaveBalanceVO> preLeaveMap = queryPreviousVOs(pk_org,
				pk_psndoc, curPeriod, previousPeriod, leaveTypeCopyVOs);

		LeaveBalanceVO[] leavevos = CollectionUtils.isEmpty(leavec) ? null
				: (LeaveBalanceVO[]) leavec.toArray(new LeaveBalanceVO[0]);

		Map<String, LeaveBalanceVO> balanceMap = CommonUtils.toMap(
				"pk_timeitem", leavevos);
		DecimalFormat dcmFmt = TBMHelper.getDecimalFormat(pk_org);

		List<Map<String, Object>> typeList = new ArrayList();
		resultMap.put("displaylist", typeList);

		for (LeaveTypeCopyVO typevo : leaveTypeCopyVOs) {
			if (3 != typevo.getEnablestate().intValue()) {

				if (!"1002Z710000000021ZM3".equals(typevo.getPk_timeitem())) {

					Map<String, Object> balanceinfo = new HashMap<String, Object>();
					typeList.add(balanceinfo);

					String unit = 0 == typevo.getTimeItemUnit() ? PublicLangRes
							.DAY() : PublicLangRes.HOUR();

					balanceinfo.put("unit", unit);
					balanceinfo.put("leavetypename", typevo.getMultilangName());
					balanceinfo.put("pk_timeitemcopy",
							typevo.getPk_timeitemcopy());
					balanceinfo.put("pk_timeitem", typevo.getPk_timeitem());

					boolean isHrssShow = typevo.getIshrssshow() == null ? false
							: typevo.getIshrssshow().booleanValue();
					if (isHrssShow) {
						balanceinfo.put("flag", "Y");
					} else {
						balanceinfo.put("flag", "N");
					}

					LeaveBalanceVO vo = balanceMap == null ? null
							: (LeaveBalanceVO) balanceMap.get(typevo
									.getPk_timeitem());
					if (vo == null) {
						if (isHrssShow) {
							balanceinfo.put("lastdayorhour", 0);
							balanceinfo.put("realdayorhour", 0);
						}
						balanceinfo.put("yidayorhour", 0);
						balanceinfo.put("usefulrestdayorhour", 0);
						balanceinfo.put("freeze", 0);
					} else {
						LeaveBalanceVO prevo = (LeaveBalanceVO) preLeaveMap
								.get(vo.getPk_timeitem());
						UFDouble lastdayorhour = vo.getLastdayorhour();
						if ((prevo != null)
								&& (!prevo.getIssettlement().booleanValue())) {
							lastdayorhour = ((LeaveBalanceVO) preLeaveMap
									.get(vo.getPk_timeitem()))
									.getUsefulrestdayorhour();
						}
						if ((lastdayorhour != null) && (isHrssShow)) {
							lastdayorhour = new UFDouble(
									dcmFmt.format(lastdayorhour));

							balanceinfo.put("lastdayorhour",
									lastdayorhour.toString());
						}
						UFDouble realdayorhour = vo.getRealdayorhour();
						if ((realdayorhour != null) && (isHrssShow)) {
							realdayorhour = new UFDouble(
									dcmFmt.format(realdayorhour));

							balanceinfo.put("realdayorhour",
									realdayorhour.toString());
						}
						UFDouble yidayorhour = vo.getYidayorhour();
						if (yidayorhour != null) {
							yidayorhour = new UFDouble(
									dcmFmt.format(yidayorhour));

							balanceinfo.put("yidayorhour",
									yidayorhour.toString());
						}
						UFDouble usefulrestdayorhour = vo
								.getUsefulrestdayorhour();
						if (usefulrestdayorhour != null) {
							usefulrestdayorhour = new UFDouble(
									dcmFmt.format(usefulrestdayorhour));

							balanceinfo.put("usefulrestdayorhour",
									usefulrestdayorhour.toString());
						}

						UFDouble freeze = vo.getFreezedayorhour();
						if (freeze != null) {
							freeze = new UFDouble(dcmFmt.format(freeze));

							balanceinfo.put("freeze", freeze.toString());
						}
					}
				}
			}
		}
		PageResult pageResult = new PageResult();
		ArrayList displaylist = (ArrayList) resultMap.get("displaylist");
		if (displaylist.size() > 0) {
			pageResult.setData(displaylist.get(0));
		}
		return pageResult.toJson();
	}

	private Collection<LeaveBalanceVO> queryCurrentVOs(String pk_org,
			String pk_psndoc, PeriodVO curPeriod,
			LeaveTypeCopyVO[] leaveTypeCopyVOs) throws BusinessException {
		Collection<LeaveBalanceVO> retc = new ArrayList();
		if (ArrayUtils.isEmpty(leaveTypeCopyVOs)) {
			return retc;
		}
		List<LeaveTypeCopyVO> monthList = new ArrayList();
		List<LeaveTypeCopyVO> yearList = new ArrayList();
		for (LeaveTypeCopyVO typevo : leaveTypeCopyVOs) {
			if (0 == typevo.getLeavesetperiod().intValue()) {
				monthList.add(typevo);
			} else {
				yearList.add(typevo);
			}
		}

		if (CollectionUtils.isNotEmpty(yearList)) {
			String condition = "pk_org = ? and pk_psndoc = ? and   curyear = ? and  curmonth is null and pk_timeitem in ("
					+ StringPiecer.getDefaultPiecesTogether(
							(SuperVO[]) yearList
									.toArray(new LeaveTypeCopyVO[0]),
							"pk_timeitem") + ")";

			SQLParameter para = new SQLParameter();
			para.addParam(pk_org);
			para.addParam(pk_psndoc);
			para.addParam(curPeriod.getTimeyear());
			BaseDAO dao = new BaseDAO();
			Collection<LeaveBalanceVO> leavec = dao.retrieveByClause(
					LeaveBalanceVO.class, condition, para);
			retc.addAll(leavec);
		}

		if (CollectionUtils.isNotEmpty(monthList)) {
			String condition = "pk_org = ? and pk_psndoc = ? and   curyear = ? and  curmonth = ? and pk_timeitem in ("
					+ StringPiecer.getDefaultPiecesTogether(
							(SuperVO[]) monthList
									.toArray(new LeaveTypeCopyVO[0]),
							"pk_timeitem") + ")";

			SQLParameter para = new SQLParameter();
			para.addParam(pk_org);
			para.addParam(pk_psndoc);
			para.addParam(curPeriod.getTimeyear());
			para.addParam(curPeriod.getTimemonth());
			BaseDAO dao = new BaseDAO();
			Collection<LeaveBalanceVO> leavec = dao.retrieveByClause(
					LeaveBalanceVO.class, condition, para);
			retc.addAll(leavec);
		}
		return retc;
	}

	private Map<String, LeaveBalanceVO> queryPreviousVOs(String pk_org,
			String pk_psndoc, PeriodVO curPeriod, PeriodVO previousPeriod,
			LeaveTypeCopyVO[] leaveTypeCopyVOs) throws BusinessException {
		Map<String, LeaveBalanceVO> reMap = new HashMap();
		if (ArrayUtils.isEmpty(leaveTypeCopyVOs)) {
			return reMap;
		}
		List<LeaveTypeCopyVO> monthList = new ArrayList();
		List<LeaveTypeCopyVO> yearList = new ArrayList();
		for (LeaveTypeCopyVO typevo : leaveTypeCopyVOs) {
			if (0 == typevo.getLeavesetperiod().intValue()) {
				monthList.add(typevo);
			} else {
				yearList.add(typevo);
			}
		}

		if (CollectionUtils.isNotEmpty(yearList)) {
			String previousYear = Integer.toString(Integer.parseInt(curPeriod
					.getTimeyear()) - 1);
			String condition = "pk_org = ? and pk_psndoc = ? and   curyear = ? and (curmonth is null or curmonth = '~') and pk_timeitem in ("
					+ StringPiecer.getDefaultPiecesTogether(
							(SuperVO[]) yearList
									.toArray(new LeaveTypeCopyVO[0]),
							"pk_timeitem") + ")";

			SQLParameter para = new SQLParameter();
			para.addParam(pk_org);
			para.addParam(pk_psndoc);
			para.addParam(previousYear);
			BaseDAO dao = new BaseDAO();
			Collection<LeaveBalanceVO> leavec = dao.retrieveByClause(
					LeaveBalanceVO.class, condition, para);
			if (CollectionUtils.isNotEmpty(leavec)) {
				for (LeaveBalanceVO vo : leavec) {
					reMap.put(vo.getPk_timeitem(), vo);
				}
			}
		}

		if ((CollectionUtils.isNotEmpty(monthList)) && (previousPeriod != null)) {
			String condition = "pk_org = ? and pk_psndoc = ? and   curyear = ? and  curmonth = ? and pk_timeitem in ("
					+ StringPiecer.getDefaultPiecesTogether(
							(SuperVO[]) monthList
									.toArray(new LeaveTypeCopyVO[0]),
							"pk_timeitem") + ")";

			SQLParameter para = new SQLParameter();
			para.addParam(pk_org);
			para.addParam(pk_psndoc);
			para.addParam(previousPeriod.getTimeyear());
			para.addParam(previousPeriod.getTimemonth());
			BaseDAO dao = new BaseDAO();
			Collection<LeaveBalanceVO> leavec = dao.retrieveByClause(
					LeaveBalanceVO.class, condition, para);
			if (CollectionUtils.isNotEmpty(leavec)) {
				for (LeaveBalanceVO vo : leavec) {
					reMap.put(vo.getPk_timeitem(), vo);
				}
			}
		}
		return reMap;
	}

	// =========日历考勤中心=========
	/**
	 * 获取考勤中心日历
	 */
	@Override
	public String queryCalendar(Map<String, Object> param)
			throws BusinessException {
		String userId = param.get("userId").toString();
		String attendMonth = param.get("attendanceMonth").toString();
		UFLiteralDate beginDate = UFLiteralDate.getDate(attendMonth+"-01");
		//获取一个月最后一天
		String[] split = attendMonth.split("-");
		Calendar monthCalendar = Calendar.getInstance();
		monthCalendar.set(Calendar.YEAR,Integer.parseInt(split[0]));
		monthCalendar.set(Calendar.MONTH,Integer.parseInt(split[1])-1);
		int lastDay = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);  
		monthCalendar.set(Calendar.DAY_OF_MONTH, lastDay);  
		UFLiteralDate endDate = UFLiteralDate.getDate(monthCalendar.getTime());
		//获取考勤中心数据
		List<Map<String,Object>> queryCalendarList = new TBMHelper().queryCalendarList(userId,beginDate,endDate);
		//返回数据
		PageResult pageResult = new PageResult();
		pageResult.setData(queryCalendarList);
		return pageResult.toJson();
	}

	/**
	 * 获取考勤中心具体一天的数据
	 */
	@Override
	public String queryCalendarDayDetails(Map<String, Object> param)
			throws BusinessException {
		String userId = param.get("userId").toString(); //
		String datestr = param.get("datestr").toString();// 当前日期
		Map<String, Object> map = new TBMHelper().queryCalendarDayDetails(userId,datestr);
		PageResult result = new PageResult();
		result.setData(map);
		return result.toJson();
	}


	
	//======================提交流程
	
	private static String getOABillTypeCode(String billtype) {
		//请加
		if (("leave".equalsIgnoreCase(billtype))|| ("6404".equalsIgnoreCase(billtype))) {
			return "leaveh_id";
		}
		//补卡
		if (("signcard".equalsIgnoreCase(billtype))|| ("6402".equalsIgnoreCase(billtype))) {
			return "signcard_id";
		}
		//加班
		if (("overtime".equalsIgnoreCase(billtype))|| ("6405".equalsIgnoreCase(billtype))) {
			return "overtimeh_id";
		}
		//出差
		if (("away".equalsIgnoreCase(billtype))|| ("6403".equalsIgnoreCase(billtype))) {
			return "awayh_id";
		}
		if (("leaveoff".equalsIgnoreCase(billtype))|| ("6406".equalsIgnoreCase(billtype))) {
			return "leaveoff_id";
		}
		if (("awayoff".equalsIgnoreCase(billtype))|| ("6407".equalsIgnoreCase(billtype))) {
			return "awayoff_id";
		}
		return billtype;
	}
	
	@Override
	public String queryProcessNodeList(Map<String, Object> param)
			throws BusinessException {
		String pk_psndoc = null;
		String OAFlowType = null;
		String applyDate = null;
			String userId = param.get("userId").toString();
			pk_psndoc = NCLocator.getInstance().lookup(
					IUserPubService.class).queryPsndocByUserid(userId);
			String billtype = param.get("billtype").toString();
			String pk_h = param.get("pk_h").toString();
			if("leave".equals(billtype) ){
				LeavehVO leavevo = new TBMQueryDao().queryByPk(LeavehVO.class, pk_h, false);
				if(leavevo == null) {
					throw new BusinessException(BDVersionValidationUtil.getDelInfo());
				}
				UFLiteralDate apply_date = leavevo.getApply_date();
				applyDate= apply_date.toString();
			} else if("signcard".equals(billtype)){
				SignhVO signhVO = new TBMQueryDao().queryByPk(SignhVO.class, pk_h, false);
				if(signhVO == null) {
					throw new BusinessException(BDVersionValidationUtil.getDelInfo());
				}
				UFLiteralDate apply_date = signhVO.getApply_date();
				applyDate= apply_date.toString();
			} else if("overtime".equals(billtype)){
				OvertimehVO overtimevo = new TBMQueryDao().queryByPk(OvertimehVO.class, pk_h, false);
				if(overtimevo == null) {
					throw new BusinessException(BDVersionValidationUtil.getDelInfo());
				}
				UFLiteralDate apply_date = overtimevo.getApply_date();
				applyDate= apply_date.toString();
			} else if("away".equals(billtype)){
				AwayhVO awayvo = new TBMQueryDao().queryByPk(AwayhVO.class, pk_h, false);
				if(awayvo == null) {
					throw new BusinessException(BDVersionValidationUtil.getDelInfo());
				}
				UFLiteralDate apply_date = awayvo.getApply_date();
				applyDate= apply_date.toString();
			}
			OAFlowType = getOABillTypeCode(billtype);
		PageResult result = new PageResult();
		ArrayList<Object> data = new ArrayList<Object>();
		NC_Itf_SelfWebService service = NCLocator
				.getInstance().lookup(NC_Itf_SelfWebService.class);
		try{
			FlowNodeVO[] oaFlowNode = service.getOAFlowNode(pk_psndoc, OAFlowType, applyDate);
			if(oaFlowNode!=null){
				for(FlowNodeVO flowvo : oaFlowNode) {
					HashMap<String, Object> dt1 = new HashMap<String,Object>();
					dt1.put("pk_node", flowvo.getNodeId());
					dt1.put("node_name", flowvo.getNodeName());
					dt1.put("node_type", flowvo.getNodeType());
					dt1.put("pk_flow", flowvo.getFlowID());
					List<Map<String,Object>> userlist = new ArrayList<Map<String,Object>>(); 
					dt1.put("operators", userlist);
					OperatorVO[] operators = flowvo.getOperators();
					if(operators!=null){
						for(OperatorVO operator :operators){
							HashMap<String, Object> userinfo = new HashMap<String,Object>();
							userinfo.put("user_id", operator.getUserId());
							userinfo.put("user_name", operator.getUserName());
							userinfo.put("org_name", operator.getSubcompany());
							userinfo.put("dept_name", operator.getDepartment());
							userinfo.put("post_name", operator.getPostion());
							userlist.add(userinfo);
						}
					}
					data.add(dt1);
				}
			}
		} catch(Exception e){
			e.printStackTrace();
			throw new BusinessException("获取流程节点失败"+e.getMessage());
		}
		result.setData(data);
		return result.toJson();
	}
	@Override
	public String queryApproverList(Map<String, Object> param)
			throws BusinessException {
		String searchKey = null;
		if(param.get("searchKey")!=null && !"".equals(param.get("searchKey"))){
			searchKey = param.get("searchKey").toString();
		} else {
			throw new BusinessException("搜索关键字不能为空");
		}
		NC_Itf_SelfWebService service = NCLocator
				.getInstance().lookup(NC_Itf_SelfWebService.class);
		ArrayList<Object> data = new ArrayList<Object>();
		try{
			OperatorVO[] oaOperator = service.getOAOperator(searchKey);
			if(oaOperator!=null){
				for(OperatorVO operator : oaOperator) {
					HashMap<String, Object> dt1 = new HashMap<String,Object>();
					dt1.put("user_id", operator.getUserId());
					dt1.put("user_name", operator.getUserName());
					dt1.put("org_name", operator.getSubcompany());
					dt1.put("dept_name", operator.getDepartment());
					dt1.put("post_name", operator.getPostion());
					data.add(dt1);
				}
			}
		} catch(Exception e){
			e.printStackTrace();
			throw new BusinessException("获取人员列表失败"+e.getMessage());
		}
		PageResult result = new PageResult();
		result.setData(data);
		return result.toJson();
	}
	/**
	 *  提交表单
	 * @param param
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public String submitBill(Map<String, Object> param)
			throws BusinessException {
		Logger.error("submitBill11==> 开始");
		HashMap<String, String> hashMap = new HashMap<String, String>();
		String pf = "";
		try{
			String userId = param.get("userId").toString();
			String pk_psndoc = NCLocator.getInstance().lookup(
					IUserPubService.class).queryPsndocByUserid(userId);
			String billtype = param.get("billtype").toString();
			String pk_h = param.get("billKey").toString();
			
			String applyDate = null;
			Logger.error("submitBill==>1111 ");
			if("leave".equals(billtype) ){
				LeavehVO leavevo = new TBMQueryDao().queryByPk(LeavehVO.class, pk_h, false);
				UFLiteralDate apply_date = leavevo.getApply_date();
				applyDate= apply_date.toString();
			} else if("signcard".equals(billtype)){
				SignhVO signhVO = new TBMQueryDao().queryByPk(SignhVO.class, pk_h, false);
				UFLiteralDate apply_date = signhVO.getApply_date();
				applyDate= apply_date.toString();
			} else if("overtime".equals(billtype)){
				//但强 加班申请单的提交与收回操作 2018-2-5 15:50:27
				AggOvertimeVO newVO = NCLocator.getInstance().lookup(IOvertimeApplyQueryMaintain.class).queryByPk(pk_h);
				String pk_org = newVO.getOvertimehVO().getPk_org();
				HashMap<String, String> eParam = new HashMap<String, String>();
				if (isDirectApprove(pk_org, "6405")) {
					eParam.put("nosendmessage", "nosendmessage");
				}
				String opration = param.get("oprationtype").toString();
				if(opration.equals("Commit")){
					PfProcessBatchRetObject validateRetObj = NCLocator.getInstance().lookup(IHrPf.class).submitValidation("Commit","Commit",null,SysInitQuery.getParaInt(pk_org,(String) IHrPf.hashBillTypePara.get("6405")).intValue(),new AggregatedValueObject[] { newVO });
					if ((validateRetObj.getRetObj() == null)
							|| (validateRetObj.getRetObj().length == 0)) {
						String errStr = validateRetObj.getExceptionMsg();
						if (StringUtils.isNotBlank(errStr))
							throw new BusinessException(errStr);
					}
					LfwPfUtil.runAction("SAVE", "6405", newVO, null, null, null, eParam,
							new String[] { userId }, null);
				}else{
					PfProcessBatchRetObject validateRetObj = NCLocator
							.getInstance().lookup(IHrPf.class)
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
					LfwPfUtil.runAction("RECALL", "6405", newVO, null, null, null, eParam,
							new String[] { userId }, null);
				}
				AggOvertimeVO newVO2 = NCLocator.getInstance().lookup(IOvertimeApplyQueryMaintain.class).queryByPk(pk_h);
				OvertimehVO parentVO = (OvertimehVO) newVO2.getParentVO();
				hashMap.put("pk_leaveh", parentVO.getPk_overtimeh());
				hashMap.put("pk_h", parentVO.getPk_overtimeh());
				hashMap.put("bill_code", parentVO.getBill_code());
				hashMap.put("approve_state", parentVO.getApprove_state()+"");
				//但强 加班申请单的提交与收回操作 2018-2-5 15:50:27
			} else if("away".equals(billtype)){
				AwayhVO awayvo = new TBMQueryDao().queryByPk(AwayhVO.class, pk_h, false);
				UFLiteralDate apply_date = awayvo.getApply_date();
				applyDate= apply_date.toString();
			}
		} catch(Exception e){
			e.printStackTrace();
			Logger.error(pf);
			throw new BusinessException("提交参数有误："+e.getMessage());
		}
		
//		NC_Itf_SelfWebService service = ((NC_Itf_SelfWebService) NCLocator
//				.getInstance().lookup(NC_Itf_SelfWebService.class));
		String message = "未知";
		try{
			Logger.error("开始提交：开始");
//			message = service.submitData(hashMap);
			Logger.error("提交成功：消息="+message);
		} catch(Exception e){
			Logger.error("提交报错："+e.getMessage());
			e.printStackTrace();
			throw new BusinessException("OA提交异常："+e.getMessage());
		}
		
		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(hashMap);
		result.setMessage(message+"...");
		Logger.error("submitBill==> 正常结束");
		return result.toJson();
	}

	@Override
	public String queryTranstype(Map<String, Object> param)
			throws BusinessException {
		String userId = param.get("userId").toString();
		String billtype = null;
		if (param.get("billtype") != null && !"".equals(param.get("billtype"))) {
			billtype = param.get("billtype").toString();
		} else {
			billtype = "leave";
		}
		List<Map<String, Object>> data = new TBMQueryDao().queryTranstype(
				userId, billtype);
		PageResult result = new PageResult();
		result.setData(data);
		result.pushDevInfo("param", param);
		return result.toJson();
	}
	
	public boolean isDirectApprove(String pk_org, String billtype)
			throws BusinessException {
		Integer type = SysInitQuery.getParaInt(pk_org,
				(String) IHrPf.hashBillTypePara.get(billtype));
		return (type != null) && (type.intValue() == 0);
	}

	@Override
	public int queryDirectApprove(Map<String, Object> param) throws BusinessException {
		String userId = param.get("userId").toString();
		String billtype = param.get("billtype").toString();
		int approvestyle = 0;
		String pk_h = param.get("pk_h").toString();
		if("leave".equals(billtype) ){
			LeavehVO leavevo = new TBMQueryDao().queryByPk(LeavehVO.class, pk_h, false);
			String pk_org = leavevo.getPk_org();
			String sql = "select value from pub_sysinit where pk_org='" + pk_org+"' and initcode ='" + (String) IHrPf.hashBillTypePara.get("6404") +"'";
			String types = (String) new BaseDAO().executeQuery(sql, new ColumnProcessor());
			Integer type =Integer.valueOf(types==null?"0":types);
			approvestyle = type==null?0:type;
		}else if("overtime".equals(billtype)){
			OvertimehVO overtimevo = new TBMQueryDao().queryByPk(OvertimehVO.class, pk_h, false);
			String pk_org = overtimevo.getPk_org();
			String sql = "select value from pub_sysinit where pk_org='" + pk_org+"' and initcode ='" + (String) IHrPf.hashBillTypePara.get("6405") +"'";
			String types = (String) new BaseDAO().executeQuery(sql, new ColumnProcessor());
			Integer type =Integer.valueOf(types==null?"0":types);
			approvestyle = type==null?0:type;
		}else if("signcard".equals(billtype)){
			SignhVO signvo = new TBMQueryDao().queryByPk(SignhVO.class, pk_h, false);
			String pk_org = signvo.getPk_org();
			String sql = "select value from pub_sysinit where pk_org='" + pk_org+"' and initcode ='" + (String) IHrPf.hashBillTypePara.get("6402") +"'";
			String types = (String) new BaseDAO().executeQuery(sql, new ColumnProcessor());
			Integer type =Integer.valueOf(types==null?"0":types);
			approvestyle = type==null?0:type;
		}else if("away".equals(billtype)){
			AwayhVO away = new TBMQueryDao().queryByPk(AwayhVO.class, pk_h, false);
			String pk_org = away.getPk_org();
			String sql = "select value from pub_sysinit where pk_org='" + pk_org+"' and initcode ='" + (String) IHrPf.hashBillTypePara.get("6403") +"'";
			String types = (String) new BaseDAO().executeQuery(sql, new ColumnProcessor());
			Integer type =Integer.valueOf(types==null?"0":types);
			approvestyle = type==null?0:type;
		}else if("leaveoff".equals(billtype)){
			LeaveoffVO leaveoffvo = new TBMQueryDao().queryByPk(LeaveoffVO.class, pk_h, false);
			String pk_org = leaveoffvo.getPk_org();
			String sql = "select value from pub_sysinit where pk_org='" + pk_org+"' and initcode ='" + (String) IHrPf.hashBillTypePara.get("6406") +"'";
			String types = (String) new BaseDAO().executeQuery(sql, new ColumnProcessor());
			Integer type =Integer.valueOf(types==null?"0":types);
			approvestyle = type==null?0:type;
		}else if("awayoff".equals(billtype)){
			AwayOffVO awayoff = new TBMQueryDao().queryByPk(AwayOffVO.class, pk_h, false);
			String pk_org = awayoff.getPk_org();
			String sql = "select value from pub_sysinit where pk_org='" + pk_org+"' and initcode ='" + (String) IHrPf.hashBillTypePara.get("6407") +"'";
			String types = (String) new BaseDAO().executeQuery(sql, new ColumnProcessor());
			Integer type =Integer.valueOf(types==null?"0":types);
			approvestyle = type==null?0:type;
		}else if("dimission".equals(billtype) || "trns".equals(billtype)){
			AggStapply aggVO = PsnDimissionRefServiceImpl.getTransmngQueryService().queryByPk(pk_h);
			StapplyVO stapplyVO = (StapplyVO) aggVO.getParentVO();
			String pk_org = stapplyVO.getPk_org();
			String sql = "select value from pub_sysinit where pk_org='" + pk_org+"' and initcode ='" + (String) IHrPf.hashBillTypePara.get("6115") +"'";
			String types = (String) new BaseDAO().executeQuery(sql, new ColumnProcessor());
			Integer type =Integer.valueOf(types==null?"0":types);
			approvestyle = type==null?0:type;
		}else if("psnreg".equals(billtype)){ //转正
			AggRegapplyVO aggvo =  new TBMQueryDao().queryByPk(AggRegapplyVO.class, pk_h, false);
			RegapplyVO regvo = (RegapplyVO) aggvo.getParentVO();
			String pk_org = regvo.getPk_org();
			String sql = "select value from pub_sysinit where pk_org='" + pk_org+"' and initcode ='" + (String) IHrPf.hashBillTypePara.get("6111") +"'";
			String types = (String) new BaseDAO().executeQuery(sql, new ColumnProcessor());
			Integer type =Integer.valueOf(types==null?"0":types);
			approvestyle = type==null?0:type;
			
		}
		return approvestyle;
	}
	
	public String doApprove(Map<String, Object> param) throws Exception{
		String userId = param.get("userId").toString();
		String queryByPk = null;
		String pk_h =  param.get("pk_h").toString();
		String billtype =  param.get("billtype").toString();
		if(billtype.equals("leave")){
			String whereSql = " pk_leaveh = '" + pk_h + "' ";
			AggLeaveVO[] aggVOs = NCLocator.getInstance().lookup(ILeaveApplyQueryMaintain.class).queryByWhereSQL(null, whereSql);
			PfProcessBatchRetObject retObj = approveValidate(aggVOs);
			PfUtilClientWeb.runBatchNew( "APPROVE" + userId, "6404", (AggregatedValueObject[])retObj.getRetObj(), null, null, new HashMap(),param);
			//最后调用
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("pk_leaveh", pk_h);
			queryByPk = queryLeaveByPk(map);
		}else if (billtype.equals("overtime")){
			String whereSql = " pk_overtimeh = '" + pk_h + "' ";
			AggOvertimeVO[] aggVOs = NCLocator.getInstance().lookup(IOvertimeApplyQueryMaintain.class).queryByCond(whereSql);
			//如若需要额外审批校验 需要复写approveValidate()方法
			PfProcessBatchRetObject retObj = NCLocator.getInstance().lookup(IHrPf.class).approveValidation("Approve", "Approve", null, aggVOs);
			PfUtilClientWeb.runBatchNew("APPROVE" + userId, "6405", (AggregatedValueObject[])retObj.getRetObj(), null, null, new HashMap(),param);
			//最后调用
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("pk_overtimeh", pk_h);
			queryByPk = queryOvertimeByPk(map);
		}else if (billtype.equals("signcard")){
			String whereSql = " pk_signh = '" + pk_h + "' ";
			AggSignVO[] aggVOs = NCLocator.getInstance().lookup(ISignCardApplyQueryMaintain.class).queryByCond(whereSql);
			//如若需要额外审批校验 需要复写approveValidate()方法
			PfProcessBatchRetObject retObj = NCLocator.getInstance().lookup(IHrPf.class).approveValidation("Approve", "Approve", null, aggVOs);
			PfUtilClientWeb.runBatchNew( "APPROVE" + userId, "6402", (AggregatedValueObject[])retObj.getRetObj(), null, null, new HashMap(),param);
			//最后调用
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("pk_signh", pk_h);
			queryByPk = querySignCardByPk(map);
		}else if (billtype.equals("away")){
			String[] whereSql = new String[]{pk_h};
			AggAwayVO[] aggVOs = NCLocator.getInstance().lookup(IAwayApplyQueryMaintain.class).queryByPks(whereSql);
			//如若需要额外审批校验 需要复写approveValidate()方法
			PfProcessBatchRetObject retObj = NCLocator.getInstance().lookup(IHrPf.class).approveValidation("Approve", "Approve", null, aggVOs);
			PfUtilClientWeb.runBatchNew( "APPROVE" + userId, "6403", (AggregatedValueObject[])retObj.getRetObj(), null, null, new HashMap(),param);
			//最后调用
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("pk_awayh", pk_h);
			queryByPk = queryAwayByPK(map);
		}else if(billtype.equals("leaveoff")){
			AggLeaveoffVO aggVO = NCLocator.getInstance().lookup(ILeaveOffApplyQueryMaintain.class).queryByPk(pk_h);
			PfProcessBatchRetObject retObj = NCLocator.getInstance().lookup(IHrPf.class).approveValidation("Approve", "Approve", null, new AggLeaveoffVO[]{aggVO});
			PfUtilClientWeb.runBatchNew( "APPROVE" + userId, "6406", (AggregatedValueObject[])retObj.getRetObj(), null, null, new HashMap(),param);
			//最后调用
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("pk_leaveoff", pk_h);
			queryByPk = NCLocator.getInstance().lookup(ITBMOffService.class).queryLeaveoffByPk(map);
		}else if (billtype.equals("awayoff")){
			AggAwayOffVO aggVO = NCLocator.getInstance().lookup(IAwayOffApplyQueryMaintain.class).queryByPk(pk_h);
			//如若需要额外审批校验 需要复写approveValidate()方法
			PfProcessBatchRetObject retObj = NCLocator.getInstance().lookup(IHrPf.class).approveValidation("Approve", "Approve", null, new AggAwayOffVO[]{aggVO});
			PfUtilClientWeb.runBatchNew( "APPROVE" + userId, "6407", (AggregatedValueObject[])retObj.getRetObj(), null, null, new HashMap(),param);
			//最后调用
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("pk_awayoff", pk_h);
			queryByPk = NCLocator.getInstance().lookup(ITBMOffService.class).queryAwayoffByPk(map);
		}else if (billtype.equals("psnreg")){
			AggRegapplyVO aggVO = NCLocator.getInstance().lookup(IRegmngQueryService.class).queryByPk(pk_h);
			//如若需要额外审批校验 需要复写approveValidate()方法
			PfProcessBatchRetObject retObj = NCLocator.getInstance().lookup(IHrPf.class).approveValidation("Approve", "Approve", null, new AggRegapplyVO[]{aggVO});
			PfUtilClientWeb.runBatchNew( "APPROVE" + userId, "6111", (AggregatedValueObject[])retObj.getRetObj(), null, null, new HashMap(),param);
			//最后调用
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("pk_hi_regapply", pk_h);
			queryByPk = NCLocator.getInstance().lookup(IPsnRegService.class).queryPsnRegByPk(map);
		}else if (billtype.equals("dimission")){
			AggStapply aggVO = PsnDimissionRefServiceImpl.getTransmngQueryService().queryByPk(pk_h);
			//如若需要额外审批校验 需要复写approveValidate()方法
			PfProcessBatchRetObject retObj = NCLocator.getInstance().lookup(IHrPf.class).approveValidation("Approve", "Approve", null, new AggStapply[]{aggVO});
			PfUtilClientWeb.runBatchNew( "APPROVE" + userId, "6115", (AggregatedValueObject[])retObj.getRetObj(), null, null, new HashMap(),param);
			//最后调用
			param.put("pk_hi_stapply", pk_h);
			Map<String, Object> map= NCLocator.getInstance().lookup(IPsnDimissionRefService.class).queryDimissionBill(param);
			PageResult pageResult = new PageResult();
			pageResult.setData(map);
			queryByPk = pageResult.toJson();
		}else if (billtype.equals("trns")){
			AggStapply aggVO = PsnDimissionRefServiceImpl.getTransmngQueryService().queryByPk(pk_h); // 后续更改  不能和离职公用一套查询
			//如若需要额外审批校验 需要复写approveValidate()方法
			PfProcessBatchRetObject retObj = NCLocator.getInstance().lookup(IHrPf.class).approveValidation("Approve", "Approve", null, new AggStapply[]{aggVO});
			PfUtilClientWeb.runBatchNew( "APPROVE" + userId, "6113", (AggregatedValueObject[])retObj.getRetObj(), null, null, new HashMap(),param);
			//最后调用
			param.put("pk_hi_stapply", pk_h);
			Map<String, Object> map= NCLocator.getInstance().lookup(IPsnDimissionRefService.class).queryDimissionBill(param);
			PageResult pageResult = new PageResult();
			pageResult.setData(map);
			queryByPk = pageResult.toJson();
		}
		return queryByPk;
	}
	//查询queryAwayByPK
	public String queryOvertimeByPk(Map<String, Object> param)
			throws BusinessException {
		String pk_overtimeh = param.get("pk_overtimeh").toString();
		Map<String, Object> overtime = new TBMOvertimeDao().queryOvertimeByPK(pk_overtimeh);
		PageResult result = new PageResult();
		result.setData(overtime);
		result.pushDevInfo("param", param);
		return result.toJson();
	}
	public String queryAwayByPK(Map<String, Object> param)
			throws BusinessException {
		String pk_awayh = param.get("pk_awayh").toString();
		Map<String, Object> awayh = new TBMAwayDao().queryAwayByPK(pk_awayh);
		PageResult result = new PageResult();
		result.setData(awayh);
		result.pushDevInfo("param", param);
		return result.toJson();
	}
	
	//查询
	public String querySignCardByPk(Map<String, Object> param)
			throws BusinessException {
		String pk_signh = param.get("pk_signh").toString();
		Map<String, Object> signh = new TBMSigncardDao().querySigncardByPK(pk_signh);
		PageResult result = new PageResult();
		result.setData(signh);
		result.pushDevInfo("param", param);
		return result.toJson();
	}
	private PfProcessBatchRetObject approveValidate(AggregatedValueObject[] aggVOs) throws BusinessException {
		
		String pk_org = ((LeavehVO)aggVOs[0].getParentVO()).getPk_org();

		LeaveCheckLengthResult checkLength = NCLocator
				.getInstance().lookup(ILeaveApplyQueryMaintain.class)
				.checkLength(pk_org, (AggLeaveVO[]) aggVOs);
		if (null == checkLength)
			return NCLocator.getInstance().lookup(IHrPf.class)
					.approveValidation("Approve", "Approve", null, aggVOs);
		;
		String psnNames = checkLength.getPsnNames();
		List<AggLeaveVO> continueList = new ArrayList();
		boolean selected = false;
		if (StringUtils.isNotBlank(psnNames)) {
			String msg = ResHelper.getString("6017leave", "06017leave0259")
					+ psnNames;
		}
		if (selected) {
			List<String> billcodeList = checkLength.getBillcodeList();
			for (AggLeaveVO aggvo : (AggLeaveVO[]) aggVOs) {
				LeavehVO headVO = aggvo.getHeadVO();
				if (!billcodeList.contains(headVO.getBill_code())) {
					continueList.add(aggvo);
				}
			}
			if (CollectionUtils.isEmpty(continueList)) {
				throw new BusinessException(ResHelper.getString("6017overtime",
						"06017overtime0055"));
			}
			aggVOs = ((AggregatedValueObject[]) continueList
					.toArray(new AggLeaveVO[0]));
		}
		return NCLocator.getInstance().lookup(IHrPf.class)
				.approveValidation("Approve", "Approve", null, aggVOs);

	}

	@Override
	public String doBatchApprove(Map<String, Object> param) throws Exception {
		ArrayList list = (ArrayList) param.get("info");
		String userId = param.get("userId").toString();
		String billactive = param.get("billactive").toString();
		String workflownotes = param.get("workflownotes").toString();
		StringBuffer infobuffer = new StringBuffer();
		for(int i=0;i<list.size();i++){
			Map maplist = (Map) list.get(i);
			String pk_h = (String) maplist.get("pk_h");
			String billtype = (String) maplist.get("billtype");
			Map<String, Object> paramresult = new HashMap<String,Object>();
			paramresult.put("pk_h", pk_h);
			paramresult.put("billtype", billtype);
			paramresult.put("workflownotes", workflownotes);
			paramresult.put("billactive", billactive);
			paramresult.put("userId", userId);
			if(billtype.equals("leave")){
				String whereSql = " pk_leaveh = '" + pk_h + "' ";
				AggLeaveVO[] aggVOs = NCLocator.getInstance().lookup(ILeaveApplyQueryMaintain.class).queryByWhereSQL(null, whereSql);
				PfProcessBatchRetObject retObj = approveValidate(aggVOs);
				PfUtilClientWeb.runBatchNew( "APPROVE" + userId, "6404", (AggregatedValueObject[])retObj.getRetObj(), null, null, new HashMap(),paramresult);
			}else if (billtype.equals("overtime")){
				String whereSql = " pk_overtimeh = '" + pk_h + "' ";
				AggOvertimeVO[] aggVOs = NCLocator.getInstance().lookup(IOvertimeApplyQueryMaintain.class).queryByCond(whereSql);
				//如若需要额外审批校验 需要复写approveValidate()方法
				PfProcessBatchRetObject retObj = NCLocator.getInstance().lookup(IHrPf.class).approveValidation("Approve", "Approve", null, aggVOs);
				PfUtilClientWeb.runBatchNew("APPROVE" + userId, "6405", (AggregatedValueObject[])retObj.getRetObj(), null, null, new HashMap(),paramresult);
			}else if (billtype.equals("signcard")){
				String whereSql = " pk_signh = '" + pk_h + "' ";
				AggSignVO[] aggVOs = NCLocator.getInstance().lookup(ISignCardApplyQueryMaintain.class).queryByCond(whereSql);
				//如若需要额外审批校验 需要复写approveValidate()方法
				PfProcessBatchRetObject retObj = NCLocator.getInstance().lookup(IHrPf.class).approveValidation("Approve", "Approve", null, aggVOs);
				PfUtilClientWeb.runBatchNew( "APPROVE" + userId, "6402", (AggregatedValueObject[])retObj.getRetObj(), null, null, new HashMap(),paramresult);
			}else if (billtype.equals("away")){
				String[] whereSql = new String[]{pk_h};
				AggAwayVO[] aggVOs = NCLocator.getInstance().lookup(IAwayApplyQueryMaintain.class).queryByPks(whereSql);
				//如若需要额外审批校验 需要复写approveValidate()方法
				PfProcessBatchRetObject retObj = NCLocator.getInstance().lookup(IHrPf.class).approveValidation("Approve", "Approve", null, aggVOs);
				PfUtilClientWeb.runBatchNew( "APPROVE" + userId, "6403", (AggregatedValueObject[])retObj.getRetObj(), null, null, new HashMap(),paramresult);
			}else if (billtype.equals("leaveoff")){
				String[] whereSql = new String[]{pk_h};
				AggLeaveoffVO[] aggVOs = NCLocator.getInstance().lookup(ILeaveOffApplyQueryMaintain.class).queryByPks(whereSql);
				PfProcessBatchRetObject retObj = NCLocator.getInstance().lookup(IHrPf.class).approveValidation("Approve", "Approve", null, aggVOs);
				PfUtilClientWeb.runBatchNew( "APPROVE" + userId, "6406", (AggregatedValueObject[])retObj.getRetObj(), null, null, new HashMap(),paramresult);
			}else if (billtype.equals("awayoff")){
				String[] whereSql = new String[]{pk_h};
				AggAwayOffVO[] aggVOs = NCLocator.getInstance().lookup(IAwayOffApplyQueryMaintain.class).queryByPks(whereSql);
				PfProcessBatchRetObject retObj = NCLocator.getInstance().lookup(IHrPf.class).approveValidation("Approve", "Approve", null, aggVOs);
				PfUtilClientWeb.runBatchNew( "APPROVE" + userId, "6407", (AggregatedValueObject[])retObj.getRetObj(), null, null, new HashMap(),paramresult);
			}
		}
		return "1";
	}

	/**
	 * 请假打印信息查询
	 */
	@Override
	public String leavePrintTemplate(Map<String, Object> param)
			throws Exception {
		String pk_leaveh = (String)param.get("id");
		TBMQueryDao dao = new TBMQueryDao();
		Map<String, Object> leaveTemp = dao.leavePrintTemplate(pk_leaveh);
		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(leaveTemp);
		return result.toJson();
	}

	/**
	 * 校验单据类别是否为产假，产检假，以及病假
	 */
	@Override
	public String checkBillType(Map<String, Object> param) throws Exception {
		String pk_h =  param.get("pk_h").toString();
		String wheresql = " pk_leaveh = '" + pk_h + "'";
		AggLeaveVO[] aggVOs = NCLocator.getInstance().lookup(ILeaveApplyQueryMaintain.class).queryByWhereSQL(null, wheresql);
		AggLeaveVO aggvo = new AggLeaveVO();
		if(aggVOs!=null && aggVOs.length==1){
			aggvo = aggVOs[0];
		}
		String billType = ((LeavehVO)aggvo.getParentVO()).getPk_leavetype();
		if(billType.equals("1002Z710000000021ZLD")||billType.equals("1002Z710000000021ZLH")||billType.equals("1001V610000000001I7Q")){
			return "Y";
		}else{
			return "N";
		}
	}

	@Override
	public Map<String, Object> checkTimeBrokenLeave(Map<String, Object> param)
			throws BusinessException {
		Map<String, Object> leavehMap = (Map<String, Object>) param
				.get("leavehMap");
		List<Map<String, Object>> bList = (List<Map<String, Object>>) param
				.get("bList");
		String userId = param.get("userId").toString();
		AggLeaveVO aggVO = getLeaveFromMap(leavehMap, bList, userId, null);
		Map<String, Object> checkresult = new HashMap<String, Object>();
		ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
		String cantsave = "0"; // 0 允许保存 1 不允许保存
		try {
			DefaultValidationService vService = new DefaultValidationService();
			vService.addValidator(new PFSaveLeaveValidator());
			vService.validate(aggVO);
			LeaveCheckResult<AggLeaveVO> checkResult = ((ILeaveApplyQueryMaintain) NCLocator.getInstance().lookup(ILeaveApplyQueryMaintain.class)).checkWhenSave(aggVO);

			Map<String, Map<Integer, ITimeScopeWithBillInfo[]>> check = checkResult.getMutexCheckResult();
			if ((check != null) && (MapUtils.isNotEmpty(check))) {
				list = new TBMAwayDao().getBrokenList(check);
			}
		} catch (BillMutexException bme) {
			Map<String, Map<Integer, ITimeScopeWithBillInfo[]>> result = bme.getMutexBillsMap();
			if (result != null) {
				list = new TBMAwayDao().getBrokenList(result);
			}
		}
		if(list!=null){
			for(Map<String, String> map : list){
				String string = map.get("billinit");
				if(string.equals("休假登记")||string.equals("休假申请")){
					cantsave = "1";
				}
			}
			checkresult.put("timebroken", list);
		}
		checkresult.put("cantsave", cantsave);
		return checkresult;
	}
	/**
	 * 获取用户参照
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@Override
	public String queryCpUser(Map<String, Object> param) throws Exception {
		Integer pageno = null;
		if (param.get("pageno") != null && !"".equals(param.get("pageno"))) {
			pageno = Integer.parseInt(param.get("pageno").toString());
		}
		Integer pagesize = null;
		if (param.get("pagesize") != null && !"".equals(param.get("pagesize"))) {
			pagesize = Integer.parseInt(param.get("pagesize").toString());
		}
		String value = (String) param.get("value");
		String pk_hrorg = getMainOrg();
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT U .cuserid,U .user_name,org.pk_org pk_org ,org.name org_name, ");
		sb.append(" nvl(dept.name,' ') dept_name, ");
		sb.append(" nvl(post.postname,' ') post_name, ");
		sb.append(" psn.name psn_name,");
		sb.append(" CASE WHEN job.pk_hrorg='" + pk_hrorg
				+ "' THEN 'Y' else 'N' END as defaultFlag  ");
		sb.append(" FROM SM_USER U ");
		sb.append(" INNER join bd_psndoc psn on psn.pk_psndoc=u.pk_base_doc ");
		sb.append(" INNER JOIN hi_psnjob job on job.pk_psndoc=psn.pk_psndoc and job.ismainjob='Y' and job.lastflag='Y'");
		sb.append(" LEFT JOIN ORG_ORGS org ON job.pk_org =org.pk_org ");
		sb.append(" LEFT join org_dept dept on job.pk_dept = dept.pk_dept ");
		sb.append(" LEFT join om_post post on job.pk_post = post.pk_post ");
		sb.append(" WHERE 1=1 ");
		if (StringUtils.isNotEmpty(value)) {
			try {
				value = URLDecoder.decode(value, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			sb.append(" and ( ");
			sb.append(" psn.code like '%" + value.toString() + "%' ");
			sb.append(" or psn.name like '%" + value.toString() + "%' ");
			sb.append(" or psn.SHORTNAME like '%" + value.toString() + "%' ");
			sb.append(" or dept.name like '%" + value.toString() + "%' ");
			sb.append(" or dept.code like '%" + value.toString() + "%' ");
			sb.append(" or dept.SHORTNAME like '%" + value.toString() + "%' ");
			sb.append(" or org.name like '%" + value.toString() + "%' ");
			sb.append(" or org.code like '%" + value.toString() + "%' ");
			sb.append(" or org.SHORTNAME like '%" + value.toString() + "%' ");
			sb.append(" )");
		}
		sb.append(" order by defaultFlag desc, org.code,u.cuserid ");

		PageResult result = new PageResult();
		if (pageno == null || pagesize == null) {
			List<Map<String, Object>> data = (List<Map<String, Object>>) new BaseDAO()
					.executeQuery(sb.toString(), new MapListProcessor());
			result.setData(data);
		} else {
			int queryTotalCount = SaasCommonHelper.queryTotalCount(sb.toString());
			String querystrSQL = SaasCommonHelper.processPageSqlNew(sb.toString(),
					pageno, pagesize);
			List<Map<String, Object>> data = (List<Map<String, Object>>) new BaseDAO()
					.executeQuery(querystrSQL, new MapListProcessor());
			result.setData(data);
			result.setTotalCount(queryTotalCount);
			result.setPageno(pageno);
		}
		result.pushDevInfo("param", param);
		return result.toJson();
	}
	
	/**
	 * 当前登陆人主任职组织
	 * 
	 * @return
	 * @throws DAOException
	 */
	private String getMainOrg() throws DAOException {
		String pk_psndoc = SaasCommonHelper.getPsnIdByUser(PubEnv.getPk_user());
		PsndocXYDao dao = new PsndocXYDao();
		Map psnInfo = dao.queryMainJobInfo(pk_psndoc);
		if (psnInfo != null) {
			String hrorg = psnInfo.get("pk_hrorg").toString();
			return hrorg;
		}
		return "";
	}
	/**
	 * 发送消息
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@Override
	public String sendMessage(Map<String, Object> param) throws Exception {
		//但强测试发送消息
		String pk_h  = param.get("pk_h").toString();
		String userid = param.get("cuserid").toString();
		String billtype = param.get("billtype").toString();
		String detail = "";
		if(billtype.equals("leave")){
			detail = pk_h+"@6404cp";
		}else if(billtype.equals("overtime")){
			detail = pk_h+"@6405cp";
		}else if(billtype.equals("away")){
			detail = pk_h+"@6403cp";
		}else if(billtype.equals("signcard")){
			detail = pk_h+"@6402cp";
		}else if(billtype.equals("leaveoff")){
			detail = pk_h+"@6406cp"; 
		}else if(billtype.equals("awayoff")){
			detail = pk_h+"@6407cp";
		}else if(billtype.equals("certify")){
			detail = pk_h+"@6888cp";
		}else if(billtype.equals("psnreg")){//add by wt 20191107 case:增加人事单据的抄送
			detail = pk_h+"@6111cp";
		}else if(billtype.equals("trns")){
			detail = pk_h+"@6113cp";
		}else if(billtype.equals("dimission")){
			detail = pk_h+"@6115cp";
		}else{
			throw new BusinessException("不能抄送该类别消息！");
		}
		try {
			MessageCenter.sendMessage(getMessages(userid,detail));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "success";
	}
	/**
	 * 
	 * @param receiver
	 * @return
	 */
	private NCMessage[] getMessages(String receiver,String detail) {
		
		String[] receuser = receiver.split(",");
		NCMessage[] mess = new NCMessage[receuser.length];
		String senderpk = PubEnv.getPk_user();
		for(int i=0;i<receuser.length;i++){
			NCMessage message = new NCMessage();
			MessageVO msgvo = new MessageVO();
			msgvo.setSubject("证明管理通知");
			msgvo.setSender(senderpk);
			msgvo.setReceiver(receuser[i]);
			msgvo.setContent("审批抄送消息，请查阅");
			msgvo.setMsgsourcetype("worklist");
			msgvo.setMsgtype("nc");
			msgvo.setDetail(detail);
			msgvo.setSendtime(new UFDateTime());
			msgvo.setSubcolor(String.valueOf(MessageCenterUIConst.MCUNREADCOLOR
					.getRGB()));
			msgvo.setPriority(2);
			msgvo.setReceipt("Y");
			message.setMessage(msgvo);
			mess[i] = message;
		}
		return mess;
	}
/**
 * add by wangtian1
 * 查询人员参照
 */
	@Override
	public String queryPsnodc(Map<String, Object> param) throws Exception {
		Integer pageno = null;
		if (param.get("pageno") != null && !"".equals(param.get("pageno"))) {
			pageno = Integer.parseInt(param.get("pageno").toString());
		}
		Integer pagesize = null;
		if (param.get("pagesize") != null && !"".equals(param.get("pagesize"))) {
			pagesize = Integer.parseInt(param.get("pagesize").toString());
		}
		String value = (String) param.get("value");
		String pk_hrorg = getMainOrg();
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT psn.pk_psndoc,psn.name psn_name,org.pk_org pk_org ,org.name org_name, ");
		sb.append(" nvl(dept.name,' ') dept_name, ");
		sb.append(" nvl(post.postname,' ') post_name, ");
//		sb.append(" psn.name psn_name,");
		sb.append(" CASE WHEN job.pk_hrorg='" + pk_hrorg
				+ "' THEN 'Y' else 'N' END as defaultFlag  ");
		sb.append(" FROM bd_psndoc psn ");
//		sb.append(" INNER join bd_psndoc psn on psn.pk_psndoc=u.pk_base_doc ");
		sb.append(" INNER JOIN hi_psnjob job on job.pk_psndoc=psn.pk_psndoc and job.ismainjob='Y' and job.lastflag='Y'");
		sb.append(" LEFT JOIN ORG_ORGS org ON job.pk_org =org.pk_org ");
		sb.append(" LEFT join org_dept dept on job.pk_dept = dept.pk_dept ");
		sb.append(" LEFT join om_post post on job.pk_post = post.pk_post ");
		sb.append(" WHERE 1=1 ");
		if (StringUtils.isNotEmpty(value)) {
			try {
				value = URLDecoder.decode(value, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			sb.append(" and ( ");
			sb.append(" psn.code like '%" + value.toString() + "%' ");
			sb.append(" or psn.name like '%" + value.toString() + "%' ");
			sb.append(" or psn.SHORTNAME like '%" + value.toString() + "%' ");
			sb.append(" or dept.name like '%" + value.toString() + "%' ");
			sb.append(" or dept.code like '%" + value.toString() + "%' ");
			sb.append(" or dept.SHORTNAME like '%" + value.toString() + "%' ");
			sb.append(" or org.name like '%" + value.toString() + "%' ");
			sb.append(" or org.code like '%" + value.toString() + "%' ");
			sb.append(" or org.SHORTNAME like '%" + value.toString() + "%' ");
			sb.append(" )");
		}
		sb.append(" order by defaultFlag desc, org.code,psn.pk_psndoc ");

		PageResult result = new PageResult();
		if (pageno == null || pagesize == null) {
			List<Map<String, Object>> data = (List<Map<String, Object>>) new BaseDAO()
					.executeQuery(sb.toString(), new MapListProcessor());
			result.setData(data);
		} else {
			int queryTotalCount = SaasCommonHelper.queryTotalCount(sb.toString());
			String querystrSQL = SaasCommonHelper.processPageSqlNew(sb.toString(),
					pageno, pagesize);
			List<Map<String, Object>> data = (List<Map<String, Object>>) new BaseDAO()
					.executeQuery(querystrSQL, new MapListProcessor());
			result.setData(data);
			result.setTotalCount(queryTotalCount);
			result.setPageno(pageno);
		}
		result.pushDevInfo("param", param);
		return result.toJson();
	}
}
