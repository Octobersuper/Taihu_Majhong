/*var reg = /\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}/;
var ip = reg.exec(window.location.href)[0];
var baseurl = "http://"+ip+":8080/Taihu_Mahjong";*/
//var baseurl = "http://localhost:8099/Taihu_Majhong";
var baseurl = "http://103.193.175.59:8080//Taihu_Majhong";
function getQueryString(name) {
	var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
	var r = window.location.search.substr(1).match(reg);
	if(r != null) {
		return unescape(r[2]);
	}
	return null;
}
$(document).on('keydown', function(e){
	if (e.keyCode === 13) {
		$(".layui-keydown").click();
	}
})