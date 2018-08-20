package com.pinyougou.manage.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录Controller
 *
 * @author Administrator
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    /**
     * 获取用户名
     */
    @RequestMapping("/loginName")
    public Map loginName() {
        String name = SecurityContextHolder.getContext ().getAuthentication ().getName ();
        Map map = new HashMap ();
        map.put ( "loginName" ,name);
        return map;
    }
}
