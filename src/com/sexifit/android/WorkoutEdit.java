package com.sexifit.android;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;


public class WorkoutEdit extends Activity implements TextToSpeech.OnInitListener {
 
	TextToSpeech talker;
	
    private Long mRowId;
    private WorkoutSetsDbAdapter mWorkoutSetsDbHelper;
    
    private Spinner mExerciseSpinner;
    private Spinner mWeightSpinner;
    private Spinner mRepSpinner;
    private String exercises_array[];
	private String weights_array[];
	private String reps_array[];
	
	private String suggested_exercise = "Dumbbell Bench Press";
	private String suggested_weight = "205";
	private String suggested_reps = "9";
	
	private String recorded_exercise = "";
	private String recorded_weight = "";
	private String recorded_reps = "";
	
	private String mActualWeight;
    private String mActualReps;
    private String mActualExercise;
    private String mActualDateTime;
	private String mSuggestedWeight;
    private String mSuggestedReps;
    private String mSuggestedExercise;
    private String mSuggestedDateTime;
    private Long mModifiedAt;
    
	private Long now; 
    //Long now = Long.valueOf(System.currentTimeMillis());
	
    // Identifiers for our menu items.
    private static final int PREVIOUS_WORKOUT_SET = Menu.FIRST;
    private static final int NEXT_WORKOUT_SET = Menu.FIRST + 1;
    private static final int EXERCISE_VIDEO = Menu.FIRST + 2;
    private static final int EXERCISE_PICTURE = Menu.FIRST + 3;
    private static final int EXERCISE_TEXT = Menu.FIRST + 4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWorkoutSetsDbHelper = new WorkoutSetsDbAdapter(this);
        mWorkoutSetsDbHelper.open();
        setContentView(R.layout.workout_edit);
        setTitle(R.string.record_results);
        
        talker = new TextToSpeech(this, this);
        
        Button confirmButton = (Button) findViewById(R.id.confirm);
        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(WorkoutSetsDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(WorkoutSetsDbAdapter.KEY_ROWID)
									: null;
		}
        
        Cursor another_note = mWorkoutSetsDbHelper.fetchWorkoutSet(mRowId);
        startManagingCursor(another_note);
        recorded_exercise = another_note.getString(another_note.getColumnIndexOrThrow(WorkoutSetsDbAdapter.KEY_RECORDED_EXERCISE));
        
        exercises_array=new String[1];
        exercises_array[0]=recorded_exercise;
        
        mExerciseSpinner = (Spinner) findViewById(R.id.exercise_spinner);
        ArrayAdapter exercise_adapter = new ArrayAdapter(this,
        		android.R.layout.simple_spinner_item, exercises_array);
        mExerciseSpinner.setAdapter(exercise_adapter);
        mExerciseSpinner.setSelection(0);
        Log.i("Exercise Spinner",mExerciseSpinner.getSelectedItem().toString());
        
        mWeightSpinner = (Spinner) findViewById(R.id.weight_spinner);
        ArrayAdapter<CharSequence> weight_adapter = ArrayAdapter.createFromResource(
                this, R.array.weights_string_array, android.R.layout.simple_spinner_item);
        weight_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mWeightSpinner.setAdapter(weight_adapter);
        mWeightSpinner.setSelection((Integer.parseInt(suggested_weight)/5));
        mWeightSpinner.getSelectedItemPosition();
        Log.i("Exercise Spinner",mExerciseSpinner.getSelectedItem().toString());
        
        mRepSpinner = (Spinner) findViewById(R.id.reps_spinner);
        ArrayAdapter<CharSequence> reps_adapter = ArrayAdapter.createFromResource(
                this, R.array.reps_string_array, android.R.layout.simple_spinner_item);
        reps_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRepSpinner.setAdapter(reps_adapter);
  
        mExerciseSpinner = (Spinner) findViewById(R.id.exercise_spinner);
        mWeightSpinner = (Spinner) findViewById(R.id.weight_spinner);
        mRepSpinner = (Spinner) findViewById(R.id.reps_spinner);
        
