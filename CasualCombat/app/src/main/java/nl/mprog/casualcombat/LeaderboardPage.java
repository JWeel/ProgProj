// UNIVERSITEIT VAN AMSTERDAM - MINOR PROGRAMMEREN - PROGRAMMEERPROJECT
// CasualCombat - created by Joseph Weel, 10321624, josefweel@gmail.com

package nl.mprog.casualcombat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

// this page contains all stored player characters. they can be deleted here as well.
public class LeaderboardPage extends AppCompatActivity {

    private ArrayList<PlayerCharacter> storedPlayerCharacters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard_page);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prepareStoredPlayerList();
        handleListTextVisibility();
    }

    // gets the player characters stored in the shared preferences
    private void prepareStoredPlayerList(){
        storedPlayerCharacters = new ArrayList<>();
        Map<String, ?> keys = getSharedPreferences(TitlePage.KEY_PREFS, MODE_PRIVATE).getAll();
        ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(this, TitlePage.KEY_PREFS, MODE_PRIVATE);
        for (String key : keys.keySet()) {
            PlayerCharacter pc = complexPreferences.getObject(key, PlayerCharacter.class);
            pc.restoreAfterSave();
            storedPlayerCharacters.add(pc);
        }
        // sort by level in descending order
        Collections.sort(storedPlayerCharacters, new Comparator<PlayerCharacter>() {
            @Override
            public int compare(PlayerCharacter pc1, PlayerCharacter pc2) {
                return pc2.getLevel() - pc1.getLevel();
            }
        });

        // set up the adapter for the ListView
        final PlayerCharacterAdapter adapter = new PlayerCharacterAdapter(this, R.layout.player_character_list, R.id.listCharName, storedPlayerCharacters);
        ListView storedPlayerListView = (ListView) findViewById(R.id.leaderboardPlayerList);
        storedPlayerListView.setAdapter(adapter);
        storedPlayerListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                getSharedPreferences(TitlePage.KEY_PREFS, MODE_PRIVATE).edit().remove(storedPlayerCharacters.get(position).getName()).commit();
                storedPlayerCharacters.remove(position);
                adapter.notifyDataSetChanged();
                handleListTextVisibility();
                return false;
            }
        });
    }

    // if player list is empty, shows message saying so. otherwise shows message explaining how to delete players
    private void handleListTextVisibility(){
        if (storedPlayerCharacters.isEmpty()) {
            findViewById(R.id.leaderboardPlayerListEmpty).setVisibility(View.VISIBLE);
            findViewById(R.id.leaderboardInfoText).setVisibility(View.INVISIBLE);
        }
        else {
            findViewById(R.id.leaderboardPlayerListEmpty).setVisibility(View.INVISIBLE);
            findViewById(R.id.leaderboardInfoText).setVisibility(View.VISIBLE);
        }
    }

    // support for action bar back press button, and button to go to information page
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.help) {
            Intent newPage = new Intent(this, InfoPage.class);
            startActivity(newPage);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // add back press button and button to go to information page to the top action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_leaderboard_page, menu);
        return true;
    }
}
