package nc.itf.saas;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import nc.vo.pub.BusinessException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public interface IPsnAttestService {

	/**
	 * 获取证明类型
	 * @return
	 * @throws BusinessException 
	 */
	public JSONArray getAttestType(String pk_defdoclist) throws BusinessException;
	
	/**
	 * 获取证明处理人
	 * @param paramJson
	 * @return
	 * @throws BusinessException 
	 */
	public JSONArray getApplyer(String paramJson) throws BusinessException ;
	
	/**
	 * 获取证明处理人
	 * 带搜索，返回分页
	 * @param paramJson
	 * @return
	 * @throws BusinessException
	 */
	public JSONObject searchApplyer(String paramJson) throws BusinessException ;
	
	/**
	 * 提交开证明
	 * @param attestJson
	 * @return
	 * @throws BusinessException 
	 */
	public String commit(String attestJson) throws BusinessException ;
	
	/**
	 * 证明预览
	 * @param id
	 * @return
	 * @throws BusinessException 
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	 */
	public String getPreview(String id) throws BusinessException, FileNotFoundException, UnsupportedEncodingException, IOException ;

	/**
	 * 获取证明处理人
	 * @param paramJson
	 * @return
	 * @throws BusinessException 
	 */
	public JSONArray queryProject(String paramJson) throws BusinessException ;

	/**
	 * 获取证明处理人
	 * 带搜索，返回分页
	 * @param paramJson
	 * @return
	 * @throws BusinessException
	 */
	public JSONObject searchProject(String paramJson) throws BusinessException ;

}
