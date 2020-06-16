package cn.tedu.store.service.impl;


import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.tedu.store.entity.Address;
import cn.tedu.store.entity.District;
import cn.tedu.store.mapper.AddressMapper;
import cn.tedu.store.mapper.DistrictMapper;
import cn.tedu.store.service.IAddressService;
import cn.tedu.store.service.IDistrictService;
import cn.tedu.store.service.ex.AccessDeniedException;
import cn.tedu.store.service.ex.AddressCountLimitException;
import cn.tedu.store.service.ex.AddressNotFoundException;
import cn.tedu.store.service.ex.DeleteException;
import cn.tedu.store.service.ex.InsertException;
import cn.tedu.store.service.ex.UpdateException;

@Service
public class AddressServiceImpl implements IAddressService {

	@Autowired
	private AddressMapper addressMapper;
	@Autowired
	private IDistrictService districtService;
	
	@Override
	public void addnew(Address address, Integer uid, String username)
			throws AddressCountLimitException, InsertException {
		// 根据参数uid查询当前用户的收货地址数量
		Integer count = countByUid(uid);
		// 判断收货地址数量是否达到上限值ADDRESS_MAX_COUNT
		if(count >=ADDRESS_MAX_COUNT) {
			// 是：抛出：AddressCountLimitException
			throw new AddressCountLimitException(
					"增加收货地址失败!当前收货地址数量已经达到上限!最多允许创建" + ADDRESS_MAX_COUNT + "条，已经创建" + count + "条!");
		}
		
		// 补全数据：uid
		address.setUid(uid);

		// 补全数据：province_name, city_name, area_name
		District province = districtService.getByCode(address.getProvinceCode());
		District city = districtService.getByCode(address.getCityCode());
		District area = districtService.getByCode(address.getAreaCode());
		address.setProvinceName(province == null ? "NULL" : province.getName());
		address.setCityName(city == null ? "NULL" : city.getName());
		address.setAreaName(area == null ? "NULL" : area.getName());

		// 判断当前用户的收货地址数量是否为0，并决定is_default的值
		Integer isDefault = count == 0 ? 1 : 0;
		// 补全数据：is_default
		address.setIsDefault(isDefault);
		
		// 创建当前时间对象
		Date now = new Date();
		// 补全数据：4个日志
		address.setCreatedUser(username);
		address.setCreatedTime(now);
		address.setModifiedUser(username);
		address.setModifiedTime(now);
		// 插入收货地址数据
		insert(address);
	}
	
	@Override
	public List<Address> getByUid(Integer uid) {
		return findByUid(uid);
	}


	@Override
	public Address getByAid(Integer aid) {
		return findByAid(aid);
	}

	@Override
	@Transactional
	public void setDefault(Integer aid, Integer uid, String username)
			throws AddressNotFoundException, AccessDeniedException, UpdateException {
		// 根据aid查询收货地址数据
		Address result = findByAid(aid);
		
		// 判断结果是否为null
		if(result == null) {
			// 是:抛出AddressNotFoundException
			throw new AddressNotFoundException(
					"设置默认收货地址失败!尝试操作的数据不存在!");
		}
		
		// 判断结果中的uid与参数uid是否一致
		if(result.getUid() != uid) {
			// 是:抛出AccessDeniedException
			throw new AccessDeniedException(
					"设置默认收货地址失败!不允许访问他人的数据!");
		}
		
		// 将该用户所有收货地址设置为非默认
		updateNonDefault(uid);
		
		// 将指定的收货地址设置为默认
		updateDefault(aid, username, new Date());
	}

