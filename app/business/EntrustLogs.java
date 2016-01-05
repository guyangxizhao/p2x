package business;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import models.t_entrust_logs;
import models.t_entrust_logs_callback;
import models.v_entrust_logs;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import play.Logger;
import play.db.jpa.JPA;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.PageBean;

import com.shove.Convert;

import constants.SupervisorEvent;

/**
 * 资金托管操作日志业务实体
 *
 * @author hys
 * @createDate  2015年3月6日 下午4:52:47
 *
 */
public class EntrustLogs {
	
	public long id;
	private long _id;
	public long userId;  
	public String userName;  
	public long merBillNo; 
	public String type;  
	public Date requestTime;  
	public String requestDetail;  
	public Date callbackTime;  
	public String callbackDetail;  

	public long getId() {
		return this._id;
	}

	public void setId(long id) {
		this._id = id;
	}

	/**
	 * 保存资金托管请求参数信息
	 * @return
	 */
	private long createReqDetail(ErrorInfo error){
		error.clear();
		
		t_entrust_logs log = new t_entrust_logs();
		
		log.time = new Date();
		log.user_id = this.userId;
		log.user_name = this.userName;
		log.mer_bill_no = this.merBillNo;
		log.type = this.type;
		log.request_detail = this.requestDetail;
		
		try{
			log.save();
		}catch(Exception e){
			Logger.error("保存资金托管请求参数信息时：" + e.getMessage());
			error.code = -1;
			error.msg = "保存资金托管请求参数信息失败";

			return -1;
		}
		
		this._id = log.id;
		error.code = 1;
		
		return log.id;
	}
	
	/**
	 * 保存资金托管回调（响应）参数信息
	 * @return
	 */
	private long createResDetail(ErrorInfo error){
		error.clear();
		
		t_entrust_logs_callback log = new t_entrust_logs_callback();
		
		log.time = new Date();
		log.mer_bill_no = this.merBillNo;
		log.callback_detail = this.callbackDetail;
		
		try{
			log.save();
		}catch(Exception e){
			Logger.error("保存资金托管回调（响应）参数信息时：" + e.getMessage());
			error.code = -1;
			error.msg = "保存资金托管回调（响应）参数信息失败";
			
			return -1;
		}
		
		this._id = log.id;
		error.code = 1;
		
		return log.id;
	}
	
	/**
	 * 添加回调参数
	 * @param error
	 * @param merBillNo
	 * @param callbackDetail
	 */
	public static void addCallbackDetail(ErrorInfo error, long merBillNo, String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign, JSONObject jsonPara){
		error.clear();
		
		JSONObject jsonData = new JSONObject();
		
		jsonData.put("pMerCode", pMerCode);
		jsonData.put("pErrCode", pErrCode);
		jsonData.put("pErrMsg", pErrMsg);
		jsonData.put("p3DesXmlPara", p3DesXmlPara);
		jsonData.put("p3DesJsonPara", jsonPara);
		jsonData.put("pSign", pSign);
		
		EntrustLogs entrustLogs = new EntrustLogs();
		entrustLogs.merBillNo = merBillNo;
		entrustLogs.callbackDetail = jsonData.toString();
		
		entrustLogs.createResDetail(error);
	}
	
	
	/**
	 * 添加资金托管请求参数
	 * @param user
	 * @param merBillNo
	 * @param type
	 * @param map Map<String, String>
	 * @param jsonObjExtra 
	 * @param jsonObj 
	 * @param jsonObjExtra 
	 * @param jsonObj2 
	 */
	public static void addEntrustLogs(ErrorInfo error, long memberId, String memberName, long merBillNo, String type, Map<String, String> map, JSONObject jsonObj, JSONObject jsonObjExtra){
		
		JSONObject jsonData = new JSONObject();
		
		for(Entry<String, String> entry : map.entrySet()){
			
			jsonData.put(entry.getKey(), entry.getValue());
			
			if("arg3DesXmlPara".equals(entry.getKey())){
				jsonData.put("arg3DesJsonPara", jsonObj==null?"":jsonObj);
			}
			
			if("argeXtraPara".equals(entry.getKey())){
				jsonData.put("argeXtraJsonPara", jsonObjExtra==null?"":jsonObjExtra);
			}
		}
		
		EntrustLogs entrustLogs = new EntrustLogs();
		entrustLogs.userId = memberId;
		entrustLogs.userName = memberName;
		entrustLogs.merBillNo = merBillNo;
		entrustLogs.type = type;
		entrustLogs.requestDetail = jsonData.toString();
		
		entrustLogs.createReqDetail(error);
	}
	
