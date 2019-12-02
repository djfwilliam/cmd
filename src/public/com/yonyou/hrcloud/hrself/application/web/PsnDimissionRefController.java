package com.yonyou.hrcloud.hrself.application.web;

import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import nc.pub.facade.PsnDimissionFacade;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 员工离职
 * Created by wujp9@yonyou.com on 2018/1/9.
 */
@RestController
@RequestMapping(value = "/portal/dimission")
public class PsnDimissionRefController {
    
    /**
     * 获取用户ID
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
     * 获取报错信息
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
     * 获取离职类型参照
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryDimissionRef", method = RequestMethod.POST)
    @ResponseBody
    public Object queryDimissionType(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = PsnDimissionFacade.queryDimissionRef(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }
    
	/**
     * 获取离职人员信息
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryDimissionPsnInfo", method = RequestMethod.POST)
    @ResponseBody
    public Object queryBusinessTypeData(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = PsnDimissionFacade.queryDimissionPsnInfo(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }
    
	/**
     * 保存离职申请单
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/saveDimissionBill", method = RequestMethod.POST)
    @ResponseBody
    public Object saveDimissionBill(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = PsnDimissionFacade.saveDimissionBill(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }
    
	/**
     * 提交离职申请单
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/submitDimissionBill", method = RequestMethod.POST)
    @ResponseBody
    public Object submitDimissionBill(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = PsnDimissionFacade.submitDimissionBill(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }
    
	/**
     * 收回离职申请单
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/rollbackDimissionBill", method = RequestMethod.POST)
    @ResponseBody
    public Object rollbackDimissionBill(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = PsnDimissionFacade.rollbackDimissionBill(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }

}
