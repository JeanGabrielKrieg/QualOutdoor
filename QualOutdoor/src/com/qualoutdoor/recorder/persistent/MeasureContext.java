package com.qualoutdoor.recorder.persistent;

import java.util.ArrayList;

import android.util.Log;
/**
 * Class representing a particular path into the three, used to keep in
 * memory last insertion location for minimizing manager moving on
 * the tree*/

public class MeasureContext {

    private static final int TREE_LENGTH = 6;

    public static final int GROUP_INDEX = 0;
    public static final int USER_INDEX = 1;
    public static final int MCC_INDEX = 2;
    public static final int MNC_INDEX = 3;
    public static final int NTC_INDEX = 4;
    public static final int METRIC_INDEX = TREE_LENGTH - 1;

    /**Tree length here it equals to 6 : 
     * 0_GROUP 1_USER 2_MCC, 3_MNC, 4_NTC, 5_Metric
     * boundaries are not considered
     */
    private int lengthTree; 
    /**list of the stages of the associated path*/
    private ArrayList<Integer> stages; 
    /**cursor pointing on a stage of the stages list*/
    private int cursor;

    public MeasureContext() {
        this.lengthTree = TREE_LENGTH;
        this.stages = new ArrayList<Integer>();
        for (int i = 0; i < this.lengthTree; i++) {
            //every stages of the list is initialized to 0
            this.stages.add(0);
        }
        //cursor is pointing at the beginning of the path
        this.cursor = 0;
    }
    
    /** 
     * Return a deep copy of this instance 
     * */
    public MeasureContext clone() {
        // Create a new measure context
        MeasureContext clone = new MeasureContext();
        clone.cursor = this.cursor;
        clone.lengthTree = this.lengthTree;
        // Clone the stages attribute
        clone.stages = new ArrayList<Integer>(this.stages.size());
        // Clone each value
        for(Integer integer : this.stages) {
            // Clone the integer value
            clone.stages.add(integer.intValue());
        }
        return clone;
    }

    /**returns tree's length*/
    public int getlength() {
        return this.lengthTree;
    }
    
    /**returns cursor's index*/
    public int getCursor() {
        return this.cursor;
    }
    
    /**returns stages located at the specified index*/
    public int getStage(int index) {
        return this.stages.get(index);
    }
    
    /**set cursor to the beginning of the path*/
    public void resetCursor() {
        this.cursor = 0;
    }

    /**set stages list fields*/
    public void set(int index, int value) {
        this.stages.set(index, value);
    }

   /**Checking if the context has all its fields correctly set*/
    public boolean isCorrectlySet() {
        int i = 0;
        for (int stage : this.stages) {
            if (stage == 0 && i != this.lengthTree - 1) {
              //last node is not checked because its set just before insertion  
                return false;
            }
            i++;
        }
        return true;
    }


    /**moving cursor one step to the leaf in the path*/
    public void moveToChild() {
        if (this.cursor < this.stages.size() - 1) {
            //if end is reached cursor doesn't move
            this.cursor++;
        }
    }

    /**returns whether cursor pointing on the end of the path*/
    public boolean isAtEnd() {
        return (this.cursor == this.stages.size() - 1);
    }

   /**resetting context : stages are set to 0 and cursor is recovered to start*/
    public void reset() {
        Log.d("DEBUG CONTEXTE", "RESET PROCEEDING");
        for (int i = 0; i < this.lengthTree; i++) {
            this.stages.set(i, 0);// on reinitialise toutes les valeurs du
                                  // contexte � 0
        }
        this.cursor = 0;// on pointe au sommet de l'arbre
    }

}
