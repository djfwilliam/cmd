package com.yonyou.hrcloud.hrself.application.web;

import nc.pub.facade.REGMngFacade;
import nc.pub.facade.RefQueryFacade;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.Map;

/**
 * ת������
 * Created by tianxx5@yonyou.com on 2019/01/04.
 */
@RestController
@RequestMapping(value = "/portal/regmng")
public class REGMngController {
    /**
     * ת�������ȡ��ʼ������Ϣ
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/getNewReg", method = RequestMethod.POST)
    @ResponseBody
    public Object getNewReg(@RequestBody Map<String,Object> param, HttpServletRequest request) {
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
        	if(!param.isEmpty()){
        		String pk_hi_regapply = param.get("pk_hi_regapply").toString();
        		if("new".equals(param.get("pk_hi_regapply").toString())){
        			ncMessageResult = REGMngFacade.getNewReg(param);
        		}else{
        			ncMessageResult = REGMngFacade.queryPsnRegByPk(param);
        		}
        		 
        	}
           
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }

    /**
     * ����һ��ת����
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/savePsnReg", method = RequestMethod.POST)
    @ResponseBody
    public Object savePsnReg(@RequestBody Map<String,Object>  param, @SuppressWarnings("restriction") HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = REGMngFacade.savePsnReg(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }

    /**
     * �ύһ��ת����
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/submitPsnReg", method = RequestMethod.POST)
    @ResponseBody
    public Object submitPsnReg(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = REGMngFacade.submitPsnReg(param);
        } catch(Exception e){
            ncMessageResult = getErrorMsg(e.getMessage());
        }
        return ncMessageResult;
    }
  
    /**
     * ��ȡ����
     * @param param
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryRefList", method = RequestMethod.POST)
    @ResponseBody
    public Object queryRefList(@RequestBody Map<String,Object>  param, HttpServletRequest request){
        String userId = getUserId(request);
        param.put("userId",userId);
        String ncMessageResult = null;
        try{
            ncMessageResult = RefQueryFacade.queryRefList(param);
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
            jsonObject.element("message","����ʧ��");
        } else {
            jsonObject.element("message",errormsg);
        }
        jsonObject.element("data",new ArrayList());
        return jsonObject.toString();
    }

}
