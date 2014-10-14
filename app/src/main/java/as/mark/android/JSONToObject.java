package as.mark.android;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by igbopie on 10/13/14.
 */
public class JSONToObject {

    public static Object toObject(JSONObject object,Class clazz) throws IllegalAccessException, InstantiationException {

        Object target = clazz.newInstance();

        Field[] fields = clazz.getDeclaredFields();

        for(Field field:fields){
            String setterName = field.getName();
            setterName = "set"+setterName.substring(0,1).toUpperCase()+setterName.substring(1,setterName.length());

            String propertyName = field.getName();
            if(propertyName.equalsIgnoreCase("id")){
                propertyName = "_id";
            }
            try {

                if(field.getType().equals(double.class)){
                    Method m = clazz.getMethod(setterName, double.class);
                    m.invoke(target, object.getDouble(propertyName));

                }else if(field.getType().equals(float.class)){
                    Method m = clazz.getMethod(setterName, float.class);
                    m.invoke(target,(float) object.getDouble(propertyName));

                }else if(field.getType().equals(int.class)){
                    Method m = clazz.getMethod(setterName, int.class);
                    m.invoke(target,object.getInt(propertyName));

                }else if(field.getType().equals(long.class)){
                    Method m = clazz.getMethod(setterName, long.class);
                    m.invoke(target,object.getLong(propertyName));

                }else if(field.getType().equals(boolean.class)){
                    Method m = clazz.getMethod(setterName, boolean.class);
                    m.invoke(target,object.getBoolean(propertyName));

                }else if(field.getType().equals(String.class)){
                    Method m = clazz.getMethod(setterName, String.class);
                    m.invoke(target,object.getString(propertyName));

                }else if(field.getType().equals(Date.class)){
                    Method m = clazz.getMethod(setterName, Date.class);
                    String tempDate = object.getString(propertyName);
                    m.invoke(target,Iso8601.toCalendar(tempDate).getTime());

                }else{
                    Log.i("Json","Type not known "+field);
                }
            } catch (NoSuchMethodException e) {
                Log.i("Json","No method "+setterName);
            } catch (JSONException e) {
                Log.i("Json",e.toString());
            } catch (InvocationTargetException e) {
                Log.i("Json",e.toString());
            } catch (ParseException e) {
                Log.i("Json",e.toString());
            }

        }


        return target;
    }
    public static List toList(JSONArray array,Class clazz){

        List list = new ArrayList();
        for(int i = 0; i < array.length(); i++){
            try {
                list.add(toObject(array.getJSONObject(i),clazz));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return list;
    }
}
