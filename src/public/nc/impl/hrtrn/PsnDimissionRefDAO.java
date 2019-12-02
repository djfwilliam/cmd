package nc.impl.hrtrn;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.hr.frame.persistence.SimpleDocServiceTemplate;
import nc.hr.utils.PubEnv;
import nc.impl.saas.hi.LfwPfUtil;
import nc.itf.hr.pf.IHrPf;
import nc.itf.ta.ITBMPsndocQueryMaintain;
import nc.jdbc.framework.processor.ArrayProcessor;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.pub.tools.HiSQLHelper;
import nc.pubitf.para.SysInitQuery;
import nc.pubitf.rbac.IUserPubService;
import nc.ui.bd.ref.model.DefdocDefaultRefModel;
import nc.vo.hi.psndoc.PsnJobVO;
import nc.vo.hr.managescope.ManagescopeBusiregionEnum;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.ta.psndoc.TBMPsndocVO;
import nc.vo.trn.transmng.AggStapply;
import nc.vo.trn.transmng.StapplyVO;
import nc.vo.uap.pf.PfProcessBatchRetObject;
import uap.distribution.util.StringUtil;

public class PsnDimissionRefDAO {

	private static BaseDAO baseDAO = new BaseDAO();

	/**
	 * 查询离职业务类型
	 * 
	 * @param userId
	 *            用户ID
	 * @param refType
	 *            单据类型 trnstype:离职业务类型参照 billtype：离职流程类型
	 * @return
	 * @throws BusinessException
	 */
	public List<Map<String, Object>> queryDimissionRef(String userId,
			String refType, String condition) throws BusinessException {
		String pk_psndoc = ((IUserPubService) NCLocator.getInstance().lookup(
				IUserPubService.class)).queryPsndocByUserid(userId);
		if (StringUtil.isBlank(pk_psndoc)) {
			throw new BusinessException("未找到该用户，请确认用户是否是员工");
		}
		String exesql = null;
		if ("trnsType".equals(refType)) { // 离职业务类型
			exesql = "select  trnstypename labelname,pk_trnstype nodekey from hr_trnstype  where  enablestate = 2 and trnsevent = 4 order by trnstypecode";
		} else if ("billType".equals(refType)) { // 流程类型
			exesql = "select pk_billtypecode, billtypename labelname, pk_billtypeid nodekey from bd_billtype where ( istransaction = 'Y' and pk_group = '0001A110000000000GBO' and nvl ( islock, 'N' ) = 'N') and ( parentbilltype = '6115' and pk_group = '0001A110000000000GBO' ) order by pk_billtypecode";
		} else if ("reason".equals(refType)) { // 离职原因
			// pk_defdoclist = '1001Z71000000000GPD1' 异动原因（离职原因）主键
			exesql = "select code,name labelname,pk_defdoc nodekey,pid parentkey from bd_defdoc  where enablestate = 2  and pk_defdoclist = '1001Z71000000000GPD1'  order by code";
		} else if ("orgRef".equals(refType)) { // 组织
			exesql = "select  code,name labelname,pk_adminorg nodekey,pk_fatherorg parentkey,displayorder from org_adminorg  where org_adminorg.pk_adminorg in (select pk_adminorg from org_admin_enable) and (enablestate = 2)  and ((pk_group = '0001A110000000000GBO'))  order by displayorder, code";
		} else if ("psnType".equals(refType)) { // 人员类别
			exesql = "select  code,name labelname,pk_psncl nodekey,parent_id parentkey from bd_psncl  where enablestate = 2 order by code";
		} else if ("dimDept".equals(refType)) { // 离职后部门
			exesql = "SELECT org_dept.code,org_dept.name labelname,org_dept.pk_dept nodekey,org_dept.pk_fatherorg parentkey,org_dept.pk_group,org_dept.pk_org,org_dept.principal,org_dept.displayorder,org_orgs.code AS org_code,org_orgs.name AS org_name FROM org_dept INNER JOIN org_orgs ON org_orgs.pk_org = org_dept.pk_org INNER JOIN org_adminorg ON org_dept.pk_org = org_adminorg.pk_adminorg AND org_adminorg.enablestate= 2 WHERE hrcanceled = 'N' AND depttype <> 1 AND org_dept.pk_org IN (SELECT pk_adminorg FROM org_admin_enable) AND org_dept.enablestate = 2 AND org_dept.hrcanceled = 'N'";
			if (condition != null) {
				exesql = exesql
						+ condition
						+ " ORDER BY org_code,org_dept.displayorder,org_dept.code";
			} 
		}else if ("dimPost".equals(refType)) { // 岗位
			exesql = "select om_post.postcode,om_post.postname labelname,om_post.pk_post nodekey,om_post.suporior parentkey,om_post.pk_group,om_post.pk_org,org_orgs.code AS org_code,org_orgs.name AS org_name from om_post INNER JOIN org_orgs ON org_orgs.pk_org = om_post.pk_org INNER JOIN org_adminorg ON om_post.pk_org = org_adminorg.pk_adminorg AND org_adminorg.enablestate= 2 where om_post.pk_org IN (SELECT pk_adminorg FROM org_admin_enable)AND om_post.enablestate = 2 ";
			if (condition != null) {
				exesql = exesql
						+ condition
						+ " ORDER BY org_code,om_post.postcode";
			} 
		}else if("dimPostSeries".equals(refType)){// 岗位序列
			exesql = "select om_postseries.postseriescode,om_postseries.postseriesname labelname,om_postseries.pk_postseries nodekey,om_postseries.father_pk parentkey,om_postseries.pk_group,om_postseries.pk_org from om_postseries where om_postseries.enablestate = 2 ";
		}else if("dimJobgrade".equals(refType)){//职级
			exesql = "select om_joblevel.code,om_joblevel.name labelname,om_joblevel.pk_joblevel nodekey,om_post.pk_post from om_joblevel inner join om_levelrelation on om_joblevel.pk_joblevel = om_levelrelation.pk_joblevel inner join om_post on om_levelrelation.pk_post = om_post.pk_post ";
			if (condition != null) {
				exesql = exesql
						+ condition
						+ " ORDER BY om_joblevel.code";
			} 
		}
		
		List<Map<String, Object>> data = (List<Map<String, Object>>) baseDAO
				.executeQuery(exesql, new MapListProcessor());
		return data;
	}

