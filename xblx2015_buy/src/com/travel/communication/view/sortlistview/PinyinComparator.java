package com.travel.communication.view.sortlistview;

import android.support.annotation.NonNull;

import java.util.Comparator;

/**
 * 用于SortModel的比较器
 */
public class PinyinComparator implements Comparator<SortModel>{
    @Override
    public int compare(@NonNull SortModel lhs, @NonNull SortModel rhs) {
        if("@".equals(lhs.getCapitalLetters())
                || "#".equals(rhs.getCapitalLetters())){
            return -1;
        }else if("#".equals(lhs.getCapitalLetters())
                || "@".equals(rhs.getCapitalLetters())){
            return 1;
        }else{
            return lhs.getCapitalLetters().compareTo(rhs.getCapitalLetters());
        }
    }
}
