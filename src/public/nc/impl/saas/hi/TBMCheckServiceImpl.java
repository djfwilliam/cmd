package nc.impl.saas.hi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.hr.utils.InSQLCreator;
import nc.itf.saas.ITBMCheckService;
import nc.itf.saas.pub.PageResult;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.generator.SequenceGenerator;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.jdbc.framework.processor.BeanProcessor;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.jdbc.framework.processor.MapProcessor;
import nc.vo.hi.psndoc.PsnJobVO;
import nc.vo.pmpub.project.ProjectHeadVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.sass.fieldcheck.FIeldCheckVO;
import nc.vo.ta.daystat.DayStatVO;
import nc.vo.ta.importdata.ImportDataVO;
import nc.vo.ta.timeregion.RegionSettingVO;

public class TBMCheckServiceImpl implements ITBMCheckService {
	
	BaseDAO baseDAO = new BaseDAO();
	
	/**
	 * 移动签到数据初始化：当天考勤数据、考勤范围判定
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String queryCheckData(Map<String, Object> param)
			throws BusinessException {
		Map<String, Object> map = new HashMap<>();
		boolean state = false;
		String userId = param.get("userId").toString();
		String xLine = param.get("xLine") == null ? null : param.get("xLine").toString();
		String yLine = param.get("yLine") == null ? null : param.get("yLine").toString();
		String type = param.get("type").toString();
		BaseDAO dao = new BaseDAO();
		SQLParameter para1 = new SQLParameter();
		SQLParameter para2 = new SQLParameter();
		SQLParameter para3 = new SQLParameter();
		StringBuffer sql1 = new StringBuffer();
		StringBuffer sql2 = new StringBuffer();
		StringBuffer sql3 = new StringBuffer();
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat df1 = new SimpleDateFormat("yyyy-HH-dd hh:mm:ss");
		String date = df.format(new Date());
		String beginTime = date + " 00:00:00";
		String endTime = date + " 23:59:59";
		
		// 考勤范围判定
		para2.addParam(userId);
		if (xLine == null || yLine == null) {
			map.put("checkstate", 0);
		} else {
			sql2.append("select a.* from tbm_regionset a ");
			sql2.append("inner join tbm_regionorg b on a.pk_region = b.pk_region ");
			sql2.append("inner join hi_psnjob c on b.pk_org = c.pk_org and c.pk_psndoc = ? ");
			sql2.append("and c.ismainjob = 'Y' and c.lastflag = 'Y' and c.endflag = 'N' ");
			List<RegionSettingVO> regionVOList = (List<RegionSettingVO>) dao
					.executeQuery(sql2.toString(), para2,
							new BeanListProcessor(RegionSettingVO.class));
			for(int i=0;i<regionVOList.size();i++){
				
				state = isInArea(xLine, yLine, regionVOList.get(i)
						.getLatitude().toDouble(), regionVOList.get(i)
						.getLongitude().toDouble(), regionVOList.get(i).getRadius()
						.toDouble());
				if(state){
					break;
				}
			}
			map.put("checkstate", state ? 1 : 0);
		}

		// 通过参数type判断是查询还是插入
		if (state) {
			if ("insert".equalsIgnoreCase(type)) {
				para3.addParam(userId);
				sql3.append("select pk_org,pk_group from hi_psnjob ");
				sql3.append("where pk_psndoc = ? and ismainjob = 'Y' and lastflag = 'Y' and endflag = 'N' ");
				PsnJobVO psnjobVO = (PsnJobVO) dao.executeQuery(
						sql3.toString(), para3, new BeanProcessor(
								PsnJobVO.class));
				ImportDataVO insVO = new ImportDataVO();
				Date now = new Date();
				insVO.setPk_org(psnjobVO.getPk_org());
				insVO.setPk_group(psnjobVO.getPk_group());
				insVO.setCreator(userId);
				insVO.setCreationtime(new UFDateTime(now));
				insVO.setCalendardate(new UFLiteralDate(now));
				insVO.setCalendartime(new UFDateTime(now));
				insVO.setDatatype(1);
				insVO.setPk_psndoc(userId);
				dao.insertVO(insVO);
				map.put("state", 200);
			}
		} else {
			map.put("state", 500);
		}

		// 获取当天考勤数据
		para1.addParam(userId);
		para1.addParam(beginTime);
		para1.addParam(endTime);
		sql1.append("select * from tbm_importdata ");
		sql1.append("where pk_psndoc = ? and calendartime >= ? and calendartime <= ? order by calendartime desc ");
		List<ImportDataVO> importVOList = (List<ImportDataVO>) dao
				.executeQuery(sql1.toString(), para1, new BeanListProcessor(
						ImportDataVO.class));
		
		map.put("checkdata", importVOList);
		PageResult result = new PageResult();
		result.setData(map);
		return result.toJson();
	}

	/**
	 * 获取当前登陆人部门负责人
	 */
	@Override
	public String queryDeptManager(Map<String, Object> param)
			throws BusinessException {
		Map<String, Object> map = new HashMap<>();
		String userId = param.get("userId").toString();
		StringBuffer sql = new StringBuffer();
		SQLParameter param1 = new SQLParameter();
		param1.addParam(userId);
		sql.append("select a.pk_psndoc,a.name from bd_psndoc a ");
		sql.append("inner join org_orgmanager b on a.pk_psndoc = b.pk_psndoc ");
		sql.append("inner join hi_psnjob c on b.pk_dept = c.pk_dept ");
		sql.append("where c.pk_psndoc = ? and c.endflag = 'N' and c.lastflag = 'Y' and b.principalflag = 'Y' ");
		List<Map<String, Object>> voList = (List<Map<String, Object>>) getBaseDAO()
				.executeQuery(sql.toString(), param1, new MapListProcessor());
		map.put("deptmanagers", voList);
		PageResult result = new PageResult();
		result.setData(map);
		return result.toJson();
	}

