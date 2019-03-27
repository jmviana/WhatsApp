package pt.com.whatsappandroid.cursoandroid.whatsapp.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import pt.com.whatsappandroid.cursoandroid.whatsapp.fragment.ContactosFragment;
import pt.com.whatsappandroid.cursoandroid.whatsapp.fragment.ConversasFragment;

public class TabAdapter extends FragmentStatePagerAdapter {

    private String[] tabTitles = {"CONVERSAS", "CONTACTOS"};

    public TabAdapter(FragmentManager fm) {
        super( fm );
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;

        switch(position){
            case 0 :
                fragment = new ConversasFragment();
                break;
            case 1 :
                fragment = new ContactosFragment();
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
