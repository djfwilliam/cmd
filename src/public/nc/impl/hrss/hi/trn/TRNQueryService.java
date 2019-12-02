package nc.impl.hrss.hi.trn;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.hrss.pub.BillCoderUtils;
import nc.hr.utils.PubEnv;
import nc.impl.hrtrn.PsnDimissionRefDAO;
import nc.impl.saas.hi.LfwPfUtil;
import nc.impl.saas.hi.TBMHelper;
import nc.itf.hr.pf.IHrPf;
import nc.itf.hrss.hi.trn.ITrnQueryService;
import nc.itf.saas.pub.PageResult;
import nc.itf.trn.transmng.ITransmngManageService;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.jdbc.framework.processor.BeanProcessor;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.md.persist.framework.IMDPersistenceQueryService;
import nc.md.persist.framework.IMDPersistenceService;
import nc.md.persist.framework.MDPersistenceService;
import nc.pub.billcode.vo.BillCodeContext;
import nc.pub.tools.HiCacheUtils;
import nc.pubitf.para.SysInitQuery;
import nc.pubitf.rbac.IUserPubService;
import nc.vo.hi.psndoc.PsnJobVO;
import nc.vo.hi.psndoc.PsndocVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.sm.UserVO;
import nc.vo.trn.transmng.AggStapply;
import nc.vo.trn.transmng.StapplyVO;
import nc.vo.uap.pf.PfProcessBatchRetObject;
import nc.vo.uif2.LoginContext;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

public class TRNQueryService implements ITrnQueryService{
	
	private LoginContext context;
	
	private LoginContext getContext(){
		if(context == null){
			context= new LoginContext();
		}
		return context;
	}

	@Override
	public String queryTrnBusiType(Map<String, Object> param) throws Exception {
		// 由于异动类型只有全局或者集团
		String billtype = param.get("billtype").toString();
		String sql = "";
		if(billtype.equals("trns")){
			sql = " select trnstypecode,trnstypename,pk_trnstype from hr_trnstype where (1=1 and enablestate = 2" +
					" and trnsevent=3 ) and (((pk_org='GLOBLE00000000000000' or pk_group = '"+PubEnv.getPk_group() +"'))) order by trnstypecode ";
		}else if (billtype.equals("dimission")){
			sql = " select trnstypecode,trnstypename,pk_trnstype from hr_trnstype where (1=1 and enablestate = 2" +
					" and trnsevent in (4,5) ) and (((pk_org='GLOBLE00000000000000' or pk_group = '"+PubEnv.getPk_group() +"'))) order by trnstypecode ";
		}
		List<Map<String, Object>> data = (List<Map<String, Object>>) new BaseDAO().executeQuery(sql, new MapListProcessor());
		PageResult result = new PageResult();
		result.setData(data);
		result.pushDevInfo("param", param);
		return result.toJson();
	}
	
	
	public String queryTemplet(Map<String, Object> param) throws Exception{
		String pk_trnstype = param.get("pk_trnstype").toString();
//		String userId = param.get("userId").toString();
		String userId = param.get("userid").toString();// update by wt 20191028 case:调配人员可选择
		Integer type = Integer.valueOf(param.get("billtype").toString()); //获取调配方式   1：组织内部调配  2：跨组织调配
//		UserVO vos = NCLocator.getInstance().lookup(IUserPubService.class).getUsersByPKs(new String[]{userId})[0];// 获取登入人员VO数据  //// update by wt 20191028 case:下面代码没有调用
		if (StringUtils.isBlank(pk_trnstype))
        {
            return null;
        }
		
		Map<String, Object> data = new TrnQueryDAO().getPsnTemplet(pk_trnstype, userId, type,null); // 查询展示模板

		PageResult result = new PageResult();
    	result.setData(data);
    	result.pushDevInfo("param", param);
		return result.toJson();
	}
	@Override
	public String queryDataType(Map<String, Object> param) throws Exception {
		
		String pk_h = param.get("pk_h").toString();
		String type = param.get("type").toString();
		String sql = "";
		if(type.equals("newseries")){
			// 更改的是职务 返回的是职务类别 
			//select  jobtypecode code,jobtypename title,pk_jobtype tid,pk_jobtype value,father_pk fid,pk_grade_source from om_jobtype  where 11=11  and ((enablestate in (2,1)) and ((pk_org = 'GLOBLE00000000000000' or pk_group = '"+PubEnv.getPk_group()+"')))  order by jobtypecode
			sql = "select om_jobtype.pk_jobtype pk_h,om_jobtype.jobtypename name from om_job om_job left join om_jobtype om_jobtype on om_job.pk_jobtype=om_jobtype.pk_jobtype where om_job.pk_job='" + pk_h +"'";
			
		}else if (type.equals("postseries")) {
			sql = "select om_postseries.PK_POSTSERIES pk_h,om_postseries.POSTSERIESNAME name from om_post om_post left join OM_POSTSERIES om_postseries on om_post.PK_POSTSERIES = om_postseries.PK_POSTSERIES where om_post.PK_POST = '"+pk_h+"'";
		}
		

		List<Map<String,Object>> list = (List<Map<String, Object>>) new BaseDAO().executeQuery(sql, new MapListProcessor());
		
		PageResult result = new PageResult();
    	result.setData(list.get(0));
    	result.pushDevInfo("param", param);
		return result.toJson();
	}

/**
 * update by wt
 * 原本:return null;
 */
	@Override
	public String queryRefInfo(Map<String, Object> param) throws Exception {
//		String userId = param.get("userId").toString();
//		String refType = param.get("refType").toString();
//		String pk_adminorg = null;
//		String condition = null;
//		if (param.get("refType") != null && !"".equals(param.get("refType"))) {
//			refType = param.get("refType").toString();
//		} 
//		if(pk_adminorg !=null && !"".equals(pk_adminorg)) {
//			condition = " AND org_dept.pk_org = '"+pk_adminorg+"' ";
//		}
//		List<Map<String, Object>> data = queryPsnRefRef(userId, refType, condition);
//		PageResult result = new PageResult();
//		result.setData(data);
//		result.pushDevInfo("param", param);
//		String json = result.toJson();
//		System.out.println(json);
//		return result.toJson();
		return null;
		
	}


