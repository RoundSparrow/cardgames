package com.worthwhilegames.cardgames.shared.activities;

import static com.worthwhilegames.cardgames.shared.Constants.CONNECTION_TYPE;
import static com.worthwhilegames.cardgames.shared.Constants.CRAZY_EIGHTS;
import static com.worthwhilegames.cardgames.shared.Constants.DIFFICULTY_OF_COMPUTERS;
import static com.worthwhilegames.cardgames.shared.Constants.EASY;
import static com.worthwhilegames.cardgames.shared.Constants.GAME_TYPE;
import static com.worthwhilegames.cardgames.shared.Constants.LANGUAGE;
import static com.worthwhilegames.cardgames.shared.Constants.LANGUAGE_US;
import static com.worthwhilegames.cardgames.shared.Constants.NUMBER_OF_COMPUTERS;
import static com.worthwhilegames.cardgames.shared.Constants.PREFERENCES;
import static com.worthwhilegames.cardgames.shared.Constants.SOUND_EFFECTS;
import static com.worthwhilegames.cardgames.shared.Constants.SPEECH_VOLUME;
import static com.worthwhilegames.cardgames.shared.Constants.WIFI;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.worthwhilegames.cardgames.R;

/**
 * This class will be used as an activity for the preferences screen. Within
 * this activity a user will be able to set up their options for the current
 * game including choosing the locale, number of computer players, the difficult
 * of the AI players, volume control and options to enable and disable sounds.
 */
public class PreferencesActivity extends Activity {

	/**
	 * An AudioManager object to be used for controlling the game volume
	 */
	private AudioManager audioManager;

	/**
	 * A SharedPreferences object to get the preferences set by the user
	 */
	private SharedPreferences sharedPref;

	/**
	 * A SharedPreferences editor for changing the game preferences based on
	 * user input
	 */
	private SharedPreferences.Editor prefsEditor;

	/**
	 * A private Spinner variable for the number of computer option
	 */
	private Spinner numComputerSpinner;

	/**
	 * A private Spinner variable for the locale option
	 */
	private Spinner localeSpinner;

	/**
	 * A private Spinner variable for game type
	 */
	private Spinner gameSpinner;

	/**
	 * A private Spinner variable for the difficulty of computers
	 */
	private Spinner difficultySpinner;

