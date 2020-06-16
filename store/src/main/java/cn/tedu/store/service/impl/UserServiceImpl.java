package cn.tedu.store.service.impl;

import java.util.Date;
import java.util.UUID;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import cn.tedu.store.entity.User;
import cn.tedu.store.mapper.UserMapper;
import cn.tedu.store.service.IUserService;
import cn.tedu.store.service.ex.InsertException;
import cn.tedu.store.service.ex.PasswordNotMatchException;
import cn.tedu.store.service.ex.UpdateException;
import cn.tedu.store.service.ex.UserNotFoundException;
import cn.tedu.store.service.ex.UsernameDuplicateException;

@Service
public class UserServiceImpl implements IUserService {
	
	@Autowired
	private UserMapper userMapper;

	@Override
	public void reg(User user) throws UsernameDuplicateException, InsertException {
		// 根据参数user对象中的username属性查询数据：userMapper.findByUsername()
		String username = user.getUsername();
		User result = userMapper.findByUsername(username);
		// 判断查询结果是否不为null（查询结果是存在的）
		if (result != null) {
			// 是：用户名已被占用，抛出UsernameDuplicateException
			throw new UsernameDuplicateException(
				"注册失败！尝试注册的用户名(" + username + ")已经被占用！");
		}

		// 得到盐值
		System.err.println("reg() > password=" + user.getPassword());
		String salt = UUID.randomUUID().toString().toUpperCase();
		// 基于参数user对象中的password进行加密，得到加密后的密码
		String md5Password = getMd5Password(user.getPassword(), salt);
		// 将加密后的密码和盐值封装到user中
		user.setSalt(salt);
		user.setPassword(md5Password);
		System.err.println("reg() > salt=" + salt);
		System.err.println("reg() > md5Password=" + md5Password);
		
		// 将user中的isDelete设置为0
		user.setIsDelete(0);

		// 封装user中的4个日志属性
		Date now = new Date();
		user.setCreatedUser(username);
		user.setCreatedTime(now);
		user.setModifiedUser(username);
		user.setModifiedTime(now);

		// 执行注册：userMapper.insert(user)
		Integer rows = userMapper.insert(user);
		if (rows != 1) {
			throw new InsertException(
				"注册失败！写入数据时出现未知错误！请联系系统管理员！");
		}
	}
	
	@Override
	public User login(String username, String password) throws UserNotFoundException, PasswordNotMatchException {
		// 根据参数username执行查询用户数据
		User result = userMapper.findByUsername(username);
		// 判断查询结果是否为null
		if (result == null) {
			// 抛出：UserNotFoundException
			throw new UserNotFoundException(
				"登录失败！用户名不存在！");
		}

		// 判断查询结果中的isDelete为1
		if (result.getIsDelete() == 1) {
			// 抛出：UserNotFoundException
			throw new UserNotFoundException(
				"登录失败！用户名不存在！");
		}

		// 从查询结果中获取盐值
		String salt = result.getSalt();
		// 根据参数password和盐值一起进行加密，得到加密后的密码
		String md5Password = getMd5Password(password, salt);
		// 判断查询结果中的password和以上加密后的密码是否不一致
		if (!result.getPassword().equals(md5Password)) {
			// 抛出：PasswordNotMatchException
			throw new PasswordNotMatchException(
				"登录失败！密码错误！");
		}

		// 将查询结果中的password、salt、isDelete设置为null
		result.setPassword(null);
		result.setSalt(null);
		result.setIsDelete(null);
		// 返回查询结果
		return result;
	}
	
