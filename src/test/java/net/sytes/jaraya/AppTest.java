package net.sytes.jaraya;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sytes.jaraya.security.Base64;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
public class AppTest {

    @Test
    public void urlEncoding() {
        String value = "17777";
        Assert.assertTrue(value.contentEquals(Base64.decodeUrl(Base64.encodeUrl(value))));
    }

    @Test
    public void formar() {
        log.info("{} {}", "hola", "mundo");
        String value = String.format("%s %s", "hola", "mundo");
        log.info(value);
    }

    @Test
    public void async() throws ExecutionException, InterruptedException {
        List<String> lista = new ArrayList<>();
        CompletableFuture<Long> completableFuture = CompletableFuture.supplyAsync(() -> sendMessages(lista));
        /* while (!completableFuture.isDone()) {
            log.info("CompletableFuture is not finished yet...");
            Thread.sleep(1000);
        }
        log.info("{}", completableFuture.get());
    */
    }

    @SneakyThrows
    private Long sendMessages(List<String> lista) {
        Thread.sleep(10000);
        log.info("listo");
        return 0L;
    }
}
