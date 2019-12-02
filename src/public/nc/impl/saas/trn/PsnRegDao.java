package nc.impl.saas.trn;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.hr.utils.MultiLangHelper;
import nc.impl.saas.hi.LfwPfUtil;
import nc.impl.saas.hi.TBMHelper;
import nc.itf.hr.pf.IHrPf;
import nc.itf.trn.regmng.IRegmngManageService;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.jdbc.framework.processor.MapProcessor;
import nc.pubitf.para.SysInitQuery;
import nc.vo.hi.psndoc.PsnJobVO;
import nc.vo.hi.psndoc.TrialVO;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.VOStatus;
import nc.vo.pub.billtype.BilltypeVO;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.pf.IPfRetCheckInfo;
import nc.vo.trn.regitem.TrnRegItemVO;
import nc.vo.trn.regmng.AggRegapplyVO;
import nc.vo.trn.regmng.RegapplyVO;
import nc.vo.uap.pf.PfProcessBatchRetObject;

public class PsnRegDao {
	
	public PsnJobVO queryPsnjob(String pk_psndoc) throws DAOException {
		String psnjobSql = "pk_psndoc=? and ismainjob='Y' and lastflag='Y' and endflag='N'";
		SQLParameter para = new SQLParameter();
		para.addParam(pk_psndoc);
		PsnJobVO[] psnjobVOs = (PsnJobVO[])new BaseDAO().retrieveByClause(PsnJobVO.class, psnjobSql, para).toArray(new PsnJobVO[0]);
		return ArrayUtils.isEmpty(psnjobVOs)?null:psnjobVOs[0];
	}
	
	public TrialVO[] queryTrial(String pk_psnorg) throws DAOException {
		String trialSql = "pk_psnorg=? and lastflag='Y'";
		SQLParameter para = new SQLParameter();
		para.addParam(pk_psnorg);
		TrialVO[] TrialVOs = (TrialVO[])new BaseDAO().retrieveByClause(TrialVO.class, trialSql, para).toArray(new TrialVO[0]);
		return TrialVOs;
	}
	
	public TrnRegItemVO[] queryRegItems(Integer probation_type, String pk_org, String pk_group) throws DAOException {
		String sql = "select b.*,a.data_type as fldtype " +
				"from hi_reg_itemset b " +
				"inner join HR_INFOSET_ITEM a " +
				"on a.pk_infoset_item=b.pk_infoset_item where b.isedit = 'Y' and " +
				"b.probation_type=? and b.pk_org=? and b.pk_group=? order by disorder";
		SQLParameter para = new SQLParameter();
		para.addParam(probation_type);
		para.addParam(pk_org);
		para.addParam(pk_group);
		List<TrnRegItemVO> itemList = (List<TrnRegItemVO>)new BaseDAO().executeQuery(sql, para, new BeanListProcessor(TrnRegItemVO.class));
		if(itemList !=null && itemList.size()>0){
			return itemList.toArray(new TrnRegItemVO[0]);
		}
		para = new SQLParameter();
		para.addParam(probation_type);
		para.addParam(pk_group);
		para.addParam(pk_group);
		itemList = (List<TrnRegItemVO>)new BaseDAO().executeQuery(sql, para, new BeanListProcessor(TrnRegItemVO.class));
		return itemList.toArray(new TrnRegItemVO[0]);
	}
	
