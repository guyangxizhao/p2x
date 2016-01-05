/**
 * @author  pangzimin | ziminy@qq.com
 * @version 15-06-24
 * @qq 
 * @example
 * @init
 * @handle
 */


if (!window.console) {
    window.console = {
        log: function(info) {
            return false
        }
    }
}

$(function(){

	try{
		$('.js-schedule-cft').circliful();
		$("#slideBox").slide({mainCell:".bd ul",autoPlay:true,interTime:5000});
	}catch(e){}

  	
  	var Bcircliful = false;
  	$(window).scroll(function(){
	  	if($(window).scrollTop() > 450 && !Bcircliful){
	  		$('.js-schedule').circliful();
	  		Bcircliful = true;
	  	}
  	});

  	$('.js-protocol').click(function() {
  		$('.userProtocol i').toggle();
  	});

  	$('.js-file').on('change', function () {
  		var str = $(this).val();
  		var t = str.lastIndexOf('\\');
  		if(str){
  			$(this).next().html(str.substring(t + 1, str.length));
  		}else{
  			return;
  		}
  	});

  	$('.js-tab').on('mouseover', '.hd a', function(){
  		var index = $(this).index();
  		$('.js-tab .hd a').removeClass('on');
  		$(this).addClass('on');
  		$('.js-tab').find('.bd').hide().eq(index).show();
  	});

  	$('.js-nav').find('dt').click(function (){
  		var me = $(this);
  		$("dt").removeClass('active');
  		$("dd").css("display", "none");
  		me.next().slideToggle('400');
  		if(me.hasClass('active')){
  			me.removeClass('active');
  		}else{
  			me.addClass('active');
  		}
  	});

  	$('.js-tab').on('click', 'span', function(){
  		var index = $(this).index();
  		$('.js-tab span').removeClass('on').eq(index).addClass('on');
  		$('.js-tab-content').find('ul').hide().eq(index).show();
  	});

    $('.js-slideBox').on('click', '.hd span', function(){
      var index = $(this).index();
      $('.js-slideBox .hd span').removeClass('on').eq(index).addClass('on');
      $('.js-slideBox .bd .table').hide().eq(index).show();
    });

})
