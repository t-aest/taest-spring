package com.taest.v2.business.action;

import com.taest.v2.business.service.IQueryService;
import com.taest.v2.spring.framework.annotation.TTAutowired;
import com.taest.v2.spring.framework.annotation.TTController;
import com.taest.v2.spring.framework.annotation.TTRequestMapping;
import com.taest.v2.spring.framework.annotation.TTRequestParam;
import com.taest.v2.spring.framework.webmvc.servlet.TTModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * 公布接口url
 *
 */
@TTController
@TTRequestMapping("/")
public class PageAction {

    @TTAutowired
    IQueryService queryService;

    @TTRequestMapping("/first.html")
    public TTModelAndView query(@TTRequestParam("taest") String taest){
        String result = queryService.query(taest);
        Map<String,Object> model = new HashMap<String,Object>();
        model.put("taest", taest);
        model.put("data", result);
        model.put("token", "123456");
        return new TTModelAndView("first.html",model);
    }

}
