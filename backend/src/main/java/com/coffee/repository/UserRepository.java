
package com.coffee.repository;

import com.coffee.entity.User;
import com.coffee.wrapper.UserWrapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByEmail(@Param("email") String email);

    List<UserWrapper> getAllUser();

    List<String> getAllAdmin();

    @Transactional
    @Modifying
    Integer updateStatus(@Param("status") String status, @Param("id") Integer id);

    @Query("SELECT new com.coffee.wrapper.UserWrapper(u.id,u.name,u.email,u.phoneNumber,u.status,u.address,u.loyaltyPoints) FROM User u WHERE u.role='customer'")
    List<UserWrapper> getAllCustomers();

}
