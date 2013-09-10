package com.kevinhankens.carrotstick;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;
import android.view.View;
import android.widget.LinearLayout;

  class ItemData {
    public static ItemsDataSource datasource;
    private static Activity activity;

    public static void initDataSource(Activity activity) {
      ItemData.datasource = new ItemsDataSource(activity);
      ItemData.activity = activity;
      ItemData.datasource.open();
    }

    public static void updateSort() {
      ViewGroup wrapper = (ViewGroup) ItemData.activity.findViewById(R.id.wrapper);
      int count = wrapper.getChildCount();
      for (int i = 1; i < count; i++) {
        LinearLayout layout = (LinearLayout) wrapper.getChildAt(i);
        Item item = ItemData.datasource.getItemById((long) layout.getId());
        item.setSort(i);
        ItemData.datasource.updateItem(item);
      }
    }

  }


