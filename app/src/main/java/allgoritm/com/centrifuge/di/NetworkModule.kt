package allgoritm.com.centrifuge.di

import allgoritm.com.centrifuge.BuildConfig
import allgoritm.com.centrifuge.data.RequestManager
import allgoritm.com.centrifuge.v1.YCentrifugeFactory
import android.annotation.SuppressLint
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.*

@Module
class NetworkModule {

    @Singleton
    @Provides
    fun provideGson() : Gson = GsonBuilder().create()


    @Provides
    @Singleton
    fun provideOkHttpClient() : OkHttpClient {
        val builder = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.NANOSECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)

        addDebugSettings(builder)
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRequestManager(client: OkHttpClient, gson: Gson) = RequestManager(client, gson)

    @Provides
    @Singleton
    fun provideCentrifugeFactory() = YCentrifugeFactory

    private fun addDebugSettings(builder: OkHttpClient.Builder) {
        if (BuildConfig.DEBUG) {
            try {
                val credential = Credentials.basic(BuildConfig.BASIC_AUTH_USER, BuildConfig.BASIC_AUTH_PASSWORD)
                val logging = HttpLoggingInterceptor()
                logging.level = HttpLoggingInterceptor.Level.BODY
                val certs = arrayOf<TrustManager>(object : X509TrustManager {
                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                    }

                    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                        return arrayOf()
                    }
                })

                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, certs, java.security.SecureRandom())
                val sslSocketFactory = sslContext.socketFactory

                builder
                    .authenticator(object : Authenticator {
                        override fun authenticate(route: Route?, response: Response): Request? {
                            return response.request.newBuilder().header("Authorization", credential).build()
                        }
                    })
                    .sslSocketFactory(sslSocketFactory, certs[0] as X509TrustManager)
                    .hostnameVerifier(HostnameVerifier { _, _ -> true })
                    .addInterceptor(logging)
            } catch (e: Exception) {
                throw RuntimeException("Init error!")
            }

        }
    }


}