	/**
	 * 外勤申请上传
	 */
	@Override
	public String insertFiledCheck(Map<String, Object> param)
			throws BusinessException {
		String billmaker = param.get("billmaker") == null ? null : param.get("billmaker").toString();
		String approver = param.get("approver") == null ? null : param.get("approver").toString();
		String addr = param.get("addr") == null ? null : param.get("addr").toString();
		String longitude = param.get("longitude") == null ? null : param.get("longitude").toString();
		String latitude = param.get("latitude") == null ? null : param.get("latitude").toString();
		int fieldtype = param.get("fieldtype") == null ? null : Integer.parseInt(param.get("fieldtype").toString());
		String content = param.get("content") == null ? null : param.get("content").toString();
		String pk_project = param.get("pk_project") == null ? null : param.get("pk_project").toString();
		String project_name = param.get("project_name") == null ? null : param.get("project_name").toString();

		FIeldCheckVO vo = new FIeldCheckVO();
		SequenceGenerator sg = new SequenceGenerator();
		vo.setPk_field(sg.generate());
		vo.setBillmaker(billmaker);
		vo.setApprover(approver);
		vo.setApprstate(0);
		vo.setFieldtime(new UFDateTime(new Date()));
		vo.setAddr(addr);
		vo.setLongitude(longitude);
		vo.setLatitude(latitude);
		vo.setFieldtype(fieldtype);
		vo.setContent(content);
		vo.setPk_project(pk_project);
		vo.setProject_name(project_name);

		getBaseDAO().insertVO(vo);
		PageResult result = new PageResult();
		return result.toJson();
	}
	
