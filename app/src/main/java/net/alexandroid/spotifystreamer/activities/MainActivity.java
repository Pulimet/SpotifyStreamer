package net.alexandroid.spotifystreamer.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import net.alexandroid.spotifystreamer.R;
import net.alexandroid.spotifystreamer.events.PlayerCtrlEvent;
import net.alexandroid.spotifystreamer.events.UiUpdateEvent;
import net.alexandroid.spotifystreamer.fragments.MainFragment;
import net.alexandroid.spotifystreamer.fragments.TracksFragment;
import net.alexandroid.spotifystreamer.helpers.MyApplication;
import net.alexandroid.spotifystreamer.helpers.MyLogger;
import net.alexandroid.spotifystreamer.helpers.ShPref;
import net.alexandroid.spotifystreamer.services.PlayerService;

import de.greenrobot.event.EventBus;


public class MainActivity extends AppCompatActivity implements MainFragment.FragmentCallback {

    public static final String TRACKS_FRAGMENT_TAG = "TRACKS_LIST";
    public static boolean sWideScreen, isShareShown;
    private MenuItem mShareMenuItem;
    private ShareActionProvider mShareActionProvider;

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
        MyLogger.log("ZAQ", "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (sWideScreen) {
            mShareMenuItem = menu.findItem(R.id.menu_item_share2);
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mShareMenuItem);
            mShareActionProvider.setShareIntent(createShareIntent());
            if (isShareShown) mShareMenuItem.setVisible(true);
        }
        return true;
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, PlayerService.tempSharingData);
        return shareIntent;
    }

    public void onEventMainThread(UiUpdateEvent event) {
        if (sWideScreen) {
            switch (event.duration) {
                case UiUpdateEvent.PLAY:
                    mShareMenuItem.setVisible(true);
                    isShareShown = true;
                    if (mShareActionProvider != null) {
                        mShareActionProvider.setShareIntent(createShareIntent());
                    }
                    break;
                case UiUpdateEvent.PAUSE:
                    mShareMenuItem.setVisible(false);
                    isShareShown = false;
                    break;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        MyApplication.setMainActivityVisibility(true);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        MyApplication.setMainActivityVisibility(false);
        super.onStop();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_country:
                showCountryCodeDialog();
                break;
            case R.id.action_lock_controls:
                if (ShPref.isShowLockScreen(getApplicationContext())) {
                    ShPref.setLockScreenShow(getApplicationContext(), false);
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.lock_hidden), Toast.LENGTH_SHORT).show();
                    EventBus.getDefault().post(new PlayerCtrlEvent(PlayerCtrlEvent.REMOVE_NOTIFICATION));
                } else {
                    ShPref.setLockScreenShow(getApplicationContext(), true);
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.lock_visible), Toast.LENGTH_SHORT).show();
                    EventBus.getDefault().post(new PlayerCtrlEvent(PlayerCtrlEvent.SHOW_NOTIFICATION));
                }
                break;
            case R.id.menu_item_share2:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showCountryCodeDialog() {
        LayoutInflater li = LayoutInflater.from(getApplicationContext());
        View dialogView = li.inflate(R.layout.dialog_country_code, null);
        final EditText userInput = (EditText) dialogView.findViewById(R.id.editText);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AppCompatAlertDialogStyle);
        builder.setView(dialogView);
        builder.setTitle(getResources().getString(R.string.country_code));
        builder.setMessage(getResources().getString(R.string.country_code_msg));
        builder.setPositiveButton(getResources().getString(R.string.country_code_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ShPref.setCountryCode(getApplicationContext(), userInput.getText().toString());
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.country_code_cancel), null);
        builder.show();
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

    @Override
    protected void onDestroy() {
        EventBus.getDefault().post(new PlayerCtrlEvent(PlayerCtrlEvent.KILL_SERVER));
        super.onDestroy();
    }
}
