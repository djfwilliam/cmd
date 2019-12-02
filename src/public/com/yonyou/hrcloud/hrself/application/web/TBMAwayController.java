package com.yonyou.hrcloud.hrself.application.web;

import com.yonyou.hrcloud.hrself.common.MessageResult;
import nc.pub.facade.TBMQueryFacade;
import net.sf.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * @author yangwshh@yonyou.com
 * @date 2017/11/11 15:16
 */
@RestController
@RequestMapping(value = "/portal/tbmAway")
public class TBMAwayController {
    /**
     * 出差參照
     * @param pk_org
     * @return
     */
    @RequestMapping(value = "/queryAwayRef", method = RequestMethod.POST)
    @ResponseBody
    public MessageResult queryAwayRef(@RequestBody String pk_org){
        try {
            List ncdate = TBMQueryFacade.queryAwayRef(pk_org);
            return new MessageResult(MessageResult.STATUS_SUCCESS,null,ncdate);
        } catch (Exception e) {
            return new MessageResult(MessageResult.STATUS_ERROR,e.getMessage(),null);
        }
    }


    /**
     * 保存出差单
     * @param param
     * @return
     */
    @RequestMapping(value = "/saveAway", method = RequestMethod.POST)
    @ResponseBody
    public MessageResult saveAway(@RequestBody Map<String, Object> param,HttpServletRequest request){
        try {
            param.put("userId",this.getUserId(request));
            Map ncdate = TBMQueryFacade.saveAway(param);

            return new MessageResult(MessageResult.STATUS_SUCCESS,null,ncdate);
        } catch (Exception e) {
            return new MessageResult(MessageResult.STATUS_ERROR,e.getMessage(),null);
        }
    }


    /**
     * 提交出差审批单
     * @param param
     * @return
     */
    @RequestMapping(value = "/submitAway", method = RequestMethod.POST)
    @ResponseBody
    public MessageResult submitAway(@RequestBody Map<String, Object> param) {
        try {
            Map ncdate = TBMQueryFacade.saveAway(param);
            return new MessageResult(MessageResult.STATUS_SUCCESS,null,ncdate);
        } catch (Exception e) {
            return new MessageResult(MessageResult.STATUS_ERROR,e.getMessage(),null);
        }
    }

    /**
     * 计算出差时长
     * @param param
     * @return
     */
    @RequestMapping(value = "/caculateAway", method = RequestMethod.POST)
    @ResponseBody
    public MessageResult caculateAway(@RequestBody Map<String, Object> param, HttpServletRequest request) {
        param.put("userId",this.getUserId(request));
        try {
            Map<String, Object> ncdate = TBMQueryFacade.calculateAway(param);
            return new MessageResult(MessageResult.STATUS_SUCCESS,null,ncdate);
        } catch (Exception e) {
            return new MessageResult(MessageResult.STATUS_ERROR,e.getMessage(),null);
        }
    }

    /**
     * 保存并提交出差审批单
     * @param param
     * @return
     */
    @RequestMapping(value = "/saveAndSubmitAway", method = RequestMethod.POST)
    @ResponseBody
    public MessageResult saveAndSubmitAway(@RequestBody Map<String, Object> param){
        try {
            String ncdate = TBMQueryFacade.saveAndSubmitAway(param);
            return new MessageResult(MessageResult.STATUS_SUCCESS,null,ncdate);
        } catch (Exception e) {
            return new MessageResult(MessageResult.STATUS_ERROR,e.getMessage(),null);
        }
    }

    /**
     * 收回出差审批单
     * @param param
     * @return
     */
    @RequestMapping(value = "/callbackAway", method = RequestMethod.POST)
    @ResponseBody
    public MessageResult callbackAway(@RequestBody Map<String, Object> param) {
        try {
            String ncdate = TBMQueryFacade.callbackAway(param);
            return new MessageResult(MessageResult.STATUS_SUCCESS,null,ncdate);
        } catch (Exception e) {
            return new MessageResult(MessageResult.STATUS_ERROR,e.getMessage(),null);
        }
    }
    /**
     * 刪除出差审批单
     * @param param
     * @return
     */
    @RequestMapping(value = "/deleteAway", method = RequestMethod.POST)
    @ResponseBody
    public MessageResult deleteAway(@RequestBody Map<String, Object> param){
        try {
            String ncdate = TBMQueryFacade.deleteAway(param);
            return new MessageResult(MessageResult.STATUS_SUCCESS,null,ncdate);
        } catch (Exception e) {
            return new MessageResult(MessageResult.STATUS_ERROR,e.getMessage(),null);
        }
    }

    /**
     *出差审批模板打印信息查询
     * @param param
     * @return
     */
    @RequestMapping(value = "/awayPrintTemplate", method = RequestMethod.POST)
    @ResponseBody
    public MessageResult awayPrintTemplate(@RequestBody Map<String, Object> param){
        try {
            String ncdate = TBMQueryFacade.awayPrintTemplate(param);
            return new MessageResult(MessageResult.STATUS_SUCCESS,null,ncdate);
        } catch (Exception e) {
            return new MessageResult(MessageResult.STATUS_ERROR,e.getMessage(),null);
        }
    }
    private String getUserId(HttpServletRequest request) {
        String userId = null;
        HttpSession session = request.getSession();
        JSONObject ncSession = (JSONObject)session.getAttribute("ncSession");
        userId = ncSession.get("userID").toString();
        return userId;
    }
}
