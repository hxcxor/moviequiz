package edu.wmich.lab2jtrapa4027;

/*
        Programmer: Jonathan Trapane
        Class ID: jtrapa4027
        Lab #2: Pick-O-Matic
        CIS 4700: Mobile Commerce Development
        Spring 2015
        Due date: 02/22/15
        Date completed: 02/22/15
        *************************************
        * Program Explanation
        * Lab2 is a movie quiz. The user is prompted
        * to enter his or her username to keep high scores.
        * Three genres of movies are possible via menu. The
        * user is presented with several choices and must
        * choose the director of the movie shown. User then
        * has the ability to find more info about the correct
        * director if he or she wishes.
        *************************************
*/

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.Set;




public class MainActivity extends Activity
{
    // Stored keys for pulling from Sharedprefs
    public static final String CHOICES = "pref_numberOfChoices";
    public static final String GENRES = "pref_genresToInclude";

    // Forces portrait mode
    private boolean phoneDevice = true;
    // Checks for changed prefs
    private boolean preferencesChanged = true;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Default sharedprefs values
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Listener for sharedprefs
        PreferenceManager.getDefaultSharedPreferences(this).
          registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        // Check screen size
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        // Checks if phone or tablet, declares not a phone if
        // large or xlarge screen size
        if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||
           screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE)
            phoneDevice = false;

        // Portrait-only if on phone
        if (phoneDevice)
            setRequestedOrientation(
               ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


    }

    // Begins after onCreate method
    @Override
    protected void onStart()
    {
        super.onStart();

        if (preferencesChanged)
        {
            // Initializes QuizFragment
            // Updates based on sharedprefs
            // Sets sharedprefs to false to avoid loop
        QuizFragment quizFragment = (QuizFragment)
                getFragmentManager().findFragmentById(R.id.quizFragment);
        quizFragment.updateGuessRows(
                PreferenceManager.getDefaultSharedPreferences(this));
        quizFragment.updateGenres(
                PreferenceManager.getDefaultSharedPreferences(this));
        quizFragment.resetQuiz();
        preferencesChanged = false;
        }
    }

    // Display menu if phone or tablet (in portrait mode)
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Get default Display object
        Display display = ((WindowManager)
                getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        // Stores screen size
        Point screenSize = new Point();
        // Stores screen size (in screenSize)
        display.getRealSize(screenSize);

        // Display menu if in portrait mode
        // If width is less than height
        if (screenSize.x < screenSize.y) {
            // Inflate menu
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }
        else
            return true;
    }

    // Display SettingsActivity when phone
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);
        return super.onOptionsItemSelected(item);
    }

    // Sharedprefs listener
    private OnSharedPreferenceChangeListener preferenceChangeListener =
            new OnSharedPreferenceChangeListener() {
                // called when the user changes the app's preferences
                @Override
                public void onSharedPreferenceChanged(
                        SharedPreferences sharedPreferences, String key) {
                    // User changed app settings
                    preferencesChanged = true;

                    // Links QuizFragment variable to quizFragment layout
                    QuizFragment quizFragment = (QuizFragment)
                            getFragmentManager().findFragmentById(R.id.quizFragment);

                    // Choices changed
                    if (key.equals(CHOICES)) {
                        quizFragment.updateGuessRows(sharedPreferences);
                    }

                    // Genres changed
                    else if (key.equals(GENRES)) {
                        Set<String> genres =
                                sharedPreferences.getStringSet(GENRES, null);

                        // If user selects a genre
                        if (genres != null && genres.size() > 0) {
                            quizFragment.updateGenres(sharedPreferences);
                        }
                        // If no genre is selected
                        else {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            // Add action genre and commit to sharedprefs
                            genres.add(
                                    "Action");
                            editor.putStringSet(GENRES, genres);
                            editor.commit();
                            // Display Toast message to user
                            Toast.makeText(MainActivity.this,
                                    "Setting Action as the default region.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    // Confirm settings change via Toast message
                    Toast.makeText(MainActivity.this, "New settings applied",
                            Toast.LENGTH_SHORT).show();
                }

     };

}