	public Map<String, String> getOrgDeptPsnclName(String pk_psndoc) throws DAOException{

//		String psnjobSql = " select a.pk_psnjob pk_psnjob, a.pk_org as pk_org, b.name as pk_orgname,a.pk_dept as pk_dept,c.name as pk_deptname, a.pk_psncl as pk_psncl, d.name as pk_psnclname " +
//				" , e.POSTNAME as pk_postname, a.pk_post as pk_post, a.pk_postseries pk_postseries " +
//				" , f.POSTSERIESNAME as pk_postseriesname, a.pk_job as pk_job, g.jobname as pk_jobname, a.series as series" +
//				" , h.JOBTYPENAME as seriesname, a.PK_JOBGRADE, i.JOBGRADENAME as pk_jobgradename" +
//				" , a.pk_jobrank, j.JOBRANKNAME as pk_jobrankname, a.PK_JOB_TYPE, k.name as pk_job_typename, a.jobmode, l.name as jobmodename"+
//				" , a.deposemode, m.name as deposemodename, a.occupation,n.name as occupationname "+
//				" , a.WORKTYPE, o.name as worktypename, a.POSTSTAT as poststat "+
//				" from hi_psnjob a " +
//				" inner join org_orgs b on b.pk_org=a.pk_org " +
//				" left join org_dept c on c.pk_dept = a.pk_dept " +
//				" left join bd_psncl d on d.pk_psncl=a.pk_psncl " +
//				" left join om_post e on e.PK_POST = a.pk_post"+
//				" left join OM_POSTSERIES f on f.PK_POSTSERIES = a.pk_postseries "+
//				" left join om_job g on g.pk_job = a.pk_job"+
//				" left join OM_JOBTYPE h on h.pk_jobtype = a.series"+
//				" left join OM_JOBGRADE i on i.pk_jobgrade = a.pk_jobgrade "+
//				" left join om_jobrank j on j.pk_jobrank = a.pk_jobrank "+
//				" left join BD_DEFDOC k on k.PK_DEFDOC = a.PK_JOB_TYPE "+
//				" left join BD_DEFDOC l on l.PK_DEFDOC = a.jobmode "+
//				" left join BD_DEFDOC m on m.PK_DEFDOC = a.deposemode "+
//				" left join BD_DEFDOC n on n.PK_DEFDOC = a.occupation "+
//				" left join BD_DEFDOC o on o.PK_DEFDOC = a.WORKTYPE "+
//				" where pk_psndoc=? and ismainjob='Y' and lastflag='Y' and endflag='N'";
		String psnjobSql = " select a.pk_psnjob pk_psnjob, a.pk_org as pk_org, b.name as pk_orgname,a.pk_dept as pk_dept,c.name as pk_deptname, a.pk_psncl as pk_psncl, d.name as pk_psnclname " +
				" , e.POSTNAME as pk_postname, a.pk_post as pk_post, a.pk_postseries pk_postseries " +
				" , f.POSTSERIESNAME as pk_postseriesname, a.pk_job as pk_job, g.jobname as pk_jobname, a.series as series" +
				" , h.JOBTYPENAME as seriesname, a.PK_JOBGRADE, i.name as pk_jobgradename" +
				" , a.pk_jobrank, j.JOBRANKNAME as pk_jobrankname, a.PK_JOB_TYPE, k.name as pk_job_typename, a.jobmode, l.name as jobmodename"+
				" , a.deposemode, m.name as deposemodename, a.occupation,n.name as occupationname "+
				" , a.WORKTYPE, o.name as worktypename, a.POSTSTAT as poststat "+
				" ,a.jobglbdef1, p.name as jobglbdef1name, a.jobglbdef2, a.jobglbdef2 as jobglbdef2name,a.jobglbdef3, a.jobglbdef3 as jobglbdef3name,a.jobglbdef4, a.jobglbdef4 as jobglbdef4name,a.jobglbdef5, a.jobglbdef5 as jobglbdef5name"+//add by wt 
				//" ,a.jobglbdef6, p.name as jobglbdef6name "+//add by wt 
				" , p.name as jobglbdef6name "+
				" from hi_psnjob a " +
				" inner join org_orgs b on b.pk_org=a.pk_org " +
				" left join org_dept c on c.pk_dept = a.pk_dept " +
				" left join bd_psncl d on d.pk_psncl=a.pk_psncl " +
				" left join om_post e on e.PK_POST = a.pk_post"+
				" left join OM_POSTSERIES f on f.PK_POSTSERIES = a.pk_postseries "+
				" left join om_job g on g.pk_job = a.pk_job"+
				" left join OM_JOBTYPE h on h.pk_jobtype = a.series"+
//				" left join OM_JOBGRADE i on i.pk_jobgrade = a.pk_jobgrade "+ //update
				" left join om_joblevel i on i.pk_joblevel = a.pk_jobgrade "+
				" left join om_jobrank j on j.pk_jobrank = a.pk_jobrank "+
				" left join BD_DEFDOC k on k.PK_DEFDOC = a.PK_JOB_TYPE "+
				" left join BD_DEFDOC l on l.PK_DEFDOC = a.jobmode "+
				" left join BD_DEFDOC m on m.PK_DEFDOC = a.deposemode "+
				" left join BD_DEFDOC n on n.PK_DEFDOC = a.occupation "+
				" left join BD_DEFDOC o on o.PK_DEFDOC = a.WORKTYPE "+
				" left join bd_psndoc p on p.pk_psndoc = a.jobglbdef1 "+//add by wt 
				" where a.pk_psndoc=? and a.ismainjob='Y' and a.lastflag='Y' and a.endflag='N'";
		SQLParameter para = new SQLParameter();
		para.addParam(pk_psndoc);
		Map<String, String> map = (Map<String, String>) new BaseDAO().executeQuery(psnjobSql, para, new MapProcessor());
		return map==null?null:map;
	}
	
