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
     * ��ȡ�������Ͳ���
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
     * ���ݵ���ҵ���������»���ģ��
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
     * �ֶ����� ְ��͸�λ���ֶ�����
     *   1. �䶯��λʱ ��λ���б���ͬʱ�䶯
     *   2. �䶯ְ��ʱ��ְ�����Ҳ��Ҫͬʱ�䶯
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
	 * ��ȡ����ʱ����Ա��cuserid
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
     * controller�л�ȡ������Ϣ�Ļ��ܷ���
     * @param errormsg
     * @return
     */
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

    /**
     * ��ȡ��������
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
     * ���䱣��/���·���
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
     * ��֯����
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
     * ���������Ϣ��ѯ
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
     * ���沢�ύǩ��������
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
     * �ջ�ǩ��������
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
