package nc.impl.saas.salary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.impl.saas.pub.SaasCommonHelper;
import nc.itf.hr.wa.IPaydataQueryService;
import nc.itf.hr.wa.IPayrollManageService;
import nc.itf.hr.wa.IWaClass;
import nc.itf.hrss.pub.profile.IProfileService;
import nc.itf.saas.ISalaryQueryService;
import nc.itf.saas.pub.MessageResult;
import nc.itf.uap.busibean.SysinitAccessor;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.vo.bd.defdoc.DefdocVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.sm.UserVO;
import nc.vo.wa.category.WaClassVO;
import nc.vo.wa.category.WaInludeclassVO;
import nc.vo.wa.classitem.WaClassItemVO;
import nc.vo.wa.item.WaItemVO;
import nc.vo.wa.payroll.PayrollVO;
import nc.vo.wa.payslip.AggPayslipVO;
import nc.vo.wa.payslip.MyPayslipVO;
import nc.vo.wa.payslip.PaySlipItemValueVO;
import nc.vo.wa.payslip.PayslipItemVO;
import nc.vo.wa.payslip.PayslipVO;
import nc.vo.wa.payslip.SendTypeEnum;
import nc.vo.wa.period.PeriodVO;
import nc.vo.wa.pub.HRWACommonConstants;
import nc.vo.wa.pub.ParaConstant;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

//import nc.impl.saas.salary.PayslipDAO;

public class SalaryQueryServiceImpl implements ISalaryQueryService {

	private static final String ITEM = "item";
	// // ������ѯ�ӿ�
	// private static ISysInitQry sysInitQry;

	// ��������
	// private String PARAM_INITCODE = "HRWA012";

	public String successFlag = "Y";// Y �ǳɹ���N�Ǳ���YN��û������
	public String des = null;
	public DefdocVO[] itemCategoryArr;

	private BaseDAO dao;

	private String groupid;

	private PayslipDAO payslipDao;

	/**
	 * ��ȡBaseDao
	 * 
	 * @return
	 */
	public BaseDAO getDao() {
		if (dao == null) {
			dao = new BaseDAO();
		}
		return dao;
	}

	public PayslipDAO getPayslipDao() {
		if (payslipDao == null) {
			payslipDao = new PayslipDAO();
		}
		return payslipDao;
	}

	public void setPayslipDao(PayslipDAO payslipDao) {
		this.payslipDao = payslipDao;
	}

	public String doBusiness(String data) throws Exception {

		JSONObject json = JSONObject.fromObject(data);
		UserVO userVo = SaasCommonHelper.getUserInfo(json.getString("userId"));// PsndocHelper.queryUserVo();
		String pk_psnbasdoc = userVo.getPk_base_doc();
		String secret = json.getString("secret");
		String beginDate = json.getString("beginDate");
		String endDate = json.getString("endDate");
		groupid = userVo.getPk_group();

		InvocationInfoProxy.getInstance().setGroupId(groupid);

		MessageResult result = new MessageResult();
		
		//��֤��Կ
		if (!temporaryDecode(secret, pk_psnbasdoc, result)) {
			return JSONObject.fromObject(result).toString();
		}
		
		//н�����ݲ�ѯ
		try {
			JSONArray resultArr = getSalary4Mobile(groupid, pk_psnbasdoc,
					beginDate, endDate);

			if ("N".equals(this.successFlag)) {
				result.setMessage(this.des);
				result.setStatusCode(MessageResult.STATUS_ERROR);
			} else if ("YN".equals(this.successFlag)) {
				result.setStatusCode(MessageResult.STATUS_SUCCESS);
			}
			// ��ѯ�ɹ��������¸���������Կ
			String newSecret = temporaryEncode(pk_psnbasdoc);
			JSONObject resultData = new JSONObject();
			resultData.put("secret", newSecret);
			resultData.put("salaryData", resultArr);
			result.setData(resultData);
		} catch (Exception e) {
			// TODO: handle exception
			result.setStatusCode(MessageResult.STATUS_ERROR);
			result.setMessage(e.getMessage());
		}
		return JSONObject.fromObject(result).toString();

	}

