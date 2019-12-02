package nc.impl.hrss.hi.trn;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.hr.utils.ResHelper;
import nc.impl.saas.hi.TBMAwayDao;
import nc.impl.saas.trn.PsnRegDao;
import nc.itf.hr.managescope.ManageScopeConst;
import nc.itf.uap.IUAPQueryBS;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.jdbc.framework.processor.BeanProcessor;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.md.persist.framework.IMDPersistenceQueryService;
import nc.md.persist.framework.MDPersistenceService;
import nc.pubitf.rbac.IUserPubService;
import nc.vo.hi.psndoc.PsnJobVO;
import nc.vo.hi.psndoc.PsndocVO;
import nc.vo.hi.psndoc.enumeration.PsnType;
import nc.vo.hr.managescope.HrRelationDeptVO;
import nc.vo.hr.managescope.HrRelationPsnVO;
import nc.vo.hr.managescope.ManagescopeBusiregionEnum;
import nc.vo.iufo.hr.PubEnv;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDate;
import nc.vo.sm.UserVO;
import nc.vo.trn.pub.TRNConst;
import nc.vo.trn.transitem.TrnTransItemVO;
import nc.vo.trn.transmng.AggStapply;
import nc.vo.trn.transmng.StapplyVO;

import org.apache.commons.lang.StringUtils;
import org.mozilla.javascript.edu.emory.mathcs.backport.java.util.Arrays;

public class TrnQueryDAO {
    private BaseDAO baseDAO;
	@SuppressWarnings("unchecked")
	public TrnTransItemVO[] queryTrnTransItems(String pk_transtype, String pk_org,
			String pk_group) throws DAOException {
		String sql = "select b.*,a.data_type as fldtype "
				+ "from hi_stbill_itemset b "
				+ "inner join HR_INFOSET_ITEM a "
				+ "on a.pk_infoset_item=b.pk_infoset_item where "
				+ "b.pk_sttype=? and b.pk_org=? and b.pk_group=? order by disorder";
		SQLParameter para = new SQLParameter();
		para.addParam(pk_transtype);
		para.addParam(pk_org);
		para.addParam(pk_group);
		List<TrnTransItemVO> itemList = (List<TrnTransItemVO>) new BaseDAO()
				.executeQuery(sql, para, new BeanListProcessor(
						TrnTransItemVO.class));
		if (itemList != null && itemList.size() > 0) {
			return itemList.toArray(new TrnTransItemVO[0]);
		}
		para = new SQLParameter();
		para.addParam(pk_transtype);
		para.addParam(pk_group);
		para.addParam(pk_group);
		itemList = (List<TrnTransItemVO>) new BaseDAO().executeQuery(sql, para,
				new BeanListProcessor(TrnTransItemVO.class));
		return itemList.toArray(new TrnTransItemVO[0]);
	}
	
	public PsnJobVO getPk_psnjob(String userId) throws Exception{
		
		String sql = " select hi_psnjob.* from hi_psnjob left join sm_user on sm_user.pk_psndoc = hi_psnjob.pk_psndoc where sm_user.cuserid='" + userId +"' " +
				" and hi_psnjob.ismainjob='Y' and hi_psnjob.endflag='N' and hi_psnjob.POSTSTAT='Y' and hi_psnjob.lastflag='Y'";
		ArrayList<PsnJobVO> vo =  (ArrayList<PsnJobVO>) new BaseDAO().executeQuery(sql, new BeanListProcessor(PsnJobVO.class));
		return vo.toArray(new PsnJobVO[0])[0];
	}

	public List<Map<String,Object>> getHrOrgs(String userid) throws Exception{
		String curDate = new UFDate().toString();
		StringBuffer sb = new StringBuffer();
		sb.append("select t.pk_hrorg pk_org, t.pk_fatherorg,t.code,t.name,t.innercode from org_hrorg t ");
		sb.append(" where 11 = 11   and (enablestate = 2) and ((pk_group = ?))");
		sb.append(" order by t.innercode, t.code ");
		SQLParameter para = new SQLParameter();
		para.addParam(PubEnv.getPk_group());
		return (List<Map<String, Object>>) new BaseDAO().executeQuery(sb.toString(), para, new MapListProcessor());
	}
	
