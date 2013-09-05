package com.kevinhankens.bouncer;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
  
/** Called when the activity is first created. */

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    ItemData.initDataSource(this);
    List<Item> values = ItemData.datasource.getAllItems();

    ViewGroup wrapper_group = (ViewGroup) findViewById(R.id.wrapper);
    for (int i = 0; i < values.size(); i++) {
      LinearLayout layout = this.createTask((CharSequence) values.get(i).getItem());
      layout.setId((int) values.get(i).getId());
      wrapper_group.addView(layout);
    }
  }

  public LinearLayout createTask(CharSequence label) {
    LinearLayout layout = new LinearLayout(getApplicationContext());
    layout.setBackgroundResource(R.drawable.shape);
    LinearLayout container = new LinearLayout(getApplicationContext());
    container.setOnTouchListener(new MyTouchListener());
    TextView text = new TextView(getApplicationContext());
    text.setText(label);
    container.addView(text);
    layout.addView(container, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    layout.setOnDragListener(new MyDragListener());
    return layout;
  }

  // Respond to a newly added item.
  public void addNew(View view) {
    ViewGroup wrapper_group = (ViewGroup) findViewById(R.id.wrapper);
    ViewGroup add = (ViewGroup) view.getParent();
    EditText edit = (EditText) add.findViewById(R.id.new_task);
    CharSequence new_label = (CharSequence) edit.getText(); 

    Item item = ItemData.datasource.createItem(new_label.toString(), 0);

    LinearLayout layout = this.createTask(new_label);
    layout.setId((int) item.getId());
    edit.setText("");
    wrapper_group.addView(layout);

    ItemData.updateSort();
  }

  @Override
  protected void onResume() {
    ItemData.datasource.open();
    super.onResume();
  }

  @Override
  protected void onPause() {
    ItemData.datasource.close();
    super.onPause();
  }

  private final class MyTouchListener implements OnTouchListener {
    public boolean onTouch(View view, MotionEvent motionEvent) {
      if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
        LinearLayout container = (LinearLayout) view;
        TextView text = (TextView) container.getChildAt(0);
        CharSequence val = (CharSequence) text.getText();
        ClipData data = ClipData.newPlainText("Move", val);
        DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
        view.startDrag(data, shadowBuilder, view, 0);
        return true;
      } 
      else {
        return false;
      }
    }
  }

  class MyDragListener implements OnDragListener {
    Drawable enterShape = getResources().getDrawable(R.drawable.shape_droptarget);
    Drawable normalShape = getResources().getDrawable(R.drawable.shape);

    @Override
    public boolean onDrag(View v, DragEvent event) {
      int action = event.getAction();
      switch (event.getAction()) {
      case DragEvent.ACTION_DRAG_STARTED:
        // Do nothing
        break;
      case DragEvent.ACTION_DRAG_ENTERED:
        v.setBackgroundDrawable(enterShape);
        break;
      case DragEvent.ACTION_DRAG_EXITED:
        v.setBackgroundDrawable(normalShape);
        break;
      case DragEvent.ACTION_DROP:
        // Dropped, reassign View to ViewGroup
        View dropped = (View) event.getLocalState();
        ViewGroup oldSlot = (ViewGroup) dropped.getParent();
        ViewGroup newSlot = (ViewGroup) v;
        ViewGroup wrapper = (ViewGroup) newSlot.getParent();
//Log.d("KEVIN", "before old count " + Integer.toString(oldSlot.getChildCount()));

        // If dropped where we started, do nothing.
        if (newSlot == oldSlot) {
          return true;
        }

        wrapper.removeView(oldSlot);
        int position = wrapper.indexOfChild(newSlot);
        wrapper.addView(oldSlot, position);
        ItemData.updateSort();
        break;
      case DragEvent.ACTION_DRAG_ENDED:
        v.invalidate();
        v.setBackgroundDrawable(normalShape);
      default:
        break;
      }
      return true;
    }
  }
} 