	public JSONArray getSalary(String groupid, String pk_psndoc,
			String beginDate, String endDate) {

		this.successFlag = "Y";
		// ������Ϣ
		// н����װ�ؼ���
		JSONArray salsryArr = new JSONArray();

		// н����VO����
		List<MyPayslipVO> list = null;
		try {

			beginDate += "-01";
			Integer maxDay = UFLiteralDate.getDaysMonth(
					Integer.parseInt(endDate.split("-")[0]),
					Integer.parseInt(endDate.split("-")[1]));
			endDate += "-" + maxDay;

			list = this.querySelfAggPayslipVOs(beginDate, endDate, pk_psndoc,
					SendTypeEnum.SELF.toIntValue());
			/* �������ݹ��� */
			if (list != null && list.size() > 0) {
				// ��ȡ����������
				DefdocVO[] payslipVOs = this.queryPayslipType();
				JSONArray itemArrBase = new JSONArray();
				for (DefdocVO vo : payslipVOs) {
					JSONObject jo = new JSONObject();
					jo.put("classCode", vo.getPk_defdoc());
					jo.put("className", vo.getName());
					jo.put("value", "");
					jo.put("fileds", new JSONArray());
					itemArrBase.add(jo);
				}
				// �̶���Ŀ
				String[] countItem = new String[] { "othercenddate",
						"wa_dataf_3", "wa_dataf_1", "wa_dataf_2" };
				/* ����0ֵ�Ƿ���ʾ���� */
				Map<String, UFBoolean> map4Org = new HashMap<String, UFBoolean>();
				try {
					for (int i = 0; i < list.size(); i++) {
						if (map4Org.containsKey(list.get(i).getPk_org())) {
							continue;
						}
						map4Org.put(
								list.get(i).getPk_org(),
								SysinitAccessor.getInstance().getParaBoolean(
										list.get(i).getPk_org(),
										ParaConstant.ZERO_SEND));
					}
				} catch (Exception e) {
					Logger.error(e.getMessage(), e);
				}
				// ��ѯ�ɹ�
				des = "�����ɹ���";
				for (int i = 0; i < list.size(); i++) {
					Map<String, Object> salaryInfo = new HashMap<String, Object>();
					String moneyType = list.get(i).getMoneyType();
					String pk_org = list.get(i).getPk_org();
					UFBoolean isShowZero = map4Org.get(pk_org);
					salaryInfo.put("splanname", moneyType);

					String cyear = list.get(i).getCyear();
					String cmonth = list.get(i).getCperiod();
					PaySlipItemValueVO[] paySlipVOs = list.get(i)
							.getPaySlipVOs();

					// н������Ŀװ�ؼ���
					JSONObject itemJson = new JSONObject();
					// Ϊн�ʵ�����
					JSONArray itemArr = JSONArray.fromObject(itemArrBase
							.toString());
					if (paySlipVOs != null && paySlipVOs.length > 0) {

						boolean haveYear = false;
						boolean haveMonth = false;
						boolean defaultType = false;// �Ƿ����Ĭ�� ����������

						for (int j = 0; j < paySlipVOs.length; j++) {

							String code = paySlipVOs[j].getCode();

							if ("wa_datacyear".equals(code)) {
								haveYear = true;
							}
							if ("wa_datacperiod".equals(code)) {
								haveMonth = true;
							}
							// н�ʲ���-0ֵ��ʾ����
							if (isShowZero != null
									&& !isShowZero.booleanValue()
									&& paySlipVOs[j].getDataType().intValue() == 2
									&& paySlipVOs[j].getValue() != null
									&& Double.valueOf(paySlipVOs[j].getValue()
											.toString()) == 0.0D) {
								continue;
							}
							if (paySlipVOs[j].getValue() != null) {
								String pk_paysliptype = paySlipVOs[j]
										.getPk_paysliptype();
								String paysliptypeName = paySlipVOs[j]
										.getPaysliptypeName();
								boolean isfileds_1=true;
								if (StringUtils.isEmpty(pk_paysliptype)) {
									pk_paysliptype = "default";
									paysliptypeName = "Ĭ�Ϸ���";
									if (!defaultType) {
										JSONObject jo = new JSONObject();
										jo.put("classCode", pk_paysliptype);
										jo.put("className", paysliptypeName);
										jo.put("fileds", Arrays.asList(code));
										isfileds_1=false;
										itemArr.add(jo);
										defaultType = true;
									}
								}
									
									for (int k = 0; k < itemArr.size(); k++) {
										JSONObject json = itemArr.getJSONObject(k);
										if (json.getString("classCode").equals(
												pk_paysliptype)) {
											if(isfileds_1){
												json.getJSONArray("fileds").add(code);
											}
										}
									}
								


								Object value = getDoubleString(paySlipVOs[j]);
								if (value != null) {// ���з��������ˣ����value��double
									JSONObject jo = new JSONObject();
									jo.put("title", paySlipVOs[j].getName());
									jo.put("content",value);
									itemJson.put(code, jo);
								}

							}

						}
						if (!haveYear && StringUtils.isNotEmpty(cyear)) {
							JSONObject jo = new JSONObject();
							jo.put("title", "������");
							jo.put("content", cyear);
							itemJson.put("wa_datacyear", jo);
						}
						if (!haveMonth && StringUtils.isNotEmpty(cmonth)) {
							JSONObject jo = new JSONObject();
							jo.put("title", "����ڼ�");
							jo.put("content", cmonth);
							itemJson.put("wa_datacperiod", jo);
						}
						// �жϹ̶����Ƿ�ӽ���,û��д��
						for (String key : countItem) {
							JSONObject jo = new JSONObject();
							if (!itemJson.keySet().contains(key)) {
								jo.put("content", "0.00");
								if ("othercenddate".equals(key)) {
									jo.put("title", "�ڼ�");
									jo.put("content", cyear + "-" + cmonth
											+ "-" + maxDay);
								} else if ("wa_dataf_3".equals(key)) {
									jo.put("title", "ʵ���ϼ�");
								} else if ("wa_dataf_1".equals(key)) {
									jo.put("title", "Ӧ���ϼ�");
								} else if ("wa_dataf_2".equals(key)) {
									jo.put("title", "�ۿ�ϼ�");
								}
								itemJson.put(key, jo);
							}
						}
						// �Ƴ��շ��ࡣ�����һ�����಻�ǿյģ����Ƴ�Ĭ�Ϸ��ࣻ
						for (int ia = itemArr.size() - 1; ia >= 0; ia--) {
							JSONObject item = itemArr.getJSONObject(ia);
							if (item.getJSONArray("fileds").size() == 0) {
								itemArr.remove(ia);
							}
						}
						// ���һ����default���ࡣ���ȥ�պ󳤶ȴ���1���õ����һ��
						if (defaultType && itemArr.size() > 1) {
							itemArr.remove(itemArr.size() - 1);
						}
						salaryInfo.put("salaryList", itemJson);
						salaryInfo.put("salaryItem", itemArr);
						salaryInfo.put("countItem", countItem);
						salsryArr.add(salaryInfo);
					}
				}
			} else {
				this.des = "��ѯ�ڼ���н�ʷ������ݣ�";
				this.successFlag = "YN";
			}
		} catch (BusinessException e) {
			this.des = "��ѯ���̳��ִ���" + e.getMessage();
			this.successFlag = "N";
		}

		return salsryArr;
	}

