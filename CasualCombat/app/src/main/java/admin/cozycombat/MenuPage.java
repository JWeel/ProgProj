package admin.cozycombat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MenuPage extends AppCompatActivity {

    // TODO
    // alternative startup would be having a set amount of points that can be distributed
    // over various skills
    // this also then on ShopPage


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_page);



        SharedPreferences prefs = getSharedPreferences("PREFS", MODE_PRIVATE);

    }

    //
    public void startClick(View startButton){
        Intent newPage = new Intent(this, PlayPage.class);
        startActivity(newPage);
    }

    //
    public void leaderboardClick(View leaderButton){
        Intent newPage = new Intent(this, LeaderboardPage.class);
        startActivity(newPage);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_menu_page, menu);
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
}
