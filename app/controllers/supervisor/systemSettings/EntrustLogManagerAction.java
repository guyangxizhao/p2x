package controllers.supervisor.systemSettings;

import models.t_entrust_logs;
import models.v_entrust_logs;
import models.v_supervisor_events;
import business.DealDetail;
import business.EntrustLogs;
import constants.Constants;
import controllers.supervisor.SupervisorController;
import utils.ErrorInfo;
import utils.PageBean;

/**
 * 资金托管日志管理
 *
 * @author hys
 * @createDate  2015年3月7日 上午9:20:02
 *
 */
public class EntrustLogManagerAction extends SupervisorController {
	
	/**
	 * 日志列表
	 */
	public static void logs(int currPage, int pageSize, int keywordType, String keyword, String beginTime, String endTime) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_entrust_logs> page = 
				EntrustLogs.queryEntrustLogs(currPage, pageSize, keywordType, keyword, beginTime, endTime, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
				
		render(page);
	}

	/**
	 * 删除操作日志页面
	 */
	public static void deleteLogsInit(int currPage, int pageSize) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_supervisor_events> page = DealDetail.querySupervisorDeleteEvents(currPage, pageSize, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}
	
	/**
	 * 删除操作日志
	 * @param type
	 */
	public static void deleteLogs(int type) {
		ErrorInfo error = new ErrorInfo();
		EntrustLogs.deleteLogs(type, error);
		
		renderJSON(error);
	}
}
