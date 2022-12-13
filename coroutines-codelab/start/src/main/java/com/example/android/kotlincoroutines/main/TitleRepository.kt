/*
 * Copyright (C) 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.kotlincoroutines.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.android.kotlincoroutines.util.BACKGROUND
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * TitleRepository provides an interface to fetch a title or request a new one be generated.
 *
 * Repository modules handle data operations. They provide a clean API so that the rest of the app
 * can retrieve this data easily. They know where to get the data from and what API calls to make
 * when data is updated. You can consider repositories to be mediators between different data
 * sources, in our case it mediates between a network API and an offline database cache.
 */
class TitleRepository(val network: MainNetwork, val titleDao: TitleDao) {

    /**
     * [LiveData] to load title.
     *
     * This is the main interface for loading a title. The title will be loaded from the offline
     * cache.
     *
     * Observing this will not cause the title to be refreshed, use [TitleRepository.refreshTitleWithCallbacks]
     * to refresh the title.
     */
    val title: LiveData<String?> = titleDao.titleLiveData.map { it?.title }

//    Both Room and Retrofit make suspending functions main-safe.
//    It's safe to call these suspend funs from Dispatchers.Main, even though they fetch from the network and write to the database.

    //By default, Kotlin coroutines provides three Dispatchers: Main, IO, and Default.
    //The IO dispatcher is optimized for IO work like reading from the network or disk, while the Default dispatcher is optimized for CPU intensive tasks.
    suspend fun refreshTitle() {
        //withContext returns its result back to the Dispatcher that called it, in this case Dispatchers.Main. The callback version called the callbacks on a thread in the BACKGROUND executor service.
//        withContext(Dispatchers.IO) {
//            val result = try {
//                // Make network request using a blocking call
//                network.fetchNextTitle().execute()
//            } catch (cause: Throwable) {
//                // If the network throws an exception, inform the caller
//                throw TitleRefreshError("Unable to refresh title", cause)
//            }
//
//            if (result.isSuccessful) {
//                // Save it to database
//                titleDao.insertTitle(Title(result.body()!!))
//            } else {
//                // If it's not successful, inform the callback of the error
//                throw TitleRefreshError("Unable to refresh title", null)
//            }
//        }
        try {
            // Make network request using a blocking call
            val result = network.fetchNextTitle()
            titleDao.insertTitle(Title(result))
        } catch (cause: Throwable) {
            // If anything throws an exception, inform the caller
            throw TitleRefreshError("Unable to refresh title", cause)
        }
    }
}

/**
 * Thrown when there was a error fetching a new title
 *
 * @property message user ready error message
 * @property cause the original cause of this exception
 */
class TitleRefreshError(message: String, cause: Throwable?) : Throwable(message, cause)

interface TitleRefreshCallback {
    fun onCompleted()
    fun onError(cause: Throwable)
}
