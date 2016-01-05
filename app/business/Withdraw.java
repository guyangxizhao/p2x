package business;


import java.util.Date;

import utils.DateUtil;
import utils.Hmac;

import com.jingtum.currency.Currency;




import constants.Constants;

/**
 * 提现业务类
 * @author houyaoshan
 *
 */
public class Withdraw {
	public String p0_cmd;
	public String p1_custom;
	public String p2_batch;
	public String p3_order;
	public String p4_bank_code;
	public String p5_bank_name;
	public String p6_branch_name;
	public String p7_amount;
	public String p8_account_name;
	public String p9_account_number;
	public String pa_account_type;
	public String pb_province;
	public String pc_city;
	public String pd_fee_type;
	public String pe_email;
	public String pf_mobile;
	public String pg_remark;
	
	public String ph_source; //提现的用户井通地址
	public String pi_target; //提现CNY回收地址
	public String pj_tx; //提现交易记录Hash
	public String hmac;
	
	public String getHmac(Withdraw withdraw){
		StringBuffer string=new StringBuffer();
			string.append(withdraw.p0_cmd);
			string.append(withdraw.p1_custom);
			string.append(withdraw.p2_batch);
			string.append(withdraw.p3_order);
			string.append(withdraw.p7_amount);
			string.append(withdraw.p9_account_number);
			String hmac= Hmac.getHmacMd5Bytes(string.toString(),Constants.JING_HMAC_KEY).toLowerCase();
			System.out.println(string.toString());
			System.out.println("hmac:"+hmac);
			return hmac;
	}
	public Withdraw getWithdraw(String p1_custom,String bank_name,String amount,String account_name,String account,String email,String mobile,String remark,String ph_source,String pi_target,String pj_tx){
		Withdraw withdraw=new Withdraw();
		withdraw.p0_cmd=Constants.JING_P0_D_CMD;
		withdraw.p1_custom=p1_custom;//测试时用易宝账号，正式环境，使用p2x 银关账号
		withdraw.p2_batch=DateUtil.simple2(new Date())+"0";//yyyyMMdd HHmmss+0 出去8位年月日 追加7位数字即可 
		withdraw.p3_order=Currency.GenerateP2XTum(new Date(),null);
		withdraw.p4_bank_code="";//CMBCHINA-NET-B2B
		withdraw.p5_bank_name=bank_name;
		withdraw.p6_branch_name="";
		withdraw.p7_amount=amount;
		withdraw.p8_account_name=account_name;
		withdraw.p9_account_number=account;
		withdraw.pa_account_type=Constants.JING_PA_ACCOUNT_TYPE_PR;
		withdraw.pb_province="";
		withdraw.pc_city="";
		withdraw.pd_fee_type=Constants.JING_FD_FEE_TYPE_TARGET;//默认用户提现，自行承担服务费
		withdraw.pe_email=email;
		withdraw.pf_mobile=mobile;
		withdraw.pg_remark=remark;
		
		withdraw.ph_source=ph_source;
		withdraw.pi_target=pi_target;
		withdraw.pj_tx=pj_tx;
		
		return withdraw;
	}
	/**
	 * 
	* @Title: fomartDouble 
	* @Description: 金额必须是两位小数的字符串
	* @param @param amount
	* @param @return    
	* @return String 
	* @throws
	 */
	public static String fomartDouble(Double amount){
		String str=amount.toString();
		switch (str.length()-str.lastIndexOf(".")) {
		case 1:
			return str+"00";
		case 2:
			return str+"0";
		default:
			return str;
		}
	}
}