	/**
	 * ����/�ƶ�Ӧ�� ����н�ʲ�ѯ
	 */
	public List<MyPayslipVO> querySelfAggPayslipVOs(String beginTime,
			String endTime, String psndocID, Integer type)
			throws BusinessException {
		List<MyPayslipVO> selfVOList = new ArrayList<MyPayslipVO>();
		// ����ڼ䣬��ʼʱ��ͽ���ʱ��Ҳ���ж���ڼ�
		PeriodVO[] periodVOs = getPayslipDao().queryPeriodVOs(beginTime,
				endTime);
		if (ArrayUtils.isEmpty(periodVOs)) {
			return null;
		}

		for (int periodIndex = 0; periodIndex < periodVOs.length; periodIndex++) {
			PeriodVO periodVO = periodVOs[periodIndex];
			// ��ô����ڴ��ڼ����й����ķ���
			List<String> waClassPKs = getPayslipDao().queryWaClassPk(periodVO,
					psndocID, beginTime, endTime);
			if (waClassPKs.isEmpty()) {// û���κη����ʹ����й�
				continue;
			}

			Map<String, WaInludeclassVO> vomap = new LinkedHashMap<String, WaInludeclassVO>();

			WaInludeclassVO[] includeClassVos = this.getPayslipDao()
					.queryWaInludeclassVOArrayByChildClassPKS(
							waClassPKs.toArray(new String[0]));
			if (waClassPKs != null && waClassPKs.size() > 0) {
				for (int i = 0; i < waClassPKs.size(); i++) {
					vomap.put(waClassPKs.get(i), null);
					if (!ArrayUtils.isEmpty(includeClassVos)) {
						for (int j = 0; j < includeClassVos.length; j++) {
							if (includeClassVos[j].getPk_childclass().equals(
									waClassPKs.get(i))) {
								vomap.put(waClassPKs.get(i), includeClassVos[j]);
								break;
							}
						}
					}
				}
			}
			Set<Entry<String, WaInludeclassVO>> set = vomap.entrySet();
			Iterator<Entry<String, WaInludeclassVO>> iter = set.iterator();
			while (iter.hasNext()) {
				Entry<String, WaInludeclassVO> entry = iter.next();
				WaInludeclassVO includeClassVO = entry.getValue();
				// �жϷ����Ƿ��η��ŵ��ӷ���������ǣ�����Ҫ��ѯ����Ϊ��ѯ������ʱ�����ӷ���һ���ѯ����������
				if (includeClassVO == null) {
					List<MyPayslipVO> list = queryPayslipInfo(entry.getKey(),
							periodVO.getCyear(), periodVO.getCperiod(),
							psndocID, type);
					if (list != null && !list.isEmpty()) {
						selfVOList.addAll(list);
					}
				}

			}

		}

		return selfVOList;
	}

