package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;

/**
 * 理财账单
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午03:42:20
 */
@Entity
public class t_bill_invests extends Model {
	private static final long serialVersionUID = 1577034327879817807L;
	public long user_id;
	public long bid_id;
	public long invest_id;
	public String mer_bill_no;
	public String title;
	public Date receive_time;
	public double receive_corpus;
	public double overdue_fine;
	public double receive_interest;
	public int status;
	public int periods;
	public Date real_receive_time;
	public double real_receive_corpus;
	public double real_receive_interest;
	@Transient
	public double receive_amount;
	@Transient
	public String dxreceive_amount;//大写金额
	
	public t_bill_invests(){
		
	}
	
	public t_bill_invests(long id,String title, double receive_amount, int status, Date receive_time
			, Date real_receive_time){
		this.id = id;
		this.title = title;
		this.receive_amount = receive_amount;
		this.status = status;
		this.receive_time = receive_time;
		this.real_receive_time = real_receive_time;
	}
	
	public t_bill_invests(long id,String title, int status, Date receive_time,double receive_amount,Date real_receive_time){
		this.id = id;
		this.title = title;
		this.status = status;
		this.receive_time = receive_time;
		this.receive_amount = receive_amount;
		this.real_receive_time = real_receive_time;
	}
	
	public t_bill_invests(long id,double receive_corpus ,double receive_interest,double overdue_fine){
		this.id = id;
		this.receive_corpus = receive_corpus;
		this.receive_interest = receive_interest;
		this.overdue_fine = overdue_fine;
	}

	public t_bill_invests(long id,long user_id, long bid_id, long invest_id,
			String mer_bill_no, String title, Date receive_time,
			double receive_corpus, double overdue_fine,
			double receive_interest, int status, int periods,
			Date real_receive_time, double real_receive_corpus,
			double real_receive_interest
			) {
		this.id=id;
		this.user_id = user_id;
		this.bid_id = bid_id;
		this.invest_id = invest_id;
		this.mer_bill_no = mer_bill_no;
		this.title = title;
		this.receive_time = receive_time;
		this.receive_corpus = receive_corpus;
		this.overdue_fine = overdue_fine;
		this.receive_interest = receive_interest;
		this.status = status;
		this.periods = periods;
		this.real_receive_time = real_receive_time;
		this.real_receive_corpus = real_receive_corpus;
		this.real_receive_interest = real_receive_interest;
		
	}
	 
}
