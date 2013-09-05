package com.kevinhankens.bouncer;

public class Item {
  private long id;
  private int sort;
  private String item;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getSort() {
    return sort;
  }

  public void setSort(int sort) {
    this.sort = sort;
  }

  public String getItem() {
    return item;
  }

  public void setItem(String item) {
    this.item = item;
  }

  // Will be used by the ArrayAdapter in the ListView
  @Override
  public String toString() {
    return item;
  }
} 

