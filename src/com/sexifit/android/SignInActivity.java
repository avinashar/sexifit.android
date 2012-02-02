package com.sexifit.android;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;


public class SignInActivity extends Activity implements OnClickListener {
	
	TextToSpeech talker;
	
	private EditText mEmailField;
	private EditText mPasswordField;
	private JSONObject jObject;
	private SignInDbAdapter mSignInDbHelper;
	private String mAuthToken;
	private Long now;
	private ProgressDialog mProgressDialog;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);
        setClickListeners();
        mSignInDbHelper = new SignInDbAdapter(this);
        mSignInDbHelper.open();
        
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Downloading Workout");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(true);
        
    }
    
	/** Get a handle to all user interface elements */
	private void findViews() {
		EditText mEmailField = (EditText) findViewById(R.id.email_field);
        EditText mPasswordField = (EditText) findViewById(R.id.password_field);
	}
    
    public void setClickListeners() {
        View signInButton = findViewById(R.id.btn_sign_in);
        signInButton.setOnClickListener(this);
    }

	public void onClick(View v) {			
		switch (v.getId()) {
		
    	case R.id.btn_sign_in:   		
    		v.performHapticFeedback(HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
    		mProgressDialog.show();
    		authenticate();
    		mProgressDialog.cancel();
    		break;
		}
		
	}
    
	private void authenticate()
	{
		EditText mEmailField = (EditText) findViewById(R.id.email_field);
        EditText mPasswordField = (EditText) findViewById(R.id.password_field);
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();
        Log.e("Authentication JSON", "EMAIL = "+ email);
        Log.e("Authentication JSON", "PASSWORD = "+ password);
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost("http://sexifit.heroku.com/sessions");
		//HttpPost post = new HttpPost("http://192.168.1.118:3000/sessions");
	    JSONObject holder = new JSONObject();
	    JSONObject userObj = new JSONObject();  
	    try {
	    	userObj.put("password", password);
	    	userObj.put("email", email);   
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
	    try {
	    	mAuthToken = parseToken(response);
	    	mSignInDbHelper.createSession(mEmailField.getText().toString(),mAuthToken);
	    	// now = Long.valueOf(System.currentTimeMillis());
	    	// mSignInDbHelper.createSession(mEmailField.getText().toString(),mAuthToken,now);	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public String parseToken(String jsonResponse) throws Exception {
		jObject = new JSONObject(jsonResponse);
		JSONObject sessionObject = jObject.getJSONObject("session");
		String attributeError = sessionObject.getString("error");
		Toast.makeText(this, attributeError, Toast.LENGTH_LONG).show();
		String attributeToken = sessionObject.getString("auth_token");
		return attributeToken;
	}
	
}