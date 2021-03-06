// UNIVERSITEIT VAN AMSTERDAM - MINOR PROGRAMMEREN - PROGRAMMEERPROJECT
// CasualCombat - created by Joseph Weel, 10321624, josefweel@gmail.com

package nl.mprog.casualcombat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Random;

// this is the main page, where a user can load or create characters and proceed to combat
public class TitlePage extends AppCompatActivity {

    static final String KEY_PREFS = "CASUALCOMBATPREFS";
    static final String KEY_PLAYER = "CASUALCOMBATPLAYER";

    static final int REQUEST_CODE_LEADERBOARD_PAGE = 0;
    static final int REQUEST_CODE_SHOP_PAGE = 1;
    static final int REQUEST_CODE_PLAY_PAGE = 2;
    static final int RESULT_EXIT = 2; // predefined result codes are -1, 0 and 1

    public static Random random;

    private ArrayList<PlayerCharacter> storedPlayerCharacters;
    private PlayerCharacter playerCharacter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_page);

        random = new Random();
        prepareStoredPlayerList();
        setPlayerVisibility(View.INVISIBLE);
        setLevelUpButtonsVisibility(View.INVISIBLE);
    }

    // prepares the list view and its adapter that correspond to player characters stored in shared preferences
    private void prepareStoredPlayerList(){
        storedPlayerCharacters = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
        Map<String, ?> keys = prefs.getAll();
        ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(this, KEY_PREFS, MODE_PRIVATE);
        for (String key : keys.keySet()) {
            PlayerCharacter pc = complexPreferences.getObject(key, PlayerCharacter.class);
            pc.restoreAfterSave();
            storedPlayerCharacters.add(pc);
        }
        // sort by level in descending order
        Collections.sort(storedPlayerCharacters, new Comparator<PlayerCharacter>() {
            @Override
            public int compare(PlayerCharacter pc1, PlayerCharacter pc2) {
                return pc1.getName().compareToIgnoreCase(pc2.getName());
            }
        });

        PlayerCharacterAdapter adapter = new PlayerCharacterAdapter(this, R.layout.player_character_list, R.id.listCharName, storedPlayerCharacters);
        ListView storedPlayerListView = (ListView) findViewById(R.id.titlePlayerList);
        storedPlayerListView.setAdapter(adapter);
        storedPlayerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                loadPlayerCharacter(position);
            }
        });
        setLoadedPlayerListVisibility(View.INVISIBLE);
    }

    // displays or hides the stored player list
    private void setLoadedPlayerListVisibility(int visibility){
        findViewById(R.id.titleLayoutList).setVisibility(visibility);
    }

    // loads a player character from the list of stored players
    private void loadPlayerCharacter(int position){
        PlayerCharacter loadedCharacter = storedPlayerCharacters.get(position);
        playerCharacter = loadedCharacter.copy();

        updatePlayerSkillViews();
        updatePlayerEquipmentViews();
        setPlayerVisibility(View.VISIBLE);
        setLevelUpButtonsVisibility(View.INVISIBLE);
        setLoadedPlayerListVisibility(View.INVISIBLE);
        setCharacterAvatar();

        ((Button) findViewById(R.id.readyButton)).setText("Start");
        findViewById(R.id.readyButton).setEnabled(true);
    }

    // sets visibility of views that show the current player character
    private void setPlayerVisibility(int visibility){
        findViewById(R.id.titleLayoutCharacter).setVisibility(visibility);
        findViewById(R.id.titleCancelButton).setVisibility(visibility);
    }

    // when pressing this button, either a new character is created, or ShopPage is started
    public void readyClick(View readyButton){

        // if character is ready, ShopPage is started
        if (playerCharacter != null && playerCharacter.finishedLevelUp()) {

            // first save the player to preferences if they are new
            if (!existsInList(playerCharacter.getName())) {
                playerCharacter.prepareForSave();
                ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(this, KEY_PREFS, MODE_PRIVATE);;
                complexPreferences.putObject(playerCharacter.getName(), playerCharacter);
                complexPreferences.commit();
                playerCharacter.restoreAfterSave();
            }

            Intent newPage = new Intent(this, ShopPage.class);
            newPage.putExtra(KEY_PLAYER, playerCharacter);
            startActivityForResult(newPage, REQUEST_CODE_SHOP_PAGE);
        }
        // otherwise, prepare creation of new character
        else {
            ((Button) readyButton).setText("Start");
            readyButton.setEnabled(false);

            // prepare creatable character
            playerCharacter = new PlayerCharacter();
            setCharacterAvatar();
            nameChangeClick(null);

            updatePlayerSkillViews();
            updatePlayerEquipmentViews();
            setPlayerVisibility(View.VISIBLE);
            setLevelUpButtonsVisibility(View.VISIBLE);
        }
    }

    // returns true if player name exists in current list of stored player characters
    private boolean existsInList(String newName){
        for (PlayerCharacter pc : storedPlayerCharacters){
            if (pc.getName().equals(newName)) return true;
        }
        return false;
    }

    // returns true if player name exists in shared preferences
    private boolean existsInStorage(String newName){
        Map<String, ?> sharedPrefsPlayerCharacters = getSharedPreferences(KEY_PREFS, MODE_PRIVATE).getAll();
        for (String key : sharedPrefsPlayerCharacters.keySet()) {
            if (newName.equals(key)) return true;
        }
        return false;
    }

    // creates a non-cancelable dialog in which a name can be entered for the player character
    public void nameChangeClick(View nameButton){

        // http://stackoverflow.com/questions/10903754/input-text-dialog-android
        // http://stackoverflow.com/questions/2620444/how-to-prevent-a-dialog-from-closing-when-a-button-is-clicked
        // http://stackoverflow.com/questions/3285412/limit-text-length-of-edittext-in-android

        final EditText dialogEditText = new EditText(this);
        dialogEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        dialogEditText.setMaxLines(1);
        dialogEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(9)});
        dialogEditText.setText(playerCharacter.getName());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter character name:");
        builder.setView(dialogEditText);
        builder.setPositiveButton("Go", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do nothing (this allows the default dismiss function to be overwritten)
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String newName = dialogEditText.getText().toString();
                if (!newName.isEmpty() && !existsInList(newName)) {
                    renamePlayerCharacter(newName);
                    dialog.dismiss();
                }
                if (existsInList(newName) && !playerCharacter.getName().equals(newName)){
                    Toast toast = Toast.makeText(getBaseContext(), "Name already taken!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    // sets the name of a player character and updates the corresponding view
    private void renamePlayerCharacter(String newName){
        playerCharacter.setName(newName);
        ((TextView) findViewById(R.id.titleCharName)).setText(playerCharacter.getName());
    }

    // called when pressing a level up button. skill that is being leveled up depends on button
    public void addClick(View addButton){
        switch(addButton.getId()){
            case R.id.titleCharHealthAdd:
                playerCharacter.addHealth();
                break;
            case R.id.titleCharMagicAdd:
                playerCharacter.addMagic();
                break;
            case R.id.titleCharStrengthAdd:
                playerCharacter.addStrength();
                break;
            case R.id.titleCharWillpowerAdd:
                playerCharacter.addWillpower();
                break;
            case R.id.titleCharDefenseAdd:
                playerCharacter.addDefense();
                break;
            case R.id.titleCharResistanceAdd:
                playerCharacter.addResistance();
                break;
            case R.id.titleCharSpeedAdd:
                playerCharacter.addSpeed();
                break;
        }

        playerCharacter.subtractLevelPoint();
        updatePlayerSkillViews();

        // once user has finished leveling up their player character, the game can be started
        if (playerCharacter.finishedLevelUp()) {
            findViewById(R.id.readyButton).setEnabled(true);
            setLevelUpButtonsVisibility(View.INVISIBLE);
        }
    }

    // updates the views that show the status of the player character
    private void updatePlayerSkillViews(){
        ((ProgressBar) findViewById(R.id.titleCharHealth)).setMax(playerCharacter.getMaxHealth());
        ((ProgressBar) findViewById(R.id.titleCharHealth)).setProgress(playerCharacter.getHealth());
        ((ProgressBar) findViewById(R.id.titleCharMagic)).setMax(playerCharacter.getMaxMagic());
        ((ProgressBar) findViewById(R.id.titleCharMagic)).setProgress(playerCharacter.getMagic());

        ((TextView) findViewById(R.id.titleCharName)).setText(playerCharacter.getName());
        ((TextView) findViewById(R.id.titleCharLevel)).setText("LEVEL " + playerCharacter.getLevel());

        ((TextView) findViewById(R.id.titleCharHealthText)).setText("" + playerCharacter.getHealth() + "/" + playerCharacter.getMaxHealth());
        ((TextView) findViewById(R.id.titleCharMagicText)).setText("" + playerCharacter.getMagic() + "/" + playerCharacter.getMaxMagic());
        ((TextView) findViewById(R.id.titleCharStrength)).setText("STR " + playerCharacter.getStrength());
        ((TextView) findViewById(R.id.titleCharWillpower)).setText("WIL " + playerCharacter.getWillpower());
        ((TextView) findViewById(R.id.titleCharDefense)).setText("DEF " + playerCharacter.getDefense());
        ((TextView) findViewById(R.id.titleCharResistance)).setText("RES " + playerCharacter.getResistance());
        ((TextView) findViewById(R.id.titleCharSpeed)).setText("SPD " + playerCharacter.getSpeed());

        ((TextView) findViewById(R.id.titleCharPoints)).setText("Points left to spend: " + playerCharacter.getLevelPoints());
    }

    // updates the views that correspond to the equipment of the player character
    private void updatePlayerEquipmentViews(){
        if (playerCharacter.getWeapon() == null)
            ((TextView) findViewById(R.id.titleCharWeapon)).setText("Weapon missing");
        else
            ((TextView) findViewById(R.id.titleCharWeapon)).setText(playerCharacter.getWeapon().getName() + "\n" + playerCharacter.getWeapon().getStatBonusAsString());

        if (playerCharacter.getArmor() == null)
            ((TextView) findViewById(R.id.titleCharArmor)).setText("Armor missing");
        else
            ((TextView) findViewById(R.id.titleCharArmor)).setText(playerCharacter.getArmor().getName() + "\n" + playerCharacter.getArmor().getStatBonusAsString());

        if (playerCharacter.getBoots() == null)
            ((TextView) findViewById(R.id.titleCharBoots)).setText("Boots missing");
        else
            ((TextView) findViewById(R.id.titleCharBoots)).setText(playerCharacter.getBoots().getName() + "\n" + playerCharacter.getBoots().getStatBonusAsString());
    }

    // shows or hides the level up buttons
    private void setLevelUpButtonsVisibility(int visibility){
        findViewById(R.id.titleCharNameChangeButton).setVisibility(visibility);
        findViewById(R.id.titleCharHealthAdd).setVisibility(visibility);
        findViewById(R.id.titleCharMagicAdd).setVisibility(visibility);
        findViewById(R.id.titleCharStrengthAdd).setVisibility(visibility);
        findViewById(R.id.titleCharWillpowerAdd).setVisibility(visibility);
        findViewById(R.id.titleCharDefenseAdd).setVisibility(visibility);
        findViewById(R.id.titleCharResistanceAdd).setVisibility(visibility);
        findViewById(R.id.titleCharSpeedAdd).setVisibility(visibility);
        findViewById(R.id.titleCharPoints).setVisibility(visibility);
    }

    // shows the list of player characters, or hides it if it is already shown
    public void loadClick(View loadButton){
        if (findViewById(R.id.titleLayoutList).getVisibility() == View.VISIBLE){

            // only show player views if one was already shown, which is when button says start
            if (((Button) findViewById(R.id.readyButton)).getText().equals("Start")) setPlayerVisibility(View.VISIBLE);

            setLoadedPlayerListVisibility(View.INVISIBLE);
            findViewById(R.id.readyButton).setEnabled(true);

        } else {
            setPlayerVisibility(View.INVISIBLE);
            setLoadedPlayerListVisibility(View.VISIBLE);
            findViewById(R.id.readyButton).setEnabled(false);

            // display empty list message if applicable
            if (storedPlayerCharacters.isEmpty()) findViewById(R.id.titlePlayerListEmpty).setVisibility(View.VISIBLE);
            else findViewById(R.id.titlePlayerListEmpty).setVisibility(View.INVISIBLE);
        }
    }

    // removes the current player character and hides the corresponding views
    public void cancelClick(View cancelButton){
        playerCharacter = null;
        ((Button) findViewById(R.id.readyButton)).setText("New");
        findViewById(R.id.readyButton).setEnabled(true);
        setPlayerVisibility(View.INVISIBLE);
    }

    // loads the leaderboard page
    public void leaderboardClick(View leaderButton){
        Intent newPage = new Intent(this, LeaderboardPage.class);
        startActivityForResult(newPage, REQUEST_CODE_LEADERBOARD_PAGE);
    }

    // sets the color of the player character's avatar image
    private void setCharacterAvatar(){
        ImageView avatar = (ImageView) findViewById(R.id.titleCharIcon);
        Drawable d = ContextCompat.getDrawable(getBaseContext(), R.drawable.avatar);
        d.mutate().setColorFilter(Color.parseColor(playerCharacter.getColorString()), PorterDuff.Mode.MULTIPLY);
        avatar.setImageDrawable(d);
    }

    // randomly changes the player character's avatar color
    public void secretClick(View characterAvatar){
        playerCharacter.changeColorString();
        setCharacterAvatar();
    }

    // called when returning to this page from other pages
    // loads player character from list, using name of current character (-> "reloads" the character)
    private void reloadPlayerCharacter(){
        String currentPlayerCharacterName = playerCharacter.getName();
        for (PlayerCharacter storedPlayerCharacter : storedPlayerCharacters){
            if (storedPlayerCharacter.getName().equals(currentPlayerCharacterName)) {
                playerCharacter = storedPlayerCharacter;
                break;
            }
        }
    }

    // when returning to this page from leaderboard, update list (players may have been deleted)
    // when returning from shop, either refresh views, or close app completely
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case REQUEST_CODE_LEADERBOARD_PAGE:
                if (playerCharacter != null) {
                    if (existsInList(playerCharacter.getName()) && !existsInStorage(playerCharacter.getName())) {
                        playerCharacter = null;
                        ((Button) findViewById(R.id.readyButton)).setText("New");
                        findViewById(R.id.readyButton).setEnabled(true);
                        setPlayerVisibility(View.INVISIBLE);
                        setLevelUpButtonsVisibility(View.INVISIBLE);
                    }
                }
                prepareStoredPlayerList();
                break;
            case REQUEST_CODE_SHOP_PAGE:
                if (resultCode == RESULT_EXIT) finish();
                if (resultCode == RESULT_OK){
                    // update character
                    prepareStoredPlayerList();
                    reloadPlayerCharacter();
                    updatePlayerSkillViews();
                    updatePlayerEquipmentViews();
                    setCharacterAvatar();
                }
                break;
        }
    }

    // pressing back when loading removes the list. otherwise exit the app
    @Override
    public void onBackPressed(){
        if (findViewById(R.id.titleLayoutList).getVisibility() == View.VISIBLE){

            // only show player views if one was already shown, which is when button says start
            if (((Button) findViewById(R.id.readyButton)).getText().equals("Start")) setPlayerVisibility(View.VISIBLE);

            setLoadedPlayerListVisibility(View.INVISIBLE);
            findViewById(R.id.readyButton).setEnabled(true);
        } else {
            super.onBackPressed();
        }
    }

    // support for action bar back press button, and button to go to information page
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.help) {
            Intent newPage = new Intent(this, InfoPage.class);
            startActivity(newPage);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // add back press button and button to go to information page to the top action bar
    @Override
    public boolean onCreateOptionsMenu(Menu title) {
        getMenuInflater().inflate(R.menu.menu_title_page, title);
        return true;
    }
}
