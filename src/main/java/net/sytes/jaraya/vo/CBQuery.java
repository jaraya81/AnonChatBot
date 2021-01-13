package net.sytes.jaraya.vo;

import com.pengrad.telegrambot.model.CallbackQuery;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class CBQuery extends BaseUpdate {
    private CallbackQuery query;

    public static CBQuery to(CallbackQuery callbackQuery) {
        if (callbackQuery == null) return null;
        return CBQuery.builder()
                .query(callbackQuery)
                .build();
    }
}