    private List<Map<String, Object>> queryPsnRefRef(String userId, String refType,String condition) throws BusinessException {
    	String pk_psndoc = ((IUserPubService) NCLocator.getInstance().lookup(IUserPubService.class)).queryPsndocByUserid(userId);
		if (pk_psndoc == null) {
			throw new BusinessException("未找到该用户，请确认用户是否是员工");
		}
		String exesql = null;
		if ("orgRef".equals(refType)) { // 组织
			exesql = "select  code,name labelname,pk_adminorg nodekey,pk_fatherorg parentkey,displayorder from org_adminorg  where org_adminorg.pk_adminorg in (select pk_adminorg from org_admin_enable) and (enablestate = 2)  and ((pk_group = '0001A110000000000PQN'))  order by displayorder, code";
		} else if ("psnType".equals(refType)) { // 人员类别
			exesql = "select  code,name labelname,pk_psncl nodekey,parent_id parentkey from bd_psncl  where enablestate = 2 order by code";
		} else if ("dimDept".equals(refType)) { //部门
			exesql = "SELECT org_dept.code,org_dept.name labelname,org_dept.pk_dept nodekey,org_dept.pk_fatherorg parentkey,org_dept.pk_group,org_dept.pk_org,org_dept.principal,org_dept.displayorder,org_orgs.code AS org_code,org_orgs.name AS org_name FROM org_dept INNER JOIN org_orgs ON org_orgs.pk_org = org_dept.pk_org INNER JOIN org_adminorg ON org_dept.pk_org = org_adminorg.pk_adminorg AND org_adminorg.enablestate= 2 WHERE hrcanceled = 'N' AND depttype <> 1 AND org_dept.pk_org IN (SELECT pk_adminorg FROM org_admin_enable) AND org_dept.enablestate = 2 AND org_dept.hrcanceled = 'N'";
			if (condition != null) {
				exesql = exesql
						+ condition
						+ " ORDER BY org_code,org_dept.displayorder,org_dept.code";
			} 
		}

		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			data = (List<Map<String, Object>>) new BaseDAO().executeQuery(exesql, new MapListProcessor());
		} catch (DAOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		return data;
}

	protected static IMDPersistenceService getMDPersistenceService()
    {
        return MDPersistenceService.lookupPersistenceService();
    }
	