	public List<Map<String, Object>> queryOrgsForRef(String userid) throws DAOException {
		String curDate = new UFDate().toString();
		StringBuffer sb = new StringBuffer();
		sb.append("select t.pk_adminorg pk, t.pk_fatherorg,t.code,t.name,t.innercode from org_adminorg t ");
		sb.append(" where t.pk_adminorg in (select pk_adminorg from org_admin_enable) ");
		sb.append("  and t.innercode in (select substr(s.innercode, 0, length(t.innercode)) ");
		sb.append(" from org_adminorg s where s.pk_adminorg in ( ");
		sb.append(" select sm_subject_org.pk_org from sm_role ");
		sb.append(" inner join sm_user_role on sm_role.pk_role = sm_user_role.pk_role ");
		sb.append("   and sm_user_role.cuserid = ? and nvl(sm_user_role.disableDate, '9999-12-31') >= '"+curDate+"' ");
		sb.append("   and sm_user_role.enableDate <= ? ");
		sb.append(" inner join sm_subject_org on sm_role.pk_role = sm_subject_org.subjectid ");
		sb.append(" inner join org_adminorg on org_adminorg.pk_adminorg = sm_subject_org.pk_org ))");
//		sb.append(" where sm_role.role_code like '"+ISaasConst.ROLE_LEADER+"')) ");
		sb.append(" order by t.innercode, t.code ");
		SQLParameter para = new SQLParameter();
		para.addParam(userid);
		para.addParam(curDate);
		return (List<Map<String, Object>>) new BaseDAO().executeQuery(sb.toString(), para, new MapListProcessor());
	}
	
	public List<Map<String, Object>> querydeptsForRef(String pk_org) throws DAOException {
		StringBuffer sb = new StringBuffer();
		sb.append("select org_dept.innercode, org_dept.name name, org_dept.pk_dept pk ");
		sb.append(" , org_dept.pk_fatherorg, org_dept.pk_group, org_dept.pk_org, org_dept.hrcanceled ");
		sb.append(" , org_dept.innercode, org_dept.principal, org_dept.createdate, org_dept.displayorder ");
		sb.append(" , org_orgs.innercode org_code, org_orgs.name org_name ");
		sb.append(" from org_dept  ");
		sb.append(" inner join org_orgs on org_orgs.pk_org = org_dept.pk_org  ");
		sb.append(" inner join org_adminorg on org_dept.pk_org = org_adminorg.pk_adminorg and org_adminorg.enablestate = 2 ");
		sb.append(" where 11 = 11 ");
		sb.append(" and hrcanceled = 'N' and depttype <> 1 and ( org_dept.pk_org = ?  ");
		sb.append(" and org_dept.pk_org in ( select pk_adminorg from org_admin_enable ) and org_dept.enablestate = 2 ");
		sb.append(" and org_dept.hrcanceled = 'N' ) ");
		sb.append(" order by org_code, org_dept.displayorder, org_dept.innercode ");
		SQLParameter para = new SQLParameter();
		para.addParam(pk_org);
		return (List<Map<String, Object>>) new BaseDAO().executeQuery(sb.toString(), para, new MapListProcessor());
	}
	
	public List<Map<String, Object>> queryPsnclForRef() throws DAOException {
		StringBuffer sb = new StringBuffer();
		sb.append("select innercode, name, pk_psncl pk, parent_id pid from bd_psncl ");
		sb.append(" where 11 = 11 ");
		sb.append("  and ( enablestate = 2 ) and ( ( 1 = 1 ) ) ");
		sb.append(" order by innercode ");
		return (List<Map<String, Object>>) new BaseDAO().executeQuery(sb.toString(), new MapListProcessor());
	}

	public List<Map<String, Object>> queryRef(String sql, SQLParameter para) throws DAOException {
		if(para == null){
			return (List<Map<String, Object>>) new BaseDAO().executeQuery(sql, new MapListProcessor());
		}
		return (List<Map<String, Object>>) new BaseDAO().executeQuery(sql, para, new MapListProcessor());
	}
	
