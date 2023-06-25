package com.taest.v1.business.mvc.action;


import com.taest.v1.business.service.IDemoService;
import com.taest.v1.springmvc.annotation.TTAutowired;
import com.taest.v1.springmvc.annotation.TTController;
import com.taest.v1.springmvc.annotation.TTRequestMapping;
import com.taest.v1.springmvc.annotation.TTRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


//虽然，用法一样，但是没有功能
@TTController
@TTRequestMapping("/demo")
public class DemoAction {

  	@TTAutowired
	private IDemoService demoService;

	@TTRequestMapping("/query")
	public void query(HttpServletRequest req, HttpServletResponse resp,
					  @TTRequestParam("name") String name){
		String result = demoService.get(name);
//		String result = "My name is " + name;
		try {
			resp.getWriter().write(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@TTRequestMapping("/add")
	public void add(HttpServletRequest req, HttpServletResponse resp,
					@TTRequestParam("a") Integer a, @TTRequestParam("b") Integer b){
		try {
			resp.getWriter().write(a + "+" + b + "=" + (a + b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@TTRequestMapping("/sub")
	public void add(HttpServletRequest req, HttpServletResponse resp,
					@TTRequestParam("a") Double a, @TTRequestParam("b") Double b){
		try {
			resp.getWriter().write(a + "-" + b + "=" + (a - b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@TTRequestMapping("/remove")
	public String  remove(@TTRequestParam("id") Integer id){
		return "" + id;
	}

}
