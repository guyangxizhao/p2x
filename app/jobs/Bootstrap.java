package jobs;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.libs.WS;
import business.BackstageSet;

import com.shove.Convert;
import com.shove.security.Encrypt;

import constants.Constants;
import constants.IPSConstants;
import constants.IPSConstants.IPSOperation;

@OnApplicationStart
public class Bootstrap extends Job {
 
    public void doJob() {
	     new BackstageSet();
	     
	     BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
	     
	     Play.configuration.setProperty("mail.smtp.host",backstageSet.emailWebsite);
	     Play.configuration.setProperty("mail.smtp.user",backstageSet.mailAccount);
	     Play.configuration.setProperty("mail.smtp.pass",backstageSet.mailPassword);
	     
	     //设置支付网关
	     if(Constants.IPS_ENABLE){
	    	 new Timer().schedule(new InitGateWay(), 5000, 5000);  //5秒后以5秒为周期执行，直到获取到网关信息
	     }
    }
	    

	/**
     * 资金托管模式下，设置支付网关，项目启动5秒后以5秒的周期执行，直到获取到网关信息
     *
     * @author hys
     * @createDate  2015年3月25日 下午4:07:34
     *
     */
	class InitGateWay extends TimerTask {

		@Override
    	public void run() {

			String version = BackstageSet.getCurrentBackstageSet().entrustVersion;
			String type = IPSOperation.QUERY_GATE_WAY+"";
			String domain = IPSConstants.DOMAIN;
			
			String argSign = Encrypt.MD5(version + domain + type + Constants.ENCRYPTION_KEY,Charset.forName("utf-8"));
	
			Map<String, String> map = new HashMap<String, String>();
			map.put("version", version);
			map.put("domain", domain);
			map.put("type", type);
			map.put("argSign", argSign);
			
			String url = IPSConstants.ACTION;
			String response = "";
			try{
				response = WS.url(url).setParameters(map).post().getString();
			}catch(Exception e){
				Logger.error("资金托管模式下，设置支付网关时，%s", e.getMessage());
				
				return;
			}
			
			Logger.info("资金托管模式下，查询domain标识的网关，response = %s", response);
			
			if(StringUtils.isNotEmpty(response)){
				int gateWayId = Convert.strToInt(response, -1);
				IPSConstants.CurrentGateWay = gateWayId;
				Logger.info("资金托管网关设置为：%s", IPSConstants.CurrentGateWay);
				
				//取消定时任务
				this.cancel();
			}
    	}
    }
}