	/**
	 * 查询离职人员信息
	 * 
	 * @param userId
	 * @param pk_psnjob
	 * @return
	 * @throws BusinessException
	 */
	public List<Map<String, Object>> queryDimissionPsnInfo(String pk_psnjob,
			String userId) throws BusinessException {
		String pk_psndoc = ((IUserPubService) NCLocator.getInstance().lookup(
				IUserPubService.class)).queryPsndocByUserid(userId);
		if (StringUtil.isBlank(pk_psndoc)) {
			throw new BusinessException("未找到该用户，请确认用户是否是员工");
		}
		if (StringUtils.isEmpty(pk_psnjob)) {
			pk_psnjob = ((Object[]) baseDAO
					.executeQuery(
							"select pk_psnjob from hi_psnjob where hi_psnjob.lastflag = 'Y' AND hi_psnjob.ismainjob = 'Y' and endflag='N' and POSTSTAT='Y' and pk_psndoc='"
									+ pk_psndoc + "'", new ArrayProcessor()))[0]
					.toString();
		}
		StringBuffer exesql = new StringBuffer("SELECT ");
		exesql.append(" hi_psnjob.PK_PSNJOB,bd_psndoc.PK_PSNDOC AS pk_psndoc,bd_psndoc.name AS psnName,hi_psnjob.pk_org AS pk_adminorg,org_orgs.name AS orgname,");
		exesql.append(" hi_psnjob.pk_dept AS pk_dimdept,org_dept.name AS dimdeptname,hi_psnjob.pk_psncl  AS pk_psncl,bd_psncl.NAME AS psntypename ");
		exesql.append(" FROM hi_psnjob LEFT OUTER JOIN BD_PSNDOC ON BD_PSNDOC.pk_psndoc = hi_psnjob.pk_psndoc ");
		exesql.append(" LEFT OUTER JOIN org_orgs ON org_orgs.pk_org = hi_psnjob.pk_org LEFT OUTER JOIN org_dept ON org_dept.pk_dept = hi_psnjob.pk_dept ");
		exesql.append(" LEFT OUTER JOIN om_post ON om_post.pk_post = hi_psnjob.pk_post LEFT OUTER JOIN bd_psncl ON bd_psncl.PK_PSNCL = hi_psnjob.PK_PSNCL ");
		exesql.append(" WHERE hi_psnjob.lastflag = 'Y' AND hi_psnjob.ismainjob = 'Y' AND endflag='N' AND POSTSTAT='Y' AND hi_psnjob.PK_PSNJOB = '"
				+ pk_psnjob + "'");
		List<Map<String, Object>> data = (List<Map<String, Object>>) baseDAO
				.executeQuery(exesql.toString(), new MapListProcessor());
		PsnJobVO psn = (PsnJobVO) new SimpleDocServiceTemplate(
				"TrnBillFormEditor").queryByPk(PsnJobVO.class, pk_psnjob, true);
		String pk_oldpsninfoorg = HiSQLHelper.getEveryHrorg(psn.getPk_psnorg(),
				psn.getAssgid(), ManagescopeBusiregionEnum.psndoc);
		String pk_newpsninfoorg = psn.getPk_hrorg();
		String pk_oldcontractorg = HiSQLHelper.getEveryHrorg(
				psn.getPk_psnorg(), psn.getAssgid(),
				ManagescopeBusiregionEnum.psnpact);
		String pk_newcontractorg = psn.getPk_hrorg();
		for (Map<String, Object> map : data) {
			map.put("pk_oldpsninfoorg", pk_oldpsninfoorg);
			map.put("pk_newpsninfoorg", pk_newpsninfoorg);
			map.put("pk_oldcontractorg", pk_oldcontractorg);
			map.put("pk_newcontractorg", pk_newcontractorg);

			map.put("oldpsninfoorgname", queryOrgNameByPk(pk_oldpsninfoorg));
			map.put("newpsninfoorgname", queryOrgNameByPk(pk_newpsninfoorg));
			map.put("oldcontractorgname", queryOrgNameByPk(pk_oldcontractorg));
			map.put("newcontractorgname", queryOrgNameByPk(pk_newcontractorg));
		}
		return data;
	}

