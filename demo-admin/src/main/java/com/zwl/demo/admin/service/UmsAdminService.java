package com.zwl.demo.admin.service;

import com.zwl.demo.admin.dto.UpdateAdminPasswordParam;
import com.zwl.demo.model.UmsAdmin;
import com.zwl.demo.admin.dto.UmsAdminParam;
import com.zwl.demo.model.UmsPermission;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface UmsAdminService {

    /**
     * 根据用户名获取后台管理员
     */
    UmsAdmin getAdminByUsername(String username);

    /**
     * 注册功能
     */
    UmsAdmin register(UmsAdminParam umsAdminParam);

    /**
     * 登录功能
     * @param username 用户名
     * @param password 密码
     * @return 生成的JWT的token
     */
    String login(String username, String password);



    /**
     * 获取用户信息
     */
    UserDetails loadUserByUsername(String username);


    /**
     * 获取用户所有权限(包括+-权限)
     */
    public List<UmsPermission> getPermissionList(Long adminId);

    String refreshToken(String token);

    UmsAdmin getItem(Long id);

    int update(Long id, UmsAdmin admin);

    int updatePassword(UpdateAdminPasswordParam updatePasswordParam);
}
