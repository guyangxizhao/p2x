package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class t_bid_jingtum extends Model {
	private static final long serialVersionUID = -6465897383380331545L;
	public long bid_id;			//'标的id'
	public String bid_address;	//'标在井通平台对应的id'
	public Date start_time;		//'创建时间'
	public Date end_time;		//'结束时间【状态修改时间】'
	public boolean  result;		//'最终审批结果'
	public boolean status;		//'当前状态 【默认：未审核】'
	
}
