package com.radiusnetworks.lib;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by user on 2016/11/29.
 */

public class SQLite extends SQLiteOpenHelper implements Serializable
{
    private final static String TAG = SQLite.class.getName();

    private final static int dbVersion = 1;
    private final static String dbName = "beacon.db";

    public final static String tableName_Beacon = "Beacon";

    private final File sqlitePath = new File("/sdcard/" + dbName);

    private SQLiteDatabase db = null;

    private Activity activity;
    private Context context;

    public SQLite(Activity activity, Context context)
    {
        super(context, dbName, null, dbVersion);

        this.activity = activity;
        this.context = context;

        if (!sqlitePath.exists())
        {
            try
            {
                sqlitePath.createNewFile();
                Log.d(TAG, "Create File: " + sqlitePath.getPath());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        db = SQLiteDatabase.openDatabase(sqlitePath.getPath(), null, SQLiteDatabase.OPEN_READWRITE);

        String sqliteStmt =
                "CREATE TABLE IF NOT EXISTS " + tableName_Beacon + "( " +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "_UUID TEXT, " +
                        "_Major INT, " +
                        "_Minor INT, " +
                        "_Proximity INT, " +
                        "_Rssi INT, " +
                        "_TxPower INT, " +
                        "_arrayL DOUBLE" +
                        "_dateT TEXT" +
                        " );";

        db.execSQL(sqliteStmt);
    }

    @Override
    public synchronized void close()
    {
        if (db != null)
            this.db.close();

        this.db = null;

        this.activity = null;
        this.context = null;

        super.close();
    }

    public void add(String aTable, String _UUID, int[] _INFO, double _INFO2, String _TIME)
    {
        ContentValues cv = new ContentValues();

        cv.put("_UUID",			_UUID);
        cv.put("_Major",		_INFO[0]);
        cv.put("_Minor",		_INFO[1]);
        cv.put("_Proximity",	_INFO[2]);
        cv.put("_Rssi",			_INFO[3]);
        cv.put("_TxPower",		_INFO[4]);
        cv.put("_arrayL",		_INFO2);
        cv.put("_dateT",		_TIME);

        db.insert(aTable, null, cv);

        Log.d(TAG, "add");
    }

    @Override
    public void onCreate(SQLiteDatabase db)	{}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}