package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    /**
     * 查询全部
     */
    @Override
    public List <TbGoods> findAll() {
        return goodsMapper.selectByExample ( null );
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage ( pageNum, pageSize );
        Page <TbGoods> page = (Page <TbGoods>) goodsMapper.selectByExample ( null );
        return new PageResult ( page.getTotal (), page.getResult () );
    }


    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbBrandMapper brandMapper;

    @Autowired
    private TbSellerMapper sellerMapper;

    @Autowired
    private TbItemMapper itemMapper;

    /**
     * 增加
     */
    @Override
    public void add(Goods goods) {
        //添加商品
        TbGoods tbGoods = goods.getGoods ();
        tbGoods.setAuditStatus ( "0" );
        goodsMapper.insert ( tbGoods );
        //添加商品详情
        TbGoodsDesc tbGoodsDesc = goods.getGoodsDesc ();
        tbGoodsDesc.setGoodsId ( tbGoods.getId () );
        goodsDescMapper.insert ( tbGoodsDesc );

        insetItems ( goods );
    }

    private void insetItems(Goods goods) {
        TbGoods tbGoods = goods.getGoods ();
        TbGoodsDesc tbGoodsDesc = goods.getGoodsDesc ();
        if ("1".equals ( tbGoods.getIsEnableSpec () )) {

            //添加商品明细
            for (TbItem item : goods.getItems ()) {
                //设置标题 商品名+属性
                String title = tbGoods.getGoodsName ();
                Map<String, Object> map = JSON.parseObject ( item.getSpec () );
                for (String key : map.keySet ()) {
                    title += " " + map.get ( key );
                }
                item.setTitle ( title );

                setItemValus ( tbGoods, tbGoodsDesc, item );

                //插入
                itemMapper.insert ( item );
            }
        } else {
            TbItem item = new TbItem ();

            //标题
            String title = tbGoods.getGoodsName ();
            item.setTitle ( title );

            //价格
            item.setPrice ( goods.getGoods ().getPrice () );
            //状态
            item.setStatus ( "1" );
            //是否默认
            item.setIsDefault ( "1" );
            //库存数量
            item.setNum ( 99999 );
            item.setSpec ( "{}" );
            setItemValus ( tbGoods, tbGoodsDesc, item );
            itemMapper.insert ( item );

        }
    }

    private void setItemValus(TbGoods tbGoods, TbGoodsDesc tbGoodsDesc, TbItem item) {
        //商品图片
        //从商品详情中取出图片信息,json字符串
        String images = tbGoodsDesc.getItemImages ();
        List <Map> list = JSON.parseArray ( images, Map.class );
        if (list.size () > 0) {
            item.setImage ( (String) list.get ( 0 ).get ( "url" ) );
        }

        //所属类目，叶子类目
        item.setCategoryid ( tbGoods.getCategory3Id () );

        //创建时间
        item.setCreateTime ( new Date () );

        //更新时间
        item.setUpdateTime ( new Date () );

        //goods_id 商品SPU  id
        item.setGoodsId ( tbGoods.getId () );

        //seller_id  商家id
        item.setSellerId ( tbGoods.getSellerId () );

        //category 分类名称
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey ( tbGoods.getCategory3Id () );
        item.setCategory ( itemCat.getName () );

        //brand 品牌
        TbBrand brand = brandMapper.selectByPrimaryKey ( tbGoods.getBrandId () );
        item.setBrand ( brand.getName () );

        //seller 商家名称
        TbSeller tbSeller = sellerMapper.selectByPrimaryKey ( tbGoods.getSellerId () );
        item.setSeller ( tbSeller.getNickName () );
    }


    /**
     * 修改
     */
    @Override
    public void update(Goods goods) {
        goodsMapper.updateByPrimaryKey ( goods.getGoods () );
        goodsDescMapper.updateByPrimaryKey ( goods.getGoodsDesc () );

        //sku列表信息先删除再重新插入
        TbItemExample example = new TbItemExample ();
        example.createCriteria ().andGoodsIdEqualTo ( goods.getGoods ().getId () );
        itemMapper.deleteByExample ( example );

        //重新插入
        insetItems ( goods );
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Goods findOne(Long id) {
        //创建对象
        Goods goods = new Goods ();

        TbGoods tbGoods = goodsMapper.selectByPrimaryKey ( id );
        goods.setGoods ( tbGoods );

        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey ( id );
        goods.setGoodsDesc ( tbGoodsDesc );

        TbItemExample example = new TbItemExample ();
        example.createCriteria ().andGoodsIdEqualTo ( id );
        List <TbItem> tbItems = itemMapper.selectByExample ( example );
        goods.setItems ( tbItems );

        return goods;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            TbGoods goods = goodsMapper.selectByPrimaryKey ( id );
            goods.setIsDelete ( "1" );
            goodsMapper.updateByPrimaryKey ( goods );
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage ( pageNum, pageSize );

        TbGoodsExample example = new TbGoodsExample ();
        Criteria criteria = example.createCriteria ();
        criteria.andIsDeleteIsNull ();

        if (goods != null) {
            if (goods.getSellerId () != null && goods.getSellerId ().length () > 0) {
                //criteria.andSellerIdLike ( "%" + goods.getSellerId () + "%" );
                criteria.andSellerIdEqualTo ( goods.getSellerId () );
            }
            if (goods.getGoodsName () != null && goods.getGoodsName ().length () > 0) {
                criteria.andGoodsNameLike ( "%" + goods.getGoodsName () + "%" );
            }
            if (goods.getAuditStatus () != null && goods.getAuditStatus ().length () > 0) {
                criteria.andAuditStatusLike ( "%" + goods.getAuditStatus () + "%" );
            }
            if (goods.getIsMarketable () != null && goods.getIsMarketable ().length () > 0) {
                criteria.andIsMarketableLike ( "%" + goods.getIsMarketable () + "%" );
            }
            if (goods.getCaption () != null && goods.getCaption ().length () > 0) {
                criteria.andCaptionLike ( "%" + goods.getCaption () + "%" );
            }
            if (goods.getSmallPic () != null && goods.getSmallPic ().length () > 0) {
                criteria.andSmallPicLike ( "%" + goods.getSmallPic () + "%" );
            }
            if (goods.getIsEnableSpec () != null && goods.getIsEnableSpec ().length () > 0) {
                criteria.andIsEnableSpecLike ( "%" + goods.getIsEnableSpec () + "%" );
            }
            if (goods.getIsDelete () != null && goods.getIsDelete ().length () > 0) {
                criteria.andIsDeleteLike ( "%" + goods.getIsDelete () + "%" );
            }

        }

        Page <TbGoods> page = (Page <TbGoods>) goodsMapper.selectByExample ( example );
        return new PageResult ( page.getTotal (), page.getResult () );
    }

    @Override
    public void updateStatus(Long[] ids ,String status) {
        for (Long id : ids) {
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey ( id );
            tbGoods.setAuditStatus ( status );
            goodsMapper.updateByPrimaryKey ( tbGoods );
        }
    }

}