	/**
	 * ���� ����ĳ��ĳ����ĳ�ڼ��н������ ���� Ա��н�ʵ���ϸ �� ����н�ʲ�ѯ��ʹ�õ�
	 */
	public List<MyPayslipVO> queryPayslipInfo(String waClassID, String cyear,
			String cperiod, String psndocID, Integer type)
			throws BusinessException {
		PayslipVO payslipVO = getPayslipDao().queryPayslipVO(waClassID, cyear,
				cperiod, type);
		if (payslipVO == null) {
			return null;// �����ڶ�Ӧ������
		}

		List<MyPayslipVO> selfVOList = new ArrayList<MyPayslipVO>();
		IWaClass waClass = NCLocator.getInstance().lookup(IWaClass.class);
		IPayrollManageService manageService = NCLocator.getInstance().lookup(
				IPayrollManageService.class);

		// ����id�ҵ�н�ʷ���
		WaClassVO classVO = waClass.queryWaClassByPK(waClassID);
		if (!isPayed(waClassID, cyear, cperiod)) {
			return null;// ����δ����
		}

		List<String> waClassPKs = new ArrayList<String>();
		WaInludeclassVO[] includeClassVO = null;

		/**
		 * ȷ�����ж��ٸ�pk_wa_class
		 */
		// //�Ƿ��η��ţ����ǣ���ÿ�η��Ŷ���Ҫ��ѯ
		// if(classVO.getMutipleflag().booleanValue()){
		// ��η�н ֻ����ӷ���(����Ӹ�����)
		includeClassVO = waClass.queryIncludeClasses(waClassID, cyear, cperiod);
		List<WaInludeclassVO> list = new ArrayList<WaInludeclassVO>();
		if (!ArrayUtils.isEmpty(includeClassVO)) {
			PayrollVO rollvo = new PayrollVO();
			rollvo.setCyear(cyear);
			rollvo.setCperiod(cperiod);

			for (int i = 0; i < includeClassVO.length; i++) {
				rollvo.setPk_wa_class(includeClassVO[i].getPk_childclass());
				if (manageService.isPayed(rollvo)) { // �ж��ӷ����Ƿ񷢷�
					waClassPKs.add(includeClassVO[i].getPk_childclass());
					list.add(includeClassVO[i]);
				}
			}
		} else {
			// ���Ƕ�η�н,ֱ����Ӹ�����
			waClassPKs.add(waClassID);
		}

		// Ϊ����������ʾ����Ҫ��װ������
		String orgName = getPayslipDao().queryOrgName(payslipVO.getPk_org());
		String currName = getPayslipDao().queryCurrName(classVO.getCurrid());

		// 20150717 shenliangc ���в��������Ŀ������Ϣ begin
		HashMap<String, WaClassItemVO> itemDecimalMap = new HashMap<String, WaClassItemVO>();

		// itemDecimalMap = this.getPayslipDao().queryAllItemDecimal(waClassPKs,
		// payslipVO.getPk_org(), cyear, cperiod);

		itemDecimalMap = this.getPayslipDao().queryAllItemInfo(waClassPKs,
				payslipVO.getPk_org(), cyear, cperiod);

		for (int waClassPKIndex = 0; waClassPKIndex < waClassPKs.size(); waClassPKIndex++) {
			// ����н��������Ŀ
			PayslipItemVO[] payslipItemVOs = getPayslipDao()
					.queryPayslipItemVO(payslipVO.getPk_payslip());
			AggPayslipVO aggVO = new AggPayslipVO();
			aggVO.setParentVO(payslipVO);
			aggVO.setTableVO(ITEM, payslipItemVOs);

			// ��ѯн��������
			String whereCondition = " and wa_data.pk_psndoc='" + psndocID
					+ "' and wa_data.pk_wa_class ='"
					+ waClassPKs.get(waClassPKIndex)
					+ "' and wa_data.cyear = '" + cyear
					+ "' and wa_data.cperiod = '" + cperiod + "'";
			ArrayList<HashMap<String, Object>> results = getPayslipDao()
					.queryPayslipData(aggVO, whereCondition);

			// Ϊ����չʾ���㣬��װ����
			if (!results.isEmpty() && results.get(0) != null) {
				Map<String, Object> map = results.get(0);
				PaySlipItemValueVO[] paySlipVOs = new PaySlipItemValueVO[payslipItemVOs.length];
				for (int i = 0; i < payslipItemVOs.length; i++) {
					PaySlipItemValueVO psivVO = new PaySlipItemValueVO();
					psivVO.setName(payslipItemVOs[i].getItem_displayname());
					Object ovalue = map.get(payslipItemVOs[i].getItem_table()
							+ payslipItemVOs[i].getSlip_item());
					// С��λ����
					// 20150717 shenliangc ��֯��������Ŀ�������ݴ�map��ȡ begin
					WaClassItemVO waClassItemVO = itemDecimalMap.get(payslipVO
							.getPk_org()
							+ waClassPKs.get(waClassPKIndex)
							+ payslipItemVOs[i].getSlip_item());
					// 20150717 shenliangc ��֯��������Ŀ�������ݴ�map��ȡ end

					// �����֯�²����ڸù���н����Ŀ�����ڼ����²���
					if (waClassItemVO == null) {
						// waItemVO =
						// itemService.queryByClassItemkeyAndPkorg(payslipVO.getPk_group(),
						// payslipItemVOs[i].getSlip_item());

						// 20150717 shenliangc ���ż�������Ŀ�������ݴ�map��ȡ begin
						waClassItemVO = itemDecimalMap.get(payslipVO
								.getPk_org()
								+ waClassPKs.get(waClassPKIndex)
								+ payslipItemVOs[i].getSlip_item());
						// 20150717 shenliangc ���ż�������Ŀ�������ݴ�map��ȡ end

					}
					if (waClassItemVO != null && ovalue != null
							&& payslipItemVOs[i].getData_type().intValue() == 2) {
						// String strValue = ovalue.toString();
						// if (!StringUtils.isEmpty(strValue)) {
						// ovalue = getFormatUFDouble(new UFDouble(strValue),
						// waClassItemVO.getIflddecimal());
						// }
						//
						// psivVO.setPk_paysliptype(waClassItemVO.getPaysliptype());
						// psivVO.setPaysliptypeName(waClassItemVO.getPaysliptypename());
						psivVO.setDecimal(waClassItemVO.getIflddecimal());
						psivVO.setPk_paysliptype(waClassItemVO.getPaysliptype());
						psivVO.setPaysliptypeName(waClassItemVO
								.getPaysliptypename());
						// ��Ϊ���ݼ��ܣ����Բ�Ҫ�ϱߵľ��ȴ�����
						ovalue = new UFDouble(ovalue.toString());
					}
					psivVO.setValue(ovalue);
					psivVO.setCode(payslipItemVOs[i].getItem_table()
							+ payslipItemVOs[i].getSlip_item());

					if (type.intValue() == 3) {
						psivVO.setIsGroupItem(isGroupItem(waClassItemVO));
						psivVO.setIsCountPro(payslipItemVOs[i].getIsCountPro());
						psivVO.setIsEmpPro(payslipItemVOs[i].getIsEmpPro());
						psivVO.setIsMngPro(payslipItemVOs[i].getIsMngPro());
					}
					psivVO.setDataType(payslipItemVOs[i].getData_type());
					paySlipVOs[i] = psivVO;
				}
				int batch = 0;
				boolean isMultiParentClass = classVO.getMutipleflag()
						.booleanValue();
				// ����Ϊ��η��ŵ��ӷ�����Ҫ��ô���
				// if(includeClassVO != null &&
				// waClassPKIndex<includeClassVO.length){
				// batch = includeClassVO[waClassPKIndex].getBatch();
				// isMultiParentClass = false;
				// }
				if (includeClassVO != null && waClassPKIndex < list.size()) {
					batch = list.get(waClassPKIndex).getBatch();
					isMultiParentClass = false;
				}
				MyPayslipVO myPayslipVO = new MyPayslipVO(
						payslipVO.getPk_org(), orgName, currName, null,
						classVO.getMultilangName(), cyear, cperiod, batch,
						isMultiParentClass);
				myPayslipVO.setPk_wa_data((String) map.get("pk_wa_data"));
				myPayslipVO.setPaySlipVOs(paySlipVOs);
				selfVOList.add(myPayslipVO);
			}
		}
		return selfVOList;
	}