	/**
	 * A private Spinner variable for the connection type
	 */
	private Spinner connectionSpinner;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences);

		// Get and change the title of the Preferences layout
		TextView title = (TextView) findViewById(R.id.title);
		title.setText(R.string.preferencesActivityTitle);

		// create the auidio manager for controlling the volume
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		// create the shared preferences object
		sharedPref = this.getSharedPreferences(PREFERENCES,	0);

		// create the preferences editor for editing the preferences
		prefsEditor = sharedPref.edit();

		// Seek Bar from the layout
		SeekBar volumeBar = (SeekBar) findViewById(R.id.volume);

		// set the volume progress bar to the media volume of the device
		volumeBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

		// set the max volume to the max volume of the device
		int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		// set the bar
		volumeBar.setMax(maxVolume);

		// create a listener for the volume bar changing
		volumeBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			/* (non-Javadoc)
			 * @see android.widget.SeekBar.OnSeekBarChangeListener#onStopTrackingTouch(android.widget.SeekBar)
			 */
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) { }

			/* (non-Javadoc)
			 * @see android.widget.SeekBar.OnSeekBarChangeListener#onStartTrackingTouch(android.widget.SeekBar)
			 */
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {	}

			/* (non-Javadoc)
			 * @see android.widget.SeekBar.OnSeekBarChangeListener#onProgressChanged(android.widget.SeekBar, int, boolean)
			 */
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// if the volume bar is changed then change the device volume to
				// the new level
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,	progress, 0);
			}
		});

		// Sound effects checkbox object
		CheckBox soundEffects = (CheckBox) findViewById(R.id.checkBoxSoundEffects);

		if (soundEffects != null) {
			// set the check box to it's preference
			soundEffects.setChecked(sharedPref.getBoolean(SOUND_EFFECTS, true));
		}

		// speech check box
		CheckBox speechVolume = (CheckBox) findViewById(R.id.checkBoxSpeechVolume);

		if (speechVolume != null) {
			// set the check box to it's preference
			speechVolume.setChecked(sharedPref.getBoolean(SPEECH_VOLUME, true));
		}

		// Number of Computers from the shared preferences
		numComputerSpinner = (Spinner) findViewById(R.id.spinnerNumComputers);

		if (numComputerSpinner != null) {
			// get the value from shared preferences
			Integer numberOfComputers = sharedPref.getInt(NUMBER_OF_COMPUTERS, 1);

			// make an array adapter of all options specified in the xml
			@SuppressWarnings("unchecked")
			ArrayAdapter<String> numCompAdapter = (ArrayAdapter<String>) numComputerSpinner.getAdapter();

			// find the current position
			int spinnerPosition = numCompAdapter.getPosition(numberOfComputers.toString());

			// set the correct position to true
			numComputerSpinner.setSelection(spinnerPosition);
		}

		// get the value of the option from the shared preferences
		difficultySpinner = (Spinner) findViewById(R.id.difficultyOption);

		// set the correct radio button based on the shared preferences
		if (difficultySpinner != null) {
			// get the value from shared preferences
			String difficulty = sharedPref.getString(DIFFICULTY_OF_COMPUTERS, EASY);

			// make an array adapter of all options specified in the xml
			@SuppressWarnings("unchecked")
			ArrayAdapter<String> difficultyAdapter = (ArrayAdapter<String>) difficultySpinner.getAdapter(); // cast to an ArrayAdapter

			// find the current position of the preference
			int difficultyPosition = difficultyAdapter.getPosition(difficulty);

			// set the position to true
			difficultySpinner.setSelection(difficultyPosition);
		}

		// based on the value of the language from the preferences set the
		// correct radio button
		localeSpinner = (Spinner) findViewById(R.id.langOption);

		if (localeSpinner != null) {
			// get the value from shared preferences
			String language = sharedPref.getString(LANGUAGE, LANGUAGE_US);

			// make an array adapter of all options specified in the xml
			@SuppressWarnings("unchecked")
			ArrayAdapter<String> localeAdapter = (ArrayAdapter<String>) localeSpinner.getAdapter(); // cast to an ArrayAdapter

			// get the position of the current item
			int localePosition = localeAdapter.getPosition(language);

			// set the correct position
			localeSpinner.setSelection(localePosition);
		}

		// Game type spinner
		gameSpinner = (Spinner) findViewById(R.id.gameOption);

		if (gameSpinner != null) {
			// value of the game type based upon the shared preferences
			String game_type = sharedPref.getString(GAME_TYPE, CRAZY_EIGHTS);

			// make an array adapter of all options specified in the xml
			@SuppressWarnings("unchecked")
			ArrayAdapter<String> gameAdapter = (ArrayAdapter<String>) gameSpinner.getAdapter(); // cast to an ArrayAdapter

			// get the current position of the selected item
			int gamePosition = gameAdapter.getPosition(game_type);

			// set the option checked based on the preferences
			gameSpinner.setSelection(gamePosition);
		}

		// connection type spinner
		connectionSpinner = (Spinner) findViewById(R.id.connectionOption);

		if (connectionSpinner != null) {
			// value of the game type based upon the shared preferences
			String connection_type = sharedPref.getString(CONNECTION_TYPE, WIFI);

			// make an array adapter of all options specified in the xml
			@SuppressWarnings("unchecked")
			ArrayAdapter<String> gameAdapter = (ArrayAdapter<String>) connectionSpinner.getAdapter(); // cast to an ArrayAdapter

			// get the current position of the selected item
			int connectionPosition = gameAdapter.getPosition(connection_type);

			// set the option checked based on the preferences
			connectionSpinner.setSelection(connectionPosition);
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		// get the values for the sound options from the preferences
		CheckBox soundEffects = (CheckBox) findViewById(R.id.checkBoxSoundEffects);
		CheckBox speechVolume = (CheckBox) findViewById(R.id.checkBoxSpeechVolume);

		// set the result of the activity
		setResult(RESULT_OK);

		// put the new preferences in the shared preferences
		// add the sound effects preference
		if (soundEffects != null) {
			prefsEditor.putBoolean(SOUND_EFFECTS, soundEffects.isChecked());
		}

		// add the speech volume preference
		if (speechVolume != null) {
			prefsEditor.putBoolean(SPEECH_VOLUME, speechVolume.isChecked());
		}

		// set number of computers
		if (numComputerSpinner != null) {
			prefsEditor.putInt(NUMBER_OF_COMPUTERS, Integer.parseInt((String) numComputerSpinner.getSelectedItem()));
		}

		// set difficulty of computers to preferences
		if (difficultySpinner != null) {
			prefsEditor.putString(DIFFICULTY_OF_COMPUTERS, (String) difficultySpinner.getSelectedItem());
		}

		// set language to preferences
		if (localeSpinner != null) {
			prefsEditor.putString(LANGUAGE,	(String) localeSpinner.getSelectedItem());
		}

		// set game type
		if (gameSpinner != null) {
			prefsEditor.putString(GAME_TYPE, (String) gameSpinner.getSelectedItem());
		}

		// set connection type
		if (gameSpinner != null) {
			prefsEditor.putString(CONNECTION_TYPE, (String) connectionSpinner.getSelectedItem());
		}

		// commit the changes to the shared preferences
		prefsEditor.commit();

		// finish the activity
		finish();
	}
}
