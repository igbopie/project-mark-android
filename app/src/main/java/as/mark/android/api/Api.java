package as.mark.android.api;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import as.mark.android.JSONToObject;
import as.mark.android.MarkApp;
import as.mark.android.model.Mark;

/**
 * Created by igbopie on 10/9/14.
 */
public class Api {

    public static final String ENDPOINT = "https://mark-test-2.herokuapp.com/";
    public static final String ENDPOINT_LOGIN = "api/user/login";
    public static final String ENDPOINT_MARK_SEARCH = "api/mark/search";


    public static final int RESPONSE_CODE_OK = 200;
    public static final int RESPONSE_CODE_OK_CREATED = 201;
    public static final int RESPONSE_CODE_CLIENT_LOGIN_TIMEOUT = 440;
    public static final int RESPONSE_CODE_CLIENT_USERNAME_ALREADY_EXISTS = 466;
    public static final int RESPONSE_CODE_CLIENT_EMAIL_ALREADY_EXISTS = 467;


    public static final String JSON_TAG_CODE = "code";
    public static final String JSON_TAG_MESSAGE = "message";
    public static final String JSON_TAG_RESPONSE = "response";


    public static final String JSON_TAG_LOGIN_TOKEN = "token";

    public static String login(String username, String password) throws Exception {
        HashMap<String,String>params = new HashMap<String, String>();
        params.put("username",username);
        params.put("password",password);

        HttpResponse httpResponse = makeRequest(ENDPOINT+ENDPOINT_LOGIN,params);
        int responseCode = httpResponse.getStatusLine().getStatusCode();
        if(responseCode == RESPONSE_CODE_OK){
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            httpResponse.getEntity().writeTo(os);
            String output = os.toString( "UTF-8" );
            //Utils.debug(Api.class,"Output:"+output);
            JSONObject jsonObj = new JSONObject(output);
            String token = jsonObj.getJSONObject(JSON_TAG_RESPONSE).getString(JSON_TAG_LOGIN_TOKEN);
            return token;

        }
        return null;
    }

    public static List searchMarks(double longitude,
                                   double latitude,
                                   double userLongitude,
                                   double userLatitude,
                                   double radius) throws Exception {
        HashMap<String,String>params = new HashMap<String, String>();
        params.put("longitude", String.valueOf(longitude));
        params.put("latitude", String.valueOf(latitude));
        params.put("userLongitude", String.valueOf(userLongitude));
        params.put("userLatitude", String.valueOf(userLatitude));
        params.put("radius", String.valueOf(radius));
        params.put("token", MarkApp.getToken());
        Log.i("Api","searchMarks:"+longitude+" "+latitude);
        HttpResponse httpResponse = makeRequest(ENDPOINT+ENDPOINT_MARK_SEARCH,params);
        int responseCode = httpResponse.getStatusLine().getStatusCode();
        if(responseCode == RESPONSE_CODE_OK){
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            httpResponse.getEntity().writeTo(os);
            String output = os.toString( "UTF-8" );
            //Utils.debug(Api.class,"Output:"+output);
            JSONObject jsonObj = new JSONObject(output);
            JSONArray results = jsonObj.getJSONArray(JSON_TAG_RESPONSE);

            Log.i("Api",results.toString());
            return JSONToObject.toList(results,Mark.class);

        }else if(responseCode == 440){
            String token = login(MarkApp.getUsername(),MarkApp.getPassword());
            Log.i("Api","Login "+token);
            MarkApp.login(MarkApp.getUsername(),MarkApp.getPassword(),token);
            return searchMarks(longitude,latitude,userLongitude,userLatitude,radius);
        }
        return null;
    }


    public static HttpResponse makeRequest(String path, Map<String,String> params) throws Exception
    {

        //url with the post data
        HttpPost httpPost = new HttpPost(path);

        //convert parameters into JSON object

        StringBuffer content = new StringBuffer();
        for(Map.Entry<String,String> entry:params.entrySet()){
            content.append(entry.getKey());
            content.append("=");
            content.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            content.append("&");
        }
        //remove last &
        if(content.length() > 0 ) {
            content.setLength(content.length() - 1);
        }

        //passes the results to a string builder/entity
        StringEntity se = new StringEntity(content.toString());

        //sets the post request as the resulting string
        httpPost.setEntity(se);
        //sets a request header so the page receving the request
        //will know what to do with it
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");

        //Handles what is returned from the page
        //ResponseHandler responseHandler = new BasicResponseHandler();

        //instantiates httpclient to make request
        DefaultHttpClient httpClient = new DefaultHttpClient();
        return httpClient.execute(httpPost);
    }

}
