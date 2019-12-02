package nc.vo.sass.fieldcheck;

import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFDateTime;

/**
 * 外勤申请VO
 * @author zszyff
 *
 */
public class FIeldCheckVO extends SuperVO {
	
	/**
	 * 主键
	 */
	public String pk_field;
	/**
	 * 提交人
	 */
	public String billmaker;
	/**
	 * 审批人
	 */
	public String approver;
	/**
	 * 审批状态
	 */
	public int apprstate;
	/**
	 * 外勤时间
	 */
	public UFDateTime fieldtime;
	/**
	 * 外勤地址
	 */
	public String addr;
	/**
	 * 外勤经度
	 */
	public String longitude;
	/**
	 * 外勤纬度
	 */
	public String latitude;
	/**
	 * 外勤类型
	 */
	public int fieldtype;
	/**
	 * 外勤说明
	 */
	public String content;
	/**
	 * 所在项目
	 */
	public String pk_project;
	/**
	 * 
	 * 项目名称
	 */
	public String project_name;

	public String getProject_name() {
		return project_name;
	}
	public void setProject_name(String project_name) {
		this.project_name = project_name;
	}
	public String getPk_project() {
		return pk_project;
	}
	public void setPk_project(String pk_project) {
		this.pk_project = pk_project;
	}
	
	public String getPk_field() {
		return pk_field;
	}
	public void setPk_field(String pk_field) {
		this.pk_field = pk_field;
	}
	public String getBillmaker() {
		return billmaker;
	}
	public void setBillmaker(String billmaker) {
		this.billmaker = billmaker;
	}
	public String getApprover() {
		return approver;
	}
	public void setApprover(String approver) {
		this.approver = approver;
	}
	public int getApprstate() {
		return apprstate;
	}
	public void setApprstate(int apprstate) {
		this.apprstate = apprstate;
	}
	public UFDateTime getFieldtime() {
		return fieldtime;
	}
	public void setFieldtime(UFDateTime fieldtime) {
		this.fieldtime = fieldtime;
	}
	public String getAddr() {
		return addr;
	}
	public void setAddr(String addr) {
		this.addr = addr;
	}
	
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public int getFieldtype() {
		return fieldtype;
	}
	public void setFieldtype(int fieldtype) {
		this.fieldtype = fieldtype;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	@Override
	public String getPrimaryKey() {
		return "pk_field";
	}
	@Override
	public String getTableName() {
		return "tbm_field";
	}
	

}
