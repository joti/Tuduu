package hu.joti.tuduu.model;

import java.util.List;

public interface IModel {

  List<Category> getAllCategories();
  void saveCategory(Category category);
  void deleteCategory(Category category);

  List<Task> getAllTasks();
  List<Task> getAllTasks(Category category);
  void saveTask(Task task);
  void deleteTask(Task task);

}
