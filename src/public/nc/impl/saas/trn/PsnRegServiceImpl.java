package nc.impl.saas.trn;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.NCLocator;
import nc.hr.utils.MultiLangHelper;
import nc.hr.utils.ResHelper;
import nc.impl.saas.hi.TBMAwayDao;
import nc.itf.saas.pub.PageResult;
import nc.itf.saas.trn.IPsnRegService;
import nc.itf.trn.regmng.IRegmngManageService;
import nc.itf.trn.regmng.IRegmngQueryService;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.pubitf.rbac.IUserPubService;
import nc.vo.hi.psndoc.PsnJobVO;
import nc.vo.hi.psndoc.PsndocVO;
import nc.vo.hi.psndoc.TrialVO;
import nc.vo.om.post.PostStdVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.billtype.BilltypeVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.sm.UserVO;
import nc.vo.trn.regitem.TrnRegItemVO;
import nc.vo.trn.regmng.AggRegapplyVO;
import nc.vo.trn.regmng.RegapplyVO;

public class PsnRegServiceImpl implements IPsnRegService {

	@Override
	public String getNewReg(Map<String, Object> param) throws BusinessException {
//		String userId = param.get("userId").toString();
		String userid = param.get("userid").toString();//update by wt 20191024 case:原来转正人为当前登陆人，现改参照可选
		PageResult result = new PageResult();
//		HashMap<String,Object> map = (HashMap<String, Object>) queryRegItems(userId);
		HashMap<String,Object> map = (HashMap<String, Object>) queryRegItems(userid);//update by wt 20191024 case:原来转正人为当前登陆人，现改参照可选
		result.setStatusCode((Integer)map.get("status"));
		result.setMessage((String)map.get("message"));
		result.setData(map.get("data"));
		return result.toJson();
	}

	private Map<String,Object> hasTrail(PsnJobVO psnJobVO) throws BusinessException{
		Map<String, Object> map = new HashMap<String,Object>();
		Integer status = 200;
		String msg = "";
		if(psnJobVO == null || (psnJobVO.getTrial_flag()==null)
				||(!psnJobVO.getTrial_flag().booleanValue())
				||(psnJobVO.getTrial_type() == null)){
			msg = (status==200)?"没有有效的试用信息!":msg;
			status = 300;
		}else{
			TrialVO[] trialVOs = new PsnRegDao().queryTrial(psnJobVO.getPk_psnorg());
			if ((!ArrayUtils.isEmpty(trialVOs)) && 
					(trialVOs[0].getTrialresult() != null) && (3 == trialVOs[0].getTrialresult().intValue())) {
				msg = (status==200)?"已有转正未通过记录，不允许增加转正单据！":msg;
				status = 300;
			}
		}
		map.put("status", status);
		map.put("message", msg);
		return map;
	}
	
	private Map<String,Object> queryRegItems(String userId) throws BusinessException{
//		String pk_psndoc = NCLocator.getInstance().lookup(IUserPubService.class).queryPsndocByUserid(userId);
		String pk_psndoc = userId;//update by wt 20191024 case:原来转正人为当前登陆人，现改参照可选
		PsnJobVO psnJobVO = new PsnRegDao().queryPsnjob(pk_psndoc);
		Map<String, Object> map = (HashMap<String, Object>) hasTrail(psnJobVO);
		if((map.get("status")!= null) && (Integer)map.get("status")==300){
			return map;
		}
		TrnRegItemVO[] itemVOs = new PsnRegDao().queryRegItems(psnJobVO.getTrial_type(),psnJobVO.getPk_org(), psnJobVO.getPk_group());
		if (ArrayUtils.isEmpty(itemVOs)){
			map.put("status", "300");
			map.put("message", "未设置转正项目，不能填写转正申请单！");
			return map;
		}
		Map<String, String> nameMap = new PsnRegDao().getOrgDeptPsnclName(pk_psndoc);
		TrialVO[] trialVOs = new PsnRegDao().queryTrial(psnJobVO.getPk_psnorg());
//		Map<String, Object> resultdata = getItemMap(itemVOs, null, nameMap, null,psnJobVO);//update by wt
		Map<String, Object> resultdata = getItemMap(itemVOs, null, nameMap, null);
		if ((trialVOs != null) && (trialVOs.length > 0)){
			resultdata.put("begindate", trialVOs[0].getBegindate().toString());
			resultdata.put("overdate", trialVOs[0].getEnddate());
			resultdata.put("regulardate", trialVOs[0].getRegulardate());
		}else{
			resultdata.put("begindate", "");
			resultdata.put("overdate", "");
			resultdata.put("regulardate", "");
		}
		resultdata.put("trial_type", psnJobVO.getTrial_type());
		resultdata.put("orgdeptname", nameMap);
		map.put("data", resultdata);
		return map;
	}
	
