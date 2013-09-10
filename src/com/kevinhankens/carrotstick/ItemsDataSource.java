package com.kevinhankens.carrotstick;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


public class ItemsDataSource {

  // Database fields
  private SQLiteDatabase database;
  private MySQLiteHelper dbHelper;
  private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
    MySQLiteHelper.COLUMN_SORT,
    MySQLiteHelper.COLUMN_ITEM };

  public ItemsDataSource(Context context) {
    dbHelper = new MySQLiteHelper(context);
  }

  public void open() throws SQLException {
    database = dbHelper.getWritableDatabase();
  }

  public void close() {
    dbHelper.close();
  }

  public Item createItem(String item, int sort) {
    ContentValues values = new ContentValues();
    values.put(MySQLiteHelper.COLUMN_SORT, sort);
    values.put(MySQLiteHelper.COLUMN_ITEM, item);
    long insertId = database.insert(MySQLiteHelper.TABLE_COMMENTS, null,
        values);
    Cursor cursor = database.query(MySQLiteHelper.TABLE_COMMENTS,
        allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
        null, null, null);
    cursor.moveToFirst();
    Item newItem = cursorToItem(cursor);
    cursor.close();
    return newItem;
  }

  public void updateItem(Item item) {
    ContentValues values = new ContentValues();
    values.put(MySQLiteHelper.COLUMN_SORT, item.getSort());
    values.put(MySQLiteHelper.COLUMN_ITEM, item.getItem());
    long id = item.getId();
    int result = database.update(MySQLiteHelper.TABLE_COMMENTS, values, MySQLiteHelper.COLUMN_ID
        + " = " + id, null);
    System.out.println("Item updated with id: " + id + " sort: " + item.getSort() + " return: " + result);
  }

  public void deleteItem(Item item) {
    long id = item.getId();
    System.out.println("Item deleted with id: " + id);
    database.delete(MySQLiteHelper.TABLE_COMMENTS, MySQLiteHelper.COLUMN_ID
        + " = " + id, null);
  }

  public Item getItemById(long id) {
    Cursor cursor = database.query(MySQLiteHelper.TABLE_COMMENTS,
        allColumns, MySQLiteHelper.COLUMN_ID + " = " + Integer.toString((int) id), null, null, null, null, null);

    cursor.moveToFirst();
    Item item = cursorToItem(cursor);
    cursor.close();

    return item;
  }

  public List<Item> getAllItems() {
    List<Item> items = new ArrayList<Item>();

    Cursor cursor = database.query(MySQLiteHelper.TABLE_COMMENTS,
        allColumns, null, null, null, null, MySQLiteHelper.COLUMN_SORT, null);

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      Item item = cursorToItem(cursor);
      items.add(item);
      cursor.moveToNext();
    }
    // Make sure to close the cursor
    cursor.close();
    return items;
  }

  private Item cursorToItem(Cursor cursor) {
    Item item = new Item();
    item.setId(cursor.getLong(0));
    item.setSort(cursor.getInt(1));
    item.setItem(cursor.getString(2));
    return item;
  }
} 
