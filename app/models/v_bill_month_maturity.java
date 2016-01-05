package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 * 我的会员账单--本月到期账单
 */
@Entity
public class v_bill_month_maturity extends Model {
    private static final long serialVersionUID = 4735070790652569002L;
	public long bid_id;
    public long user_id;
    public int year;
    public int month;
    public String bill_no;
    public String name;
    public String bid_no;
    public double amount;
    public double apr;
    public String title;
    public String period;
    public Date repayment_time;
    public String overdue_time;
    public long overdue_count;
    public String supervisor_name;
    public long supervisor_id;
    
    @Transient
	public String sign;
    
    public v_bill_month_maturity(){
		
	}
	
	public v_bill_month_maturity(long bid_id, String bill_no, String name, String bid_no, double amount,
			double apr, String title, String period, Date repayment_time, String overdue_time, long overdue_count
			,String supervisor_name, long supervisor_id){
		this.bid_id = bid_id;
		this.bill_no = bill_no;
		this.name = name;
		this.bid_no = bid_no;
		this.amount = amount;
		this.apr = apr;
		this.title = title;
		this.period = period;
		this.repayment_time = repayment_time;
		this.overdue_time = overdue_time;
		this.overdue_count = overdue_count;
		this.supervisor_name = supervisor_name;
		this.supervisor_id = supervisor_id;
	}

	/**
	 * 获取加密ID
	 */
	public String getSign() {
		return Security.addSign(this.id, Constants.BILL_ID_SIGN);
	}
}