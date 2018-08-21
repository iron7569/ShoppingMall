package com.pinyougou.service;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义登录权限认证类
 *
 * @author Administrator
 */
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger ( UserDetailsServiceImpl.class );

    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        LOGGER.info ( "权限认证,用户名:" + username );

        TbSeller seller = sellerService.findOne ( username );

        if (seller == null) {
            LOGGER.info ( "权限认证,用户不存在" );
            //throw new RuntimeException ( "用户不存在" );
            return null;
        }
        if (!seller.getStatus ().equals ( "1" )) {
            LOGGER.info ( "权限认证,审核未通过,审核状态:" + seller.getStatus () );
            //未认证 审核未通过
            //throw new RuntimeException ( "审核暂未通过" );
            return null;
        }

        //桌面项目提交

        List <GrantedAuthority> grantedAuthorities = new ArrayList <> ();
        grantedAuthorities.add ( new SimpleGrantedAuthority ( "ROLE_SELLER" ) );

        UserDetails userDetails = new User ( username, seller.getPassword (), grantedAuthorities );

        //工作空间版本
        return userDetails;
    }
}
