package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 交易记录
 * @author cp
 * @version 6.0
 * @created 2014年5月20日 下午7:54:31
 */
@Entity
public class v_user_details extends Model {
	private static final long serialVersionUID = -4851413184806423804L;
	public long user_id;
	public Date time;
	public long operation;
	public double amount;//金额记录
	public double user_balance;//账户总额
	public double balance;//可用余额
	public double freeze;//冻结金额
	public double recieve_amount;//待收金额
	public String summary;
	public String name;//交易名称
	public int type;
}
