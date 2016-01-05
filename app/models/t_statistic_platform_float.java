package models;

import javax.persistence.Entity;
import play.db.jpa.Model;


/**
 * 平台浮存金统计表
 * @author lwh
 *
 */

@Entity
public class t_statistic_platform_float extends Model{
	private static final long serialVersionUID = -2330850755485169503L;
	public int year;
	public int month;
	public int day;
	public double balance_float_sum;
	public double freeze_float_sum;
	public double float_sum;
	public long has_balance_user_account;
	public double average_balance;
	public long has_balance_vip_user_account;
	public double vip_balance_float;
	public double average_vip_balance;
}
