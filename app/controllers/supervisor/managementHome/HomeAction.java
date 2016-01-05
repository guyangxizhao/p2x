package controllers.supervisor.managementHome;

import java.util.Calendar;

import org.apache.commons.lang.StringUtils;

import jingtum.Jingtum;
import p2x.jingtum.https.APICallClient;
import net.sf.json.JSONObject;
import reports.StatisticBorrow;
import reports.StatisticDebt;
import reports.StatisticInvest;
import business.Bid;
import business.Invest;
import business.News;
import business.User;
import constants.Constants;
import controllers.supervisor.SupervisorController;
import utils.Arith;
import utils.ErrorInfo;

/**
 * 管理首页
 * @author zhs
 *
 */
public class HomeAction  extends SupervisorController {
	/**
	 * 管理首页
	 */
	public static void showHome() {
		ErrorInfo error = new ErrorInfo();
		
		Calendar instance = Calendar.getInstance();
		int year = instance.get(Calendar.YEAR);
		int month = instance.get(Calendar.MONTH) + 1;
		
		long onlineUserNum = User.queryOnlineUserNum();
		long todayRegisterUserCount = User.queryTodayRegisterUserCount(error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		long totalRegisterUserCount = User.queryTotalRegisterUserCount(error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		long todayBidCount = Bid.queryTodayBidCount(error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		long totalBidCount = Bid.queryTotalBidCount(error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		double totalBidDealAmount = StatisticBorrow.queryTotalBorrowAmount(year, month, error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		long totalInvestCount = Invest.queryTotalInvestCount(error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		double totalInvestDealAmount = StatisticInvest.queryTotalInvestAmount(year, month, error) + StatisticDebt.queryTotalDebtAmount(year, month, error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		long totalNewsCount = News.queryTotalNewsCount(error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		String result=null;
		if(params.get("ids")!=null){
			String ids=params.get("ids");
			 result= auditJingtong(ids).replace("\"", "");
		}
		render(result,onlineUserNum, todayRegisterUserCount, totalRegisterUserCount, todayBidCount, totalBidCount,totalBidDealAmount, totalInvestCount, totalInvestDealAmount, totalNewsCount);
	}
	public static String auditJingtong(String ids){
		
		String[] allIds=ids.contains(",")?ids.split(","):new String[]{ids};
		
		int m=0;
		String id=null;
		JSONObject json1 =null;
		JSONObject json2 =null;
		String result="";
		APICallClient apiClient =new APICallClient(); 
		
		for (int i = 0; i < allIds.length; i++) {
			
			m=Integer.valueOf(allIds[i].split(":")[1]);
			id=allIds[i].split(":")[0];
			json1=apiClient.apiCall("set_trust",
					"jp64GVM12GPjh9S1Df3yE3114i4FHDjso2",
					"snfddURKmMmiUszH2au7dyM4qiR6M",
					"9999999",
					id,
					"jBciDE8Q3uJjf111VeiUNM775AMKHEbBLS");
			
			json2 = apiClient.apiCall("payment",
					"jBciDE8Q3uJjf111VeiUNM775AMKHEbBLS",
					"sniENPnL4dJjaQv5xVJ9kGAZbin1p",
					m+1+".00",
					id,
					id,
					"jp64GVM12GPjh9S1Df3yE3114i4FHDjso2",
					m+".00",
					"",
					"");
			result+=json1.toString()+json2.toString()+"<br>";
		}
		return result;
	}
}
