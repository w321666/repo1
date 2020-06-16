package cn.tedu.store.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cn.tedu.store.controller.ex.FileEmptyException;
import cn.tedu.store.controller.ex.FileSizeException;
import cn.tedu.store.controller.ex.FileTypeException;
import cn.tedu.store.controller.ex.FileUploadIOException;
import cn.tedu.store.controller.ex.FileUploadStateException;
import cn.tedu.store.entity.User;
import cn.tedu.store.service.IUserService;
import cn.tedu.store.util.JsonResult;

/**
 * 处理用户数据相关请求的控制器类
 */
@RestController
@RequestMapping("users")
public class UserController extends BaseController {

	/**
	 * 上传的头像的最大大小
	 */
	public static final long AVATAR_MAX_SIZE = 2 * 1024 * 1024;
	/**
	 * 上传时允许的头像文件的类型
	 */
	public static final List<String> AVATAR_CONTENT_TYPES = new ArrayList<>();
	/**
	 * 初始化上传时允许的头像文件的类型
	 */
	static {
		AVATAR_CONTENT_TYPES.add("image/jpeg");
		AVATAR_CONTENT_TYPES.add("image/png");
	}
	public static final String AVATAR_DIR = "upload";

	
	
	@Autowired
	private IUserService userService;
	
	@RequestMapping("reg")
	public JsonResult<Void> reg(User user){
		// 执行注册
		userService.reg(user);
		// 响应操作成功
		return new JsonResult<Void>(BaseController.SUCCESS);
	}
	
	@RequestMapping("login")
	public JsonResult<User> login(
		String username,String password,
		HttpSession session){
		// 执行登录，获取登录返回结果
		User user = userService.login(username, password);
		// 向Session中封装数据
		session.setAttribute("uid", user.getUid());
		session.setAttribute("username", user.getUsername());
		// 向客户端响应操作成功
		return new JsonResult<>(SUCCESS,user);
	}
	
	@RequestMapping("change_password")
	public JsonResult<Void> changPassword(
		@RequestParam("old_password")String oldPassword,
		@RequestParam("new_password")String newPassword,
		HttpSession session){
		// 从sessino中获取uid和username
		Integer uid = getUidFromSession(session);
		String username = getUsernameFromSession(session);
		// 执行修改密码
		userService.changePassword(uid, username, oldPassword, newPassword);
		// 响应修改成功
		return new JsonResult<>(SUCCESS);
	}
	
	@GetMapping("get_info")
	public JsonResult<User> getByUid(
			HttpSession session){
		// 从session中获取uid
		Integer uid = getUidFromSession(session);
		// 查询匹配的数据
		User data = userService.getByUid(uid);
		// 响应
		return new JsonResult<>(SUCCESS, data);
	}
	
	@RequestMapping("change_info")
	public JsonResult<Void> changeInfo(
			User user, HttpSession session){
		// 从session中获取uid和username
		Integer uid = getUidFromSession(session);
		String username = getUsernameFromSession(session);
		// 将uid和username封装到user中
		user.setUid(uid);
		user.setUsername(username);
		// 执行修改
		userService.changeInfo(user);
		// 响应
		return new JsonResult<>(SUCCESS);
	}
	
	@PostMapping("change_avatar")
	public JsonResult<String> changeAvatar(
		HttpServletRequest request,
		@RequestParam("file") MultipartFile file){
		// 检查文件是否为空
		if(file.isEmpty()) {
			throw new FileEmptyException(
					"上传失败!请选择有效的文件!");
		}
		
		// 检查文件大小
		if(file.getSize()>AVATAR_MAX_SIZE){
			throw new FileSizeException(
					"上传失败!不允许使用超过" + (AVATAR_MAX_SIZE/1024) + "KB的文件!");
		}
		
		// 检查文件类型
		if(!AVATAR_CONTENT_TYPES.contains(file.getContentType())) {
			throw new FileTypeException(
				"上传失败!仅允许使用以下类型的图片文件: "+ AVATAR_CONTENT_TYPES);
		}
		
		// 确定文件夹
		String dirPath = request.getServletContext().getRealPath(AVATAR_DIR);
		File dir = new File(dirPath);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		
		// 确定文件名
		String originalFilename = file.getOriginalFilename();
		String suffix = "";
		int beginIndex = originalFilename.lastIndexOf(".");
		if(beginIndex !=-1) {
			suffix = originalFilename.substring(beginIndex);
		}
		String filename = UUID.randomUUID().toString() + suffix;
		
		// 执行保存
		File dest = new File(dir,filename);
		try {
			file.transferTo(dest);
		} catch (IllegalStateException e) {
			throw new FileUploadStateException(
					"上传失败!请检查原文件是否存在并可以被访问!");
		} catch (IOException e) {
			throw new FileUploadIOException(
					"上传失败!读取数据时出现未知错误!");
		}
		
		// 更新数据表
		String avatar = "/upload/" + filename;
		HttpSession session = request.getSession();
		Integer uid = getUidFromSession(session);
		String username = getUsernameFromSession(session);
		userService.changeAvatar(uid,username,avatar);
		
		// 返回
		JsonResult<String> jr = new JsonResult<String>();
		jr.setState(SUCCESS);
		jr.setData(avatar);
		return jr;
	}
	
}
