package net.sytes.jaraya.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Builder
@ToString
@Data
public class StatsDto {
    String key;
    String name;
    Object value;
}
