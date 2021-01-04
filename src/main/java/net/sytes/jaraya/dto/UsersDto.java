package net.sytes.jaraya.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Tolerate;
import net.sytes.jaraya.model.User;

import java.util.List;

@ToString
@Data
@Builder
public class UsersDto {

    private Integer size;
    private List<User> user;

    @Tolerate
    public UsersDto() {
    }
}
