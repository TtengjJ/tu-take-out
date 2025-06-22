package com.sky.mapper;


import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    //根据openid查询用户
    @Select(" SELECT * FROM user WHERE openid=#{openid}")
    User getUserByOpenId(String openid);

    //新增用户
    void insert(User user);
}
