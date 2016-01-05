package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class t_jingtum_transfer_details  extends Model {
	private static final long serialVersionUID = -6666244243802832841L;
	public String from_address;
	public String to_address;
	public String trsfer_amount;
	public String from_currency;
	public String client_resource_id;
	public String hash;
	public String state;
	public String fee;
	public String date;
	public String result;
	public String success;
	public Date create_time;
}
