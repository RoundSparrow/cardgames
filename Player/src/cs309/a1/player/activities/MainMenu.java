package cs309.a1.player.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import cs309.a1.player.R;
import cs309.a1.shared.SoundManager;

public class MainMenu extends Activity {

	private static final int QUIT_GAME = Math.abs("QUIT_GAME".hashCode());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Button play = (Button) findViewById(R.id.btPlay);
		Button about = (Button) findViewById(R.id.btAbout);
		Button rules = (Button) findViewById(R.id.btRules);

		//initialize the sounds
		SoundManager.initSounds(getApplicationContext());


		rules.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent ruleButtonClick = new Intent(MainMenu.this, RulesActivity.class);
				startActivity(ruleButtonClick);

				/* Ashley's Debugging Purposes */
				//if (Util.isDebugBuild() && User.isAshley()) {
				//Intent showCardsActivity = new Intent(MainMenu.this, ShowCardsActivity.class);
				//startActivity(showCardsActivity);
				//}
			}
		});

		about.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent aboutButtonClick = new Intent(MainMenu.this,	AboutActivity.class);
				startActivity(aboutButtonClick);
			}
		});

		play.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent playButtonClick = new Intent(MainMenu.this, ShowCardsActivity.class);
				startActivity(playButtonClick);
			}
		});

	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, QuitApplicationActivity.class);
		startActivityForResult(intent, QUIT_GAME);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == QUIT_GAME && resultCode == RESULT_OK){
			finish();
		}
	}
}