	/**
	 * 我的外勤查询
	 */
	@Override
	public String queryMyField(Map<String, Object> param)
			throws BusinessException {
		PageResult result = new PageResult();
		String userid = param.get("userId").toString();
		int type = Integer.parseInt(param.get("type").toString()); // type: 0:外勤申请单 1：外勤审批单
		int state  =Integer.parseInt(param.get("state").toString()); // state： 0：待办 1：已办
		StringBuffer sql = new StringBuffer();
		StringBuffer cond = new StringBuffer();
		sql.append("select a.*,b.name from tbm_field a ");
		sql.append("inner join bd_psndoc b on a.billmaker = b.pk_psndoc where ");
		if (type == 0) {
			if (state == 0) {
				cond.append("billmaker = ? and apprstate = 0 ");
			} else {
				cond.append("billmaker = ? and apprstate in(1, 2) ");
			}
		} else if (type == 1) {
			if (state == 0) {
				cond.append("approver = ? and apprstate = 0 ");
			} else {
				cond.append("approver = ? and apprstate in(1, 2) ");
			}
		}
		sql.append(cond).append("order by fieldtime desc ");
		SQLParameter pam = new SQLParameter();
		pam.addParam(userid);
		List<Map<String, Object>> voList = (List<Map<String, Object>>) getBaseDAO()
				.executeQuery(sql.toString(), pam, new MapListProcessor());
		result.setData(voList);
		return result.toJson();
	}
	
	/**
	 * 根据单据主键查询外勤单据详情
	 */
	@Override
	public String queryMyFieldByPK(Map<String, Object> param)
			throws BusinessException {
		PageResult result = new PageResult();
		String pk = param.get("pk").toString();
		StringBuffer sql = new StringBuffer();
		sql.append("select a.*,b.name from tbm_field a inner join bd_psndoc b on a.approver = b.pk_psndoc where a.pk_field = ? ");
		SQLParameter pam = new SQLParameter();
		pam.addParam(pk);
		Map<String, Object> vo = (Map<String, Object>) getBaseDAO()
				.executeQuery(sql.toString(), pam, new MapProcessor());
		result.setData(vo);
		return result.toJson();
	}
	
	/**
	 * 根据单据主键删除外勤单据详情
	 */
	@Override
	public String deleteMyFieldByPK(Map<String, Object> param)
			throws BusinessException {
		PageResult result = new PageResult();
		String pk = param.get("pk").toString();
		StringBuffer sql = new StringBuffer();
		SQLParameter pam = new SQLParameter();
		pam.addParam(pk);
		sql.append("delete from tbm_field where pk_field = ? ");
		getBaseDAO().executeUpdate(sql.toString(), pam);
		return result.toJson();
	}

