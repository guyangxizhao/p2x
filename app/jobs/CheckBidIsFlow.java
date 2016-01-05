package jobs;

import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import business.Bid;
import constants.Constants;

/**
 * 每5分钟检查一次是否流标
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-8-1 上午10:32:52
 */
@Every("5min")
public class CheckBidIsFlow extends Job {

	@Override
	public void doJob() throws Exception {
		if(Constants.IS_FLOW_BID) {
			Logger.info("--------------检查是否流标,开始---------------------");
			Bid.checkBidIsFlow();
			
			//环迅资金托管需要打开
			//Bid.verifyToFail();
			
			Logger.info("--------------检查是否流标,结束---------------------");
		}
	}
}
