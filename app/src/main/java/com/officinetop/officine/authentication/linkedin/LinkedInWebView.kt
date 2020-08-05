package com.officinetop.officine.authentication.linkedin

import android.app.Activity
import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.officinetop.officine.R
import com.officinetop.officine.retrofit.RetrofitClient
import com.officinetop.officine.utils.Constant
import com.officinetop.officine.utils.getProgressDialog
import kotlinx.android.synthetic.main.activity_linkedin_login.*
import okhttp3.ResponseBody
import org.jetbrains.anko.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class LinkedInWebView : AppCompatActivity() {

    /*CONSTANT FOR THE AUTHORIZATION PROCESS*/

    /****FILL THIS WITH YOUR INFORMATION */
    //This is the public api key of our application// 81nih3m4xgyr71
    private val API_KEY = "81txmpo07r3ew8"
    //This is the private api key of our application // ytP7ra2wy419xkvt
    private val SECRET_KEY = "mCnCDHjxDWtUB8Lv"
    //This is any string we want to use. This will be used for avoiding CSRF attacks. You can generate one here: http://strongpasswordgenerator.com/
    private val STATE = "xyz"
    //E3ZYKC1T6H2yP4z
    //This is the url that LinkedIn Auth process will redirect to. We can put whatever we want that starts with http:// or https:// .
    //We use a made up url that we will intercept when redirecting. Avoid Uppercases.
    private val REDIRECT_URI = Constant.domainBaseURL
    private val scope = //"scope=r_emailaddress"
            "scope=r_liteprofile%20r_emailaddress%20w_member_social"
    /** */

    //These are constants used for build the urls
    private val AUTHORIZATION_URL =
            "https://www.linkedin.com/oauth/v2/authorization"
//            "https://www.linkedin.com/uas/oauth2/authorization"
    private val ACCESS_TOKEN_URL =
            "https://www.linkedin.com/oauth/v2/accessToken"
                  //  "https://www.linkedin.com/uas/oauth2/accessToken"



    private val SECRET_KEY_PARAM = "client_secret"
    private val RESPONSE_TYPE_PARAM = "response_type"
    private val GRANT_TYPE_PARAM = "grant_type"
    private val GRANT_TYPE = "authorization_code"
    private val RESPONSE_TYPE_VALUE = "code"
    private val CLIENT_ID_PARAM = "client_id"
    private val STATE_PARAM = "state"
    private val REDIRECT_URI_PARAM = "redirect_uri"
    /*---------------------------------------*/
    private val QUESTION_MARK = "?"
    private val AMPERSAND = "&"
    private val EQUALS = "="

    lateinit var webView: WebView
    lateinit var pd:ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_linkedin_login)

        //get the webView from the layout
        webView = linkedin_web_view

        //Request focus for the webview
        webView.requestFocus(View.FOCUS_DOWN)

        //Show a progress filterDialog to the user
        pd = ProgressDialog.show(this, "", getString(R.string.Please_wait), true)

        //Set a custom web view client
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                //This method will be executed each time a page finished loading.
                //The only we do is dismiss the progressDialog, in case we are showing any.
                if (pd.isShowing) {
                    pd.dismiss()
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView, authorizationUrl: String): Boolean {
                //This method will be called when the Auth proccess redirect to our RedirectUri.
                //We will check the url looking for our RedirectUri.
                if (authorizationUrl.startsWith(REDIRECT_URI)) {
                    Log.i("LinkedIn", "")
                    val uri = Uri.parse(authorizationUrl)
                    //We take from the url the authorizationToken and the state token. We have to check that the state token returned by the Service is the same we sent.
                    //If not, that means the request may be a result of CSRF and must be rejected.
                    val stateToken = uri.getQueryParameter(STATE_PARAM)
                    if (stateToken == null || stateToken != STATE) {
                        Log.e("LinkedIn", "State token doesn't match")
                        return true
                    }

                    //If the user doesn't allow authorization to our application, the authorizationToken Will be null.
                    val authorizationToken = uri.getQueryParameter(RESPONSE_TYPE_VALUE)
                    if (authorizationToken == null) {
                        Log.i("LinkedIn", "The user doesn't allow authorization.")
                        return true
                    }
                    Log.i("LinkedIn", "Auth token received: $authorizationToken")




                    //Generate URL for requesting Access Token
                    val accessTokenUrl = getAccessTokenUrl(authorizationToken)
                    Log.d("LinkedInWebView", "shouldOverrideUrlLoading: access token url = $accessTokenUrl")
                    //We make the request in a AsyncTask
//                    PostRequestAsyncTask().execute(accessTokenUrl)
                    loadAuthURL(accessTokenUrl)

                } else {
                    //Default behaviour
                    Log.i("LinkedIn", "Redirecting to: $authorizationUrl")
                    webView.loadUrl(authorizationUrl)
                }
                return true
            }
        }




        //Get the authorization Url
        val authUrl = getAuthorizationUrl()
        Log.i("LinkedIn", "Loading Auth Url: $authUrl")
        //Load the authorization URL into the webView
        webView.loadUrl(authUrl)
    }


    private fun loadAuthURL(authURL : String){


        doAsync {


            val url = URL(authURL)
            val connection = url.openConnection() as HttpsURLConnection


            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            var response = ""
            var line : String?

            do {

                line = reader.readLine()
                response+=line

                if (line == null)

                    break

                println(line)

            } while (true)

            reader.close()

            if(response.contains("access_token")){
                val json = JSONObject(response)
                val accessToken = json.getString("access_token")
                val expiresIn = json.getLong("expires_in")

                Log.d("LinkedInWebView", "loadAuthURL: access token = $accessToken")
                Log.d("LinkedInWebView", "loadAuthURL: expires in = $expiresIn")


                RetrofitClient.linkedInClient.linkedInGetEmail(accessToken = "Bearer $accessToken")
                        .enqueue(object : Callback<ResponseBody> {
                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                Log.d("LinkedInWebView", "onFailure: email error = ${t.message}")
                                toast("Failed to fetch user data")
                                finish()
                            }

                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                val body = response.body()?.string()
                                Log.d("LinkedInWebView", "onResponse: email response = $body")
                                try {

                                    val rootJSON = JSONObject(body)
                                    val elementArray  = rootJSON.getJSONArray("elements")
                                    val handleItem = JSONObject(elementArray[0].toString())
                                    val handleObject = handleItem.getString("handle~")
                                    val emailAddress = JSONObject(handleObject).getString("emailAddress")


                                    if (emailAddress.isNotEmpty()) {


                                        RetrofitClient.linkedInClient.linkedInGetDetail("Bearer $accessToken").enqueue(object : Callback<ResponseBody>{
                                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                                Log.d("LinkedInWebView", "onFailure: detail error = ${t.message}")
                                            }

                                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                                val detailBody = response.body()?.string()

                                                val firstName = JSONObject(detailBody).getString("localizedFirstName")
                                                val lastName = JSONObject(detailBody).getString("localizedLastName")
                                                val id = JSONObject(detailBody).getString("id")

                                                val intent = intentFor<Any>("first" to firstName,
                                                        "last" to lastName,
                                                        "id" to id,
                                                "email" to emailAddress)

                                                setResult(Activity.RESULT_OK, intent)
                                                finish()

                                                Log.d("LinkedInWebView", "onResponse: detail = $detailBody")
                                            }

                                        })

                                    }
                                    else{
                                        alert {
                                            message = ("User either doesn't have email or has kept hidden from public")
                                            isCancelable = false
                                            okButton {
                                                setResult(Activity.RESULT_CANCELED)
                                                finish()
                                            }
                                        }.show()
                                    }
                                }
                                catch (e:Exception){
                                    e.printStackTrace()
                                    toast("Failed to fetch user data")
                                    finish()
                                    Log.e("LinkedIN", "LinkedInWebView: onResponse = ${e.message}" )
                                }

                            }

                        })
            }

        }
    }


    /**
     * Method that generates the url for get the access token from the Service
     * @return Url
     */
    private fun getAccessTokenUrl(authorizationToken: String): String {
        return (ACCESS_TOKEN_URL
                + QUESTION_MARK
                + GRANT_TYPE_PARAM + EQUALS + GRANT_TYPE
                + AMPERSAND
                + RESPONSE_TYPE_VALUE + EQUALS + authorizationToken
                + AMPERSAND
                + CLIENT_ID_PARAM + EQUALS + API_KEY
                + AMPERSAND
                + REDIRECT_URI_PARAM + EQUALS + REDIRECT_URI
                + AMPERSAND
                + SECRET_KEY_PARAM + EQUALS + SECRET_KEY
                + AMPERSAND + scope)
    }

    /**
     * Method that generates the url for get the authorization token from the Service
     * @return Url
     */
    private fun getAuthorizationUrl(): String {
        return (AUTHORIZATION_URL
                + QUESTION_MARK + RESPONSE_TYPE_PARAM + EQUALS + RESPONSE_TYPE_VALUE
                + AMPERSAND + CLIENT_ID_PARAM + EQUALS + API_KEY
                + AMPERSAND + STATE_PARAM + EQUALS + STATE
                + AMPERSAND + REDIRECT_URI_PARAM + EQUALS + REDIRECT_URI
                + AMPERSAND + scope)
    }




}