	@Override
	public void changePassword(Integer uid, String username, String oldPassword, String newPassword)
			throws UserNotFoundException, PasswordNotMatchException, UpdateException {
		System.err.println("changePassword() ---> BEGIN:");
		System.err.println("changePassword() 原密码=" + oldPassword);
		System.err.println("changePassword() 新密码=" + newPassword);
		
		// 根据参数uid查询用户数据
		User result = userMapper.findByUid(uid);
		// 判断查询结果是否为null
		if (result == null) {
			// 抛出：UserNotFoundException
			throw new UserNotFoundException(
				"修改密码失败！用户名不存在！");
		}

		// 判断查询结果中的isDelete为1
		if (result.getIsDelete() == 1) {
			// 抛出：UserNotFoundException
			throw new UserNotFoundException(
				"修改密码失败！用户名不存在！");
		}

		// 从查询结果中获取盐值
		String salt = result.getSalt();
		// 根据参数oldPassword和盐值一起进行加密，得到加密后的密码
		String oldMd5Password = getMd5Password(oldPassword, salt);
		System.err.println("changePassword() 盐值=" + salt);
		System.err.println("changePassword() 原密码加密=" + oldMd5Password);
		System.err.println("changePassword() 正确密码=" + result.getPassword());
		// 判断查询结果中的password和以上加密后的密码是否不一致
		if (!result.getPassword().equals(oldMd5Password)) {
			// 抛出：PasswordNotMatchException
			throw new PasswordNotMatchException(
				"修改密码失败！原密码错误！");
		}

		// 根据参数newPassword和盐值一起进行加密，得到加密后的密码
		String newMd5Password = getMd5Password(newPassword, salt);
		System.err.println("changePassword() 新密码加密=" + newMd5Password);
		// 创建当前时间对象
		Date now = new Date();
		// 执行更新密码，并获取返回的受影响的行数
		Integer rows = userMapper.updatePassword(uid, newMd5Password, username, now);
		// 判断受影响的行数是否不为1
		if (rows != 1) {
			// 抛出：UpdateException
			throw new UpdateException(
				"修改密码失败！更新密码时出现未知错误！");
		}
		
		System.err.println("changePassword() ---> END.");
	}
	
	@Override
	public void changeAvatar(Integer uid, String username, String avatar)
			throws UserNotFoundException, PasswordNotMatchException, UpdateException {
		// 根据参数uid查询用户数据
		User result = userMapper.findByUid(uid);
		// 判断查询结果是否为null
		if (result == null) {
			// 抛出：UserNotFoundException
			throw new UserNotFoundException(
				"修改密码失败！用户名不存在！");
		}

		// 判断查询结果中的isDelete为1
		if (result.getIsDelete() == 1) {
			// 抛出：UserNotFoundException
			throw new UserNotFoundException(
				"修改密码失败！用户名不存在！");
		}
		
		// 创建当前时间对象
		Date now = new Date();
		// 执行更新头像，并获取返回的受影响的行数
		Integer rows = userMapper.updateAvatar(uid, avatar, username, now);
		// 判断受影响的行数是否不为1
		if (rows != 1) {
			// 抛出：UpdateException
			throw new UpdateException(
				"修改密码失败！更新密码时出现未知错误！");
		}
		
	}
	
	@Override
	public User getByUid(Integer uid) {
		// 根据uid查询用户数据
		User result = userMapper.findByUid(uid);
		
		// 如果查询到数据，则需要将查询结果中的password,salt,isDelete设为null
		if(result != null) {
			result.setPassword(null);
			result.setSalt(null);
			result.setIsDelete(null);
		}
		
		//将查询结果返回
		return result;
	}
		
	@Override
	public void changeInfo(User user) 
			throws UserNotFoundException,UpdateException{
		// 根据参数user中的uid，即user.getUid()查询数据
		User result = userMapper.findByUid(user.getUid());
		// 检查查询结果是否存在，是否标记为删除
		// 判断查询结果是否为null
		if (result == null) {
			// 抛出：UserNotFoundException
			throw new UserNotFoundException(
				"修改个人资料失败！用户数据不存在！");
		}

		// 判断查询结果中的isDelete为1
		if (result.getIsDelete() == 1) {
			// 抛出：UserNotFoundException
			throw new UserNotFoundException(
				"修改个人资料失败！用户数据不存在！");
		}	
		// 创建当前时间对象	
		Date now = new Date();
		// 将时间封装到参数user中
		user.setModifiedUser(user.getUsername());
		user.setModifiedTime(now);
		// 执行修改个人资料
		Integer rows = userMapper.updateInfo(user);
		// 判断以上修改时的返回值是否不为1
		if (rows !=1) {
			//抛出:UpdateException
			throw new UpdateException(
					"修改个人资料失败！更新用户数据时出现位置错误！");
		}
	}

	/**
	 * 对密码进行加密
	 * @param password 原始密码
	 * @param salt 盐值
	 * @return 加密后的密码
	 */
	private String getMd5Password(String password, String salt) {
		// 规则：对password+salt进行3重加密
		String str = password + salt;
		for (int i = 0; i < 3; i++) {
			str = DigestUtils
				.md5DigestAsHex(str.getBytes()).toUpperCase();
		}
		return str;
	}

	


	

	
}



