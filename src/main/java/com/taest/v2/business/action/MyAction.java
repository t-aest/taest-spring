package com.taest.v2.business.action;


import com.taest.v2.business.service.IModifyService;
import com.taest.v2.business.service.IQueryService;
import com.taest.v2.spring.framework.annotation.TTAutowired;
import com.taest.v2.spring.framework.annotation.TTController;
import com.taest.v2.spring.framework.annotation.TTRequestMapping;
import com.taest.v2.spring.framework.annotation.TTRequestParam;
import com.taest.v2.spring.framework.webmvc.servlet.TTModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 公布接口url
 *
 */
@TTController
@TTRequestMapping("/web")
public class MyAction {

	@TTAutowired
	IQueryService queryService;
	@TTAutowired
	IModifyService modifyService;

	@TTRequestMapping("/query.json")
	public TTModelAndView query(HttpServletRequest request, HttpServletResponse response,
								@TTRequestParam("name") String name){
		String result = queryService.query(name);
		return out(response,result);
	}
	
	@TTRequestMapping("/add*.json")
	public TTModelAndView add(HttpServletRequest request,HttpServletResponse response,
			   @TTRequestParam("name") String name,@TTRequestParam("addr") String addr){
		try {
			String result = modifyService.add(name, addr);
			return out(response,result);
		}catch (Throwable e){
			Map<String,String> model = new HashMap<String,String>();
			model.put("detail",e.getCause().getMessage());
			model.put("stackTrace", Arrays.toString(e.getStackTrace()));
			return new TTModelAndView("500",model);
		}
	}
	
	@TTRequestMapping("/remove.json")
	public TTModelAndView remove(HttpServletRequest request,HttpServletResponse response,
		   @TTRequestParam("id") Integer id){
		String result = modifyService.remove(id);
		return out(response,result);
	}
	
	@TTRequestMapping("/edit.json")
	public TTModelAndView edit(HttpServletRequest request,HttpServletResponse response,
			@TTRequestParam("id") Integer id,
			@TTRequestParam("name") String name){
		String result = modifyService.edit(id,name);
		return out(response,result);
	}
	
	
	
	private TTModelAndView out(HttpServletResponse resp,String str){
		try {
			resp.getWriter().write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
	
}