	// 获取相应的人事管理组织 和 合同管理组织 
	
	public String getHrcmOrg(String psnorgPk, Integer assgid, ManagescopeBusiregionEnum busiregionEnum) throws BusinessException{
		String[] hrorgPks = null;
        String psnCondition = PsnJobVO.PK_PSNORG + "='" + psnorgPk + "' and " + PsnJobVO.ASSGID + "=" + assgid;
        
        // PsnJobVO psnjob = getIPsndocQryService().queryAvailablePsnjobByCondition(psnCondition, null)[0];
        Collection<?> c = new BaseDAO().retrieveByClause(PsnJobVO.class, psnCondition);
        PsnJobVO[] psnjobArray = null;
        
//        if (c == null || c.size() != 1)
        if (c == null)//update by wt 
        {
            Logger.error("此次查询的人员不存在或者和管理范围没有关系！psnorgPk=" + psnorgPk + " assgid=" + assgid);
            throw new BusinessException(ResHelper.getString("6005mngscp", "06005mngscp0126")/* @res "此次查询的人员不存在或者和管理范围没有关系！" */);
        }else {
        	psnjobArray = c.toArray((PsnJobVO[])(Array.newInstance(PsnJobVO.class, c.size())));
        }
        
        PsnJobVO psnjob = psnjobArray[0];
        
        // 兼职人员的人事业务没有HR组织
        if (!psnjob.getIsmainjob().booleanValue() && busiregionEnum == ManagescopeBusiregionEnum.psndoc)
        {
            return null;
        }
        
        // 相关人员的合同业务没有HR组织
        if (psnjob.getPsntype() == Integer.parseInt(PsnType.POI.getEnumValue().getValue())
            && ManagescopeBusiregionEnum.psnpact == busiregionEnum)
        {
            return null;
        }
        
        String attr = busiregionEnum.getCode() + ManageScopeConst.PROPERTY_BUSI;
        
        // 先查询人员委托关系表如果有则找到，如果没有则找部门委托关系表
        String condition =
            HrRelationPsnVO.PK_PSNORG + "=? and " + HrRelationPsnVO.ASSGID + "=? and " + attr + "=?";
        SQLParameter param = new SQLParameter();
        param.addParam(psnorgPk);
        param.addParam(assgid);
        param.addParam("Y");
        Collection<HrRelationPsnVO> collRelationPsnVO = getBaseDAO().retrieveByClause(HrRelationPsnVO.class, condition, param);
        HrRelationPsnVO[] hrRelationPsnVOs = collRelationPsnVO.toArray(new HrRelationPsnVO[0]);
        
        if (hrRelationPsnVOs != null && hrRelationPsnVOs.length > 0)
        {
            hrorgPks = new String[hrRelationPsnVOs.length];
            for (int i = 0; i < hrorgPks.length; i++)
            {
                hrorgPks[i] = hrRelationPsnVOs[i].getPk_hrorg();
            }
        }
        else
        {
            // 找部门委托关系
            String pkDept = psnjob.getPk_dept();
            String deptCondition = HrRelationDeptVO.PK_DEPT + "=? and " + attr + "=?";
            SQLParameter param1 = new SQLParameter();
            param1.addParam(pkDept);
            param1.addParam("Y");
            Collection<HrRelationDeptVO> collRelationDeptVO = getBaseDAO().retrieveByClause(HrRelationDeptVO.class, deptCondition, param1);
            HrRelationDeptVO[] hrRelationDeptVOs = collRelationDeptVO.toArray(new HrRelationDeptVO[0]);
            if (hrRelationDeptVOs != null)
            {
                hrorgPks = new String[hrRelationDeptVOs.length];
                for (int i = 0; i < hrorgPks.length; i++)
                {
                    hrorgPks[i] = hrRelationDeptVOs[i].getPk_hrorg();
                }
            }
        }
        if(hrorgPks!=null&&hrorgPks.length>0){
        	return hrorgPks[0];
        }else {
        	return null;
        }
	}
    private BaseDAO getBaseDAO()
    {
        if (baseDAO == null)
        {
            baseDAO = new BaseDAO();
        }
        
        return baseDAO;
    }
    
