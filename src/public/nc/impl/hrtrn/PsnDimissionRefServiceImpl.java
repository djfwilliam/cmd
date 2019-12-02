package nc.impl.hrtrn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.impl.saas.hi.TBMHelper;
import nc.itf.hr.frame.IHrBillCode;
import nc.itf.hrtrn.IPsnDimissionRefService;
import nc.itf.saas.pub.PageResult;
import nc.itf.trn.transmng.ITransmngManageService;
import nc.itf.trn.transmng.ITransmngQueryService;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.pubitf.rbac.IUserPubService;
import nc.vo.hi.psndoc.PsnJobVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.trn.transmng.AggStapply;
import nc.vo.trn.transmng.StapplyVO;

import org.apache.commons.lang.ArrayUtils;

public class PsnDimissionRefServiceImpl implements IPsnDimissionRefService{
	
	public static ITransmngManageService getTrnManageService() {
		return (ITransmngManageService) NCLocator.getInstance().lookup(
				ITransmngManageService.class);
	}
	
	public static ITransmngQueryService getTransmngQueryService() {
		return (ITransmngQueryService) NCLocator.getInstance().lookup(
				ITransmngQueryService.class);
	}
	
	public PsnJobVO queryPsnjob(String pk_psndoc) throws DAOException {
		String psnjobSql = "pk_psndoc=? and ismainjob='Y' and lastflag='Y' and endflag='N'";
		SQLParameter para = new SQLParameter();
		para.addParam(pk_psndoc);
		PsnJobVO[] psnjobVOs = (PsnJobVO[])new BaseDAO().retrieveByClause(PsnJobVO.class, psnjobSql, para).toArray(new PsnJobVO[0]);
		return ArrayUtils.isEmpty(psnjobVOs)?null:psnjobVOs[0];
	}

	@Override
	public String queryDimissionRef(Map<String, Object> param)
			throws BusinessException {
		String userId = param.get("userId").toString();
		String refType = param.get("refType").toString();
		String pk_adminorg = null;
		String pk_dept = null;
		String pk_post = null;
		if(param.get("bindNodePk") != null && !"".equals(param.get("bindNodePk"))){
			pk_adminorg = param.get("bindNodePk").toString();
		}
		if(param.get("bindDeptPk") != null && !"".equals(param.get("bindDeptPk"))){
			pk_dept = param.get("bindDeptPk").toString();
		}
		if(param.get("bindPostPk") != null && !"".equals(param.get("bindPostPk"))){
			pk_post = param.get("bindPostPk").toString();
		}
		String condition = null;
		if (param.get("refType") != null && !"".equals(param.get("refType"))) {
			refType = param.get("refType").toString();
		} else {
			refType = "trnstype";
		}
		if(pk_adminorg !=null && !"".equals(pk_adminorg)) {
			if(refType.equals("dimDept")){
				condition = " AND org_dept.pk_org = '"+pk_adminorg+"' ";
			}else if (refType.equals("dimPost") && pk_dept !=null && !"".equals(pk_dept)){
				condition = " AND om_post.pk_org = '"+pk_adminorg+"' and om_post.pk_dept= '"+ pk_dept +"'";
			}
		}else if(pk_post !=null && !"".equals(pk_post)){
			if(refType.equals("dimJobgrade")){
				condition = " AND om_post.pk_post = '"+pk_post+"' ";
			}
		}
		List<Map<String, Object>> data = new PsnDimissionRefDAO().queryDimissionRef(
				userId, refType, condition);
		PageResult result = new PageResult();
		result.setData(data);
		result.pushDevInfo("param", param);
		String json = result.toJson();
		System.out.println(json);
		return result.toJson();
	}

