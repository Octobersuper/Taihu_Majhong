package com.zcf.mahjong.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.gson.Gson;

/**
 * 
 * @author Administrator 文件上传 具体步骤： 1）获得磁盘文件条目工厂 DiskFileItemFactory 要导包 2） 利用
 *         request 获取 真实路径 ，供临时文件存储，和 最终文件存储 ，这两个存储位置可不同，也可相同 3）对
 *         DiskFileItemFactory 对象设置一些 属性 4）高水平的API文件上传处理 ServletFileUpload
 *         upload = new ServletFileUpload(factory); 目的是调用
 *         parseRequest（request）方法 获得 FileItem 集合list ，
 * 
 *         5）在 FileItem 对象中 获取信息， 遍历， 判断 表单提交过来的信息 是否是 普通文本信息 另做处理 6） 第一种. 用第三方
 *         提供的 item.write( new File(path,filename) ); 直接写到磁盘上 第二种. 手动处理
 * 
 */
@WebServlet("/upload")
public class VoiceUpload extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8280686629155707434L;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// 接受参数转码
		request.setCharacterEncoding("UTF-8");
		// 转发参数转码
		response.setContentType("text/json;charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		// 输出-必须将此对象创建顺序放在转码之后
		PrintWriter out = response.getWriter();
		Map map = new HashMap();
		// 为解析类提供配置信息
		DiskFileItemFactory factory = new DiskFileItemFactory();
		// 创建解析类的实例
		ServletFileUpload sfu = new ServletFileUpload(factory);
		String name = "";
		String extName = "";
		// 开始解析
		// sfu.setFileSizeMax(300*150);
		// 每个表单域中数据会封装到一个对应的FileItem对象上
		try {
			List<FileItem> items = sfu.parseRequest(request);
			// 区分表单域
			for (int i = 0; i < items.size(); i++) {
				FileItem item = items.get(i);
				// isFormField为true，表示这不是文件上传表单域
				if (!item.isFormField()) {
					// D:\\develop\\apache-tomcat-8.5.34\\webapps\\Reputation_PK\\Voice\\615a5b3e-7f2c-47ed-b4ae-ff10a49495ad.mp3
					String path = "F:\\HaoYi_Mahjong\\Voice\\";
					name = item.getName();
					if (name == null || name.trim().equals("")) {
						continue;
					}
					// 扩展名格式：
					if (name.lastIndexOf(".") >= 0) {
						extName = name.substring(name.lastIndexOf("."));
					}
					if (extName.equals(".wav") || extName.equals(".mp3") || extName.equals(".MP3")|| extName.equals(".png")|| extName.equals(".jpg")|| extName.equals(".gif")) {
						File file = null;
						do {
							// 生成文件名：
							name = UUID.randomUUID().toString();
							file = new File(path + name + extName);
						} while (file.exists());
						File saveFile = new File(path + name + extName);
						try {
							item.write(saveFile);
							map.put("message", "success");
							map.put("picurl", name + extName);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						map.put("message", "文件类型错误!");
					}
				}
			}
		} catch (Exception e) {
			map.put("message", "sizeBig");
		}
		String json = new Gson().toJson(map).toString();
		out.print(json);
	}
}
