package com.yhh.example.demo.mapper;

import com.yhh.example.demo.entity.User;
import com.yhh.example.demo.entity.UserCustom;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @Author: -小野猪-
 * @Date: 2019/6/30 10:35
 * @Version: 1.0
 * @Desc:
 */
public interface UserMapper {

    //用户信息综合查询
    //@Options
    //@SelectKey(statement = {"xx"}, keyProperty = "", before = true, keyColumn = "xx", resultType = Object.class)
//    @ResultMap({"xxx", "yyyy"})
//    @Select("select * from user where user.sex=#{user.sex} and user.username like '%${user.username}%'")
//    @Results({
//            @Result(property = "id", column = "id", javaType = Long.class),
//            @Result(property = "username", column = "username")
//    })
    List<UserCustom> findUserList(@Param(value = "user") User user) throws Exception;

    //用户信息综合查询总数
    int findUserCount(User user) throws Exception;



    //根据id查询用户信息       产生的动态代理对象调用selectOne方法(根据返回值判断的)
    User findUserById(int id) throws Exception;

    //根据用户名列查询用户列表       产生的动态代理对象调用selectList方法(根据返回值判断的)
    List<User> findUserByName(String name) throws Exception;

    //插入用户
    int insertUser(User user)throws Exception;

    //删除用户
    int deleteUser(int id)throws Exception;
}