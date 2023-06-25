package com.taest.v1.business.service.impl;

import com.taest.v1.business.service.IDemoService;
import com.taest.v1.springmvc.annotation.TTService;

/**
 * 核心业务逻辑
 */
@TTService
public class DemoService implements IDemoService {

	public String get(String name) {
		return "My name is " + name + ",from service.";
	}

}
