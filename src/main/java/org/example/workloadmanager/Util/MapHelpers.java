package org.example.workloadmanager.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapHelpers {

    public static <T> void addAllToListInMapOrCreateNewList(Map<String, List<T>> map, String key, List<T> list){
        if(map.containsKey(key))
            map.get(key).addAll(list);
        else{
            List<T> newList = new ArrayList<>();
            newList.addAll(list);
            map.put(key, newList);
        }
    }
}
