package cs309.a1.player.activities;

import android.app.Activity;
import android.os.Bundle;
import cs309.a1.player.R;

public class ShowCardsActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player_hand);
	}

}
