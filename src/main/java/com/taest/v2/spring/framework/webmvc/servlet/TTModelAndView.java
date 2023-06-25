package com.taest.v2.spring.framework.webmvc.servlet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TTModelAndView {

    private String viewName;

    private Map<String,?> model;

    public TTModelAndView(String viewName){
        this.viewName = viewName;
    }


}