	private Map<String, Object> getItemMap(TrnRegItemVO[] itemVOs, AggRegapplyVO aggVO, Map<String, String> nameMap, Map<String, String> refnameMap){
		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, Object> oldMap;
		Map<String, Object> newMap;
		List<TrnRegItemVO> list = new ArrayList<TrnRegItemVO>();
		RegapplyVO applyVO = aggVO == null ?null:(RegapplyVO) aggVO.getParentVO();
		List<Map<String, Object>> oldList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> newList = new ArrayList<Map<String, Object>>();
		//组织、部门、人员类别不可修改，必须显示，动态可选项中去掉必须显示
		String[] removeData = {"pk_org","pk_psncl","pk_dept","pk_post","pk_postseries","series","pk_job"
								,"pk_jobgrade","pk_jobrank","pk_job_type","jobmode","deposemode","poststat"
								,"occupation","worktype"};
		
		Map<String, String> valueMap = new HashMap<String, String>();
		for(String str: removeData){
			for(int i=0; i<itemVOs.length; i++){
				if(itemVOs[i]!= null && itemVOs[i].getItemkey().indexOf(str) == 3
						&& itemVOs[i].getItemkey().length() == str.length()+3){
					if(applyVO == null){
						valueMap.put(itemVOs[i].getItemkey()+"_value", nameMap.get(str));
						if(itemVOs[i].getFldtype()==4){
							valueMap.put(itemVOs[i].getItemkey()+"_displayname", nameMap.get(str));
						}else{
							valueMap.put(itemVOs[i].getItemkey()+"_displayname", nameMap.get(str+ "name"));
						}
					}else{
						if(itemVOs[i].getFldtype()==4){
							valueMap.put(itemVOs[i].getItemkey()+"_value", applyVO.getAttributeValue(itemVOs[i].getItemkey()) == null? "N":applyVO.getAttributeValue(itemVOs[i].getItemkey()).toString());
							valueMap.put(itemVOs[i].getItemkey()+"_displayname", applyVO.getAttributeValue(itemVOs[i].getItemkey()) == null? "N":applyVO.getAttributeValue(itemVOs[i].getItemkey()).toString());
						}else{
							valueMap.put(itemVOs[i].getItemkey()+"_value", (String)applyVO.getAttributeValue(itemVOs[i].getItemkey()));
							valueMap.put(itemVOs[i].getItemkey()+"_displayname", refnameMap.get(itemVOs[i].getItemkey()+"name")== null? "无":(String)refnameMap.get(itemVOs[i].getItemkey()+"name"));
						}
					}
					list.add(itemVOs[i]);
					itemVOs[i] = null;
				}
			}
		}
		if(list.size()<itemVOs.length){
			for(int i=0; i<itemVOs.length; i++){
				if(itemVOs[i]!= null && itemVOs[i].getItemkey().indexOf("memo")!=3){
					if(applyVO != null){
						valueMap.put(itemVOs[i].getItemkey()+"_value", (String)applyVO.getAttributeValue(itemVOs[i].getItemkey()));
						valueMap.put(itemVOs[i].getItemkey()+"_displayname", refnameMap.get(itemVOs[i].getItemkey()+"name") == null? "无":(String)refnameMap.get(itemVOs[i].getItemkey()+ "name"));
					}else{
//						valueMap.put(itemVOs[i].getItemkey()+"_value", "");
//						valueMap.put(itemVOs[i].getItemkey()+"_displayname", "");
						valueMap.put(itemVOs[i].getItemkey()+"_value", nameMap.get(itemVOs[i].getItemkey().substring(3)));
						valueMap.put(itemVOs[i].getItemkey()+"_displayname", nameMap.get(itemVOs[i].getItemkey().substring(3)+ "name"));
					}
					list.add(itemVOs[i]);
					itemVOs[i] = null;
				}
			}
		}
		for(TrnRegItemVO vo: list){
			String itemkey = vo.getItemkey();
			if(itemkey.startsWith("old")){
				oldMap = new HashMap<String, Object>();
				oldMap.put("itemKey", vo.getItemkey());
				oldMap.put("itemName", vo.getItemname());
				oldMap.put("datatype", vo.getFldtype());
				oldMap.put("isrequired", vo.getIsmustnotnull()==null?false:vo.getIsmustnotnull().booleanValue());
				Object value = valueMap.get(vo.getItemkey()+"_value");
				Object displayname = valueMap.get(vo.getItemkey()+"_displayname");
				if(vo.getFldtype() == 4){
					value= ("Y".equals(value))? true: false;
					displayname= ("Y".equals(displayname))? "是": "否";
				}
				oldMap.put("value", value);
				oldMap.put("displayname", displayname);
				oldList.add(oldMap);
			}else if (itemkey.startsWith("new")){
				newMap = new HashMap<String, Object>();
				newMap.put("itemKey", vo.getItemkey());
				newMap.put("itemName", vo.getItemname());
				newMap.put("datatype", vo.getFldtype());
				newMap.put("isrequired", vo.getIsmustnotnull()==null?false:vo.getIsmustnotnull().booleanValue());
				Object value = valueMap.get(vo.getItemkey()+"_value");
				Object displayname = valueMap.get(vo.getItemkey()+"_displayname");
				if(vo.getFldtype() == 4){
					value= ("Y".equals(value))? true: false;
					displayname= ("Y".equals(displayname))? "是": "否";
				}
				newMap.put("value", value);
				newMap.put("displayname", displayname);
				newList.add(newMap);
			}
		}
		data.put("olddata", oldList);
		data.put("newdata", newList);
		return data;
	}
	

