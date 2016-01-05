import net.sf.json.*;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.SQLQuery;
import org.hibernate.engine.ExecuteUpdateResultCheckStyle;
import org.hibernate.transform.Transformers;
import org.hibernate.usertype.UserType;
import org.junit.*;

import com.google.zxing.BarcodeFormat;
import com.shove.code.Qrcode;
import com.shove.security.Encrypt;

import constants.Constants;


import business.BackstageSet;
import business.Bid;
import business.Bill;
import business.BillInvests;
import business.BottomLinks;
import business.OverBorrow;
import business.StatisticalReport;
import business.Supervisor;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import constants.Constants;
import constants.OptionKeys;
import constants.Constants.*;
import business.Bid;
import business.CreditLevel;
import business.DataSafety;
import business.Debt;
import business.Invest;
import business.News;
import business.NewsType;
import business.OverBorrow;
import business.Product;
import business.Right;
import business.RightGroup;
import business.StationLetter;
import business.Supervisor;
import business.User;
import business.UserAuditItem;
import business.Bid.Purpose;
import business.Bid.Repayment;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import play.db.jpa.JPA;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;



import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.jobs.Job;
import play.jobs.On;
import play.test.*;
import reports.StatisticMember;
import reports.StatisticRecharge;
import reports.StatisticSecurity;
import utils.Arith;
import utils.DataUtil;
import utils.DateUtil;
import utils.ErrorInfo;
import reports.StatisticAuditItems;
import reports.StatisticBorrow;
import reports.StatisticInvest;
import reports.StatisticProduct;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.PageBean;
import utils.RegexUtils;
import models.*;

public class BasicTest extends UnitTest {
	
	@Test
	public void a(){
		ErrorInfo error = new ErrorInfo();
		
		new Bill().systemMakeOverdue(error); //系统标记逾期
	}
	
	@Test
	public void aa(){
		
	}
	
	@Test
	public void aaa(){
		
	}
	
	@Test
	public void aaaa(){
		
	}
	
	@Test
	public void aaaaa(){
		
	}
}
