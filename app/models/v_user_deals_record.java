package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 交易记录
 * @author wangliang
 * @created 2015年7月24日 下午6:14:31
 */
@Entity
public class v_user_deals_record extends Model {
	private static final long serialVersionUID = 5275934985874710087L;
	public long user_id;
	public Date time;
	public long operation;
	public double amount;//金额记录
	public double user_balance;//账户总额
	public double balance;//可用余额
	public double freeze;//冻结金额
	public double recieve_amount;//待收金额
	public String name;//交易类型名称
	public int type;
}
