package com.ai.planetsdb.util;


@SuppressWarnings("PointlessBitwiseExpression")
final public class Utils {

    // Set to 0 until we have UI to perform undo
//  public static final long UNDO_DELAY = 0;

    // When edit view started it has status MODIFY_UNINITIALIZED till
    // all views are filled with initial data by worker thread
    public static final int MODIFY_UNINITIALIZED = 0;
    public static final int MODIFY_ALL = 1;

    // When the edit planet view finishes it passes back the appropriate exit
    // code.
    public static final int DONE_EXIT = 1 << 0;
    public static final int DONE_SAVE = 1 << 1;
    public static final int DONE_DELETE = 1 << 2;

    // Bitfield of view validation errors
    public static final int ERROR_EMPTY_NAME = 1 << 0;

    public static final String ACTION_SAVE_OR_DELETE_COMPLETED = "saveCompleted";

    private Utils() {
    }
}
