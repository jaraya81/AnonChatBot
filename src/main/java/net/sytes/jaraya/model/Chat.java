package net.sytes.jaraya.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Tolerate;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@Builder
@ToString
public class Chat implements Serializable {

    private Long id;
    private Long user1;
    private Long user2;
    private String state;
    private Timestamp datecreation;
    private Timestamp dateupdate;

    @Tolerate
    public Chat() {
        super();
    }

    public Long otherId(Long idUser) {
        return user1.longValue() != idUser.longValue() ? user1 : user2.longValue();
    }
}
