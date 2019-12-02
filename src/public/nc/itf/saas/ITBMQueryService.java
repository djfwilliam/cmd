package nc.itf.saas;

import java.util.Map;

import nc.vo.pub.BusinessException;

/**
 * ��Ա��Ϣ
 * @author nijb@yonyou.com
 * 
 */
public interface ITBMQueryService {
	
	//��ѯ�ҵ�����
	public String getMyApplication(Map<String,Object> param) throws BusinessException;//queryBills
	//��ѯ�ҵ�����
	public String getMyApprove(Map<String,Object> param) throws BusinessException;
	//��ȡ�������
	public String queryLeaveType(Map<String,Object> param) throws BusinessException;
	//��ѯ��ٵ�
	public String queryLeaveByPk(Map<String,Object> param) throws BusinessException;
	//������ٵ�
	public String saveLeave(Map<String,Object> param) throws BusinessException;
	//�ύ��
	public String submitLeave(Map<String,Object> param) throws BusinessException;
	//���沢�ύ��
	public String saveAndSubmitLeave(Map<String,Object> param) throws BusinessException;
	//��ת���������ҳ��
	public String getNewLeave(Map<String,Object> param) throws BusinessException;
	//�������ʱ��
	public String calculateLeaveLength(Map<String,Object> param) throws BusinessException;
	//�����������ʣ��
	public String getLeaveBalance(Map<String, Object> param) throws BusinessException;
	//ɾ��һ���ٵ�
	public String deleteLeave(Map<String, Object> param) throws BusinessException;
	//������������
	public String queryCalendar(Map<String,Object> param) throws BusinessException;
	//��ȡ������ϸ����
	public String queryCalendarDayDetails(Map<String,Object> param) throws BusinessException;
	//��ȡ���̽ڵ��б�
	public String queryProcessNodeList(Map<String,Object> param) throws BusinessException;
	//��ȡ�������б�
	public String queryApproverList(Map<String,Object> param) throws BusinessException;
	
	public String submitBill(Map<String,Object> params) throws BusinessException;
	//��ȡ��������
	public String queryTranstype(Map<String, Object> param) throws BusinessException;
	//��ȡ�������
	public int queryDirectApprove(Map<String, Object> param) throws BusinessException;
	//��������
	public String doApprove(Map<String, Object> param) throws Exception;
	//��������
	public String doBatchApprove(Map<String, Object> param)throws Exception;
	//��ٴ�ӡ��Ϣ��ѯ
	public String leavePrintTemplate(Map<String, Object> param)throws Exception;
	
	//У���Ƿ�Ϊ���� ����� �Ͳ���
	public String checkBillType(Map<String,Object> param )throws Exception;

	public Map<String, Object> checkTimeBrokenLeave(Map<String, Object> param)throws BusinessException;
	
	/**
	 * ��ȡ�û�����
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public String queryCpUser(Map<String, Object> param)throws Exception;
	/**
	 * ��ȡ��Ա����
	 * @param param
	 * @return
	 * @throws Exception
	 * @author wangtian1
	 */
	public String queryPsnodc(Map<String, Object> param)throws Exception;
	/**
	 * ���ͳ�����Ϣ
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public String sendMessage(Map<String, Object> param)throws Exception;
	
	
}