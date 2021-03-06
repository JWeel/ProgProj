// UNIVERSITEIT VAN AMSTERDAM - MINOR PROGRAMMEREN - PROGRAMMEERPROJECT
// CasualCombat - created by Joseph Weel, 10321624, josefweel@gmail.com

package nl.mprog.casualcombat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import item.EquippableItem;
import item.Item;
import item.UsableItem;
import move.Move;

// on this page a user can save and level up their player character, and spend their earned gold
public class ShopPage extends AppCompatActivity {

    private static final int RESTORE_HEALTH_PRICE = 1;
    private static final int RESTORE_MAGIC_PRICE = 1;
    private static final int MAX_BUYABLE_USABLE_STOCK = 5;

    private int currentBuyableUsableStock;

    private PlayerCharacter playerCharacter;

    // this boolean is flagged true if the user is starting a new game session
    // the activity stack is TitlePage -> ShopPage -> PlayPage , and on a new session ShopPage is ignored
    private boolean firstVisit;

    // whenever the shop is visited, new usable items, equippable items and spells become available
    private UsableItem buyableUsableItem;
    private EquippableItem buyableEquippableItem;
    private Move buyableSpell;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_page);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firstVisit = true;

        Intent previousPage = getIntent();
        playerCharacter = previousPage.getParcelableExtra(TitlePage.KEY_PLAYER);

        // long clicking the death text view sends the user back to the title screen
        findViewById(R.id.shopDeathText).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onBackPressed();
                return true;
            }
        });

        preparePage();
    }

    // sets up views. can initialize buyable items and spell if player character is alive
    private void preparePage(){

        // if new session, the shop is ignored and user proceeds to PlayPage right away
        if (firstVisit){
            nextClick(null);
        }
        // otherwise, check for player death to display shop or death message
        else {
            if (playerCharacter.isDead()) {
                setTitle("Defeat");
                findViewById(R.id.shopLayoutCharacter).setVisibility(View.INVISIBLE);
                findViewById(R.id.shopLayoutEquipment).setVisibility(View.INVISIBLE);
                findViewById(R.id.shopLayoutMiddle).setVisibility(View.INVISIBLE);
                findViewById(R.id.shopLayoutShop).setVisibility(View.INVISIBLE);
                findViewById(R.id.shopDeathText).setVisibility(View.VISIBLE);
            }
            // if player character survived combat, shop can be initialized
            else {
                setCharacterAvatar();
                updatePlayerSkillViews();
                updatePlayerEquipmentViews();
                if (playerCharacter.getLevelPoints() > 0) {
                    setLevelUpButtonsVisibility(View.VISIBLE);
                    findViewById(R.id.nextButton).setEnabled(false);
                    findViewById(R.id.saveButton).setEnabled(false);
                }

                initializeShop();
            }
        }
    }

    // called when pressing a level up button. skill that is being leveled up depends on button
    public void addClick(View addButton){
        switch(addButton.getId()){
            case R.id.shopCharHealthAdd:
                playerCharacter.addHealth();
                break;
            case R.id.shopCharMagicAdd:
                playerCharacter.addMagic();
                break;
            case R.id.shopCharStrengthAdd:
                playerCharacter.addStrength();
                break;
            case R.id.shopCharWillpowerAdd:
                playerCharacter.addWillpower();
                break;
            case R.id.shopCharDefenseAdd:
                playerCharacter.addDefense();
                break;
            case R.id.shopCharResistanceAdd:
                playerCharacter.addResistance();
                break;
            case R.id.shopCharSpeedAdd:
                playerCharacter.addSpeed();
                break;
        }

        playerCharacter.subtractLevelPoint();
        updatePlayerSkillViews();

        // once user has finished leveling up their player character, the game can be saved and continued
        if (playerCharacter.finishedLevelUp()) {
            findViewById(R.id.saveButton).setEnabled(true);
            findViewById(R.id.nextButton).setEnabled(true);
            setLevelUpButtonsVisibility(View.INVISIBLE);
        }
    }

    // updates the views that show the status of the player character
    private void updatePlayerSkillViews(){
        updatePlayerBars();

        ((TextView) findViewById(R.id.shopCharName)).setText(playerCharacter.getName());
        ((TextView) findViewById(R.id.shopCharHealthText)).setText("" + playerCharacter.getHealth() + "/" + playerCharacter.getMaxHealth());
        ((TextView) findViewById(R.id.shopCharMagicText)).setText("" + playerCharacter.getMagic() + "/" + playerCharacter.getMaxMagic());
        ((TextView) findViewById(R.id.shopCharLevel)).setText("LEVEL " + playerCharacter.getLevel());
        ((TextView) findViewById(R.id.shopCharStrength)).setText("STR " + playerCharacter.getStrength());
        ((TextView) findViewById(R.id.shopCharWillpower)).setText("WIL " + playerCharacter.getWillpower());
        ((TextView) findViewById(R.id.shopCharDefense)).setText("DEF " + playerCharacter.getDefense());
        ((TextView) findViewById(R.id.shopCharResistance)).setText("RES " + playerCharacter.getResistance());
        ((TextView) findViewById(R.id.shopCharSpeed)).setText("SPD " + playerCharacter.getSpeed());

        ((TextView) findViewById(R.id.shopCharPoints)).setText("Points left to spend: " + playerCharacter.getLevelPoints());
    }

    // updates the progress bars that correspond with the health and magic of the player character
    private void updatePlayerBars(){
        ProgressBar shopCharHealth = (ProgressBar) findViewById(R.id.shopCharHealth);
        shopCharHealth.setMax(playerCharacter.getMaxHealth());
        shopCharHealth.setProgress(playerCharacter.getHealth());
        ((TextView) findViewById(R.id.shopCharHealthText)).setText("" + playerCharacter.getHealth() + "/" + playerCharacter.getMaxHealth());

        ProgressBar shopCharMagic = (ProgressBar) findViewById(R.id.shopCharMagic);
        shopCharMagic.setMax(playerCharacter.getMaxMagic());
        shopCharMagic.setProgress(playerCharacter.getMagic());
        ((TextView) findViewById(R.id.shopCharMagicText)).setText("" + playerCharacter.getMagic() + "/" + playerCharacter.getMaxMagic());
    }

    // updates the views that correspond to the equipment of the player character
    private void updatePlayerEquipmentViews(){
        if (playerCharacter.getWeapon() == null)
            ((TextView) findViewById(R.id.shopCharWeapon)).setText("Weapon missing");
        else
            ((TextView) findViewById(R.id.shopCharWeapon)).setText(playerCharacter.getWeapon().getName() + "\n" + playerCharacter.getWeapon().getStatBonusAsString());

        if (playerCharacter.getArmor() == null)
            ((TextView) findViewById(R.id.shopCharArmor)).setText("Armor missing");
        else
            ((TextView) findViewById(R.id.shopCharArmor)).setText(playerCharacter.getArmor().getName() + "\n" + playerCharacter.getArmor().getStatBonusAsString());

        if (playerCharacter.getBoots() == null)
            ((TextView) findViewById(R.id.shopCharBoots)).setText("Boots missing");
        else
            ((TextView) findViewById(R.id.shopCharBoots)).setText(playerCharacter.getBoots().getName() + "\n" + playerCharacter.getBoots().getStatBonusAsString());
    }

    // shows or hides the level up buttons
    private void setLevelUpButtonsVisibility(int visibility){
        findViewById(R.id.shopCharHealthAdd).setVisibility(visibility);
        findViewById(R.id.shopCharMagicAdd).setVisibility(visibility);
        findViewById(R.id.shopCharStrengthAdd).setVisibility(visibility);
        findViewById(R.id.shopCharWillpowerAdd).setVisibility(visibility);
        findViewById(R.id.shopCharDefenseAdd).setVisibility(visibility);
        findViewById(R.id.shopCharResistanceAdd).setVisibility(visibility);
        findViewById(R.id.shopCharSpeedAdd).setVisibility(visibility);
        findViewById(R.id.shopCharPoints).setVisibility(visibility);
    }

    // randomly gets buyable items and spells, and displays them on the views
    private void initializeShop(){
        setRandomBuyables();
        currentBuyableUsableStock = MAX_BUYABLE_USABLE_STOCK;

        displayBuyable(R.id.shopPriceUsable, R.id.shopNameUsable, R.id.shopInfoUsable, buyableUsableItem);
        displayBuyable(R.id.shopPriceEquippable, R.id.shopNameEquippable, R.id.shopInfoEquippable, buyableEquippableItem);
        displayBuyable(R.id.shopPriceSpell, R.id.shopNameSpell, R.id.shopInfoSpell, buyableSpell);

        updateShop();
    }

    // randomly chooses buyable items and spells from a predefined list of possibilities based on player character level
    private void setRandomBuyables(){

        // possible usable items are the same for every level
        ArrayList<Integer> possibleUsableItems = new ArrayList<>();
        possibleUsableItems.add(Item.BOMB);
        possibleUsableItems.add(Item.HERB);
        possibleUsableItems.add(Item.DART);

        // possibilities for equippable items and spells depend on level
        ArrayList<Integer> possibleEquippableItems = new ArrayList<>();
        ArrayList<Integer> possibleSpells = new ArrayList<>();

        int level = playerCharacter.getLevel();
        if (level == 1){
            possibleEquippableItems.add(Item.WOODEN_SWORD);
            possibleSpells.add(Move.SHOCK);
        } else if (level > 1 && level <= 9) {
            possibleEquippableItems.add(Item.WOODEN_SWORD);
            possibleEquippableItems.add(Item.METAL_ROD);
            possibleEquippableItems.add(Item.MAIL_HAUBERK);
            possibleEquippableItems.add(Item.LESSER_WARD);
            possibleEquippableItems.add(Item.SANDLES);
            possibleSpells.add(Move.SHOCK);
            if (level > 6) possibleSpells.add(Move.TORNADO);
        } else if (level > 9) {
            possibleEquippableItems.add(Item.GLADIUS);
            possibleEquippableItems.add(Item.FINE_SCEPTER);
            possibleEquippableItems.add(Item.PLATE_COAT);
            possibleEquippableItems.add(Item.GREATER_WARD);
            possibleEquippableItems.add(Item.SWEET_KICKS);
            possibleSpells.add(Move.SHOCK);
            possibleSpells.add(Move.TORNADO);
            possibleSpells.add(Move.ARCANE_BLAST);
        }

        int randomUsableItemId = possibleUsableItems.get(TitlePage.random.nextInt(possibleUsableItems.size()));
        buyableUsableItem = (UsableItem) Item.findItemById(randomUsableItemId);

        int randomEquippableItemId = possibleEquippableItems.get(TitlePage.random.nextInt(possibleEquippableItems.size()));
        buyableEquippableItem = (EquippableItem) Item.findItemById(randomEquippableItemId);

        int randomSpellid = possibleSpells.get(TitlePage.random.nextInt(possibleSpells.size()));
        buyableSpell = Move.findMoveById(randomSpellid);
    }

    // displays information of a buyable item
    private void displayBuyable(int priceId, int nameId, int infoId, Item item){
        ((TextView) findViewById(priceId)).setText("" + item.getPrice() + "G");
        ((TextView) findViewById(nameId)).setText(item.getName());
        ((TextView) findViewById(infoId)).setText(item.getInfo());
    }

    // displays information of a buyable spell
    private void displayBuyable(int priceId, int nameId, int infoId, Move spell){
        ((TextView) findViewById(priceId)).setText("" + spell.getPrice() + "G");
        ((TextView) findViewById(nameId)).setText(spell.getName());
        ((TextView) findViewById(infoId)).setText(spell.getInfo());
    }

    // changes enabeled status and visibility of buttons based on player character gold
    private void updateShop(){
        // set character gold view
        ((TextView) findViewById(R.id.shopCharMoney)).setText("Current gold: " + playerCharacter.getMoney());

        // setup restore health and magic buttons
        if (playerCharacter.getMoney() < RESTORE_HEALTH_PRICE || playerCharacter.getHealth() == playerCharacter.getMaxHealth())
            findViewById(R.id.shopHealthRegen).setEnabled(false);
        else findViewById(R.id.shopHealthRegen).setEnabled(true);
        if (playerCharacter.getMoney() < RESTORE_MAGIC_PRICE || playerCharacter.getMagic() == playerCharacter.getMaxMagic())
            findViewById(R.id.shopMagicRegen).setEnabled(false);
        else findViewById(R.id.shopMagicRegen).setEnabled(true);

        // check enough gold for buyable items, and check if already have it
        TextView usableWarningText = (TextView) findViewById(R.id.shopWarningUsable);
        if (buyableUsableItem != null) {
            if (playerCharacter.getMoney() < buyableUsableItem.getPrice()) {
                findViewById(R.id.shopLayoutUsable).setBackgroundColor(Color.parseColor("#996666"));
                usableWarningText.setVisibility(View.VISIBLE);
                usableWarningText.setText("INSUFFICIENT GOLD");
                findViewById(R.id.shopLayoutUsableInner).setVisibility(View.INVISIBLE);
            } else {
                usableWarningText.setVisibility(View.INVISIBLE);
                findViewById(R.id.shopLayoutUsable).setBackgroundColor(Color.parseColor("#999999"));
                findViewById(R.id.shopLayoutUsableInner).setVisibility(View.VISIBLE);
            }
        // sold out
        } else {
            usableWarningText.setVisibility(View.VISIBLE);
            usableWarningText.setText("SOLD OUT");
            findViewById(R.id.shopLayoutUsable).setBackgroundColor(Color.parseColor("#666666"));
            findViewById(R.id.shopLayoutUsableInner).setVisibility(View.INVISIBLE);
        }

        // check enough gold for buyable equipment, and check if already have it
        TextView equippableWarningText = (TextView) findViewById(R.id.shopWarningEquippable);
        if (buyableEquippableItem != null) {
            if (playerCharacter.alreadyHasEquipment(buyableEquippableItem)) {
                findViewById(R.id.shopLayoutEquippable).setBackgroundColor(Color.parseColor("#669966"));
                equippableWarningText.setVisibility(View.VISIBLE);
                equippableWarningText.setText("ALREADY OWNED");
                findViewById(R.id.shopLayoutEquippableInner).setVisibility(View.INVISIBLE);

            } else if (playerCharacter.getMoney() < buyableEquippableItem.getPrice()) {
                findViewById(R.id.shopLayoutEquippable).setBackgroundColor(Color.parseColor("#996666"));
                equippableWarningText.setVisibility(View.VISIBLE);
                equippableWarningText.setText("INSUFFICIENT GOLD");
                findViewById(R.id.shopLayoutEquippableInner).setVisibility(View.INVISIBLE);

            // otherwise buyable as intended
            } else {
                equippableWarningText.setVisibility(View.INVISIBLE);
                findViewById(R.id.shopLayoutEquippable).setBackgroundColor(Color.parseColor("#999999"));
                findViewById(R.id.shopLayoutEquippableInner).setVisibility(View.VISIBLE);
            }
        // sold out
        } else {
            equippableWarningText.setVisibility(View.VISIBLE);
            equippableWarningText.setText("SOLD OUT");
            findViewById(R.id.shopLayoutEquippable).setBackgroundColor(Color.parseColor("#666666"));
            findViewById(R.id.shopLayoutEquippableInner).setVisibility(View.INVISIBLE);
        }

        // check enough gold for buyable spell, and check if already have it
        TextView spellWarningText = (TextView) findViewById(R.id.shopWarningSpell);
        if (buyableSpell != null) {
            if (playerCharacter.getSpells().contains(buyableSpell.getId())) {
                findViewById(R.id.shopLayoutSpell).setBackgroundColor(Color.parseColor("#669966"));
                spellWarningText.setVisibility(View.VISIBLE);
                spellWarningText.setText("ALREADY OWNED");
                findViewById(R.id.shopLayoutSpellInner).setVisibility(View.INVISIBLE);

            } else if (playerCharacter.getMoney() < buyableSpell.getPrice()) {
                findViewById(R.id.shopLayoutSpell).setBackgroundColor(Color.parseColor("#996666"));
                spellWarningText.setVisibility(View.VISIBLE);
                spellWarningText.setText("INSUFFICIENT GOLD");
                findViewById(R.id.shopLayoutSpellInner).setVisibility(View.INVISIBLE);

            // otherwise buyable as intended
            } else {
                spellWarningText.setVisibility(View.INVISIBLE);
                findViewById(R.id.shopLayoutSpell).setBackgroundColor(Color.parseColor("#999999"));
                findViewById(R.id.shopLayoutSpellInner).setVisibility(View.VISIBLE);
            }
        // sold out
        } else {
            spellWarningText.setVisibility(View.VISIBLE);
            spellWarningText.setText("SOLD OUT");
            findViewById(R.id.shopLayoutSpell).setBackgroundColor(Color.parseColor("#666666"));
            findViewById(R.id.shopLayoutSpellInner).setVisibility(View.INVISIBLE);
        }
    }

    // when pressed, gold is spent to fully restore player character health
    public void restoreHealthClick(View restoreHealthButton){
        playerCharacter.restoreHealthFully();
        playerCharacter.subtractMoney(RESTORE_HEALTH_PRICE);
        updatePlayerBars();
        updateShop();
        // save button can be re-enabled if applicable
        if (findViewById(R.id.nextButton).isEnabled()) findViewById(R.id.saveButton).setEnabled(true);
    }

    // when pressed, gold is spent to fully restore player character magic
    public void restoreMagicClick(View restoreMagicButton){
        playerCharacter.restoreMagicFully();
        playerCharacter.subtractMoney(RESTORE_MAGIC_PRICE);
        updatePlayerBars();
        updateShop();
        // save button can be re-enabled if applicable
        if (findViewById(R.id.nextButton).isEnabled()) findViewById(R.id.saveButton).setEnabled(true);
    }

    // when pressed, gold is spent to place buyable item in player character inventory
    public void usableClick(View usableView){
        if (buyableUsableItem != null) {
            if (playerCharacter.getMoney() >= buyableUsableItem.getPrice()){
                playerCharacter.subtractMoney(buyableUsableItem.getPrice());
                UsableItem boughtItem = buyableUsableItem;
                playerCharacter.addUsableItem(boughtItem);

                // buyable item is only set to null (sold out) once stock is 0
                currentBuyableUsableStock--;
                if (currentBuyableUsableStock == 0) buyableUsableItem = null;
                else buyableUsableItem = (UsableItem) Item.findItemById(boughtItem.getId());
            }
        }
        updateShop();
        // save button can be re-enabled if applicable
        if (findViewById(R.id.nextButton).isEnabled()) findViewById(R.id.saveButton).setEnabled(true);
    }

    // when pressed, gold is spent to to have player character equip item
    public void equippableClick(View equippableView){
        if (buyableEquippableItem != null) {
            if (playerCharacter.getMoney() >= buyableEquippableItem.getPrice()){
                playerCharacter.subtractMoney(buyableEquippableItem.getPrice());
                playerCharacter.equipItem(buyableEquippableItem);
                buyableEquippableItem = null;
                updatePlayerSkillViews();
                updatePlayerEquipmentViews();
            }
        }
        updateShop();
        // save button can be re-enabled if applicable
        if (findViewById(R.id.nextButton).isEnabled()) findViewById(R.id.saveButton).setEnabled(true);
    }

    // when pressed, gold is spent to place spell in player character's list of known spells
    public void spellClick(View spellView){
        if (buyableSpell != null) {
            if (playerCharacter.getMoney() >= buyableSpell.getPrice()){
                playerCharacter.subtractMoney(buyableSpell.getPrice());
                playerCharacter.addSpell(buyableSpell);
                buyableSpell = null;
            }
        }
        updateShop();
        // save button can be re-enabled if applicable
        if (findViewById(R.id.nextButton).isEnabled()) findViewById(R.id.saveButton).setEnabled(true);
    }

    // when pressed, the player character is stored in the shared preferences
    public void saveClick(View saveButton){
        playerCharacter.prepareForSave();
        ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(this, TitlePage.KEY_PREFS, MODE_PRIVATE);;
        complexPreferences.putObject(playerCharacter.getName(), playerCharacter);
        complexPreferences.commit();
        findViewById(R.id.saveButton).setEnabled(false);
        playerCharacter.restoreAfterSave();
    }

    // when pressed, a new PlayPage is created where new Combat starts
    public void nextClick(View nextButton){
        Intent newPage = new Intent(this, PlayPage.class);
        newPage.putExtra(TitlePage.KEY_PLAYER, playerCharacter);
        startActivityForResult(newPage, TitlePage.REQUEST_CODE_PLAY_PAGE);
    }

    // sets the color of the player character's avatar image
    private void setCharacterAvatar(){
        ImageView avatar = (ImageView) findViewById(R.id.shopCharIcon);
        Drawable d = ContextCompat.getDrawable(getBaseContext(), R.drawable.avatar);
        d.mutate().setColorFilter(Color.parseColor(playerCharacter.getColorString()), PorterDuff.Mode.MULTIPLY);
        avatar.setImageDrawable(d);
    }

    // randomly changes the player character's avatar color
    public void secretClick(View characterAvatar){
        playerCharacter.changeColorString();
        setCharacterAvatar();
    }

    // when returning to this page, either return further to TitlePage, or reset the shop
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TitlePage.REQUEST_CODE_PLAY_PAGE){
            if (resultCode == TitlePage.RESULT_EXIT) {
                setResult(resultCode);
                finish();
            }
            if (resultCode == RESULT_CANCELED){
                // if new session, this activity is ignored
                if (firstVisit) {
                    setResult(resultCode);
                    finish();
                }
            }
            // if OK, that means combat ended and the shop is reset
            if (resultCode == RESULT_OK) {
                firstVisit = false;
                playerCharacter = data.getExtras().getParcelable(TitlePage.KEY_PLAYER);
                preparePage();
            }
        }
    }

    // unless the character is dead, a dialog pops up to warn user to save
    @Override
    public void onBackPressed() {
        if (playerCharacter.isDead()){
            setResult(TitlePage.RESULT_OK);
            finish();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("You are about to leave this page.\nUnsaved progress will be lost.");
            builder.setPositiveButton("Return to title screen", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setResult(RESULT_OK);
                    finish();
                }
            });
            builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // do nothing -> dismisses dialog
                }
            });
            builder.setNegativeButton("Exit app", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setResult(TitlePage.RESULT_EXIT);
                    finish();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
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
        getMenuInflater().inflate(R.menu.menu_shop_page, menu);
        return true;
    }
}
