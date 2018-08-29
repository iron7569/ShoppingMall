package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;
import entity.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;

	private static final Logger LOGGER = LoggerFactory.getLogger ( ContentServiceImpl.class );
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insert(content);
		redisTemplate.boundHashOps ( "content" ).delete ( content.getCategoryId () );
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
	    //查询修改前分类id
        Long categoryId = contentMapper.selectByPrimaryKey ( content.getId () ).getCategoryId ();
        //删除缓存
        redisTemplate.boundHashOps ( "content" ).delete ( categoryId );

        contentMapper.updateByPrimaryKey ( content );

        //判断是否对分类id作了修改
        if (categoryId.longValue () != content.getCategoryId ().longValue ()) {
            //删除缓存
            redisTemplate.boundHashOps ( "content" ).delete ( content.getCategoryId () );
        }
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
            Long categoryId = contentMapper.selectByPrimaryKey ( id ).getCategoryId ();
            redisTemplate.boundHashOps ( "content" ).delete ( categoryId );
            contentMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}
			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}
			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}
			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}
	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public List <TbContent> findByCategoryId(Long categoryId) {

		List <TbContent> list = (List <TbContent>) redisTemplate.boundHashOps ( "content" ).get ( categoryId );

		if (list != null) {
			LOGGER.info ( "redis缓存中查询广告数据" );
			return list;
		}

		LOGGER.info ( "数据库中查询广告数据" );
		TbContentExample example = new TbContentExample ();
		example.createCriteria ().andCategoryIdEqualTo ( categoryId ).andStatusEqualTo ( "1" );
		example.setOrderByClause ( "sort_order" );
		list = contentMapper.selectByExample ( example );
		redisTemplate.boundHashOps ( "content" ).put ( categoryId,list );
		return list;
	}

}
