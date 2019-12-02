/*     */ package com.yonyou.hrcloud.hrself.application.web;
/*     */ 
/*     */ import com.yonyou.hrcloud.hrself.common.MessageResult;
/*     */ import java.util.HashMap;
/*     */ import javax.servlet.http.HttpServletRequest;
/*     */ import javax.servlet.http.HttpSession;
/*     */ import nc.pub.facade.MessageFacade;
/*     */ import nc.pub.facade.MessagePageFacade;
/*     */ import net.sf.json.JSONArray;
/*     */ import net.sf.json.JSONObject;
/*     */ import org.springframework.web.bind.annotation.RequestBody;
/*     */ import org.springframework.web.bind.annotation.RequestMapping;
/*     */ import org.springframework.web.bind.annotation.ResponseBody;
/*     */ 
/*     */ @org.springframework.web.bind.annotation.RestController
/*     */ @RequestMapping({"/portal/message"})
/*     */ public class MessageController
/*     */ {
/*     */   @RequestMapping({"/queryUnreadMessageCount"})
/*     */   @ResponseBody
/*     */   public MessageResult queryUnreadMessageCount(HttpServletRequest request)
/*     */   {
/*  23 */     String psnCode = getUserId(request);
/*  24 */     String ncdata = null;
/*     */     try {
/*  26 */       ncdata = MessageFacade.queryUnreadMessageCount(psnCode);
/*  27 */       return getMessageSucess(ncdata);
/*     */     } catch (Exception e) {
/*  29 */       return getMessageError(e);
/*     */     }
/*     */   }
/*     */   
/*     */   @RequestMapping({"/queryMessageList"})
/*     */   @ResponseBody
/*     */   public MessageResult queryMessageList(HttpServletRequest request, String isRead) {
/*  36 */     String psnCode = getUserId(request);
/*  37 */     String ncdata = null;
/*     */     try {
/*  39 */       ncdata = MessageFacade.queryMessageByCondition(psnCode, isRead);
/*  40 */       return getMessageSucess(ncdata);
/*     */     } catch (Exception e) {
/*  42 */       return getMessageError(e);
/*     */     }
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   @RequestMapping({"/queryMessageCount"})
/*     */   @ResponseBody
/*     */   public Object queryMessageCount(HttpServletRequest request)
/*     */   {
/*  54 */     return MessagePageFacade.queryMessageCount(getUserCode(request), getUserId(request), com.yonyou.hrcloud.hrself.util.PropertyUtil.getPropertyByKey("XyWorkflowServiceHttpPort_address"));
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   @RequestMapping(value={"/queryMessageListByPage"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
/*     */   @ResponseBody
/*     */   public Object queryMessageListByPage(HttpServletRequest request, @RequestBody HashMap parm)
/*     */   {
/*  67 */     HashMap<String, Object> res = new HashMap();
/*  68 */     String psnCode = getUserId(request);
/*  69 */     String ncdata = null;
/*  70 */     String isRead = (String)parm.get("isRead");
/*  71 */     Integer currPage = (Integer)parm.get("currPage");
/*  72 */     Integer pageSize = (Integer)parm.get("pageSize");
/*     */     try {
/*  74 */       ncdata = MessagePageFacade.queryMessageByPageCondition(psnCode, isRead, currPage.intValue(), pageSize.intValue());
/*  75 */       MessageResult result = new MessageResult();
/*  76 */       result.setData(JSONObject.fromObject(ncdata));
/*  77 */       return result;
/*     */     } catch (Exception e) {
/*  79 */       return getMessageError(e);
/*     */     }
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   @RequestMapping(value={"/queryInfoMessageListByPage"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
/*     */   @ResponseBody
/*     */   public Object queryInfoMessageListByPage(HttpServletRequest request, @RequestBody HashMap parm)
/*     */   {
/*  91 */     HashMap<String, Object> res = new HashMap();
/*  92 */     String psnCode = getUserId(request);
/*  93 */     String ncdata = null;
/*  94 */     String isRead = (String)parm.get("isRead");
/*  95 */     String newType = (String)parm.get("newType");
/*  96 */     Integer currPage = (Integer)parm.get("currPage");
/*  97 */     Integer pageSize = (Integer)parm.get("pageSize");
/*     */     try {
/*  99 */       ncdata = MessagePageFacade.queryInfoMessageByPageCondition(psnCode, isRead, newType, currPage.intValue(), pageSize.intValue());
/* 100 */       MessageResult result = new MessageResult();
/* 101 */       result.setData(JSONObject.fromObject(ncdata));
/* 102 */       return result;
/*     */     } catch (Exception e) {
/* 104 */       return getMessageError(e);
/*     */     }
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   @RequestMapping(value={"/queryWorkMessageListByPage"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
/*     */   @ResponseBody
/*     */   public Object queryWorkMessageListByPage(HttpServletRequest request, @RequestBody HashMap parm)
/*     */   {
/* 116 */     HashMap<String, Object> res = new HashMap();
/* 117 */     String psnCode = getUserId(request);
/* 118 */     String ncdata = null;
/* 119 */     String isRead = (String)parm.get("isRead");
/* 120 */     String newType = (String)parm.get("newType");
/* 121 */     Integer currPage = (Integer)parm.get("currPage");
/* 122 */     Integer pageSize = (Integer)parm.get("pageSize");
/*     */     try {
/* 124 */       ncdata = MessagePageFacade.queryWorkMessageByPageCondition(psnCode, isRead, newType, currPage.intValue(), pageSize.intValue());
/* 125 */       MessageResult result = new MessageResult();
/* 126 */       result.setData(JSONObject.fromObject(ncdata));
/* 127 */       return result;
/*     */     } catch (Exception e) {
/* 129 */       return getMessageError(e);
/*     */     }
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */   @RequestMapping({"/queryMessageByPk"})
/*     */   @ResponseBody
/*     */   public MessageResult queryMessageByPk(String messagePk)
/*     */   {
/* 140 */     String ncdata = null;
/*     */     try {
/* 142 */       ncdata = MessageFacade.queryNCMessageByPk(messagePk);
/* 143 */       return getMessageSucess(ncdata);
/*     */     } catch (Exception e) {
/* 145 */       return getMessageError(e);
/*     */     }
/*     */   }
/*     */   
/* 149 */   private String getUserId(HttpServletRequest request) { HttpSession session = request.getSession();
/* 150 */     JSONObject ncSession = (JSONObject)session.getAttribute("ncSession");
/* 151 */     return ncSession.get("userID").toString();
/*     */   }
/*     */   
/*     */   private String getUserCode(HttpServletRequest request) {
/* 155 */     HttpSession session = request.getSession();
/* 156 */     JSONObject ncSession = (JSONObject)session.getAttribute("ncSession");
/* 157 */     return ncSession.get("userCode").toString();
/*     */   }
/*     */   
/*     */   private MessageResult getMessageError(Exception e) {
/* 161 */     MessageResult messageResult = new MessageResult();
/* 162 */     messageResult.setStatusCode(300);
/* 163 */     messageResult.setMessage(e.toString());
/* 164 */     return messageResult;
/*     */   }
/*     */   
/*     */   private MessageResult getMessageSucess(String ncData) {
/* 168 */     MessageResult messageResult = new MessageResult();
/* 169 */     messageResult.setStatusCode(200);
/* 170 */     if (ncData.startsWith("[")) {
/* 171 */       JSONArray jsonArray = JSONArray.fromObject(ncData);
/* 172 */       messageResult.setData(jsonArray);
/* 173 */     } else if (ncData.startsWith("{")) {
/* 174 */       JSONObject jsonObject = JSONObject.fromObject(ncData);
/* 175 */       messageResult.setData(jsonObject);
/*     */     }
/* 177 */     return messageResult;
/*     */   }
/*     */   
/* 180 */   private HashMap ncPageDateHandler(String ncdata) { HashMap<String, Object> res = new HashMap();
/* 181 */     Integer totalCount = Integer.valueOf(0);
/* 182 */     JSONArray serviceNcdata = null;
/* 183 */     if (ncdata != null) {
/* 184 */       JSONArray ncjsonArray = JSONArray.fromObject(ncdata);
/* 185 */       JSONArray pagedate = (JSONArray)ncjsonArray.get(0);
/* 186 */       if ((ncjsonArray.size() > 0) && (pagedate.size() > 0)) {
/* 187 */         serviceNcdata = pagedate;
/* 188 */         JSONObject totalCountObject = (JSONObject)ncjsonArray.get(ncjsonArray.size() - 1);
/* 189 */         totalCount = (Integer)totalCountObject.get("totalCount");
/*     */       }
/*     */     }
/* 192 */     res.put("statusCode", Integer.valueOf(200));
/* 193 */     res.put("message", "œÏ”¶≥…π¶");
/* 194 */     res.put("totalCount", totalCount);
/* 195 */     res.put("data", serviceNcdata);
/* 196 */     return res;
/*     */   }
/*     */ }

/* Location:           E:\yonyouworrspace\itemSupport\nc190923ora1119\hotwebs\hrssc\WEB-INF\lib\hrcloud-hrself-application-2.7.0.jar
 * Qualified Name:     com.yonyou.hrcloud.hrself.application.web.MessageController
 * Java Class Version: 7 (51.0)
 * JD-Core Version:    0.7.1
 */