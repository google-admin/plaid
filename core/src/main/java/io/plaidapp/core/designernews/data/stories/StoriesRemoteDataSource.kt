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

package io.plaidapp.core.designernews.data.stories

import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.api.DesignerNewsSearchService
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.data.stories.model.StoryResponse
import retrofit2.Response
import java.io.IOException

/**
 * Data source class that handles work with Designer News API.
 */
class StoriesRemoteDataSource(
    private val service: DesignerNewsService,
    private val searchService: DesignerNewsSearchService
) {

    suspend fun loadStories(page: Int): Result<List<StoryResponse>> {
        return try {
            val response = service.getStories(page).await()
            getResult(response = response, onError = {
                Result.Error(
                    IOException("Error getting stories ${response.code()} ${response.message()}")
                )
            })
        } catch (e: Exception) {
            Result.Error(IOException("Error getting stories", e))
        }
    }

    suspend fun search(query: String, page: Int): Result<List<StoryResponse>> {
        // TODO: replace with DesignerNewsSearchService
        return try {
            val response = service.search(query, page).await()
            getResult(response = response, onError = {
                Result.Error(
                    IOException("Error searching $query ${response.code()} ${response.message()}")
                )
            })
        } catch (e: Exception) {
            Result.Error(IOException("Error searching $query", e))
        }
    }

    private inline fun getResult(
        response: Response<List<StoryResponse>>,
        onError: () -> Result.Error
    ): Result<List<StoryResponse>> {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                return Result.Success(body)
            }
        }
        return onError.invoke()
    }

    companion object {
        @Volatile
        private var INSTANCE: StoriesRemoteDataSource? = null

        fun getInstance(
            service: DesignerNewsService,
            searchService: DesignerNewsSearchService
        ): StoriesRemoteDataSource {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StoriesRemoteDataSource(service, searchService).also { INSTANCE = it }
            }
        }
    }
}
