
package com.coffee.dao;

import com.coffee.POJO.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface UserDao extends JpaRepository<User, Integer> {//kho lưu trữ jpa
    User findByEmailId(@Param("email") String email);


}
