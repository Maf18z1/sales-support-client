package com.example.productsalessupportclient.presentation.role

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.productsalessupportclient.data.network.CategoryHistoryPointResponse
import com.example.productsalessupportclient.data.network.StockBatchResponse
import com.example.productsalessupportclient.data.network.StockDetailResponse
import com.example.productsalessupportclient.data.network.StockOverviewResponse
import com.example.productsalessupportclient.data.network.StockDetailUpsertRequest
import com.example.productsalessupportclient.data.repository.PurchaserStockRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PurchaserStockViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    private lateinit var repository: PurchaserStockRepository
    private lateinit var stockViewModel: PurchaserStockViewModel
    private lateinit var editViewModel: PurchaserStockEditViewModel
    private lateinit var analyticsViewModel: PurchaserStockAnalyticsViewModel

    private val token = "test-token"

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repository = mockk()
        stockViewModel = PurchaserStockViewModel(repository, token)
        editViewModel = PurchaserStockEditViewModel(repository, token)
        analyticsViewModel = PurchaserStockAnalyticsViewModel(repository, token)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun load_overview_populatesItems() = runTest {
        val items = listOf(
            StockOverviewResponse(
                assortmentId = 1,
                name = "Товар 1",
                category = "Выпечка",
                article = "A1",
                price = 120.0,
                stockQuantity = 10,
                lastUpdated = "2026-05-26T10:00:00",
                nearestExpiryDate = "2026-06-01",
                nearestExpiryDays = 6
            )
        )

        coEvery {
            repository.loadOverview(token, null, null, null, null)
        } returns items

        stockViewModel.load(null, null, null, null)
        advanceUntilIdle()

        assertFalse(stockViewModel.uiState.isLoading)
        assertEquals(items, stockViewModel.uiState.items)
        assertEquals(null, stockViewModel.uiState.error)
    }

    @Test
    fun load_overview_setsError_whenRepositoryFails() = runTest {
        coEvery {
            repository.loadOverview(token, null, null, null, null)
        } throws RuntimeException("Ошибка загрузки")

        stockViewModel.load(null, null, null, null)
        advanceUntilIdle()

        assertFalse(stockViewModel.uiState.isLoading)
        assertTrue(stockViewModel.uiState.error?.contains("Ошибка загрузки") == true)
    }

    @Test
    fun editScreen_load_populatesDetailAndBatches() = runTest {
        val detail = StockDetailResponse(
            assortmentId = 1,
            name = "Тирамису",
            category = "Десерты",
            article = "DES-1",
            price = 450.0,
            stockQuantity = 15,
            lastUpdated = "2026-05-26T10:00:00",
            batches = emptyList()
        )

        val batches = listOf(
            StockBatchResponse(
                batchId = 11,
                assortmentId = 1,
                quantity = 7,
                expiryDate = "2026-06-10",
                receivedDate = "2026-05-20"
            )
        )

        coEvery { repository.loadDetail(token, 1) } returns detail
        coEvery { repository.loadBatches(token, 1) } returns batches

        editViewModel.load(1)
        advanceUntilIdle()

        assertFalse(editViewModel.uiState.isLoading)
        assertEquals(detail, editViewModel.uiState.detail)
        assertEquals(batches, editViewModel.uiState.batches)
        assertEquals(null, editViewModel.uiState.error)
    }

    @Test
    fun editScreen_saveDetail_callsRepositoryAndInvokesCallback() = runTest {
        val request = StockDetailUpsertRequest(
            name = "Тирамису",
            category = "Десерты",
            article = "DES-1",
            price = 460.0,
            stockQuantity = 20
        )

        coEvery {
            repository.updateDetail(token, 1, request)
        } returns StockDetailResponse(
            assortmentId = 1,
            name = "Тирамису",
            category = "Десерты",
            article = "DES-1",
            price = 460.0,
            stockQuantity = 20,
            lastUpdated = "2026-05-26T10:00:00",
            batches = emptyList()
        )

        var savedCalled = false

        editViewModel.saveDetail(
            assortmentId = 1,
            request = request,
            onSaved = { savedCalled = true }
        )

        advanceUntilIdle()

        assertTrue(savedCalled)
        coVerify(exactly = 1) {
            repository.updateDetail(token, 1, request)
        }
    }

    @Test
    fun editScreen_deleteBatch_callsRepositoryAndReloadsData() = runTest {
        val detail = StockDetailResponse(
            assortmentId = 1,
            name = "Тирамису",
            category = "Десерты",
            article = "DES-1",
            price = 450.0,
            stockQuantity = 15,
            lastUpdated = "2026-05-26T10:00:00",
            batches = emptyList()
        )

        val batches = listOf(
            StockBatchResponse(
                batchId = 11,
                assortmentId = 1,
                quantity = 7,
                expiryDate = "2026-06-10",
                receivedDate = "2026-05-20"
            )
        )

        coEvery { repository.loadDetail(token, 1) } returns detail
        coEvery { repository.loadBatches(token, 1) } returns batches
        coEvery {
            repository.deleteBatch(token, 11)
        } returns mapOf("message" to "Batch deleted")

        editViewModel.load(1)
        advanceUntilIdle()

        editViewModel.deleteBatch(batchId = 11, assortmentId = 1)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.deleteBatch(token, 11)
        }
        coVerify(atLeast = 2) {
            repository.loadDetail(token, 1)
            repository.loadBatches(token, 1)
        }
    }

    @Test
    fun analytics_loadCategories_setsCategories() = runTest {
        val categories = listOf("Десерты", "Выпечка", "Хлеб")

        coEvery { repository.loadCategories(token) } returns categories

        analyticsViewModel.loadCategories()
        advanceUntilIdle()

        assertEquals(categories, analyticsViewModel.uiState.categories)
        assertEquals(null, analyticsViewModel.uiState.error)
    }

    @Test
    fun analytics_loadHistory_setsHistory() = runTest {
        val history = listOf(
            CategoryHistoryPointResponse(date = "2026-05-20", quantity = 5),
            CategoryHistoryPointResponse(date = "2026-05-21", quantity = 8)
        )

        coEvery { repository.loadCategoryHistory(token, "Десерты") } returns history

        analyticsViewModel.loadHistory("Десерты")
        advanceUntilIdle()

        assertFalse(analyticsViewModel.uiState.isLoading)
        assertEquals(history, analyticsViewModel.uiState.history)
        assertEquals(null, analyticsViewModel.uiState.error)
    }
}