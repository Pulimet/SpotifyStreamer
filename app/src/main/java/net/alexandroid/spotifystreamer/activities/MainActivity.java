package net.alexandroid.spotifystreamer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import net.alexandroid.spotifystreamer.R;
import net.alexandroid.spotifystreamer.fragments.MainFragment;
import net.alexandroid.spotifystreamer.fragments.TracksFragment;


public class MainActivity extends AppCompatActivity implements MainFragment.FragmentCallback{

    public static final String TRACKS_FRAGMENT_TAG = "TRACKS_LIST";
    public static boolean sWideScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.tracks_list) != null) {
            sWideScreen = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.tracks_list, new TracksFragment(), TRACKS_FRAGMENT_TAG).commit();
            }
        } else {
            sWideScreen = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onArtistSelected(String artistId, String artistName) {
        if (sWideScreen) {
            Bundle args = new Bundle();
            args.putString(Intent.EXTRA_TEXT, artistId);
            args.putString(Intent.EXTRA_REFERRER_NAME, artistName);
            TracksFragment fragment = new TracksFragment();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.tracks_list, fragment, TRACKS_FRAGMENT_TAG).commit();
        } else {
            Intent intent = new Intent(this, TracksActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, artistId);
            intent.putExtra(Intent.EXTRA_REFERRER_NAME, artistName);
            startActivity(intent);
        }
    }
}