	@Override
	public String queryPsnRegByPk(Map<String, Object> param)
			throws BusinessException {
		String pk_hi_regapply = param.get("pk_hi_regapply").toString();
		Map<String, Object> regApply = queryPsnregByPk(pk_hi_regapply);
		PageResult result = new PageResult();
		result.setData(regApply);
		result.pushDevInfo("param", param);
		return result.toJson();
	}

	@SuppressWarnings("unchecked")
	@Override
	public String savePsnReg(Map<String, Object> param)
			throws BusinessException {
		Map<String, Object> regMap = (Map<String, Object>) param
				.get("regMap");
		List<Map<String, Object>> newdata = (List<Map<String, Object>>) param
				.get("newdata");
		String userId = param.get("userId").toString();//当前登录人
		AggRegapplyVO aggVO = getRegFromMap(regMap, newdata, userId);
		AggRegapplyVO newReg = new PsnRegDao().savePsnReg(userId, false, aggVO);
		RegapplyVO parentVO = (RegapplyVO) newReg.getParentVO();
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("pk_hi_regapply", parentVO.getPk_hi_regapply());
		hashMap.put("bill_code", parentVO.getBill_code());
		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(hashMap);
		return result.toJson();
	}
	
	
	private AggRegapplyVO getRegFromMap(Map<String, Object> regMap
			, List<Map<String, Object>> newdata, String userid) throws BusinessException {
		if (MapUtils.isEmpty(regMap))
			return null;
		String pk_psndoc = NCLocator.getInstance().lookup(IUserPubService.class).queryPsndocByUserid(userid);
		if (StringUtils.isBlank(pk_psndoc))
			throw new BusinessException(ResHelper.getString("6017mobile","06017mytime000001"));
		PsnJobVO psnJobVO = new PsnRegDao().queryPsnjob(pk_psndoc);
		AggRegapplyVO aggVO = new AggRegapplyVO();
		String pk_hi_regapply = null;
		if (regMap.get("pk_hi_regapply") != null && !"".equals(regMap.get("pk_hi_regapply"))) {
			pk_hi_regapply = regMap.get("pk_hi_regapply").toString();
		}
		AggRegapplyVO oldAggVO = null;
		RegapplyVO vo = null;
		if (pk_hi_regapply != null) {
			oldAggVO = NCLocator.getInstance()
					.lookup(IRegmngQueryService.class).queryByPk(pk_hi_regapply);
			vo = (RegapplyVO) oldAggVO.getParentVO();
		//	vo.setTs(regMap.get("ts") == null ? new UFDateTime(): new UFDateTime((String) regMap.get("ts")));
		} else {
			vo = new RegapplyVO();
			UFLiteralDate newdate = new UFLiteralDate();
			vo.setApply_date(newdate);
			vo.setBillmaker(userid);
			vo.setIshrssbill(UFBoolean.TRUE);
			vo.setApprove_state(Integer.valueOf(-1));
			vo.setTs(regMap.get("ts") == null ? new UFDateTime()
					: new UFDateTime((String) regMap.get("ts")));
			vo.setCreationtime(regMap.get("creationtime") == null ? new UFDateTime()
					: new UFDateTime((String) regMap.get("creationtime")));
			vo.setRegulardate(regMap.get("regulardate")==null? new UFLiteralDate()
					: new UFLiteralDate((String)regMap.get("regulardate")));
			vo.setProbation_type((Integer)regMap.get("trial_type"));
			int trialresult = Integer.parseInt((String)regMap.get("regularresult"));
			vo.setTrialresult(trialresult);
			if(trialresult == 2){
				vo.setTrialdelaydate(regMap.get("yanqidate")==null? new UFLiteralDate()
				: new UFLiteralDate((String)regMap.get("yanqidate")));
			}
			vo.setEnd_date(regMap.get("overdate")==null? new UFLiteralDate()
			: new UFLiteralDate((String)regMap.get("overdate")));
			vo.setIfsynwork(new UFBoolean((Boolean)regMap.get("synchronized")));
			vo.setIsneedfile(new UFBoolean(false));
			vo.setAssgid(psnJobVO.getAssgid());
			vo.setBegin_date(regMap.get("begindate")==null? psnJobVO.getBegindate()
			: new UFLiteralDate((String)regMap.get("begindate")));
		}
		//新增 209/11/25
		vo.setAttributeValue("newjobglbdef6", (String)regMap.get("regularselfrate"));   //个人综合评分
		vo.setAttributeValue("newjobglbdef7", (String)regMap.get("regulardepartmentrate"));  //部门综合评分
		vo.setAttributeValue("newjobglbdef8", (String)regMap.get("duty"));    //工作职责
		vo.setAttributeValue("newjobglbdef9", (String)regMap.get("selfsummarize"));   //个人总结
		vo.setAttributeValue("newjobglbdef10", (String)regMap.get("departmentsummarize"));   //部门考核评语
		
		vo.setMemo((String)regMap.get("memo"));
		aggVO.setParentVO(vo);
		vo.setPk_org(psnJobVO.getPk_org());
		vo.setPk_group(psnJobVO.getPk_group());
		vo.setBillmaker(userid);
		vo.setPk_psnorg(psnJobVO.getPk_psnorg());
		vo.setPk_psndoc(pk_psndoc);
		vo.setPk_psnjob(psnJobVO.getPk_psnjob());
		vo.setTranstypeid(regMap.get("transtypeid") == null ? "" : regMap.get("transtypeid").toString());
		if(regMap.get("transtypeid") != null){
			String transtypesql = "select pk_billtypecode from bd_billtype where pk_billtypeid = '" + regMap.get("transtypeid").toString() + "'";
			vo.setTranstype((String)(new BaseDAO().executeQuery(transtypesql, new ColumnProcessor())));
		}
		vo.setPk_billtype("6111");
		vo.setCreator(userid);
		//根据工作记录为转正前字段赋值
		vo.setOldpk_org(psnJobVO.getPk_org());
		vo.setOldpk_psncl(psnJobVO.getPk_psncl());
		vo.setOldpk_dept(psnJobVO.getPk_dept());
		vo.setOldpk_post(psnJobVO.getPk_post());
		vo.setOldpk_postseries(psnJobVO.getPk_postseries());
		vo.setOldpk_job(psnJobVO.getPk_job());
		vo.setOldseries(psnJobVO.getSeries());
		vo.setOldpk_jobgrade(psnJobVO.getPk_jobgrade());
		vo.setOldpk_jobrank(psnJobVO.getPk_jobrank());
		vo.setOldpk_job_type(psnJobVO.getPk_job_type());
		vo.setOldjobmode(psnJobVO.getJobmode());
		vo.setOlddeposemode(psnJobVO.getDeposemode());
		vo.setOldpoststat(psnJobVO.getPoststat());
		vo.setOldoccupation(psnJobVO.getOccupation());
		vo.setOldworktype(psnJobVO.getWorktype());
		
		for(Map<String, Object> newmap: newdata){
			String itemKey = (String)newmap.get("itemKey");
			switch(itemKey){
			case "newpk_org":
				vo.setNewpk_org((String)newmap.get("value"));
				break;
			case "newpk_psncl":
				vo.setNewpk_psncl((String)newmap.get("value"));
				break;
			case "newpk_dept":
				vo.setNewpk_dept((String)newmap.get("value"));
				break;
			case "newpk_post":
				vo.setNewpk_post((String)newmap.get("value"));
				break;
			case "newpk_postseries":
				vo.setNewpk_postseries((String)newmap.get("value"));
				break;
			case "newpk_job":
				vo.setNewpk_job((String)newmap.get("value"));
				break;
			case "newseries":
				vo.setNewseries((String)newmap.get("value"));
				break;
			case "newpk_jobgrade":
				vo.setNewpk_jobgrade((String)newmap.get("value"));
				break;
			case "newpk_jobrank":
				vo.setNewpk_jobrank((String)newmap.get("value"));
				break;
			case "newpk_job_type":
				vo.setNewpk_job_type((String)newmap.get("value"));
				break;
			case "newjobmode":
				vo.setNewjobmode((String)newmap.get("value"));
				break;
			case "newdeposemode":
				vo.setNewdeposemode((String)newmap.get("value"));
				break;
			case "newpoststat":
				if(StringUtils.isEmpty((String) newmap.get("value"))){
					break;
				}
				vo.setNewpoststat(new UFBoolean((Boolean)newmap.get("value")));
				break;
			case "newoccupation":
				vo.setNewoccupation((String)newmap.get("value"));
				break;
			case "newworktype":
				vo.setNewworktype((String)newmap.get("value"));
				break;
			}
		}
		
		//判断岗位信息和岗位序列是否匹配，已岗位信息为主
		String pk_post = vo.getNewpk_post();
		if(pk_post != null){
			PostStdVO postvo= (PostStdVO) new BaseDAO().retrieveByPK(PostStdVO.class, pk_post);
			vo.setNewpk_postseries(postvo.getPk_postseries());
		}
		return aggVO;
	}
	
