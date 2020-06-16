package cn.tedu.store.service.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.tedu.store.entity.Cart;
import cn.tedu.store.mapper.CartMapper;
import cn.tedu.store.service.ICartService;
import cn.tedu.store.service.ex.AccessDeniedException;
import cn.tedu.store.service.ex.CartNotFoundException;
import cn.tedu.store.service.ex.InsertException;
import cn.tedu.store.service.ex.UpdateException;
import cn.tedu.store.vo.CartVO;


/**
 * 购物车数据的业务层实现类
 */
@Service
public class CartServiceImpl implements ICartService {

	@Autowired
	private CartMapper cartMapper;

	@Override
	public void addToCart(Cart cart, Integer uid, String username) throws InsertException, UpdateException {
		// 创建时间对象
		Date now = new Date();
		
		// 根据参数cart中封装的uid和gid执行查询
		Cart result = findByUidAndGid(uid, cart.getGid());
		// 检查查询结果是否为null
		if(result == null) {
			// 是：
			// 基于参数uid向参数cart中封装uid
			cart.setUid(uid);
			// 基于参数username向参数cart中封装createdUser和modifiedUser
			cart.setCreatedUser(username);
			cart.setModifiedUser(username);
			// 向参数cart中封装createdTime和modifiedTime
			cart.setCreatedTime(now);
			cart.setModifiedTime(now);
			// 执行插入数据
			insert(cart);
		}else {
			// 否：updateNum(cid, num, modifiedUser, modifiedTime);
			// 从查询结果中获取cid
			Integer cid = result.getCid();
			// 从查询结果中获取num，它是商品的原数量
			Integer oldNum = result.getNum();
			// 将以上获取的原数量与参数cart中的num相加，得到新的数量
			Integer newNum = oldNum + cart.getNum();
			// 执行修改数量
			updateNum(cid, newNum, username, now);
		}
	}
	
	@Override
	public Integer add(Integer cid, Integer uid, String username) throws CartNotFoundException, AccessDeniedException, UpdateException {
		// 根据参数cid查询购物车数据
		Cart result = findByCid(cid);
		// 判断查询结果是否为null
		if(result == null) {
			// 是:抛出CartNotFoundException
			throw new CartNotFoundException(
					"增加数量失败!尝试访问的购物车数据不存在!");
		}
		// 判断查询结果中的uid与参数uid是否不匹配
		if(result.getUid() != uid) {
			// 是:抛出AccessDeniedExcetion
			throw new AccessDeniedException(
					"增加数量失败!非法访问!");
		}
		// 从查询结果中取出num，增加1，得到新的数量
		Integer newNum = result.getNum()+1;
		// 更新商品数量
		updateNum(cid, newNum, username, new Date());
		// 返回
		return newNum;
	}
	
	@Override
	public List<CartVO> getByUid(Integer uid) {
		return findByUid(uid);
	}

	@Override
	public List<CartVO> getByCids(Integer[] cids, Integer uid) {
		// 获取数据
		List<CartVO> results = findByCids(cids);
		// 检查数据归属，将去除归属错误的数据
		Iterator<CartVO> it = results.iterator();
		while (it.hasNext()) {
			if(it.next().getUid() != uid) {
				it.remove();
			}
		}
		// 返回
		return results;
	}
	
	/**
	 * 插入购物车数据
	 * @param cart 购物车数据
	 * @throws InsertException 插入数据异常
	 */
	private void insert(Cart cart) throws InsertException{
		Integer rows = cartMapper.insert(cart);
		if (rows != 1) {
			throw new InsertException(
					"将商品添加到购物车失败!插入数据时出现未知错误!");
		}
	}
	
	/**
	 * 修改购物车中商品的数量
	 * @param cid 购物车数据的id
	 * @param num 新的商品数量
	 * @param modifiedUser 修改执行人
	 * @param modifiedTime 修改时间
	 * @throws UpdateException 插入数据异常
	 */
	private void updateNum(Integer cid,Integer num,
			String modifiedUser,Date modifiedTime)
				throws UpdateException{
		Integer rows = cartMapper.updateNum(cid, num, modifiedUser, modifiedTime);
		if(rows != 1) {
			throw new UpdateException(
					"更新商品数量失败!更新数据时出现未知错误!");
		}
	}
	
	/**
	 * 根据用户id和商品id查询购物车数据
	 * @param uid 用户id
	 * @param gid 商品id
	 * @return 匹配的购物车数据，如果没有匹配的数据，则返回null
	 */
	private Cart findByUidAndGid(Integer uid,Long gid) {
		return cartMapper.findByUidAndGid(uid, gid);
	}
	
	/**
	 * 获取某用户的购物车数据列表
	 * @param uid 用户的id
	 * @return 该用户的购物车数据列表
	 */
	private List<CartVO> findByUid(Integer uid) {
		return cartMapper.findByUid(uid);
	}

	/**
	 * 根据购物车数据id查询购物车数据详情
	 * @param cid 购物车数据id
	 * @return 匹配的购物车数据，如果没有匹配的数据，则返回null
	 */
	private Cart findByCid(Integer cid) {
		return cartMapper.findByCid(cid);
	}

	/**
	 * 根据数据id获取购物车数据列表
	 * @param cids 用户的id
	 * @return 匹配的购物车数据列表
	 */
	List<CartVO> findByCids(Integer[] cids) {
		return cartMapper.findByCids(cids);
	}


	
}
