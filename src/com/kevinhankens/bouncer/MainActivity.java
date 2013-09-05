package com.kevinhankens.bouncer;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
  
  /**
   * Constructor.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    // Basic setup.
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // Initiate the database connection and retrieve all items.
    ItemData.initDataSource(this);
    List<Item> values = ItemData.datasource.getAllItems();

    // Draw the item list on the screen from the saved items.
    ViewGroup wrapper_group = (ViewGroup) findViewById(R.id.wrapper);
    for (int i = 0; i < values.size(); i++) {
      LinearLayout layout = this.createTask((CharSequence) values.get(i).getItem());
      layout.setId((int) values.get(i).getId());
      wrapper_group.addView(layout);
    }
  }

  /**
   * Creates a layout view with a task.
   *
   * @param CharSequence label
   *   The text to print on the task.
   * 
   * @return LinearLayout
   */
  public LinearLayout createTask(CharSequence label) {
    // The layout view contains a touch view and a text view.
    LinearLayout layout = new LinearLayout(getApplicationContext());
    layout.setBackgroundResource(R.drawable.shape);
    // Add a delete button.
    ImageView delete = new ImageView(getApplicationContext());
    delete.setImageResource(R.drawable.ic_delete);
    //delete.paddingRight("10dp");
    registerForContextMenu(delete);
    delete.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        ViewGroup wrapper = (ViewGroup) findViewById(R.id.wrapper);
        LinearLayout layout = (LinearLayout) v.getParent();
        Item item = ItemData.datasource.getItemById((long) layout.getId());
        ItemData.datasource.deleteItem(item);
        wrapper.removeView(layout);
      }
    });
    layout.addView(delete);
    LinearLayout container = new LinearLayout(getApplicationContext());
    container.setOnTouchListener(new MyTouchListener());
    TextView text = new TextView(getApplicationContext());
    text.setText(label);
    text.setTextSize(20);
    container.addView(text);
    layout.addView(container, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    layout.setOnDragListener(new MyDragListener());
    return layout;
  }

  /**
   * Respond to a newly added item from the text input.
   *
   * @param View view
   *   The button that was clicked.
   *
   * @return void
   */
  public void addNew(View view) {
    // Set up a new item.
    ViewGroup wrapper_group = (ViewGroup) findViewById(R.id.wrapper);
    ViewGroup add = (ViewGroup) view.getParent();
    EditText edit = (EditText) add.findViewById(R.id.new_task);
    CharSequence new_label = (CharSequence) edit.getText(); 
    // Save it to the db.
    Item item = ItemData.datasource.createItem(new_label.toString(), 0);
    // Create the new item.
    LinearLayout layout = this.createTask(new_label);
    layout.setId((int) item.getId());
    edit.setText("");
    wrapper_group.addView(layout);
    // Add it to the bottom of the list.
    ItemData.updateSort();
  }

  /**
   * When the app is resumed, open the database.
   */
  @Override
  protected void onResume() {
    ItemData.datasource.open();
    super.onResume();
  }

  /**
   * When the app is paused, close the database.
   */
  @Override
  protected void onPause() {
    ItemData.datasource.close();
    super.onPause();
  }

  /**
   * Listen for touch events.
   */
  private final class MyTouchListener implements OnTouchListener {
    public boolean onTouch(View view, MotionEvent motionEvent) {
      if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
        // Capture the text that was touched and start the drag event.
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

  /**
   * Listen for drag events.
   */
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
        // When dropped, determine the containers that were touched and dropped onto.
        View dropped = (View) event.getLocalState();
        ViewGroup oldSlot = (ViewGroup) dropped.getParent();
        ViewGroup newSlot = (ViewGroup) v;
        ViewGroup wrapper = (ViewGroup) newSlot.getParent();
//Log.d("KEVIN", "before old count " + Integer.toString(oldSlot.getChildCount()));

        // If dropped where we started, do nothing.
        if (newSlot == oldSlot) {
          return true;
        }

        // Remove the old one and add it to the new position.
        wrapper.removeView(oldSlot);
        int position = wrapper.indexOfChild(newSlot);
        wrapper.addView(oldSlot, position);
        // Update the sort order in the db.
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