	@SuppressWarnings("unchecked")
	@Override
	public String saveBill(Map<String, Object> param) throws Exception {
		Map<String,Object> innerMap = (Map<String, Object>) param.get("innerMap");
		List<Map<String,Object>> newtemplet = (List<Map<String,Object>>) param.get("newtemplet");
		List<Map<String,Object>> oldtemplet = (List<Map<String,Object>>) param.get("oldtemplet");
		List<Map<String,Object>> crtmanage = (List<Map<String,Object>>) param.get("crtmanage");
		Map<String,Object> newcrtmanage = (Map<String,Object>) param.get("newcrtmanage");
		String fun_code = param.get("fun_code").toString();
		innerMap.put("fun_code", fun_code);
		String userID = param.get("userId").toString();
//		String pk_psndoc = innerMap.get("userid").toString();// add by wt 20191028 case:保存调配人
		AggStapply agg = null;
		if(param.get("pk_h")!=null&&!"".equals(param.get("pk_h"))&&!"new".equals(param.get("pk_h"))){
			agg = new TrnQueryDAO().getAggVO(param.get("pk_h").toString());
		}
		agg = getAggStapplyVO(innerMap,newtemplet,oldtemplet,crtmanage,newcrtmanage,userID,agg);

		if(param.get("pk_h")!=null&&!"".equals(param.get("pk_h"))&&!"new".equals(param.get("pk_h"))){
			agg.getParentVO().setStatus(VOStatus.UPDATED);
			getMDPersistenceService().saveBill( agg ); // 更新
		}else {
			checkData(userID);
//			checkData(pk_psndoc);//update by wt 20191028 case:调配校验单据
			NCLocator.getInstance().lookup(ITransmngManageService.class).insertBill(agg);
		}
		Map<String,String> map = new HashMap<String,String>();
		StapplyVO head = (StapplyVO) agg.getParentVO();
		map.put("pk_h", head.getPk_hi_stapply());
		map.put("bill_code", head.getBill_code());
		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(map);
		return result.toJson();
	}
	
	public void checkData(String userid) throws Exception{
//		UserVO vos = NCLocator.getInstance().lookup(IUserPubService.class).getUsersByPKs(new String[]{userid})[0];
//		String pk_psndoc = vos.getPk_psndoc();
		String pk_psndoc = userid;
		String sql = "select count(*) from hi_stapply where pk_psndoc = ? and approve_state in (-1,2,3)";
		SQLParameter param = new SQLParameter();
		param.addParam(pk_psndoc);
		Integer count = (Integer) new BaseDAO().executeQuery(sql,param, new ColumnProcessor());
		if(count > 0 ){
			throw new BusinessException("您已有正在审批或者未提交的单据，请确认后再填写单子");
		}
	}
	/**
	 * 判断单据号是否时自动生成
	 * @param billType
	 * @param pk_group
	 * @param pk_org
	 * @return
	 * @throws BusinessException
	 */
    private boolean isAutoGenerateBillCode(String billType, String pk_group, String pk_org) throws BusinessException
    {
        BillCodeContext billCodeContext = HiCacheUtils.getBillCodeContext(billType, pk_group, pk_org);
        return billCodeContext != null;
    }

