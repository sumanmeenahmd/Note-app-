package com.example

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color as AndroidColor
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.webkit.ServiceWorkerClientCompat
import androidx.webkit.ServiceWorkerControllerCompat
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TARGET_HOST = "notes-app-chi.pages.dev"
private const val TARGET_URL = "https://$TARGET_HOST"

class MainActivity : ComponentActivity() {

    private var isSplashReady by mutableStateOf(false)
    private var isDarkTheme by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { !isSplashReady }

        // Safety timeout so splash never blocks indefinitely on slow networks
        lifecycleScope.launch {
            delay(1500L)
            isSplashReady = true
        }

        isDarkTheme = isSystemDarkMode(resources.configuration)
        applySystemBars(isDarkTheme)
        configureServiceWorker(this)

        setContent {
            val darkTheme = isDarkTheme

            LaunchedEffect(darkTheme) {
                applySystemBars(darkTheme)
            }

            MaterialTheme(
                colorScheme = if (darkTheme) {
                    darkColorScheme(
                        background = Color(0xFF0F172A),
                        surface = Color(0xFF0F172A),
                        onBackground = Color(0xFFF8FAFC),
                        primary = Color(0xFF60A5FA),
                        onPrimary = Color(0xFF0F172A)
                    )
                } else {
                    lightColorScheme(
                        background = Color(0xFFF8FAFC),
                        surface = Color(0xFFF8FAFC),
                        onBackground = Color(0xFF0F172A),
                        primary = Color(0xFF203A53),
                        onPrimary = Color(0xFFFFFFFF)
                    )
                }
            ) {
                NotesWebViewScreen(
                    isDarkTheme = darkTheme,
                    onInitialFrameReady = { isSplashReady = true }
                )
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        isDarkTheme = isSystemDarkMode(newConfig)
        applySystemBars(isDarkTheme)
    }

    private fun applySystemBars(dark: Boolean) {
        enableEdgeToEdge(
            statusBarStyle = if (dark) {
                SystemBarStyle.dark(AndroidColor.TRANSPARENT)
            } else {
                SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
            },
            navigationBarStyle = if (dark) {
                SystemBarStyle.dark(AndroidColor.TRANSPARENT)
            } else {
                SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
            }
        )
    }

    private fun isSystemDarkMode(configuration: Configuration): Boolean {
        return (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun NotesWebViewScreen(
    isDarkTheme: Boolean,
    onInitialFrameReady: () -> Unit
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var hasFatalError by remember { mutableStateOf(false) }
    var isOnline by remember { mutableStateOf(isNetworkAvailable(context)) }
    var webViewRecreateKey by remember { mutableIntStateOf(0) }

    var pendingFileCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    var pendingDownloadBytes by remember { mutableStateOf<ByteArray?>(null) }

    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        pendingFileCallback?.onReceiveValue(uris.toTypedArray())
        pendingFileCallback = null
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { destinationUri ->
        val bytes = pendingDownloadBytes
        pendingDownloadBytes = null
        if (destinationUri != null && bytes != null) {
            runCatching {
                context.contentResolver.openOutputStream(destinationUri)?.use { stream ->
                    stream.write(bytes)
                }
            }
        }
    }

    val blobBridge = remember {
        BlobDownloadBridge { fileName, base64Data ->
            val decoded = runCatching {
                val pureBase64 = base64Data.substringAfter(",", base64Data)
                Base64.decode(pureBase64, Base64.DEFAULT)
            }.getOrNull() ?: return@BlobDownloadBridge
            pendingDownloadBytes = decoded
            createDocumentLauncher.launch(fileName)
        }
    }

    // Monitor network connectivity for automatic offline cache switching & auto-recovery
    DisposableEffect(context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isOnline = true
                webViewRef?.post {
                    webViewRef?.settings?.cacheMode = WebSettings.LOAD_DEFAULT
                    if (hasFatalError) {
                        hasFatalError = false
                        webViewRef?.loadUrl(TARGET_URL)
                    }
                }
            }

            override fun onLost(network: Network) {
                val stillOnline = isNetworkAvailable(context)
                isOnline = stillOnline
                webViewRef?.post {
                    webViewRef?.settings?.cacheMode =
                        if (stillOnline) WebSettings.LOAD_DEFAULT
                        else WebSettings.LOAD_CACHE_ELSE_NETWORK
                }
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        runCatching { connectivityManager?.registerNetworkCallback(request, callback) }

        onDispose {
            runCatching { connectivityManager?.unregisterNetworkCallback(callback) }
        }
    }

    // Sync WebView dark mode whenever device theme changes
    LaunchedEffect(isDarkTheme, webViewRef) {
        webViewRef?.let { applyWebViewDarkTheme(it, isDarkTheme) }
    }

    BackHandler(enabled = canGoBack) {
        val view = webViewRef
        if (view != null && view.canGoBack()) {
            view.goBack()
        } else {
            canGoBack = false
        }
    }

    val surfaceColor = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        if (!hasFatalError) {
            androidx.compose.runtime.key(webViewRecreateKey) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("notes_webview"),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            setBackgroundColor(
                                if (isDarkTheme) AndroidColor.parseColor("#0F172A")
                                else AndroidColor.parseColor("#F8FAFC")
                            )

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                allowContentAccess = true
                                allowFileAccess = false
                                loadsImagesAutomatically = true
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                setSupportZoom(false)
                                builtInZoomControls = false
                                displayZoomControls = false
                                mediaPlaybackRequiresUserGesture = false
                                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                                cacheMode = if (isOnline) {
                                    WebSettings.LOAD_DEFAULT
                                } else {
                                    WebSettings.LOAD_CACHE_ELSE_NETWORK
                                }
                            }

                            val currentWebView = this
                            CookieManager.getInstance().apply {
                                setAcceptCookie(true)
                                setAcceptThirdPartyCookies(currentWebView, true)
                            }

                            applyWebViewDarkTheme(this, isDarkTheme)
                            addJavascriptInterface(blobBridge, "AndroidBlobDownloader")

                            setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
                                val guessedName = URLUtil.guessFileName(
                                    url,
                                    contentDisposition,
                                    mimeType
                                ).ifBlank {
                                    val ext = MimeTypeMap.getSingleton()
                                        .getExtensionFromMimeType(mimeType) ?: "txt"
                                    "note-export.$ext"
                                }

                                when {
                                    url.startsWith("blob:") -> {
                                        val escapedUrl = url.replace("'", "\\'")
                                        val escapedName = guessedName.replace("'", "\\'")
                                        evaluateJavascript(
                                            """
                                            (function() {
                                              fetch('$escapedUrl')
                                                .then(function(r) { return r.blob(); })
                                                .then(function(blob) {
                                                  var reader = new FileReader();
                                                  reader.onloadend = function() {
                                                    if (window.AndroidBlobDownloader) {
                                                      window.AndroidBlobDownloader.onBlobReady('$escapedName', reader.result);
                                                    }
                                                  };
                                                  reader.readAsDataURL(blob);
                                                });
                                            })();
                                            """.trimIndent(),
                                            null
                                        )
                                    }

                                    url.startsWith("data:") -> {
                                        val decoded = runCatching {
                                            val base64Part = url.substringAfter(",", "")
                                            Base64.decode(base64Part, Base64.DEFAULT)
                                        }.getOrNull()
                                        if (decoded != null) {
                                            pendingDownloadBytes = decoded
                                            createDocumentLauncher.launch(guessedName)
                                        }
                                    }

                                    else -> {
                                        runCatching {
                                            ctx.startActivity(
                                                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            )
                                        }
                                    }
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onShowFileChooser(
                                    webView: WebView?,
                                    filePathCallback: ValueCallback<Array<Uri>>?,
                                    fileChooserParams: FileChooserParams?
                                ): Boolean {
                                    pendingFileCallback?.onReceiveValue(null)
                                    pendingFileCallback = filePathCallback
                                    val acceptTypes = fileChooserParams?.acceptTypes
                                        ?.filter { it.isNotBlank() }
                                        ?.toTypedArray()
                                        ?.takeIf { it.isNotEmpty() }
                                        ?: arrayOf("*/*")
                                    fileChooserLauncher.launch(acceptTypes)
                                    return true
                                }
                            }

                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val uri = request?.url ?: return false
                                    val host = uri.host?.lowercase()

                                    // Upgrade http://notes-app-chi.pages.dev to https:// for PWA & security
                                    if (host == TARGET_HOST) {
                                        if (uri.scheme == "http") {
                                            val httpsUri = uri.buildUpon().scheme("https").build()
                                            view?.loadUrl(httpsUri.toString())
                                            return true
                                        }
                                        return false
                                    }

                                    // Keep about:blank or data/blob inside WebView if triggered internally
                                    if (uri.scheme in listOf("about", "data", "blob")) {
                                        return false
                                    }

                                    // Open external links outside the app
                                    runCatching {
                                        ctx.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                    }
                                    return true
                                }

                                override fun onPageCommitVisible(view: WebView?, url: String?) {
                                    super.onPageCommitVisible(view, url)
                                    canGoBack = view?.canGoBack() == true
                                    onInitialFrameReady()
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    canGoBack = view?.canGoBack() == true
                                    onInitialFrameReady()
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    super.onReceivedError(view, request, error)
                                    if (request?.isForMainFrame == true) {
                                        hasFatalError = true
                                        onInitialFrameReady()
                                    }
                                }

                                override fun onRenderProcessGone(
                                    view: WebView?,
                                    detail: android.webkit.RenderProcessGoneDetail?
                                ): Boolean {
                                    webViewRecreateKey += 1
                                    return true
                                }
                            }

                            webViewRef = this
                            loadUrl(TARGET_URL)
                        }
                    },
                    update = { view ->
                        view.setBackgroundColor(
                            if (isDarkTheme) AndroidColor.parseColor("#0F172A")
                            else AndroidColor.parseColor("#F8FAFC")
                        )
                    }
                )
            }
        } else {
            OfflineFallback(
                onRetry = {
                    val onlineNow = isNetworkAvailable(context)
                    isOnline = onlineNow
                    hasFatalError = false
                    webViewRef?.settings?.cacheMode = if (onlineNow) {
                        WebSettings.LOAD_DEFAULT
                    } else {
                        WebSettings.LOAD_CACHE_ELSE_NETWORK
                    }
                    webViewRecreateKey += 1
                }
            )
        }
    }
}

