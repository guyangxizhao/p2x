(function( factory ) {
	if ( typeof define === "function" && define.amd ) {
		define( ["jquery", "../jquery.validate"], factory );
	} else {
		factory( jQuery );
	}
}(function( $ ) {

/*
 * Translated default messages for the jQuery validation plugin.
 * Locale: ZH (Chinese, 中文 (Zhōngwén), 汉语, 漢語)
 */
$.extend($.validator.messages, {
	required: "",
	remote: "",
	email: "",
	url: "",
	date: "",
	dateISO: "",
	number: "",
	digits: "",
	creditcard: "",
	equalTo: "",
	extension: "",
	maxlength: $.validator.format("<font color='red'><b>最多可以输入 {0} 个字符</b></font>"),
	minlength: $.validator.format(""),
	rangelength: $.validator.format(""),
	range: $.validator.format(""),
	max: $.validator.format(""),
	min: $.validator.format("")
	/*required: "<font color='red'><b>这是必填字段</b></font>",
	remote: "请修正此字段",
	email: "请输入有效的电子邮件地址",
	url: "请输入有效的网址",
	date: "请输入有效的日期",
	dateISO: "请输入有效的日期 (YYYY-MM-DD)",
	number: "<font color='red'><b>请输入有效的数字</b></font>",
	digits: "<font color='red'><b>只能输入数字</b></font>",
	creditcard: "请输入有效的信用卡号码",
	equalTo: "你的输入不相同",
	extension: "请输入有效的后缀",
	maxlength: $.validator.format("<font color='red'><b>最多可以输入 {0} 个字符</b></font>"),
	minlength: $.validator.format("<font color='red'><b>最少要输入 {0} 个字符</b></font>"),
	rangelength: $.validator.format("<font color='red'><b>请输入长度在 {0} 到 {1} 之间的字符串</b></font>"),
	range: $.validator.format("<font color='red'><b>请输入范围在 {0} 到 {1} 之间的数值</b></font>"),
	max: $.validator.format("<font color='red'><b>请输入不大于 {0} 的数值</b></font>"),
	min: $.validator.format("<font color='red'><b>请输入不小于 {0} 的数值</b></font>")*/
});

}));