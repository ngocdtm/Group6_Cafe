package com.coffee.entity;


import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;


@NamedQuery(name = "Message.getAllMessagesByUserId",
        query = "SELECT NEW com.coffee.wrapper.MessageWrapper(m.id, m.content, m.createdDate, " +
                "m.fromUser.id, m.fromUser.name, m.fromUser.avatar, " +
                "m.toUser.id, m.toUser.name, m.toUser.avatar, m.seen) " +
                "FROM Message m WHERE m.fromUser.id = :userId OR m.toUser.id = :userId " +
                "ORDER BY m.createdDate")

@NamedQuery(name = "Message.getAllMessages",
        query = "SELECT NEW com.coffee.wrapper.MessageWrapper(m.id, m.content, m.createdDate, " +
                "m.fromUser.id, m.fromUser.name, m.fromUser.avatar, " +
                "m.toUser.id, m.toUser.name, m.toUser.avatar, m.seen) " +
                "FROM Message m WHERE (m.fromUser.id = :fromUserId AND m.toUser.id = :toUserId) OR " +
                "(m.fromUser.id = :toUserId AND m.toUser.id = :fromUserId) " +
                "ORDER BY m.createdDate")


@NamedQuery(name = "Message.getUnreadMessages",
        query = "SELECT NEW com.coffee.wrapper.MessageWrapper(m.id, m.content, m.createdDate, " +
                "m.fromUser.id, m.fromUser.name, m.fromUser.avatar, " +
                "m.toUser.id, m.toUser.name, m.toUser.avatar, m.seen) " +
                "FROM Message m WHERE m.toUser.id = :userId AND m.seen = false")


@Data
@Entity
@Table(name = "message")
public class Message implements Serializable {


    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;


    @Column(name = "content")
    private String content;


    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id")
    private User fromUser;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id")
    private User toUser;


    @Column(name = "seen")
    private Boolean seen = false;


    // Constructor
    public Message() {
        this.createdDate = new Date();
    }
}
