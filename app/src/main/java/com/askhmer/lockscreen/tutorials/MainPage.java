package com.askhmer.lockscreen.tutorials;

import android.content.Intent;
import android.os.Bundle;

import com.askhmer.lockscreen.activity.RegisterOrLogin;
import com.askhmer.lockscreen.utils.MutiLanguage;
import com.askhmer.lockscreen.utils.SharedPreferencesFile;
import com.github.paolorotolo.appintro.AppIntro2;

/**
 * Created by Longdy on 6/30/2016.
 */
public class MainPage extends AppIntro2 {

    @Override
    public void init(Bundle savedInstanceState) {

        /*start set lanaguage*/
        new MutiLanguage(getApplicationContext(),this).StartUpCheckLanguage();

        // Add your slide's fragments here.
        // AppIntro will automatically generate the dots indicator and buttons.
        addSlide(new ScreenOnes());
        addSlide(new ScreenTwos());
        addSlide(new ScreenThrees());
        addSlide(new ScreenFours());
        addSlide(new ScreenFive());
        setDepthAnimation();

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest.
        // addSlide(AppIntroFragment.newInstance(title, description, image, background_colour));

        // OPTIONAL METHODS
        // Override bar/separator color.
        //setBarColor(Color.parseColor("#3F51B5"));
        //setSeparatorColor(Color.parseColor("#2196F3"));

        // Hide Skip/Done button.
        /*showSkipButton(false);*/
        setProgressButtonEnabled(true);

        // Turn vibration on and set intensity.
        // NOTE: you will probably need to ask VIBRATE permisssion in Manifest.
        setVibrate(true);
        setVibrateIntensity(50);
    }

    @Override
    public void onDonePressed() {
        SharedPreferencesFile mSharedPreferencesFile =
                new SharedPreferencesFile(getApplicationContext(),SharedPreferencesFile.FILE_INFORMATION_TEMP);

        if (mSharedPreferencesFile.getBooleanSharedPreference(SharedPreferencesFile.IS_KEY_INTRO_FINISH) == true) {
            this.finish();
        }else {
            Intent mainIntent = new Intent(this, RegisterOrLogin.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);;
            startActivity(mainIntent);
            mSharedPreferencesFile.putBooleanSharedPreference(SharedPreferencesFile.IS_KEY_INTRO_FINISH, true);
            finish();
        }
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onSlideChanged() {

    }
}
