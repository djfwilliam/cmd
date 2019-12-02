package nc.itf.saas;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import nc.vo.pub.BusinessException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public interface IPsnAttestService {

	/**
	 * ��ȡ֤������
	 * @return
	 * @throws BusinessException 
	 */
	public JSONArray getAttestType(String pk_defdoclist) throws BusinessException;
	
	/**
	 * ��ȡ֤��������
	 * @param paramJson
	 * @return
	 * @throws BusinessException 
	 */
	public JSONArray getApplyer(String paramJson) throws BusinessException ;
	
	/**
	 * ��ȡ֤��������
	 * �����������ط�ҳ
	 * @param paramJson
	 * @return
	 * @throws BusinessException
	 */
	public JSONObject searchApplyer(String paramJson) throws BusinessException ;
	
	/**
	 * �ύ��֤��
	 * @param attestJson
	 * @return
	 * @throws BusinessException 
	 */
	public String commit(String attestJson) throws BusinessException ;
	
	/**
	 * ֤��Ԥ��
	 * @param id
	 * @return
	 * @throws BusinessException 
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	 */
	public String getPreview(String id) throws BusinessException, FileNotFoundException, UnsupportedEncodingException, IOException ;

	/**
	 * ��ȡ֤��������
	 * @param paramJson
	 * @return
	 * @throws BusinessException 
	 */
	public JSONArray queryProject(String paramJson) throws BusinessException ;

	/**
	 * ��ȡ֤��������
	 * �����������ط�ҳ
	 * @param paramJson
	 * @return
	 * @throws BusinessException
	 */
	public JSONObject searchProject(String paramJson) throws BusinessException ;

}
