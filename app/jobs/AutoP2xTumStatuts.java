package jobs;

import business.Bid;
import play.jobs.Every;
import play.jobs.Job;

/**
 * p2x向井通申请融资通后 ，
 * 关于井通审核状态的查询
 * @author houyaoshan
 *
 */
@Every("5min")
public class AutoP2xTumStatuts extends Job {
	 @Override
	public void doJob() throws Exception {
			Bid.getP2xTumStatus();//自动查询井通审核状态
	}
}
