package nc.impl.saas.hi;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.hr.utils.PubEnv;
import nc.hr.utils.ResHelper;
import nc.itf.hr.pf.IHrPf;
import nc.itf.om.IDeptQueryService;
import nc.itf.om.IOrgInfoQueryService;
import nc.itf.saas.ITBMAwayService;
import nc.itf.saas.pub.PageResult;
import nc.itf.ta.IAwayAppInfoDisplayer;
import nc.itf.ta.IAwayApplyApproveManageMaintain;
import nc.itf.ta.IAwayApplyQueryMaintain;
import nc.itf.ta.ITBMPsndocQueryMaintain;
import nc.itf.ta.ITimeItemQueryService;
import nc.itf.ta.ITimeRuleQueryService;
import nc.itf.ta.algorithm.ITimeScopeWithBillInfo;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.pubitf.para.SysInitQuery;
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
import nc.vo.ta.away.AggAwayVO;
import nc.vo.ta.away.AwaybVO;
import nc.vo.ta.away.AwayhVO;
import nc.vo.ta.bill.BillMutexException;
import nc.vo.ta.psndoc.TBMPsndocVO;
import nc.vo.ta.timeitem.AwayTypeCopyVO;
import nc.vo.ta.timerule.TimeRuleVO;
import nc.vo.ta.wf.pub.TaWorkFlowManager;
import nc.vo.trade.pub.IBillStatus;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class TBMAwayServiceImpl implements ITBMAwayService {

	/**
	 * 出差参照
	 */
	@Override
	public List queryAwayRef(String pk_org) throws DAOException {
		return new TBMAwayDao().queryAwayRef(pk_org);
	}

	/**
	 * 查询出差审批单
	 * 
	 * @param pk_awayh
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public Map<String, Object> queryAwayByPK(String pk_awayh)
			throws BusinessException {
		return new TBMAwayDao().queryAwayByPK(pk_awayh);
	}

	/**
	 * 保存出差审批单
	 */
	@Override
	public Map<String, Object> saveAway(Map<String, Object> param)
			throws BusinessException {
		Map<String, Object> awayhMap = (Map<String, Object>) param
				.get("awayhMap");
		List<Map<String, Object>> bList = (List<Map<String, Object>>) param
				.get("bList");
		String userId = param.get("userId").toString();
		//封装城aggvo
		AggAwayVO aggVo = getAwayVOFormMap(awayhMap, bList, userId, null);
		//重新计算时长
		calculateMerge(aggVo);
		//校验
		//String warningMessage = null;
		try {
			Map<String, Map<Integer, ITimeScopeWithBillInfo[]>> check = NCLocator.getInstance().lookup(IAwayApplyQueryMaintain.class).check(aggVo);
			if ((check != null) && (MapUtils.isNotEmpty(check))) {
				throw new BusinessException(ResHelper.getString("6017leave", "06017leave0195"));
				//warningMessage = ResHelper.getString("6017leave", "06017leave0195");
			}
		} catch (BillMutexException bme) {
			Map<String, Map<Integer, ITimeScopeWithBillInfo[]>> result = bme.getMutexBillsMap();
			if (result != null) {
				throw new BusinessException("操作单据与其他单据有时间冲突，操作失败！");
				//throw new BusinessException(ResHelper.getString("6017leave", "06017leave0195"));
			}
			if (StringUtils.isNotBlank(bme.getMessage())) {
				throw new BusinessException("操作单据与其他单据有时间冲突，操作失败！");
				//throw new BusinessException(bme.getMessage());
			}
		}
		IAwayApplyApproveManageMaintain awayApplyApprove = (IAwayApplyApproveManageMaintain) NCLocator
				.getInstance().lookup(IAwayApplyApproveManageMaintain.class);
		AggAwayVO newAggVo = null;
		if ( aggVo.getAwayhVO().getPk_awayh() == null){
			// 在此生成表单bill_code
			String newBill_code = TBMHelper.getBillCode("6403",aggVo.getHeadVO().getPk_group(),aggVo.getHeadVO().getPk_org());
			aggVo.getHeadVO().setBill_code(newBill_code);
			try{
				newAggVo = awayApplyApprove.insertData(aggVo);
			} catch(BusinessException e){
				TBMHelper.rollbackBillCode("6403",aggVo.getHeadVO().getPk_group(),aggVo.getHeadVO().getPk_org(),newBill_code);
				throw new BusinessException(e.getMessage());
			}
		} else {
			newAggVo = awayApplyApprove.updateData(aggVo);
		}
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("pk_awayh", ((AwayhVO) newAggVo.getParentVO()).getPk_awayh());
		res.put("bill_code", ((AwayhVO) newAggVo.getParentVO()).getBill_code());
		return res;
	}

	/**
	 * 提交出差审批单
	 * 
	 * @param param
	 * @return
	 * @throws BusinessException
	 */
	/********************************tianxx5**************************************/
	@Override
	public String submitAway(Map<String, Object> param)
			throws BusinessException {
		String pk_awayh = param.get("pk_awayh").toString();
		TBMAwayDao dao = new TBMAwayDao();
		AggAwayVO aggVO = dao.queryAwayApplyByPK(pk_awayh);
		Map<String, Object> submitAway = dao.submitAway(aggVO);
		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(submitAway);
		return result.toJson();
	}
	/********************************tianxx5**************************************/

	/**
	 * 保存并提交出差审批单
	 * 
	 * @param param
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public String saveAndSubmitAway(Map<String, Object> param)
			throws BusinessException {

		String pk_awayh = (String) param.get("billKey");
		String billtype = (String) param.get("billtype");
		TBMAwayDao dao = new TBMAwayDao();
		AggAwayVO aggVO = dao.queryAwayApplyByPK(pk_awayh);
		Map<String, Object> submitAway = dao.submitAway(aggVO);
		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(submitAway);
 		return result.toJson();
	}

	/**
	 * 删除或收回一个出差申請单
	 * 
	 * @param pk_leaveh
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public String deleteAway(Map<String, Object> param)
			throws BusinessException {
		PageResult pageResult = new PageResult();
		String pk_awayh = param.get("pk_awayh").toString();
		Map<String, Object> result = new HashMap();
		AggAwayVO aggVO = (AggAwayVO) ((IAwayApplyQueryMaintain) NCLocator
				.getInstance().lookup(IAwayApplyQueryMaintain.class))
				.queryByPk(pk_awayh);
		deleteBeforcheckState(aggVO.getAwayhVO().getApprove_state());
		((IAwayApplyApproveManageMaintain) NCLocator.getInstance().lookup(
				IAwayApplyApproveManageMaintain.class)).deleteData(aggVO);
		result.put("flag", "2");
		pageResult.setData(result);
		return pageResult.toJson();
	}

	/**
	 * 计算出差天数
	 */

	@Override
	public Map<String, Object> calculateAway(Map<String, Object> param)
			throws BusinessException {
		Map<String, Object> awayhMap = (Map<String, Object>) param
				.get("awayhMap");
		List<Map<String, Object>> bList = (List<Map<String, Object>>) param
				.get("bList");
		String userid = param.get("userId").toString();
		AggAwayVO aggvo = getAwayVOFormMap(awayhMap, bList,userid, null);
		calculateMerge(aggvo);
		List<Map<String, Object>> awaybList = new ArrayList<Map<String, Object>>();
		awayhMap.put("blist", awaybList);
		String pk_awaytypecopy = (String) awayhMap.get("pk_timeitemcopy");
		AwayTypeCopyVO[] awayCopyTypes = ((ITimeItemQueryService) NCLocator
				.getInstance().lookup(ITimeItemQueryService.class))
				.queryAwayCopyTypesByOrg(aggvo.getAwayhVO().getPk_org(),
						"pk_timeitemcopy = '" + pk_awaytypecopy + "' ");

		AwayTypeCopyVO copyvo = awayCopyTypes[0];
		AwaybVO[] awaybVOs = aggvo.getAwaybVOs();
		String unit = 0 == copyvo.getTimeItemUnit() ? PublicLangRes.DAY()
				: PublicLangRes.HOUR();
		DecimalFormat decimalFormat = getDecimalFormat(aggvo.getAwayhVO()
				.getPk_org());
		int j = 0;
		for (Map<String, Object> bmap : bList) {
			AwaybVO bvo = awaybVOs[j];
			bmap.put("awayhour",new UFDouble(decimalFormat.format(bvo.getAwayhour())).toString());
			bmap.put("unit", unit);
			awaybList.add(bmap);
			j++;
		}
		HashMap<String, Object> res = new HashMap<String, Object>();
		res.put("unit", unit);
		res.put("bList", awaybList);
		res.put("sumhour",
				new UFDouble(decimalFormat.format(aggvo.getHeadVO()
						.getSumhour())).toString());
		return res;
	}
	/**
	 * 计算时长 返回aggvo
	 * 
	 * @param aggvo
	 * @return
	 * @throws BusinessException
	 */
	private AggAwayVO calculateMerge(AggAwayVO aggvo) throws BusinessException {
		if (aggvo == null)
			return aggvo;
		AwayhVO mainVO = aggvo.getAwayhVO();
		PsnJobVO psnjob = (PsnJobVO) new BaseDAO().retrieveByPK(PsnJobVO.class, mainVO.getPk_psnjob());
		mainVO.setPk_psnorg(psnjob.getPk_psnorg());
		TBMPsndocVO latestVO = ((ITBMPsndocQueryMaintain) NCLocator.getInstance().lookup(ITBMPsndocQueryMaintain.class))
				.queryByPsndocAndDateTime(mainVO.getPk_psndoc(), new UFDateTime());
		if (latestVO == null) {
			mainVO.setSumhour(UFDouble.ZERO_DBL);
			AwaybVO[] awaybVOs = aggvo.getAwaybVOs();
			for(int i = 0,j = ArrayUtils.getLength(awaybVOs);i < j;i++){
				awaybVOs[i].setAwayhour(UFDouble.ZERO_DBL);
			}
		} else {
			mainVO.setPk_org(latestVO.getPk_org());
			aggvo = NCLocator.getInstance().lookup(IAwayAppInfoDisplayer.class).calculate(aggvo, TimeZone.getDefault());
		}
		return aggvo;
	}
	private void deleteBeforcheckState(Integer pfsate) throws BusinessException {
		if (-1 != pfsate.intValue()) {
			throw new BusinessException(ResHelper.getString("6017hrta",
					"06017hrta0089"));
		}
	}

	private AggAwayVO getAwayVOFormMap(Map<String, Object> awayhMap,
			List<Map<String, Object>> bList, String userid,
			AwayTypeCopyVO typeVO) throws BusinessException {
		if (MapUtils.isEmpty(awayhMap)) {
			return null;
		}

		String pk_psndoc = ((IUserPubService) NCLocator.getInstance().lookup(
				IUserPubService.class)).queryPsndocByUserid(userid);
		if (StringUtils.isBlank(pk_psndoc)) {
			throw new BusinessException(ResHelper.getString("6017mobile", "06017mytime000001"));
		}

		AggAwayVO aggVO = new AggAwayVO();
		AwayhVO mainVO = new AwayhVO();
		aggVO.setParentVO(mainVO);
		AggAwayVO oldAggVO = null;
		mainVO.setTs(awayhMap.get("ts") == null ? new UFDateTime(): new UFDateTime((String) awayhMap.get("ts")));
		UFLiteralDate apply_date = new UFLiteralDate();
		if (StringUtils.isNotBlank((String) awayhMap.get("pk_awayh"))) {
			oldAggVO = ((IAwayApplyQueryMaintain) NCLocator.getInstance().lookup(IAwayApplyQueryMaintain.class)).queryByPk((String) awayhMap.get("pk_awayh"));
			mainVO.setCreationtime(oldAggVO.getHeadVO().getCreationtime());
			mainVO.setBill_code(oldAggVO.getHeadVO().getBill_code());
			apply_date = oldAggVO.getHeadVO().getApply_date();
		}else {
			mainVO.setCreationtime(awayhMap.get("creationtime") == null ? new UFDateTime(): new UFDateTime((String) awayhMap.get("creationtime")));
		}
		
		TBMPsndocVO latestVO = ((ITBMPsndocQueryMaintain) NCLocator.getInstance().lookup(ITBMPsndocQueryMaintain.class)).queryByPsndocAndDate(pk_psndoc, apply_date);
				//.queryByPsndocAndDateTime(pk_psndoc, apply_date);
		if (latestVO == null) {
			throw new BusinessException(ResHelper.getString("6017mobile", "06017mytime000002"));
		}
		String pk_awaytypecopy = (String) awayhMap.get("pk_timeitemcopy");
		String pk_org = latestVO.getPk_org();
		String pk_group = latestVO.getPk_group();
		initGroup(pk_group);
		if (typeVO == null) {
			AwayTypeCopyVO[] awayCopyTypes = ((ITimeItemQueryService) NCLocator.getInstance().lookup(ITimeItemQueryService.class))
					.queryAwayCopyTypesByOrg(pk_org, "pk_timeitemcopy = '" + pk_awaytypecopy + "' ");
			if (ArrayUtils.isEmpty(awayCopyTypes))
				throw new BusinessException(ResHelper.getString("6017hrta", "06017hrta0091"));
			typeVO = awayCopyTypes[0];
		}
		mainVO.setApply_date(new UFLiteralDate());
		mainVO.setBillmaker(userid);
		mainVO.setApprove_state(IBillStatus.FREE);
		String transtypeid = (String) awayhMap.get("transtypeid");
		if(!StringUtils.isEmpty(transtypeid) && !"~".equals(transtypeid)){
			String querySQL = "select pk_billtypecode transtype from bd_billtype where pk_billtypeid=?";
			SQLParameter para = new SQLParameter();
			para.addParam(transtypeid);
			String transtype = (String) new BaseDAO().executeQuery(querySQL, para, new ColumnProcessor());
			mainVO.setTranstypeid(transtypeid);
			mainVO.setTranstype(transtype);
		}else{
			mainVO.setTranstypeid("~");
			mainVO.setTranstype("~");
		}
		
		String pk_awayh = (String) awayhMap.get("pk_awayh");
		mainVO.setPk_awayh(pk_awayh);
		if (StringUtils.isNotBlank((String) awayhMap.get("sumhour"))) {
			mainVO.setSumhour(new UFDouble(Double.valueOf((String) awayhMap.get("sumhour"))));
		}
		mainVO.setCreator(userid);
		mainVO.setPk_org(pk_org);
		mainVO.setPk_group(pk_group);
		String pk_timeitem = typeVO.getPk_timeitem();
		mainVO.setPk_awaytype(pk_timeitem);
		mainVO.setPk_awaytypecopy(pk_awaytypecopy);
		mainVO.setPk_billtype("6403");
		mainVO.setIshrssbill(UFBoolean.TRUE);
		mainVO.setAwayremark((String) awayhMap.get("awayremark")); //出差描述
		/** 合并单据调整，不能出主表人员信息了 */
		String pk_psnorg = latestVO.getPk_psnorg();
		mainVO.setPk_psnorg(pk_psnorg);
		mainVO.setPk_psndoc(pk_psndoc);
		String pk_psnjob = latestVO.getPk_psnjob();
		mainVO.setPk_psnjob(pk_psnjob);
		PsnJobVO jobVO = (PsnJobVO) new BaseDAO().retrieveByPK(PsnJobVO.class, mainVO.getPk_psnjob());
		String pk_org_v = ((IOrgInfoQueryService) NCLocator.getInstance().lookup(IOrgInfoQueryService.class)).getOrgVid(jobVO.getPk_org(), new UFDate());
		mainVO.setPk_org_v(pk_org_v);

		String pk_dept_v = ((IDeptQueryService) NCLocator.getInstance().lookup(
				IDeptQueryService.class)).getDeptVid(jobVO.getPk_dept(),
				new UFDate());
		mainVO.setPk_dept_v(pk_dept_v);
		//add by wt 20190821 begin 
//		mainVO.setPk_project((String) awayhMap.get("pk_project"));
		// 处理时间段
		if (CollectionUtils.isNotEmpty(bList)) {
			List<AwaybVO> bvoList=new ArrayList<AwaybVO>();
			for (Map<String, Object> bmap : bList) {
				AwaybVO bvo = new AwaybVO();
				String pk_awayb = (String) bmap.get("pk_awayb");
				if (StringUtils.isBlank(pk_awayb)) {
					bvo.setStatus(VOStatus.NEW);
				} else {
					bvo.setStatus(VOStatus.UPDATED);
					bvo.setTs(bmap.get("ts") == null ? new UFDateTime(): new UFDateTime((String) bmap.get("ts")));
				}
				bvo.setPk_awayb(pk_awayb);
				bvo.setPk_awayh(pk_awayh);
				bvo.setPk_org(pk_org);
				bvo.setPk_group(pk_group);
				bvo.setPk_psndoc(pk_psndoc);
				bvo.setPk_psnorg(pk_psnorg);
				bvo.setPk_psnjob(pk_psnjob);
				bvo.setPk_awaytype(pk_timeitem);
				bvo.setPk_awaytypecopy(pk_awaytypecopy);
				bvo.setAwayhour(new UFDouble((String) bmap.get("awayhour")));
				bvo.setAwaybegintime(new UFDateTime((String) bmap.get("begintime")));
				bvo.setAwaybegindate(new UFLiteralDate((String) bmap.get("begintime")));
				bvo.setAwayendtime(new UFDateTime((String) bmap.get("endtime")));
				bvo.setAwayenddate(new UFLiteralDate((String) bmap.get("endtime")));
				bvo.setAwayaddress((String) bmap.get("awayaddress"));
				bvo.setAwayremark((String) bmap.get("awayremark"));
				bvo.setWorkprocess((String) bmap.get("workprocess"));
				bvoList.add(bvo);
			}
			if (oldAggVO != null && oldAggVO.getBodyVOs() != null) {// 计算被删除的子集
				AwaybVO[] bodyVOs = oldAggVO.getBodyVOs();
				for (AwaybVO bvo : bodyVOs) {
					boolean isdelete = true;
					for (AwaybVO bvo2 : bvoList) {
						if (bvo2.getPk_awayb() != null && bvo.getPk_awayb().equals(bvo2.getPk_awayb())) {
							isdelete = false;
							break;
						}
					}
					if (isdelete) {
						bvo.setStatus(VOStatus.DELETED);
						bvoList.add(bvo);
					}
				}
			}
			aggVO.setChildrenVO(bvoList.toArray(new AwaybVO[0]));
		}
		aggVO = NCLocator.getInstance().lookup(IAwayAppInfoDisplayer.class).calculate(aggVO, TimeZone.getDefault());
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

	public boolean isDirectApprove(String pk_org, String billtype)
			throws BusinessException {
		Integer type = SysInitQuery.getParaInt(pk_org,
				(String) IHrPf.hashBillTypePara.get(billtype));
		return (type != null) && (type.intValue() == 0);
	}

	/**
	 * 收回一条单据
	 */
	@Override
	public String callbackAway(Map<String, Object> param)
			throws BusinessException {
		String pk_awayh = (String)param.get("billKey");
		TBMAwayDao dao = new TBMAwayDao();
		AggAwayVO aggVO = dao.queryAwayApplyByPK(pk_awayh);
		Map<String, Object> rollbackAway = dao.rollbackAway(aggVO);
		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(rollbackAway);
		return result.toJson();
	}
	/**
	 * 出差打印模板查询
	 */
	@Override
	public String awayPrintTemplate(Map<String, Object> param)
			throws BusinessException {
		String pk_awayh = (String)param.get("id");
		TBMAwayDao dao = new TBMAwayDao();
		Map<String, Object> awayTemp = dao.awayPrintTemplate(pk_awayh);
		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(awayTemp);
		return result.toJson();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> checkTimeBrokenAway(Map<String, Object> param)
			throws BusinessException {
		Map<String, Object> awayhMap = (Map<String, Object>) param
				.get("awayhMap");
		List<Map<String, Object>> bList = (List<Map<String, Object>>) param
				.get("bList");
		String userId = param.get("userId").toString();
		//封装城aggvo
		AggAwayVO aggVo = getAwayVOFormMap(awayhMap, bList, userId, null);
		//重新计算时长
		calculateMerge(aggVo);
		Map<String, Object> checkresult = new HashMap<String, Object>();
		ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
		String cantsave = "0"; // 0 允许保存 1 不允许保存
		try {
			Map<String, Map<Integer, ITimeScopeWithBillInfo[]>> check = NCLocator.getInstance().lookup(IAwayApplyQueryMaintain.class).check(aggVo);
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
				if(string.equals("出差登记")||string.equals("出差申请")){
					cantsave = "1";
				}
			}
			checkresult.put("timebroken", list);
		}
		checkresult.put("cantsave", cantsave);
		return checkresult;
	}
	@Override
	public Map<String, Object> saveAwayWithTimeBroken(Map<String, Object> param)
			throws BusinessException {

		Map<String, Object> awayhMap = (Map<String, Object>) param
				.get("awayhMap");
		List<Map<String, Object>> bList = (List<Map<String, Object>>) param
				.get("bList");
		String userId = param.get("userId").toString();
		//封装城aggvo
		AggAwayVO aggVo = getAwayVOFormMap(awayhMap, bList, userId, null);
		//重新计算时长
		calculateMerge(aggVo);
		IAwayApplyApproveManageMaintain awayApplyApprove = (IAwayApplyApproveManageMaintain) NCLocator
				.getInstance().lookup(IAwayApplyApproveManageMaintain.class);
		AggAwayVO newAggVo = null;
		if ( aggVo.getAwayhVO().getPk_awayh() == null){
			// 在此生成表单bill_code
			String newBill_code = TBMHelper.getBillCode("6403",aggVo.getHeadVO().getPk_group(),aggVo.getHeadVO().getPk_org());
			aggVo.getHeadVO().setBill_code(newBill_code);
			try{
				newAggVo = awayApplyApprove.insertData(aggVo);
			} catch(BusinessException e){
				TBMHelper.rollbackBillCode("6403",aggVo.getHeadVO().getPk_group(),aggVo.getHeadVO().getPk_org(),newBill_code);
				throw new BusinessException(e.getMessage());
			}
		} else {
			newAggVo = awayApplyApprove.updateData(aggVo);
		}
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("pk_awayh", ((AwayhVO) newAggVo.getParentVO()).getPk_awayh());
		res.put("bill_code", ((AwayhVO) newAggVo.getParentVO()).getBill_code());
		return res;
	
	}
}
