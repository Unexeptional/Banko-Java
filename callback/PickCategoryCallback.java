package com.unexceptional.beast.banko.newVersion.callback;

import com.unexceptional.beast.banko.newVersion.db.model.Category;
import com.unexceptional.beast.banko.newVersion.db.model.Product;

public interface PickCategoryCallback {
    void onCategoryItemPick(Category category);
}
