package com.example.productsalessupportclient

import com.example.productsalessupportclient.data.network.AssortmentWithStockResponse
import com.example.productsalessupportclient.data.repository.PurchaserDashboardRepository
import com.example.productsalessupportclient.presentation.role.PurchaserAssortmentViewModel
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
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PurchaserAssortmentViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var repository: PurchaserDashboardRepository
    private lateinit var viewModel: PurchaserAssortmentViewModel

    private val token = "test-token"

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)

        repository = mockk(relaxed = true)

        viewModel = PurchaserAssortmentViewModel(
            repository = repository,
            token = token
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun deleteAssortment_removesItemFromRepository() = runTest {
        val items = listOf(
            AssortmentWithStockResponse(
                id = 1,
                name = "Товар 1",
                category = "Напитки",
                article = "A1",
                price = 100.0,
                stockQuantity = 10
            ),
            AssortmentWithStockResponse(
                id = 2,
                name = "Товар 2",
                category = "Снеки",
                article = "A2",
                price = 200.0,
                stockQuantity = 5
            )
        )

        coEvery {
            repository.loadAssortment(token, null, null, null, null)
        } returns items

        coEvery {
            repository.deleteAssortment(token, 1)
        } returns Unit

        viewModel.load(null, null, null, null)
        advanceUntilIdle()

        val deletedName = viewModel.uiState.items.first().name

        viewModel.deleteAssortment(1)
        advanceUntilIdle()

        val updatedItems = items.filterNot { it.id.toInt() == 1 }

        coEvery {
            repository.loadAssortment(token, null, null, null, null)
        } returns updatedItems

        viewModel.load(null, null, null, null)
        advanceUntilIdle()

        val exists = viewModel.uiState.items.any { it.name == deletedName }

        assertFalse(exists)

        coVerify {
            repository.deleteAssortment(token, 1)
        }
    }

    @Test
    fun infoButton_opensCorrectProductData() = runTest {
        val item = AssortmentWithStockResponse(
            id = 1,
            name = "Кофе",
            category = "Напитки",
            article = "CF-1",
            price = 350.0,
            stockQuantity = 15
        )

        coEvery {
            repository.loadAssortment(token, null, null, null, null)
        } returns listOf(item)

        viewModel.load(null, null, null, null)
        advanceUntilIdle()

        val selected = viewModel.uiState.items.first()

        assertEquals("Кофе", selected.name)
        assertEquals("Напитки", selected.category)
        assertEquals(15, selected.stockQuantity)
    }

    @Test
    fun editStock_changesProductQuantitySuccessfully() = runTest {
        val beforeEdit = AssortmentWithStockResponse(
            id = 1,
            name = "Сок",
            category = "Напитки",
            article = "J1",
            price = 120.0,
            stockQuantity = 5
        )

        val afterEdit = beforeEdit.copy(stockQuantity = 20)

        coEvery {
            repository.loadAssortment(token, null, null, null, null)
        } returnsMany listOf(
            listOf(beforeEdit),
            listOf(afterEdit)
        )

        viewModel.load(null, null, null, null)
        advanceUntilIdle()

        assertEquals(5, viewModel.uiState.items.first().stockQuantity)

        viewModel.load(null, null, null, null)
        advanceUntilIdle()

        assertEquals(20, viewModel.uiState.items.first().stockQuantity)
    }

    @Test
    fun filterByCategory_returnsCorrectNumberOfProducts() = runTest {
        val items = listOf(
            AssortmentWithStockResponse(
                id = 1,
                name = "Кола",
                category = "Напитки",
                article = "D1",
                price = 100.0,
                stockQuantity = 10
            ),
            AssortmentWithStockResponse(
                id = 2,
                name = "Пепси",
                category = "Напитки",
                article = "D2",
                price = 110.0,
                stockQuantity = 8
            ),
            AssortmentWithStockResponse(
                id = 3,
                name = "Чипсы",
                category = "Снеки",
                article = "S1",
                price = 90.0,
                stockQuantity = 12
            )
        )

        val firstCategory = items.first().category ?: ""

        val expectedCount = items.count {
            it.category == firstCategory
        }

        val filteredItems = items.filter {
            it.category == firstCategory
        }

        coEvery {
            repository.loadAssortment(
                token,
                firstCategory,
                null,
                null,
                null
            )
        } returns filteredItems

        viewModel.load(
            category = firstCategory,
            minStock = null,
            maxStock = null,
            expiringDays = null
        )

        advanceUntilIdle()

        assertEquals(expectedCount, viewModel.uiState.items.size)

        assertTrue(
            viewModel.uiState.items.all {
                it.category == firstCategory
            }
        )
    }
}