		populateFields();
        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }
        });
    }
    
    public void say(String text2say){
    	talker.speak(text2say, TextToSpeech.QUEUE_FLUSH, null);
    }

	@Override
	public void onInit(int status) {
		mExerciseSpinner.getSelectedItem();
		mActualWeight = Integer.toString(mWeightSpinner.getSelectedItemPosition()*5);
        mActualReps = Integer.toString(mRepSpinner.getSelectedItemPosition());
		say("Next Set! "+mExerciseSpinner.getSelectedItem()+". Weight: "+mActualWeight+"lb. Reps: "+mActualReps+"or more.");

	}

	@Override
	public void onDestroy() {
		if (talker != null) {
			talker.stop();
			talker.shutdown();
		}

		super.onDestroy();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        //menu.add(0, PREVIOUS_WORKOUT_SET, 0, R.string.menu_last_set).setIcon(R.drawable.ic_menu_back);
        //menu.add(0, NEXT_WORKOUT_SET, 0, R.string.menu_next_set).setIcon(R.drawable.ic_menu_forward);
        //menu.add(0, EXERCISE_VIDEO, 0, R.string.menu_exercise_video).setIcon(R.drawable.ic_menu_movie);
        //menu.add(0, EXERCISE_PICTURE, 0, R.string.menu_exercise_picture).setIcon(R.drawable.ic_menu_gallery);
        //menu.add(0, EXERCISE_TEXT, 0, R.string.menu_exercise_text).setIcon(R.drawable.ic_menu_text);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle all of the possible menu actions.
        switch (item.getItemId()) {
        case PREVIOUS_WORKOUT_SET:
            break;
        case NEXT_WORKOUT_SET:
            break;
        }
        return super.onOptionsItemSelected(item);
    }
        
    public void populateFields() {
        if (mRowId != null) {
            Cursor note = mWorkoutSetsDbHelper.fetchWorkoutSet(mRowId);
            startManagingCursor(note);
            recorded_exercise = note.getString(note.getColumnIndexOrThrow(WorkoutSetsDbAdapter.KEY_RECORDED_EXERCISE));
            recorded_weight = note.getString(note.getColumnIndexOrThrow(WorkoutSetsDbAdapter.KEY_ACTUAL_WEIGHT));
            recorded_reps = note.getString(note.getColumnIndexOrThrow(WorkoutSetsDbAdapter.KEY_ACTUAL_REPS));
            mExerciseSpinner.setSelection(0);
            mWeightSpinner.setSelection((Integer.parseInt(recorded_weight)/5));
            mRepSpinner.setSelection((Integer.parseInt(recorded_reps)));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(WorkoutSetsDbAdapter.KEY_ROWID, mRowId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }

    private void saveState() {
        mActualWeight = Integer.toString(mWeightSpinner.getSelectedItemPosition()*5);
        mActualReps = Integer.toString(mRepSpinner.getSelectedItemPosition());
        mModifiedAt = Long.valueOf(System.currentTimeMillis());

        
		if(mModifiedAt==null) {
			mModifiedAt=Long.valueOf(System.currentTimeMillis());
			Toast.makeText(this, Long.toString(mModifiedAt), Toast.LENGTH_LONG).show();
		}
		else {
		}
        
        if (mRowId == null) {
            long id = mWorkoutSetsDbHelper.createWorkoutSet(	
            										mActualWeight 	//actual_weight 
            										,mActualReps 	//actual_reps
            										,null 	//
            										,null 	//
            										,null 	//
            										,null 	//
            										,null 	//
            										,null	//
            										//,mModifiedAt	//
            										);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mWorkoutSetsDbHelper.updateWorkoutSet(	
            								mRowId 
            								,mActualWeight 
            								,mActualReps
            								,mModifiedAt
            								//,"189197"
            								// ADD MORE VARIABLES 
            								);
        }
    }

}
