package com.alvaroquintana.adivinaperro.ui.recognition

import app.cash.turbine.test
import com.alvaroquintana.adivinaperro.managers.BreedClassifier
import com.alvaroquintana.domain.BreedPrediction
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecognitionViewModelTest {

    private val classifier = mockk<BreedClassifier>()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { classifier.isAvailable() } returns true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = RecognitionViewModel(classifier)

    @Test
    fun `initial state is Idle`() = runTest {
        val viewModel = createViewModel()
        assertEquals(RecognitionUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `classify transitions Idle to Loading to Results`() = runTest {
        val predictions = listOf(
            BreedPrediction("n02099601-golden_retriever", "Golden Retriever", 57L, 0.85f),
            BreedPrediction("n02099712-Labrador_retriever", "Labrador Retriever", 58L, 0.65f)
        )
        coEvery { classifier.classify(any()) } returns predictions

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(RecognitionUiState.Idle, awaitItem())

            viewModel.classify(ByteArray(10))
            assertEquals(RecognitionUiState.Loading, awaitItem())

            advanceUntilIdle()
            val result = awaitItem()
            assertTrue(result is RecognitionUiState.Results)
            assertEquals(2, (result as RecognitionUiState.Results).predictions.size)
        }
    }

    @Test
    fun `classify with all low confidence emits NotRecognized`() = runTest {
        val predictions = listOf(
            BreedPrediction("n02099601-golden_retriever", "Golden Retriever", 57L, 0.2f),
            BreedPrediction("n02099712-Labrador_retriever", "Labrador Retriever", 58L, 0.1f)
        )
        coEvery { classifier.classify(any()) } returns predictions

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(RecognitionUiState.Idle, awaitItem())

            viewModel.classify(ByteArray(10))
            assertEquals(RecognitionUiState.Loading, awaitItem())

            advanceUntilIdle()
            assertEquals(RecognitionUiState.NotRecognized, awaitItem())
        }
    }

    @Test
    fun `classify with error emits Error state`() = runTest {
        coEvery { classifier.classify(any()) } throws RuntimeException("Model load failed")

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(RecognitionUiState.Idle, awaitItem())

            viewModel.classify(ByteArray(10))
            assertEquals(RecognitionUiState.Loading, awaitItem())

            advanceUntilIdle()
            val result = awaitItem()
            assertTrue(result is RecognitionUiState.Error)
            assertEquals("Model load failed", (result as RecognitionUiState.Error).message)
        }
    }

    @Test
    fun `resetToIdle transitions back to Idle`() = runTest {
        val predictions = listOf(
            BreedPrediction("n02099601-golden_retriever", "Golden Retriever", 57L, 0.85f)
        )
        coEvery { classifier.classify(any()) } returns predictions

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(RecognitionUiState.Idle, awaitItem())

            viewModel.classify(ByteArray(10))
            assertEquals(RecognitionUiState.Loading, awaitItem())

            advanceUntilIdle()
            assertTrue(awaitItem() is RecognitionUiState.Results)

            viewModel.resetToIdle()
            assertEquals(RecognitionUiState.Idle, awaitItem())
        }
    }

    @Test
    fun `isAvailable delegates to classifier`() {
        every { classifier.isAvailable() } returns true
        assertTrue(createViewModel().isAvailable())

        every { classifier.isAvailable() } returns false
        assertEquals(false, createViewModel().isAvailable())
    }

    @Test
    fun `results include all predictions when best is above threshold`() = runTest {
        val predictions = listOf(
            BreedPrediction("label1", "Breed A", 1L, 0.8f),
            BreedPrediction("label2", "Breed B", 2L, 0.3f),
            BreedPrediction("label3", "Breed C", 3L, 0.5f)
        )
        coEvery { classifier.classify(any()) } returns predictions

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Idle
            viewModel.classify(ByteArray(10))
            awaitItem() // Loading
            advanceUntilIdle()

            val result = awaitItem() as RecognitionUiState.Results
            assertEquals(3, result.predictions.size)
        }
    }
}
