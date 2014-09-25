package com.qualoutdoor.recorder.persistent;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Class representing storage table into database
 * */
public class TableDB {
    
    /**name of the table*/
    private String name;
    /**list of the table's columns : name and nature of them*/
    private HashMap<String, String> columns;

    /**
     * Constructor
     */
    public TableDB(String n, HashMap<String, String> col) {
        this.name = n;
        this.columns = col;
    }

    /**
     * other constructor : columns name and type are given distinctly
     * */
    public TableDB(String n, String[] value, String[] type) {
        if (value.length == type.length) {
            this.name = n;
            this.columns = new HashMap<String, String>();
            for (int i = 0; i < value.length; i++) {
                this.columns.put(value[i], type[i]);
            }
        } else {
            throw new RuntimeException(
                    "TableDB creation : number of columns and number of types must be equal");
        }

    }

    /**
     * return table name
     * */
    public String getName() {
        return this.name;
    }

    /**
     * return the list of columns' name
     * */
    public ArrayList<String> getColumsName() {
        ArrayList<String> columnsName = new ArrayList<String>();
        for (int i = 0; i < this.columns.size(); i++) {
            columnsName.add(this.columns.keySet().toArray()[i].toString());
        }
        return columnsName;
    }

    /**
     * return the SQL statement into a string that order to create into SQL database
     * the table described by the object
     */
    public String createTableintoDB() {
        String columnsFields = "";
        int i = 0;
        for (String col : this.getColumsName()) {
            if (i != 0) {
                columnsFields = columnsFields + ",";
            }
            columnsFields = columnsFields + " " + col + " "
                    + this.columns.get(col) + " ";
            i++;
        }
        String request = "CREATE TABLE " + this.name + " ( " + columnsFields
                + " );";
        return request;
    }

}
