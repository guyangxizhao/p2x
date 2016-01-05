package business;

import java.io.Serializable;

import net.sf.json.JSONObject;
import utils.DigestUtil;


/**
 *  易宝支付  业务类
 * @author houyaoshan
 *
 */
public class YeepayPayment implements Serializable {
	private static final long serialVersionUID = -6882978811212218394L;
	public String keyValue;   			// 商家密钥
	public String nodeAuthorizationURL;	// 交易请求地址
	private String p0_Cmd; 		     	// 在线支付请求，固定值 ”Buy”
	public String p1_MerId;				// 商户编号
	public String p2_Order;				// 商户订单号
	public String p3_Amt;           	// 支付金额
	private String p4_Cur;				// 交易币种  人民币
	public String p5_Pid; 				// 商品名称
	public String p6_Pcat; 				// 商品种类
	public String p7_Pdesc;				// 商品描述
	public String p8_Url;				// 商户接收支付成功数据的地址
	private String p9_SAF;				// 需要填写送货信息 0：不需要  1:需要  【本平台暂时没有实物，无需送货地址】
	public String pa_MP;				// 商户扩展信息
	private String pd_FrpId;    		// 支付通道编码
	private String pr_NeedResponse;		// 默认为"1"，需要应答机制
    private String hmac;					// 交易签名串
    

	
	
	
	public String getHmac(){
		return this.hmac;
	}
	
	public String getP0_Cmd(){
		return this.p0_Cmd="Buy";
	}
	
	public String getP4_Cur(){
		return this.p4_Cur="CNY";
	}
	
	public String getP9_SAF(){
		return this.p9_SAF="0";
	}
	
	public void setPd_FrpId(String pd_FrpId){
		this.pd_FrpId=pd_FrpId.toUpperCase();
	}
	
	public String getPd_FrpId(){
		return this.pd_FrpId.toUpperCase();
	}
	public String getPr_NeedResponse(){
		return this.pr_NeedResponse="1";
	}
	
	public YeepayPayment() {
		super();
	}

	/**
	 * 充值接口  对象的基本拼装
	 * @param keyValue
	 * @param nodeAuthorizationURL
	 * @param p1_MerId
	 * @param p2_Order
	 * @param p3_Amt
	 * @param p5_Pid
	 * @param p6_Pcat
	 * @param p7_Pdesc
	 * @param p8_Url
	 * @param pa_MP
	 * @param pd_FrpId
	 */
	public YeepayPayment(String keyValue, String nodeAuthorizationURL,
			String p1_MerId, String p2_Order, String p3_Amt,
			String p5_Pid, String p6_Pcat, String p7_Pdesc,
			String p8_Url, String pa_MP, String pd_FrpId
			) {
		this.keyValue = keyValue;
		this.nodeAuthorizationURL = nodeAuthorizationURL;
		this.p3_Amt = p3_Amt;
		this.p0_Cmd = "Buy";
		this.p1_MerId = p1_MerId;
		this.p2_Order = p2_Order;
		this.p4_Cur = "CNY";
		this.p5_Pid = p5_Pid;
		this.p6_Pcat = p6_Pcat;
		this.p7_Pdesc = p7_Pdesc;
		this.p8_Url = p8_Url;
		this.p9_SAF = "0";
		this.pa_MP = pa_MP;
		this.pd_FrpId = pd_FrpId;
		this.pr_NeedResponse = "1";
		/**
		 * 创建对象时，自动生成对应的hamc md5 值
		 */
		StringBuffer sValue = new StringBuffer();
			sValue.append(this.p0_Cmd);
			sValue.append(p1_MerId);
			sValue.append(p2_Order);
			sValue.append(p3_Amt);
			sValue.append(this.p4_Cur);
			sValue.append(p5_Pid);
			sValue.append(p6_Pcat);
			sValue.append(p7_Pdesc);
			sValue.append(p8_Url);
			sValue.append(this.p9_SAF);
			sValue.append(pa_MP);
			sValue.append(pd_FrpId);
			sValue.append(pr_NeedResponse);
		this.hmac = DigestUtil.hmacSign(sValue.toString(), keyValue);
	}
	
	
	
	
	
	/**
	 *充值 返回  回调函数
	 * @param hmac
	 * @param p1_MerId
	 * @param r0_Cmd
	 * @param r1_Code
	 * @param r2_TrxId
	 * @param r3_Amt
	 * @param r4_Cur
	 * @param r5_Pid
	 * @param r6_Order
	 * @param r7_Uid
	 * @param r8_MP
	 * @param r9_BType
	 * @param keyValue
	 * @return
	 */
	public static boolean verifyCallback(String hmac, String p1_MerId,
			String r0_Cmd, String r1_Code, String r2_TrxId, String r3_Amt,
			String r4_Cur, String r5_Pid, String r6_Order, String r7_Uid,
			String r8_MP, String r9_BType, String keyValue) {
		StringBuffer sValue = new StringBuffer();
		// 商户编号
		sValue.append(p1_MerId);
		// 业务类型
		sValue.append(r0_Cmd);
		// 支付结果
		sValue.append(r1_Code);
		// 易宝支付交易流水号
		sValue.append(r2_TrxId);
		// 支付金额
		sValue.append(r3_Amt);
		// 交易币种
		sValue.append(r4_Cur);
		// 商品名称
		sValue.append(r5_Pid);
		// 商户订单号
		sValue.append(r6_Order);
		// 易宝支付会员ID
		sValue.append(r7_Uid);
		// 商户扩展信息
		sValue.append(r8_MP);
		// 交易结果返回类型
		sValue.append(r9_BType);
		String sNewString = null;
		sNewString = DigestUtil.hmacSign(sValue.toString(), keyValue);
		if (hmac.equals(sNewString)) {
			return (true);
		}
		return (false);
	}
	
	
}