	public static UFDouble getFormatUFDouble(UFDouble d, int digits) {
		UFDouble o = null;
		if (d != null) {
			int power = d.getPower();

			if (power != -digits) {
				o = d.setScale(digits, UFDouble.ROUND_HALF_UP);
			} else {
				o = d;
			}
		}
		return o;
	}

	private UFBoolean isGroupItem(WaItemVO itemVO) {
		if (itemVO != null
		// && (HRWACommonConstants.GROUP_ID.equals(itemVO.getPk_org()) ||
		// PubEnv.getPk_group().equals(itemVO.getPk_org()))) {
				&& (HRWACommonConstants.GROUP_ID.equals(itemVO.getPk_org()) || groupid
						.equals(itemVO.getPk_org()))) {
			return UFBoolean.TRUE;
		}
		return UFBoolean.FALSE;
	}

	/**
	 * �жϷ����Ƿ��ѷ��� ҵ�������ϵĵ���н�ʷ����� �����ڼ�״̬�жϾͿ����ˡ� ���н�ʷ�����ֻҪ��һ���ӷ������ž���Ϊ�������ѷ���
	 * 
	 * @param waClassVO
	 *            //н�ʷ���������Ƕ�η��ţ������Ǹ�����
	 * @param cyear
	 * @param cperiod
	 * @return
	 * @throws BusinessException
	 */
	private boolean isPayed(String classid, String cyear, String cperiod)
			throws BusinessException {
		return NCLocator.getInstance().lookup(IPaydataQueryService.class)
				.isAnyTimesPayed(classid, cyear, cperiod);
	}