	public AggRegapplyVO savePsnReg(String userid, boolean needCheck, AggRegapplyVO aggVO) throws BusinessException {
		IRegmngManageService regM =  NCLocator
				.getInstance().lookup(IRegmngManageService.class);
		String pk_hi_regapply = aggVO.getParentVO().getPrimaryKey();
		AggRegapplyVO newAggVO = null;
		if (StringUtils.isBlank(pk_hi_regapply)) {
			String newBill_code = TBMHelper.getBillCode("6111",((RegapplyVO)aggVO.getParentVO()).getPk_group(),((RegapplyVO)aggVO.getParentVO()).getPk_org());
			((RegapplyVO)aggVO.getParentVO()).setBill_code(newBill_code);
			try{
				aggVO.getParentVO().setStatus(VOStatus.NEW);
				newAggVO = regM.insertBill(aggVO);
			} catch(BusinessException e){
				TBMHelper.rollbackBillCode("6111",((RegapplyVO)aggVO.getParentVO()).getPk_group(),((RegapplyVO)aggVO.getParentVO()).getPk_org(),newBill_code);
				throw new BusinessException(e.getMessage());
			}
		} else {
			aggVO.getParentVO().setStatus(VOStatus.UPDATED);
			newAggVO = regM.updateBill(aggVO, false);
		}
		return newAggVO;
	}
	
