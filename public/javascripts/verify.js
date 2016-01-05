$(function(){
	
	$('#switch_qlogin').click(function(){
		$('#switch_login').removeClass("switch_btn_focus").addClass('switch_btn');
		$('#switch_qlogin').removeClass("switch_btn").addClass('switch_btn_focus');
		$('#switch_bottom').animate({left:'0px',width:'70px'});
		$('#qlogin').css('display','none');
		$('#web_qr_login').css('display','block');
		
		});
	$('#switch_login').click(function(){
		
		$('#switch_login').removeClass("switch_btn").addClass('switch_btn_focus');
		$('#switch_qlogin').removeClass("switch_btn_focus").addClass('switch_btn');
		$('#switch_bottom').animate({left:'154px',width:'70px'});
		
		$('#qlogin').css('display','block');
		$('#web_qr_login').css('display','none');
		});
if(getParam("a")=='0')
{
	$('#switch_login').trigger('click');
}

	});
	
function logintab(){
	scrollTo(0);
	$('#switch_qlogin').removeClass("switch_btn_focus").addClass('switch_btn');
	$('#switch_login').removeClass("switch_btn").addClass('switch_btn_focus');
	$('#switch_bottom').animate({left:'154px',width:'96px'});
	$('#qlogin').css('display','none');
	$('#web_qr_login').css('display','block');
	
}


//根据参数名获得该参数 pname等于想要的参数名 
function getParam(pname) { 
    var params = location.search.substr(1); // 获取参数 平且去掉？ 
    var ArrParam = params.split('&'); 
    if (ArrParam.length == 1) { 
        //只有一个参数的情况 
        return params.split('=')[1]; 
    } 
    else { 
         //多个参数参数的情况 
        for (var i = 0; i < ArrParam.length; i++) { 
            if (ArrParam[i].split('=')[0] == pname) { 
                return ArrParam[i].split('=')[1]; 
            } 
        } 
    } 
}  

function hintError(ele, hintEle, msg) {
	if (ele)
		$(ele).addClass("error");
	$(hintEle).html("<font color='red'><b>" + msg + "</b></font>");
}
function hintSuccess(ele, hintEle, msg) {
	$(hintEle).html("<font color='green'><b>" + msg + "</b></font>");
}
function outError(ele){
	if (ele)
		$(ele).removeClass("error");
}


$(document).ready(function(){
	$("#reg").click(function(){
		if($("#name").val() == ""){
			hintError("#name", "#userCue", "请输入用户名");
		}else if($("#passwd").val() == ""){
			hintError("#passwd", "#userCue", "请输入密码");
		}else if($("#code").val() == ""){
			hintError("#code", "#userCue", "请输入验证码");
		}else{
			outError("#name");
			outError("#passwd");
			outError("#code");
			$('#loginForm').submit();
		}
	});
	$(".input-group input").focus(function(){
		$(this).removeClass("error");
		$("#userCue").html("");
	});
});




	
	
	






