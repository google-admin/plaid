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

package io.plaidapp.core.producthunt.data.api.model

import android.os.Parcelable
import io.plaidapp.core.data.PlaidItem
import kotlinx.android.parcel.Parcelize

/**
 * Models a post on Product Hunt.
 */
@Parcelize
class Post(
    override val id: Long = 0,
    override val title: String? = null,
    override var url: String? = null,
    val name: String,
    val tagline: String,
    val discussion_url: String,
    val redirect_url: String,
    val comments_count: Int,
    val votes_count: Int
) : PlaidItem(id, title, url), Parcelable
