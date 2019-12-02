package nc.impl.saas.hi;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.hr.utils.MultiLangHelper;
import nc.itf.hr.pf.IHrPf;
import nc.itf.ta.IAwayApplyApproveManageMaintain;
import nc.itf.ta.IAwayApplyQueryMaintain;
import nc.itf.ta.IAwayRegisterQueryMaintain;
import nc.itf.ta.ITimeItemQueryService;
import nc.itf.ta.ITimeRuleQueryService;
import nc.itf.ta.algorithm.ITimeScopeWithBillInfo;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.jdbc.framework.processor.MapProcessor;
import nc.pubitf.para.SysInitQuery;
import nc.vo.pmpub.project.ProjectHeadVO;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.billtype.BilltypeVO;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.ta.PublicLangRes;
import nc.vo.ta.away.AggAwayVO;
import nc.vo.ta.away.AwayRegVO;
import nc.vo.ta.away.AwaybVO;
import nc.vo.ta.away.AwayhVO;
import nc.vo.ta.overtime.OvertimeRegVO;
import nc.vo.ta.overtime.OvertimebVO;
import nc.vo.ta.timeitem.AwayTypeCopyVO;
import nc.vo.ta.timerule.TimeRuleVO;
import nc.vo.uap.pf.PfProcessBatchRetObject;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class TBMAwayDao {
	/**
	 * 出差参照（本地出差还是外地出差）
	 * 
	 * @param pk_org
	 * @return
	 * @throws DAOException
	 */
	public List queryAwayRef(String pk_org) throws DAOException {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT ");
		sb.append(" 	tbm_timeitem.timeitemcode, ");
		sb.append(" 	timeitemname, ");
		sb.append(" 	tbm_timeitem.pk_timeitem, ");
		sb.append(" 	tbm_timeitem.itemtype, ");
		sb.append(" 	tbm_timeitemcopy.pk_timeitemcopy, ");
		sb.append(" 	tbm_timeitemcopy.pk_org, ");
		sb.append(" 	tbm_timeitemcopy.timeitemunit, ");
		sb.append(" 	tbm_timeitemcopy.leavesetperiod ");
		sb.append(" FROM ");
		sb.append(" 	tbm_timeitem ");
		sb.append(" INNER JOIN tbm_timeitemcopy ON ");
		sb.append(" 	tbm_timeitem.pk_timeitem = tbm_timeitemcopy.pk_timeitem ");
		sb.append(" 	AND tbm_timeitemcopy.pk_org = '" + pk_org + "' ");
		sb.append(" WHERE ");
		sb.append(" 	11 = 11 ");
		sb.append(" 	AND( ");
		sb.append(" 		tbm_timeitem.itemtype = 2 ");
		sb.append(" 		AND tbm_timeitemcopy.enablestate = 2 ");
		sb.append(" 	) ");
		sb.append(" ORDER BY ");
		sb.append(" 	tbm_timeitem.timeitemcode ");
		return (List) new BaseDAO().executeQuery(sb.toString(),
				new MapListProcessor());
	}

	/**
	 * 保存出差申请（校验信息怎么展示）
	 * 
	 * @param awayhMap
	 * @param bList
	 * @param userid
	 * @param needCheck
	 * @param aggvo
	 * @return
	 * @throws BusinessException
	 */
	public AggAwayVO saveAway(Map<String, Object> awayhMap,
			List<Map<String, Object>> bList, String userid, String needCheck,
			AggAwayVO aggvo) throws BusinessException {
		awayhMap.put("blist", bList);
		String warningMessage = "";
		IAwayApplyApproveManageMaintain awayM = (IAwayApplyApproveManageMaintain) NCLocator
				.getInstance().lookup(IAwayApplyApproveManageMaintain.class);
		if (StringUtils.isBlank(aggvo.getAwayhVO().getPk_awayh())) {
			aggvo = awayM.insertDataDirect(aggvo);
			Logger.error("出差单保存新单据");
		} else {
			aggvo = (AggAwayVO) awayM.updateData(aggvo);
		}
		return aggvo;
	}

	/**
	 * 提交出差申请
	 * @param userid
	 * @param aggvo
	 * @return
	 * @throws BusinessException
	 */
	public Map<String, Object> submitAway(AggAwayVO aggvo) throws BusinessException {
		HashMap<String, String> eParam = new HashMap<String, String>();
		if (isDirectApprove(aggvo.getAwayhVO().getPk_org(), "6403")) {
			eParam.put("nosendmessage", "nosendmessage");
		}
		PfProcessBatchRetObject validateRetObj = ((IHrPf) NCLocator
				.getInstance().lookup(IHrPf.class))
				.submitValidation("Commit","Commit",null,SysInitQuery.getParaInt(aggvo.getAwayhVO().getPk_org(),
								(String) IHrPf.hashBillTypePara.get("6403"))
								.intValue(),
						new AggregatedValueObject[] { aggvo });

		if ((validateRetObj.getRetObj() == null)
				|| (validateRetObj.getRetObj().length == 0)) {
			String errStr = validateRetObj.getExceptionMsg();
			if (StringUtils.isNotBlank(errStr))
				throw new BusinessException(errStr);
		}
		LfwPfUtil.runAction("SAVE", "6403", aggvo, null, null, null, eParam,
				new String[] { aggvo.getAwayhVO().getBillmaker() }, null);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("flag", "2");
		result.put("approve_state", aggvo.getAwayhVO().getApprove_state());
		return result;
	}
	
	/**
	 * 提交出差申请
	 * @param userid
	 * @param aggvo
	 * @return
	 * @throws BusinessException
	 */
	public Map<String, Object> rollbackAway(AggAwayVO aggvo) throws BusinessException {
		HashMap<String, String> eParam = new HashMap<String, String>();
		if (isDirectApprove(aggvo.getAwayhVO().getPk_org(), "6403")) {
			eParam.put("nosendmessage", "nosendmessage");
		}
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
		LfwPfUtil.runAction("RECALL", "6403", aggvo, null, null, null, eParam,
				new String[] { aggvo.getAwayhVO().getBillmaker() }, null);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("flag", "2");
		result.put("approve_state", aggvo.getAwayhVO().getApprove_state());
		return result;
	}
	

	/**
	 * 查询出差审批单
	 * @param pk_awayh
	 * @return
	 * @throws BusinessException
	 */
	public Map<String, Object> queryAwayByPK(String pk_awayh)
			throws BusinessException {
		Map<String, Object> result = new HashMap<String, Object>();
		String whereSql = "pk_awayh ='" + pk_awayh + "'";
		AggAwayVO[] aggVOs = ((IAwayApplyQueryMaintain) NCLocator.getInstance()
				.lookup(IAwayApplyQueryMaintain.class)).queryByCondition(null,
				whereSql);
		if (ArrayUtils.isEmpty(aggVOs)){//针对从考勤信息“员工日历”登记单数据跳转，需要查询对应登记单数据  by tianxx5
			AwayRegVO regvo = ((IAwayRegisterQueryMaintain) NCLocator.getInstance()
					.lookup(IAwayRegisterQueryMaintain.class)).queryByPk(pk_awayh);
			AwayTypeCopyVO[] awayCopyTypes = ((ITimeItemQueryService) NCLocator
					.getInstance().lookup(ITimeItemQueryService.class))
					.queryAwayCopyTypesByOrg(regvo.getPk_org(),
							"pk_timeitemcopy = '" + regvo.getPk_awaytypecopy()
									+ "' ");
			String typename = "";
			AwayTypeCopyVO typeVO = awayCopyTypes[0];
			DecimalFormat dcmFmt = getDecimalFormat(regvo.getPk_org());
			result.put("pk_awayh", regvo.getPk_awayreg());
			result.put("pk_awayreg", regvo.getPk_awayreg());
			result.put("billcode", regvo.getBill_code());
			result.put("transtypeid", null);
			result.put("transtype", null);
			result.put("pk_timeitemcopy", regvo.getPk_awaytypecopy());
			result.put("awayremark", regvo.getAwayremark());
			result.put("ts", regvo.getTs().toString());
			result.put("creationtime", regvo.getCreationtime().toString());
			result.put("approve_state", "10");
			String unit = 0 == typeVO.getTimeItemUnit() ? PublicLangRes.DAY()
					: PublicLangRes.HOUR();
			result.put("sumhour", new UFDouble(dcmFmt.format(regvo.getAwayhour())).toString());
			result.put("unit", unit);
			result.put("requestid",regvo.getAttributeValue("requestid"));
			if (!ArrayUtils.isEmpty(awayCopyTypes)) {
				typename = typeVO.getMultilangName();
			}
			result.put("awaytypename", typename);
			List<Map<String, Object>> blist = new ArrayList<Map<String, Object>>();
			result.put("awaybs", blist);
			Map<String, Object> binfo = new HashMap<String, Object>();
			blist.add(binfo);
			binfo.put("begintime", regvo.getAwaybegintime().toString());
			binfo.put("endtime", regvo.getAwayendtime().toString());
			if(regvo.getAwayhour()!=null){
				binfo.put("awayhour",
						new UFDouble(dcmFmt.format(regvo.getAwayhour())).toString());
			} else {
				binfo.put("awayhour",new UFDouble(dcmFmt.format(0)).toString());
			}
			binfo.put("pk_awayb", "virtualawayPK");
			binfo.put("awayremark", regvo.getAwayremark());
			binfo.put("awayaddress", regvo.getAwayaddress());
			binfo.put("workprocess", regvo.getWorkprocess());
			binfo.put("unit", unit);
			binfo.put("ts", regvo.getTs().toString());
			return result;
		}
		AggAwayVO aggVO = aggVOs[0];
		AwayhVO headVO = aggVO.getHeadVO();
		AwaybVO[] bodyVOs = aggVO.getAwaybVOs();
		AwayTypeCopyVO[] awayCopyTypes = ((ITimeItemQueryService) NCLocator
				.getInstance().lookup(ITimeItemQueryService.class))
				.queryAwayCopyTypesByOrg(headVO.getPk_org(),
						"pk_timeitemcopy = '" + headVO.getPk_awaytypecopy()
								+ "' ");
		String typename = "";
		AwayTypeCopyVO typeVO = awayCopyTypes[0];
		DecimalFormat dcmFmt = getDecimalFormat(headVO.getPk_org());
		result.put("pk_awayh", headVO.getPk_awayh());
		result.put("billcode", headVO.getBill_code());
		result.put("transtypeid", headVO.getTranstypeid());
		result.put("transtype", headVO.getTranstype());
		result.put("pk_timeitemcopy", headVO.getPk_awaytypecopy());
		result.put("awayremark", headVO.getAwayremark());
		result.put("ts", headVO.getTs().toString());
		result.put("creationtime", headVO.getCreationtime().toString());
		result.put("approve_state", headVO.getApprove_state().toString());
		String unit = 0 == typeVO.getTimeItemUnit() ? PublicLangRes.DAY()
				: PublicLangRes.HOUR();
		result.put("sumhour", new UFDouble(dcmFmt.format(headVO.getSumhour())).toString());
		result.put("unit", unit);
		result.put("requestid",headVO.getAttributeValue("requestid"));
		if (StringUtils.isNotBlank(headVO.getTranstypeid())) {
			BilltypeVO billType = (BilltypeVO) new BaseDAO().retrieveByPK(
					BilltypeVO.class, headVO.getTranstypeid());
			result.put("transtypename",
					MultiLangHelper.getName(billType, "billtypename"));
		}
		// add by wt 20190822 begin
//		if (StringUtils.isNotBlank(headVO.getPk_project())) {
//			ProjectHeadVO projectVO = (ProjectHeadVO) new BaseDAO().retrieveByPK(
//					ProjectHeadVO.class, headVO.getPk_project());
//			result.put("project_name",MultiLangHelper.getName(projectVO, "project_name"));
//		}
		// add by wt 20190822 end


		if (!ArrayUtils.isEmpty(awayCopyTypes)) {
			typename = typeVO.getMultilangName();
		}
		result.put("awaytypename", typename);

		List<Map<String, Object>> blist = new ArrayList<Map<String, Object>>();
		result.put("awaybs", blist);
		if (ArrayUtils.isEmpty(bodyVOs)) {
			return result;
		}

		Arrays.sort(bodyVOs,new Comparator<AwaybVO>() {
			@Override
			public int compare(AwaybVO paramT1, AwaybVO paramT2) {
				UFDateTime awaybegintime1 = paramT1.getAwaybegintime();
				UFDateTime awayendtime2 = paramT2.getAwayendtime();
				return awaybegintime1.compareTo(awayendtime2);
			}
		});
		for (AwaybVO bvo : bodyVOs) {
			Map<String, Object> binfo = new HashMap<String, Object>();
			blist.add(binfo);
			binfo.put("begintime", bvo.getAwaybegintime().toString());
			binfo.put("endtime", bvo.getAwayendtime().toString());
			if(bvo.getAwayhour()!=null){
				binfo.put("awayhour",
						new UFDouble(dcmFmt.format(bvo.getAwayhour())).toString());
			} else {
				binfo.put("awayhour",new UFDouble(dcmFmt.format(0)).toString());
			}
			binfo.put("pk_awayb", bvo.getPk_awayb());
			binfo.put("awayremark", bvo.getAwayremark());
			binfo.put("awayaddress", bvo.getAwayaddress());
			binfo.put("workprocess", bvo.getWorkprocess());
			binfo.put("unit", unit);
			binfo.put("ts", bvo.getTs().toString());
		}
		ArrayList<Map<String,Object>> workFlowNote = queryWorkFlowNote(headVO.getTranstype(),headVO.getBill_code(),headVO.getPk_awayh());
		if(workFlowNote!=null && workFlowNote.size()>0){
			result.put("workflownote", workFlowNote);
		}
		return result;
	}
	//此处代码需要抽成静态方法供其他类调用 danqiang3
	public ArrayList<Map<String, Object>> queryWorkFlowNote(String param1,String param2, String billversionpk) throws BusinessException {
		StringBuffer sqlbuffer = new StringBuffer();
		sqlbuffer.append(" select checknote,senddate,a.user_name sendernameml,b.user_name checknameml, case approveresult when 'Y' then '批准' when 'N' then '不批准' when 'R' then '驳回' else '' end approveresult,case approvestatus when 0 then '待审批' when 1 then '审批已完成' when 2 then '已挂起' when 3 then '已终止' else '无效(作废)' end approvestatus, ")
				.append(" c.DEALDATE from pub_workflownote c, sm_user a, sm_user b where c.senderman = a.cuserid and c.checkman = b.cuserid and billversionpk = '"+billversionpk+"' and actiontype <> 'BIZ'");
		if(param1==null||"".equals(param1)){
			sqlbuffer.append(" and workflow_type in ( 2, 3, 6 ) order by c.senddate, c.dealdate asc, c.dealtimemillis asc");
		}else{
			sqlbuffer.append(" and pk_billtype = '"+param1+"' and workflow_type in ( 2, 3, 6 ) order by c.senddate, c.dealdate asc, c.dealtimemillis asc");
		}
		ArrayList st = (ArrayList) new BaseDAO().executeQuery(sqlbuffer.toString(), new MapListProcessor());
		ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if(st!= null && st.size()>1){
			Map<String, Object> object = (Map<String, Object>) st.get(0);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("checknote", "提交");
			map.put("senddate", object.get("senddate").toString());
			map.put("sendernameml", object.get("sendernameml").toString());
			map.put("checknameml", object.get("sendernameml").toString());
			map.put("approveresult", "");
			map.put("dealdate", object.get("senddate").toString());
			list.add(map);
			for(int i=0;i<st.size();i++){
				Map<String, Object> map2 = (Map<String, Object>) st.get(i);
//				list.add((Map<String, Object>) st.get(i));
				Map<String, Object> map11 = (Map<String, Object>) st.get(i);
				map11.put("checknote", map11.get("checknote")==null?"":map11.get("checknote").toString());
				map11.put("senddate", map11.get("dealdate")==null?"":map11.get("dealdate").toString());
				map11.put("sendernameml", map11.get("checknameml")==null?"":map11.get("checknameml").toString());
				map11.put("checknameml", map11.get("checknameml")==null?"":map11.get("checknameml").toString());
				map11.put("approveresult", map11.get("approveresult")==null?map11.get("approvestatus").toString():map11.get("approveresult").toString());
				map11.put("dealdate", map11.get("dealdate")==null?"":map11.get("dealdate").toString());
				list.add(map11);
				String approveresult = map2.get("approveresult")== null?"":map2.get("approveresult").toString();
				if(approveresult.equals("驳回")&&i<st.size()-1){
					Map<String, Object> map4 = (Map<String, Object>) st.get(i+1);
					Map<String, Object> map3 = new HashMap<String, Object>();
					map3.put("checknote", "提交");
					map3.put("senddate", map2.get("senddate").toString());
					map3.put("sendernameml", object.get("sendernameml").toString());
					map3.put("checknameml", object.get("sendernameml").toString());
					map3.put("approveresult", "");
					map3.put("dealdate", map4.get("senddate").toString());
					list.add(map3);
				}
			}
		}else{
			list = st;
		}
		return list;
	}
	/**
	 * 根据出差申请单的编码查询打印信息
	 * @param pk_awayh
	 * @return
	 * @throws BusinessException
	 */
	public Map<String, Object> awayPrintTemplate(String pk_awayh) throws BusinessException {
		Map<String, Object> map = new HashMap<>();
		StringBuilder builder = new StringBuilder();
		builder.append("select a.bill_code as billcode, b.name as orgname, d.name as deptname, e.timeitemname as type, a.approve_state as status, ");
		builder.append("f.user_name as applyer, a.apply_date as applydate, a.approve_time as approvedate, g.postname as postname ");
		builder.append("from tbm_awayh a ");
		builder.append("inner join org_hrorg b on a.pk_org = b.pk_hrorg ");
		builder.append("inner join hi_psnjob c on c.pk_psnjob = a.pk_psnjob ");
		builder.append("inner join org_dept d on d.pk_dept = c.pk_dept ");
		builder.append("inner join tbm_timeitem e on e. pk_timeitem = a.pk_awaytype ");
		builder.append("inner join sm_user f on f.cuserid = a.billmaker ");
		builder.append("inner join om_post g on g.pk_post = c.pk_post ");
		builder.append("where a.pk_awayh = '" + pk_awayh + "' ");
		HashMap<String, Object> dataMap = (HashMap<String, Object>)new BaseDAO().executeQuery(builder.toString(), new MapProcessor());
		
		StringBuilder sb = new StringBuilder();
		sb.append("select a.awaybegintime as awaybegintime, a.awayaddress as awayaddress, a.awayendtime as awayendtime, a.awayhour as awayhour, a.workprocess as awayremark ");
		sb.append("from tbm_awayb a ");
		sb.append("where a.pk_awayh = '" + pk_awayh + "'");
		List list = (List) new BaseDAO().executeQuery(sb.toString(),
				new MapListProcessor());
		
		StringBuilder sba = new StringBuilder();
		sba.append("select b.user_name as sender, a.senddate as senddate, c.user_name as approver, a.dealdate as approdate, a.checknote as approveidea ");
		sba.append("from pub_workflownote a ");
		sba.append("inner join sm_user b on b.cuserid = a.senderman ");
		sba.append("inner join sm_user c on c.cuserid = a.checkman ");
		sba.append("where  billid = '" + pk_awayh + "' and actiontype <> 'BIZ' ");
		sba.append("order by a.senddate asc ");
		List notelist = (List) new BaseDAO().executeQuery(sba.toString(),
				new MapListProcessor());
		map.put("data", dataMap);
		map.put("tableData", list);
		map.put("approveData", notelist);
		return map;
	}

	/**
	 * 查询出差申请单
	 * @param pk_awayh
	 * @return
	 * @throws BusinessException
	 */
	public AggAwayVO queryAwayApplyByPK(String pk_awayh)
			throws BusinessException {
		String whereSql = "pk_awayh ='" + pk_awayh + "'";
		AggAwayVO[] aggVOs = ((IAwayApplyQueryMaintain) NCLocator.getInstance()
				.lookup(IAwayApplyQueryMaintain.class)).queryByCondition(null,
				whereSql);
		if (ArrayUtils.isEmpty(aggVOs))
			return null;
		return aggVOs[0];
	}

	private String getNameByPsndoc(String pk_psndoc) throws BusinessException{
		String sql = " select name from bd_psndoc where pk_psndoc = ? ";
		SQLParameter para = new SQLParameter();
		para.addParam(pk_psndoc);
		String name = (String) new BaseDAO().executeQuery(sql, para,new ColumnProcessor());
		return name;
	}

	public boolean isDirectApprove(String pk_org, String billtype)
			throws BusinessException {
		Integer type = SysInitQuery.getParaInt(pk_org,
				(String) IHrPf.hashBillTypePara.get(billtype));
		return (type != null) && (type.intValue() == 0);
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
	/**
	 * 返回时长冲突的list
	 * @param result
	 * @return
	 */
	public ArrayList<Map<String, String>> getBrokenList(Map<String, Map<Integer, ITimeScopeWithBillInfo[]>> result){
		ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Iterator<Map.Entry<String, Map<Integer, ITimeScopeWithBillInfo[]>>> entries = result.entrySet().iterator();
		while(entries.hasNext()){
			Map.Entry<String, Map<Integer, ITimeScopeWithBillInfo[]>> entry = entries.next();
			Map<Integer, ITimeScopeWithBillInfo[]> value = entry.getValue();
			Iterator<?> itera = value.entrySet().iterator();
			while(itera.hasNext()){
				Map.Entry<Integer, ITimeScopeWithBillInfo[]> vlau = (Entry<Integer, ITimeScopeWithBillInfo[]>) itera.next();
				Integer key = vlau.getKey();
				if(key == 2){
					ITimeScopeWithBillInfo[] value2 = vlau.getValue();
					for(ITimeScopeWithBillInfo time : value2){
						Map<String,String> map = new HashMap<String,String>();
						String name = time.getClass().getName();
						if(name.equals("nc.vo.ta.overtime.OvertimeRegVO")){
							//来自加班登记
							nc.vo.ta.overtime.OvertimeRegVO vo= (OvertimeRegVO) time;
							map.put("billinit", "加班登记");
							map.put("begindatetime", vo.getOvertimebegintime().toString());
							map.put("enddatetime", vo.getOvertimeendtime().toString());
						}else{
							//加班申请
							nc.vo.ta.overtime.OvertimebVO vo= (OvertimebVO)time;
							map.put("billinit", "加班申请");
							map.put("begindatetime", vo.getOvertimebegintime().toString());
							map.put("enddatetime", vo.getOvertimeendtime().toString());
						}
						list.add(map);
					}
				}else if(key == 1){
					ITimeScopeWithBillInfo[] value2 = vlau.getValue();
					for(ITimeScopeWithBillInfo time : value2){
						Map<String,String> map = new HashMap<String,String>();
						String name = time.getClass().getName();
						if(name.equals("nc.vo.ta.leave.LeaveRegVO")){
							//来自休假登记
							nc.vo.ta.leave.LeaveRegVO vo= (nc.vo.ta.leave.LeaveRegVO)time;
							map.put("billinit", "休假登记");
							map.put("begindatetime", vo.getLeavebegintime().toString());
							map.put("enddatetime", vo.getLeaveendtime().toString());
						}else{
							//休假申请
							nc.vo.ta.leave.LeavebVO vo= (nc.vo.ta.leave.LeavebVO)time;
							map.put("billinit", "休假申请");
							map.put("begindatetime", vo.getLeavebegintime().toString());
							map.put("enddatetime", vo.getLeaveendtime().toString());
						}
						list.add(map);
					}
				}else if(key == 4){
					ITimeScopeWithBillInfo[] value2 = vlau.getValue();
					for(ITimeScopeWithBillInfo time : value2){
						Map<String,String> map = new HashMap<String,String>();
						String name = time.getClass().getName();
						if(name.equals("nc.vo.ta.away.AwayRegVO")){
							//来自出差登记
							nc.vo.ta.away.AwayRegVO bvo = (AwayRegVO)time;
							map.put("billinit", "出差申请");
							map.put("begindatetime", bvo.getAwaybegintime().toString());
							map.put("enddatetime", bvo.getAwayendtime().toString());
						}else{
							//出差申请
							nc.vo.ta.away.AwaybVO bvo = (AwaybVO)time;
							map.put("billinit", "出差申请");
							map.put("begindatetime", bvo.getAwaybegintime().toString());
							map.put("enddatetime", bvo.getAwayendtime().toString());
						}
						list.add(map);
					}
				}
			}
		}
		return list;
	}
	
}
