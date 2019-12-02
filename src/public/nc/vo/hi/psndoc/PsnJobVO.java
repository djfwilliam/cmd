/*      */ package nc.vo.hi.psndoc;
/*      */ 
/*      */ import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFLiteralDate;

import org.apache.commons.lang.ArrayUtils;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ public class PsnJobVO
/*      */   extends PsnSuperVO
/*      */ {
/*      */   private static final String _TABLE_NAME = "hi_psnjob";
/*      */   public static final String ASSGID = "assgid";
/*      */   public static final String BEGINDATE = "begindate";
/*      */   public static final String CLERKCODE = "clerkcode";
/*      */   public static final String DATAORIGINFLAG = "dataoriginflag";
/*      */   public static final String DEPOSEMODE = "deposemode";
/*      */   public static final String ENDDATE = "enddate";
/*      */   public static final String ENDFLAG = "endflag";
/*      */   public static final String ISMAINJOB = "ismainjob";
/*      */   public static final String JOBMODE = "jobmode";
/*      */   public static final String LASTFLAG = "lastflag";
/*      */   public static final String MEMO = "memo";
/*      */   public static final String OCCUPATION = "occupation";
/*      */   public static final String ORIBILLPK = "oribillpk";
/*      */   public static final String ORIBILLTYPE = "oribilltype";
/*      */   public static final String PK_DEPT = "pk_dept";
/*      */   public static final String PK_GROUP = "pk_group";
/*      */   public static final String PK_HRGROUP = "pk_hrgroup";
/*      */   public static final String PK_HRORG = "pk_hrorg";
/*      */   public static final String PK_JOB = "pk_job";
/*      */   public static final String PK_JOB_TYPE = "pk_job_type";
/*      */   public static final String PK_JOBGRADE = "pk_jobgrade";
/*      */   public static final String PK_JOBRANK = "pk_jobrank";
/*      */   public static final String PK_ORG = "pk_org";
/*      */   public static final String PK_POST = "pk_post";
/*      */   public static final String PK_POSTSERIES = "pk_postseries";
/*      */   public static final String PK_PSNCL = "pk_psncl";
/*      */   public static final String PK_PSNDOC = "pk_psndoc";
/*      */   public static final String PK_PSNJOB = "pk_psnjob";
/*      */   public static final String PK_PSNORG = "pk_psnorg";
/*      */   public static final String POSTSTAT = "poststat";
/*      */   public static final String PSNTYPE = "psntype";
/*      */   public static final String RECORDNUM = "recordnum";
/*      */   private static final long serialVersionUID = 9099264722673975580L;
/*      */   public static final String SERIES = "series";
/*      */   public static final String SHOWORDER = "showorder";
/*      */   public static final String TRIAL_FLAG = "trial_flag";
/*      */   public static final String TRIAL_TYPE = "trial_type";
/*      */   public static final String TRNSEVENT = "trnsevent";
/*      */   public static final String TRNSREASON = "trnsreason";
/*      */   public static final String TRNSTYPE = "trnstype";
/*      */   public static final String WORKTYPE = "worktype";
/*      */   public static final String PK_ORG_V = "pk_org_v";
/*      */   public static final String PK_DEPT_V = "pk_dept_v";


			public static final String GOBGLBDEF6 = "gobglbdef6";
			public static final String GOBGLBDEF7 = "gobglbdef7";
			public static final String GOBGLBDEF8 = "gobglbdef8";
			public static final String GOBGLBDEF9 = "gobglbdef9";
			public static final String GOBGLBDEF10 = "gobglbdef10";
			
			
			
/*      */   private Integer assgid;
/*      */   private UFLiteralDate begindate;
/*      */   private String clerkcode;
/*      */   private Integer dataoriginflag;
/*      */   private String deposemode;
/*      */   private String deptname;
/*      */   private UFLiteralDate enddate;
/*  114 */   private UFBoolean endflag = UFBoolean.FALSE;
/*  115 */   private UFBoolean ismainjob = UFBoolean.TRUE;
/*      */   
/*      */   private String jobmode;
/*      */   
/*      */   private String jobname;
/*      */   
/*      */   private UFBoolean lastflag;
/*      */   
/*      */   private String memo;
/*      */   
/*      */   private String occupation;
/*      */   
/*      */   private String oribillpk;
/*      */   
/*      */   private String oribilltype;
/*      */   
/*      */   private String pk_dept;
/*      */   private String pk_group;
/*      */   private String pk_hrgroup;
/*      */   private String pk_hrorg;
/*      */   private String pk_job;
/*      */   private String pk_job_type;
/*      */   private String pk_jobgrade;
/*      */   private String pk_jobrank;
/*      */   private String pk_org;
/*      */   private String orgname;
/*      */   private String pk_post;
/*      */   private String pk_postseries;
/*      */   private String pk_psncl;
/*      */   private String pk_psndoc;
/*      */   private String pk_psnjob;
/*      */   private String pk_psnorg;
/*      */   private UFBoolean poststat;
/*      */   private Integer psntype;
/*      */   private Integer recordnum;
/*      */   private String series;
/*      */   private Integer showorder;
/*      */   private UFBoolean trial_flag;
/*      */   private Integer trial_type;
/*      */   private Integer trnsevent;
/*      */   private String trnsreason;
/*      */   private String trnstype;
/*      */   private String worktype;
/*      */   private String pk_dept_v;
/*      */   private String pk_org_v;
			

		     //ÐÂ¼Ó×Ö¶Î
			 private String gobglbdef6;
			 private String gobglbdef7;
			 private String gobglbdef8;
			 private String gobglbdef9;
			 private String gobglbdef10;
/*      */   
/*      */   public static String getDefaultTableName()
/*      */   {
/*  163 */     return "hi_psnjob";
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String[] getAttributeNames()
/*      */   {
/*  179 */     String[] strAttrNames = super.getAttributeNames();
/*      */     
/*  181 */     strAttrNames = (String[])ArrayUtils.removeElement(strAttrNames, "pk_org_v");
/*  182 */     strAttrNames = (String[])ArrayUtils.removeElement(strAttrNames, "pk_dept_v");
/*      */     
/*  184 */     return strAttrNames;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public Integer getAssgid()
/*      */   {
/*  196 */     return assgid;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public UFLiteralDate getBegindate()
/*      */   {
/*  208 */     return begindate;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getClerkcode()
/*      */   {
/*  220 */     return clerkcode;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public Integer getDataoriginflag()
/*      */   {
/*  232 */     return dataoriginflag;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getDeposemode()
/*      */   {
/*  244 */     return deposemode;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getDeptname()
/*      */   {
/*  256 */     return deptname;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public UFLiteralDate getEnddate()
/*      */   {
/*  268 */     return enddate;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public UFBoolean getEndflag()
/*      */   {
/*  280 */     return endflag;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public UFBoolean getIsmainjob()
/*      */   {
/*  292 */     return ismainjob;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getJobmode()
/*      */   {
/*  304 */     return jobmode;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getJobname()
/*      */   {
/*  316 */     return jobname;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public UFBoolean getLastflag()
/*      */   {
/*  328 */     return lastflag;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getMemo()
/*      */   {
/*  340 */     return memo;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getOccupation()
/*      */   {
/*  352 */     return occupation;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getOribillpk()
/*      */   {
/*  364 */     return oribillpk;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getOribilltype()
/*      */   {
/*  376 */     return oribilltype;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getParentPKFieldName()
/*      */   {
/*  389 */     return "pk_psndoc";
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getPk_dept()
/*      */   {
/*  401 */     return pk_dept;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getPk_group()
/*      */   {
/*  413 */     return pk_group;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getPk_hrgroup()
/*      */   {
/*  425 */     return pk_hrgroup;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getPk_hrorg()
/*      */   {
/*  437 */     return pk_hrorg;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getPk_job()
/*      */   {
/*  449 */     return pk_job;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getPk_job_type()
/*      */   {
/*  461 */     return pk_job_type;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getPk_jobgrade()
/*      */   {
/*  473 */     return pk_jobgrade;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getPk_jobrank()
/*      */   {
/*  485 */     return pk_jobrank;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getPk_org()
/*      */   {
/*  497 */     return pk_org;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getPk_post()
/*      */   {
/*  509 */     return pk_post;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getPk_postseries()
/*      */   {
/*  521 */     return pk_postseries;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getPk_psncl()
/*      */   {
/*  533 */     return pk_psncl;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getPk_psndoc()
/*      */   {
/*  545 */     return pk_psndoc;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getPk_psnjob()
/*      */   {
/*  557 */     return pk_psnjob;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getPk_psnorg()
/*      */   {
/*  569 */     return pk_psnorg;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getPKFieldName()
/*      */   {
/*  582 */     return "pk_psnjob";
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public UFBoolean getPoststat()
/*      */   {
/*  594 */     return poststat;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public Integer getPsntype()
/*      */   {
/*  606 */     return psntype;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public Integer getRecordnum()
/*      */   {
/*  618 */     return recordnum;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getSeries()
/*      */   {
/*  630 */     return series;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public Integer getShoworder()
/*      */   {
/*  642 */     return showorder;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getTableName()
/*      */   {
/*  655 */     return "hi_psnjob";
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public UFBoolean getTrial_flag()
/*      */   {
/*  667 */     return trial_flag;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public Integer getTrial_type()
/*      */   {
/*  679 */     return trial_type;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public Integer getTrnsevent()
/*      */   {
/*  691 */     return trnsevent;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getTrnsreason()
/*      */   {
/*  703 */     return trnsreason;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getTrnstype()
/*      */   {
/*  715 */     return trnstype;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String getWorktype()
/*      */   {
/*  727 */     return worktype;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setAssgid(Integer assgid)
/*      */   {
/*  740 */     this.assgid = assgid;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setBegindate(UFLiteralDate begindate)
/*      */   {
/*  753 */     this.begindate = begindate;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setClerkcode(String clerkcode)
/*      */   {
/*  766 */     this.clerkcode = clerkcode;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setDataoriginflag(Integer dataoriginflag)
/*      */   {
/*  779 */     this.dataoriginflag = dataoriginflag;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setDeposemode(String deposemode)
/*      */   {
/*  792 */     this.deposemode = deposemode;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setDeptname(String deptname)
/*      */   {
/*  805 */     this.deptname = deptname;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setEnddate(UFLiteralDate enddate)
/*      */   {
/*  818 */     this.enddate = enddate;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setEndflag(UFBoolean endflag)
/*      */   {
/*  831 */     this.endflag = endflag;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setIsmainjob(UFBoolean ismainjob)
/*      */   {
/*  844 */     this.ismainjob = ismainjob;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setJobmode(String jobmode)
/*      */   {
/*  857 */     this.jobmode = jobmode;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setJobname(String jobname)
/*      */   {
/*  870 */     this.jobname = jobname;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setLastflag(UFBoolean lastflag)
/*      */   {
/*  883 */     this.lastflag = lastflag;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setMemo(String memo)
/*      */   {
/*  896 */     this.memo = memo;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setOccupation(String occupation)
/*      */   {
/*  909 */     this.occupation = occupation;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setOribillpk(String oribillpk)
/*      */   {
/*  922 */     this.oribillpk = oribillpk;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setOribilltype(String oribilltype)
/*      */   {
/*  935 */     this.oribilltype = oribilltype;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setPk_dept(String pkDept)
/*      */   {
/*  948 */     pk_dept = pkDept;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setPk_group(String pkGroup)
/*      */   {
/*  961 */     pk_group = pkGroup;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setPk_hrgroup(String pk_hrgroup)
/*      */   {
/*  974 */     this.pk_hrgroup = pk_hrgroup;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setPk_hrorg(String pkOperorg)
/*      */   {
/*  987 */     pk_hrorg = pkOperorg;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setPk_job(String pkJob)
/*      */   {
/* 1000 */     pk_job = pkJob;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setPk_job_type(String pk_job_type)
/*      */   {
/* 1013 */     this.pk_job_type = pk_job_type;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setPk_jobgrade(String pkJobgrade)
/*      */   {
/* 1026 */     pk_jobgrade = pkJobgrade;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setPk_jobrank(String pkJobrank)
/*      */   {
/* 1039 */     pk_jobrank = pkJobrank;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setPk_org(String pkOrg)
/*      */   {
/* 1052 */     pk_org = pkOrg;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setPk_post(String pkPost)
/*      */   {
/* 1065 */     pk_post = pkPost;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setPk_postseries(String pkPostseries)
/*      */   {
/* 1078 */     pk_postseries = pkPostseries;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setPk_psncl(String pkPsncl)
/*      */   {
/* 1091 */     pk_psncl = pkPsncl;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setPk_psndoc(String pkPsndoc)
/*      */   {
/* 1104 */     pk_psndoc = pkPsndoc;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setPk_psnjob(String pkPsnjob)
/*      */   {
/* 1117 */     pk_psnjob = pkPsnjob;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setPk_psnorg(String pkPsnorg)
/*      */   {
/* 1130 */     pk_psnorg = pkPsnorg;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setPoststat(UFBoolean poststat)
/*      */   {
/* 1143 */     this.poststat = poststat;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setPsntype(Integer psntype)
/*      */   {
/* 1156 */     this.psntype = psntype;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setRecordnum(Integer recordnum)
/*      */   {
/* 1169 */     this.recordnum = recordnum;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setSeries(String series)
/*      */   {
/* 1182 */     this.series = series;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setShoworder(Integer showorder)
/*      */   {
/* 1195 */     this.showorder = showorder;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setTrial_flag(UFBoolean trialFlag)
/*      */   {
/* 1208 */     trial_flag = trialFlag;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setTrial_type(Integer trialType)
/*      */   {
/* 1221 */     trial_type = trialType;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setTrnsevent(Integer trnsevent)
/*      */   {
/* 1234 */     this.trnsevent = trnsevent;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setTrnsreason(String trnsreason)
/*      */   {
/* 1247 */     this.trnsreason = trnsreason;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setTrnstype(String trnstype)
/*      */   {
/* 1260 */     this.trnstype = trnstype;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void setWorktype(String worktype)
/*      */   {
/* 1273 */     this.worktype = worktype;
/*      */   }
/*      */   
/*      */   public void setOrgname(String orgname)
/*      */   {
/* 1278 */     this.orgname = orgname;
/*      */   }
/*      */   
/*      */   public String getOrgname()
/*      */   {
/* 1283 */     return orgname;
/*      */   }
/*      */   
/*      */   public void setPk_dept_v(String pk_dept_v)
/*      */   {
/* 1288 */     this.pk_dept_v = pk_dept_v;
/*      */   }
/*      */   
/*      */   public String getPk_dept_v()
/*      */   {
/* 1293 */     return pk_dept_v;
/*      */   }
/*      */   
/*      */   public void setPk_org_v(String pk_org_v)
/*      */   {
/* 1298 */     this.pk_org_v = pk_org_v;
/*      */   }
/*      */   
/*      */   public String getPk_org_v()
/*      */   {
/* 1303 */     return pk_org_v;
/*      */   }
			 
			public String getGobglbdef6() {
				return gobglbdef6;
			}
			public void setGobglbdef6(String gobglbdef6) {
				this.gobglbdef6 = gobglbdef6;
			}
			public String getGobglbdef7() {
				return gobglbdef7;
			}
			public void setGobglbdef7(String gobglbdef7) {
				this.gobglbdef7 = gobglbdef7;
			}
			public String getGobglbdef8() {
				return gobglbdef8;
			}
			public void setGobglbdef8(String gobglbdef8) {
				this.gobglbdef8 = gobglbdef8;
			}
			public String getGobglbdef9() {
				return gobglbdef9;
			}
			public void setGobglbdef9(String gobglbdef9) {
				this.gobglbdef9 = gobglbdef9;
			}
			public String getGobglbdef10() {
				return gobglbdef10;
			}
			public void setGobglbdef10(String gobglbdef10) {
				this.gobglbdef10 = gobglbdef10;
			}

			 
/*      */ }

/* Location:           E:\yonyouworrspace\itemSupport\nc190923ora1119\modules\hrhi\lib\pubhrhi_personnelmgt.jar
 * Qualified Name:     nc.vo.hi.psndoc.PsnJobVO
 * Java Class Version: 7 (51.0)
 * JD-Core Version:    0.7.1
 */