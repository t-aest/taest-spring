package com.taest.v2.spring.framework.webmvc.servlet;


import java.io.File;

public class TTViewResolver {

    private final String DEFAULT_TEMPLATE_SUFFIX = ".html";

    private File templateRootDir;

    public TTViewResolver(String templateRoot) {

        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        templateRootDir = new File(templateRootPath);

    }

    public TTView resolveViewName(String viewName){
        if (null == viewName || "".equals(viewName)) return null;
        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFIX) ? viewName : (viewName + DEFAULT_TEMPLATE_SUFFIX);
        File templateFile = new File((templateRootDir.getPath() + "/" + viewName).replaceAll("/+","/"));
        return new TTView(templateFile);
    }


}
