package hu.joti.tuduu.model;

import java.io.Serializable;

public class Category implements Serializable, Comparable {

  private int id;
  private String name;

  public Category() {
    id = -1;
  }

  public Category(String name) {
    id = -1;
    this.name = name;
  }

  public Category(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public int compareTo(Object o) {
    return getName().compareTo(((Category)o).getName());
  }
}
