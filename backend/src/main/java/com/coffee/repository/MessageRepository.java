package com.coffee.repository;


import com.coffee.entity.Message;
import com.coffee.entity.User;
import com.coffee.wrapper.MessageWrapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;


import java.util.List;


public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<MessageWrapper> getAllMessages(Integer fromUserId, Integer toUserId);
    List<MessageWrapper> getAllMessagesByUserId(Integer userId);
    List<MessageWrapper> getUnreadMessages(Integer userId);
    boolean existsByFromUserAndToUserAndContent(User fromUser, User toUser, String content);
}






