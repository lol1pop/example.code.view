package io.meorg.code.view.configuration

import com.switips.us.algopix.AlgopixWebProductApi
import com.switips.us.genius_connector.GeniusProductAPI
import com.switips.us.genius_connector.GeniusReviewAPI
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class AffiliateConfiguration {

    private fun OkHttpClient.Builder.withExtendTimeouts() =
        callTimeout(3, TimeUnit.MINUTES)
            .connectTimeout(3, TimeUnit.MINUTES)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

    private fun OkHttpClient.withExtendTimeouts() =
        newBuilder().withExtendTimeouts()

    private fun withExtendTimeouts() =
        OkHttpClient.Builder().withExtendTimeouts()

    @Bean
    fun geniusReviewAPI(): GeniusReviewAPI = GeniusReviewAPI.provider()

    @Bean
    fun geniusProductApi(): GeniusProductAPI = GeniusProductAPI.provider(client = withExtendTimeouts())

    @Bean
    fun algopixWebProductApi(client: OkHttpClient): AlgopixWebProductApi =
        AlgopixWebProductApi.provider(client = client)

}