    /**
     * 数据组装  按照map集合中的数据进行单据vo的数据组装
     * @param innerMap
     * @param newtemplet
     * @param oldtemplet
     * @param crtmanage
     * @param newcrtmanage
     * @param userid
     * @param aggvo
     * @return
     * @throws Exception
     */
	private AggStapply getAggStapplyVO(Map<String, Object> innerMap,
			List<Map<String, Object>> newtemplet, List<Map<String, Object>> oldtemplet,
			List<Map<String, Object>> crtmanage, Map<String, Object> newcrtmanage,
			String userid, AggStapply aggvo)throws Exception{ 
		String fun_code = innerMap.get("fun_code").toString();
//		String pk_psndoc = innerMap.get("userid").toString();// add by wt 20191028 case:保存调配人
		if(aggvo!=null){
			 StapplyVO vo =  (StapplyVO)aggvo.getParentVO();
			 fun_code = vo.getFun_code();
		}
		if (MapUtils.isEmpty(innerMap))
			return null;
		UserVO vos = NCLocator.getInstance().lookup(IUserPubService.class).getUsersByPKs(new String[]{userid})[0];
		String pk_psndoc = vos.getPk_psndoc();
//		String sql = "select * from bd_psndoc where pk_psndoc='"+pk_psndoc+"'";// update by wt 20191028 case:保存调配人
//    	PsndocVO vos = (PsndocVO) new BaseDAO().executeQuery(sql, new BeanProcessor(PsndocVO.class));
		if (StringUtils.isBlank(pk_psndoc))
			throw new BusinessException("没有找到人员信息");
		AggStapply agg = new AggStapply();
		StapplyVO vo = null;
		// 申请单信息
		if(aggvo == null){  // aggvo为空  表示新增单据
			vo = new StapplyVO();
			String billCode =TBMHelper.getBillCode(fun_code.equals("60090transapply")?"6113":"6115",PubEnv.getPk_group(), vos.getPk_org()); //BillCoderUtils.getBillCode(PubEnv.getPk_group(), vos.getPk_org(), fun_code.equals("60090transapply")?"6113":"6115");
			vo.setBill_code(billCode); //单据编码
		}else {
			agg = aggvo;
			vo = (StapplyVO) agg.getParentVO();  // 此时vo为需要更新的vo数据  将界面上的数据进行一一赋值  准备更新到数据库
		}
		
		String pk_trnstype = "";
		if(StringUtils.isNotEmpty(innerMap.get("transtypeid").toString())){
			pk_trnstype = innerMap.get("transtypeid").toString();
			String transtypesql = "select pk_billtypecode from bd_billtype where pk_billtypeid = '" + pk_trnstype + "'";
			vo.setTranstypeid(pk_trnstype); //流程类型
			vo.setTranstype((String)(new BaseDAO().executeQuery(transtypesql, new ColumnProcessor())));// 流程类型code
		}else{
			vo.setTranstype(fun_code.equals("60090transapply")?"6113":"6115");// 流程类型code
		}
		vo.setApprove_state(-1); // 审批状态
		vo.setBillmaker(userid); // 申请人

		vo.setPk_billtype(fun_code.equals("60090transapply")?"6113":"6115"); // 交易类型
		vo.setPk_group(PubEnv.getPk_group()); // 所属集团
		vo.setPk_org(getPk_psnjob(pk_psndoc).getPk_org()); // 人事组织
//		vo.setPk_org(getPk_psnjob(pk_psndoc).getPk_hrorg()); //update by wt 20191105 case：nc中根据人力组织查询单据
		vo.setFun_code(fun_code.equals("60090transapply")?"60090transapply":"60090dimissionapply");
		vo.setIshrssbill(UFBoolean.TRUE);
		vo.setCreator(userid);
		vo.setAssgid(0);
		vo.setCreationtime(new UFDateTime());
		vo.setPk_psnorg(getPk_psnjob(pk_psndoc).getPk_psnorg());
		// 人员信息
		vo.setTrial_flag(UFBoolean.FALSE);
		vo.setPk_psnjob(getPk_psnjob(pk_psndoc).getPk_psnjob());
		vo.setPk_psndoc(pk_psndoc);
		vo.setStapply_mode(fun_code.equals("60090transapply")?Integer.valueOf(innerMap.get("transtypevalue").toString()):1);// 调配方式
		vo.setPk_trnstype(innerMap.get("pk_trnstype").toString()); // 调配业务类型
		vo.setSreason(innerMap.get("pk_trnreason").toString()); // 调配原因
		vo.setEffectdate(new UFLiteralDate(innerMap.get("effecttime").toString().substring(0, 10))); // 生效日期
		vo.setMemo(innerMap.get("memo").toString()); // 调配说明
		// 调配前信息
		for(Map<String,Object> item : oldtemplet ){
			String itemKey = item.get("itemKey").toString();
			String value = item.get("value")==null?"":item.get("value").toString();
			vo.setAttributeValue(itemKey, value);
		}
		//调配后信息
		for(Map<String,Object> item : newtemplet ){
			String itemKey = item.get("itemKey").toString();
			String value = item.get("value")==null?"":item.get("value").toString();
			vo.setAttributeValue(itemKey, value);
		}
		// 合同管理组织
		Map<String,Object> crt = crtmanage.get(0);
		vo.setPk_old_hi_org(crt.get("pk_old_hi_org").toString());
		vo.setPk_hi_org(newcrtmanage.get("pk_hi_org").toString());
		vo.setPk_old_hrcm_org(crt.get("pk_old_hrcm_org").toString());
		vo.setPk_hrcm_org(newcrtmanage.get("pk_hrcm_org").toString());
		vo.setIsrelease(new UFBoolean((Boolean)innerMap.get("isrelease")));
		vo.setIsend(new UFBoolean((Boolean)innerMap.get("isend")));
		// 执行信息
		if(fun_code.equals("60090dimissionapply")){
			vo.setIfaddblack(new UFBoolean((Boolean)innerMap.get("ifaddblack")));
			vo.setIsdisablepsn(new UFBoolean((Boolean)innerMap.get("isdisablepsn")));
		}else {
			vo.setIfendpart(new UFBoolean((Boolean)innerMap.get("ifendpart")));
			vo.setIfsynwork(new UFBoolean((Boolean)innerMap.get("ifsynwork")));
		}
		vo.setIfaddpsnchg(UFBoolean.TRUE);	
		vo.setIsneedfile(UFBoolean.FALSE);
		agg.setParentVO(vo);
		return agg;
	}
	/**
	 * 获取人员工作主职记录主键
	 * @param pk_psndoc
	 * @return
	 * @throws Exception
	 */
	private PsnJobVO getPk_psnjob(String pk_psndoc) throws Exception{
		String sql = "select * from hi_psnjob where ismainjob='Y' and endflag='N' and poststat='Y' and lastflag='Y' and pk_psndoc='" + pk_psndoc + "'";
		PsnJobVO jobvo = (PsnJobVO) new BaseDAO().executeQuery(sql, new BeanProcessor(PsnJobVO.class));
		return jobvo;
	}


