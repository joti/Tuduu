package hu.joti.tuduu.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DBModel implements IModel {

  private TuduuDBHelper helper;

  public DBModel(Context context) {
    helper = TuduuDBHelper.instance(context);
  }

  @Override
  public List<Category> getAllCategories() {
    SQLiteDatabase db = helper.getReadableDatabase();
    Cursor cursor = db.rawQuery("select * from category", null);

    List<Category> items = new ArrayList<>();

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      int id = cursor.getInt(cursor.getColumnIndex("ca_id"));
      String name = cursor.getString(cursor.getColumnIndex("name"));

      Category category = new Category(id, name);
      items.add(category);

      cursor.moveToNext();
    }
    cursor.close();
    db.close();

    return items;
  }

  @Override
  public void saveCategory(Category category) {
    SQLiteDatabase db = helper.getWritableDatabase();
    ContentValues cv = new ContentValues();
    cv.put("name", category.getName());
    if (category.getId() < 0) {
      long id = db.insert("category", null, cv);
      category.setId((int) id);
    } else {
      cv.put("ca_id", category.getId());
      db.update("category", cv, "ca_id = ?", new String[]{category.getId() + ""});
    }
    db.close();
  }

  @Override
  public void deleteCategory(Category category) {
    SQLiteDatabase db = helper.getWritableDatabase();
    db.delete("category", "ca_id=?", new String[]{category.getId() + ""});
    db.close();
  }

  @Override
  public List<Task> getAllTasks() {
    return getAllTasks(null);
  }

  @Override
  public List<Task> getAllTasks(Category category) {
    SQLiteDatabase db = helper.getReadableDatabase();
    Cursor cursor;
    if (category == null) {
      cursor = db.rawQuery("select t.*, c.name as ca_name from task t, category c where c.ca_id = t.ca_id", null);
    } else {
      cursor = db.rawQuery("select t.*, c.name as ca_name from task t, category c where c.ca_id = t.ca_id and c.ca_id=?", new String[]{category.getId() + ""});
    }
    List<Task> tasks = new ArrayList<>();

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      int categoryId = cursor.getInt(cursor.getColumnIndex("ca_id"));
      String categoryName = cursor.getString(cursor.getColumnIndex("ca_name"));
      Category taskcategory = new Category(categoryId, categoryName);

      int id = cursor.getInt(cursor.getColumnIndex("ta_id"));
      String name = cursor.getString(cursor.getColumnIndex("name"));
      String desc = cursor.getString(cursor.getColumnIndex("description"));
      int priority = cursor.getInt(cursor.getColumnIndex("priority"));
      String deadline = cursor.getString(cursor.getColumnIndex("deadline"));
      String doneDate = cursor.getString(cursor.getColumnIndex("donedate"));

      Task task = new Task(id, taskcategory, name, desc, priority, deadline, doneDate);
      tasks.add(task);

      cursor.moveToNext();
    }
    cursor.close();
    db.close();

    return tasks;
  }

  @Override
  public void saveTask(Task task) {
    SQLiteDatabase db = helper.getWritableDatabase();
    ContentValues cv = new ContentValues();
    cv.put("ca_id", task.getCategory().getId());
    cv.put("name", task.getName());
    cv.put("description", task.getDescription());
    cv.put("deadline", task.getDeadline());
    cv.put("donedate", task.getDoneDate());
    cv.put("priority", task.getPriority());

    Log.i("TUDUU", "Id=" + task.getId());
    if (task.getId() < 0) {
      Log.i("TUDUU","insert");
      long id = db.insert("task", null, cv);
      task.setId((int) id);
    } else {
      Log.i("TUDUU","update");
      cv.put("ta_id", task.getId());
      db.update("task", cv, "ta_id = ?", new String[]{task.getId() + ""});
    }
    db.close();
  }

  @Override
  public void deleteTask(Task task) {
    SQLiteDatabase db = helper.getWritableDatabase();
    db.delete("task", "ta_id=?", new String[]{task.getId() + ""});
    db.close();
  }

  public Calendar getCalendarFromString(String dateString){
    Calendar cal = null;

    if (dateString != null)
      Log.i("TUDUU", "dateString: " + dateString);

    if (dateString != null && dateString.length() == 8){
      cal = Calendar.getInstance();

      int year = Integer.parseInt(dateString.substring(0,4).trim());
      int month = Integer.parseInt(dateString.substring(4,6).trim());
      int day = Integer.parseInt(dateString.substring(6).trim());

      cal.set(Calendar.YEAR, year);
      cal.set(Calendar.MONTH, month);
      cal.set(Calendar.DAY_OF_MONTH, day);
    }
    return cal;
  }

  public String getStringFromCalendar(Calendar cal){
    String dateString = "";
    if (cal != null){
      dateString = String.format("%4d%2d%2d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
    }
    return dateString;
  }

}
