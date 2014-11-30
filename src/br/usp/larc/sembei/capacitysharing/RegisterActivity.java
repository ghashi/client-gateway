package br.usp.larc.sembei.capacitysharing;

import br.usp.larc.sembei.capacitysharing.crypto.MSSCryptoProvider;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		findViewById(R.id.register_button).setOnClickListener(registerListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.register, menu);
		return true;
	}

	View.OnClickListener registerListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			TextView activationCodeView = (TextView) findViewById(R.id.activation_code_edittext);
			if (activationCodeView.getText().toString().equals("")) {
				Toast toast = Toast.makeText(
						RegisterActivity.this.getApplicationContext(),
						"Please, fill in your activation code", Toast.LENGTH_LONG);
				toast.show();
			} else {
				MSSCryptoProvider mss = new MSSCryptoProvider(RegisterActivity.this);
				 findViewById(R.id.home_spinner).setVisibility(View.VISIBLE);
				 Toast toast = Toast.makeText(
				 RegisterActivity.this.getApplicationContext(),
				 "Generating Key Pair", Toast.LENGTH_LONG);
				 toast.show();
				new KeyGenTask().execute(mss);
			}
		}
	};

	private class KeyGenTask extends
			AsyncTask<MSSCryptoProvider, Integer, Void> {

		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			findViewById(R.id.home_spinner).setVisibility(View.INVISIBLE);
			Bundle extras = getIntent().getExtras();
			String supplicant = extras.getString(MainActivity.SUPPLICANT);

			if(supplicant.equals(MainActivity.CLIENT)){
				startActivity(new Intent(RegisterActivity.this,
						ClientActivity.class));
			} else if(supplicant.equals(MainActivity.GATEWAY)){
				startActivity(new Intent(RegisterActivity.this,
						GatewayActivity.class));
			}
		}

		protected Void doInBackground(MSSCryptoProvider... params) {
			MSSCryptoProvider mss = params[0];
			mss.keyGen();
			return null;
		}

	}
}
