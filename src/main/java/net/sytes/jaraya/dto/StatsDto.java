package net.sytes.jaraya.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Builder
@ToString
@Data
public class StatsDto {

    private Long total;
    private Long active;
    private Long paused;
    private Long banned;
}