	/**
	 *获取参照信息
	 * 
	 */
	public String getHrOrgs(String param) throws Exception {
		PageResult result = new PageResult();
		List<Map<String, Object>> orgList = new TrnQueryDAO().getHrOrgs(InvocationInfoProxy.getInstance().getUserId());
		JSONArray dataArr = new JSONArray() ;
		
		if(orgList != null && orgList.size() > 0){
			for(Map<String, Object> map : orgList){
				JSONObject jo = new JSONObject() ;
				jo.put("pk_org", map.get("pk_org"));
				jo.put("name", map.get("name"));
				jo.put("code", map.get("code"));
				jo.put("pid", map.get("pk_fatherorg"));
				jo.put("innercode", map.get("innercode"));
				dataArr.add(jo);
			}
		}
		if(dataArr.size()==0){
			result.setData(null);
			result.setStatusCode(300);
			return result.toJson();
		}
		result.setData(dataArr);
		result.pushDevInfo("param", param);
		return result.toJson();
	}

	/**
	 * 获取数据库中已保存的数据
	 */
	@Override
	public String queryVOData(Map<String, Object> param) throws Exception {
		String pk_h = param.get("pk_h")==null?"":param.get("pk_h").toString();
		String userId = param.get("userId").toString();
		Map<String, Object> voData = new TrnQueryDAO().getTrnVOData(pk_h, userId);
		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(voData);
		return result.toJson();
	}


	@Override
	public String deleteVOData(Map<String, Object> param) throws Exception {
		String pk_h = param.get("pk_h").toString();
		Map<String,Object> map = new HashMap<String,Object>();
		if(StringUtil.isEmpty(pk_h)){
			throw new BusinessException("不存在该单据。");
		} else {
			checkState(pk_h);
			deleteVO(pk_h);
			map.put("flag", 2);
		}
		PageResult result = new PageResult();
		result.setData(map);
		return result.toJson();
	}
	
	private void checkState(String pk_h) throws Exception{
		String sql = "select * from hi_stapply where pk_hi_stapply=?";
		SQLParameter par = new SQLParameter();
		par.addParam(pk_h);
		List<StapplyVO> agg = (List<StapplyVO>) new BaseDAO().executeQuery(sql,par, new BeanListProcessor(StapplyVO.class));
		if(agg==null||agg.size()<=0){
			throw new BusinessException("不存在该单据。");
		}
		StapplyVO vo = (StapplyVO)agg.get(0);
		if(vo.getApprove_state()!= -1){
			throw new BusinessException("该单据不为自由态，不允许操作");
		}
	}
	
	private void deleteVO(String pk_h) throws Exception{
		String sql = "delete from hi_stapply where pk_hi_stapply = ?";
		SQLParameter par = new SQLParameter();
		par.addParam(pk_h);
		new BaseDAO().executeUpdate(sql,par);
	}


