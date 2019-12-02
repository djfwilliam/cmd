package nc.impl.saas.hi;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.bs.uif2.validation.DefaultValidationService;
import nc.hr.utils.PubEnv;
import nc.hr.utils.ResHelper;
import nc.itf.hr.frame.IPersistenceRetrieve;
import nc.itf.om.IDeptQueryService;
import nc.itf.om.IOrgInfoQueryService;
import nc.itf.saas.ITBMOvertimeService;
import nc.itf.saas.pub.PageResult;
import nc.itf.saas.pub.SaasMapUtils;
import nc.itf.ta.IOvertimeAppInfoDisplayer;
import nc.itf.ta.IOvertimeApplyApproveManageMaintain;
import nc.itf.ta.IOvertimeApplyQueryMaintain;
import nc.itf.ta.ITBMPsndocQueryMaintain;
import nc.itf.ta.ITimeItemQueryService;
import nc.itf.ta.ITimeRuleQueryService;
import nc.itf.ta.algorithm.ITimeScopeWithBillInfo;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.pubitf.rbac.IUserPubService;
import nc.vo.hi.psndoc.PsnJobVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.ta.PublicLangRes;
import nc.vo.ta.bill.BillMutexException;
import nc.vo.ta.overtime.AggOvertimeVO;
import nc.vo.ta.overtime.OvertimebVO;
import nc.vo.ta.overtime.OvertimehVO;
import nc.vo.ta.overtime.pf.validator.PFSaveOvertimeValidator;
import nc.vo.ta.psndoc.TBMPsndocVO;
import nc.vo.ta.timeitem.OverTimeTypeCopyVO;
import nc.vo.ta.timerule.TimeRuleVO;
import nc.vo.ta.wf.pub.TaWorkFlowManager;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;

public class TBMOvertimeServiceImpl implements ITBMOvertimeService {
	/**
	 * 加班参照
	 */
	@Override
	public List<Map<String, Object>> getOvertimeRef(String pk_org)
			throws DAOException {
		return new TBMOvertimeDao().getOvertimeRef(pk_org);
	}

	/**
	 * 查询加班申请单据
	 */
	@Override
	public Map<String, Object> queryOvertimeByPK(String pk_overtimeh)
			throws BusinessException {
		return new TBMOvertimeDao().queryOvertimeByPK(pk_overtimeh);
	}

	/**
	 * 保存加班申请单据
	 */
	@Override
	public String saveOvertime(Map<String, Object> param)
			throws BusinessException {
		Map<String, Object> overtimeMap = (Map<String, Object>) param
				.get("overtimehMap");
		List<Map<String, Object>> bList = (List<Map<String, Object>>) param
				.get("bList");
		String userId = param.get("userId").toString();
		AggOvertimeVO aggVo = getOvertimeFormMap(overtimeMap, bList, userId,
				null);
		AggOvertimeVO newAggVo = new TBMOvertimeDao().saveOvertime(overtimeMap,
				bList, userId, "Y", aggVo);
		Map<String, Object> resMap=new HashMap<String,Object>();
		resMap.put("pk_overtimeh",((OvertimehVO) newAggVo.getParentVO()).getPk_overtimeh());
		resMap.put("bill_code",((OvertimehVO) newAggVo.getParentVO()).getBill_code());
		return JSONObject.toJSONString(resMap);
	}

