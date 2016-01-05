package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * 井通转账投标记录
 * @author houyaoshan
 *
 */
@Entity
public class v_invest_jingtum extends Model{
	private static final long serialVersionUID = -6210452178029140444L;
	public long bid_id;
	public long user_id;
	public String bid_address;
	public String invest_amount;
	public String wallet_address;
	public String wallet_secret;
}
