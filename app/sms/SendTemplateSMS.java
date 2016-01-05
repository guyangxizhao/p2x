package sms;

import java.util.HashMap;

import business.BackstageSet;

import com.cloopen.rest.sdk.CCPRestSDK;


public class SendTemplateSMS {
	public HashMap<String, Object> sendTemplateSMS(String phone, int codeSMS) {
		CCPRestSDK restAPI = new CCPRestSDK();
		BackstageSet options = BackstageSet.getCurrentBackstageSet();
		
		// 初始化服务器地址和端口
		restAPI.init(options.smsServer, options.smsServPort);
		// 初始化主帐号名称和主帐号令牌
		restAPI.setAccount(options.smsAccount, options.smsPassword);
		// 初始化应用ID
		restAPI.setAppId(options.smsAppid);

		HashMap<String, Object> result = restAPI.sendTemplateSMS(phone,
				options.smsTemplateId,
				new String[] {
				codeSMS + "",
					options.smsTimeLimit
				});
		
		return result;
	}
}
