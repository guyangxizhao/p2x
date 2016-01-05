$(function(){
    $(".list li").click(function(){
        $(".list li").removeClass('active');
        $(" .j-box").hide();
        $(this).addClass('active');
        $(" .j-box").eq($(this).index()).show();
    });

    $(".security-list li").click(function(){
        $(".security-list li").removeClass('active');
        $(" .security-btm").hide();
        $(this).addClass('active');
        $(" .security-btm").eq($(this).index()).show();
    });

    //帮助中心
    $(".help li").click(function(){
        $(".help li").removeClass('active');
        $(".help-btm").hide();
        $(this).addClass('active');
        $(".help-btm").eq($(this).index()).show();
        $(".j-help-text").css('display', 'none');
        $(".j-help-text").eq($(this).index()).css('display', 'block');
    });
    $(".h_li1 .help-btm li").click(function(){
        $(".h_li1 .help-btm li").removeClass('active2');
        $(".help-bottom .h_div1 .j-loan").removeClass('show');
        $(this).addClass('active2');
        $(".help-bottom .h_div1 .j-loan").eq($(this).index()).addClass('show');

    });

    $(".h_li2 .help-btm li").click(function(){
        $(".h_li2 .help-btm li").removeClass('active2');
        $(".help-bottom .h_div2 .j-loan").removeClass('show');
        $(this).addClass('active2');
        $(".help-bottom .h_div2 .j-loan").eq($(this).index()).addClass('show');

    });

    $(".h_li3 .help-btm li").click(function(){
        $(".h_li3 .help-btm li").removeClass('active2');
        $(".help-bottom .h_div3 .j-loan").removeClass('show');
        $(this).addClass('active2');
        $(".help-bottom .h_div3 .j-loan").eq($(this).index()).addClass('show');

    });

    $(".help .help-btm").mouseleave(function(){
        $(".help .help-btm").hide();
    });
    $("a").bind("focus",function() {
        if(this.blur) {this.blur()};
    });

    //服务条款
    $(".service-list li").toggle(function(){
        $(".service-list li").removeClass('on');
        $(".service-list li p").hide();
        $(this).addClass('on');
        $(".service-list li p").eq($(this).index()).show();
    },function(){
        $(".service-list li p").hide();
        $(".service-list li").removeClass('on');
    });
});/**
 * Created by Administrator on 2015/7/24.
 */
