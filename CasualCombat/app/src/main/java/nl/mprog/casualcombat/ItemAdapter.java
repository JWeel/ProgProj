// UNIVERSITEIT VAN AMSTERDAM - MINOR PROGRAMMEREN - PROGRAMMEERPROJECT
// CasualCombat - created by Joseph Weel, 10321624, josefweel@gmail.com

package nl.mprog.casualcombat;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import item.Item;
import item.UsableItem;
import move.Move;

// this adapter is used to display usable items nicely
class ItemAdapter extends ArrayAdapter<Integer> {

    // the player character stores only the ids of the items, the actual items are stored here
    private ArrayList<UsableItem> usableItems;

    public ItemAdapter(Context context, int resource, int textViewResourceId, ArrayList<Integer> moveIds){
        super(context, resource, textViewResourceId, moveIds);

        usableItems = new ArrayList<>();
        for (Integer id : moveIds) {
            usableItems.add((UsableItem) Item.findItemById(id));
        }
    }

    // updates the item list (called after an item was used and removed from the id list)
    void updateUsableItems(PlayerCharacter playerCharacter){
        usableItems.clear();
        for (Integer id : playerCharacter.getUsableItems()) {
            usableItems.add((UsableItem) Item.findItemById(id));
        }
        this.notifyDataSetChanged();
    }

    UsableItem getListItem(int position){
        return usableItems.get(position);
    }

    @Override
    public int getCount(){
        return usableItems.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View listItem = super.getView(position, convertView, parent);

        UsableItem item = usableItems.get(position);

        ((TextView) listItem.findViewById(R.id.listItemName)).setText(item.getName());
        ((TextView) listItem.findViewById(R.id.listItemInfo)).setText(item.getInfo());

        int drawableId = 0;
        switch(item.getItemMove().getRange()){
            case Move.RANGE_SELF:
                drawableId = R.drawable.range_self;
                break;
            case Move.RANGE_SINGLE:
                drawableId = R.drawable.range_single;
                break;
            case Move.RANGE_CLOSE:
                drawableId = R.drawable.range_close;
                break;
            case Move.RANGE_FAR:
                drawableId = R.drawable.range_far;
                break;
        }
        listItem.findViewById(R.id.listItemRange).setBackgroundResource(drawableId);

        return listItem;
    }
}