    public Map<String,Object> getPsnTemplet(String pk_trnstype,String userId,Integer stapply_mode, Map<?, ?> map) throws BusinessException{
//    	UserVO vos = NCLocator.getInstance().lookup(IUserPubService.class).getUsersByPKs(new String[]{userId})[0];
//		String pk_psndoc = NCLocator.getInstance().lookup(IUserPubService.class).queryPsndocByUserid(userId);
    	String sql = "select * from bd_psndoc where pk_psndoc='"+userId+"'";//update wt 20191028 case:调配人
    	PsndocVO vos = (PsndocVO) new BaseDAO().executeQuery(sql, new BeanProcessor(PsndocVO.class));
		if (StringUtils.isBlank(pk_trnstype))
        {
            return null;
        }
		TrnTransItemVO[] itemVOs = new TrnQueryDAO().queryTrnTransItems(pk_trnstype, vos.getPk_org(), vos.getPk_group());
		
		String[] order = new String[]{"oldpk_org","oldpk_psncl","oldpk_dept","oldpk_post","newpk_org","newpk_psncl","newpk_dept","newpk_post"};
		
		Map<String, Object> oldMap;
		Map<String, Object> newMap;
		
		List<Map<String, Object>> oldList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> newList = new ArrayList<Map<String, Object>>();
		Map<String, String> jobvo = new PsnRegDao().getOrgDeptPsnclName(vos.getPk_psndoc()); // 人员工作记录vo
		for (String or:order){
			for(TrnTransItemVO vo : itemVOs){
				String itemkey = vo.getItemkey();
				if(itemkey.startsWith("old")&&itemkey.equals(or)){
					oldMap = new HashMap<String, Object>();
					oldMap.put("itemKey", vo.getItemkey());
					oldMap.put("itemName", vo.getItemname().replace("新", ""));
					oldMap.put("datatype", vo.getFldtype());
					oldMap.put("isrequired", vo.getIsmustnotnull()==null?false:vo.getIsmustnotnull().booleanValue());
					oldMap.put("value", jobvo.get(itemkey.substring(3)));
					oldMap.put("displayname", jobvo.get(itemkey.substring(3)+"name")==null?"":jobvo.get(itemkey.substring(3)+"name"));
					oldList.add(oldMap);
				}else if(itemkey.startsWith("new")&&itemkey.equals(or)){
					newMap = new HashMap<String, Object>();
					newMap.put("itemKey", vo.getItemkey());
					newMap.put("itemName", vo.getItemname().replace("新", ""));
					newMap.put("datatype", vo.getFldtype());
					newMap.put("isrequired", vo.getIsmustnotnull()==null?false:vo.getIsmustnotnull().booleanValue());
					if(map!=null){
						newMap.put("value", map.get(itemkey));
						newMap.put("displayname", map.get(itemkey.substring(3)+"name")==null?"":map.get(itemkey.substring(3)+"name"));
					}else {
						newMap.put("value", jobvo.get(itemkey.substring(3)));
						newMap.put("displayname", jobvo.get(itemkey.substring(3)+"name")==null?"":jobvo.get(itemkey.substring(3)+"name"));
					}
					newList.add(newMap);
				}
			}
		}
		for(TrnTransItemVO vo : itemVOs){
			if(!Arrays.asList(order).contains(vo.getItemkey())){
				String itemkey = vo.getItemkey();
				if(itemkey.startsWith("old")){
					oldMap = new HashMap<String, Object>();
					oldMap.put("itemKey", vo.getItemkey());
					oldMap.put("itemName", vo.getItemname().replace("新", ""));
					oldMap.put("datatype", vo.getFldtype());
					oldMap.put("isrequired", vo.getIsmustnotnull()==null?false:vo.getIsmustnotnull().booleanValue());
					oldMap.put("value", jobvo.get(itemkey.substring(3)));
					oldMap.put("displayname", jobvo.get(itemkey.substring(3)+"name")==null?"":jobvo.get(itemkey.substring(3)+"name"));
					oldList.add(oldMap);
				}else if (itemkey.startsWith("new")){
					newMap = new HashMap<String, Object>();
					newMap.put("itemKey", vo.getItemkey());
					newMap.put("itemName", vo.getItemname().replace("新", ""));
					newMap.put("datatype", vo.getFldtype());
					newMap.put("isrequired", vo.getIsmustnotnull()==null?false:vo.getIsmustnotnull().booleanValue());
					if(map!=null){
						newMap.put("value", map.get(itemkey));
						newMap.put("displayname", map.get(itemkey.substring(3)+"name")==null?"":map.get(itemkey.substring(3)+"name"));
					}else {
						newMap.put("value", jobvo.get(itemkey.substring(3)));
						newMap.put("displayname", jobvo.get(itemkey.substring(3)+"name")==null?"":jobvo.get(itemkey.substring(3)+"name"));
					}
					newList.add(newMap);
				}
			}
		}
		List<Map<String,Object>> crtAndManager = getCrtAndManager(stapply_mode,jobvo.get("pk_psnjob"));
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("oldmap", oldList);
		data.put("newmap", newList);
		data.put("crtManage", crtAndManager);
		if (map!=null) {
			String pk_hi_org = map.get("pk_hi_org").toString();
			String pk_hrcm_org = map.get("pk_hrcm_org").toString();
			String pk_hi_orgname = map.get("pk_hi_orgname").toString();
			String pk_hrcm_orgname = map.get("pk_hrcm_orgname").toString();
			Map<String,Object> newcrt = new HashMap<String,Object>();
			newcrt.put(StapplyVO.PK_HI_ORG, pk_hi_org);
			newcrt.put("hiorg_name", pk_hi_orgname);
			newcrt.put(StapplyVO.PK_HRCM_ORG, pk_hrcm_org);
			newcrt.put("hrcmorg_name", pk_hrcm_orgname);
			data.put("newcrtmanage", newcrt);
		}
    	return data;
    }
	public List<Map<String, Object>> getCrtAndManager(Integer transMode,String pk_psnjob) throws BusinessException{
			
			PsnJobVO psn = (PsnJobVO) new BaseDAO().retrieveByPK(PsnJobVO.class, pk_psnjob);//getService().queryByPk(PsnJobVO.class, pk_psnjob, true);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String,Object> map = new HashMap<String,Object>();
	    // 原合同管理组织应该是什么时候都需要查委托关系
	    if (transMode == null || TRNConst.TRANSMODE_INNER == transMode || TRNConst.TRANSMODE_CROSS_OUT == transMode || TRNConst.TRNSEVENT_DIMIS == transMode)
	    {
	    	String pk_old_hi_org = new TrnQueryDAO().getHrcmOrg(psn.getPk_psnorg(), psn.getAssgid(), ManagescopeBusiregionEnum.psndoc);
	    	String oldhiname = getHrOrgName(pk_old_hi_org);
	    	String oldhrcm = new TrnQueryDAO().getHrcmOrg(psn.getPk_psnorg(), psn.getAssgid(), ManagescopeBusiregionEnum.psnpact);
	    	String oldhrcmname = getHrOrgName(oldhrcm);
	        // 如果是离职、内调、调出， 原人事组织都是当前hr组织
	    	map.put(StapplyVO.PK_OLD_HI_ORG, pk_old_hi_org);
	    	map.put("oldhi_name", oldhiname);
	    	map.put(StapplyVO.PK_OLD_HRCM_ORG, oldhrcm);
	    	map.put("oldhrcm_name", oldhrcmname);
	    }
	    list.add(map);
	    return list;
	}
	
	
	public String getHrOrgName(String pk_org) throws BusinessException{
		
		String sql = "select name from org_hrorg where pk_hrorg ='" + pk_org+ "'";
		String name = (String) new BaseDAO().executeQuery(sql, new ColumnProcessor());
		
		return name;
	}
	
