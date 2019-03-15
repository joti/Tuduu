package hu.joti.tuduu.model;

import java.io.Serializable;

public class Task implements Serializable, Comparable {

  private int id;
  private Category category;
  private String name;
  private String description;
  private int priority;
  private String deadline;
  private String doneDate;

  public Task() {
    this.id = -1;
  }

  public Task(Category category, String name, String description, int priority, String deadline, String doneDate) {
    this.id = -1;
    this.name = name;
    this.description = description;
    this.priority = priority;
    this.deadline = deadline;
    this.category = category;
    this.doneDate = doneDate;
  }

  public Task(int id, Category category, String name, String description, int priority, String deadline, String doneDate) {
    this.id = id;
    this.category = category;
    this.name = name;
    this.description = description;
    this.priority = priority;
    this.deadline = deadline;
    this.doneDate = doneDate;
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public String getDeadline() {
    if (deadline == null)
      return "";
    else
      return deadline;
  }

  public void setDeadline(String deadline) {
    this.deadline = deadline;
  }

  public String getDoneDate() {
    if (doneDate == null)
      return "";
    else
      return doneDate;
  }

  public void setDoneDate(String doneDate) {
    this.doneDate = doneDate;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  @Override
  public int compareTo(Object o) {
    return name.compareTo(((Task)o).getName());
  }
}
