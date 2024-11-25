package com.coffee.wrapper;


import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;


@Data
@NoArgsConstructor
public class MessageWrapper {
    private Integer id;
    private String content;
    private Date createdDate;
    private Integer fromUserId;
    private String fromUserName;
    private String fromUserAvatar;
    private Integer toUserId;
    private String toUserName;
    private String toUserAvatar;
    private Boolean seen;


    public MessageWrapper(Integer id, String content, Date createdDate,
                          Integer fromUserId, String fromUserName, String fromUserAvatar,
                          Integer toUserId, String toUserName, String toUserAvatar,
                          Boolean seen) {
        this.id = id;
        this.content = content;
        this.createdDate = createdDate;
        this.fromUserId = fromUserId;
        this.fromUserName = fromUserName;
        this.fromUserAvatar = fromUserAvatar;
        this.toUserId = toUserId;
        this.toUserName = toUserName;
        this.toUserAvatar = toUserAvatar;
        this.seen = seen;
    }
}



