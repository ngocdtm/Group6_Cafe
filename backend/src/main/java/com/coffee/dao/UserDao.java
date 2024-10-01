
package com.coffee.dao;

import com.coffee.POJO.User;
import com.coffee.wrapper.UserWrapper;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface UserDao extends JpaRepository<User, Integer> {//kho lưu trữ jpa
    User findByEmailId(@Param("email") String email);

    User findByEmail(String email);

    List<UserWrapper> getAllUser();

    List<String> getAllAdmin();
    @Transactional
    @Modifying
    Integer updateStatus(@Param("status") String status, @Param("id") Integer id);
}
