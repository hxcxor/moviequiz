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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class QuizFragment extends Fragment {
    // String for error messages
    private static final String TAG = "FilmQuiz Activity";
    // Number of flags in quiz
    private static final int FILMS_IN_QUIZ = 8;

    private List<String> fileNameList; // film file names
    private List<String> quizDirectorsList; // directors in current quiz
    private Set<String> genresSet; // genres in quiz
    private String correctAnswer; // correct film for current poster
    private int totalGuesses; // number of guesses made
    private int correctAnswers; // number of correct answers
    private int guessRows; // number of rows displaying buttons
    private SecureRandom random; // randomizes quiz
    private Handler handler; // delays loading next poster
    private Animation shakeAnimation; // animation when incorrect guess

    private TextView questionNumberTextView; // shows question #
    private ImageView posterImageView; // displays poster
    private LinearLayout[] guessLinearLayouts; // rows of answer buttons
    private TextView answerTextView; // displays correct or incorrect

    // configures QuizFragment when View is created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view =
                inflater.inflate(R.layout.fragment_quiz, container, false);

        // List of file names
        fileNameList = new ArrayList<String>();
        // List of directors
        quizDirectorsList = new ArrayList<String>();
        // Randomizer for quiz
        random = new SecureRandom();
        // Poster load delay
        handler = new Handler();

        // load animation for incorrect answers
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.incorrect_shake);
        // repeats animation not once, not twice, but thrice
        shakeAnimation.setRepeatCount(3);

        // GUI component references
        // Imageviews for question number and poster images
        questionNumberTextView = (TextView) view.findViewById(R.id.questionNumberTextView);
        posterImageView = (ImageView) view.findViewById(R.id.posterImageView);
        // Number of button rows
        guessLinearLayouts = new LinearLayout[3];
        guessLinearLayouts[0] = (LinearLayout) view.findViewById(R.id.row1LinearLayout);
        guessLinearLayouts[1] = (LinearLayout) view.findViewById(R.id.row2LinearLayout);
        guessLinearLayouts[2] = (LinearLayout) view.findViewById(R.id.row3LinearLayout);
        answerTextView = (TextView) view.findViewById(R.id.answerTextView);

        // listeners for guess buttons
        for (LinearLayout row : guessLinearLayouts) {
            for (int column = 0; column < row.getChildCount(); column++) {
                Button button = (Button) row.getChildAt(column);
                button.setOnClickListener(guessButtonListener);
            }
        }

        // text for questionNumberTextView
        questionNumberTextView.setText(
                getResources().getString(R.string.question, 1, FILMS_IN_QUIZ));
        // returns fragment's view for display
        return view;
    }

    // update guessRows based on preferences
    public void updateGuessRows(SharedPreferences sharedPreferences) {
        // obtain number of guess buttons from sharedprefs
        String choices = sharedPreferences.getString(MainActivity.CHOICES, null);
        // pulls integer from guessRows value then divides by 3
        guessRows = Integer.parseInt(choices) / 3;

        // hides all guess buttons
        for (LinearLayout layout : guessLinearLayouts)
            layout.setVisibility(View.INVISIBLE);

        // display correct guess button layout
        for (int row = 0; row < guessRows; row++)
            guessLinearLayouts[row].setVisibility(View.VISIBLE);
    }

    // update genres based on sharedprefs
    public void updateGenres(SharedPreferences sharedPreferences) {
        genresSet = sharedPreferences.getStringSet(MainActivity.GENRES, null);
    }
    // method for sharing
    public void shareResults()
    {

        // create an intent to share results
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                // get results string
                getString(R.string.share_results, getResources().getString(R.string.results,
                        totalGuesses, (1000 / (double) totalGuesses))));
        shareIntent.setType("text/plain");

        // show apps capable of displaying text
        startActivity(Intent.createChooser(shareIntent,
                getString(R.string.share_results)));
    }


    // launch next quiz
    public void resetQuiz() {

        // Assetmanager grabs image file names for genres
        AssetManager assets = getActivity().getAssets();
        // Clears list of image file names
        fileNameList.clear();

        try {
            // loop genres
            for (String genre : genresSet) {
                // obtain all poster image files in region
                String[] paths = assets.list(genre);

                for (String path : paths)
                    fileNameList.add(path.replace(".png", ""));
            }
        } catch (IOException exception) {
            Log.e(TAG, "Error loading image file names", exception);
        }

        // reset number of correct answers
        correctAnswers = 0;
        // reset total guesses made
        totalGuesses = 0;
        // clear directors list
        quizDirectorsList.clear();

        int posterCounter = 1;
        int numberOfPosters = fileNameList.size();

        // add FILMS_IN_QUIZ random names to quizDirectorsList
        while (posterCounter <= FILMS_IN_QUIZ) {
            int randomIndex = random.nextInt(numberOfPosters);

            // get random file name
            String fileName = fileNameList.get(randomIndex);

            // if genre is enabled and hasn't been chosen
            if (!quizDirectorsList.contains(fileName)) {
                // adds file to list
                quizDirectorsList.add(fileName);
                ++posterCounter;
            }
        }

        loadNextPoster(); // start quiz by loading first poster
    }

    // load next poster
    private void loadNextPoster() {
        // pull file name of next poster and remove from list
        String nextImage = quizDirectorsList.remove(0);
        // update correct answer
        correctAnswer = nextImage;
        // clears answer textview
        answerTextView.setText("");

        // displays current question number
        questionNumberTextView.setText(
                getResources().getString(R.string.question,
                        (correctAnswers + 1), FILMS_IN_QUIZ));

        // extract genre from image name
        String genre = nextImage.substring(0, nextImage.indexOf('-'));

        // assetmanager loads next image
        AssetManager assets = getActivity().getAssets();

        try {
            // get InputStream for next poster image
            InputStream stream =
                    assets.open(genre + "/" + nextImage + ".png");

            // load as drawable and display on posterImageView
            Drawable poster = Drawable.createFromStream(stream, nextImage);
            posterImageView.setImageDrawable(poster);
        } catch (IOException exception) {
            Log.e(TAG, "Error loading " + nextImage, exception);
        }

        // shuffles file names
        Collections.shuffle(fileNameList);

        // put correct answer at end of fileNameList
        int correct = fileNameList.indexOf(correctAnswer);
        fileNameList.add(fileNameList.remove(correct));

        // add guess buttons based on guessRows value
        for (int row = 0; row < guessRows; row++) {
            // set buttons in tablerow
            for (int column = 0;
                 column < guessLinearLayouts[row].getChildCount(); column++) {
                // obtain reference to Button for configuration
                Button newGuessButton = (Button) guessLinearLayouts[row].getChildAt(column);
                newGuessButton.setEnabled(true);

                // get country name and set as newGuessButton text
                String fileName = fileNameList.get((row * 3) + column);
                newGuessButton.setText(getDirectorName(fileName));
            }
        }

        // randomly replace one button with correct answer
        // random row
        int row = random.nextInt(guessRows);
        // random column
        int column = random.nextInt(3);
        // get row
        LinearLayout randomRow = guessLinearLayouts[row];
        // get director name for correct answer
        String directorName = getDirectorName(correctAnswer);
        ((Button) randomRow.getChildAt(column)).setText(directorName);
    }

    // parse movie file name and return director name
    private String getDirectorName(String name) {
        return name.substring(name.indexOf('*') + 1).replace('_', ' ');
    }
    // parse movie file name and return director name with underscore for search
    private String getDirectorSearch(String search) {
        return search.substring(search.indexOf('*') + 1);
    }

    // called when guess button is touched
    private OnClickListener guessButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Button guessButton = ((Button) v);
            String guess = guessButton.getText().toString();
            // uses parsed correct answer for buttons
            String answer = getDirectorName(correctAnswer);
            // increment number of user guesses
            ++totalGuesses;

            // on correct guess
            if (guess.equals(answer)) {
                // increment number of correct answers
                ++correctAnswers; //

                // display correct answer using green color
                answerTextView.setText(answer + "!");
                answerTextView.setTextColor(
                        getResources().getColor(R.color.correct_answer));

                // disable incorrect answers
                disableButtons();

                // if user correct answers FILMS_IN_QUIZ director
                if (correctAnswers == FILMS_IN_QUIZ) {
                    // DialogFragment display quiz stats
                    DialogFragment quizResults =
                            new DialogFragment() {
                                // create an AlertDialog and return it
                                @Override
                                public Dialog onCreateDialog(Bundle bundle) {
                                    AlertDialog.Builder builder =
                                            new AlertDialog.Builder(getActivity());
                                    builder.setCancelable(false);

                                    builder.setMessage(
                                            getResources().getString(R.string.results,
                                                    totalGuesses, (1000 / (double) totalGuesses)));

                                    // "reset Quiz" button
                                    builder.setPositiveButton("Reset Quiz",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int id) {
                                                    resetQuiz();
                                                }
                                            }
                                    );
                                    // share button
                                    builder.setNegativeButton("Share",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int id) {
                                                    shareResults();


                                                }
                                            }
                                    );


                                    // returns alert dialog
                                    return builder.create();
                                }

                                ;
                            };

                    // use FragmentManager to display DialogFragment
                    quizResults.show(getFragmentManager(), "quiz results");
                }
                // correct answer but not end of quiz
                else {
                    // dialog for wikipedia search
                    DialogFragment quizSearch =
                            new DialogFragment() {
                                // create an AlertDialog and return it
                                @Override
                                public Dialog onCreateDialog(Bundle bundle) {
                                    AlertDialog.Builder builder2 =
                                            new AlertDialog.Builder(getActivity());
                                    builder2.setCancelable(false);
                                    // title of dialog
                                    builder2.setTitle("Wikipedia Search");
                                    // message of dialog
                                    builder2.setMessage("Would you like to learn more " +
                                            "about this director?");

                                    // if search button
                                    builder2.setPositiveButton("Yes",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int id) {
                                                    // initiate browser
                                                    Intent browserIntent = new Intent(
                                                            Intent.ACTION_VIEW,
                                                            Uri.parse("http://en.wikipedia.org/wiki/"
                                                            + Uri.encode(getDirectorSearch(correctAnswer), "UTF-8")));
                                                    startActivity(browserIntent);
                                                }
                                            });
                                    // if no, continue to next question
                                    builder2.setNegativeButton("No",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    handler.postDelayed(
                                                            new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    loadNextPoster();
                                                                }
                                                                // 1 second delay
                                                            }, 1000);
                                                }
                                            });


                                    // returns alert dialog
                                    return builder2.create()


                                            ;
                                }

                                // use FragmentManager to display DialogFragment
                                //                   quizResults.show(getFragmentManager(), "quiz results");
                                //               }

                                // load next flag after delay
                                //                   handler.postDelayed(
                                //                           new Runnable() {

                                //                               public void run() {
                                //                                   loadNextPoster();
                                //                               }
                                // 2 second delay
                                //                           }, 2000);
                            };

                    // use FragmentManager to display DialogFragment
                    quizSearch.show(getFragmentManager(), "quiz search");
                }
            }
            // incorrect guess
            else {
                // start shake animation
                posterImageView.startAnimation(shakeAnimation);

                // show incorrect in red color
                answerTextView.setText("Incorrect!");
                answerTextView.setTextColor(
                        getResources().getColor(R.color.incorrect_answer));
                //disable incorrect answer
                guessButton.setEnabled(false);
            }

        }

        ;


        // method to disable buttons
        private void disableButtons() {
            for (int row = 0; row < guessRows; row++) {
                LinearLayout guessRow = guessLinearLayouts[row];
                for (int i = 0; i < guessRow.getChildCount(); i++)
                    guessRow.getChildAt(i).setEnabled(false);
            }
        }

    };
}
