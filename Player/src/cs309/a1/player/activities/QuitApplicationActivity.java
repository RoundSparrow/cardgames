package cs309.a1.player.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import cs309.a1.shared.R;

public class QuitApplicationActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.quitgameprompt);

		TextView body = (TextView) findViewById(R.id.textView1);
		body.setText(R.string.exit_application);
		
		Button affirmative = (Button) findViewById(R.id.affirmative);
		affirmative.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_OK);
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});

		Button negative = (Button) findViewById(R.id.negative);
		negative.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
	}

}