	@Override
	@Transactional
	public void delete(Integer aid, Integer uid, String username)
			throws AddressNotFoundException, AccessDeniedException, DeleteException, UpdateException {
		// 根据aid查询收货地址数据
		Address result = findByAid(aid);
		
		// 判断结果是否为null
		if(result == null) {
			// 是:抛出AddressNotFoundException
			throw new AddressNotFoundException(
					"删除收货地址失败!尝试操作的数据不存在!");
		}
		
		// 判断结果中的uid与参数uid是否一致
		if(result.getUid() != uid) {
			// 是:抛出AccessDeniedException
			throw new AccessDeniedException(
					"删除收货地址失败!不允许访问他人的数据!");
		}
		// 执行删除
		deleteByAid(aid);
		
		// 判断此前的查询结果中的isDefault是否为0
		if (result.getIsDefault() == 0) {
			// return;
			return;
		}		
		
		// 统计当前用户的收货地址数量:countByUid(uid)
		Integer count = countByUid(uid);
		// 判断剩余收货地址数量是否为0
		if(count == 0) {
			// return;
			return;
		}
		
		// 查询当前用户最近修改的收货地址
		Address lastmodifiedaAddress = findLastModified(uid);
		// 将最近修改的收货地址设置为默认
		updateDefault(lastmodifiedaAddress.getAid(), username, new Date());
	}
	
	/**
	 * 插入收货地址数据
	 * @param address 收货地址数据
	 * @throws InsertException 插入数据异常
	 */
	private void insert(Address address)throws InsertException{
		Integer rows = addressMapper.insert(address);
		if(rows!=1) {
			throw new InsertException(
					"增加收货地址失败!插入数据时出现未知错误!");
		}
	}
	
	/**
	 * 根据收货地址id删除数据
	 * @param aid 收货地址id
	 * @DeleteException 删除数据异常
	 */
	private void deleteByAid(Integer aid) throws DeleteException {
		Integer rows = addressMapper.deleteByAid(aid);
		if (rows != 1) {
			throw new DeleteException(
					"删除收货地址失败!删除时出现未知错误");
		}
	}
	
	/**
	 * 将某用户的所有收货地址设置为非默认
	 * @param uid 用户的id
	 * @throws UpdateException 更新数据异常
	 */
	private void updateNonDefault(Integer uid)throws UpdateException {
		Integer rows = addressMapper.updateNonDefault(uid);
		if (rows == 0) {
			throw new UpdateException(
					"设置默认收货地址失败!更新时出现位置错误!");
		}
	}
	
	/**
	 * 将指定的收货地址设置为默认
	 * @param aid 收货地址的数据id
	 * @param modifiedUser 修改执行人
	 * @param modifiedTime 修改时间
	 * @throws UpdateException 更新数据异常
	 */
	private void updateDefault(Integer aid,
			String modifiedUser, Date modifiedTime) 
				throws UpdateException{
		Integer rows = addressMapper.updateDefault(aid, modifiedUser, modifiedTime);
		if (rows != 1) {
			throw new UpdateException(
					"设置默认收货地址失败!更新时出现位置错误!");
		}
	}
	
	/**
	 * 统计某个用户的收货地址数据的数量
	 * @param uid 用户的id
	 * @return 该用户的收货地址数据的数量
	 */
	private Integer countByUid(Integer uid) {
		return addressMapper.countByUid(uid);
	}

	/**
	 * 根据省/市/区的代号查询详情
	 * @param code 省/市/区的代号
	 * @return 匹配的省/市/区的详情，如果没有匹配的数据，则返回null
	 */
	private District findByCode(String code) {
		return districtService.getByCode(code);
	}
	
	/**
	 * 根据用户id查询该用户的收货地址列表
	 * @param uid 用户id
	 * @return 该用户的收货地址列表
	 */
	private List<Address> findByUid(Integer uid) {
		return addressMapper.findByUid(uid);
	}
	
	/**
	 * 根据收货地址的数据id查询详情
	 * @param aid 收货地址的数据id
	 * @return 匹配的收货地址的详情，如果没有匹配的数据，则返回null
	 */
	private Address findByAid(Integer aid) {
		return addressMapper.findByAid(aid);
	}
	
	/**
	 * 查询某用户最后一次修改的收货数据
	 * @param uid 用户的id
	 * @return 匹配的收货地址的详情
	 */
	private Address findLastModified(Integer uid) {
		return addressMapper.findLastModified(uid);
	}


}
