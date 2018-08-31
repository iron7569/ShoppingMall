package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemCatExample;
import com.pinyougou.pojo.TbItemCatExample.Criteria;
import com.pinyougou.sellergoods.service.ItemCatService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class ItemCatServiceImpl implements ItemCatService {

    @Autowired
    private TbItemCatMapper itemCatMapper;

    /**
     * 查询全部
     */
    @Override
    public List <TbItemCat> findAll() {
        return itemCatMapper.selectByExample ( null );
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage ( pageNum, pageSize );
        Page <TbItemCat> page = (Page <TbItemCat>) itemCatMapper.selectByExample ( null );
        return new PageResult ( page.getTotal (), page.getResult () );
    }

    /**
     * 增加
     */
    @Override
    public void add(TbItemCat itemCat) {
        itemCatMapper.insert ( itemCat );
    }


    /**
     * 修改
     */
    @Override
    public void update(TbItemCat itemCat) {
        itemCatMapper.updateByPrimaryKey ( itemCat );
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbItemCat findOne(Long id) {
        return itemCatMapper.selectByPrimaryKey ( id );
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) throws Exception{
        for (Long id : ids) {
            //先查询,查看当前分类有无子分类
            List <TbItemCat> list = findByParentId ( id );
            if (list.size () != 0) {
                throw new RuntimeException ( "所选分类无法删除(包含子分类)" );
            } else {
                itemCatMapper.deleteByPrimaryKey ( id );
            }
        }
    }


    @Override
    public PageResult findPage(TbItemCat itemCat, int pageNum, int pageSize) {
        PageHelper.startPage ( pageNum, pageSize );

        TbItemCatExample example = new TbItemCatExample ();
        Criteria criteria = example.createCriteria ();

        if (itemCat != null) {
            if (itemCat.getName () != null && itemCat.getName ().length () > 0) {
                criteria.andNameLike ( "%" + itemCat.getName () + "%" );
            }

        }

        Page <TbItemCat> page = (Page <TbItemCat>) itemCatMapper.selectByExample ( example );
        return new PageResult ( page.getTotal (), page.getResult () );
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List <TbItemCat> findByParentId(Long parentId) {
        TbItemCatExample example = new TbItemCatExample ();
        example.createCriteria ().andParentIdEqualTo ( parentId );

        //缓存处理
        //根据商品分类名称缓存模板id
        List <TbItemCat> itemCatList = findAll ();
        for (TbItemCat itemCat : itemCatList) {
            redisTemplate.boundHashOps ( "itemCat" ).put ( itemCat.getName (),itemCat.getTypeId () );
            System.out.println ("根据商品分类名称缓存模板id");
        }

        return itemCatMapper.selectByExample ( example );
    }

}