	/**
	 * 提交加班申请
	 * 
	 * @param param
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public String submitOvertime(Map<String, Object> param)
			throws BusinessException {
		String userId = param.get("userId").toString();
		String pk_awayh = param.get("pk_overtimeh").toString();
		String whereSql = " pk_overtimeh = '" + pk_awayh + "' ";
		AggOvertimeVO[] aggVOs = ((IOvertimeApplyQueryMaintain) NCLocator
				.getInstance()
				.lookup(IOvertimeApplyApproveManageMaintain.class))
				.queryByCond(whereSql);
		AggOvertimeVO aggOvertimeVO = aggVOs[0];
		Map<String, Object> submitOvertime = new TBMOvertimeDao()
				.submitOvertime(userId, aggOvertimeVO);
		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(submitOvertime);
		return result.toJson();
	}

	/**
	 * 保存并提交出差审批单
	 * 
	 * @param param
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public String saveAndSubmitOvertime(Map<String, Object> param)
			throws BusinessException {
		Map<String, Object> overtimehMap = (Map<String, Object>) param
				.get("overtimehMap");
		List<Map<String, Object>> bList = (List<Map<String, Object>>) param
				.get("bList");
		String userId = param.get("userId").toString();
		AggOvertimeVO aggOvertimeVO = getOvertimeFormMap(overtimehMap, bList,
				userId, null);
		TBMOvertimeDao overtimeDao = new TBMOvertimeDao();
		AggOvertimeVO newAggVo = overtimeDao.saveOvertime(overtimehMap, bList,
				userId, "Y", aggOvertimeVO);
		Map<String, Object> submitOvertime = overtimeDao.submitOvertime(userId,
				newAggVo);
		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(submitOvertime);
		return result.toJson();
	}

	/**
	 * 删除一个加班申請单
	 * 
	 * @param pk_leaveh
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public String deleteOvertime(Map<String, Object> param)
			throws BusinessException {
		// String userid = param.get("userId").toString();
		String pk_overtimeh = param.get("pk_overtimeh").toString();
		Map<String, Object> result = new HashMap();
		AggOvertimeVO aggVO = (AggOvertimeVO) ((IOvertimeApplyQueryMaintain) NCLocator
				.getInstance().lookup(IOvertimeApplyQueryMaintain.class))
				.queryByPk(pk_overtimeh);
		deleteBeforcheckState(aggVO.getOvertimehVO().getApprove_state());
		// initGroup(aggVO.getLeavehVO().getPk_group());
		((IOvertimeApplyApproveManageMaintain) NCLocator.getInstance().lookup(
				IOvertimeApplyApproveManageMaintain.class)).deleteData(aggVO);
		result.put("flag", "2");
		PageResult pageResult = new PageResult();
		pageResult.setData(result);
		return pageResult.toJson();
	}

	@Override
	public Map<String, Object> calculateOvertime(Map<String, Object> param)
			throws BusinessException {
		Map<String, Object> overtimehMap = (Map<String, Object>) param
				.get("overtimehMap");
		List<Map<String, Object>> bList = (List<Map<String, Object>>) param
				.get("bList");
		String userid = param.get("userId").toString();
		AggOvertimeVO aggvo = getOvertimeFormMap(overtimehMap, bList, userid,
				null);
		try {
			aggvo = (AggOvertimeVO) ((IOvertimeAppInfoDisplayer) NCLocator
					.getInstance().lookup(IOvertimeAppInfoDisplayer.class))
					.calculate(aggvo, TimeZone.getDefault());
		} catch (Exception e) {
			Logger.error("移动端加班时长计算报错啦------" + e.getMessage(), e);
			throw new BusinessException(e.getMessage(), e);
		}
		OvertimehVO headVO = aggvo.getHeadVO();
		TimeRuleVO timeRulevo = ((ITimeRuleQueryService) NCLocator
				.getInstance().lookup(ITimeRuleQueryService.class))
				.queryByOrg(headVO.getPk_org());
		DecimalFormat dcmFmt = TBMHelper.getDecimalFormat(timeRulevo
				.getTimedecimal());

		OverTimeTypeCopyVO[] overtimecopys = ((ITimeItemQueryService) NCLocator
				.getInstance().lookup(ITimeItemQueryService.class))
				.queryOvertimeCopyTypesByOrg(headVO.getPk_org(),
						"pk_timeitemcopy = '" + headVO.getPk_overtimetypecopy()
								+ "' ");
		OverTimeTypeCopyVO typeVO = overtimecopys[0];
		String unit = (0 == typeVO.getTimeItemUnit() ? PublicLangRes.DAY()
				: PublicLangRes.HOUR());
		
		List<Map<String, Object>> otbList = new ArrayList();
		overtimehMap.put("blist", otbList);
		OvertimebVO[] overtimebVOs = aggvo.getOvertimebVOs();
		DecimalFormat decimalFormat = getDecimalFormat(aggvo.getOvertimehVO().getPk_org());
		//前端要求传过来什么就返回什么。
		for(int i = 0; i<bList.size();i++){
			OvertimebVO bvo = overtimebVOs[i];
			Map<String, Object> bMap = bList.get(i);
			otbList.add(bMap);
			bMap.put("length", new UFDouble(decimalFormat.format(bvo.getOvertimehour())).toString());
			bMap.put("unit", unit);
		}
		/*for (OvertimebVO bvo : overtimebVOs) {
			//去除删除掉的bvo
			if (bvo.getStatus()!=3) {
				Map<String, Object> bMap = new HashMap();
				
				bMap.put("pk_overtimeb", bvo.getPk_overtimeb());
				bMap.put("begintime", bvo.getOvertimebegintime().toString());
				bMap.put("endtime", bvo.getOvertimeendtime().toString());
				UFDouble ufDouble = new UFDouble(decimalFormat.format(bvo
						.getOvertimehour()));
				bMap.put("length", ufDouble.toString());
				bMap.put("isneedcheck", bvo.getIsneedcheck().toString());
				bMap.put("overtimealready", bvo.getOvertimealready().toString());
				bMap.put("overtimeremark", bvo.getOvertimeremark());
				bMap.put("unit", unit);
			}
		}*/
		Map<String, Object> res=new HashMap<>();
		res.put("sumhour", aggvo.getHeadVO().getSumhour());
		res.put("bList", otbList);
		res.put("unit", unit);
		return res;
	}

	private void deleteBeforcheckState(Integer pfsate) throws BusinessException {
		if (-1 != pfsate.intValue()) {
			throw new BusinessException(ResHelper.getString("6017hrta",
					"06017hrta0089"));
		}
	}

	private AggOvertimeVO getOvertimeFormMap(Map<String, Object> overtimehMap,
			List<Map<String, Object>> bList, String userid,
			OverTimeTypeCopyVO typeVO) throws BusinessException {
		if (MapUtils.isEmpty(overtimehMap)) {
			return null;
		}

		String pk_psndoc = ((IUserPubService) NCLocator.getInstance().lookup(
				IUserPubService.class)).queryPsndocByUserid(userid);
		if (StringUtils.isBlank(pk_psndoc)) {
			throw new BusinessException(ResHelper.getString("6017mobile",
					"06017mytime000001"));
		}

		TBMPsndocVO latestVO = ((ITBMPsndocQueryMaintain) NCLocator
				.getInstance().lookup(ITBMPsndocQueryMaintain.class))
				.queryByPsndocAndDateTime(pk_psndoc, new UFDateTime());
		if (latestVO == null) {
			throw new BusinessException(ResHelper.getString("6017mobile",
					"06017mytime000002"));
		}
		String pk_timeitemcopy = (String) overtimehMap.get("pk_timeitemcopy");
		String pk_org = latestVO.getPk_org();
		String pk_group = latestVO.getPk_group();
		initGroup(pk_group);
		if (typeVO == null) {
			OverTimeTypeCopyVO[] overtimeCopyTypes = ((ITimeItemQueryService) NCLocator
					.getInstance().lookup(ITimeItemQueryService.class))
					.queryOvertimeCopyTypesByOrg(pk_org, "pk_timeitemcopy = '"
							+ pk_timeitemcopy + "' ");

			if (ArrayUtils.isEmpty(overtimeCopyTypes)) {
				throw new BusinessException(ResHelper.getString("6017mobile",
						"06017mytime000053"));
			}
			typeVO = overtimeCopyTypes[0];
		}
		AggOvertimeVO aggVO = new AggOvertimeVO();
		// 申请单
		OvertimehVO mainVO = new OvertimehVO();
		AggOvertimeVO oldAggVO=null;
		if (StringUtils.isNotBlank((String) overtimehMap.get("pk_overtimeh"))) {
			oldAggVO = ((IOvertimeApplyQueryMaintain) NCLocator.getInstance()
					.lookup(IOvertimeApplyQueryMaintain.class))
					.queryByPk((String) overtimehMap.get("pk_overtimeh"));
			//mainVO.setTs(oldAggVO.getHeadVO().getTs());
			mainVO.setCreationtime(oldAggVO.getHeadVO().getCreationtime());
			mainVO.setTs(overtimehMap.get("ts") == null ? new UFDateTime(): new UFDateTime((String) overtimehMap.get("ts")));
			mainVO.setBill_code(oldAggVO.getHeadVO().getBill_code());
		}else {
			mainVO.setTs(overtimehMap.get("ts") == null ? new UFDateTime(): new UFDateTime((String) overtimehMap.get("ts")));
			mainVO.setCreationtime(overtimehMap.get("creationtime") == null ? new UFDateTime(): new UFDateTime((String) overtimehMap.get("creationtime")));
		}
		
		UFLiteralDate newdate = new UFLiteralDate();
		mainVO.setApply_date(newdate);
		mainVO.setBillmaker(userid);
		mainVO.setApprove_state(Integer.valueOf(-1));

		mainVO.setTranstypeid((String) overtimehMap.get("transtypeid"));
		//但强 给transtype赋值 2018-4-2 16:35:34 
		mainVO.setTranstypeid(overtimehMap.get("transtypeid") == null ? "" : overtimehMap.get("transtypeid").toString());
		if(overtimehMap.get("transtypeid") != null){
			String transtypesql = "select pk_billtypecode from bd_billtype where pk_billtypeid = '" + overtimehMap.get("transtypeid").toString() + "'";
			mainVO.setTranstype((String)(new BaseDAO().executeQuery(transtypesql, new ColumnProcessor())));
		}
		
		String pk_overtimeh = overtimehMap.get("pk_overtimeh") == null ? null
				: (String) overtimehMap.get("pk_overtimeh");
		mainVO.setPk_overtimeh(pk_overtimeh);

		mainVO.setTs(overtimehMap.get("ts") == null ? new UFDateTime()
				: new UFDateTime((String) overtimehMap.get("ts")));
		mainVO.setCreationtime(overtimehMap.get("creationtime") == null ? new UFDateTime()
				: new UFDateTime((String) overtimehMap.get("creationtime")));
		mainVO.setCreator(userid);

		aggVO.setParentVO(mainVO);
		mainVO.setPk_org(pk_org);
		mainVO.setPk_group(pk_group);
		mainVO.setBillmaker(userid);
		String pk_psnorg = latestVO.getPk_psnorg();
		mainVO.setPk_psnorg(pk_psnorg);
		mainVO.setPk_psndoc(pk_psndoc);
		String pk_psnjob = latestVO.getPk_psnjob();
		mainVO.setPk_psnjob(pk_psnjob);
		String pk_timeitem = typeVO.getPk_timeitem();
		mainVO.setPk_overtimetype(pk_timeitem);
		mainVO.setPk_overtimetypecopy(pk_timeitemcopy);
		
		mainVO.setSumhour(SaasMapUtils.getDoubleValue(overtimehMap,"sumhour"));
		mainVO.setPk_billtype("6405");
		mainVO.setIshrssbill(UFBoolean.TRUE);
		//加班原因
//TODO
//		mainVO.setOvertimereason((String)overtimehMap.get("overtimereason"));
		//部门
		mainVO.setAttributeValue("creatordept", new TBMQueryDao().getDeptnameByPsndocAndDateTime(pk_psndoc,new UFDateTime())); //所属部门
		PsnJobVO jobVO = (PsnJobVO) ((IPersistenceRetrieve) NCLocator
				.getInstance().lookup(IPersistenceRetrieve.class))
				.retrieveByPk(null, PsnJobVO.class, mainVO.getPk_psnjob());

		String pk_org_v = ((IOrgInfoQueryService) NCLocator.getInstance()
				.lookup(IOrgInfoQueryService.class)).getOrgVid(
				jobVO.getPk_org(), new UFDate());
		mainVO.setPk_org_v(pk_org_v);

		String pk_dept_v = ((IDeptQueryService) NCLocator.getInstance().lookup(
				IDeptQueryService.class)).getDeptVid(jobVO.getPk_dept(),
				new UFDate());
		mainVO.setPk_dept_v(pk_dept_v);
		//add by wt begin
		String pk_project = overtimehMap.get("pk_project") == null ? null
				: (String) overtimehMap.get("pk_project");
//		mainVO.setPk_project(pk_project);
		//add by wt end
		if (CollectionUtils.isEmpty(bList)) {
			return aggVO;
		}

		//OvertimebVO[] bvos = new OvertimebVO[bList.size()];
		List<OvertimebVO> newbvos=new ArrayList<OvertimebVO>();
		for (Map<String, Object> bmap : bList) {
			OvertimebVO bvo = new OvertimebVO();
			String pk_overtimeb = (String) bmap.get("pk_overtimeb");
			if (StringUtils.isBlank(pk_overtimeb)) {
				bvo.setStatus(2);
			} else {
				bvo.setStatus(1);
			}
			bvo.setPk_overtimeb(pk_overtimeb);
			bvo.setPk_overtimeh(pk_overtimeh);
//			bvos[i] = bvo;
			newbvos.add(bvo);
			bvo.setPk_psnorg(pk_psnorg);
			bvo.setPk_org(pk_org);
			bvo.setPk_group(pk_group);
			bvo.setPk_psndoc(pk_psndoc);
			bvo.setPk_psnjob(pk_psnjob);
			bvo.setPk_overtimetype(pk_timeitem);
			bvo.setPk_overtimetypecopy(pk_timeitemcopy);
			bvo.setOvertimehour(new UFDouble(bmap.get("length")==null?"0":bmap.get("length").toString()));
			bvo.setOvertimeremark((String) bmap.get("overtimeremark"));
			//处理ts
			if (StringUtils.isBlank(pk_overtimeb)) {
				bvo.setStatus(2);
			} else {
				bvo.setStatus(1);
				bvo.setTs(bmap.get("ts") == null ? new UFDateTime(): new UFDateTime((String) bmap.get("ts")));
			}
			bvo.setOvertimebegintime(new UFDateTime((String) bmap
					.get("begintime")));
			bvo.setOvertimebegindate(new UFLiteralDate((String) bmap
					.get("begintime")));
			bvo.setOvertimeendtime(new UFDateTime((String) bmap.get("endtime")));
			bvo.setOvertimeenddate(new UFLiteralDate((String) bmap
					.get("endtime")));
			String check = (String) bmap.get("isneedcheck");
			bvo.setIsneedcheck(StringUtils.isBlank(check) ? UFBoolean.TRUE
					: UFBoolean.valueOf(check));
			String str = (String) bmap.get("overtimealready");
			bvo.setOvertimealready(StringUtils.isBlank(str) ? UFDouble.ZERO_DBL
					: new UFDouble(str));
//TODO
//			bvo.setOvertimesubsidy(SaasMapUtils.getStringValue(bmap,"overtimesubsidy"));
			bvo.setDeduct(Integer.valueOf(0));
			if (bvo.getActhour() == null) {
				bvo.setActhour(bvo.getOvertimehour());
			}
		}
		
		if (oldAggVO != null && oldAggVO.getBodyVOs() != null) {// 计算被删除的子集
			OvertimebVO[] bodyVOs = oldAggVO.getBodyVOs();
			for (OvertimebVO oldbvo : bodyVOs) {
				boolean isdelete = true;
				for (OvertimebVO newbvo : newbvos) {
					if (newbvo.getPk_overtimeb() != null
							&& oldbvo.getPk_overtimeb().equals(newbvo.getPk_overtimeb())) {
						isdelete = false;
						break;
					}
				}
				if (isdelete) {
					OvertimebVO newbvo = (OvertimebVO) oldbvo.clone();
					newbvo.setStatus(3);
					newbvos.add(newbvo);
				}
			}
		}
		OvertimebVO[] bodyvos = new OvertimebVO[newbvos.size()];
		newbvos.toArray(bodyvos);
		aggVO.setChildrenVO(bodyvos);
		return aggVO;
	}

	private void initGroup(String pk_group) {
		String groupId = InvocationInfoProxy.getInstance().getGroupId();
		if (StringUtils.isBlank(groupId)) {
			InvocationInfoProxy.getInstance().setGroupId(pk_group);
		}
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
	
	private String[] generateBillCodes2(String pk_org, String billtype,
			String codeField, Class<?> clz, int count) throws BusinessException {
		if (count == 0) {
			return null;
		}
		String prefix = null;
		if (("overtime".equalsIgnoreCase(billtype))
				|| ("6405".equalsIgnoreCase(billtype))) {
			prefix = "jb" + billtype + PubEnv.getServerDate().toStdString();
		} else if (("away".equalsIgnoreCase(billtype))
				|| ("6403".equalsIgnoreCase(billtype))) {
			prefix = "cc" + billtype + PubEnv.getServerDate().toStdString();
		} else if (("leave".equalsIgnoreCase(billtype))
				|| ("6404".equalsIgnoreCase(billtype))) {
			prefix = "xj" + billtype + PubEnv.getServerDate().toStdString();
		} else if (("sign".equalsIgnoreCase(billtype))
				|| ("6402".equalsIgnoreCase(billtype))) {
			prefix = "qk" + billtype + PubEnv.getServerDate().toStdString();
		}
		String flowCode = TaWorkFlowManager.getFlowCode(prefix, codeField, clz);
		String[] billCodes = new String[count];
		for (int i = 0; i < count; i++) {
			billCodes[i] = (prefix + "_" + TBMHelper.getFlowCode(flowCode, i));
		}
		return billCodes;
	}

	/**
	 * 加班待遇参照
	 * @return
	 */
	@Override
	public String getOvertimesubsidy(Map<String, Object> param)
			throws BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 加班申请单打印查询
	 */
	@Override
	public String overtimePrintTemplate(Map<String, Object> param)
			throws BusinessException {
		String pk_overtimeh = (String)param.get("id");
		TBMOvertimeDao dao = new TBMOvertimeDao();
		Map<String, Object> overtimeTemp = dao.awayPrintTemplate(pk_overtimeh);
		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(overtimeTemp);
		return result.toJson();
	}

	/**
	 * 校验时长冲突
	 */
	@Override
	public Map<String, Object> checkTimeBrokenOvertime(Map<String, Object> param)
			throws BusinessException {
		Map<String, Object> overtimeMap = (Map<String, Object>) param
				.get("overtimehMap");
		List<Map<String, Object>> bList = (List<Map<String, Object>>) param
				.get("bList");
		String userId = param.get("userId").toString();
		AggOvertimeVO aggvo = getOvertimeFormMap(overtimeMap, bList, userId,
				null);
		DefaultValidationService vService = new DefaultValidationService();
		vService.addValidator(new PFSaveOvertimeValidator());
		vService.validate(aggvo);
		Map<String, Object> checkresult = new HashMap<String, Object>();
		ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
		String cantsave = "0"; // 0 允许保存 1 不允许保存
		try{
			IOvertimeApplyQueryMaintain maintain = NCLocator.getInstance().lookup(IOvertimeApplyQueryMaintain.class);
			Map<String, Map<Integer, ITimeScopeWithBillInfo[]>> check = maintain.check(aggvo);
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
				if(string.equals("加班登记")||string.equals("加班申请")){
					cantsave = "1";
				}
			}
			checkresult.put("timebroken", list);
		}
		checkresult.put("cantsave", cantsave);
		return checkresult;
	}
	
}
