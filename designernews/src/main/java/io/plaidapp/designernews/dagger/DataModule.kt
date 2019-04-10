/*
 * Copyright 2018 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.plaidapp.designernews.dagger

import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import io.plaidapp.core.dagger.scope.FeatureScope
import io.plaidapp.core.data.api.DeEnvelopingConverter
import io.plaidapp.designernews.data.api.DesignerNewsService
import io.plaidapp.designernews.data.database.DesignerNewsDatabase
import io.plaidapp.designernews.data.database.LoggedInUserDao
import io.plaidapp.designernews.worker.UpvoteStoryWorkerFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Dagger module to provide data functionality for DesignerNews.
 */
@Module
class DataModule(val context: Context) {

    @Provides
    @FeatureScope
    fun provideDesignerNewsService(
        client: Lazy<OkHttpClient>,
        gson: Gson
    ): DesignerNewsService {
        return Retrofit.Builder()
            .baseUrl(DesignerNewsService.ENDPOINT)
            .callFactory { client.get().newCall(it) }
            .addConverterFactory(DeEnvelopingConverter(gson))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
            .create(DesignerNewsService::class.java)
    }

    @Provides
    @FeatureScope
    fun provideLoggedInUserDao(context: Context): LoggedInUserDao {
        return DesignerNewsDatabase.getInstance(context).loggedInUserDao()
    }


    @Provides
    @FeatureScope
    fun provideWorkManager(service: DesignerNewsService): WorkManager {
        // provide custom configuration
        val config = Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .setWorkerFactory(UpvoteStoryWorkerFactory(service))
                .build()

        //initialize WorkManager
        WorkManager.initialize(context, config)

        // TODO update to WorkManager v2.1.0 new getInstance(context)
        // to avoid possible double initialization (and the failsafe exception)
        return WorkManager.getInstance()
    }
}
