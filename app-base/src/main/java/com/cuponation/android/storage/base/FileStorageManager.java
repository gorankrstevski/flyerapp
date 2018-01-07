package com.cuponation.android.storage.base;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by goran on 7/15/16.
 */

public class FileStorageManager {

    private File file;
    private Gson gson;

    public FileStorageManager(String fileName) {
        file = new File(fileName);
        gson = new Gson();

    }

    public <T> void add(T object, Type type) throws IOException {

        List<T> items = getAll(type);

        if (items == null) {
            items = new ArrayList<T>();
        }
        items.add(object);

        FileUtils.write(file, gson.toJson(items));
    }

    public <T> List<T> getAll(Type type) {
        try {
            if(file.exists()){
                String json = FileUtils.readFileToString(file);
                return gson.fromJson(json, type);
            }
            return new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e){
            e.printStackTrace();
        }
        return null;
    }

    public void clear() throws IOException {
        FileUtils.write(file, "");
    }

    public <T> void addList(List<T> list, Type type) throws IOException {
        List<T> items = getAll(type);

        if (items == null) {
            items = new ArrayList<T>();
        }

        items.addAll(list);

        FileUtils.write(file, gson.toJson(items));
    }

    public <T> void remove(int location, Type type) throws IOException {
        List items = getAll(type);

        if (items == null) {
            items = new ArrayList<T>();
        }
        items.remove(location);

        FileUtils.write(file, gson.toJson(items));
    }

    public <T> void set(int position, T object, Type type) throws IOException {
        List items  = getAll(type);
        items.set(position, object);
        FileUtils.write(file, gson.toJson(items));
    }
}
