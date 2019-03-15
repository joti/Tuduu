package hu.joti.tuduu.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TuduuDBHelper extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "tuduu.db";
  private static final int DATABASE_VERSION = 1;

  private static TuduuDBHelper helper;

  private TuduuDBHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  public static TuduuDBHelper instance(Context context){
    if (helper == null){
      helper = new TuduuDBHelper(context.getApplicationContext());
    }
    return helper;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE category (ca_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)");
    db.execSQL("CREATE TABLE task (ta_id INTEGER PRIMARY KEY AUTOINCREMENT, ca_id INTEGER, name TEXT, description TEXT, priority INTEGER, deadline TEXT, donedate TEXT)");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    //ALTER TABLE lesz későbbi verzióknál
  }
}
