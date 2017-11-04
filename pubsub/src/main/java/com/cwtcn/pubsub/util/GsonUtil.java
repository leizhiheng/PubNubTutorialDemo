package com.cwtcn.pubsub.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by leizhiheng on 2017/11/1.
 */

public class GsonUtil {
    /**
     * 按章节点得到相应的内容
     * @param jsonString json字符串
     * @param note 节点
     * @return 节点对应的内容
     */
    public static String getNoteJsonString(String jsonString,String note){
        if(TextUtils.isEmpty(jsonString)){
            throw new RuntimeException("json字符串");
        }
        if(TextUtils.isEmpty(note)){
            throw new RuntimeException("note标签不能为空");
        }
        JsonElement element = new JsonParser().parse(jsonString);
        if(element.isJsonNull()){
            throw new RuntimeException("得到的jsonElement对象为空");
        }
        return element.getAsJsonObject().get(note).toString();
    }

    /**
     * 按照节点得到节点内容，然后传化为相对应的bean数组
     * @param jsonString 原json字符串
     * @param note 节点标签
     * @param beanClazz 要转化成的bean class
     * @return 返回bean的数组
     */
    public static <T> List<T> parserJsonToArrayBeans(String jsonString,String note,Class<T> beanClazz){
        String noteJsonString = getNoteJsonString(jsonString,note);
        return parserJsonToArrayBeans(noteJsonString,beanClazz);
    }
    /**
     * 按照节点得到节点内容，转化为一个数组
     * @param jsonString json字符串
     * @param beanClazz 集合里存入的数据对象
     * @return 含有目标对象的集合
     */
    public static <T> List<T> parserJsonToArrayBeans(String jsonString,Class<T> beanClazz){
        if(TextUtils.isEmpty(jsonString)){
            throw new RuntimeException("json字符串为空");
        }
        JsonElement jsonElement = new JsonParser().parse(jsonString);
        if(jsonElement.isJsonNull()){
            throw new RuntimeException("得到的jsonElement对象为空");
        }
        if(!jsonElement.isJsonArray()){
            throw new RuntimeException("json字符不是一个数组对象集合");
        }
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        List<T> beans = new ArrayList<T>();
        for (JsonElement jsonElement2: jsonArray) {
            T bean = new Gson().fromJson(jsonElement2, beanClazz);
            beans.add(bean);
        }
        return beans;
    }

    /**
     * 把相对应节点的内容封装为对象
     * @param jsonString json字符串
     * @param clazzBean  要封装成的目标对象
     * @return 目标对象
     */
    public static <T> T parserJsonToArrayBean(String jsonString,Class<T> clazzBean){
        if(TextUtils.isEmpty(jsonString)){
            throw new RuntimeException("json字符串为空");
        }
        JsonElement jsonElement = new JsonParser().parse(jsonString);
        if(jsonElement.isJsonNull()){
            throw new RuntimeException("json字符串为空");
        }
        if(!jsonElement.isJsonObject()){
            throw new RuntimeException("json不是一个对象");
        }
        return new Gson().fromJson(jsonElement, clazzBean);
    }
    /**
     * 按照节点得到节点内容，转化为一个数组
     * @param jsonString json字符串
     * @param note json标签
     * @param clazzBean 集合里存入的数据对象
     * @return 含有目标对象的集合
     */
    public static <T> T parserJsonToArrayBean(String jsonString,String note,Class<T> clazzBean){
        String noteJsonString = getNoteJsonString(jsonString, note);
        return parserJsonToArrayBean(noteJsonString, clazzBean);
    }

    /**
     * 把bean对象转化为json字符串
     * @param obj bean对象
     * @return 返回的是json字符串
     */
    public static String toJsonString(Object obj){
        if(obj!=null){
            return new Gson().toJson(obj);
        }else{
            throw new RuntimeException("对象不能为空");
        }
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

}
