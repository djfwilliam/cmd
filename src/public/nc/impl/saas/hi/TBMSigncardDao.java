package nc.impl.saas.hi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.jdbc.framework.processor.MapProcessor;
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.hr.utils.CommonUtils;
import nc.hr.utils.MultiLangHelper;
import nc.hr.utils.PubEnv;
import nc.hr.utils.ResHelper;
import nc.itf.bd.defdoc.IDefdocQryService;
import nc.itf.hr.frame.IHrBillCode;
import nc.itf.hr.frame.IPersistenceRetrieve;
import nc.itf.hr.pf.IHrPf;
import nc.itf.om.IDeptQueryService;
import nc.itf.om.IOrgInfoQueryService;
import nc.itf.ta.ISignApplyManageService2;
import nc.itf.ta.ISignCardApplyApproveManageMaintain;
import nc.itf.ta.ISignCardApplyQueryMaintain;
import nc.itf.ta.ISignCardRegisterManageMaintain;
import nc.itf.ta.ISignCardRegisterQueryMaintain;
import nc.itf.ta.ITBMPsndocQueryMaintain;
import nc.itf.ta.ITimeRuleQueryService;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.pub.billcode.itf.IBillcodeManage;
import nc.pub.billcode.vo.BillCodeContext;
import nc.pubitf.para.SysInitQuery;
import nc.pubitf.rbac.IUserPubService;
import nc.vo.bd.defdoc.DefdocVO;
import nc.vo.hi.psndoc.PsnJobVO;
import nc.vo.pmpub.project.ProjectHeadVO;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.billtype.BilltypeVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.ta.bill.BillMutexException;
import nc.vo.ta.psndoc.TBMPsndocVO;
import nc.vo.ta.signcard.AggSignVO;
import nc.vo.ta.signcard.SignCardBeyondTimeVO;
import nc.vo.ta.signcard.SignRegVO;
import nc.vo.ta.signcard.SignbVO;
import nc.vo.ta.signcard.SignhVO;
import nc.vo.ta.timerule.TimeRuleVO;
import nc.vo.ta.wf.pub.TaWorkFlowManager;
import nc.vo.uap.pf.PfProcessBatchRetObject;
import nc.vo.uif2.LoginContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class TBMSigncardDao {
	/**
	 * 保存签卡
	 * 
	 * @param signhMap
	 * @param bList
	 * @param userid
	 * @param needCheck
	 * @return
	 * @throws BusinessException
	 */
	public AggSignVO saveSigncard(Map<String, Object> signhMap,
			List<Map<String, Object>> bList, String userid, String needCheck)
			throws BusinessException {
		if (CollectionUtils.isEmpty(bList)) {
			throw new BusinessException(ResHelper.getString("6017hrta",
					"06017hrta0094"));
		}
		signhMap.put("signbs", bList);
		AggSignVO aggvo = new AggSignVO();
		SignhVO mainvo = new SignhVO();
		aggvo.setParentVO(mainvo);
		String pk_psndoc = ((IUserPubService) NCLocator.getInstance().lookup(
				IUserPubService.class)).queryPsndocByUserid(userid);
		TBMPsndocVO latestVO = ((ITBMPsndocQueryMaintain) NCLocator
				.getInstance().lookup(ITBMPsndocQueryMaintain.class))
				.queryByPsndocAndDateTime(pk_psndoc, new UFDateTime());
		if (latestVO == null) {
			throw new BusinessException("没有考勤档案");
		}
		String pk_org = latestVO.getPk_org();
		String pk_group = latestVO.getPk_group();
		initGroup(pk_group);
		PsnJobVO jobVO = (PsnJobVO) ((IPersistenceRetrieve) NCLocator
				.getInstance().lookup(IPersistenceRetrieve.class))
				.retrieveByPk(null, PsnJobVO.class, latestVO.getPk_psnjob());
		String pk_org_v = ((IOrgInfoQueryService) NCLocator.getInstance()
				.lookup(IOrgInfoQueryService.class)).getOrgVid(
				jobVO.getPk_org(), new UFDate());
		String pk_dept_v = ((IDeptQueryService) NCLocator.getInstance().lookup(
				IDeptQueryService.class)).getDeptVid(jobVO.getPk_dept(),
				new UFDate());
		String pk_signh = signhMap.get("pk_signh") == null ? null
				: (String) signhMap.get("pk_signh");
		AggSignVO oldAggVo=null;
		if (StringUtils.isBlank(pk_signh)) {
			mainvo.setStatus(2);// 新增
		} else {
			mainvo.setPk_signh(pk_signh);
			mainvo.setStatus(1);// 修改
			String whereSql = "pk_signh = '" + pk_signh + "' ";
			AggSignVO[] aggVOs = ((ISignCardApplyQueryMaintain) NCLocator
					.getInstance().lookup(ISignCardApplyQueryMaintain.class))
					.queryByCond(whereSql);
			if (aggVOs.length>0) {
				oldAggVo=aggVOs[0];
			}
			mainvo.setTs(signhMap.get("ts") == null ? new UFDateTime(): new UFDateTime((String) signhMap.get("ts")));
			mainvo.setBill_code(oldAggVo.getSignhVO().getBill_code());
		}
		UFDateTime dateTime = new UFDateTime();
		mainvo.setCreationtime(dateTime);
		mainvo.setPk_psndoc(pk_psndoc);
		mainvo.setPk_psnjob(latestVO.getPk_psnjob());
		mainvo.setPk_psnorg(latestVO.getPk_psnorg());
		mainvo.setPk_org(pk_org);
		mainvo.setPk_org_v(pk_org_v);
		mainvo.setPk_dept_v(pk_dept_v);
		mainvo.setPk_group(pk_group);
		mainvo.setBillmaker(userid);
		mainvo.setApprove_state(Integer.valueOf(-1));
		mainvo.setCreator(userid);
		mainvo.setApply_date(new UFLiteralDate());
		mainvo.setPk_billtype("6402");
		String transtypeid = (String) signhMap.get("transtypeid");
		if(!StringUtils.isEmpty(transtypeid) && !"~".equals(transtypeid)){
			String querySQL = "select pk_billtypecode transtype from bd_billtype where pk_billtypeid=?";
			SQLParameter para = new SQLParameter();
			para.addParam(transtypeid);
			String transtype = (String) new BaseDAO().executeQuery(querySQL, para, new ColumnProcessor());
			mainvo.setTranstypeid(transtypeid);
			mainvo.setTranstype(transtype);
//			mainvo.setPk_billtype(transtype);
		}else{
			mainvo.setTranstypeid("~");
			mainvo.setTranstype("~");
		}
		mainvo.setIshrssbill(UFBoolean.TRUE);
		mainvo.setSignremark((String) signhMap.get("signremark"));
		//add by wt 20190822 begin
//		mainvo.setPk_project(signhMap.get("pk_project") == null ? "" : signhMap.get("pk_project").toString());
		//add by wt 20190822 end
		TimeRuleVO timeRule = ((ITimeRuleQueryService) NCLocator.getInstance()
				.lookup(ITimeRuleQueryService.class)).queryByOrg(pk_org);
		List<SignbVO> bvos=new ArrayList<SignbVO>();
		for (Map<String, Object> bMap : bList) {
			SignbVO bvo = new SignbVO();
			bvos.add(bvo);
			bvo.setPk_signh(pk_signh);
			String pk_signb = bMap.get("pk_signb") == null ? null
					: (String) bMap.get("pk_signb");
			bvo.setPk_signb(pk_signb);
			if (StringUtils.isBlank(pk_signb)) {
				bvo.setStatus(2);
			} else {
				bvo.setStatus(1);
			}
			bvo.setSignremark((String)signhMap.get("signremark"));
			bvo.setSigndate(new UFLiteralDate((String) bMap.get("time")));
			bvo.setSigntime(new UFDateTime((String) bMap.get("time")));
			if (timeRule.getCheckinflag().booleanValue()) {
				bvo.setSignstatus(Integer.valueOf((String) bMap
						.get("signstatus")));
			} else {
				bvo.setSignstatus(Integer.valueOf(2));
			}
			bvo.setPk_psndoc(pk_psndoc);
			bvo.setPk_psnjob(latestVO.getPk_psnjob());
			bvo.setPk_psnorg(latestVO.getPk_psnorg());
			bvo.setPk_org(pk_org);
			bvo.setPk_group(pk_group);
			
		}
		//不允许有相等的时间
		if (bvos.size() > 1) {
			for (int i1 = 0; i1 < bvos.size(); i1++) {
				for (int i2 = i1 + 1; i2 < bvos.size(); i2++) {
					if (bvos.get(i1).getSigntime().toString()
							.equals(bvos.get(i2).getSigntime().toString())) {
						throw new BusinessException(ResHelper.getString(
								"6017hrta", "06017hrta0095"));
					}
				}
			}
		}
		SignbVO[] oldbvos=null;
		//处理删除的子单据
		if (StringUtils.isNotBlank(pk_signh) && oldAggVo!=null && oldAggVo.getSignbVOs().length>0) {
			oldbvos=oldAggVo.getSignbVOs();
			for (SignbVO oldsignbVO : oldbvos) {
				boolean isdeleted=true;
				for(SignbVO newsignbVo:bvos){
					if (oldsignbVO.getPk_signb().equals(newsignbVo.getPk_signb())) {
						isdeleted=false;
						break;
					}
				}
				if (isdeleted) {
					//3代表已删除
					oldsignbVO.setStatus(3);
					bvos.add(oldsignbVO);
				}
			}
		}
		SignbVO[] newbvos = new SignbVO[bvos.size()];
		bvos.toArray(newbvos);
		aggvo.setChildrenVO(newbvos);
		String warningMessage = "";
		if ("Y".equalsIgnoreCase(needCheck)) {
			try{
				SignCardBeyondTimeVO[] beyondVOs = ((ISignCardRegisterManageMaintain) NCLocator.getInstance().lookup(ISignCardRegisterManageMaintain.class)).vldAndGetBydPrt(pk_org, newbvos);
				//SignCardBeyondTimeVO[] beyondVOs = NCLocator.getInstance().lookup(ISignCardApplyApproveManageMaintain.class).beforSaveCheck(pk_org,newbvos);
				if (!ArrayUtils.isEmpty(beyondVOs)) {
					warningMessage = ResHelper.getString("6017mobile","06017mytime000054");
					throw new BusinessException(warningMessage);
				}
				
			}catch(Exception e){
				if(e instanceof BillMutexException)
					throw new BusinessException("操作单据与其他单据有时间冲突，操作失败！");
				throw new BusinessException(e.getMessage());
			}
		}
		ISignCardApplyApproveManageMaintain signM = (ISignCardApplyApproveManageMaintain) NCLocator.getInstance().lookup(ISignCardApplyApproveManageMaintain.class);
//	TODO
		ISignApplyManageService2 signM2 = NCLocator.getInstance().lookup(ISignApplyManageService2.class);
		if (StringUtils.isBlank(pk_signh)) {
			String newBill_code = TBMHelper.getBillCode("6402",mainvo.getPk_group(),mainvo.getPk_org());
			aggvo.getSignhVO().setBill_code(newBill_code);
			try{
				aggvo = (AggSignVO) signM.insertData(aggvo); // 但强  签卡保存需要校验相同时间不能同时拥有两张单据
			} catch(BusinessException e){
				TBMHelper.rollbackBillCode("6402",mainvo.getPk_group(),mainvo.getPk_org(),newBill_code);
				throw new BusinessException(e.getMessage());
			}
		} else {
			aggvo = (AggSignVO) signM.updateData(aggvo);
		}
		return aggvo;
	}

	/**
	 * 提交签卡
	 * 
	 * @param userid
	 * @param aggvo
	 * @return
	 * @throws BusinessException
	 */
	/********************************tianxx5**************************************/
	public Map<String, Object> submitSigncard(String userid, AggSignVO aggvo)
			throws BusinessException {
		String pk_psndoc = ((IUserPubService) NCLocator.getInstance().lookup(
				IUserPubService.class)).queryPsndocByUserid(userid);
		TBMPsndocVO latestVO = ((ITBMPsndocQueryMaintain) NCLocator
				.getInstance().lookup(ITBMPsndocQueryMaintain.class))
				.queryByPsndocAndDateTime(pk_psndoc, new UFDateTime());
		if (latestVO == null) {
			throw new BusinessException(ResHelper.getString("6017mobile",
					"06017mytime000001"));
		}
		String pk_org = latestVO.getPk_org();
		PfProcessBatchRetObject validateRetObj = ((IHrPf) NCLocator
				.getInstance().lookup(IHrPf.class))
				.submitValidation(
						"Commit",
						"Commit",
						null,
						SysInitQuery.getParaInt(aggvo.getSignhVO().getPk_org(),
								(String) IHrPf.hashBillTypePara.get("6402"))
								.intValue(),
						new AggregatedValueObject[] { aggvo });
		if ((validateRetObj.getRetObj() == null)
				|| (validateRetObj.getRetObj().length == 0)) {
			String errStr = validateRetObj.getExceptionMsg();
			if (StringUtils.isNotBlank(errStr))
				throw new BusinessException(errStr);
		}
		HashMap<String, String> eParam = new HashMap();
		if (isDirectApprove(pk_org, "6402")) {
			eParam.put("nosendmessage", "nosendmessage");
		}
		LfwPfUtil.runAction("SAVE", "6402", aggvo, null, null, null, eParam,
				new String[] { userid }, null);

		Map<String, Object> result = new HashMap();
		result.put("flag", "2");
		return result;
	}
	/********************************tianxx5**************************************/
	/**
	 * 查看签卡申请详情
	 * 
	 * @param pk_signcardh
	 * @return
	 * @throws BusinessException
	 */
	public Map<String, Object> querySigncardByPK(String pk_signcardh)
			throws BusinessException {
		Map<String, Object> result = new HashMap();

		String whereSql = "pk_signh = '" + pk_signcardh + "' ";
		AggSignVO[] aggVOs = ((ISignCardApplyQueryMaintain) NCLocator
				.getInstance().lookup(ISignCardApplyQueryMaintain.class))
				.queryByCond(whereSql);
		if (ArrayUtils.isEmpty(aggVOs)){//针对从考勤信息“员工日历”登记单数据跳转，需要查询对应登记单数据  by tianxx5
			SignRegVO regVO = ((ISignCardRegisterQueryMaintain) NCLocator
					.getInstance().lookup(ISignCardRegisterQueryMaintain.class)).queryByPk(pk_signcardh);
			result.put("pk_signh", regVO.getPk_signreg());
			result.put("billcode", null);
			result.put("transtypeid", null);
			result.put("transtype", null);
			result.put("ts", regVO.getTs().toString());
			result.put("creationtime", regVO.getCreationtime().toString());
			result.put("approve_state", "10");
			result.put("signremark", regVO.getSignremark());
			result.put("requestid",regVO.getAttributeValue("requestid"));

			TimeRuleVO timeRule = ((ITimeRuleQueryService) NCLocator.getInstance()
					.lookup(ITimeRuleQueryService.class)).queryByOrg(regVO
					.getPk_org());
			result.put("issignstatus",
					timeRule.getCheckinflag().booleanValue() == true ? "Y" : "N");
			List<Map<String, Object>> blist = new ArrayList();
			result.put("signbs", blist);
			List<String> pk_reasionList = new ArrayList();
			if (StringUtils.isNotBlank(regVO.getSignreason())) {
				pk_reasionList.add(regVO.getSignreason());
			}
			DefdocVO[] reasions = ((IDefdocQryService) NCLocator.getInstance()
					.lookup(IDefdocQryService.class))
					.queryDefdocByPk((String[]) pk_reasionList
							.toArray(new String[0]));
			Map<String, DefdocVO> reasionMap = new HashMap();
			if (!ArrayUtils.isEmpty(reasions)) {
				reasionMap = CommonUtils.toMap("pk_defdoc", reasions);
			}
			Map<String, Object> binfo = new HashMap();
			blist.add(binfo);
			binfo.put("time", regVO.getSigntime().toString());
			binfo.put("pk_signb", "virtualsignPK");
			binfo.put("pk_reason", regVO.getSignreason());
			binfo.put("signstatus", regVO.getSignstatus());
			binfo.put("signremark", regVO.getSignremark());
			DefdocVO defdocVO = (DefdocVO) reasionMap.get(regVO.getSignreason());
			if (defdocVO != null) {
				binfo.put("reasonname",
						MultiLangHelper.getName(defdocVO, "name"));
			} else {
				binfo.put("reasonname", null);
			}
			Integer signstatus = regVO.getSignstatus();
			if (signstatus.intValue() == 0) {
				binfo.put("statusname",
						ResHelper.getString("6017hrta", "06017hrta0083"));
			} else if (signstatus.intValue() == 1) {
				binfo.put("statusname",
						ResHelper.getString("6017hrta", "06017hrta0084"));
			}
			binfo.put("ts", regVO.getTs().toString());
			return result;
		}
		AggSignVO aggVO = aggVOs[0];
		SignhVO headVO = aggVO.getSignhVO();
		SignbVO[] bodyVOs = aggVO.getSignbVOs();

		result.put("pk_signh", headVO.getPk_signh());
		result.put("billcode", headVO.getBill_code());
		result.put("transtypeid", headVO.getTranstypeid());
		result.put("transtype", headVO.getTranstype());
		result.put("ts", headVO.getTs().toString());
		result.put("creationtime", headVO.getCreationtime().toString());
		result.put("approve_state", headVO.getApprove_state().toString());
		result.put("signremark", headVO.getSignremark());
		result.put("requestid",headVO.getAttributeValue("requestid"));

		TimeRuleVO timeRule = ((ITimeRuleQueryService) NCLocator.getInstance()
				.lookup(ITimeRuleQueryService.class)).queryByOrg(headVO
				.getPk_org());
		result.put("issignstatus",
				timeRule.getCheckinflag().booleanValue() == true ? "Y" : "N");

		if (StringUtils.isNotBlank(headVO.getTranstypeid())) {
			BilltypeVO billType = (BilltypeVO) new BaseDAO().retrieveByPK(
					BilltypeVO.class, headVO.getTranstypeid());
			result.put("transtypename",
					MultiLangHelper.getName(billType, "billtypename"));
		}
		// add by wt 20190822
//		if (StringUtils.isNotBlank(headVO.getPk_project())) {
//			ProjectHeadVO projectVO = (ProjectHeadVO) new BaseDAO().retrieveByPK(
//					ProjectHeadVO.class, headVO.getPk_project());
//			result.put("project_name",MultiLangHelper.getName(projectVO, "project_name"));
//		}
		List<Map<String, Object>> blist = new ArrayList();
		result.put("signbs", blist);
		if (ArrayUtils.isEmpty(bodyVOs)) {
			return result;
		}
		List<String> pk_reasionList = new ArrayList();
		for (SignbVO bvo : bodyVOs) {
			if (StringUtils.isNotBlank(bvo.getSignreason())) {
				pk_reasionList.add(bvo.getSignreason());
			}
		}
		DefdocVO[] reasions = ((IDefdocQryService) NCLocator.getInstance()
				.lookup(IDefdocQryService.class))
				.queryDefdocByPk((String[]) pk_reasionList
						.toArray(new String[0]));
		Map<String, DefdocVO> reasionMap = new HashMap();
		if (!ArrayUtils.isEmpty(reasions)) {
			reasionMap = CommonUtils.toMap("pk_defdoc", reasions);
		}
		for (SignbVO bvo : bodyVOs) {
			//不显示已经删除的子单据
			if(bvo.getStatus()==3){
				continue;
			}
			Map<String, Object> binfo = new HashMap();
			blist.add(binfo);
			binfo.put("time", bvo.getSigntime().toString());
			binfo.put("pk_signb", bvo.getPk_signb());
			binfo.put("pk_reason", bvo.getSignreason());
			binfo.put("signstatus", bvo.getSignstatus());
			binfo.put("signremark", bvo.getSignremark());
			DefdocVO defdocVO = (DefdocVO) reasionMap.get(bvo.getSignreason());
			if (defdocVO != null) {
				binfo.put("reasonname",
						MultiLangHelper.getName(defdocVO, "name"));
			} else {
				binfo.put("reasonname", null);
			}
			Integer signstatus = bvo.getSignstatus();
			if (signstatus.intValue() == 0) {
				binfo.put("statusname",
						ResHelper.getString("6017hrta", "06017hrta0083"));
			} else if (signstatus.intValue() == 1) {
				binfo.put("statusname",ResHelper.getString("6017hrta", "06017hrta0084"));
			}
			binfo.put("ts", bvo.getTs().toString());
		}
		String pk_org = headVO.getPk_org();
		String pk_group = headVO.getPk_group();

		BillCodeContext billCodeContext = getBillCodeContext("6402", pk_group,
				pk_org);
		if (billCodeContext == null) {
			result.put("isbillcodeedit", "Y");
		} else {
			if (StringUtils.isBlank(headVO.getBill_code())) {
				String[] billCode = ((IHrBillCode) NCLocator.getInstance()
						.lookup(IHrBillCode.class)).getBillCode("6402",
						pk_group, pk_org, 1);
				result.put("billcode", billCode[0]);
			}
			if (billCodeContext.isEditable()) {
				result.put("isbillcodeedit", "Y");
			} else {
				result.put("isbillcodeedit", "N");
			}
		}

		boolean directApprove = isDirectApprove(pk_org, "6402");
		if (directApprove) {
			result.put("istranstypeedit", "N");
		} else {
			result.put("istranstypeedit", "Y");
		}

		ArrayList<Map<String,Object>> workFlowNote = new TBMAwayDao().queryWorkFlowNote(headVO.getTranstype(),headVO.getBill_code(),headVO.getPk_signh());
		if(workFlowNote!=null && workFlowNote.size()>0){
			result.put("workflownote", workFlowNote);
		}
		return result;
	}
		/**
	 * 签卡申请单打印信息查询
	 * @param pk_signh
	 * @return
	 */
	public Map<String, Object> signtimePrintTemplate(String pk_signh)throws BusinessException{
		Map<String, Object> map = new HashMap<>();
		StringBuilder builder = new StringBuilder();
		builder.append("select a.bill_code as billcode, b.name as orgname, d.name as deptname, a.approve_state as status, ");
		builder.append("f.user_name as applyer, a.apply_date as applydate, a.approve_time as approvedate, g.postname as postname ");
		builder.append("from tbm_signh a ");
		builder.append("inner join org_hrorg b on a.pk_org = b.pk_hrorg ");
		builder.append("inner join hi_psnjob c on c.pk_psnjob = a.pk_psnjob ");
		builder.append("inner join org_dept d on d.pk_dept = c.pk_dept ");
		builder.append("inner join sm_user f on f.cuserid = a.billmaker ");
		builder.append("inner join om_post g on g.pk_post = c.pk_post ");
		builder.append("where a.pk_signh = '" + pk_signh + "' ");
		HashMap<String, Object> dataMap = (HashMap<String, Object>)new BaseDAO().executeQuery(builder.toString(), new MapProcessor());
		StringBuilder sb = new StringBuilder();
		sb.append("select a.signtime as signtime ");
		sb.append("from tbm_signb a ");
		sb.append("where a.pk_signh = '" + pk_signh + "'");
		sb.append("order by a.signtime asc ");
		List list = (List) new BaseDAO().executeQuery(sb.toString(),
				new MapListProcessor());
		StringBuilder sba = new StringBuilder();
		sba.append("select b.user_name as sender, a.senddate as senddate, c.user_name as approver, a.dealdate as approdate, a.checknote as approveidea ");
		sba.append("from pub_workflownote a ");
		sba.append("inner join sm_user b on b.cuserid = a.senderman ");
		sba.append("inner join sm_user c on c.cuserid = a.checkman ");
		sba.append("where  billid = '" + pk_signh + "' and actiontype <> 'BIZ' ");
		sba.append("order by a.senddate asc ");
		List notelist = (List) new BaseDAO().executeQuery(sba.toString(),
				new MapListProcessor());
		map.put("data", dataMap);
		map.put("tableData", list);
		map.put("approveData", notelist);
		return map;
	}
/**
	 * 删除签卡申请
	 * 
	 * @param pk_signh
	 * @return
	 * @throws BusinessException
	 */
	public Map<String, Object> deleteSignCard(String pk_signh)
			throws BusinessException {
		Map<String, Object> result = new HashMap();
		String whereSql = "pk_signh = '" + pk_signh + "' ";
		AggSignVO[] aggVOs = ((ISignCardApplyQueryMaintain) NCLocator
				.getInstance().lookup(ISignCardApplyQueryMaintain.class))
				.queryByCond(whereSql);
		if (ArrayUtils.isEmpty(aggVOs))
			return result;
		AggSignVO aggVO = aggVOs[0];
		deleteBeforcheckState(aggVO.getSignhVO().getApprove_state());
		((ISignCardApplyApproveManageMaintain) NCLocator.getInstance().lookup(
				ISignCardApplyApproveManageMaintain.class)).deleteData(aggVO);
		result.put("flag", "2");
		return result;
	}

	public boolean isDirectApprove(String pk_org, String billtype)
			throws BusinessException {
		Integer type = SysInitQuery.getParaInt(pk_org,
				(String) IHrPf.hashBillTypePara.get(billtype));
		return (type != null) && (type.intValue() == 0);
	}

	public BillCodeContext getBillCodeContext(String billType, String pk_group,
			String pk_org) throws BusinessException {
		return ((IBillcodeManage) NCLocator.getInstance().lookup(
				IBillcodeManage.class)).getBillCodeContext(billType, pk_group,
				pk_org);
	}

	private void initGroup(String pk_group) {
		String groupId = InvocationInfoProxy.getInstance().getGroupId();
		if (StringUtils.isBlank(groupId)) {
			InvocationInfoProxy.getInstance().setGroupId(pk_group);
		}
	}

	public void deleteBeforcheckState(Integer pfsate) throws BusinessException {
		if (-1 != pfsate.intValue()) {
			throw new BusinessException(ResHelper.getString("6017hrta",
					"06017hrta0089"));
		}
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
	 * 提交签卡
	 * 
	 * @param userid
	 * @param aggvo
	 * @return
	 * @throws BusinessException
	 */
	/********************************tianxx5**************************************/
	public Map<String, Object> callbackSigncard(AggSignVO aggvo)
			throws BusinessException {
		String pk_psndoc = ((IUserPubService) NCLocator.getInstance().lookup(
				IUserPubService.class)).queryPsndocByUserid(aggvo.getSignhVO().getBillmaker());
		TBMPsndocVO latestVO = ((ITBMPsndocQueryMaintain) NCLocator
				.getInstance().lookup(ITBMPsndocQueryMaintain.class))
				.queryByPsndocAndDateTime(pk_psndoc, new UFDateTime());
		if (latestVO == null) {
			throw new BusinessException(ResHelper.getString("6017mobile",
					"06017mytime000001"));
		}
		String pk_org = latestVO.getPk_org();
		PfProcessBatchRetObject validateRetObj = ((IHrPf) NCLocator
				.getInstance().lookup(IHrPf.class))
				.callbackValidate("CallBack","CallBack",null,true,
						new AggregatedValueObject[] { aggvo });
		if ((validateRetObj.getRetObj() == null)
				|| (validateRetObj.getRetObj().length == 0)) {
			String errStr = validateRetObj.getExceptionMsg();
			if (StringUtils.isNotBlank(errStr))
				throw new BusinessException(errStr);
		}
		HashMap<String, String> eParam = new HashMap();
		LfwPfUtil.runAction("RECALL", "6402", aggvo, null, null, null, eParam,
				new String[] { aggvo.getSignhVO().getBillmaker() }, null);

		Map<String, Object> result = new HashMap();
		result.put("flag", "2");
		result.put("approve_state", aggvo.getSignhVO().getApprove_state());
		return result;
	}
	/********************************tianxx5**************************************/
	public int queryCountSignCard(String pk_psndoc,String begindate,String enddate)throws BusinessException{
		String querySql = "select count(*) from tbm_signb left join tbm_signh on tbm_signb.pk_signh = tbm_signh.pk_signh where tbm_signh.pk_psndoc='" + pk_psndoc + "' and tbm_signh.apply_date >='" + begindate +"' and tbm_signh.apply_date <= '" + enddate + "' and tbm_signh.approve_state in (1,2,3)";
		int count = (int) new BaseDAO().executeQuery(querySql, new ColumnProcessor());
		return count;
	}
}
