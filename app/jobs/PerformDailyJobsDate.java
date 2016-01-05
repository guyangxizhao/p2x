package jobs;

import play.Logger;
import play.jobs.Job;
import play.jobs.On;
import business.StatHomePage;


/**
 * 统计平台注册人数、投资金额等数据
 */
@On("0 0 4 * * ?")
public class PerformDailyJobsDate extends Job<PerformDailyJobsDate> {
	public void doJob() {
		Logger.info("--------------[start]统计首页指标---------------------");
		StatHomePage.doStat();
		Logger.info("--------------[end]统计首页指标---------------------");
	}
}
