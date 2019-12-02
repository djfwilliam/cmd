package nc.pub.facade;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.saas.IRefInfoService;
import nc.ref.saas.pub.IReferenceDataModal;
import nc.vo.pub.BusinessException;

public class RefFacade
{
    
    final static String REF_CODE = "refcode";
    final static String KEY = "key";
    final static String VALUE = "value";
    final static String DATA = "data";
    final static String TOTAL = "total";
    //¿ÉÒÔ¿¼ÂÇ»º´æ
    
    public static Map<String,Object> queryData(Map<String,String> paramMap) throws Exception{
        String refCode = paramMap.get(REF_CODE);
//        String refCode = "orgs";
        if(refCode == null){
            return null;
        }
        IReferenceDataModal dataModal = getDataModal(refCode);
        if(dataModal == null ){
            return null;
        }
        String data = dataModal.queryData(paramMap);
        Map<String,Object> dataMap = getDataMap(data,dataModal);
        dataMap.put(TOTAL, dataModal.getTotal(paramMap));
        return dataMap;
    }
    public static String queryRefValue(Map<String,String> paramMap) throws Exception{
        String refCode = paramMap.get(REF_CODE);
        if(refCode == null){
            return null;
        }
        IReferenceDataModal dataModal = getDataModal(refCode);
        if(dataModal == null ){
            return null;
        }
        String data = dataModal.queryRefValue(paramMap);
        return data;
    }


    public static Map<String,Object> querySearchData(Map<String,String> paramMap) throws Exception{
        String refCode = paramMap.get(REF_CODE);
        if(refCode == null){
            return null;
        }
        IReferenceDataModal dataModal = getDataModal(refCode);
        if(dataModal == null ){
            return null;
        }
        String data = dataModal.querySearch(paramMap);
        return getDataMap(data,dataModal);
    } 
    
    private static Map<String, Object> getDataMap(String data,IReferenceDataModal dataModal)
            throws Exception
    {
    	JSONArray json = JSONArray.parseArray(data);
        Map<String,Object> dataMap = new HashMap<String,Object>(json.size());
        String key = dataModal.getUniqueKeyColumnName();
        String name = dataModal.getDisNameColumnName();
        dataMap.put(KEY, key);
        dataMap.put(VALUE, name);
        dataMap.put(DATA, data);
        return dataMap;
    } 
    
    private static IReferenceDataModal getDataModal(String refCode) throws BusinessException{
        IRefInfoService refInfo = NCLocator.getInstance().lookup(IRefInfoService.class);
        String refClass = refInfo.getRefClassByCode(refCode);
        if(refClass == null){
            return null;
        }else{
            try{
                IReferenceDataModal service = (IReferenceDataModal) Class.forName(refClass).newInstance();
                return service;
            }catch(ClassNotFoundException e){
                Logger.error(e.getMessage(),e.getCause());
                return null;
            }
            catch (InstantiationException e)
            {
                Logger.error(e.getMessage(),e.getCause());
                return null;
            }
            catch (IllegalAccessException e)
            {
                Logger.error(e.getMessage(),e.getCause());
                return null;
            }
        }
    }
}
