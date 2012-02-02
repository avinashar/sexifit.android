package com.sexifit.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.sexifit.android.R;

/**
 
"CREATE TABLE workouts_sets
(id INTEGER PRIMARY KEY
,actual_weight DECIMAL
,actual_reps INTEGER
,actual_exercise VARCHAR2(45)
,actual_datetime TIMESTAMP
,expected_weight DECIMAL
,expected_reps INTEGER
,expected_exercise VARCHAR2(45)
,expected_datetime TIMESTAMP
,position INTEGER
,workout_id INTEGER
,start_end_flag INTEGER
,create_flag INTEGER
,update_flag INTEGER
,delete_flag INTEGER
,modified_at TIMESTAMP
,created_at TIMESTAMP 
);"

*/

/**
 * Simple workout_sets database access helper class. Defines the basic CRUD operations
 * for the workout app, and gives the ability to list workout_sets as well as
 * retrieve or modify a specific workout_set.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class WorkoutSetsDbAdapter {

    private static final String TAG 							= "WorkoutSetsDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
	
	private static final String DATABASE_NAME 					= "sexifit_transactional";
    private static final String DATABASE_TABLE 					= "workout_sets";
    private static final int DATABASE_VERSION 					= 2;
	
    public static final String KEY_ACTUAL_WEIGHT 				= "actual_weight";
    public static final String KEY_ACTUAL_REPS 					= "actual_reps";
    public static final String KEY_ROWID 						= "_id";
    public static final String KEY_EXERCISE_SET_ID 				= "exercise_set_id";
	public static final String KEY_SUGGESTED_DATETIME 			= "suggested_date_time";
	public static final String KEY_SUGGESTED_EXERCISE 			= "suggested_exercise";
	public static final String KEY_SUGGESTED_WEIGHT 			= "suggested_weight";
	public static final String KEY_SUGGESTED_REPS 				= "suggested_reps";
	public static final String KEY_ACTUAL_DATETIME 				= "recorded_date_time";
	public static final String KEY_RECORDED_EXERCISE 			= "recorded_exercise";
	public static final String KEY_RECORDED_WEIGHT 				= "recorded_weight";
	public static final String KEY_RECORDED_REPS 				= "recorded_reps";
	public static final String KEY_ID 							= "_id";
	public static final String KEY_MODIFIED_AT 					= "modified_at";	
	public static final String KEY_CREATED_AT 					= "created";

    /**
     * Database Creation SQL Statement
     */
    private static final String DATABASE_CREATE =
    	"CREATE TABLE " 			+ DATABASE_TABLE + " ("
        + KEY_ROWID 				+ " INTEGER PRIMARY KEY, "
        + KEY_ACTUAL_WEIGHT 		+ " TEXT, "
        + KEY_ACTUAL_REPS 			+ " TEXT, " 
        + KEY_SUGGESTED_EXERCISE 	+ " TEXT, "
    	+ KEY_SUGGESTED_WEIGHT 		+ " TEXT, "
    	+ KEY_SUGGESTED_REPS 		+ " TEXT, "
        + KEY_RECORDED_EXERCISE 	+ " TEXT, "
    	+ KEY_RECORDED_WEIGHT 		+ " TEXT, "
    	+ KEY_RECORDED_REPS 		+ " TEXT, "
    	+ KEY_MODIFIED_AT 			+ " INTEGER "
		+ ");";

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public WorkoutSetsDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public WorkoutSetsDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new note using the actual_weight and actual_reps provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param actual_weight the actual_weight of the note
     * @param actual_reps the actual_reps of the note
     * @return rowId or -1 if failed
     */
    public long createWorkoutSet(
    						String actual_weight 
    						,String actual_reps
    						,String suggested_exercise
    						,Integer suggested_weight
    						,Integer suggested_reps
    						,String recorded_exercise
    						,Integer recorded_weight
    						,Integer recorded_reps
    						//,long modified_at
    						//,Integer created_at
    						) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ACTUAL_WEIGHT, actual_weight);
        initialValues.put(KEY_ACTUAL_REPS, actual_reps);
        initialValues.put(KEY_SUGGESTED_EXERCISE, suggested_exercise);
        initialValues.put(KEY_SUGGESTED_WEIGHT, suggested_weight);
        initialValues.put(KEY_SUGGESTED_REPS, suggested_reps);
        initialValues.put(KEY_RECORDED_EXERCISE, recorded_exercise);
        initialValues.put(KEY_RECORDED_WEIGHT, recorded_weight);
        initialValues.put(KEY_RECORDED_REPS, recorded_reps);
        //initialValues.put(KEY_CREATED_AT, created_at);
        //initialValues.put(KEY_MODIFIED_AT, modified_at);
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }
    
    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteWorkoutSet(long rowId) {
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor fetchAllWorkoutSets() {
        return mDb.query(DATABASE_TABLE, new String[] 
                        {
        				KEY_ROWID 
        				,KEY_ACTUAL_WEIGHT
        				,KEY_ACTUAL_REPS
        				,KEY_SUGGESTED_EXERCISE 
        				,KEY_SUGGESTED_WEIGHT
        				,KEY_SUGGESTED_REPS
        				,KEY_RECORDED_EXERCISE 
        				,KEY_RECORDED_WEIGHT
        				,KEY_RECORDED_REPS
        				//,KEY_CREATED_AT
        				//,KEY_UPDATED_AT
        				}, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchWorkoutSet(long rowId) throws SQLException {
        Cursor mCursor =
            mDb.query(true, DATABASE_TABLE, new String[] 
            			           {
            						KEY_ROWID
            						,KEY_ACTUAL_WEIGHT
            						,KEY_ACTUAL_REPS
                    				,KEY_SUGGESTED_EXERCISE 
                    				,KEY_SUGGESTED_WEIGHT
                    				,KEY_SUGGESTED_REPS
                    				,KEY_RECORDED_EXERCISE 
                    				,KEY_RECORDED_WEIGHT
                    				,KEY_RECORDED_REPS
                    				//,KEY_CREATED_AT
                    				,KEY_MODIFIED_AT
            						}, KEY_ROWID + "=" + rowId,
            						null, null, null, null, null
            						);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the actual_weight and actual_reps
     * values passed in
     * 
     * @param rowId id of note to update
     * @param actual_weight value to set note actual_weight to
     * @param actual_reps value to set note actual_reps to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateWorkoutSet(
								long rowId 
								,String actual_weight 
								,String actual_reps
								//,String suggested_exercise
	    						//,Integer suggested_weight
	    						//,Integer suggested_reps
	    						//,String recorded_exercise
	    						//,Integer recorded_weight
	    						//,Integer recorded_reps
	    						//,Integer created_at
	    						,long modified_at
								) 
    {
        ContentValues args = new ContentValues();
        args.put(KEY_ACTUAL_WEIGHT, actual_weight);
        args.put(KEY_ACTUAL_REPS, actual_reps);
        //args.put(KEY_SUGGESTED_EXERCISE, suggested_exercise);
        //args.put(KEY_SUGGESTED_WEIGHT, suggested_weight);
        //args.put(KEY_SUGGESTED_REPS, suggested_reps);
        //args.put(KEY_RECORDED_EXERCISE, recorded_exercise);
        //args.put(KEY_RECORDED_WEIGHT, recorded_weight);
        //args.put(KEY_RECORDED_REPS, recorded_reps);
        //args.put(KEY_CREATED_AT, created_at);
        args.put(KEY_MODIFIED_AT, modified_at);
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
