package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class t_invest_jingtum extends Model {
	private static final long serialVersionUID = 7839813517042656812L;
	public long bid_id;
	public long user_id;
	public String bid_address;
	public String invest_amount;
	public String wallet_address;
	public String wallet_secret;
	public Date create_time;
	public boolean status;
}
