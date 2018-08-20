package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pinyougou.pojogroup.Specification;
import com.pinyougou.sellergoods.service.SpecificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationExample;
import com.pinyougou.pojo.TbSpecificationExample.Criteria;

import entity.PageResult;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private TbSpecificationMapper specificationMapper;

    @Autowired
    private TbSpecificationOptionMapper specificationOptionMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger ( SpecificationServiceImpl.class );

    /**
     * 查询全部
     */
    @Override
    public List <TbSpecification> findAll() {
        return specificationMapper.selectByExample ( null );
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage ( pageNum, pageSize );
        Page <TbSpecification> page = (Page <TbSpecification>) specificationMapper.selectByExample ( null );
        return new PageResult ( page.getTotal (), page.getResult () );
    }

    /**
     * 增加
     */
    @Override
    public void add(Specification specification) {
        TbSpecification tbSpecification = specification.getSpecification ();
        specificationMapper.insert ( tbSpecification );

        for (TbSpecificationOption tbSpecificationOption : specification.getSpecificationOptionList ()) {
            //关联
            tbSpecificationOption.setSpecId ( tbSpecification.getId () );

            specificationOptionMapper.insert ( tbSpecificationOption );
        }
    }


    /**
     * 修改
     */
    @Override
    public void update(Specification specification) {
        specificationMapper.updateByPrimaryKey ( specification.getSpecification () );

        TbSpecificationOptionExample example = new TbSpecificationOptionExample ();
        example.createCriteria ().andSpecIdEqualTo ( specification.getSpecification ().getId () );

        int i = specificationOptionMapper.deleteByExample ( example );
        LOGGER.info ( "删除关联规格详情数据:"+i+"条" );

        //重新插入
        for (TbSpecificationOption tbSpecificationOption : specification.getSpecificationOptionList ()) {
            tbSpecificationOption.setSpecId ( specification.getSpecification ().getId () );
            specificationOptionMapper.insert ( tbSpecificationOption );
        }
//        for (TbSpecificationOption tbSpecificationOption : specification.getSpecificationOptionList ()) {
//            if (tbSpecificationOption.getId () == null) {
//                //保存
//                tbSpecificationOption.setSpecId ( specification.getSpecification ().getId () );
//                specificationOptionMapper.insert ( tbSpecificationOption );
//            } else {
//                //修改
//                specificationOptionMapper.updateByPrimaryKey ( tbSpecificationOption );
//            }
//        }
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Specification findOne(Long id) {
        TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey ( id );

        TbSpecificationOptionExample example = new TbSpecificationOptionExample ();
        example.createCriteria ().andSpecIdEqualTo ( tbSpecification.getId () );
        List <TbSpecificationOption> options = specificationOptionMapper.selectByExample ( example );
        Specification specification = new Specification ();
        specification.setSpecification ( tbSpecification );
        specification.setSpecificationOptionList ( options );
        return specification;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            //删除规格表中数据
            specificationMapper.deleteByPrimaryKey ( id );

            //获取关联的规格详情记录
            TbSpecificationOptionExample example = new TbSpecificationOptionExample ();
            example.createCriteria ().andSpecIdEqualTo ( id );
            int i = specificationOptionMapper.deleteByExample ( example );
            LOGGER.info ( "规格详情删除:"+i+"条关联数据" );
//            //遍历删除
//            for (TbSpecificationOption tbSpecificationOption : list) {
//                specificationOptionMapper.deleteByPrimaryKey ( tbSpecificationOption.getId () );
//            }

        }
    }


    @Override
    public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
        PageHelper.startPage ( pageNum, pageSize );

        TbSpecificationExample example = new TbSpecificationExample ();
        Criteria criteria = example.createCriteria ();

        if (specification != null) {
            if (specification.getSpecName () != null && specification.getSpecName ().length () > 0) {
                criteria.andSpecNameLike ( "%" + specification.getSpecName () + "%" );
            }

        }

        Page <TbSpecification> page = (Page <TbSpecification>) specificationMapper.selectByExample ( example );
        return new PageResult ( page.getTotal (), page.getResult () );
    }

    @Override
    public List <Map> getSpecificationOptionList() {
        return specificationMapper.getSpecificationOptionList ();
    }

}
