package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.mapper.TbTypeTemplateMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.pojo.TbTypeTemplateExample;
import com.pinyougou.pojo.TbTypeTemplateExample.Criteria;
import com.pinyougou.sellergoods.service.TypeTemplateService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

    @Autowired
    private TbTypeTemplateMapper typeTemplateMapper;

    /**
     * 查询全部
     */
    @Override
    public List <TbTypeTemplate> findAll() {
        return typeTemplateMapper.selectByExample ( null );
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage ( pageNum, pageSize );
        Page <TbTypeTemplate> page = (Page <TbTypeTemplate>) typeTemplateMapper.selectByExample ( null );
        return new PageResult ( page.getTotal (), page.getResult () );
    }

    /**
     * 增加
     */
    @Override
    public void add(TbTypeTemplate typeTemplate) {
        typeTemplateMapper.insert ( typeTemplate );
    }


    /**
     * 修改
     */
    @Override
    public void update(TbTypeTemplate typeTemplate) {
        typeTemplateMapper.updateByPrimaryKey ( typeTemplate );
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbTypeTemplate findOne(Long id) {
        return typeTemplateMapper.selectByPrimaryKey ( id );
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            typeTemplateMapper.deleteByPrimaryKey ( id );
        }
    }


    @Override
    public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
        PageHelper.startPage ( pageNum, pageSize );

        TbTypeTemplateExample example = new TbTypeTemplateExample ();
        Criteria criteria = example.createCriteria ();

        if (typeTemplate != null) {
            if (typeTemplate.getName () != null && typeTemplate.getName ().length () > 0) {
                criteria.andNameLike ( "%" + typeTemplate.getName () + "%" );
            }
            if (typeTemplate.getSpecIds () != null && typeTemplate.getSpecIds ().length () > 0) {
                criteria.andSpecIdsLike ( "%" + typeTemplate.getSpecIds () + "%" );
            }
            if (typeTemplate.getBrandIds () != null && typeTemplate.getBrandIds ().length () > 0) {
                criteria.andBrandIdsLike ( "%" + typeTemplate.getBrandIds () + "%" );
            }
            if (typeTemplate.getCustomAttributeItems () != null && typeTemplate.getCustomAttributeItems ().length () > 0) {
                criteria.andCustomAttributeItemsLike ( "%" + typeTemplate.getCustomAttributeItems () + "%" );
            }

        }

        Page <TbTypeTemplate> page = (Page <TbTypeTemplate>) typeTemplateMapper.selectByExample ( example );

        //缓存处理
        saveToRedis ();

        return new PageResult ( page.getTotal (), page.getResult () );
    }

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 缓存数据:根据模板id缓存品牌列表
     */
    private void saveToRedis(){
        //查询所有模板数据
        List <TbTypeTemplate> templateList = findAll ();
        for (TbTypeTemplate typeTemplate : templateList) {
            List list = JSON.parseArray ( typeTemplate.getBrandIds (),Map.class );
            //存储品牌列表
            redisTemplate.boundHashOps ( "brandList" ).put ( typeTemplate.getId (), list);

            //调用方法查询规格列表
            List <Map> specList = getSpecList ( typeTemplate.getId () );
            //缓存规格列表
            redisTemplate.boundHashOps ( "specList" ).put ( typeTemplate.getId (), specList);

        }
        System.out.println ("缓存数据:根据模板id缓存品牌列表");
        System.out.println ("缓存数据:根据模板id缓存规格列表");
    }

    @Override
    public List <Map> getTemplateList() {
        return typeTemplateMapper.getTemplateList ();
    }

    @Autowired
    private TbSpecificationOptionMapper specificationOptionMapper;

    @Override
    public List <Map> getSpecList(Long id) {
        //根据id查询模板对象
        TbTypeTemplate template = typeTemplateMapper.selectByPrimaryKey ( id );
        //获得规格specIds属性  [{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
        String specIds = template.getSpecIds ();
        //转换成集合
        List <Map> list = JSON.parseArray ( specIds, Map.class );
        //遍历,添加属性
        for (Map map : list) {
            //获取规格id
            Object i = map.get ( "id" );
            //查询规格详情
            TbSpecificationOptionExample example = new TbSpecificationOptionExample ();
            example.createCriteria ().andSpecIdEqualTo ( new Long ( (Integer) i ) );
            List <TbSpecificationOption> options = specificationOptionMapper.selectByExample ( example );

            map.put ( "options",options );
        }
        return list;
    }

}
