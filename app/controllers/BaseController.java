package controllers;

import play.Play;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http.Request;
import constants.Constants;

public class BaseController extends Controller {

	@Before
	protected static void injectionInterceptor() throws Exception {
		String injectionVal = new com.shove.web.security.InjectionInterceptor().run();
		if (injectionVal == null || injectionVal.length() > 0) {
			render(Constants.ERROR_PAGE_PATH_INJECTION, injectionVal);
		}
	}
	
	/**
	 * 获取当前请求根路径
	 * @return
	 */
	public static String getBaseURL(){
		String baseURL = Constants.BASE_URL;
		
		Request req = Request.current();
		if(req != null){
			baseURL = req.getBase() + Play.configuration.getProperty("http.path") + "/";
		}
		
		return baseURL;
	}
}
