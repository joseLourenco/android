package io.homeassistant.companion.android

import androidx.activity.OnBackPressedCallback
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.InputCallback
import androidx.car.app.model.Template
import androidx.car.app.model.signin.InputSignInMethod
import androidx.car.app.model.signin.SignInTemplate
import io.homeassistant.companion.android.HomeAssistantApplication.R
import io.homeassistant.companion.android.oauth2.OAuth2Client
import io.homeassistant.companion.android.oauth2.Token
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class LoginScreen(carContext: CarContext) : Screen(carContext) {

    private val ioScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private val CLIENT_ID = "https://home-assistant.io/android"
    private val REDIRECT_URI = "homeassistant://auth-callback"
    private val GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code"

    enum class State {
        URL, USERNAME, PASSWORD, AUTHENTICATING, SIGN_IN_FAILED
    }

    private var mInstructions: String? = null
    var mState = State.URL
    var mLastErrorMessage = "" // last displayed error message

    var mErrorMessage = ""
    var homeAssistantUrl: String? = null
    var username: String? = null
    var password: String? = null

    init {
        // Handle back pressed events manually, as we use them to navigate between templates within
        // the same screen.

        // Handle back pressed events manually, as we use them to navigate between templates within
        // the same screen.
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                mErrorMessage = ""
                if (mState === State.URL) {
                    screenManager.pop()
                } else {
                    mState = State.URL
                    invalidate()
                }
            }
        }
        carContext.onBackPressedDispatcher.addCallback(this, callback)

        mInstructions = getCarContext().getString(R.string.credentials_login_option_subtitle)
    }

    override fun onGetTemplate(): Template {
        when (mState) {
            State.URL, State.SIGN_IN_FAILED -> return getUrlSignInTemplate()
            State.USERNAME -> return getUsernameSignInTemplate()
            State.PASSWORD, State.AUTHENTICATING -> return getPasswordSignInTemplate()
            else -> throw IllegalStateException("Invalid state: $mState")
        }
    }

    fun getUrlSignInTemplate(): Template {
        if (mState === State.SIGN_IN_FAILED) {
            mErrorMessage = carContext.getString(R.string.sign_in_error)
            mState = State.URL
        }
        val listener: InputCallback = object : InputCallback {
            override fun onInputSubmitted(text: String) {
                if (mState === State.URL) {
                    homeAssistantUrl = text
                    submitUrl()
                }
            }

            override fun onInputTextChanged(text: String) {
                // This callback demonstrates how to use handle the text changed event.
                // In this case, we check that the user name doesn't exceed a certain length.
                if (mState === State.URL) {
                    homeAssistantUrl = text
                    mErrorMessage = validateUrl()

                    // Invalidate the template (and hence possibly update the error message) only
                    // if clearing up the error string, or if the error is changing.
                    if (!mLastErrorMessage.isEmpty()
                        && (mErrorMessage.isEmpty()
                                || mLastErrorMessage != mErrorMessage)
                    ) {
                        invalidate()
                    }
                }
            }
        }
        val builder = InputSignInMethod.Builder(listener)
            .setHint(carContext.getString(R.string.home_assistant_url_hint))
            .setKeyboardType(InputSignInMethod.KEYBOARD_DEFAULT)
        builder.setErrorMessage(mErrorMessage)
        mLastErrorMessage = mErrorMessage
        if (homeAssistantUrl != null) {
            builder.setDefaultValue(homeAssistantUrl!!)
        }
        val signInMethod = builder.build()
        val signInTemplate = SignInTemplate.Builder(signInMethod)
            .setTitle(carContext.getString(R.string.app_name))
            .setHeaderAction(Action.APP_ICON)
            .setAdditionalText(carContext.getString(R.string.additional_home_assistant_url_message))

        if (mInstructions != null) {
            signInTemplate.setInstructions(mInstructions!!)
        }

        return signInTemplate.build()
    }

    /**
     * Moves to the username screen if the homeAssistant url currently entered is valid, or displays
     * an error message otherwise.
     */
    fun submitUrl() {
        mErrorMessage = validateUrl()
        val isError = !mErrorMessage.isEmpty()
        if (!isError) {
            // If there's no error, go to the username screen.
            mState = State.USERNAME
        }

        // Invalidate the template so that we either display an error, or go to the username screen.
        invalidate()
    }

    /**
     * Validates the currently entered url and returns an error message string if invalid,
     * or an empty string otherwise.
     */
    fun validateUrl(): String {
        return if (homeAssistantUrl != null &&
            (homeAssistantUrl!!.startsWith("http://") || homeAssistantUrl!!.startsWith("https://"))
        ) {
            ""
        } else {
            carContext.getString(R.string.invalid_home_assistant_url)
        }
    }

    fun getUsernameSignInTemplate(): Template {
        val listener: InputCallback = object : InputCallback {
            override fun onInputSubmitted(text: String) {
                if (mState === State.USERNAME) {
                    username = text
                    submitUsername()
                }
            }

            override fun onInputTextChanged(text: String) {
                // This callback demonstrates how to use handle the text changed event.
                // In this case, we check that the user name doesn't exceed a certain length.
                if (mState === State.USERNAME) {
                    username = text
                    mErrorMessage = validateUsername()

                    // Invalidate the template (and hence possibly update the error message) only
                    // if clearing up the error string, or if the error is changing.
                    if (!mLastErrorMessage.isEmpty()
                        && (mErrorMessage.isEmpty()
                                || mLastErrorMessage != mErrorMessage)
                    ) {
                        invalidate()
                    }
                }
            }
        }
        val builder = InputSignInMethod.Builder(listener)
            .setHint(carContext.getString(R.string.username_hint))
            .setKeyboardType(InputSignInMethod.KEYBOARD_DEFAULT)
        builder.setErrorMessage(mErrorMessage)
        mLastErrorMessage = mErrorMessage
        if (username != null) {
            builder.setDefaultValue(username!!)
        }
        val signInMethod = builder.build()
        val signInTemplate = SignInTemplate.Builder(signInMethod)
            .setTitle(carContext.getString(R.string.app_name))
            .setHeaderAction(Action.APP_ICON)
            .setAdditionalText(carContext.getString(R.string.additional_username_message))

        if (mInstructions != null) {
            signInTemplate.setInstructions(mInstructions!!)
        }

        return signInTemplate.build()
    }

    /**
     * Moves to the password screen if the user name currently entered is valid, or displays
     * an error message otherwise.
     */
    fun submitUsername() {
        mErrorMessage = validateUsername()
        val isError = !mErrorMessage.isEmpty()
        if (!isError) {
            // If there's no error, go to the username screen.
            mState = State.PASSWORD
        }

        // Invalidate the template so that we either display an error, or go to the username screen.
        invalidate()
    }

    /**
     * Validates the currently entered user name and returns an error message string if invalid,
     * or an empty string otherwise.
     */
    fun validateUsername(): String {
        return if (username != null && !username!!.isEmpty()) {
            ""
        } else {
            carContext.getString(R.string.invalid_username)
        }
    }

    fun getPasswordSignInTemplate(): Template {
        val callback: InputCallback = object : InputCallback {
            override fun onInputSubmitted(text: String) {
                password = text
                //TODO
                getAccessToken()
                invalidate()
            }
        }
        val builder: InputSignInMethod.Builder = InputSignInMethod.Builder(callback)
            .setHint(carContext.getString(R.string.password_hint))
            .setInputType(InputSignInMethod.INPUT_TYPE_PASSWORD)
        builder.setErrorMessage(mErrorMessage)
        val signInMethod: InputSignInMethod = builder.build()
        val signInTemplate = SignInTemplate.Builder(signInMethod)
            .setTitle(carContext.getString(R.string.app_name))
            .setHeaderAction(Action.BACK)
            .setLoading(mState === State.AUTHENTICATING)
            .setAdditionalText(carContext.getString(R.string.additional_password_message))

        if (mInstructions != null) {
            signInTemplate.setInstructions(mInstructions!!)
        }

        return signInTemplate.build()
    }

    fun getAccessToken() {
        ioScope.launch {
            val client = OAuth2Client(
                username,
                password,
                CLIENT_ID,
                "",
                homeAssistantUrl + "/auth/authorize"
            )
            val token: Token = client.accessToken
        }
    }

}