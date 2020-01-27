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
public class Report implements Serializable {

    private Long id;
    private Long user;
    private Timestamp datecreation;

    @Tolerate
    public Report() {
        super();
    }
}
