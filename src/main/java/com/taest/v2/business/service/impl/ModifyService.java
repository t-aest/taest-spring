package com.taest.v2.business.service.impl;

import com.taest.v2.business.service.IModifyService;
import com.taest.v2.spring.framework.annotation.TTService;

/**
 * 增删改业务
 *
 */
@TTService
public class ModifyService implements IModifyService {

	/**
	 * 增加
	 */
	public String add(String name,String addr) throws Exception {
		throw new Exception("这是故意抛出来的异常");
//		return "modifyService add,name=" + name + ",addr=" + addr;
	}

	/**
	 * 修改
	 */
	public String edit(Integer id,String name) {
		return "modifyService edit,id=" + id + ",name=" + name;
	}

	/**
	 * 删除
	 */
	public String remove(Integer id) {
		return "modifyService id=" + id;
	}
	
}
