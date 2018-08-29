package com.pinyougou.solrutil;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    public void importData(){

        TbItemExample example = new TbItemExample ();
        example.createCriteria ().andStatusEqualTo ( "1" );
        List <TbItem> list = itemMapper.selectByExample ( example );

        for (TbItem item : list) {
            System.out.println (item.getBrand ()+"  "+item.getTitle ()+"  " + item.getSeller ());
            String spec = item.getSpec ();
            Map map = JSON.parseObject ( spec, Map.class );
            item.setSpecMap ( map );
        }

        solrTemplate.saveBeans ( list );
        solrTemplate.commit ();

    }

    public static void main(String[] args) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");

        SolrUtil solrUtil = (SolrUtil) applicationContext.getBean ( "solrUtil" );

        solrUtil.importData ();
    }
}
