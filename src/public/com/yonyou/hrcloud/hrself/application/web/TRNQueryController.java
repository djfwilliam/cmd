package com.yonyou.hrcloud.hrself.application.web;

import nc.pub.facade.TRNQueryFacade;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.yonyou.hrcloud.hrself.common.MessageResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping(value = "/portal/trnquery")
public class TRNQueryController {
    /**
     * 获取流程类型参照
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryTranstype", method = RequestMethod.POST)
    @ResponseBody
    public Object queryTranstype(@RequestBody Map<String,Object> param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TRNQueryFacade.queryTrnBusiType(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }

    /**
     * 根据调配业务类型重新绘制模版
     * @param param
     * @param request
     * @return
     */

    @RequestMapping(value = "/queryTemplet", method = RequestMethod.POST)
    @ResponseBody
    public Object queryTemplet(@RequestBody Map<String,Object> param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TRNQueryFacade.queryTemplet(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;

    }

    /**
     * 字段联动 职务和岗位的字段联动
     *   1. 变动岗位时 岗位序列必须同时变动
     *   2. 变动职务时，职务类别也需要同时变动
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryDataType", method = RequestMethod.POST)
    @ResponseBody
    public Object queryDataType(@RequestBody Map<String,Object> param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TRNQueryFacade.queryDataType(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }
	/**
	 * 获取登入时的人员的cuserid
	 * @param request
	 * @return
	 */
    private String getUserId(HttpServletRequest request) {
        String userId = null;
        HttpSession session = request.getSession();
        JSONObject ncSession = (JSONObject)session.getAttribute("ncSession");
        userId = ncSession.get("userID").toString();
        return userId;
    }
    /**
     * controller中获取错误信息的汇总方法
     * @param errormsg
     * @return
     */
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
     * 获取参照内容
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryRefInfo", method = RequestMethod.POST)
    @ResponseBody
    public Object queryRefInfo(@RequestBody Map<String,Object> param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TRNQueryFacade.queryRefInfo(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }
    /**
     * 调配保存/更新方法
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/savebill", method = RequestMethod.POST)
    @ResponseBody
    public Object saveBill(@RequestBody Map<String,Object> param, HttpServletRequest request){
    	String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = TRNQueryFacade.saveBill(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }
    /**
     * 组织参照
     * @return
     */
    @RequestMapping("/hrorgs")
    public Object queryHrOrgs() {
    	String ncMessageResult = null;
        try{
            ncMessageResult = TRNQueryFacade.hrorgsQuery("");
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }
    /**
     * 详情界面信息查询
     * @param param
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/queryVOData", method = RequestMethod.POST)
    @ResponseBody
    public Object queryVOData(@RequestBody Map<String,Object> param, HttpServletRequest request) throws Exception{
    	String ncMessageResult = null;
    	String userId = getUserId(request);
        param.put("userId",userId);
        try{
            ncMessageResult = TRNQueryFacade.queryVOData(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }
    
    /**
     * 保存并提交签卡审批单
     * @param param
     * @return
     */
    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    @ResponseBody
    public MessageResult submitTrns(@RequestBody Map<String, Object> param, HttpServletRequest request){
        try {
        	String userId = getUserId(request);
            param.put("userId",userId);
            String ncdate = TRNQueryFacade.submitTrns(param);
            return new MessageResult(MessageResult.STATUS_SUCCESS,null,ncdate);
        } catch (Exception e) { 
            return new MessageResult(MessageResult.STATUS_ERROR,e.getMessage(),null);
        }
    }

    /**
     * 收回签卡审批单
     * @param param
     * @return
     */
    @RequestMapping(value = "/callback", method = RequestMethod.POST)
    @ResponseBody
    public MessageResult callbackTrns(@RequestBody Map<String, Object> param, HttpServletRequest request) {
        try {
        	String userId = getUserId(request);
            param.put("userId",userId);
            String ncdate = TRNQueryFacade.callbackTrns(param);
            return new MessageResult(MessageResult.STATUS_SUCCESS,null,ncdate);
        } catch (Exception e) {
            return new MessageResult(MessageResult.STATUS_ERROR,e.getMessage(),null);
        }
    }
    
    @RequestMapping(value="/validateValidBudget",method = RequestMethod.POST)
    @ResponseBody
    public MessageResult validateValidBudget(@RequestBody Map<String, Object> param, HttpServletRequest request){
    	try {
        	String userId = getUserId(request);
            param.put("userId",userId);
            String ncdate = TRNQueryFacade.validateValidBudget(param);
            return new MessageResult(MessageResult.STATUS_SUCCESS,null,ncdate);
        } catch (Exception e) {
            return new MessageResult(MessageResult.STATUS_ERROR,e.getMessage(),null);
        }
    }
    
}
