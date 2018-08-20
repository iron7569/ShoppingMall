package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

public interface BrandService {

    /**
     * 查询所有品牌接口方法
     * @return 品牌信息列表
     */
    List<TbBrand> findAll();


    /**
     * 分页查询品牌信息
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页数据
     */
    PageResult<TbBrand> findPage(int pageNum , int pageSize);

    /**
     * 添加品牌
     * @param brand 品牌对象
     */
    void add(TbBrand brand);

    /**
     * 批量删除品牌
     * @param ids id数组
     */
    void delete(Long[] ids);

    /**
     * 更新品牌信息
     * @param brand 品牌信息,包含id
     */
    void update(TbBrand brand);

    /**
     * 根据id查询品牌
     * @param id 品牌id
     * @return 返回品牌对象
     */
    TbBrand findOne(Long id);


    /**
     * 带条件分页查询品牌信息
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param brand  查询条件
     * @return 分页数据
     */
    PageResult<TbBrand> findPage(TbBrand brand, int pageNum , int pageSize);

    /**
     * 获取所有品牌详情信息,用map封装id name
     * @return list集合
     */
    List<Map> getBrandOptionList();
}
