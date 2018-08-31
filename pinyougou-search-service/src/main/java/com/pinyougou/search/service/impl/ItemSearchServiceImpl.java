package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 搜索
     *
     * @param searchMap
     * @return
     */
    @Override
    public Map <String, Object> search(Map searchMap) {
        Map <String, Object> map = new HashMap <> ();

        //查询商品
        map.putAll ( getSearchMap ( searchMap ) );

        //查询分类列表
        List <String> categoryList = getCategoryList ( searchMap );
        map.put ( "categoryList", categoryList );

        //查询品牌和规格列表
        if ("".equals ( (String) searchMap.get ( "category" ) )){
            //没有分类名称,返回分类名称列表第一个
            if (categoryList.size () > 0) {
                map.putAll ( getSpecAndBrandListFromRedis ( categoryList.get ( 0 ) ) );
            }
        }else {
            Map specAndBrandList = getSpecAndBrandListFromRedis ( (String) searchMap.get ( "category" ) );
            map.putAll ( specAndBrandList );
        }


        return map;
    }

    /**
     * 从缓存中获取品牌和规格列表
     *
     * @param category
     * @return
     */
    private Map getSpecAndBrandListFromRedis(String category) {
        Map map = new HashMap ();

        //从缓存中读取类型模板id
        Long typeId = (Long) redisTemplate.boundHashOps ( "itemCat" ).get ( category );
        if (typeId != null) {

            //获取品牌列表
            List brandList = (List) redisTemplate.boundHashOps ( "brandList" ).get ( typeId );
            map.put ( "brandList", brandList );

            //获取规格列表
            List specList = (List) redisTemplate.boundHashOps ( "specList" ).get ( typeId );
            map.put ( "specList", specList );

        }

        return map;

    }

    /**
     * 获取搜索结果列表
     *
     * @param searchMap 搜索参数
     * @return
     */
    private Map <String, Object> getSearchMap(Map searchMap) {
        Map <String, Object> map = new HashMap <> ();

//        //查询对象
//        Query query = new SimpleQuery ();
//        //查询条件
//        Criteria criteria = new Criteria ( "item_keywords" );
//        criteria.is ( searchMap.get ( "keywords" ) );
//        query.addCriteria ( criteria );
//        //执行查询获取结果
//        ScoredPage <TbItem> items = solrTemplate.queryForPage ( query, TbItem.class );
//        map.put ( "rows", items.getContent ());
        //1.关键字查询,高亮处理
        //高亮处理
        //高亮查询对象
        HighlightQuery query = new SimpleHighlightQuery ();
        //创建高亮选项,设置高亮显示的域
        HighlightOptions highlightOptions = new HighlightOptions ().addField ( "item_title" );
        //高亮前缀
        highlightOptions.setSimplePrefix ( "<em style='color:red'>" );
        //后缀
        highlightOptions.setSimplePostfix ( "</em>" );
        //添加选项
        query.setHighlightOptions ( highlightOptions );
        //查询条件
        Criteria criteria = new Criteria ( "item_keywords" );
        criteria.is ( searchMap.get ( "keywords" ) );
        //添加查询条件
        query.addCriteria ( criteria );

        //2.分类筛选  过滤查询
        if (!"" .equals ( searchMap.get ( "category" ) ) ) {
            //过滤查询对象
            FilterQuery filterQuery = new SimpleFacetQuery ();
            //查询条件
            Criteria filterCriteria = new Criteria ( "item_category" ).is ( searchMap.get ( "category" ) );
            filterQuery.addCriteria ( filterCriteria );
            query.addFilterQuery ( filterQuery );
        }

        //3.品牌筛选  过滤查询
        if (!"" .equals ( searchMap.get ( "brand" ) ) ) {
            //过滤查询对象
            FilterQuery filterQuery = new SimpleFacetQuery ();
            //查询条件
            Criteria filterCriteria = new Criteria ( "item_brand" ).is ( searchMap.get ( "brand" ) );
            filterQuery.addCriteria ( filterCriteria );
            query.addFilterQuery ( filterQuery );
        }

        //4.规格筛选
        if (searchMap.get ( "spec" ) != null) {
            Map<String,String> specMap = (Map) searchMap.get ( "spec" );
            for (String key : specMap.keySet ()) {

                FilterQuery filterQuery = new SimpleFacetQuery ();
                //查询条件
                Criteria filterCriteria = new Criteria ( "item_spec_"+key ).is ( searchMap.get ( key ) );
                filterQuery.addCriteria ( filterCriteria );
                query.addFilterQuery ( filterQuery );
            }
        }

        //执行查询获取结果
        HighlightPage <TbItem> tbItems = solrTemplate.queryForHighlightPage ( query, TbItem.class );
        // 遍历 获取高亮入口对象
        for (HighlightEntry <TbItem> entry : tbItems.getHighlighted ()) {

            for (HighlightEntry.Highlight highlight : entry.getHighlights ()) {
                System.out.println ( highlight.getSnipplets () );
            }

            TbItem item = entry.getEntity ();
            if (entry.getHighlights ().size () > 0 && entry.getHighlights ().get ( 0 ).getSnipplets ().size () > 0) {
                item.setTitle ( entry.getHighlights ().get ( 0 ).getSnipplets ().get ( 0 ) );
                //设置高亮的结果
            }
        }
        map.put ( "rows", tbItems.getContent () );
        return map;
    }

    /**
     * 获取分类列表
     *
     * @param searchMap
     * @return
     */
    public List <String> getCategoryList(Map searchMap) {
        List <String> list = new ArrayList <> ();
        //查询对象
        Query query = new SimpleQuery ();
        //查询条件
        //根据关键字查询   where.........
        Criteria criteria = new Criteria ( "item_keywords" );
        criteria.is ( searchMap.get ( "keywords" ) );
        query.addCriteria ( criteria );

        //分组选项
        GroupOptions groupOptions = new GroupOptions ();
        //根据分类分组  group by ............
        groupOptions.addGroupByField ( "item_category" );

        query.setGroupOptions ( groupOptions );

        //执行查询 获取分组页
        GroupPage <TbItem> groupPage = solrTemplate.queryForGroupPage ( query, TbItem.class );

        //获取分组结果
        GroupResult <TbItem> groupResult = groupPage.getGroupResult ( "item_category" );

        //获取分组结果入口页
        Page <GroupEntry <TbItem>> entries = groupResult.getGroupEntries ();

        //获取分组入口集合
        List <GroupEntry <TbItem>> content = entries.getContent ();

        //遍历获取分组
        for (GroupEntry <TbItem> entry : content) {
            list.add ( entry.getGroupValue () );
        }

        return list;
    }
}