	/**
	 * 根据 pk_org查询组织名称
	 * 
	 * @param pk_org
	 * @return
	 * @throws BusinessException
	 */
	private String queryOrgNameByPk(String pk_org) throws BusinessException {
		return ((Object[]) baseDAO.executeQuery(
				"select name from org_orgs where pk_org='" + pk_org + "'",
				new ArrayProcessor()))[0].toString();

	}

	/**
	 * 查询离职申请单
	 * 
	 * @param pk_user
	 * @param pk_hi_stapply
	 * @return
	 * @throws BusinessException
	 */
	public Map<String, Object> queryDimissionBill(String pk_user,
			AggStapply aggVO) throws BusinessException {
		String pk_psndoc = ((IUserPubService) NCLocator.getInstance().lookup(
				IUserPubService.class)).queryPsndocByUserid(pk_user);
		if (StringUtil.isBlank(pk_psndoc)) {
			throw new BusinessException("未找到该用户，请确认用户是否是员工");
		}
		StapplyVO stapplyVO = getStapplyVO(aggVO);
		Map<String, Object> result = new HashMap<String, Object>();
		StringBuffer exesql = new StringBuffer();
		exesql.append("select hi_stapply.PK_HI_STAPPLY,hi_stapply.pk_trnstype,hr_trnstype.trnstypename,hi_stapply.transtypeid pk_billtypeid,bd_billtype.BILLTYPENAME");
		exesql.append(",hi_stapply.PK_PSNDOC,BD_PSNDOC.NAME psnname,hi_stapply.SREASON pk_defdoc,bd_defdoc.NAME reasonname,hi_stapply.EFFECTDATE effectivedate");
		exesql.append(",hi_stapply.newpk_org pk_adminorg,org_adminorg.NAME orgname,hi_stapply.newpk_dept pk_dimdept,org_dept.NAME dimdeptname");
		exesql.append(",hi_stapply.newpk_psncl pk_psncl,bd_psncl.NAME psntypename,hi_stapply.MEMO,hi_stapply.approve_state,bd_project.pk_project,bd_project.project_name  ");
		exesql.append("from hi_stapply left join hr_trnstype on hi_stapply.pk_trnstype =hr_trnstype.pk_trnstype ");
		exesql.append("LEFT JOIN bd_billtype ON hi_stapply.transtypeid = bd_billtype. pk_billtypeid ");
		exesql.append("left join BD_PSNDOC on HI_STAPPLY.PK_PSNDOC=BD_PSNDOC.PK_PSNDOC ");
		exesql.append("left join bd_defdoc on hi_stapply.SREASON = bd_defdoc.PK_DEFDOC ");
		exesql.append("left join org_adminorg on org_adminorg.pk_adminorg = hi_stapply.newpk_org ");
		exesql.append("left join org_dept on org_dept.PK_DEPT = hi_stapply.newpk_dept ");
		exesql.append("left join bd_psncl on bd_psncl.PK_PSNCL = hi_stapply.newpk_psncl ");
		exesql.append("left join bd_project on bd_project.pk_project = hi_stapply.NEWJOBGLBDEF7 ");//add by wt 2191101 case:离职单据增加项目
		exesql.append("WHERE  hi_stapply.PK_HI_STAPPLY = '"
				+ stapplyVO.getPk_hi_stapply() + "' ");
		List<Map<String, Object>> data = (List<Map<String, Object>>) baseDAO
				.executeQuery(exesql.toString(), new MapListProcessor());
		ArrayList<Map<String, Object>> workFlowNote = queryWorkFlowNote(
				stapplyVO.getTranstype(), stapplyVO.getBill_code(),
				stapplyVO.getPk_hi_stapply());
		for (Map<String, Object> map : data) {
			result.put("trnstypename", map.get("trnstypename"));
			result.put("billtypename", map.get("billtypename"));
			result.put("approve_state", map.get("approve_state"));
			result.put("project_name", map.get("project_name"));//add by wt 2191101 case:离职单据增加项目
			result.put("dimissionTab", data);
			if (workFlowNote != null && workFlowNote.size() > 0) {
				result.put("workflownote", workFlowNote);
			}
		}
		
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
		result.put("operatorPeople", "N");
		List<Map<String, Object>> cuseridList = (List<Map<String, Object>>)baseDAO.executeQuery(sqlBuffer.toString(),  new MapListProcessor());
		if(cuseridList!=null && cuseridList.size()>0){
			for(int i = 0; i < cuseridList.size(); i++){
				Map<?,?> tempMap =(Map<?, ?>) cuseridList.get(0);
				if(tempMap.get("cuserid").toString().equals(pk_user)){
					result.put("operatorPeople", "Y");
    				break;
				}
			}
		}
		return result;
	}

