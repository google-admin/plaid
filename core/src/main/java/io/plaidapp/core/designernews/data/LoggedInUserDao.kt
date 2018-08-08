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

package io.plaidapp.core.designernews.data

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import io.plaidapp.core.designernews.data.users.model.LoggedInUser

/**
 * This Data Access Object handles Room database operations for the [LoggedInUser] class.
 */
@Dao
abstract class LoggedInUserDao {
    @Query("SELECT * FROM logged_in_user LIMIT 1")
    abstract fun getLoggedInUser(): LiveData<LoggedInUser>

    /**
     * Sets the [LoggedInUser]. This method guarantees that only one
     * LoggedInUser is ever in the table by first deleting all table
     * data before inserting the LoggedInUser.
     *
     * This method should be used instead of [insertLoggedInUser].
     */
    @Transaction
    open fun setLoggedInUser(loggedInUser: LoggedInUser) {
        deleteLoggedInUser()
        insertLoggedInUser(loggedInUser)
    }

    @Query("DELETE FROM logged_in_user")
    abstract fun deleteLoggedInUser()

    /**
     * This method should not be used.  Instead, use [setLoggedInUser],
     * as that method guarantees only a single [LoggedInUser] will reside
     * in the table.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertLoggedInUser(loggedInUser: LoggedInUser)
}