	private AggRegapplyVO getRegFromMap(Map<String, Object> regMap, List<Map<String, Object>> newdata, String userid, String psndocPk) throws BusinessException {
		if (MapUtils.isEmpty(regMap))
			return null;
		String pk_psndoc = NCLocator.getInstance().lookup(IUserPubService.class).queryPsndocByUserid(userid);
		if (StringUtils.isBlank(pk_psndoc))
			throw new BusinessException(ResHelper.getString("6017mobile","06017mytime000001"));
//		PsnJobVO psnJobVO = new PsnRegDao().queryPsnjob(pk_psndoc); 
		PsnJobVO psnJobVO = new PsnRegDao().queryPsnjob(psndocPk);//update by wt 20191024 case:参照转正人
		AggRegapplyVO aggVO = new AggRegapplyVO();
		String pk_hi_regapply = null;
		if (regMap.get("pk_hi_regapply") != null && !"".equals(regMap.get("pk_hi_regapply"))) {
			pk_hi_regapply = regMap.get("pk_hi_regapply").toString();
		}
		AggRegapplyVO oldAggVO = null;
		RegapplyVO vo = null;
		if (pk_hi_regapply != null) {
			oldAggVO = NCLocator.getInstance()
					.lookup(IRegmngQueryService.class).queryByPk(pk_hi_regapply);
			vo = (RegapplyVO) oldAggVO.getParentVO();
		//	vo.setTs(regMap.get("ts") == null ? new UFDateTime(): new UFDateTime((String) regMap.get("ts")));
		} else {
			vo = new RegapplyVO(); // update by wt case:表头数据不修改
			UFLiteralDate newdate = new UFLiteralDate();
			vo.setApply_date(newdate);
			vo.setBillmaker(userid);
			vo.setIshrssbill(UFBoolean.TRUE);
			vo.setApprove_state(Integer.valueOf(-1));
			vo.setTs(regMap.get("ts") == null ? new UFDateTime()
					: new UFDateTime((String) regMap.get("ts")));
			vo.setCreationtime(regMap.get("creationtime") == null ? new UFDateTime()
					: new UFDateTime((String) regMap.get("creationtime")));
//			vo.setRegulardate(regMap.get("regulardate")==null? new UFLiteralDate()
//					: new UFLiteralDate((String)regMap.get("regulardate")));
//			vo.setProbation_type((Integer)regMap.get("trial_type"));
//			int trialresult = Integer.parseInt((String)regMap.get("regularresult"));
//			vo.setTrialresult(trialresult);
//			if(trialresult == 2){
//				vo.setTrialdelaydate(regMap.get("yanqidate")==null? new UFLiteralDate()
//				: new UFLiteralDate((String)regMap.get("yanqidate")));
//			}
//			vo.setEnd_date(regMap.get("overdate")==null? new UFLiteralDate()
//			: new UFLiteralDate((String)regMap.get("overdate")));
			vo.setIfsynwork(new UFBoolean((Boolean)regMap.get("synchronized")));
			vo.setIsneedfile(new UFBoolean(false));
			vo.setAssgid(psnJobVO.getAssgid());
			vo.setBegin_date(regMap.get("begindate")==null? psnJobVO.getBegindate()
			: new UFLiteralDate((String)regMap.get("begindate")));
		}
//		vo.setTs(regMap.get("ts") == null ? new UFDateTime()
//				: new UFDateTime((String) regMap.get("ts")));
		vo.setRegulardate(regMap.get("regulardate")==null? new UFLiteralDate()
				: new UFLiteralDate((String)regMap.get("regulardate")));
		vo.setProbation_type((Integer)regMap.get("trial_type"));
		int trialresult = Integer.parseInt((String)regMap.get("regularresult"));
		vo.setTrialresult(trialresult);
		if(trialresult == 2){
			vo.setTrialdelaydate(regMap.get("yanqidate")==null? new UFLiteralDate()
			: new UFLiteralDate((String)regMap.get("yanqidate")));
		}
		vo.setEnd_date(regMap.get("overdate")==null? new UFLiteralDate()
				: new UFLiteralDate((String)regMap.get("overdate")));
		vo.setBegin_date(regMap.get("begindate")==null? psnJobVO.getBegindate()
				: new UFLiteralDate((String)regMap.get("begindate")));
		vo.setMemo((String)regMap.get("memo"));
		aggVO.setParentVO(vo);
//		vo.setPk_org(psnJobVO.getPk_org());
		vo.setPk_org(psnJobVO.getPk_hrorg());////update by wt 20191105 case：nc中根据人力组织查询单据
		vo.setPk_group(psnJobVO.getPk_group());
		vo.setBillmaker(userid);
		vo.setPk_psnorg(psnJobVO.getPk_psnorg());
//		vo.setPk_psndoc(pk_psndoc);
		vo.setPk_psndoc(psndocPk);////update by wt 20191024 case:参照转正人
		vo.setPk_psnjob(psnJobVO.getPk_psnjob());
		vo.setTranstypeid(regMap.get("transtypeid") == null ? "" : regMap.get("transtypeid").toString());
		if(regMap.get("transtypeid") != null){
			String transtypesql = "select pk_billtypecode from bd_billtype where pk_billtypeid = '" + regMap.get("transtypeid").toString() + "'";
			vo.setTranstype((String)(new BaseDAO().executeQuery(transtypesql, new ColumnProcessor())));
		}
		vo.setPk_billtype("6111");
		vo.setCreator(userid);
		//根据工作记录为转正前字段赋值
		vo.setOldpk_org(psnJobVO.getPk_org());
		vo.setOldpk_psncl(psnJobVO.getPk_psncl());
		vo.setOldpk_dept(psnJobVO.getPk_dept());
		vo.setOldpk_post(psnJobVO.getPk_post());
		vo.setOldpk_postseries(psnJobVO.getPk_postseries());
		vo.setOldpk_job(psnJobVO.getPk_job());
		vo.setOldseries(psnJobVO.getSeries());
		vo.setOldpk_jobgrade(psnJobVO.getPk_jobgrade());
		vo.setOldpk_jobrank(psnJobVO.getPk_jobrank());
		vo.setOldpk_job_type(psnJobVO.getPk_job_type());
		vo.setOldjobmode(psnJobVO.getJobmode());
		vo.setOlddeposemode(psnJobVO.getDeposemode());
		vo.setOldpoststat(psnJobVO.getPoststat());
		vo.setOldoccupation(psnJobVO.getOccupation());
		vo.setOldworktype(psnJobVO.getWorktype());
		
		for(Map<String, Object> newmap: newdata){
			String itemKey = (String)newmap.get("itemKey");
			switch(itemKey){
			case "newpk_org":
				vo.setNewpk_org((String)newmap.get("value"));
				break;
			case "newpk_psncl":
				vo.setNewpk_psncl((String)newmap.get("value"));
				break;
			case "newpk_dept":
				vo.setNewpk_dept((String)newmap.get("value"));
				break;
			case "newpk_post":
				vo.setNewpk_post((String)newmap.get("value"));
				break;
			case "newpk_postseries":
				vo.setNewpk_postseries((String)newmap.get("value"));
				break;
			case "newpk_job":
				vo.setNewpk_job((String)newmap.get("value"));
				break;
			case "newseries":
				vo.setNewseries((String)newmap.get("value"));
				break;
			case "newpk_jobgrade":
				vo.setNewpk_jobgrade((String)newmap.get("value"));
				break;
			case "newpk_jobrank":
				vo.setNewpk_jobrank((String)newmap.get("value"));
				break;
			case "newpk_job_type":
				vo.setNewpk_job_type((String)newmap.get("value"));
				break;
			case "newjobmode":
				vo.setNewjobmode((String)newmap.get("value"));
				break;
			case "newdeposemode":
				vo.setNewdeposemode((String)newmap.get("value"));
				break;
			case "newpoststat":
				vo.setNewpoststat(new UFBoolean(true));
				break;
			case "newoccupation":
				vo.setNewoccupation((String)newmap.get("value"));
				break;
			case "newworktype":
				vo.setNewworktype((String)newmap.get("value"));
				break;
			case "newjobglbdef1": // add by wt 20191024 case:保存新增字段
				vo.setAttributeValue("newjobglbdef1", ((String)newmap.get("value")));
				break;
			case "newjobglbdef2": // add by wt 20191024 case:保存新增字段
				vo.setAttributeValue("newjobglbdef2", (newmap.get("value")));
				break;
			case "newjobglbdef3": // add by wt 20191024 case:保存新增字段
				vo.setAttributeValue("newjobglbdef3", (newmap.get("value")));
				break;
			case "newjobglbdef4": /// add by wt 20191024 case:保存新增字段
				vo.setAttributeValue("newjobglbdef4", (newmap.get("value")));
				break;
			case "newjobglbdef5": // add by wt 20191024 case:保存新增字段
				vo.setAttributeValue("newjobglbdef5", (newmap.get("value")));
				break;
			case "newjobglbdef6": // add by wt 20191024 case:保存新增字段
				vo.setAttributeValue("newjobglbdef6", (newmap.get("value")));
				break;
			case "newjobglbdef7": // add by wt 20191024 case:保存新增字段
				vo.setAttributeValue("newjobglbdef7", ((String)newmap.get("value")));
				break;
			case "newjobglbdef8": // add by wt 20191024 case:保存新增字段
				vo.setAttributeValue("newjobglbdef8", (newmap.get("value")));
				break;
			case "newjobglbdef9": // add by wt 20191024 case:保存新增字段
				vo.setAttributeValue("newjobglbdef9", (newmap.get("value")));
				break;
			case "newjobglbdef10": // add by wt 20191024 case:保存新增字段
				vo.setAttributeValue("newjobglbdef10", (newmap.get("value")));
				break;
			case "newjobglbdef11": // add by wt 20191024 case:保存新增字段
				vo.setAttributeValue("newjobglbdef11", (newmap.get("value")));
				break;
			}
		}
		
		//判断岗位信息和岗位序列是否匹配，已岗位信息为主
		String pk_post = vo.getNewpk_post();
		if(pk_post != null){
			PostStdVO postvo= (PostStdVO) new BaseDAO().retrieveByPK(PostStdVO.class, pk_post);
			vo.setNewpk_postseries(postvo.getPk_postseries());
		}
		return aggVO;
	}

