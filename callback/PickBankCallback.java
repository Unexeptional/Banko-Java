package com.unexceptional.beast.banko.newVersion.callback;

import com.unexceptional.beast.banko.newVersion.db.model.Bank;

public interface PickBankCallback {
    void onBankItemPick(Bank bank);
}
