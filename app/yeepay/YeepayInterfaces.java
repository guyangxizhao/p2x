package yeepay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import business.YeepayPayment;

/**
 * 易宝支付  接口
 * @author houyaoshan
 *
 */
public class YeepayInterfaces {

	/**
	 * Object cast to Map
	 */
	public static Map objToMap(YeepayPayment entity){
		Map<String, String> map=new HashMap<String, String>();
		try {
			map.put("nodeAuthorizationURL", entity.nodeAuthorizationURL);
			map.put("keyValue", entity.keyValue);
			map.put("p1_MerId", entity.p1_MerId);
			map.put("p2_Order", entity.p2_Order);
			map.put("p3_Amt", entity.p3_Amt);
			map.put("p5_Pid", entity.p5_Pid);
			map.put("p6_Pcat", entity.p6_Pcat);
			map.put("p7_Pdesc", entity.p7_Pdesc);
			map.put("p8_Url", entity.p8_Url);
			map.put("pa_MP", entity.pa_MP);
			map.put("hmac", entity.getHmac());
			map.put("p0_Cmd", entity.getP0_Cmd());
			map.put("p4_Cur", entity.getP4_Cur());
			map.put("p9_SAF", entity.getP9_SAF());
			map.put("pd_FrpId", entity.getPd_FrpId());
			map.put("pr_NeedResponse", entity.getPr_NeedResponse());
			return   map;
		} catch (Exception e) {
			return null;
		}
	}
	
	
	
}
