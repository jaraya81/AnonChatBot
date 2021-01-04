package net.sytes.jaraya;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class AppTest {

    @Test
    public void formar() {
        log.info("{} {}", "hola", "mundo");
        String value = String.format("%s %s", "hola", "mundo");
        log.info(value);
    }
}
