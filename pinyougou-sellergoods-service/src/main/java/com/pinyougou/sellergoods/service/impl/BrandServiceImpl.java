package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {

    @Autowired
    private TbBrandMapper brandMapper;

    @Override
    public List <TbBrand> findAll() {
        return brandMapper.selectByExample ( null );
    }

    @Override
    public PageResult <TbBrand> findPage(int pageNum, int pageSize) {
        //运用分页插件
        PageHelper.startPage ( pageNum, pageSize );

        Page <TbBrand> page = (Page <TbBrand>) brandMapper.selectByExample ( null );

        return new PageResult <> ( page.getTotal (), page.getResult () );
    }

    @Override
    public void add(TbBrand brand) {
        brandMapper.insert ( brand );
    }

    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            brandMapper.deleteByPrimaryKey ( id );
        }
    }

    @Override
    public void update(TbBrand brand) {
        brandMapper.updateByPrimaryKey ( brand );
    }

    @Override
    public TbBrand findOne(Long id) {
        return brandMapper.selectByPrimaryKey ( id );
    }

    @Override
    public PageResult <TbBrand> findPage(TbBrand brand, int pageNum, int pageSize) {
        //运用分页插件
        PageHelper.startPage ( pageNum, pageSize );

        TbBrandExample example = null;

        if (brand != null) {
            example = new TbBrandExample ();
            TbBrandExample.Criteria criteria = example.createCriteria ();
            if (brand.getName () != null && brand.getName ().trim () != "") {
                criteria.andNameLike ( "%" + brand.getName () + "%" );
            }

            if (brand.getFirstChar () != null && brand.getFirstChar ().trim () != null) {
                criteria.andFirstCharLike ( "%" + brand.getFirstChar () + "%" );
            }
        }


        Page <TbBrand> page = (Page <TbBrand>) brandMapper.selectByExample ( example );

        return new PageResult <> ( page.getTotal (), page.getResult () );
    }

    @Override
    public List <Map> getBrandOptionList() {
        return brandMapper.getBrandOptionList ();
    }
}
