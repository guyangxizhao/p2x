package controllers;

import play.*;
import play.cache.Cache;
import play.db.jpa.Blob;
import play.libs.Codec;
import play.libs.Images;
import play.mvc.*;
import utils.CaptchaUtil;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.FileUtil;
import utils.PageBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONArray;
import business.News;
import business.SystemUpgradeLogs;
import business.User;
import constants.Constants;
import models.*;

public class Application extends BaseController {

    public static void index() {
        render();
    }
    
    public static void errorFront() {
        render();
    }
    
    public static void errorSupervisor() {
        render();
    }
    
    public static void exportExcel() {
    	List<t_bills> list = t_bills.findAll();
    	
    	File file = ExcelUtils.export(
    			"我的理财账单", 
    			list,
				new String[] {"账单标题", "到期还款时间", "应还本金"}, 
				new String[] {"title", "repayment_time", "repayment_corpus"});
    	
    	String filename = file.getName();
		
    	renderBinary(file, filename);
    }
    
    /**
	 * 生成随机码
	 */
	public static void randomID() {
		String randomID = Codec.UUID();
		renderText(randomID);
	}
	
	/**
	 * 生成验证码图片
	 * @param id
	 */
	public static void captcha(String id) {
		
		if(StringUtils.isEmpty(id)){  //缓存key不能为空
			return;
		}
		
		String cacheCode = (String) Cache.get(id);
		if(!StringUtils.isEmpty(cacheCode)){  
			return;
		}
		
        Images.Captcha captcha = Images.captcha().setBackground("#DEF1F8", "#DEF1F8");
        String code = captcha.getText("#0056A0", 4);
        
        if(CaptchaUtil.isExistCaptcha(id)){
        	Cache.set(id, code, "10mn");
        	Logger.debug("CaptchaUtil>CaptchaImage[id:%s,code:%s,getCache:%s]", id,code,Cache.get(id));
    	}

        renderBinary(captcha);
    }

	
	/**
	 * 图片查看
	 * @param uuid
	 * @throws FileNotFoundException
	 */
	public static void images(String uuid) throws FileNotFoundException{
		Blob blob = new Blob();
    	InputStream is = null;
		try {
			is = new FileInputStream(new File(blob.getStore(), uuid.split("\\.")[0]));
		} catch (Exception e) {
			String img_url= Constants.QINIU_IMG_DOMAIN+uuid;
			img_url=FileUtil.getQiNiuDownToken(img_url);
			try {
				redirect(img_url);
			} catch (Exception e2) {
				e2.printStackTrace();
				is = new FileInputStream(Play.getFile("public/images/default.png"));
			}
		}
    	renderBinary(is);
	}
	
	public static void downloadFiles(String uuids, int type) {
		Http.Response.current().headers.put("Pragma", new Http.Header("Pragma", "public"));
		Http.Response.current().headers.put("Cache-Control", new Http.Header("Cache-Control", "max-age=0"));
		
		uuids = uuids.replaceAll(Constants.HTTP_PATH, "");
		uuids = uuids.replaceAll("images\\?uuid=", ""); // 问号和/需要转义
		String[] files = uuids.split(":");
		
		if(files.length <1 ){
			return;
		}
		
		String suffix = ".zip";
		long sys_time = System.currentTimeMillis();
		String fileName = sys_time+suffix;
		String path = Play.getFile("/data").getAbsolutePath()+File.separator+fileName;

		renderBinary(type==1 ? FileUtil.zipImages(files, path) : FileUtil.zipFiles(files, path), fileName);
	}
	
	/**
	 * 图片下载
	 * @param uuids 必须带一个“：”，否则非法
	 * @throws IOException
	 */
	public static void dlImages(String uuids) throws IOException{
		Http.Response.current().headers.put("Pragma", new Http.Header("Pragma", "public"));
		Http.Response.current().headers.put("Cache-Control", new Http.Header("Cache-Control", "max-age=0"));
		
		uuids = uuids.replaceAll("\\" + Constants.HTTP_PATH + "images\\?uuid=", ""); // 问号和/需要转义
		System.out.println("uuids2:"+uuids);
		String[] images = uuids.split(":");
		
		if(images.length <1 ){
			return;
		}
		
		String suffix = ".zip";
		long sys_time = System.currentTimeMillis();
		String fileName = sys_time+suffix;
		String path = Play.getFile("/tmp/").getAbsolutePath()+"/"+fileName;

		renderBinary(FileUtil.zipImages(images, path), fileName);
	}
	
	/**
	 * 根据省获得市联动
	 */
	public static void getCity(long provinceId){
		List<t_dict_ad_citys> cityList = User.queryCity(provinceId);
		JSONArray json = JSONArray.fromObject(cityList);
		renderJSON(json);
	}
	
	
	public static void dlWidget(){
		Http.Response.current().headers.put("Pragma", new Http.Header("Pragma", "public"));
		Http.Response.current().headers.put("Cache-Control", new Http.Header("Cache-Control", "max-age=0"));
		
		String path = Play.getFile("/public/").getAbsolutePath()+"/Setup.exe";
		File file = new File(path);
		renderBinary(file);
	}
	
	public static void vipAgreement() {
		String agreement = News.queryVipAgreement();
		
		renderText(agreement);
	}
	
	public static void logs(){
		ErrorInfo error = new ErrorInfo();
		PageBean<t_system_upgrade_logs> page = SystemUpgradeLogs.queryLogs(error);
		render(page);
	}
	
	public static void dlpacks(String version){
		Blob blob = new Blob();
		File file = new File(blob.getStore(), version+".tar.gz");
		renderBinary(file);
	}
}