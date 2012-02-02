package com.sexifit.android;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class WorkoutListActivity extends ListActivity {
	
	//private static final String URI						= 	"http://sexifit.heroku.com/"
	private static final String URI 						= 	"http://192.168.1.107:3000";
	private 			 String AUTH_TOKEN 					= 	"";
	private static final String RESOURCE_WORKOUTS 			= 	"/simple_workouts";
	private static final String RESOURCE_ENTRIES 			= 	"/simple_entries";
	private static final String FORMAT_XML 					= 	"?format=xml";
	private static final String FORMAT_JSON 				= 	"?format=json";
	
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    private static final int MENU_ACTION_DOWNLOAD_WORKOUT	= Menu.FIRST;
    private static final int MENU_ACTION_UPLOAD_RESULTS 	= Menu.FIRST + 1;
    private static final int MENU_ACTION_MUSIC		 		= Menu.FIRST + 2;
    private static final int MENU_ACTION_PREFERENCES 		= Menu.FIRST + 3;
    private static final int MENU_ACTION_DELETE_ALL_NOTES 	= Menu.FIRST + 4;
    private static final int MENU_ACTION_DELETE_ID 			= Menu.FIRST + 5;
    
    private WorkoutSetsDbAdapter mWorkoutSetsDbHelper;
    private SignInDbAdapter mSessionsDbHelper;
    
    private Long now; 
    
    List<String> myList = new ArrayList();
    
    private ProgressDialog mProgressDialog;
    
    //Long now = Long.valueOf(System.currentTimeMillis());
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.workouts_list);
        mWorkoutSetsDbHelper = new WorkoutSetsDbAdapter(this);
        mWorkoutSetsDbHelper.open();
        mSessionsDbHelper = new SignInDbAdapter(this);
        mSessionsDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
        
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Downloading Workout");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(true);
        //mProgressDialog.show();
        
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_ACTION_DOWNLOAD_WORKOUT, 0, R.string.menu_get_new_notes)
    		.setIcon(R.drawable.ic_menu_import);
        menu.add(0, MENU_ACTION_UPLOAD_RESULTS, 0, R.string.menu_post_new_notes)
    		.setIcon(R.drawable.ic_menu_export);
        menu.add(0, MENU_ACTION_MUSIC, 0, R.string.menu_music)
		.setIcon(R.drawable.ic_menu_music_library);
        menu.add(0, MENU_ACTION_DELETE_ALL_NOTES, 0, R.string.menu_delete_all_notes)
    	.setIcon(R.drawable.ic_menu_delete);
        menu.add(0, MENU_ACTION_PREFERENCES, 0, R.string.menu_preferences)
    	.setIcon(R.drawable.ic_menu_preferences);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) { 
        	
            case MENU_ACTION_DOWNLOAD_WORKOUT:
            	mProgressDialog.show();
            	deleteAllNotes();
            	getWorkout();
                fillData();
                mProgressDialog.cancel();
                return true;
            case MENU_ACTION_UPLOAD_RESULTS:
            	mProgressDialog.show();
            	postNewWorkout();
            	//deleteAllNotes();
                fillData();
                mProgressDialog.cancel();
                return true;
            case MENU_ACTION_MUSIC:
            	Intent intent = new Intent(android.provider.MediaStore.INTENT_ACTION_MUSIC_PLAYER);
        		startActivityForResult(intent, 0);
                return true;
            case MENU_ACTION_DELETE_ALL_NOTES:
            	deleteAllNotes();
                fillData();
                return true;
            case MENU_ACTION_PREFERENCES:
        		initializeAuthToken();
        		return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

   

    private void createWorkoutSet() {
        Intent i = new Intent(this, WorkoutEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, WorkoutEdit.class);
        i.putExtra(WorkoutSetsDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }
    
    private void initializeAuthToken(){
    	Cursor sessionsCursor = mSessionsDbHelper.fetchAllSessions();
		startManagingCursor(sessionsCursor);
    	if (sessionsCursor.moveToLast()){
    		AUTH_TOKEN = sessionsCursor.getString(2);
    		String uri = "http://sexifit.heroku.com/workout_sets/generate?auth_token="+AUTH_TOKEN;
        	//String uri = "http://192.168.1.118:3000/generate?auth_token="+AUTH_TOKEN;
    		//Toast.makeText(this, uri, Toast.LENGTH_LONG).show();
    	} while (sessionsCursor.moveToNext());
    }
    
    private void getWorkout()
	{
      initializeAuthToken();
	  HttpClient httpClient = new DefaultHttpClient();
	  String xmlResponse;
	  try
	  {
		String url = "http://sexifit.heroku.com/workout_sets/generate?auth_token="+AUTH_TOKEN;
		//String url = "http://192.168.1.118:3000/workout_sets/generate?auth_token="+AUTH_TOKEN;
	    Log.d( "pottingerable", "performing get " + url );
	    HttpGet method = new HttpGet( new URI(url) );
	    method.setHeader("Accept", "application/json");
    	method.setHeader("Content-Type","application/json");
	    HttpResponse response = httpClient.execute(method);
	    if ( response != null )
	    {
	    	xmlResponse = getResponse(response.getEntity());
	        Log.i( "Pottinger", "received " + xmlResponse);
	    	parseXMLString(xmlResponse);
	    }
	    else
	    {
	      Log.i( "Gertig", "got a null response" );
	    }
	  } catch (IOException e) {
	    Log.e( "Error", "IOException " + e.getMessage() );
	  } catch (URISyntaxException e) {
	    Log.e( "Error", "URISyntaxException " + e.getMessage() );
	  }
	  
	}
    
    private void getNewWorkout()
	{	
    	initializeAuthToken();
    	DefaultHttpClient client = new DefaultHttpClient();
    	String uri = "http://sexifit.heroku.com/workout_sets/generate?auth_token="+AUTH_TOKEN;
    	//String uri = "http://192.168.1.118:3000/workout_sets/generate?auth_token="+AUTH_TOKEN;
     	HttpPost post = new HttpPost(uri);
	    JSONObject holder = new JSONObject();
	    JSONObject userObj = new JSONObject();  
	    try {
	    	userObj.put("scheduled_workout_index", "1");
	    	userObj.put("routine_id", "4");   
		    holder.put("exercise_set", userObj);
		    Log.e("Workout JSON Request", "Workout JSON Request = "+ holder.toString());
	    	StringEntity se = new StringEntity(holder.toString());
	    	post.setEntity(se);
	    	post.setHeader("Accept", "application/json");
	    	post.setHeader("Content-Type","application/json");
	    } catch (UnsupportedEncodingException e) {
	    	Log.e("Error",""+e);
	        e.printStackTrace();
	    } catch (JSONException js) {
	    	js.printStackTrace();
	    }

	    String response = null;
	    try {
	    	ResponseHandler<String> responseHandler = new BasicResponseHandler();
	        response = client.execute(post, responseHandler);
	        Log.i("AUTHENTICATION", "Received "+ response +"!");
	    } catch (ClientProtocolException e) {
	        e.printStackTrace();
	        Log.e("ClientProtocol",""+e);
	    } catch (IOException e) {
	        e.printStackTrace();
	        Log.e("IO",""+e);
	    }	    
	    parseXMLString(response);
	}
    
    public void postNewWorkout()
	{
    	initializeAuthToken();
		 Log.i( "Sexidroid", "Performing POST to workouts");
		 DefaultHttpClient client = new DefaultHttpClient();
		 JSONObject holder = new JSONObject();
		 JSONObject workoutObj = new JSONObject();
		 HttpPut post = new HttpPut("http://sexifit.heroku.com/workout_sets/"+"?auth_token="+AUTH_TOKEN);
		 //HttpPut post = new HttpPut("http://192.168.1.118:3000/workout_sets/"+"?auth_token="+AUTH_TOKEN);
		 Cursor notesCursor = mWorkoutSetsDbHelper.fetchAllWorkoutSets();
		 startManagingCursor(notesCursor);    
	        if (notesCursor.moveToFirst())
	        {
            do {  
       		 try {
     	    	workoutObj.put("actual_weight", notesCursor.getString(1));
     		    workoutObj.put("actual_reps", notesCursor.getString(2));
     		    holder.put("workout_set", workoutObj);
     		    //holder.put("workout", workoutObj);
     		    String workout_id = notesCursor.getString(4);
     		    Log.e("Workout JSON", "Workout JSON = "+workout_id);
     		    post = new HttpPut("http://sexifit.heroku.com/workout_sets/"+workout_id+"?auth_token="+AUTH_TOKEN);
     		    //post = new HttpPut("http://192.168.1.118:3000/workout_sets/"+workout_id+"?auth_token="+AUTH_TOKEN);
     		    Log.e("Workout JSON", "Workout JSON = "+ holder.toString());
     	    	StringEntity se = new StringEntity(holder.toString());
     	    	post.setEntity(se);
     	    	post.setHeader("Content-Type","application/json");
     	    } catch (UnsupportedEncodingException e) {
     	    	Log.e("Error",""+e);
     	        e.printStackTrace();
     	    } catch (JSONException js) {
     	    	js.printStackTrace();
     	    }
     	    HttpResponse response = null;
     	    try {
     	        response = client.execute(post);
     	    } catch (ClientProtocolException e) {
     	        e.printStackTrace();
     	        Log.e("ClientProtocol",""+e);
     	    } catch (IOException e) {
     	        e.printStackTrace();
     	        Log.e("IO",""+e);
     	    }
     	    HttpEntity entity = response.getEntity();
     	    if (entity != null) {
     	        try {
     	            entity.consumeContent();
     	        } catch (IOException e) {
     	        	Log.e("IO E",""+e);
     	            e.printStackTrace();
     	        }
     	    }
     	    Toast.makeText(this, "Your post was successfully uploaded", Toast.LENGTH_LONG).show();
            } while (notesCursor.moveToNext());
        }
	}
    
	private void authenticate()
	{
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost("http://sexifit.heroku.com/sessions");
		//HttpPost post = new HttpPost("http://192.168.1.118:3000/sessions");
	    JSONObject holder = new JSONObject();
	    JSONObject userObj = new JSONObject();  
	    try {
	    	userObj.put("password", "t4u2t4u2");
	    	userObj.put("email", "dpott197@gmail.com");   
		    holder.put("user", userObj);
		    Log.e("Authentication JSON", "Authentication JSON = "+ holder.toString());
	    	StringEntity se = new StringEntity(holder.toString());
	    	post.setEntity(se);
	    	post.setHeader("Accept", "application/json");
	    	post.setHeader("Content-Type","application/json");
	    } catch (UnsupportedEncodingException e) {
	    	Log.e("Error",""+e);
	        e.printStackTrace();
	    } catch (JSONException js) {
	    	js.printStackTrace();
	    }

	    String response = null;
	    try {
	    	ResponseHandler<String> responseHandler = new BasicResponseHandler();
	        response = client.execute(post, responseHandler);
	        Log.i("AUTHENTICATION", "Received "+ response +"!");
	    } catch (ClientProtocolException e) {
	        e.printStackTrace();
	        Log.e("ClientProtocol",""+e);
	    } catch (IOException e) {
	        e.printStackTrace();
	        Log.e("IO",""+e);
	    }	    
	    Toast.makeText(this, response, Toast.LENGTH_LONG).show();
	}
    
    public void parseXMLString(String xmlString) 
	{
		try {
	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(xmlString));
	        Document doc = db.parse(is);
	        NodeList nodes = doc.getElementsByTagName("workout");
	        Integer id;
			String actual_weight;
			String body;
			String suggested_exercise;
			Integer suggested_weight;
			Integer suggested_reps;
			String recorded_exercise;
			Integer recorded_weight;
			Integer recorded_reps;
			
			Integer exercise_set_id;
			String weight;
			String reps;
			String exercise;
			
			Integer workout_id;
	        for (int i = 0; i < nodes.getLength(); i++) {
	           Element element = (Element) nodes.item(i);          
	           
	           NodeList workoutExerciseSetId = element.getElementsByTagName("workout_id");
	           Element line = (Element) workoutExerciseSetId.item(0);
	           workout_id = Integer.parseInt(getCharacterDataFromElement(line));
	           Log.i( "Pottinger", "parsed id: " + workout_id);
	           
	           NodeList workoutWeight = element.getElementsByTagName("weight");
	           line = (Element) workoutWeight.item(0);
	           weight = getCharacterDataFromElement(line).trim();
	           Log.i( "Pottinger", "parsed weight: " + weight);
	           
	           NodeList workoutReps = element.getElementsByTagName("reps");
	           line = (Element) workoutReps.item(0);
	           reps = getCharacterDataFromElement(line).trim();
	           Log.i( "Pottinger", "parsed reps: " + reps);
	           
	           NodeList workoutExercise = element.getElementsByTagName("exercise");
	           line = (Element) workoutExercise.item(0);
	           exercise = getCharacterDataFromElement(line).trim();
	           Log.i( "Pottinger", "parsed exercise: " + exercise);
	        
	           Integer weightInt = 0;
	           Integer repsInt 	 = 0;
	           
	           //now = Long.valueOf(System.currentTimeMillis());
	           //now = null;
	           
	           mWorkoutSetsDbHelper.createWorkoutSet(
	        		   						weight, 
	        		   						reps, 
	        		   						exercise, 
	        		   						workout_id, 
	        		   						repsInt,
	        		   						exercise,
	        		   						weightInt,
	        		   						repsInt
	        		   						//now
	           							);
	        }
		}
	    catch (Exception e) {
	        e.printStackTrace();
	    }	
	}
	
	public static String getCharacterDataFromElement(Element e) 
	{
	    Node child = e.getFirstChild();
	    if (child instanceof CharacterData) {
	       CharacterData cd = (CharacterData) child;
	       return cd.getData();
	    }
	    return "?";
	}
	
	 @Override
	    public void onCreateContextMenu(ContextMenu menu, View v,
	            ContextMenuInfo menuInfo) {
	        super.onCreateContextMenu(menu, v, menuInfo);
	        menu.add(0, MENU_ACTION_DELETE_ID, 0, R.string.menu_delete);
	    }

	    @Override
	    public boolean onContextItemSelected(MenuItem item) {
	        switch(item.getItemId()) {
	            case MENU_ACTION_DELETE_ID:
	                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	                mWorkoutSetsDbHelper.deleteWorkoutSet(info.id);
	                fillData();
	                return true;
	        }
	        return super.onContextItemSelected(item);
	    }
	    
	    public String getResponse(HttpEntity entity)
		{
		  String response = "";
		  try
		  {
		    int length = (int) entity.getContentLength();
		    StringBuffer sb = new StringBuffer(length);
		    InputStreamReader isr = new InputStreamReader(entity.getContent(), "UTF-8");
		    char buff[] = new char[length];
		    int cnt;
		    while (( cnt = isr.read(buff, 0, length - 1 )) > 0 )
		    {
		      sb.append(buff, 0, cnt);
		    }
		      response = sb.toString();
		      isr.close();
		  } catch ( IOException ioe ) {
		    ioe.printStackTrace();
		  }
		  return response;
		}
	    
	    private void fillData()
	    {
	    	
	        Cursor notesCursor = mWorkoutSetsDbHelper.fetchAllWorkoutSets();
	        startManagingCursor(notesCursor);
	        
	        	        String[] from = new String[]{
	        							WorkoutSetsDbAdapter.KEY_RECORDED_EXERCISE
	        							,WorkoutSetsDbAdapter.KEY_ACTUAL_WEIGHT
	        							,WorkoutSetsDbAdapter.KEY_ACTUAL_REPS
	        							//,WorkoutSetsDbAdapter.KEY_MODIFIED_AT
	        							};
	        int[] to = new int[]		{
	        							R.id.exercise
	        							,R.id.weight
	        							,R.id.reps
	        							//,R.id.time
	        							};
	        SimpleCursorAdapter notes = new SimpleCursorAdapter(
	        													this, 
												        		R.layout.workouts_list_item, 
												        		notesCursor, 
												        		from, 
												        		to
												        		);
	        setListAdapter(notes);
	    }
	    
	    public void deleteAllNotes()
	    {
	    	Cursor notesCursor = mWorkoutSetsDbHelper.fetchAllWorkoutSets();
	        startManagingCursor(notesCursor);    
	        if (notesCursor.moveToFirst())
	        {
	            do {  
	            	Log.i("Pottinger",notesCursor.getString(0));
	                mWorkoutSetsDbHelper.deleteWorkoutSet(notesCursor.getInt(0));
	            } while (notesCursor.moveToNext());
	        }
	    }
}
