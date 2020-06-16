package cn.tedu.store.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.tedu.store.entity.Address;
import cn.tedu.store.service.IAddressService;
import cn.tedu.store.util.JsonResult;

@RestController
@RequestMapping("addresses")
public class AddressController extends BaseController{

	@Autowired
	private IAddressService addressService;
	
	@RequestMapping("addnew")
	public JsonResult<Void> addnew(
		Address address,HttpSession session){
		// 从Session中获取uid和username
		Integer uid = getUidFromSession(session);
		String username = getUsernameFromSession(session);
		// 调用业务层对象执行增加
		addressService.addnew(address, uid, username);
		// 响应成功
		return new JsonResult<>(SUCCESS);
	}
	
	@GetMapping("/")
	public JsonResult<List<Address>> getbyUid(HttpSession session){
		// 从session中获取uid
		Integer uid = getUidFromSession(session);
		// 调用业务层对象获取数据
		List<Address> data = addressService.getByUid(uid);
		// 响应
		return new JsonResult<>(SUCCESS,data);
	}
	
	@RequestMapping("{aid}/set_default")
	public JsonResult<Void> setDefault(
		@PathVariable("aid")Integer aid,
			HttpSession session){
		// 从Session中获取uid和username
		Integer uid = getUidFromSession(session);
		String username = getUsernameFromSession(session);
		// 调用业务层对象执行设置默认
		addressService.setDefault(aid, uid, username);
		// 响应成功
		return new JsonResult<>(SUCCESS);
			
	}
	
	@RequestMapping("{aid}/delete")
	public JsonResult<Void> delete(
		@PathVariable("aid")Integer aid,
			HttpSession session){
		// 从Session中获取uid和username
		Integer uid = getUidFromSession(session);
		String username = getUsernameFromSession(session);
		// 调用业务层对象执行设置默认
		addressService.delete(aid, uid, username);
		// 响应成功
		return new JsonResult<>(SUCCESS);
			
	}
	
}
