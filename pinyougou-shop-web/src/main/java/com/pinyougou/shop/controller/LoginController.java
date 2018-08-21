package com.pinyougou.shop.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/login")
@RestController
public class LoginController {

    @RequestMapping("/loginName")
    public Map loginName() {
        Map map = new HashMap ();
        String name = SecurityContextHolder.getContext ().getAuthentication ().getName ();
        map.put ( "loginName", name );
        return map;
    }
}
