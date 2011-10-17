package net.acuttone.reddimg;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.login);
		Button button = (Button) findViewById(R.id.btnLogin);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new LoginTask().execute(null);
			}

		});
	}

	class LoginTask extends AsyncTask<Void, Void, Boolean> {

		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = ProgressDialog.show(LoginActivity.this, "", "Logging in...");
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			EditText etUsername = (EditText) findViewById(R.id.etUsername);
			EditText etPassword = (EditText) findViewById(R.id.etPassword);
			String username = etUsername.getText().toString();
			String password = etPassword.getText().toString();
			boolean success = RedditApplication.instance().getRedditClient().doLogin(username, password);
			return success;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			progressDialog.dismiss();
			AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
			alertDialog.setTitle("Reddimg");
			alertDialog.setMessage(result ? "Logon successful" : "Logon failed");
			alertDialog.setCancelable(false);
			alertDialog.setButton(Dialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					LoginActivity.this.finish();
				}

			});
			alertDialog.show();
		}
	}
}