package models;

import javax.persistence.Entity;

import constants.Constants;
import play.db.jpa.Model;
import utils.Hmac;

@Entity
public class t_jingtum_withdraw_records extends Model{
	private static final long serialVersionUID = 5639954665710399582L;
	//{"r0_cmd":"Withdraw","r1_code":"0046","r2_custom":"10001126856",
	//"r3_order":"81000000030000201507161970010101203FDB9B","r4_dcode":"0030",
	//"r5_bank_status":"","r6_error_msg":"","hmac":"5e05439c95724b1f7e858556b22b64ec"}
	public String bank_code;//银行状态
	public String orders;//订单号
	public String dcode;//打款状态
	public String bank_status;//
	public String error_msg;//错误信息
	public boolean result;//最终结果，【1：成功】，其他全为失败
	public String remark1;//备用
	public String remark2;//备用
	
	/**
	 *验证 提现接口数据的正确性， 是否被篡改
	 * @param r0_cmd
	 * @param r1_code
	 * @param r2_custom
	 * @param r3_order
	 * @param r4_dcode
	 * @param r5_bank_status
	 * @param hmac
	 * @return
	 */
	public boolean eqhmac(String r0_cmd,String r1_code,String r2_custom,String r3_order,String r4_dcode,String r5_bank_status,String hmac){
		StringBuffer string =new StringBuffer();
		string.append(r0_cmd);
		string.append(r1_code);
		string.append(r2_custom);
		string.append(r3_order);
		string.append(r4_dcode);
		string.append(r5_bank_status);
		String  result=Hmac.getHmacMd5Bytes(string.toString(),Constants.JING_HMAC_KEY);
		if(result.equals(hmac)){
			return true;
		}else{
			return false;
		}
	}
}
