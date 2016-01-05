package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * 资金托管回调（响应）参数信息
 *
 * @author hys
 * @createDate  2015年4月9日 上午9:17:30
 *
 */
@Entity
public class t_entrust_logs_callback extends Model {
	private static final long serialVersionUID = -2568914861349340518L;
	public Date time;  //创建时间
	public long mer_bill_no;  //流水号
	public String callback_detail;  //回调参数详情
}
