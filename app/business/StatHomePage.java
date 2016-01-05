package business;

import static constants.Constants.HOME_PG_DEAL_MONEY;
import static constants.Constants.HOME_PG_INV_PEOPLE;
import static constants.Constants.HOME_PG_LOAN_MONEY;
import static constants.Constants.HOME_PG_REG_PEOPLE;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;

import javax.persistence.Query;

import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;

public class StatHomePage {
	private static final String SQL_QUERY_REG = "select count(*) total from t_users";
	private static final String SQL_QUERY_INV = "select COUNT(DISTINCT user_id) total from t_bill_invests";
	private static final String SQL_QUERY_LOAN = "select COUNT(DISTINCT user_id) total from t_bids";
	private static final String SQL_QUERY_DEAL = "select format(sum(amount)/10000,2) as total from t_bids where status in (3,4)";
	
	private static final DecimalFormat FORMATER_INT = new DecimalFormat("#,##0");
	private static final DecimalFormat FORMATER_DEC = new DecimalFormat("#,##0.00");
	
	
	private static String statistic(String sql) {
		Query query = JPA.em().createNativeQuery(sql);
		Object r = query.getSingleResult();
		
		if (r == null)
			return "0";
		if (r instanceof BigInteger)
			return FORMATER_INT.format((BigInteger) r);
		if (r instanceof BigDecimal)
			return FORMATER_DEC.format((BigDecimal) r);
		
		return "0";
	}
	
	public static void doStat() {
		statRegPeople();
		statInvPeople();
		statLoanMoney();
		statDealMoney();
	}

	/**
	 * 统计注册人数
	 */
	public static void statRegPeople() {
		Cache.add(HOME_PG_REG_PEOPLE, statistic(SQL_QUERY_REG));
		Logger.debug("注册人数[" + Cache.get(HOME_PG_REG_PEOPLE) + "]");
	}

	/**
	 * 投资人数
	 */
	public static void statInvPeople() {
		Cache.add(HOME_PG_INV_PEOPLE, statistic(SQL_QUERY_INV));
		Logger.debug("投资人数[" + Cache.get(HOME_PG_INV_PEOPLE) + "]");
	}

	/**
	 * 借款金额
	 */
	public static void statLoanMoney() {
		Cache.add(HOME_PG_LOAN_MONEY, statistic(SQL_QUERY_LOAN));
		Logger.debug("借款金额[" + Cache.get(HOME_PG_LOAN_MONEY) + "]");
	}

	/**
	 * 成交金额
	 */
	public static void statDealMoney() {
		Cache.add(HOME_PG_DEAL_MONEY, statistic(SQL_QUERY_DEAL));
		Logger.debug("成交金额[" + Cache.get(HOME_PG_DEAL_MONEY) + "]");
	}
}
