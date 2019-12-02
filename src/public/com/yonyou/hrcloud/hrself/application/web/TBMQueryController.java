package com.yonyou.hrcloud.hrself.application.web;

import java.util.ArrayList;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import nc.pub.facade.PsnDimissionFacade;
import nc.pub.facade.REGMngFacade;
import nc.pub.facade.TBMQueryFacade;
import nc.pub.facade.TRNQueryFacade;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.yonyou.hrcloud.hrself.common.MessageResult;

/**
 * 员工假勤
 * Created by nijb@yonyou.com on 2017/11/9.
 */
@RestController
@RequestMapping(value = "/portal/tbmquery")
public class TBMQueryController {
    /**
     * 获取我的申请列表
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/getMyApplication", method = RequestMethod.POST)
    @ResponseBody
    public Object getMyApplication(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.getMyApplication(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }

    /**
     * 获取审批中心列表
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/getMyApprove", method = RequestMethod.POST)
    @ResponseBody
    public Object getMyApprove(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.getMyApprove(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }

    /**
     * 获取详情
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/getBillInfo", method = RequestMethod.POST)
    @ResponseBody
    public Object getBillInfo(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        Object ncMessageResult = null;
        String billtype=(String) param.get("billtype");
        String pk_h=(String) param.get("pk_h");
        String source = param.get("source") == null?"":"mobile";
        if ("leave".equals(billtype)){
            try{
                if(pk_h!=null && !"".equals(pk_h)) {
                    param.put("pk_leaveh",pk_h);
                }
                ncMessageResult = TBMQueryFacade.queryLeaveByPk(param);
            } catch(Exception e){
                ncMessageResult = getErrorMsg(e.getMessage());
            }
        }else if ("overtime".equals(billtype)){
            try {
                String pk_overtimeh = null;
                if(pk_h!=null && !"".equals(pk_h)) {
                    pk_overtimeh = pk_h;
                } else {
                    pk_overtimeh = (String) param.get("pk_overtimeh");
                }
                ncMessageResult= TBMQueryFacade.queryOvertimeByPK(pk_overtimeh);
                return new MessageResult(MessageResult.STATUS_SUCCESS,null,ncMessageResult);
            } catch (Exception e) {
                ncMessageResult = getErrorMsg(e.getMessage());
            }
        }else if ("signcard".equals(billtype)){
            try {
                String pk_signcardh = null;
                if(pk_h!=null && !"".equals(pk_h)) {
                    pk_signcardh = pk_h;
                } else {
                    pk_signcardh = (String) param.get("pk_signcardh");
                }
                ncMessageResult=TBMQueryFacade.querySigncardByPK(pk_signcardh);
                return new MessageResult(MessageResult.STATUS_SUCCESS,null,ncMessageResult);
            } catch (Exception e) {
                ncMessageResult = getErrorMsg(e.getMessage());
            }
        }else if ("away".equals(billtype)){
            try {
                String pk_awayh = null;
                if(pk_h!=null && !"".equals(pk_h)) {
                        pk_awayh = pk_h;
                } else {
                    pk_awayh = (String) param.get("pk_awayh");
                }
                ncMessageResult= TBMQueryFacade.queryAwayByPK(pk_awayh);
                return new MessageResult(MessageResult.STATUS_SUCCESS,null,ncMessageResult);
            } catch (Exception e) {
                ncMessageResult = getErrorMsg(e.getMessage());
            }
        }else if ("leaveoff".equals(billtype)){
            try{
                if(pk_h!=null && !"".equals(pk_h)) {
                    param.put("pk_leaveoff",pk_h);
                }
                ncMessageResult = TBMQueryFacade.queryLeaveoffByPk(param);
            } catch(Exception e){
                ncMessageResult = getErrorMsg(e.getMessage());
            }
        }else if ("awayoff".equals(billtype)){
            try{
                if(pk_h!=null && !"".equals(pk_h)) {
                    param.put("pk_awayoff",pk_h);
                }
                ncMessageResult = TBMQueryFacade.queryAwayoffByPk(param);
            } catch(Exception e){
                ncMessageResult = getErrorMsg(e.getMessage());
            }
        }else if ("psnreg".equals(billtype)){
            try{
                if(pk_h!=null && !"".equals(pk_h)) {
                    param.put("pk_hi_regapply",pk_h);
                }
                ncMessageResult = REGMngFacade.queryPsnRegByPk(param);
            } catch(Exception e){
                ncMessageResult = getErrorMsg(e.getMessage());
            }
        }
        else if ("trns".equals(billtype) || ("dimission".equals(billtype)&&source.equals("mobile"))){
            try{
                if(pk_h!=null && !"".equals(pk_h)) {
                    param.put("pk_hi_stapply",pk_h);
                }
                ncMessageResult = TRNQueryFacade.queryVOData(param);
            } catch(Exception e){
                ncMessageResult = getErrorMsg(e.getMessage());
            }
        }else if ("dimission".equals(billtype)&&!source.equals("mobile")){
            try{
                if(pk_h!=null && !"".equals(pk_h)) {
                    param.put("pk_hi_stapply",pk_h);
                }
                ncMessageResult = PsnDimissionFacade.queryDimissionBill(param);
                return new MessageResult(MessageResult.STATUS_SUCCESS,null,ncMessageResult);
            } catch(Exception e){
                ncMessageResult = getErrorMsg(e.getMessage());
            }
        }else{
            ncMessageResult = getErrorMsg("tbm_h_code有误");
        }
        return ncMessageResult;
    }

    /**
     * 审批单据（单条审批）
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/doApprove", method = RequestMethod.POST)
    @ResponseBody
    public Object doApprove(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        Object ncMessageResult = null;
        try{
        	//新增20191129  调配和离职   吧各部门的各交接细项写到nc的审批意见里
        	if(param.get("billtype").toString().equals("trns") || param.get("billtype").toString().equals("dimission")){
        		String workflownotes = param.get("workflownotes").toString() ;
    			JSONArray handoverArray = JSONArray.fromObject(param.get("handover").toString());
    			String handoverStr = "||";
    			for(int i = 0; i < handoverArray.size(); i++){
    				JSONObject handover = handoverArray.getJSONObject(i);
    				if(StringUtils.isNotEmpty(handover.get("item").toString()) && StringUtils.isNotEmpty(handover.get("status").toString())){
    					handoverStr += handover.get("item").toString()+":" +handover.get("status").toString() + "--";
    				}
    			}
    			if(handoverStr.equals("||")){
    				if(StringUtils.isNotEmpty(param.get("approveRemark").toString())){
    					workflownotes += param.get("approveRemark").toString();
    				}
    			}else{
    				if(StringUtils.isNotEmpty(param.get("approveRemark").toString())){
    					workflownotes += handoverStr.substring(0, handoverStr.lastIndexOf("--")) + "||" + param.get("approveRemark").toString();
    				}
    			}
//    			workflownotes += "||"+"\n" +"cccccc";
    			param.put("workflownotes",workflownotes);
        	}
            ncMessageResult = TBMQueryFacade.doApprove(param);
            return new MessageResult(MessageResult.STATUS_SUCCESS,null,ncMessageResult);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }

    /**
     * 审批单据（单条审批）
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/doBatchApprove", method = RequestMethod.POST)
    @ResponseBody
    public Object doBatchApprove(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        Object ncMessageResult = null;
        String userId = getUserId(request);
        param.put("userId",userId);
        try{
            ncMessageResult = TBMQueryFacade.doBatchApprove(param);
            return new MessageResult(MessageResult.STATUS_SUCCESS,null,ncMessageResult);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }


    /**
     * 获取详情
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/isDirectApprove", method = RequestMethod.POST)
    @ResponseBody
    public Object isDirectApprove(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        Object ncMessageResult = 0;
        try {
            ncMessageResult = TBMQueryFacade.queryDirectApprove(param);
            return new MessageResult(MessageResult.STATUS_SUCCESS,null,ncMessageResult);
        }catch( Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return new MessageResult(MessageResult.STATUS_ERROR,null,ncMessageResult);
    }


    @RequestMapping(value = "/getUser",method = RequestMethod.POST)
    public Object getUser(@RequestBody Map<String,Object> params){
        return "";
    }

    /**
     * 保存一条请假单
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/saveLeave", method = RequestMethod.POST)
    @ResponseBody
    public Object saveLeave(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.saveLeave(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }
    /**
     * 提交一条请假单
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/submitLeave", method = RequestMethod.POST)
    @ResponseBody
    public Object submitLeave(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.submitLeave(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }

    /**
     * 提交一条请假单
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/submitBill", method = RequestMethod.POST)
    @ResponseBody
    public Object submitBill(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try {
            ncMessageResult = TBMQueryFacade.submitBill(param);
        }catch (Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        /*if(param.get("billtype") == null ||"".equals(param.get("billtype"))) {
            ncMessageResult = getErrorMsg("billtype参数必传");
            return ncMessageResult;
        }
        try{
            String pk_h = param.get("pk_h").toString();
            String billtype= param.get("billtype").toString();
            if("leave".equals(billtype) ){
                param.put("pk_leaveh",pk_h);
                ncMessageResult = TBMQueryFacade.submitLeave(param);
            } else if("overtime".equals(billtype)){
                param.put("pk_overtimeh",pk_h);
                ncMessageResult = TBMQueryFacade.submitOvertime(param);
            }else if("signcard".equals(billtype)){
                param.put("pk_signcardh",pk_h);
                ncMessageResult = TBMQueryFacade.submitSigncard(param);
            }else if("away".equals(billtype)){
                param.put("pk_awayh",pk_h);
                ncMessageResult = TBMQueryFacade.submitAway(param);
            } else {
                ncMessageResult = getErrorMsg("billtype参数不正确。");
            }
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }*/
        return ncMessageResult;
    }
    /**
     * 提交一条请假单
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/deleteBill", method = RequestMethod.POST)
    @ResponseBody
    public Object deleteBill(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        if(param.get("billtype") == null ||"".equals(param.get("billtype"))) {
            ncMessageResult = getErrorMsg("billtype参数必传");
            return ncMessageResult;
        }
        try{
            String pk_h = param.get("pk_h").toString();
            String billtype= param.get("billtype").toString();
            if("leave".equals(billtype) ){
                param.put("pk_leaveh",pk_h);
                ncMessageResult = TBMQueryFacade.deleteLeave(param);
            } else if("overtime".equals(billtype)){
                param.put("pk_overtimeh",pk_h);
                ncMessageResult = TBMQueryFacade.deleteOvertime(param);
            }else if("signcard".equals(billtype)){
                Map<String,Object> delinfo = TBMQueryFacade.deleteSignCard(pk_h);
                return new MessageResult(MessageResult.STATUS_SUCCESS,"操作成功",delinfo);
            }else if("away".equals(billtype)){
                param.put("pk_awayh",pk_h);
                ncMessageResult = TBMQueryFacade.deleteAway(param);
            }else if("leaveoff".equals(billtype) ){
                param.put("pk_leaveoff",pk_h);
                ncMessageResult = TBMQueryFacade.deleteLeaveoff(param);
            }else if("awayoff".equals(billtype) ){
                param.put("pk_awayoff",pk_h);
                ncMessageResult = TBMQueryFacade.deleteAwayoff(param);
            }else if("psnreg".equals(billtype) ){
                param.put("pk_hi_regapply",pk_h);
                ncMessageResult = REGMngFacade.deletePsnReg(param);
            }else if("dimission".equals(billtype) ){
                param.put("pk_hi_stapply",pk_h);
                ncMessageResult = PsnDimissionFacade.deleteDimissionBill(param);
            }else if("trns".equals(billtype)){
            	param.put("pk_h",pk_h);
                ncMessageResult = TRNQueryFacade.deleteVOData(param);
            }
            else {
                ncMessageResult = getErrorMsg("billtype参数不正确。");
            }
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }
    /**
     * 保存并提交一条请假单
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/saveAndSubmitLeave", method = RequestMethod.POST)
    @ResponseBody
    public Object saveAndSubmitLeave(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.saveAndSubmitLeave(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }
    /**
     * 获取请假类型参照
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryLeaveType", method = RequestMethod.POST)
    @ResponseBody
    public Object queryLeaveType(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.queryLeaveType(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }

    /**
     * 获取流程类型参照
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryTranstype", method = RequestMethod.POST)
    @ResponseBody
    public Object queryTranstype(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.queryTranstype(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }
    /**
     * 计算请假时长
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/calculateLeaveLength", method = RequestMethod.POST)
    @ResponseBody
    public Object calculateLeaveLength(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.calculateLeaveLength(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }
    /**
     * 计算请假剩余
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/getLeaveBalance", method = RequestMethod.POST)
    @ResponseBody
    public Object getLeaveBalance(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.getLeaveBalance(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }
    /**
     * 删除请假单
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/deleteLeave", method = RequestMethod.POST)
    @ResponseBody
    public Object deleteLeave(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.deleteLeave(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }
    /**
     * 获取一个新的请假单
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/getNewLeave", method = RequestMethod.POST)
    @ResponseBody
    public Object getNewLeave(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.getNewLeave(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }
    /**
     * 获取考勤日历中心
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryCalendar", method = RequestMethod.POST)
    @ResponseBody
    public Object queryCalendar(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.queryCalendar(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }
    /**
     * 获取考勤日历中心
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryCalendarDayDetails", method = RequestMethod.POST)
    @ResponseBody
    public Object queryCalendarDayDetails(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.queryCalendarDayDetails(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }

    /**
     * 获取流程节点列表
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryProcessNodeList", method = RequestMethod.POST)
    @ResponseBody
    public Object queryProcessNodeList(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.queryProcessNodeList(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }
    /**
     * 获取流程节点列表
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryApproverList", method = RequestMethod.POST)
    @ResponseBody
    public Object queryApproverList(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.queryApproverList(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }
    private String getUserId(HttpServletRequest request) {
        String userId = null;
        HttpSession session = request.getSession();
        JSONObject ncSession = (JSONObject)session.getAttribute("ncSession");
        userId = ncSession.get("userID").toString();
        return userId;
    }
    private String getErrorMsg(String errormsg) {
        JSONObject jsonObject= new JSONObject();
        jsonObject.element("statusCode","300");

        if(errormsg == null || "".equals(errormsg)){

        }
        if(StringUtils.isEmpty(errormsg) ){
            jsonObject.element("message","操作失败");
        } else {
            jsonObject.element("message",errormsg);
        }
        jsonObject.element("data",new ArrayList());
        return jsonObject.toString();
    }

    /**
     * 请假申请单打印信息查询
     * @param param
     * @return
     */
    @RequestMapping(value = "/leavePrintTemplate", method = RequestMethod.POST)
    @ResponseBody
    public MessageResult leavePrintTemplate(@RequestBody Map<String, Object> param){
        try {
            String ncdate = TBMQueryFacade.leavePrintTemplate(param);
            return new MessageResult(MessageResult.STATUS_SUCCESS,null,ncdate);
        } catch (Exception e) {
            return new MessageResult(MessageResult.STATUS_ERROR,e.getMessage(),null);
        }
    }

    /**
     * 销假申请单打印信息查询
     * @param param
     * @return
     */
    @RequestMapping(value = "/leaveoffPrintTemplate", method = RequestMethod.POST)
    @ResponseBody
    public MessageResult leaveoffPrintTemplate(@RequestBody Map<String, Object> param){
        try {
            String ncdate = TBMQueryFacade.leaveoffPrintTemplate(param);
            return new MessageResult(MessageResult.STATUS_SUCCESS,null,ncdate);
        } catch (Exception e) {
            return new MessageResult(MessageResult.STATUS_ERROR,e.getMessage(),null);
        }
    }

    @RequestMapping(value = "/checkTimeBroken", method = RequestMethod.POST)
    @ResponseBody
    public Object checkTimeBroken(@RequestBody Map<String,Object>  param, HttpServletRequest request)
    {
        String userId = getUserId(request);
        param.put("userId",userId);
        String type = param.get("type").toString();
        Object result = null;
        try{
            if(type.equals("leave")){
                result = TBMQueryFacade.checkTimeBrokenLeave(param);
            }else if(type.equals("overtime")){
                result = TBMQueryFacade.checkTimeBrokenOvertime(param);
            }else if(type.equals("away")){
                result = TBMQueryFacade.checkTimeBrokenAway(param);
            }else{}
            return new MessageResult(MessageResult.STATUS_SUCCESS,null,result);
        }catch(Exception e){
            result =  getErrorMsg(e.getMessage());
            return new MessageResult(MessageResult.STATUS_ERROR,null,result);
        }
    }

    /**
     * 保存一条请假单
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryCpuser", method = RequestMethod.POST)
    @ResponseBody
    public Object queryCpuser(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.getUserInfo(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }
    /**
     * 查询人员
     * @param param
     * @param request
     * @return
     * @author wangtian1
     */
    @RequestMapping(value = "/queryPsndoc", method = RequestMethod.POST)
    @ResponseBody
    public Object queryPsndoc(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.getPsndocInfo(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }

    /**
     * 保存一条请假单
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/sendMessage", method = RequestMethod.POST)
    @ResponseBody
    public Object sendMessage(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.sendMessage(param);
            return new MessageResult(MessageResult.STATUS_SUCCESS,null,ncMessageResult);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
            return new MessageResult(MessageResult.STATUS_ERROR,null,ncMessageResult);
        }
    }

    /**
     * 销假申请获取休假记录
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryLeaveReg", method = RequestMethod.POST)
    @ResponseBody
    public Object queryLeaveReg(@RequestBody Map<String,Object>  param, HttpServletRequest request) {
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.queryLeavereg4off(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }

    /**
     * 销假申请获取休假记录
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryLeaveregByPk", method = RequestMethod.POST)
    @ResponseBody
    public Object queryLeaveregByPk(@RequestBody Map<String,Object>  param, HttpServletRequest request) {
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.queryLeaveregByPk(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }

    /**
     * 计算销假时长
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/calculateLeaveoffLength", method = RequestMethod.POST)
    @ResponseBody
    public Object calculateLeaveoffLength(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.calculateLeaveoffLength(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }

    /**
     * 保存一条销假单
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/saveLeaveoff", method = RequestMethod.POST)
    @ResponseBody
    public Object saveLeaveoff(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.saveLeaveoff(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }

    /**
     * 提交一条销假单
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/submitLeaveoff", method = RequestMethod.POST)
    @ResponseBody
    public Object submitLeaveoff(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.submitLeaveoff(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }

    /**
     * 销差申请获取出差记录
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryAwayReg", method = RequestMethod.POST)
    @ResponseBody
    public Object queryAwayReg(@RequestBody Map<String,Object>  param, HttpServletRequest request) {
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.queryAwayreg4off(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }

    /**
     * 销差申请获取出差记录
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryAwayregByPk", method = RequestMethod.POST)
    @ResponseBody
    public Object queryAwayregByPk(@RequestBody Map<String,Object>  param, HttpServletRequest request) {
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.queryAwayregByPk(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }

    /**
     * 计算销差时长
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/calculateAwayoffLength", method = RequestMethod.POST)
    @ResponseBody
    public Object calculateAwayoffLength(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.calculateAwayoffLength(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }

    /**
     * 保存一条销差单
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/saveAwayoff", method = RequestMethod.POST)
    @ResponseBody
    public Object saveAwayoff(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.saveAwayoff(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }

    /**
     * 提交一条销差单
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/submitAwayoff", method = RequestMethod.POST)
    @ResponseBody
    public Object submitAwayoff(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TBMQueryFacade.submitAwayoff(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }

    /**
     * 销差申请单打印信息查询
     * @param param
     * @return
     */
    @RequestMapping(value = "/awayoffPrintTemplate", method = RequestMethod.POST)
    @ResponseBody
    public MessageResult awayoffPrintTemplate(@RequestBody Map<String, Object> param){
        try {
            String ncdate = TBMQueryFacade.awayoffPrintTemplate(param);
            return new MessageResult(MessageResult.STATUS_SUCCESS,null,ncdate);
        } catch (Exception e) {
            return new MessageResult(MessageResult.STATUS_ERROR,e.getMessage(),null);
        }
    }

}
