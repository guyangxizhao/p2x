package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import constants.Constants;
import business.ProductAuditItem;
import play.Logger;
import play.db.jpa.Model;
import utils.Security;

/**
 * 审核资料
 * 
 */
@Entity
public class v_product_audit_items extends Model {
	public Integer product_id;
	public Long audit_item_id;
	public String name;
	public Integer type;//类型
	public Integer ptype;//是否必须
}
