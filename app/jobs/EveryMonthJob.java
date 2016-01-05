package jobs;


import constants.Constants;
import business.Optimization.InvestOZ;
import play.jobs.Job;
import play.jobs.On;
import reports.StatisticDebt;



//每月最后一天23:50执行
@On("0 50 23 L * ?")
public class EveryMonthJob extends Job{
	
	public void doJob() {
		
		InvestOZ.add(); // 我的账户->理财情况统计表
	}
}
