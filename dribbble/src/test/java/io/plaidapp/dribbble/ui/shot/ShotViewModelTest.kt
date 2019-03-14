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

package io.plaidapp.dribbble.ui.shot

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.plaidapp.core.data.Result
import io.plaidapp.core.dribbble.data.ShotsRepository
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.util.event.Event
import io.plaidapp.dribbble.domain.CreateShotUiModelUseCase
import io.plaidapp.dribbble.domain.GetShareShotInfoUseCase
import io.plaidapp.dribbble.domain.ShareShotInfo
import io.plaidapp.dribbble.testShot
import io.plaidapp.dribbble.testShotUiModel
import io.plaidapp.test.shared.LiveDataTestUtil
import io.plaidapp.test.shared.provideFakeCoroutinesContextProvider
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.coroutines.CoroutineContext

/**
 * Tests for [ShotViewModel], mocking out its dependencies.
 */
class ShotViewModelTest {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val shotId = 1337L
    private val repo: ShotsRepository = mock()
    private val getShareShotInfoUseCase: GetShareShotInfoUseCase = mock()
    private val createShotUiModel: CreateShotUiModelUseCase = mock {
        on { runBlocking { invoke(any()) } } doReturn testShotUiModel
    }

    @Test
    fun loadShot_existsInRepo() {
        // Given that the repo successfully returns the requested shot
        // When view model is constructed
        val viewModel = withViewModel()

        // Then a shotUiModel is present
        val result: ShotUiModel? = LiveDataTestUtil.getValue(viewModel.shotUiModel)
        assertNotNull(result)
    }

    @Test
    @ObsoleteCoroutinesApi
    fun loadShot_emitsTwoUiModels() {
        val testContext = TestCoroutineContext()

        // Here we create the VM that should post 2 objects to LiveData
        val viewModel = withViewModel(context = testContext)

        // Checking the fast result has been emitted
        val fastResult: ShotUiModel? = LiveDataTestUtil.getValue(viewModel.shotUiModel)
        assertNotNull(fastResult)
        assertTrue(fastResult!!.formattedDescription.isEmpty())

        // Triggering the launch in the method
        testContext.triggerActions()

        // Checking the slow result has been emitted
        val slowResult: ShotUiModel? = LiveDataTestUtil.getValue(viewModel.shotUiModel)
        assertNotNull(slowResult)
        assertTrue(slowResult!!.formattedDescription.isNotEmpty())
    }

    @Test(expected = IllegalStateException::class)
    fun loadShot_notInRepo() {
        // Given that the repo fails to return the requested shot
        whenever(repo.getShot(shotId)).thenReturn(Result.Error(Exception()))

        // When the view model is constructed
        ShotViewModel(
            shotId,
            repo,
            createShotUiModel,
            getShareShotInfoUseCase,
            provideFakeCoroutinesContextProvider()
        )
        // Then it throws
    }

    @Test
    fun shotClicked_sendsOpenLinkEvent() = runBlocking {
        // Given a view model with a shot with a known URL
        val url = "https://dribbble.com/shots/2344334-Plaid-Product-Icon"
        val mockShotUiModel = mock<ShotUiModel> { on { this.url } doReturn url }
        runBlocking { whenever(createShotUiModel.invoke(any())).thenReturn(mockShotUiModel) }
        val viewModel = withViewModel(shot = testShot.copy(htmlUrl = url))

        // When there is a request to view the shot
        viewModel.viewShotRequested()

        // Then an event is emitted to open the given url
        val openLinkEvent: Event<String>? = LiveDataTestUtil.getValue(viewModel.openLink)
        assertNotNull(openLinkEvent)
        assertEquals(url, openLinkEvent!!.peek())
    }

    @Test
    fun shotShareClicked_sendsShareInfoEvent() {
        // Given a VM with a mocked use case which return a known Share Info object
        val expected = ShareShotInfo(mock(), "Title", "Share Text", "Mime")
        val viewModel = withViewModel(shareInfo = expected)

        // When there is a request to share the shot
        viewModel.shareShotRequested()

        // Then an event is raised with the expected info
        val shareInfoEvent: Event<ShareShotInfo>? = LiveDataTestUtil.getValue(viewModel.shareShot)
        assertNotNull(shareInfoEvent)
        assertEquals(expected, shareInfoEvent!!.peek())
    }

    @Test
    fun getAssistWebUrl_returnsShotUrl() {
        // Given a view model with a shot with a known URL
        val url = "https://dribbble.com/shots/2344334-Plaid-Product-Icon"
        val mockShotUiModel = mock<ShotUiModel> { on { this.url } doReturn url }
        runBlocking { whenever(createShotUiModel.invoke(any())).thenReturn(mockShotUiModel) }
        val viewModel = withViewModel(shot = testShot.copy(htmlUrl = url))

        // When there is a request to share the shot
        val assistWebUrl = viewModel.getAssistWebUrl()

        // Then the expected URL is returned
        assertEquals(url, assistWebUrl)
    }

    @Test
    fun getShotId_returnsId() {
        // Given a view model with a shot with a known ID
        val id = 1234L
        val mockShotUiModel = mock<ShotUiModel> { on { this.id } doReturn id }
        runBlocking { whenever(createShotUiModel.invoke(any())).thenReturn(mockShotUiModel) }
        val viewModel = withViewModel(shot = testShot.copy(id = id))

        // When there is a request to share the shot
        val shotId = viewModel.getShotId()

        // Then the expected ID is returned
        assertEquals(id, shotId)
    }

    @ExperimentalCoroutinesApi
    private fun withViewModel(
        shot: Shot = testShot,
        shareInfo: ShareShotInfo? = null,
        context: CoroutineContext = Unconfined
    ): ShotViewModel {
        whenever(repo.getShot(shotId)).thenReturn(Result.Success(shot))
        if (shareInfo != null) {
            runBlocking {
              whenever(getShareShotInfoUseCase(any())).thenReturn(shareInfo)
            }
        }
        return ShotViewModel(
            shotId,
            repo,
            createShotUiModel,
            getShareShotInfoUseCase,
            provideFakeCoroutinesContextProvider(context)
        )
      }
}