	public PeriodVO queryDefaultPeriod(String pk_psndoc, Integer type)
			throws BusinessException {
		String sqlSin = "SELECT MAX(wa_period.cyear) AS cyear ,MAX(wa_period.cperiod) AS cperiod "
				+ "FROM wa_period,wa_periodstate,wa_payslip,wa_data "
				+ "WHERE wa_period.pk_wa_period = wa_periodstate.pk_wa_period "
				+ "	AND wa_period.cyear = wa_payslip.accyear "
				+ "	AND wa_period.cperiod = wa_payslip.accmonth "
				+ "	AND wa_payslip.type = "
				+ type
				+ " "
				+ "	AND wa_periodstate.pk_wa_class = wa_data.pk_wa_class "
				+ "	AND wa_periodstate.payoffflag = 'Y' "
				+ "	AND wa_data.pk_wa_class = wa_payslip.pk_wa_class "
				+ "	AND wa_data.pk_psndoc = '"
				+ pk_psndoc
				+ "'"
				// guoqtԭ�й������ѯ�����ظ���������
				+ "	and wa_data.cyear=wa_period.cyear and wa_data.cperiod=wa_period.cperiod "
				// guoqt��ѯδͣ����н�����ݣ�Ϊ�����ṩ��
				+ "	and wa_data.stopflag='N' ";

		PeriodVO voSin = getPayslipDao().executeQueryVO(sqlSin, PeriodVO.class);
		return voSin;
	}


