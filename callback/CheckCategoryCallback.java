package com.unexceptional.beast.banko.newVersion.callback;

import com.unexceptional.beast.banko.newVersion.ui.dialog.categories.CheckCategoryFullDialog;

import java.util.List;

public interface CheckCategoryCallback {
    void sendItems(List<CheckCategoryFullDialog.SuperParentCategory> itemsIds);
}
