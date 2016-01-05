package business;

import utils.DigestUtil;
import net.sf.json.JSONObject;

/**
 * yeepay 易宝支付 返回结果
 * 
 * @author houyaoshan
 *
 */
public class RefundResult {
	private String r0_Cmd; // 请求命令
	private String r1_Code; // 请求结果
	private String r2_TrxId; // 交易流水号
	private String r3_Amt; // 交易金额
	private String r4_Cur; // 交易币种
	private String hmac; // 签名校验

	public RefundResult(String json) {
		JSONObject  tmp=JSONObject.fromObject(json);
		if(tmp.containsKey("r0_Cmd")){
			this.r0_Cmd=tmp.getString("r0_Cmd");	
		}else{
			this.r0_Cmd="";
		}
		
		if(tmp.containsKey("r1_Code")){
			this.r1_Code=tmp.getString("r1_Code");
		}else{
			this.r1_Code="";
		}

		if(tmp.containsKey("r2_TrxId")){
			this.r2_TrxId=tmp.getString("r2_TrxId");
		}else{
			this.r2_TrxId="";
		}
		
		if(tmp.containsKey("r3_Amt")){
			this.r3_Amt=tmp.getString("r3_Amt");
		}else{
			this.r3_Amt="";
		}
		
		if(tmp.containsKey("r4_Cur")){
			this.r4_Cur=tmp.getString("r4_Cur");
		}else{
			this.r4_Cur="";
		}
		
		if(tmp.containsKey("hmac")){
			this.hmac=tmp.getString("hmac");
		}else{
			this.hmac="";
		}
	}
	
	/**
	 *  根据对象判断 是否被修改过
	 * @param result
	 * @return
	 */
//	public boolean  isReal(RefundResult result){
//		StringBuffer sValue = new StringBuffer();
//		sValue.append(result.r0_Cmd);
//		sValue.append(result.r1_Code);
//		sValue.append(result.r2_TrxId);
//		sValue.append(result.r3_Amt);
//		sValue.append(result.r4_Cur);
//	String  hmacStr= DigestUtil.hmacSign(sValue.toString(), keyValue);
//	}
}