    protected static IMDPersistenceQueryService getMDQueryService()
    {
        return MDPersistenceService.lookupPersistenceQueryService();
    }
	
	public AggStapply getAggVO(String pk_h) throws BusinessException{
		Collection<?> c = getMDQueryService().queryBillOfVOByCond(AggStapply.class, "pk_hi_stapply='"+ pk_h+"'", false);
		
		AggStapply[] aggvo = null;
		if (c != null && c.size() > 0)
        {
			aggvo = c.toArray((AggStapply[]) Array.newInstance(AggStapply.class, c.size()));
        }
		
		return aggvo[0];
	}    
    public Map<String,Object> getTrnVOData(String pk_h,String userId) throws BusinessException{
    	
    	StringBuffer stb = new StringBuffer();
    	stb.append(" select a.stapply_mode,a.bill_code,bd.pk_psndoc,a.pk_trnstype,t.TRNSTYPENAME TRNSTYPENAME,a.transtype, ");
    	stb.append(" a.transtypeid,v.BILLTYPENAME transtypename,a.pk_billtype,a.sreason, ");
    	stb.append("  u.name sreasonname,a.effectdate,a.memo,a.NEWDEPOSEMODE,b.name NEWDEPOSEMODENAME, ");
    	stb.append(" a.NEWJOBMODE,c.NAME NEWJOBMODENAME,a.NEWMEMO,a.NEWOCCUPATION,d.name OCCUPATIONNAME,");
    	stb.append(" a.NEWPK_DEPT,e.name pk_deptname,a.newpk_job,f.JOBNAME pk_jobname,a.NEWPK_JOB_TYPE,");
    	stb.append(" g.NAME pk_job_typename,a.NEWPK_JOBGRADE,h.NAME pk_jobgradename,a.NEWPK_JOBRANK,");
    	stb.append(" i.JOBRANKNAME pk_jobrankname,a.newpk_org,j.NAME pk_orgname,a.NEWPK_POST,k.POSTNAME pk_postname,");
    	stb.append(" a.NEWPK_POSTSERIES,l.POSTSERIESNAME PK_POSTSERIESNAME,a.NEWPK_PSNCL,m.NAME pk_psnclname, ");
    	stb.append(" a.NEWPOSTSTAT,a.NEWSERIES,n.JOBTYPENAME SERIES,a.NEWWORKTYPE,o.NAME worktypename, ");
    	stb.append(" a.PK_GROUP,a.PK_HI_ORG,p.name pk_hi_orgname,a.PK_HRCM_ORG,q.NAME pk_hrcm_orgname, ");
    	stb.append("  a.pk_org,r.name orgname,a.pk_psndoc,s.NAME psnname,a.pk_psnjob,a.pk_psnorg,a.isend,a.ifsynwork,a.ifendpart,a.isrelease,a.isdisablepsn,a.ifaddblack ");
    	stb.append(" from hi_stapply a ");
    	stb.append(" left join bd_defdoc b on a.NEWDEPOSEMODE = b.PK_DEFDOC ");
    	stb.append(" left join bd_defdoc c on a.NEWJOBMODE = c.PK_DEFDOC left join bd_defdoc d on a.NEWOCCUPATION = d.pk_defdoc");
    	stb.append(" left join org_dept e on e.PK_DEPT = a.NEWPK_DEPT  left join om_job f on f.PK_JOB = a.NEWPK_JOB ");
    	stb.append(" left join bd_defdoc g on a.NEWPK_JOB_TYPE = g.PK_DEFDOC  left join OM_JOBLEVEL h on h.PK_JOBLEVEL = a.NEWPK_JOBGRADE ");
    	stb.append(" left join OM_JOBRANK i on i.PK_JOBRANK = a.NEWPK_JOBRANK left join ORG_ADMINORG j on j.PK_ADMINORG = a.NEWPK_ORG ");
    	stb.append(" left join om_post k on k.PK_POST = a.NEWPK_POST left join OM_POSTSERIES l on k.PK_POSTSERIES = l.PK_POSTSERIES");
    	stb.append("               left join BD_PSNCL m on m.PK_PSNCL = a.NEWPK_PSNCL left join OM_JOBTYPE n on n.PK_JOBTYPE = a.NEWSERIES ");
    	stb.append("        	   left join bd_defdoc o on o.PK_DEFDOC = a.NEWWORKTYPE left join ORG_HRORG p on p.PK_HRORG = a.PK_HI_ORG ");
    	stb.append("               left join ORG_HRORG q on q.PK_HRORG = a.PK_HRCM_ORG left join ORG_HRORG r on r.PK_HRORG = a.PK_ORG");
    	stb.append("               left join bd_psndoc s on s.pk_psndoc = a.PK_PSNDOC left join HR_TRNSTYPE t on t.PK_TRNSTYPE = a.PK_TRNSTYPE ");
    	stb.append("               left join bd_defdoc u on u.PK_DEFDOC = a.SREASON left join bd_billtype v on v.PK_BILLTYPEID = a.TRANSTYPEID ");
//    	stb.append("               left join sm_user sm on sm.pk_base_doc = a.pk_psndoc ");
    	stb.append("               left join bd_psndoc bd on bd.pk_psndoc = a.pk_psndoc ");//update by wt 20191028 case:调配单据由用户转为人员
    	
    	stb.append(" where pk_hi_stapply =?");
    	
    	SQLParameter param = new SQLParameter();
    	param.addParam(pk_h);
    	
    	ArrayList<?> list = (ArrayList<?>) getBaseDAO().executeQuery(stb.toString(), param, new MapListProcessor());
    	
    	if(list!=null && list.size()>0){
    		Map<?,?> map =(Map<?, ?>) list.get(0);
//    		Map<String, Object> templet = getPsnTemplet(map.get("pk_trnstype").toString(), map.get("cuserid").toString(), Integer.valueOf(map.get("stapply_mode").toString()),map);
    		Map<String, Object> templet = getPsnTemplet(map.get("pk_trnstype").toString(), map.get("pk_psndoc").toString(), Integer.valueOf(map.get("stapply_mode").toString()),map);//update by wt 20191028 case:调配单据由用户转为人员
    		Map<String,Object> innermap = new HashMap<String,Object>();
    		innermap.put("transtypevalue", Integer.valueOf(map.get("stapply_mode").toString()));
    		innermap.put("pk_trnstype", map.get("pk_trnstype").toString()); //trnreasonname
    		innermap.put("trnstypename", map.get("trnstypename").toString()); 
    		innermap.put("transtypename", map.get("transtypename")==null?"":map.get("transtypename").toString()); 
    		innermap.put("transtypeid", map.get("transtypeid")==null?"":map.get("transtypeid").toString());
    		//trnreasonname
    		innermap.put("trnreasonname", map.get("sreasonname")==null?"":map.get("sreasonname").toString());
    		innermap.put("pk_trnreason", map.get("sreason")==null?"":map.get("sreason").toString());
    		innermap.put("psnname", map.get("psnname").toString());
    		innermap.put("pk_psndoc", map.get("pk_psndoc").toString());//add by wt 20191028 case:调配人
    		innermap.put("effecttime", map.get("effectdate").toString());
    		innermap.put("memo", map.get("memo")==null?"":map.get("memo").toString());
    		innermap.put("isend", map.get("isend")==null?false:map.get("isend").toString().equals("Y")?true:false);
    		innermap.put("ifsynwork", map.get("ifsynwork")==null?false:map.get("ifsynwork").toString().equals("Y")?true:false);
    		innermap.put("ifendpart", map.get("ifendpart")==null?false:map.get("ifendpart").toString().equals("Y")?true:false);
    		innermap.put("isrelease", map.get("isrelease")==null?false:map.get("isrelease").toString().equals("Y")?true:false);
    		innermap.put("isdisablepsn", map.get("isdisablepsn")==null?false:map.get("isdisablepsn").toString().equals("Y")?true:false);
    		innermap.put("ifaddblack", map.get("ifaddblack")==null?false:map.get("ifaddblack").toString().equals("Y")?true:false);
    		ArrayList<Map<String,Object>> workFlowNote = new TBMAwayDao().queryWorkFlowNote(map.get("transtype").toString(), map.get("bill_code").toString(), pk_h);
    		innermap.put("workflownote", workFlowNote);
    		templet.put("innermap", innermap);
    		
    		//2019/11/28添加
    		StringBuffer sqlBuffer = new StringBuffer();
    		sqlBuffer.append(" select cuserid ");
    		sqlBuffer.append(" from sm_user ");
    		sqlBuffer.append(" where pk_psndoc in ( SELECT glbdef3 FROM org_dept ");
    		sqlBuffer.append("     WHERE pk_org in (select org_adminorg.pk_adminorg ");
    		sqlBuffer.append("                      from org_adminorg ");
    		sqlBuffer.append("                      where org_adminorg.enablestate = 2 ");
    		sqlBuffer.append("                        and org_adminorg.innercode like ");
    		sqlBuffer.append("                            (select innercode || '%' from org_adminorg where pk_adminorg = '0001A110000000002JK0') ");
    		sqlBuffer.append("                        and org_adminorg.pk_adminorg not in (select aosm.pk_adminorg from (select aos.code, aos.innercode, length(rtrim(aos.innercode, ' ')) innercodelen ");
    		sqlBuffer.append("                                           from org_hrorg hrorg inner join org_adminorg aos on aos.pk_adminorg = hrorg.pk_hrorg ");
    		sqlBuffer.append("                                           where aos.innercode like (select innercode || '%' from org_adminorg where pk_adminorg = '0001A110000000002JK0') ");
    		sqlBuffer.append("                                             and aos.pk_adminorg <> '0001A110000000002JK0' and hrorg.enablestate = 2) sub_hrorg, org_adminorg aosm ");
    		sqlBuffer.append("                                     where sub_hrorg.innercode = substr(aosm.innercode, 1, sub_hrorg.innercodelen)) ");
    		sqlBuffer.append("                        and org_adminorg.pk_adminorg in (select pk_adminorg from org_admin_enable))");
    		sqlBuffer.append("       and enablestate = 2 and hrcanceled = 'N' and 1 = 1 and glbdef3 is not null and glbdef3 <> '~' ");
    		sqlBuffer.append(" ) ");
    		templet.put("operatorPeople", "N");
    		ArrayList<?> cuseridList = (ArrayList<?>) getBaseDAO().executeQuery(sqlBuffer.toString(), null, new MapListProcessor());
    		if(cuseridList!=null && cuseridList.size()>0){
    			for(int i = 0; i < cuseridList.size(); i++){
    				Map<?,?> tempMap =(Map<?, ?>) cuseridList.get(0);
    				if(tempMap.get("cuserid").toString().equals(userId)){
    					templet.put("operatorPeople", "Y");
        				break;
    				}
    			}
    		}
    		return templet;
    	}else {
    		return null;
    	}
    }
}
