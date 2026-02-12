package com.example.usercenterpractice.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.usercenterpractice.model.domain.User;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 86150
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2026-01-30 12:00:46
* @Entity generator.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