	/**
	 * 根据单据主键更新外勤单据详情
	 */
	@Override
	public String updateMyFieldByPK(Map<String, Object> param)
			throws BusinessException {
		PageResult result = new PageResult();
		String pk = param.get("pk").toString();
		String state = param.get("state").toString();
		// 0：审批通过 1：审批不通过
		StringBuffer sql1 = new StringBuffer();
		SQLParameter param1 = new SQLParameter();
		param1.addParam(pk);
		if ("0".equalsIgnoreCase(state)) {
			try {
				sql1.append("update tbm_field set apprstate = 1 where pk_field = ? ");
				StringBuffer sql2 = new StringBuffer();
				sql2.append("select b.pk_org, b.pk_group, a.billmaker, a.fieldtime, a.ADDR, a.project_name, a.CONTENT from tbm_field a ");
				sql2.append("inner join hi_psnjob b on a.billmaker = b.pk_psndoc ");
				sql2.append("where a.pk_field = ? and b.ismainjob = 'Y' and b.lastflag = 'Y' and b.endflag = 'N' ");
				Map<String, Object> vo = (Map<String, Object>) getBaseDAO()
						.executeQuery(sql2.toString(), param1,
								new MapProcessor());
				ImportDataVO insVO = new ImportDataVO();
				Date now = new Date();
				SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat sf1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				String fieldtime = vo.get("fieldtime").toString();
				insVO.setPk_org(vo.get("pk_org").toString());
				insVO.setPk_group(vo.get("pk_group").toString());
				insVO.setCreator(vo.get("billmaker").toString());
				insVO.setCreationtime(new UFDateTime(now));
				insVO.setCalendardate(new UFLiteralDate(sf.parse(fieldtime)));
				insVO.setCalendartime(new UFDateTime(sf1.parse(fieldtime)));
				insVO.setDatatype(1);
				insVO.setDr(1); // 这里利用dr为1代表外勤
				insVO.setPk_psndoc(vo.get("billmaker").toString());
				getBaseDAO().insertVO(insVO);
				//回写信息到考勤日报                            地址 F_V_1 项目 F_V_2  备注 F_V_3
				StringBuffer daystatSql = new StringBuffer();
				daystatSql.append("update tbm_daystat set F_V_1 = '"+vo.get("addr").toString()+"'");
				if(vo.get("project_name") != null){
					daystatSql.append(",F_V_2 = '"+vo.get("project_name").toString()+"'");
				}
				if(vo.get("content") != null){
					daystatSql.append(",F_V_3 = '"+ vo.get("content").toString() +"'");
				}
				daystatSql.append(" where PK_PSNDOC='"+vo.get("billmaker").toString()+"' ");
				daystatSql.append(" and CALENDAR ='"+ new UFLiteralDate(sf.parse(fieldtime)) +"' ");
				getBaseDAO().executeUpdate(daystatSql.toString());
				
				

				String day = new UFLiteralDate(sf.parse(fieldtime)).toString();
//				String day = new UFLiteralDate(sf.parse("2019-09-30 10:29:18")).toString();
				String yearMonth = day.substring(0, 7);
//				yearMonth = "2019-09";
				 //获取当前月最后一天
		        Calendar ca = Calendar.getInstance();    
		        ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));  
		        String lastday = sf.format(ca.getTime());
		        int year = ca.get(Calendar.YEAR);
		        String month = "0" + (ca.get(Calendar.MONTH) + 1);
		        if(day.equals(lastday)){//最后一天  
					//回写信息到考勤月报                           备注 F_V_1 项目 F_V_2  合同号 F_V_3
		        	//备注：当月在哪些项目待多少天
		        	String countSql = "select F_V_2,count(1) from tbm_daystat "
		        			+ " where pk_psndoc ='"+vo.get("billmaker").toString()+"' and CALENDAR like '"+yearMonth+"%' "
		        			+ " and F_V_2 is not null group by F_V_2 ";
		        	List countList =  (List) getBaseDAO().executeQuery(countSql.toString(),new ArrayListProcessor());	
		        	String memo = "该员工本月在";
		        	for(Object object : countList){
		        		if(object !=null){
		        			Object[] obj = (Object[]) object;
		        			String msg = "[" + obj[0].toString() + "]工作了" + obj[1].toString() + "天;";
		        			memo = memo + msg;
		        		}
		        	}
		        	//项目：当月最后打卡的项目，和合同号
		        	String projectSql = "select * from ( "
		        			+ " select a.FIELDTIME,a.project_name,a.pk_project,b.def1 from tbm_field a "
		        			+ " left join bd_project b on b.pk_project = a.pk_project "
		        			+ " where a.pk_project is not null and FIELDTIME like '"+yearMonth+"%' and BILLMAKER='"+vo.get("billmaker").toString()+"' and APPRSTATE = 1"
		        					+ "order by a.FIELDTIME desc "
		        			+ " ) where ROWNUM = 1 ";
		        	Map<String, Object> projectVO = (Map<String, Object>) getBaseDAO().executeQuery(projectSql.toString(),new MapProcessor());	
		        	
		        	
	        		String updProjectSql = "update tbm_monthstat set F_V_1 ='"+ memo +"',F_V_2 = '"+projectVO.get("project_name")+"'"
    								+ ", F_V_3 = '"+projectVO.get("def1")+"'"
	        						+ " where tbmmonth ='"+month+"' and tbmyear ='"+year+"' and PK_PSNDOC='"+vo.get("billmaker").toString()+"' ";
					getBaseDAO().executeUpdate(updProjectSql);

		        
		        }
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else if ("1".equalsIgnoreCase(state)) {
			sql1.append("update tbm_field set apprstate = 2 where pk_field = ? ");
		}
		getBaseDAO().executeUpdate(sql1.toString(), param1);
		return result.toJson();
	}
	
