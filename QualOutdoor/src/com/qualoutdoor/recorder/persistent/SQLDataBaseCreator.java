package com.qualoutdoor.recorder.persistent;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Class that create tables into database
 * */

public class SQLDataBaseCreator extends SQLiteOpenHelper{
	
    /**Database name*/
	private static final String DATABASE_NAME = "recorder.db";
	/**Database version*/
	private static final int DATABASE_VERSION = 1;
	/**Table storing tree architecture*/
	private TableDB table_reference;
	/**Table storing leaves' details*/
	private TableDB table_measure;
	
	
	/**
	 * Constructor
	 * tableDB objects are created
	 * */
	public SQLDataBaseCreator(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.table_reference = new TableDB("recorder_tt",new String[] {"LINE","VALUE","LEVEL"}, new String[] {"INTEGER","INTEGER NOT NULL","INTEGER"});
		this.table_measure = new TableDB("measure_it",new String[] {"ID","DATE","LAT","LNG","DATA"}, new String[] {"INTEGER PRIMARY KEY AUTOINCREMENT","TIMESTAMP default (strftime('%s', 'now'))","REAL","REAL","VARCHAR"});
		
	}
	
	/**
	 * In case of dataBase upgrading : existing tables are replaced with new ones
	 * */
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//dropping existing tables 
		db.execSQL("DROP TABLE IF EXISTS '" + this.table_reference.getName() + "' ");
		db.execSQL("DROP TABLE IF EXISTS '" + this.table_measure.getName() + "'");
		//creating them again
		onCreate(db);
	}
	
	/**
	 * this function is called only when database is not initialized.
	 * Calling SQL statements for creating tables
	 * 
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {	
			//calling creation statements
			db.execSQL(table_reference.createTableintoDB());
			db.execSQL(table_measure.createTableintoDB());
			//calling statement for initialize reference table with a root line
			db.execSQL("INSERT INTO "+this.table_reference.getName()+" (LINE,VALUE,LEVEL) VALUES (2,0,0); ");
	}
	
	
	/**
	 * returns the object associated to the reference table into database
	 * */
	public TableDB getTableReference() {
			return this.table_reference;
	}

	 /**
     * returns the object associated to the measure table into database
     * */
	public TableDB getTableMeasure(){
		return this.table_measure;
	}
	
	
	
}