	/**
	 * 提交离职单据
	 * 
	 * @param aggVO
	 * @return
	 * @throws BusinessException
	 */
	public Map<String, Object> submitDimission(AggStapply aggVO)
			throws BusinessException {
		HashMap<String, String> eParam = getDirectApprove(aggVO);
		PfProcessBatchRetObject validateRetObj = ((IHrPf) NCLocator
				.getInstance().lookup(IHrPf.class))
				.submitValidation(
						"Commit",
						"Commit",
						null,
						SysInitQuery.getParaInt(
								getStapplyVO(aggVO).getPk_org(),
								(String) IHrPf.hashBillTypePara.get("6115"))
								.intValue(),
						new AggregatedValueObject[] { aggVO });

		if ((validateRetObj.getRetObj() == null)
				|| (validateRetObj.getRetObj().length == 0)) {
			String errStr = validateRetObj.getExceptionMsg();
			if (StringUtils.isNotBlank(errStr))
				throw new BusinessException(errStr);
		}
		LfwPfUtil.runAction("SAVE", "6115", aggVO, null, null, null, eParam,
				new String[] { getStapplyVO(aggVO).getBillmaker() }, null);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("flag", "2");
		result.put("approve_state", getStapplyVO(aggVO).getApprove_state());
		return result;
	}

	/**
	 * 收回离职单据
	 * 
	 * @param aggVO
	 * @return
	 * @throws BusinessException
	 */
	public Map<String, Object> rollbackDimission(AggStapply aggVO)
			throws BusinessException {
		HashMap<String, String> eParam = getDirectApprove(aggVO);
		PfProcessBatchRetObject validateRetObj = ((IHrPf) NCLocator
				.getInstance().lookup(IHrPf.class)).callbackValidate(
				"CallBack", "CallBack", null, true,
				new AggregatedValueObject[] { aggVO });
		if ((validateRetObj.getRetObj() == null)
				|| (validateRetObj.getRetObj().length == 0)) {
			String errStr = validateRetObj.getExceptionMsg();
			if (StringUtils.isNotBlank(errStr))
				throw new BusinessException(errStr);
		}
		LfwPfUtil.runAction("RECALL", "6115", aggVO, null, null, null, eParam,
				new String[] { getStapplyVO(aggVO).getBillmaker() }, null);
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("falg", 2);
		result.put("approve_state", getStapplyVO(aggVO).getApprove_state());
		return result;
	}

