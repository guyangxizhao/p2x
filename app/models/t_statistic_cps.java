package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

@Entity
public class t_statistic_cps extends Model{
	private static final long serialVersionUID = -8248819958475074714L;
	public int year;
	public int month;
	public long cps_count;
	public long recommend_count;
	public long recharge_count;
	public double invest_amount;
	public double bid_amount;
	public double manage_fee;
	public double cps_amount;
}