    protected static IMDPersistenceQueryService getMDQueryService()
    {
        return MDPersistenceService.lookupPersistenceQueryService();
    }
	
	@Override
	public String submitTrns(Map<String, Object> param) throws Exception {
		String pk = param.get("billKey").toString();
		
		String billtype = param.get("billtype").toString();
		
		Collection<?> c = getMDQueryService().queryBillOfVOByCond(AggStapply.class, "pk_hi_stapply='"+ pk+"'", true);
		
		AggStapply[] aggvo = null;
		if (c != null && c.size() > 0)
        {
			aggvo = c.toArray((AggStapply[]) Array.newInstance(AggStapply.class, c.size()));
        }
		PfProcessBatchRetObject validateRetObj = ((IHrPf) NCLocator.getInstance().lookup(IHrPf.class))
				.submitValidation("Commit","Commit",null,
						SysInitQuery.getParaInt(((StapplyVO)aggvo[0].getParentVO()).getPk_org(),
								(String) IHrPf.hashBillTypePara.get(billtype.equals("dimission")?"6115":"6113")).intValue(),
								new AggregatedValueObject[] { aggvo[0] });
		if ((validateRetObj.getRetObj() == null)|| (validateRetObj.getRetObj().length == 0)) {
			String errStr = validateRetObj.getExceptionMsg();
			if (StringUtils.isNotBlank(errStr))
				throw new BusinessException(errStr);
		}
		HashMap<String, String> eParam = new HashMap();
		if (isDirectApprove(((StapplyVO)aggvo[0].getParentVO()).getPk_org(), billtype.equals("dimission")?"6115":"6113")) {
			eParam.put("nosendmessage", "nosendmessage");
		}
		((StapplyVO)aggvo[0].getParentVO()).setApply_date(PubEnv.getServerLiteralDate());
		LfwPfUtil.runAction("SAVE", billtype.equals("dimission")?"6115":"6113", aggvo[0], null, null, null, eParam,
				new String[] { param.get("userId").toString() }, null);

		Map<String, Object> resultm = new HashMap();
		resultm.put("flag", "2");
		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(resultm);
		return result.toJson();
	}
	
	/**
	 * 编制校验
	 * @param pk_org
	 * @param billtype
	 * @return
	 * @throws BusinessException
	 */
	
	
	public boolean isDirectApprove(String pk_org, String billtype)
			throws BusinessException {
		Integer type = SysInitQuery.getParaInt(pk_org,
				(String) IHrPf.hashBillTypePara.get(billtype));
		return (type != null) && (type.intValue() == 0);
	}

	@Override
	public String callbackTrns(Map<String, Object> param) throws Exception {
		String pk = param.get("billKey").toString();
		String billtype = param.get("billtype").toString();
		Collection<?> c = getMDQueryService().queryBillOfVOByCond(AggStapply.class, "pk_hi_stapply='"+ pk+"'", false);
		
		AggStapply[] aggvo = null;
		if (c != null && c.size() > 0)
        {
			aggvo = c.toArray((AggStapply[]) Array.newInstance(AggStapply.class, c.size()));
        }
		HashMap<String, String> eParam = new HashMap();
		LfwPfUtil.runAction("RECALL", billtype.equals("dimission")?"6115":"6113", aggvo[0], null, null, null, eParam,new String[] { param.get("userId").toString() }, null);
		Map<String, Object> resultm = new HashMap();
		resultm.put("flag", "2");
		resultm.put("approve_state", ((StapplyVO)aggvo[0].getParentVO()).getApprove_state());
		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(resultm);
		return result.toJson();
	}

	@Override
	public String validateValidBudget(Map<String, Object> param)
			throws Exception {
		String pk = param.get("billKey").toString();
		String billtype = param.get("billtype").toString();
		Collection<?> c = getMDQueryService().queryBillOfVOByCond(AggStapply.class, "pk_hi_stapply='"+ pk+"'", false);
		
		AggStapply[] aggvo = null;
		if (c != null && c.size() > 0)
        {
			aggvo = c.toArray((AggStapply[]) Array.newInstance(AggStapply.class, c.size()));
        }
		TrnsValidateValidBudget ce = new TrnsValidateValidBudget();
		String message = ce.validateValidBudget(aggvo);
		return message;
	}
}
