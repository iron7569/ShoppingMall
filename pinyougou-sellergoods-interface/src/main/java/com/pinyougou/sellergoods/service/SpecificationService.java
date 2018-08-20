package com.pinyougou.sellergoods.service;
import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbSpecification;

import com.pinyougou.pojogroup.Specification;
import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SpecificationService {

	/**
	 * 返回全部列表
	 * @return
	 */
	List<TbSpecification> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
	void add(Specification specification);
	
	
	/**
	 * 修改
	 */
	void update(Specification specification);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	Specification findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	PageResult findPage(TbSpecification specification, int pageNum, int pageSize);


	/**
	 * 获取规格详情列表,用于模板页下拉框展示
	 * @return map集合的lis列表 id  text(spec_name)
	 */
	List<Map> getSpecificationOptionList();
	
}
