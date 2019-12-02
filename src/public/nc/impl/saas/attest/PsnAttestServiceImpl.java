package nc.impl.saas.attest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.common.RuntimeEnv;
import nc.hr.utils.PubEnv;
import nc.impl.saas.hi.PsndocXYDao;
import nc.impl.saas.pub.SaasCommonHelper;
import nc.itf.bd.defdoc.IDefdocQryService;
import nc.itf.hi.psnattest.IPsnAttestManageService;
import nc.itf.org.IOrgConst;
import nc.itf.saas.IPsnAttestService;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.vo.bd.defdoc.DefdocVO;
import nc.vo.hi.psnattest.PsnAttestVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class PsnAttestServiceImpl implements IPsnAttestService{

	@Override
	public JSONArray getAttestType(String pk_defdoclist) throws BusinessException {
		IDefdocQryService docService = NCLocator.getInstance().lookup(IDefdocQryService.class);
		JSONArray docArrJson = new JSONArray();
		DefdocVO[] docArr = docService.queryDefdocVOsByDoclistPk(pk_defdoclist, IOrgConst.GLOBEORG, "~");
		if(ArrayUtils.isEmpty(docArr))
			return docArrJson;
		for(int i=0 ; i<docArr.length ; i++){
			DefdocVO doc = docArr[i] ;
			JSONObject jo = new JSONObject() ;
			jo.put("id", doc.getPk_defdoc());
			jo.put("code", doc.getCode()) ;
			jo.put("name", doc.getName());
			docArrJson.add(jo) ;
		}
		return docArrJson;
	}

	@Override
	public JSONObject searchApplyer(String paramJson) throws BusinessException {
		JSONObject param = JSONObject.fromObject(paramJson);
		String applyRole = param.getString("applyRoleCode");
		String value = (String)param.get("value");
		int pageno = param.get("pageno") == null ? 0 : param.getInt("pageno");
		int pagesize = param.get("pagesize")  == null ? 10 : param.getInt("pagesize");
		
		String pk_hrorg = getMainOrg();
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT U .cuserid,U .user_name,org.pk_org pk_org ,org.name org_name, ");
		sb.append(" nvl(dept.name,' ') dept_name, ");
		sb.append(" nvl(post.postname,' ') post_name, ");
		sb.append(" psn.name psn_name,");
		sb.append(" CASE WHEN job.pk_hrorg='" + pk_hrorg + "' THEN 'Y' else 'N' END as defaultFlag ");
		sb.append(" FROM SM_USER U ");
		sb.append(" INNER JOIN SM_USER_ROLE ur ON U .cuserid = ur.cuserid ");
		sb.append(" INNER join bd_psndoc psn on psn.pk_psndoc=u.pk_base_doc ");
		sb.append(" INNER JOIN hi_psnjob job on job.pk_psndoc=psn.pk_psndoc and job.ismainjob='Y' and job.lastflag='Y'");
		sb.append(" LEFT JOIN SM_ROLE ROLE ON ur.pk_role = ROLE .pk_role ");
		sb.append(" LEFT JOIN ORG_ORGS org ON job.pk_org =org.pk_org ");
		sb.append(" LEFT join org_dept dept on job.pk_dept = dept.pk_dept ");
		sb.append(" LEFT join om_post post on job.pk_post = post.pk_post ");
		sb.append(" WHERE ROLE .role_code LIKE '"+applyRole+"%' ");
		if(StringUtils.isNotEmpty(value)){
			try {
				value = URLDecoder.decode(value, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			sb.append(" and ( ") ;
			sb.append(" psn.code like '%" + value.toString() + "%' ");
			sb.append(" or psn.name like '%" + value.toString() + "%' ");
			sb.append(" or psn.SHORTNAME like '%" + value.toString() + "%' ");
			sb.append(" or dept.name like '%" + value.toString() + "%' ");
			sb.append(" or dept.code like '%" + value.toString() + "%' ");
			sb.append(" or dept.SHORTNAME like '%" + value.toString() + "%' ");
			sb.append(" or org.name like '%" + value.toString() + "%' ");
			sb.append(" or org.code like '%" + value.toString() + "%' ");
			sb.append(" or org.SHORTNAME like '%" + value.toString() + "%' ");
			sb.append(" )") ;
		}
		sb.append(" order by defaultFlag desc, org.code,u.cuserid ") ;
		Map<String, Object> result = SaasCommonHelper.queryPageResult(sb.toString(),new MapListProcessor(),pageno,pagesize);
		
		return JSONObject.fromObject(result);
	}
	@Override
	public JSONArray getApplyer(String paramJson) throws BusinessException {
		
		JSONObject param = JSONObject.fromObject(paramJson);
		String applyRole = param.getString("applyRoleCode");
		
		String pk_hrorg = getMainOrg();
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT U .cuserid,U .user_name,org.pk_org pk_org ,org.name org_name, ");
		sb.append(" nvl(dept.name,'') dept_name, ");
		sb.append(" nvl(post.postname,'') post_name, ");
		sb.append(" psn.name psn_name,");
		sb.append(" CASE WHEN job.pk_hrorg='" + pk_hrorg + "' THEN 'Y' else 'N' END as defaultFlag ");
		sb.append(" FROM SM_USER U ");
		sb.append(" INNER JOIN SM_USER_ROLE ur ON U .cuserid = ur.cuserid ");
		sb.append(" INNER join bd_psndoc psn on psn.pk_psndoc=u.pk_base_doc ");
		sb.append(" INNER JOIN hi_psnjob job on job.pk_psndoc=psn.pk_psndoc and job.ismainjob='Y' and job.lastflag='Y'");
		sb.append(" LEFT JOIN SM_ROLE ROLE ON ur.pk_role = ROLE .pk_role ");
		sb.append(" LEFT JOIN ORG_ORGS org ON job.pk_org =org.pk_org ");
		sb.append(" LEFT join org_dept dept on job.pk_dept = dept.pk_dept ");
		sb.append(" LEFT join om_post post on job.pk_post = post.pk_post ");
		sb.append(" WHERE ROLE .role_code LIKE '"+applyRole+"%' ");
		sb.append(" and job.pk_org ='" +pk_hrorg+"' ");
		
		List<Map<String, Object>> userMapList = (List<Map<String, Object>>) new BaseDAO().executeQuery(sb.toString(), new MapListProcessor());
		JSONArray userJson = new JSONArray();
		if(CollectionUtils.isEmpty(userMapList))
			return userJson;
		for(Map<String, Object> map : userMapList){
			JSONObject jo = new JSONObject();
			for(String key : map.keySet()){
				jo.put(key, map.get(key));
			}
			userJson.add(jo);
		}
		return userJson;
	}

	@Override
	public String commit(String attestJson) throws BusinessException {
		String pk_psndoc = SaasCommonHelper.getPsnIdByUser(InvocationInfoProxy.getInstance().getUserId());
		JSONObject jo = JSONObject.fromObject(attestJson);
		PsnAttestVO vo = new PsnAttestVO();
		vo.setApply_user(PubEnv.getPk_user());//申请人
		vo.setPsn_list(jo.getString("applyUser"));//申请人
		vo.setExtend_col1(jo.getString("filePath"));//附件
		vo.setAttest_type(jo.getString("attestType"));//证明类型
//		vo.setExtend_col2(jo.getString("describe"));//描述
		vo.setMemo(jo.getString("describe"));
		vo.setIs_agree(UFBoolean.FALSE);//默认不同意
		vo.setBilltype("2");
		vo.setDr(0);
		vo.setApply_time(new UFDateTime());
		PsndocXYDao dao = new PsndocXYDao();
		Map psnMap = dao.queryMainJobInfo(pk_psndoc);
		vo.setPk_group(psnMap.get("pk_group").toString());
		vo.setPk_org(psnMap.get("pk_hrorg").toString());
		
		NCLocator.getInstance().lookup(IPsnAttestManageService.class).insert(vo);
		return null;
	}

	@Override
	public String getPreview(String id) throws BusinessException, UnsupportedEncodingException, IOException {
		IDefdocQryService service = NCLocator.getInstance().lookup(IDefdocQryService.class);
		DefdocVO [] docs = service.queryDefdocByPk(new String[]{id});
		
		JSONObject result= new JSONObject();
		if(docs == null || docs.length == 0){
			result.put("commitFlag", false);
			result.put("contents", "<div>没有证明类型档案<div/>");
			return result.toString() ;
		}
		String docId = docs[0].getPk_defdoc();
		if (!docs[0].getName().contains("其他")) {
			String homePath = RuntimeEnv.getInstance().getNCHome();
			String filePath = homePath + "/tempdoc/" + docId + ".html";
			File file = new File(filePath);
			if (!file.exists()) {
				result.put("commitFlag", false);
				result.put("contents", "<div>系统模版丢失，请自行上传<div/>");
				return result.toString();
			}
			InputStream input = null;
			ByteArrayOutputStream bos = null;
			try{
				input = new FileInputStream(file);
				
				bos = new ByteArrayOutputStream();
				int i = -1;
				while ((i = input.read()) != -1) {
					bos.write(i);
				}
				byte[] buff = bos.toByteArray();
				result.put("commitFlag", true);
				result.put("contents", new String(buff, "UTF-8"));
				return result.toString();
			} finally {
				IOUtils.closeQuietly(input);
				IOUtils.closeQuietly(bos);
			}
		} else {
			//其他证明类型返回为空
			result.put("commitFlag", false);
			result.put("contents", "<div>其他证明没有模版，请自行上传<div/>");
			return result.toString();
		}
	}	

	/**
	 * 当前登陆人主任职组织
	 * @return
	 * @throws DAOException
	 */
	private String getMainOrg() throws DAOException{
		String pk_psndoc = SaasCommonHelper.getPsnIdByUser(PubEnv.getPk_user());
		PsndocXYDao dao = new PsndocXYDao();
		Map psnInfo = dao.queryMainJobInfo(pk_psndoc);
		if(psnInfo != null){
			String hrorg = psnInfo.get("pk_hrorg").toString();
			return hrorg ;
		}
		return "" ;
	}

	@Override
	public JSONArray queryProject(String paramJson) throws BusinessException {
		// TODO 自动生成的方法存根
		return null;
	}

	@Override
	public JSONObject searchProject(String paramJson) throws BusinessException {
		JSONObject param = JSONObject.fromObject(paramJson);
		String applyRole = param.getString("applyRoleCode");
		String value = (String)param.get("value");
		int pageno = param.get("pageno") == null ? 0 : param.getInt("pageno");
		int pagesize = param.get("pagesize")  == null ? 10 : param.getInt("pagesize");
		
		String pk_hrorg = getMainOrg();
		StringBuffer sb = new StringBuffer();
		sb.append("select pk_project,project_code,project_name from bd_project ");
		if(StringUtils.isNotEmpty(value)){
			try {
				value = URLDecoder.decode(value, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			sb.append(" where (") ;
			sb.append(" project_code like '%" + value.toString() + "%' ");
			sb.append(" or project_name like '%" + value.toString() + "%' ");
			sb.append(" )") ;
		}
		sb.append(" order by project_code, project_name ") ;
		Map<String, Object> result = SaasCommonHelper.queryPageResult(sb.toString(),new MapListProcessor(),pageno,pagesize);
		
		return JSONObject.fromObject(result);
	}
}