	@Override
	public String queryDimissionPsnInfo(Map<String, Object> param)
			throws BusinessException {
		String userId = param.get("userId").toString();
		String pk_psnjob = param.get("pk_psndoc").toString();
		List<Map<String, Object>> data = new PsnDimissionRefDAO().queryDimissionPsnInfo(pk_psnjob,userId);
		PageResult result = new PageResult();
		result.setData(data);
		result.pushDevInfo("param", param);
		String json = result.toJson();
		System.out.println(json);
		return result.toJson();
	}

	@Override
	public String saveDimissionBill(Map<String, Object> param)
			throws BusinessException {
		HashMap<String,String> dimissionWriteMap =  (HashMap<String, String>) param.get("dimissionWriteMap");
		HashMap<String,String> bList = ((ArrayList<HashMap<String,String>>) param.get("bList")).get(0);
		String userId = param.get("userId").toString();
		String pk_psndoc = ((IUserPubService) NCLocator.getInstance().lookup(
				IUserPubService.class)).queryPsndocByUserid(userId);
		PsnJobVO psnjobVO = queryPsnjob(pk_psndoc);
		Map<String, Object> dimissionMap = new PsnDimissionRefDAO().queryDimissionPsnInfo(psnjobVO.getPk_psnjob(),
				userId).get(0);
		AggStapply vo = new AggStapply();
		StapplyVO parentVO = new StapplyVO();
		parentVO.setApply_date(new UFLiteralDate());
		parentVO.setApprove_state(-1);
		parentVO.setAssgid(1); // 人员任职id
		parentVO.setBill_code(TBMHelper.getBillCode("6115", psnjobVO.getPk_group(), psnjobVO.getPk_org())); //
		parentVO.setBillmaker(userId);
		parentVO.setCreationtime(new UFDateTime());
		parentVO.setCreator(userId);
		parentVO.setDr(0);
		parentVO.setEffectdate(new UFLiteralDate(bList.get("effectiveDate"))); //
		parentVO.setFun_code("60090dimissionapply");
		parentVO.setIfaddblack(new UFBoolean(false));
		parentVO.setIsdisablepsn(new UFBoolean(true));
		parentVO.setIsend(new UFBoolean(false));
		parentVO.setIshrssbill(new UFBoolean(true));
		parentVO.setIsneedfile(new UFBoolean(false));
		parentVO.setIsrelease(new UFBoolean(false));
		parentVO.setMemo(bList.get("workprocess")==null?null:bList.get("workprocess").toString()); //
		parentVO.setNewpk_dept(dimissionWriteMap.get("pk_dimdept")); //
		parentVO.setNewpk_org(dimissionWriteMap.get("pk_adminorg")); //
		parentVO.setNewpk_psncl(dimissionWriteMap.get("pk_psncl")); //
		parentVO.setNewpoststat(new UFBoolean(true)); //新是否在岗
		parentVO.setOldpk_dept(dimissionMap.get("pk_dimdept").toString()); //
		parentVO.setOldpk_org(dimissionMap.get("pk_adminorg").toString()); //
		parentVO.setOldpk_psncl(dimissionMap.get("pk_psncl").toString()); //
		parentVO.setOldpoststat(new UFBoolean(true));
		parentVO.setPk_billtype("6115");
		parentVO.setPk_group(psnjobVO.getPk_group());//
		parentVO.setPk_hi_org(dimissionMap.get("pk_newpsninfoorg").toString());//
		parentVO.setPk_hrcm_org(dimissionMap.get("pk_newcontractorg").toString());//
		parentVO.setPk_old_hi_org(dimissionMap.get("pk_oldpsninfoorg").toString());
		parentVO.setPk_old_hrcm_org(dimissionMap.get("pk_oldcontractorg").toString());
//		parentVO.setPk_org(psnjobVO.getPk_org()); //
		parentVO.setPk_org(psnjobVO.getPk_hrorg());//update by wt 20191105 case：nc中根据人力组织查询单据
		parentVO.setPk_psndoc(dimissionMap.get("pk_psndoc").toString()); //
		parentVO.setPk_psnjob(psnjobVO.getPk_psnjob()); //
		parentVO.setPk_psnorg(psnjobVO.getPk_psnorg()); //
		parentVO.setPk_trnstype(dimissionWriteMap.get("pk_trnstype")); //
		parentVO.setSreason(dimissionWriteMap.get("pk_defdoc"));
		parentVO.setStapply_mode(1);
		parentVO.setAttributeValue("newjobglbdef7", dimissionWriteMap.get("pk_project") == null ? "": dimissionWriteMap.get("pk_project"));
		if(dimissionWriteMap.get("pk_billtypeid") != null){
			String transtypesql = "select pk_billtypecode from bd_billtype where pk_billtypeid = '" + dimissionWriteMap.get("pk_billtypeid").toString() + "'";
			parentVO.setTranstype((String)(new BaseDAO().executeQuery(transtypesql, new ColumnProcessor())));
		}
		parentVO.setTranstypeid(dimissionWriteMap.get("pk_billtypeid"));
		AggStapply saveVO = null;
		if(param.get("billKey") != null){ //存在单据主键，说明是修改保存
			parentVO.setPk_hi_stapply(param.get("billKey").toString());
			vo.setParentVO(parentVO);
			saveVO = getTrnManageService().updateBill(vo, true);
		}else{ // 新增保存
			vo.setParentVO(parentVO);
			saveVO = getTrnManageService().insertBill(vo);
		}
		StapplyVO stapplyVO = (StapplyVO)saveVO.getParentVO();
		Map<String, String> data = new HashMap<String,String>();
		data.put("pk_bill",stapplyVO.getPk_hi_stapply());
		PageResult result = new PageResult();
		result.setData(data);
		result.pushDevInfo("param", param);
		return result.toJson();
	}

