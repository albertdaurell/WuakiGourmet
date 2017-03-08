package tv.wuaki.wuakigourmet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.util.*;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;

public class Check extends AppCompatActivity {

    private static String userNameKey = "uname";
    private static String passwordKey = "pwd";

    /**
     * Create a instance, load the parameters and prepare the UI
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        this.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                loadResult(false);
            }
        });

        this.loadParams();
        this.loadResult(true);

    }

    /**
     * Load and decrypt the parameters ( username / password )
     */
    private void loadParams() {
        EditText username = (EditText) this.findViewById(R.id.username);
        EditText password = (EditText) this.findViewById(R.id.password);
        SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
        username.setText(tv.wuaki.wuakigourmet.Crypto.decrypt(mPrefs.getString(userNameKey, "")));
        password.setText(tv.wuaki.wuakigourmet.Crypto.decrypt(mPrefs.getString(passwordKey, "")));
    }

    /**
     * Encrypt and store the parameters ( username / password ) and commit the result
     */
    private void storeParams() {

        EditText username = (EditText) this.findViewById(R.id.username);
        EditText password = (EditText) this.findViewById(R.id.password);
        SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
        Editor mPrefsEditor = mPrefs.edit();
        mPrefsEditor.putString(userNameKey, tv.wuaki.wuakigourmet.Crypto.encrypt(username.getText().toString()));
        mPrefsEditor.putString(passwordKey, tv.wuaki.wuakigourmet.Crypto.encrypt(password.getText().toString()));
        mPrefsEditor.commit();

    }

    /**
     * Load the result and cleanup the current webview html
     *
     * @param clean Cleanup the view or append the result to the webview html
     */
    public void loadResult(Boolean clean) {

        final Check that = this;

        setResult("...", !clean);

        this.storeParams();
        this.findViewById(R.id.button).setVisibility(View.GONE);
        this.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {

                    // Prepare the request parameters
                    EditText username = (EditText) that.findViewById(R.id.username);
                    EditText password = (EditText) that.findViewById(R.id.password);
                    String strusername = username.getText().toString();
                    String strpassword = password.getText().toString();

                    // Avoid empty requests
                    if (strusername.length() == 0 || strpassword.length() == 0) {
                        setResult("<div align='center'>" + getString(R.string.empty_parameters) + "</div>", false);
                        return;
                    }

                    // Create a new HttpClient and Post Header using the URL string
                    String url = "http://tarjetagourmet.chequegourmet.com/processLogin_iphoneApp.jsp";
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(url);

                    try {

                        // Prepare the parameters
                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
                        nameValuePairs.add(new BasicNameValuePair("usuario", strusername));
                        nameValuePairs.add(new BasicNameValuePair("contrasena", strpassword));
                        nameValuePairs.add(new BasicNameValuePair("token", "xAeSYsTQQTCVyPOGWLpR"));
                        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                        // Execute POST and get the response
                        HttpResponse response = httpclient.execute(httppost);

                        // Read the response
                        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "ISO-8859-1"));
                        StringBuffer result = new StringBuffer();
                        String line = "";
                        while ((line = rd.readLine()) != null) {
                            result.append(line);
                        }

                        // And store the result
                        setResult(result.toString(), false);

                    } catch (Throwable e) {
                        setResult("<div align='center'>" + getString(R.string.request_exception) + "</div>", false);
                    }

                } catch (Exception e) {
                    setResult("<div align='center'>" + getString(R.string.thread_exception) + "</div>", false);
                }

            }
        });

        thread.start();

    }

    private String result = "";

    public void setResult(final String txt, final Boolean prepend) {

        Calendar cal = Calendar.getInstance();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String pre = sdf.format(cal.getTime());
        final String str = "<div align='center'>" + pre + "</div><hr/>" + txt;

        final Check that = this;

        runOnUiThread(new Runnable() {
            public void run() {

                WebView webview = (WebView) that.findViewById(R.id.webView);
                that.findViewById(R.id.button).setVisibility(View.VISIBLE);
                that.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);

                if (prepend == false) {
                    result = str;
                }

                // Modify background and text color
                // result += "<style type=\"text/css\" >html, body, tr, td, div, span { color: white !important; background: black !important; }</style>";

                webview.getSettings().setJavaScriptEnabled(false);
                webview.loadData("<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\" ?>" + result, "text/html; charset=UTF-8", "UTF-8");

            }
        });

    }

}