	public Map<String, Object> rollBackPsnReg(String userid, AggRegapplyVO newVO)
			throws BusinessException {
		String pk_org = ((RegapplyVO)newVO.getParentVO()).getPk_org();
		HashMap<String, String> eParam = new HashMap<String, String>();
		if (isDirectApprove(pk_org, "6111")) {
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
		LfwPfUtil.runAction("RECALL", "6111", newVO, null, null, null, eParam,
				new String[] { userid }, null);

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("flag", "2");
		return result;
	}

	public Map<String, Object> submitPsnReg(String userid, AggRegapplyVO newVO)
			throws BusinessException {
		String pk_org = ((RegapplyVO)newVO.getParentVO()).getPk_org();
		HashMap<String, String> eParam = new HashMap<String, String>();
		AggRegapplyVO[] voss = new AggRegapplyVO[1];
		voss[0] = newVO;
		if (isDirectApprove(pk_org, "6111")) {
			String sql = "update hi_regapply set approve_state = "+IPfRetCheckInfo.PASSING+" where pk_hi_regapply='" + voss[0].getParentVO().getPrimaryKey()+"'";
			new BaseDAO().executeUpdate(sql);
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("flag", "2");
			return result;
		}
 		PfProcessBatchRetObject validateRetObj = ((IHrPf) NCLocator.getInstance().lookup(IHrPf.class)).submitValidation("Commit","Commit",null,SysInitQuery.getParaInt(pk_org,(String) IHrPf.hashBillTypePara.get("6111")).intValue(),new AggregatedValueObject[] { newVO });
		if ((validateRetObj.getRetObj() == null)
				|| (validateRetObj.getRetObj().length == 0)) {
			String errStr = validateRetObj.getExceptionMsg();
			if (StringUtils.isNotBlank(errStr))
				throw new BusinessException(errStr);
		}
		LfwPfUtil.runAction("SAVE", "6111", newVO, null, null, null, eParam,
				new String[] { userid }, null);

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("flag", "2");
		return result;
	}
	
	public boolean isDirectApprove(String pk_org, String billtype)
			throws BusinessException {
		String sql = "select value from pub_sysinit where pk_org='" + pk_org+"' and initcode ='" + (String) IHrPf.hashBillTypePara.get(billtype) +"'";
		String types = (String) new BaseDAO().executeQuery(sql, new ColumnProcessor());
		if(types == null || types.equals("null") || types.equals("")){
			types = "0";
		}
		Integer type =Integer.valueOf(types);
		return (type != null) && (type.intValue() == 0);
	}
	
	public Map<String, String> getRefName(String pk_hi_regapply) throws DAOException{
		String psnjobSql = " select a.newpk_org as newpk_org, b.name as newpk_orgname,b1.name as oldpk_orgname " +
				" , a.newpk_dept as newpk_dept,c.name as newpk_deptname,c1.name as oldpk_deptname " +
				" , a.newpk_psncl as newpk_psncl, d.name as newpk_psnclname, d1.name as oldpk_psnclname " +
				" , e.POSTNAME as newpk_postname, e1.POSTNAME as oldpk_postname, a.newpk_post as newpk_post, a.newpk_postseries as newpk_postseries " +
				" , f.POSTSERIESNAME as newpk_postseriesname, f1.POSTSERIESNAME as oldpk_postseriesname " +
				" , a.newpk_job as newpk_job, g.jobname as newpk_jobname, g1.jobname as oldpk_jobname, a.newseries as newseries" +
				" , h.JOBTYPENAME as newseriesname, h1.JOBTYPENAME as oldseriesname, a.newPK_JOBGRADE, i.name as newpk_jobgradename, i1.name as oldpk_jobgradename " +
				" , a.newpk_jobrank, j.JOBRANKNAME as newpk_jobrankname,j1.JOBRANKNAME as oldpk_jobrankname, a.newPK_JOB_TYPE " +
				" , k.name as newpk_job_typename, k1.name as oldpk_job_typename, a.newjobmode, l.name as newjobmodename,l1.name as oldjobmodename "+
				" , a.newdeposemode, m.name as newdeposemodename, m1.name as olddeposemodename, a.newoccupation,n.name as newoccupationname,n1.name as oldoccupationname "+
				" , a.newWORKTYPE, o.name as newworktypename, o1.name as oldworktypename, a.newPOSTSTAT as newpoststat ,a.oldPOSTSTAT as oldpoststat "+
				" from hi_regapply a " +
				" inner join org_orgs b on b.pk_org=a.newpk_org " +
				" inner join org_orgs b1 on b1.pk_org=a.oldpk_org " +
				" left join org_dept c on c.pk_dept = a.newpk_dept " +
				" left join org_dept c1 on c1.pk_dept = a.oldpk_dept " +
				" left join bd_psncl d on d.pk_psncl=a.newpk_psncl " +
				" left join bd_psncl d1 on d1.pk_psncl=a.oldpk_psncl " +
				" left join om_post e on e.PK_POST = a.newpk_post"+
				" left join om_post e1 on e1.PK_POST = a.oldpk_post"+
				" left join OM_POSTSERIES f on f.PK_POSTSERIES = a.newpk_postseries "+
				" left join OM_POSTSERIES f1 on f1.PK_POSTSERIES = a.oldpk_postseries "+
				" left join om_job g on g.pk_job = a.newpk_job"+
				" left join om_job g1 on g1.pk_job = a.oldpk_job"+
				" left join OM_JOBTYPE h on h.pk_jobtype = a.newseries"+
				" left join OM_JOBTYPE h1 on h1.pk_jobtype = a.oldseries"+
//				" left join OM_JOBGRADE i on i.pk_jobgrade = a.newpk_jobgrade "+
//				" left join OM_JOBGRADE i1 on i1.pk_jobgrade = a.oldpk_jobgrade "+
				" left join om_joblevel i on i.pk_joblevel = a.newpk_jobgrade "+ // update by wt case:职级表出现错误
				" left join om_joblevel i1 on i1.pk_joblevel = a.oldpk_jobgrade "+
				" left join om_jobrank j on j.pk_jobrank = a.newpk_jobrank "+
				" left join om_jobrank j1 on j1.pk_jobrank = a.oldpk_jobrank "+
				" left join BD_DEFDOC k on k.PK_DEFDOC = a.newPK_JOB_TYPE "+
				" left join BD_DEFDOC k1 on k1.PK_DEFDOC = a.oldPK_JOB_TYPE "+
				" left join BD_DEFDOC l on l.PK_DEFDOC = a.newjobmode "+
				" left join BD_DEFDOC l1 on l1.PK_DEFDOC = a.oldjobmode "+
				" left join BD_DEFDOC m on m.PK_DEFDOC = a.newdeposemode "+
				" left join BD_DEFDOC m1 on m1.PK_DEFDOC = a.olddeposemode "+
				" left join BD_DEFDOC n on n.PK_DEFDOC = a.newoccupation "+
				" left join BD_DEFDOC n1 on n1.PK_DEFDOC = a.oldoccupation "+
				" left join BD_DEFDOC o on o.PK_DEFDOC = a.newWORKTYPE "+
				" left join BD_DEFDOC o1 on o1.PK_DEFDOC = a.oldWORKTYPE "+
				" where a.pk_hi_regapply = ? ";
		SQLParameter para = new SQLParameter();
		para.addParam(pk_hi_regapply);
		Map<String, String> map = (Map<String, String>) new BaseDAO().executeQuery(psnjobSql, para, new MapProcessor());
		return map==null?null:map;
	}
	
}