	public JSONArray getSalary4Mobile(String groupid, String userid,
			String beginDate, String endDate) {
		return getSalary(groupid, userid, beginDate, endDate);
	}

	public Object getDoubleString(PaySlipItemValueVO itemValueVO) {
		Object o = itemValueVO.getValue();
		if (o instanceof UFDouble) {
			double value = ((UFDouble) o).doubleValue();
			UFDouble ud = new UFDouble(value, itemValueVO.getDecimal());
			if (ud.compareTo(new UFDouble(0)) == 0) {
				return null;// ���value��UFDouble�ҵ���0������null��Ϊ����ų���׼��
			} else {
				return ud.toString();
			}

		} else {
			return o;
		}
	}

	/**
	 * ��ѯ����������
	 * 
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	private DefdocVO[] queryPayslipType() throws BusinessException {
		DefdocVO[] vos = null;

		String sql = " select * from bd_defdoc where pk_defdoclist='1001CC10000000001EYU' and nvl(dr,0)=0 order by code ";

		List<DefdocVO> list = (List<DefdocVO>) new BaseDAO().executeQuery(sql,
				new BeanListProcessor(DefdocVO.class));
		// IDefdocQryService docService =
		// NCLocator.getInstance().lookup(IDefdocQryService.class);
		// vos = docService.queryDefdocVOsByDoclistPk("10011310000000006DFE",
		// "GLOBLE00000000000000","~");
		vos = list.toArray(new DefdocVO[0]);
		return vos;
	}

	/**
	 * н�ʵ���ѯ�������
	 */
	@Override
	public String pwdService(String paramJson, int serviceType) {
		// TODO �Զ����ɵķ������
		MessageResult result = new MessageResult();
		result.setData("");
		try {
			JSONObject json = JSONObject.fromObject(paramJson);
			String pk_psndoc = json.getString("pk_psndoc");
			IProfileService iPfofileService = NCLocator.getInstance().lookup(
					IProfileService.class);
			switch (serviceType) {
			case 0:
				String pwd = json.getString("pwd");
				checkPwd(pk_psndoc, pwd, iPfofileService, result);
				break;
			case 1:
				String oldPwd = json.getString("oldPwd");
				String newPwd = json.getString("newPwd");
				updatePwd( oldPwd, newPwd,pk_psndoc, iPfofileService, result);
				break;
			case 2:
				restPwd(pk_psndoc, iPfofileService, result);
				break;
			case 3:
				exist(pk_psndoc, iPfofileService, result);
				break;
			default:
				String pwd1 = json.getString("pwd");
				checkPwd(pk_psndoc, pwd1, iPfofileService, result);
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			result.setMessage(e.getMessage());
			result.setStatusCode(MessageResult.STATUS_ERROR);
		}

		return JSONObject.fromObject(result).toString();
	}

	/**
	 * У������
	 * 
	 * @param pk_psndoc
	 * @param pwd
	 * @param iPfofileService
	 * @param result
	 * @throws BusinessException
	 */
	private void checkPwd(String pk_psndoc, String pwd,
			IProfileService iPfofileService, MessageResult result)
			throws BusinessException {
		// TODO �Զ����ɵķ������
		boolean pass = iPfofileService.checkWaPwd(pk_psndoc, pwd);
		if (!pass) {
			result.setMessage("������������ԣ�");
			result.setStatusCode(MessageResult.STATUS_ERROR);
		} else {
			result.setData(temporaryEncode(pk_psndoc));
		}
	}

	/**
	 * ������ʱ����
	 * 
	 * @param pk_psndoc
	 * @return
	 */
	private String temporaryEncode(String pk_psndoc) {
		// TODO �Զ����ɵķ������
		String secret = System.currentTimeMillis() + pk_psndoc;
		byte[] b = secret.getBytes();
		for (int i = 0; i < b.length; i++) {
			b[i] = (byte) (b[i] + new Byte("6"));
		}
		b = Base64.encodeBase64(b);
		secret = new String(b);
		return secret;
	}

	/**
	 * ��ʱ�������У��
	 * @param secret
	 * @param pk_psndoc
	 * @param result
	 * @return
	 */
	private boolean temporaryDecode(String secret, String pk_psndoc,
			MessageResult result) {
		boolean pass = true;
		result.setData("");
		try {
			if (StringUtils.isEmpty(secret)) {
				result.setMessage("��֤ʧ��");
				pass = false;
			}
			byte[] b = Base64.decodeBase64(secret.getBytes());
			for (int i = 0; i < b.length; i++) {
				b[i] = (byte) (b[i] - new Byte("6"));
			}
			secret = new String(b);
			if (!secret.endsWith(pk_psndoc)) {
				result.setMessage("��֤ʧ��");
				pass = false;
			}
			long lastTime = Long.parseLong(secret.substring(0,secret.length()-pk_psndoc.length()));
			if (lastTime < System.currentTimeMillis() - 1000 * 60 * 20) {// ��ʮ���ӷ����޲�������ʱ
				result.setMessage("��֤�ѳ�ʱ");
				result.setStatusCode(2301);
				pass = false;
			}
		} catch (Exception e) {
			// TODO: handle exception
			result.setMessage("��֤ʧ��");
			pass = false;
		}

		return pass;
	}

	/**
	 * �ж��Ƿ��״ε�¼
	 * 
	 * @param pk_psndoc
	 * @return
	 * @throws BusinessException
	 */
	private void exist(String pk_psndoc, IProfileService iPfofileService,
			MessageResult result) throws BusinessException {
		// TODO �Զ����ɵķ������
		boolean exist = iPfofileService.existWaPwd(pk_psndoc);
		if (!exist) {
			result.setMessage("����ʹ�ã�����������");
			result.setStatusCode(MessageResult.STATUS_ERROR);
		}
	}

	/**
	 * ��������
	 * 
	 * @param paramJson
	 * @return
	 * @throws BusinessException
	 */
	private void restPwd(String pk_psndoc, IProfileService iPfofileService,
			MessageResult result) throws BusinessException {
		// TODO �Զ����ɵķ������
		List<Map<String, String>> resMap = iPfofileService.resetSalaryPswd(
				groupid, InvocationInfoProxy.getInstance().getUserId());
		if ("1".equals(resMap.get(0).get("flag"))) {
			result.setStatusCode(MessageResult.STATUS_ERROR);
			result.setMessage(resMap.get(0).get("des"));
		}
	}

	/**
	 * �޸�����
	 * 
	 * @param paramJson
	 * @return
	 * @throws BusinessException
	 */
	private void updatePwd(String oldPwd, String newPwd, String pk_psndoc,
			IProfileService iPfofileService, MessageResult result)
			throws BusinessException {
		// TODO �Զ����ɵķ������
		List<Map<String, String>> resMap = iPfofileService.modifySalaryPswd(
				groupid, InvocationInfoProxy.getInstance().getUserId(), oldPwd,
				newPwd);
		if ("1".equals(resMap.get(0).get("flag"))) {
			result.setStatusCode(MessageResult.STATUS_ERROR);
			result.setMessage(resMap.get(0).get("des"));
		}
	}

}
