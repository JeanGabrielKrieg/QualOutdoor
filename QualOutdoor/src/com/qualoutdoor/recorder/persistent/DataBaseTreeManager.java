package com.qualoutdoor.recorder.persistent;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Class that permits to insert new measures into local database system while
 * following tree architecture
 * */
public class DataBaseTreeManager {
    /** Database to work on */
    private SQLiteDatabase db;
    /** table of the database where the tree is stored */
    private TableDB table;
    /** cursor moving on the tree nodes */
    private TreeCursor cursor;

    public DataBaseTreeManager(SQLiteDatabase db, TableDB table) {
        this.db = db;
        this.table = table;
        this.cursor = new TreeCursor();
        this.cursor.init();
    }

    /** returns the last line of the sub tree engendered by the pointed node */
    public int getSubTreeBoundary() {
        int lastChild;
        // preparing SQL statement : searching next node brother/uncle
        String selectQuery = "SELECT line , VALUE , LEVEL  FROM "
                + table.getName()
                + " WHERE level <= ? AND line > ?  ORDER BY LINE ASC ";
        // executing it
        Cursor c = db.rawQuery(
                selectQuery,
                new String[] {
                        Integer.toString(this.cursor.level),
                        Integer.toString(this.cursor.line)
                });
        if (c.moveToFirst()) {
            lastChild = c.getInt(0) - 1;
        } else {
            // if no line is found, sub tree spread untill the end of the
            // references table
            // counting the remaining lines
            String countQuery = "SELECT Count(*) FROM " + table.getName();
            Cursor c2 = db.rawQuery(countQuery, null);
            c2.moveToFirst();
            lastChild = c2.getInt(0) + 1;
            c2.close();
        }
        c.close();
        return lastChild;
    }

    /**
     * Checks if a specified node is included in sub tree engendered by pointed
     * node and is the direct child of the pointed node. If found, true is
     * returned and manager will point on it, else it won't move and returns
     * false
     */
    public boolean hasSon(int ref) {
        boolean result;
        // getting subtree boundary in order to edge searching interval
        int boundary = getSubTreeBoundary();
        // research query
        String selectQuery = "SELECT  LINE , VALUE , LEVEL  FROM "
                + table.getName()
                + " WHERE VALUE = ? AND line <= ? AND line > ?";
        // execution of the query
        Cursor c = db.rawQuery(selectQuery,
                new String[] {
                        Integer.toString(ref), Integer.toString(boundary),
                        Integer.toString(this.cursor.line)
                });
        // if a node is found, and if it is a direct child
        if (c.moveToFirst() && cursor.getLevel() == c.getInt(2) - 1) {
            this.cursor.update(c.getInt(0), c.getInt(1), c.getInt(2));
            result = true;
            // if not
        } else {
            result = false;
        }
        c.close();
        return result;
    }

    /**
     * Method that inserts a new line associated to a non leaf node into
     * reference table just under the pointed one with the specified reference.
     * Cursor is updated on it after insertion
     */
    public void insert(int ref) {
        // incrementing index of line under the insertion index
        String updateQuery1 = "UPDATE " + table.getName()
                + " SET line = line + 1 WHERE LINE > " + this.cursor.line + ";";
        db.execSQL(updateQuery1);
        // new line insertion
        String insertQuery = "INSERT INTO " + table.getName()
                + " (LINE,VALUE,LEVEL) VALUES (" + (this.cursor.line + 1) + ","
                + ref + "," + (this.cursor.level + 1) + ");";
        db.execSQL(insertQuery);
        // cursor updating
        this.cursor.update(this.cursor.line + 1, ref, this.cursor.level + 1);
    }

    /**
     * Method that inserts a new line associated to a leaf node into reference
     * table just under the pointed one with the specified reference, cursor is
     * not updated after insertion
     */
    public void insertLeaf(int ref) {
        // incrementing index of line under the insertion index
        String updateQuery1 = "UPDATE " + table.getName()
                + " SET line = line + 1 WHERE LINE > " + this.cursor.line + ";";
        db.execSQL(updateQuery1);
        // new line insertion
        String insertQuery = "INSERT INTO " + table.getName()
                + " (LINE,VALUE,LEVEL) VALUES (" + (this.cursor.line + 1) + ","
                + ref + "," + (this.cursor.level + 1) + ");";
        db.execSQL(insertQuery);
    }

    /**
     * Function that make cursor focus on the child of the pointed line having
     * the specified reference if it doesn't exist this child is create
     * */
    public void findOrCreate(int ref) {
        if (!this.hasSon(ref)) {
            this.insert(ref);
        }
    }

    /**
     * Method that makes cursor pointing on the father of the current pointed
     * node. If current line doesn't not have father, cursor won't move.
     * */
    public void getFather() throws DataBaseException {
        if (this.cursor.level != 0) {
            // the father of the current line is the nearest above which had a
            // level equals to the
            // current level +1
            // here is the associated query
            String selectQuery = "SELECT max( LINE ) , VALUE , LEVEL FROM "
                    + this.table.getName() + " WHERE LINE < ? AND LEVEL = ?";
            Cursor c = db.rawQuery(
                    selectQuery,
                    new String[] {
                            Integer.toString(this.cursor.line),
                            Integer.toString(this.cursor.level - 1)
                    });
            if (c.moveToFirst()) {
                // father is found : cursor is updated
                this.cursor.update(c.getInt(0), c.getInt(1), c.getInt(2));
            } else {
                throw new DataBaseException(
                        "TREE MANAGER GET FATHER : can't find node father");
            }
            c.close();
        }
    }

    /** Method reseting manager on tree root */
    public void reset() {
        this.cursor.init();
    }

    /** Returns the cursor's current position */
    public TreeCursor getCursor() {
        return this.cursor;
    }

    /**
     * Makes manager move to the node associated to the next line in the
     * reference table returns true if this line exists, if not it returns false
     */
    public boolean moveToNextLine() {
        boolean exists;
        String selectQuery = "SELECT  LINE, VALUE , LEVEL FROM "
                + this.table.getName() + " WHERE LINE = ?";
        Cursor c = db.rawQuery(selectQuery, new String[] {
            Integer.toString(this.cursor.line + 1)
        });
        if (c.moveToFirst()) {
            this.cursor.update(c.getInt(0), c.getInt(1), c.getInt(2));
            exists = true;
        } else {
            exists = false;
        }
        c.close();
        return exists;
    }

    /** Internal class that describes cursor objects */
    public class TreeCursor {
        /** Current pointed line's index */
        private int line;
        /** Current pointed line's reference */
        private int reference;
        /** Current pointed line's level */
        private int level;

        /** returns cursor's level */
        public int getLevel() {
            return this.level;
        }

        /** returns cursor's reference */
        public int getReference() {
            return this.reference;
        }

        /** returns cursor's line */
        public int getLine() {
            return this.line;
        }

        /** initialize cursor on the root */
        public void init() {
            this.line = 2;
            this.reference = 0;
            this.level = 0;
        }

        /** update cursor with specified attributes */
        public void update(int li, int ref, int lev) {
            this.line = li;
            this.reference = ref;
            this.level = lev;
        }
    }

}
