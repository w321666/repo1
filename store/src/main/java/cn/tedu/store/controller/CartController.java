package cn.tedu.store.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.tedu.store.entity.Cart;
import cn.tedu.store.service.ICartService;
import cn.tedu.store.util.JsonResult;
import cn.tedu.store.vo.CartVO;

@RestController
@RequestMapping("carts")
public class CartController extends BaseController{

	@Autowired
	private ICartService cartService;

	@RequestMapping("add_to_cart")
	public JsonResult<Void> addToCart(Cart cart,HttpSession session){
		// 从Session中获取uid和username
		Integer uid = getUidFromSession(session);
		String username = getUsernameFromSession(session);
		// 调用业务层对象执行加入购物车
		cartService.addToCart(cart, uid, username);
		// 响应成功
		return new JsonResult<>(SUCCESS);
	}
	
	@GetMapping("/")
	public JsonResult<List<CartVO>> getByUid(
			HttpSession session){
		// 从session中获取uid
		Integer uid = getUidFromSession(session);
		// 调用业务层对象获取数据
		List<CartVO> data
			= cartService.getByUid(uid);
		// 响应
		return new JsonResult<>(SUCCESS,data);
	}
	
	@RequestMapping("{cid}/add")
	public JsonResult<Integer> add(
		@PathVariable("cid") Integer cid,
		HttpSession session){
		// 从session中获取uid和username
		Integer uid = getUidFromSession(session);
		String username = getUsernameFromSession(session);
		// 执行
		Integer data = cartService.add(cid, uid, username);
		// 响应
		return new JsonResult<>(SUCCESS,data);
	}

	@GetMapping("get_by_cids")
	public JsonResult<List<CartVO>> getByCids(
			Integer[] cids,HttpSession session){
		// 从session中获取uid
		Integer uid = getUidFromSession(session);
		// 执行
		List<CartVO> data = cartService.getByCids(cids,uid);
		// 响应
		return new JsonResult<>(SUCCESS,data);
	}
	
}
