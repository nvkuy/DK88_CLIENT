package com.example.dk88;



import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "DK88";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "StudentClass";

    private static final String KEY_STUDENTID = "studentId";
    private static final String KEY_CLASSID = "classId";
    private static final String KEY_HAVE = "have";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        String create_students_table = String.format("CREATE TABLE %s(%s TEXT PRIMARY KEY, %s TEXT PRIMARY KEY, %s INTEGER)", TABLE_NAME, KEY_STUDENTID, KEY_CLASSID, KEY_HAVE);
        String create_students_table = "CREATE TABLE StudentClass (" +
                "studentId TEXT," +
                "classId TEXT," +
                "have INTEGER," +
                "PRIMARY KEY (studentId, classId)" +
                ");";
        db.execSQL(create_students_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String drop_students_table = "DROP TABLE IF EXISTS StudentClass";
        db.execSQL(drop_students_table);

        onCreate(db);
    }
    public void addStudentClass(StudentClass studentClass) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_STUDENTID, studentClass.getStudentId());
        values.put(KEY_CLASSID, studentClass.getClassId());
        values.put(KEY_HAVE, studentClass.getHave());

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public List<StudentClass> getAllStudentClass() {
        List<StudentClass>  studentClassList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        while(cursor.isAfterLast() == false) {
            StudentClass studentClass = new StudentClass(cursor.getString(0), cursor.getString(1), cursor.getInt(2));
            studentClassList.add(studentClass);
            cursor.moveToNext();
        }
        return studentClassList;
    }



}
