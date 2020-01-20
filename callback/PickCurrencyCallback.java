package com.unexceptional.beast.banko.newVersion.callback;

import com.unexceptional.beast.banko.newVersion.db.model.Currency;

public interface PickCurrencyCallback {
    void onCurrencyItemPick(Currency currency);
}
