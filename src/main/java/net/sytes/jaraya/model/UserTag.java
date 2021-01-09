package net.sytes.jaraya.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Tolerate;

import java.sql.Timestamp;

@Data
@Builder
@ToString
public class UserTag {

    private Long id;
    private Long idUser;
    private String tag;
    private Timestamp creationdate;

    @Tolerate
    public UserTag() {
        super();
    }

    public enum Columns {
        ID("id"), ID_USER("idUser"), TAG("tag"), CREATION("creationdate");

        String value;

        Columns(String value) {
            this.value = value;
        }

        public String value(){
            return value;
        }
    }

}