	/**
	 * 根据单据主键批量更新外勤单据详情
	 */
	@Override
	public String batchUpdateMyFieldByPKS(Map<String, Object> param)
			throws BusinessException {
		PageResult result = new PageResult();
		ArrayList<String> pks = (ArrayList<String>) param.get("pks");
		String state = param.get("state").toString();
		InSQLCreator inc = new InSQLCreator();
		String insql = inc.getInSQL(pks.toArray(new String[pks.size()]));
		StringBuffer sql = new StringBuffer();
		// 0：审批通过 1：审批不通过
		if ("0".equalsIgnoreCase(state)) {
			try {
				sql.append("update tbm_field set apprstate = 1 where pk_field in ("
						+ insql + ") ");
				StringBuffer sql2 = new StringBuffer();
				sql2.append("select b.pk_org, b.pk_group, a.billmaker, a.fieldtime from tbm_field a ");
				sql2.append("inner join hi_psnjob b on a.billmaker = b.pk_psndoc ");
				sql2.append("where a.pk_field in("
						+ insql
						+ ") and b.ismainjob = 'Y' and b.lastflag = 'Y' and b.endflag = 'N' ");
				List<HashMap<String, Object>> voList = (List<HashMap<String, Object>>) getBaseDAO()
						.executeQuery(sql2.toString(), new MapListProcessor());
				ImportDataVO[] voArray = new ImportDataVO[voList.size()];
				Date now = new Date();
				SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat sf1 = new SimpleDateFormat(
						"yyyy-MM-dd hh:mm:ss");
				for (int i = 0; i < voList.size(); i++) {
					ImportDataVO insVO = new ImportDataVO();
					String fieldtime = voList.get(i).get("fieldtime")
							.toString();
					insVO.setPk_org(voList.get(i).get("pk_org").toString());
					insVO.setPk_group(voList.get(i).get("pk_group").toString());
					insVO.setCreator(voList.get(i).get("billmaker").toString());
					insVO.setCreationtime(new UFDateTime(now));
					insVO.setCalendardate(new UFLiteralDate(sf.parse(fieldtime)));
					insVO.setCalendartime(new UFDateTime(sf1.parse(fieldtime)));
					insVO.setDatatype(1);
					insVO.setDr(1); // 这里利用dr为1代表外勤
					insVO.setPk_psndoc(voList.get(i).get("billmaker")
							.toString());
					voArray[i] = insVO;
				}
				getBaseDAO().insertVOArray(voArray);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else if ("1".equalsIgnoreCase(state)) {
			sql.append("update tbm_field set apprstate = 2 where pk_field in (" + insql + ") ");
		}
		getBaseDAO().executeUpdate(sql.toString());
		return result.toJson();
	}
	
	private static double rad(double d) { 
        return d * Math.PI / 180.0; 
    }
	
	/**
	 * 根据两点经纬度计算出距离，然后判断是否在打卡半径内
	 * @param xLine 当前位置纬度
	 * @param yLine 当前位置经度
	 * @param lng2 考勤位置纬度
	 * @param lat2 考勤位置经度
	 * @return
	 */
	private boolean isInArea(String xLine, String yLine, Double lng2, Double lat2, Double radius) {
		double EARTH_RADIUS = 6378.137;
		Double lat1 = Double.parseDouble(yLine);
		Double lng1 = Double.parseDouble(xLine);
		
		double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double difference = radLat1 - radLat2;
        double mdifference = rad(lng1) - rad(lng2);
        double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(difference / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(mdifference / 2), 2)));
        distance = distance * EARTH_RADIUS;
        distance = Math.round(distance * 10000) / 10000;
		
        if (distance * 1000 > radius) {
        	return false;
        } else {
        	return true;
        }
	}
	
	private BaseDAO getBaseDAO() {
		if (this.baseDAO== null) {
			this.baseDAO = new BaseDAO();
		}
		return this.baseDAO;
	}
}