	/**
	 * 获取StapplyVO
	 * 
	 * @param aggVO
	 * @return
	 */
	private StapplyVO getStapplyVO(AggStapply aggVO) {
		return (StapplyVO) aggVO.getParentVO();
	}

	/**
	 * 获取是否直批参数
	 * 
	 * @param pk_org
	 * @param billtype
	 * @return
	 * @throws BusinessException
	 */
	public HashMap<String, String> getDirectApprove(AggStapply aggVO)
			throws BusinessException {
		HashMap<String, String> eParam = new HashMap<String, String>();
		Integer type = SysInitQuery.getParaInt(getStapplyVO(aggVO).getPk_org(),
				(String) IHrPf.hashBillTypePara.get("6115"));
		if ((type != null) && (type.intValue() == 0)) {
			eParam.put("nosendmessage", "nosendmessage");
		}
		return eParam;
	}

	// 此处代码需要抽成静态方法供其他类调用 danqiang3
	public ArrayList<Map<String, Object>> queryWorkFlowNote(String param1,
			String param2, String billversionpk) throws BusinessException {
		StringBuffer sqlbuffer = new StringBuffer();
		sqlbuffer
				.append(" select checknote,senddate,a.user_name sendernameml,b.user_name checknameml, case approveresult when 'Y' then '批准' when 'N' then '不批准' when 'R' then '驳回' else '' end approveresult,case approvestatus when 0 then '待审批' when 1 then '审批已完成' when 2 then '已挂起' when 3 then '已终止' else '无效(作废)' end approvestatus, ")
				.append(" c.DEALDATE from pub_workflownote c, sm_user a, sm_user b where c.senderman = a.cuserid and c.checkman = b.cuserid and billversionpk = '"
						+ billversionpk + "' and actiontype <> 'BIZ'");
		if (param1 == null || "".equals(param1)) {
			sqlbuffer
					.append(" and workflow_type in ( 2, 3, 6 ) order by c.senddate, c.dealdate asc, c.dealtimemillis asc");
		} else {
			sqlbuffer
					.append(" and pk_billtype = '"
							+ param1
							+ "' and workflow_type in ( 2, 3, 6 ) order by c.senddate, c.dealdate asc, c.dealtimemillis asc");
		}
		ArrayList st = (ArrayList) new BaseDAO().executeQuery(
				sqlbuffer.toString(), new MapListProcessor());
		ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if (st != null && st.size() > 1) {
			Map<String, Object> object = (Map<String, Object>) st.get(0);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("checknote", "提交");
			map.put("senddate", object.get("senddate").toString());
			map.put("sendernameml", object.get("sendernameml").toString());
			map.put("checknameml", object.get("sendernameml").toString());
			map.put("approveresult", "");
			map.put("dealdate", object.get("senddate").toString());
			list.add(map);
			for (int i = 0; i < st.size(); i++) {
				Map<String, Object> map2 = (Map<String, Object>) st.get(i);
				// list.add((Map<String, Object>) st.get(i));
				Map<String, Object> map11 = (Map<String, Object>) st.get(i);
				map11.put("checknote", map11.get("checknote") == null ? ""
						: map11.get("checknote").toString());
				map11.put("senddate", map11.get("dealdate") == null ? ""
						: map11.get("dealdate").toString());
				map11.put("sendernameml", map11.get("checknameml") == null ? ""
						: map11.get("checknameml").toString());
				map11.put("checknameml", map11.get("checknameml") == null ? ""
						: map11.get("checknameml").toString());
				map11.put(
						"approveresult",
						map11.get("approveresult") == null ? map11.get(
								"approvestatus").toString() : map11.get(
								"approveresult").toString());
				map11.put("dealdate", map11.get("dealdate") == null ? ""
						: map11.get("dealdate").toString());
				list.add(map11);
				String approveresult = map2.get("approveresult") == null ? ""
						: map2.get("approveresult").toString();
				if (approveresult.equals("驳回") && i < st.size() - 1) {
					Map<String, Object> map4 = (Map<String, Object>) st
							.get(i + 1);
					Map<String, Object> map3 = new HashMap<String, Object>();
					map3.put("checknote", "提交");
					map3.put("senddate", map2.get("senddate").toString());
					map3.put("sendernameml", object.get("sendernameml")
							.toString());
					map3.put("checknameml", object.get("sendernameml")
							.toString());
					map3.put("approveresult", "");
					map3.put("dealdate", map4.get("senddate").toString());
					list.add(map3);
				}
			}
		} else {
			list = st;
		}
		return list;
	}
}
