package com.zwl.demo.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.zwl.demo.admin.bo.AdminUserDetails;
import com.zwl.demo.admin.dto.UpdateAdminPasswordParam;
import com.zwl.demo.dao.UmsAdminRoleRelationDao;
import com.zwl.demo.admin.dto.UmsAdminParam;
import com.zwl.demo.admin.service.UmsAdminService;
import com.zwl.demo.mapper.UmsAdminMapper;
import com.zwl.demo.model.UmsAdmin;
import com.zwl.demo.model.UmsAdminExample;
import com.zwl.demo.model.UmsPermission;
import com.zwl.demo.security.utils.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@Service
public class UmsAdminServiceImpl implements UmsAdminService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UmsAdminServiceImpl.class);

    @Autowired
    UmsAdminMapper umsAdminMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UmsAdminRoleRelationDao UmsAdminRoleRelationDao;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;


    @Override
    public UmsAdmin getAdminByUsername(String username) {

        UmsAdminExample umsAdminExample = new UmsAdminExample();
        umsAdminExample.createCriteria().andUsernameEqualTo(username);
        List<UmsAdmin> umsAdmins = umsAdminMapper.selectByExample(umsAdminExample);
        if (umsAdmins != null && umsAdmins.size() >0){
            return umsAdmins.get(0);
        }

        return null;
    }

    @Override
    public UmsAdmin register(UmsAdminParam umsAdminParam) {

        UmsAdmin umsAdmin = new UmsAdmin();
        BeanUtils.copyProperties(umsAdminParam, umsAdmin);
        umsAdmin.setCreateTime(new Date());
        umsAdmin.setStatus(1);
        //查询是否有相同用户名的用户
        UmsAdminExample example = new UmsAdminExample();
        example.createCriteria().andUsernameEqualTo(umsAdmin.getUsername());
        List<UmsAdmin> umsAdminList = umsAdminMapper.selectByExample(example);
        if (umsAdminList.size() > 0) {
            return null;
        }

        String encrptPass = passwordEncoder.encode(umsAdmin.getPassword());
        umsAdmin.setPassword(encrptPass);
        umsAdminMapper.insert(umsAdmin);


        return umsAdmin;
    }

    @Override
    public String login(String username, String password) {

        String token = null;
        try {
            UserDetails userDetails = loadUserByUsername(username);
            if (!passwordEncoder.matches(password, userDetails.getPassword())){
                throw new BadCredentialsException("密码不正确");
            }

            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities() );

            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            token = jwtTokenUtil.generateToken(userDetails);



        }catch (AuthenticationException e) {
            LOGGER.warn("登录异常:{}", e.getMessage());
        }
        return token;

    }

    @Override
    public List<UmsPermission> getPermissionList(Long adminId) {
        return UmsAdminRoleRelationDao.getPermissionList(adminId);
    }

    @Override
    public String refreshToken(String token) {
        return jwtTokenUtil.refreshHeadToken(token);
    }

    @Override
    public UmsAdmin getItem(Long id) {
        return umsAdminMapper.selectByPrimaryKey(id);
    }

    @Override
    public int update(Long id, UmsAdmin admin) {
        admin.setId(id);
        //密码已经加密处理，需要单独修改
        admin.setPassword(null);
        return   umsAdminMapper.updateByPrimaryKeySelective(admin);

    }

    @Override
    public int updatePassword(UpdateAdminPasswordParam param) {
        if(StrUtil.isEmpty(param.getUsername())
                ||StrUtil.isEmpty(param.getOldPassword())
                ||StrUtil.isEmpty(param.getNewPassword())){
            return -1;
        }

        UmsAdminExample example = new UmsAdminExample();
        example.createCriteria().andUsernameEqualTo(param.getUsername());
        List<UmsAdmin> adminList = umsAdminMapper.selectByExample(example);
        if(CollUtil.isEmpty(adminList)){
            return -2;
        }

        UmsAdmin umsAdmin = adminList.get(0);
        if(!passwordEncoder.matches(param.getOldPassword(),umsAdmin.getPassword())){
            return -3;
        }

        umsAdmin.setPassword(passwordEncoder.encode(param.getNewPassword()));
        umsAdminMapper.updateByPrimaryKey(umsAdmin);
        return 1;


    }

    @Override
    public UserDetails loadUserByUsername(String username) {

        //获取用户信息
        UmsAdmin admin = getAdminByUsername(username);
        if (admin != null) {
            List<UmsPermission> permissionList = getPermissionList(admin.getId());
            return new AdminUserDetails(admin,permissionList);
        }
        throw new UsernameNotFoundException("用户名或密码错误");

    }
}
