package cs309.a1.player;

import android.app.Activity;
import android.widget.TextView;

public class ConnectionFailActivity extends Activity{
	
	@Override
	protected void onStart() {
		super.onStart();
		setContentView(R.layout.information);

		TextView title = (TextView) findViewById(R.id.informationTitle);
		title.setText(R.string.ConnectFailActivity_title);
		
	}
}
