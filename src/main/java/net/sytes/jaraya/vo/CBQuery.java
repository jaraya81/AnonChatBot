package net.sytes.jaraya.vo;

import com.pengrad.telegrambot.model.CallbackQuery;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@Slf4j
public class CBQuery extends BaseUpdate {
    private CallbackQuery query;

    public static CBQuery to(CallbackQuery callbackQuery) {
        if (callbackQuery == null) return null;
        return CBQuery.builder()
                .query(callbackQuery)
                .build();
    }
}