@Composable
private fun OfflineFallback(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Refresh,
            contentDescription = stringResource(R.string.cd_offline_icon),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(44.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.offline_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.offline_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .height(48.dp)
                .testTag("retry_button")
        ) {
            Text(text = stringResource(R.string.action_retry))
        }
    }
}

private class BlobDownloadBridge(
    private val onFileReady: (fileName: String, base64Data: String) -> Unit
) {
    @JavascriptInterface
    fun onBlobReady(fileName: String, base64Data: String) {
        onFileReady(fileName, base64Data)
    }
}

private fun configureServiceWorker(context: Context) {
    if (WebViewFeature.isFeatureSupported(WebViewFeature.SERVICE_WORKER_BASIC_USAGE)) {
        val controller = ServiceWorkerControllerCompat.getInstance()
        val online = isNetworkAvailable(context)
        controller.serviceWorkerWebSettings.apply {
            allowContentAccess = true
            allowFileAccess = false
            cacheMode = if (online) {
                WebSettings.LOAD_DEFAULT
            } else {
                WebSettings.LOAD_CACHE_ELSE_NETWORK
            }
        }
        controller.setServiceWorkerClient(object : ServiceWorkerClientCompat() {
            override fun shouldInterceptRequest(request: WebResourceRequest) = null
        })
    }
}

@Suppress("DEPRECATION")
private fun applyWebViewDarkTheme(webView: WebView, isDark: Boolean) {
    val settings = webView.settings
    if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
        WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, isDark)
    } else if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
        WebSettingsCompat.setForceDark(
            settings,
            if (isDark) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF
        )
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
            WebSettingsCompat.setForceDarkStrategy(
                settings,
                WebSettingsCompat.DARK_STRATEGY_PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING
            )
        }
    }
}

private fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
