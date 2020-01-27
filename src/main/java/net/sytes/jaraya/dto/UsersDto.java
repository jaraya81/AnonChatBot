package net.sytes.jaraya.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Tolerate;
import net.sytes.jaraya.model.User;

import java.io.Serializable;
import java.util.List;

@ToString
@Data
@Builder

public class UsersDto implements Serializable {

    private Integer size;
    private List<User> user;

    @Tolerate
    public UsersDto() {
    }
}
