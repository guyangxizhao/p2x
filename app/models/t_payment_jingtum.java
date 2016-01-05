package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * 标 还款的 井通转账记录
 * @author houyaoshan
 *
 */
@Entity
public class t_payment_jingtum  extends Model{
	private static final long serialVersionUID = 1768640855854513817L;
	public long bid_id;//标id
	public String bid_address;//本标的地址【用于收回融资通】
	public int periods;//当前标的还款期数【第几期】 
	public long bid_user_id;//标的借款人id
	public long bid_pay_id;//标的投资者用户id
	public String invest_amount;//本次 本标 的还款额度
	public String tong_amount;//收回【融资通】的金额【从投资者 向 p2x 收回】
	public Date create_time;//记录创建时间
	public Date update_time;//更新时间
	public boolean result;//本次转账结果
	public boolean tong_result;//收回【融资通】的结果
	
}