	@Override
	public String deletePsnReg(Map<String, Object> param)
			throws BusinessException {
		String pk_hi_regapply = param.get("pk_hi_regapply").toString();
		Map<String, Object> result = new HashMap<String, Object>();
		AggRegapplyVO aggVO = (AggRegapplyVO) NCLocator
				.getInstance().lookup(IRegmngQueryService.class)
				.queryByPk(pk_hi_regapply);
		deleteBeforcheckState(((RegapplyVO)aggVO.getParentVO()).getApprove_state());
		NCLocator.getInstance().lookup(
				IRegmngManageService.class).deleteBill(aggVO);
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

	@Override
	public String submitPsnReg(Map<String, Object> param)
			throws BusinessException {
		String userId = param.get("userId").toString();
		String pk_hi_regapply = param.get("billKey").toString();
		//但强 收回和提交操作判断  rollback 收回 commit 提交 
		String opration = param.get("oprationtype").toString();
		AggRegapplyVO aggVO = NCLocator
				.getInstance().lookup(IRegmngQueryService.class).queryByPk(pk_hi_regapply);
		if(opration.equals("rollBack")){
			//收回操作
			new PsnRegDao().rollBackPsnReg(userId,
					aggVO);
		}else{
			//提交操作
			new PsnRegDao().submitPsnReg(userId,
					aggVO);
		}
		AggRegapplyVO aggVO2 = NCLocator.getInstance().lookup(IRegmngQueryService.class).queryByPk(pk_hi_regapply);
		RegapplyVO parentVO = (RegapplyVO) aggVO2.getParentVO();
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("pk_hi_regapply", parentVO.getPk_hi_regapply());
		hashMap.put("pk_h", parentVO.getPk_hi_regapply());
		hashMap.put("bill_code", parentVO.getBill_code());
		hashMap.put("approve_state", parentVO.getApprove_state());
		PageResult result = new PageResult();
		result.pushDevInfo("param", param);
		result.setData(hashMap);
		return result.toJson();
	}
	
	private Map<String, Object> queryPsnregByPk(String pk_hi_regapply)
			throws BusinessException {
		Map<String, Object> result = new HashMap<String, Object>();

		AggRegapplyVO aggVO = ((IRegmngQueryService) NCLocator
				.getInstance().lookup(IRegmngQueryService.class)).queryByPk(pk_hi_regapply);
		if (aggVO == null){
			return result;
		}
		RegapplyVO headVO = (RegapplyVO) aggVO.getParentVO();
		result.put("billcode", headVO.getBill_code());
		result.put("transtypeid", headVO.getTranstypeid());
		result.put("transtype", headVO.getTranstype());
		result.put("memo", headVO.getMemo()== null ? "": headVO.getMemo());//update by wt case:memo的length
		result.put("billmaker", headVO.getBillmaker());
//		BaseDAO baseDAO = new BaseDAO();
//		if(StringUtils.isNotBlank(headVO.getBillmaker())){
//			UserVO user = (UserVO) baseDAO.retrieveByPK(UserVO.class, headVO.getBillmaker());
//			result.put("user_name", user.getUser_name());
//		}
//		result.put("user_name ", headVO.getBillmaker());
		BaseDAO baseDAO = new BaseDAO(); //update by wt 20191024 case:获取转正人的信息
		if(StringUtils.isNotBlank(headVO.getPk_psndoc())){
			PsndocVO psndocVO = (PsndocVO) baseDAO.retrieveByPK(PsndocVO.class, headVO.getPk_psndoc());
			result.put("user_name", psndocVO.getName());
		}
		result.put("user_name ", headVO.getPk_psndoc());// update by wt 
		
		//update by djf
		result.put("regularselfrate", headVO.getAttributeValue("newjobglbdef6"));  //个人综合评分
		result.put("regulardepartmentrate", headVO.getAttributeValue("newjobglbdef7"));  //部门综合评分
		result.put("duty", headVO.getAttributeValue("newjobglbdef8"));  //工作职责
		result.put("selfsummarize", headVO.getAttributeValue("newjobglbdef9"));  //个人总结
		result.put("departmentsummarize", headVO.getAttributeValue("newjobglbdef10"));  //部门考核评语
		 
		
		result.put("ts", headVO.getTs().toString());
		result.put("creationtime", headVO.getCreationtime().toString());
		result.put("approve_state", headVO.getApprove_state().toString());
		if (StringUtils.isNotBlank(headVO.getTranstypeid())) {
			BilltypeVO billType = (BilltypeVO) baseDAO.retrieveByPK(
					BilltypeVO.class, headVO.getTranstypeid());
			result.put("transtypename",
					MultiLangHelper.getName(billType, "billtypename"));
		}
		result.put("requestid",headVO.getAttributeValue("requestid"));
		result.put("probation_type", headVO.getProbation_type()==1?"入职试用":"转岗试用");
		result.put("trial_type", headVO.getProbation_type());
		result.put("regulardate", headVO.getRegulardate() == null? "":headVO.getRegulardate().toString());
		result.put("overdate", headVO.getEnd_date() == null? "":headVO.getEnd_date().toString());
		result.put("begindate", headVO.getBegin_date() == null? "":headVO.getBegin_date().toString());
		result.put("yanqidate", headVO.getTrialdelaydate() == null? "":headVO.getTrialdelaydate().toString());
		result.put("regularresult", headVO.getTrialresult());
		result.put("regularresultname", headVO.getTrialresult() == 1? "转正":headVO.getTrialresult() == 2?"延长试用期":"未通过试用");
		result.put("synchronized", headVO.getIfsynwork().booleanValue());
		
		ArrayList<Map<String,Object>> workFlowNote = new TBMAwayDao().queryWorkFlowNote(headVO.getTranstype(),headVO.getBill_code(),headVO.getPk_hi_regapply());
		if(workFlowNote!=null && workFlowNote.size()>0){
			result.put("workflownote", workFlowNote);
		}
		TrnRegItemVO[] itemVOs = new PsnRegDao().queryRegItems(headVO.getProbation_type(),headVO.getPk_org(), headVO.getPk_group());
		Map<String, String> refnameMap = new PsnRegDao().getRefName(headVO.getPk_hi_regapply());
		Map<String, Object> resultdata = getItemMap(itemVOs, aggVO, null, refnameMap);
		result.put("data", resultdata);
		return result;
	}

}