	/**
	 * 添加资金托管请求参数2
	 * @param user
	 * @param merBillNo
	 * @param type
	 * @param map Map<String, Object>
	 * @param jsonObjExtra 
	 * @param jsonObj 
	 * @param jsonObjExtra 
	 * @param jsonObj2 
	 */
	public static void addEntrustLogs2(ErrorInfo error, long memberId, String memberName, long merBillNo, String type, Map<String, Object> map, JSONObject jsonObj, JSONObject jsonObjExtra){
		
		JSONObject jsonData = new JSONObject();
		
		for(Entry<String, Object> entry : map.entrySet()){
			
			jsonData.put(entry.getKey(), entry.getValue().toString());
			
			if("arg3DesXmlPara".equals(entry.getKey())){
				jsonData.put("arg3DesJsonPara", jsonObj==null?"":jsonObj);
			}
			
			if("argeXtraPara".equals(entry.getKey())){
				jsonData.put("argeXtraJsonPara", jsonObjExtra==null?"":jsonObjExtra);
			}
			
		}
		
		EntrustLogs entrustLogs = new EntrustLogs();
		entrustLogs.userId = memberId;
		entrustLogs.userName = memberName;
		entrustLogs.merBillNo = merBillNo;
		entrustLogs.type = type;
		entrustLogs.requestDetail = jsonData.toString();
		
		entrustLogs.createReqDetail(error);
	}
	
	/**
	 * 查询资金托管操作日志
	 * @param currPage
	 * @param pageSize
	 * @param keywordType
	 * @param keyword
	 * @param startTime
	 * @param endTime
	 * @param error
	 * @return
	 */
	public static PageBean<v_entrust_logs> queryEntrustLogs(int currPage, int pageSize,
			int keywordType, String keyword, String beginTime, String endTime, ErrorInfo error) {
		error.clear();
				
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}
		
		if (keywordType < 0 || keywordType > 3) {
			keywordType = 0;
		}
		
		StringBuffer where = new StringBuffer("1 = 1");
		
		List<Object> params = new ArrayList<Object>();
		
		//在对关键字搜索
		if (StringUtils.isNotBlank(keyword)) {
			switch (keywordType) {
			case 0:
				where.append(" and (user_id = ? or user_name like ? or mer_bill_no = ? or type like ?) ");
				params.add(Convert.strToLong(keyword, 0));
				params.add("%" + keyword + "%");
				params.add(Convert.strToLong(keyword, 0));
				params.add("%" + keyword + "%");
				break;
			case 1:
				where.append(" and user_id = ? ");
				params.add(Convert.strToLong(keyword, 0));
				break;
			case 2:
				where.append(" and user_name like ?");
				params.add("%" + keyword + "%");
				break;
			case 3:
				where.append(" and mer_bill_no = ? ");
				params.add(Convert.strToLong(keyword, 0));
				break;
			case 4:
				where.append(" and type like ?");
				params.add("%" + keyword + "%");
				break;
			default:
				break;
			}
		}
		
		
		if(beginTime != null&&!"".equals(beginTime)) {
			
			where.append(" and time > ? ");
			params.add(DateUtil.strDateToStartDate(beginTime));
		}
		
		if(endTime != null&&!"".equals(endTime)) {
			where.append(" and time < ? ");
			params.add(DateUtil.strDateToEndDate(endTime));
		}
		
		int count = 0;
		List<v_entrust_logs> page = null;
		
		try {
			page = v_entrust_logs.find(where.toString(), params.toArray()).fetch(currPage, pageSize);
            count = (int)t_entrust_logs.count(where.toString(), params.toArray()); 
		} catch (Exception e) {
			Logger.error("查询资金托管操作日志时，%s", e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}

		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("keywordType", keywordType);

		if (StringUtils.isNotBlank(keyword)) {
			map.put("keyword", keyword);
		}
		
		if(beginTime != null) {
			map.put("beginTime", beginTime);
		}
		
		if(endTime != null) {
			map.put("endTime", endTime);
		}
		
		//统计天数
		Date minDate = null;
		String sql = "select min(t_entrust_logs.time) from t_entrust_logs";
		minDate = (Date) JPA.em().createNativeQuery(sql).getSingleResult();
		
		map.put("days", (minDate==null)? 0 : DateUtil.diffDays2(minDate, new Date()));
		
		PageBean<v_entrust_logs> bean = new PageBean<v_entrust_logs>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.totalCount = count;
		bean.page = page;
		bean.conditions = map;
		
		error.code = 0;

		return bean;
	}
	
	/**
	 * 删除资金托管操作日志
	 * @param type 0 全部、 1 一周前、 2 一月前 
	 * @param error
	 */
	public static int deleteLogs(int type, ErrorInfo error) {
		error.clear();

		if (type < 0 || type > 2) {
			error.code = -1;
			error.msg = "删除操作日志（资金托管）,参数有误";
			
			return error.code;
		}
		
		Date date = null;
		String description = null;
		
		if (1 == type) {
			date = DateUtils.addWeeks(new Date(), -1);
			description = "删除一周前操作日志（资金托管）";
		} else if (2 == type) {
			date = DateUtils.addMonths(new Date(), -1);
			description = "删除一个月前操作日志（资金托管）";
		} else {
			description = "删除全部操作日志（资金托管）";
		}
		
		try {
			if (0 == type) {
				t_entrust_logs.deleteAll();
				t_entrust_logs_callback.deleteAll();
			} else {
				t_entrust_logs.delete("time < ?", date);
				t_entrust_logs_callback.delete("time < ?", date);
			}
		} catch (Exception e) {
			Logger.error("删除资金托管操作日志时，%s", e.getMessage());

			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.DELETE_EVENT, description, error);
		
		if (error.code < 0) {
			return error.code;
		}
		
		error.code = 0;
		error.msg = "删除操作日志成功（资金托管）";
		
		return error.code;
	}
}