	@Override
	public Map<String, Object> queryDimissionBill(Map<String, Object> param)
			throws BusinessException {
		String userId = param.get("userId").toString();
		String pk_hi_stapply = param.get("pk_hi_stapply") ==null ? null : param.get("pk_hi_stapply").toString();
		AggStapply aggVO = getTransmngQueryService().queryByPk(pk_hi_stapply);
		Map<String, Object> data = new PsnDimissionRefDAO().queryDimissionBill(userId,aggVO);
		return data;
	}

	@Override
	public String deleteDimissionBill(Map<String, Object> param)
			throws BusinessException {
		String pk_hi_stapply = param.get("pk_hi_stapply").toString();
		Map<String, Object> result = new HashMap();
		AggStapply aggVO = getTransmngQueryService().queryByPk(pk_hi_stapply);
		deleteBeforcheckState(((StapplyVO)aggVO.getParentVO()).getApprove_state());
		getTrnManageService().deleteBill(aggVO);
		result.put("flag", "2");
		PageResult pageResult = new PageResult();
		pageResult.setData(result);
		return pageResult.toJson();
	}

	private void deleteBeforcheckState(Integer approve_state) throws BusinessException {
		if (-1 != approve_state.intValue()) {
			throw new BusinessException("单据状态不是自由状态不能删除");
		}
	}

	@Override
	public String submitDimissionBill(Map<String, Object> param) throws BusinessException {
		String pk_hi_stapply = param.get("billKey").toString();
		AggStapply aggVO = getTransmngQueryService().queryByPk(pk_hi_stapply);
		Map<String, Object> submitDimission = new PsnDimissionRefDAO().submitDimission(aggVO);
		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(submitDimission);
		return result.toJson();
	}

	@Override
	public String rollbackDimissionBill(Map<String, Object> param) throws BusinessException {
		String pk_hi_stapply = (String)param.get("billKey");
		AggStapply aggVO = getTransmngQueryService().queryByPk(pk_hi_stapply);
		Map<String, Object> rollbackAway = new PsnDimissionRefDAO().rollbackDimission(aggVO);
		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(rollbackAway);
		return result.toJson();
	}

}
