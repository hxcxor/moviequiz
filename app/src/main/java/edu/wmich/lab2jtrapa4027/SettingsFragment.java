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

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment
{
    // Establishes the preferences GUI
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // Loads the created preferences XML file.
        addPreferencesFromResource(R.xml.preferences);
    }
}

