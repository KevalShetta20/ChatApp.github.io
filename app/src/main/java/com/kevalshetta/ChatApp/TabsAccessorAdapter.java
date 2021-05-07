package com.kevalshetta.ChatApp;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabsAccessorAdapter extends FragmentPagerAdapter {
    public TabsAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                ChatsFragment cf = new ChatsFragment();
                return cf;
            case 1:
                GroupsFragment gf = new GroupsFragment();
                return gf;
            case 2:
                FriendsFragment ff = new FriendsFragment();
                return ff;
            case 3:
                RequestFragment rf = new RequestFragment();
                return rf;

            default: return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Chats";
            case 1:
                return "Groups";
            case 2:
                return "Friends";
            case 3:
                return "Requests";

            default: return null;
        }
    }
}
