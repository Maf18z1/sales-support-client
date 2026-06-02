package com.example.productsalessupportclient.presentation.role

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.productsalessupportclient.data.network.CreateSupplierOrderRequest
import com.example.productsalessupportclient.data.network.SupplierCatalogItemResponse
import com.example.productsalessupportclient.data.network.SupplierOrderDetailResponse
import com.example.productsalessupportclient.data.network.SupplierOrderItemResponse
import com.example.productsalessupportclient.data.network.SupplierOrderSummaryResponse
import com.example.productsalessupportclient.data.network.SupplierResponse
import com.example.productsalessupportclient.data.repository.PurchaserDashboardRepository
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
class PurchaserSupplierOrderViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    private lateinit var repository: PurchaserDashboardRepository
    private val token = "test-token"

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun supplierOrdersViewModel_init_loadsSuppliersAndOrders() = runTest {
        val suppliers = listOf(
            SupplierResponse(
                id = 1,
                name = "Поставщик 1",
                phone = "+79990000000",
                email = "s1@mail.ru",
                address = "Адрес 1"
            )
        )

        val orders = listOf(
            SupplierOrderSummaryResponse(
                id = 10,
                orderNumber = "PO-001",
                orderDate = "2026-05-26",
                status = "new",
                supplierId = 1,
                supplierName = "Поставщик 1",
                itemsList = "Товар 1 x 5"
            )
        )

        coEvery { repository.loadSuppliers(token) } returns suppliers
        coEvery {
            repository.loadOrders(token, null, null, null, null)
        } returns orders

        val vm = PurchaserSupplierOrdersViewModel(repository, token)
        advanceUntilIdle()

        assertEquals(suppliers, vm.uiState.suppliers)
        assertEquals(orders, vm.uiState.orders)
        assertFalse(vm.uiState.isLoading)
        assertEquals(null, vm.uiState.error)
    }

    @Test
    fun supplierOrdersViewModel_loadSuppliers_success() = runTest {
        val suppliers = listOf(
            SupplierResponse(
                id = 1,
                name = "Поставщик 1",
                phone = null,
                email = null,
                address = null
            )
        )

        coEvery { repository.loadSuppliers(token) } returns suppliers
        coEvery {
            repository.loadOrders(token, null, null, null, null)
        } returns emptyList()

        val vm = PurchaserSupplierOrdersViewModel(repository, token)
        advanceUntilIdle()

        vm.loadSuppliers()
        advanceUntilIdle()

        assertEquals(suppliers, vm.uiState.suppliers)
        assertEquals(null, vm.uiState.error)
    }

    @Test
    fun supplierOrdersViewModel_loadOrders_success() = runTest {
        val orders = listOf(
            SupplierOrderSummaryResponse(
                id = 10,
                orderNumber = "PO-001",
                orderDate = "2026-05-26",
                status = "in_transit",
                supplierId = 1,
                supplierName = "Поставщик 1",
                itemsList = "Товар 1 x 5"
            )
        )

        coEvery { repository.loadSuppliers(token) } returns emptyList()
        coEvery {
            repository.loadOrders(token, null, null, null, null)
        } returns emptyList()
        coEvery {
            repository.loadOrders(
                token,
                "in_transit",
                null,
                "2026-05-01",
                "2026-05-31"
            )
        } returns orders

        val vm = PurchaserSupplierOrdersViewModel(repository, token)
        advanceUntilIdle()

        vm.loadOrders(
            status = "in_transit",
            supplierId = null,
            dateFrom = "2026-05-01",
            dateTo = "2026-05-31"
        )
        advanceUntilIdle()

        assertEquals(orders, vm.uiState.orders)
        assertEquals(false, vm.uiState.isLoading)
        assertEquals(null, vm.uiState.error)
    }

    @Test
    fun supplierOrdersViewModel_loadOrders_failure_setsError() = runTest {
        coEvery { repository.loadSuppliers(token) } returns emptyList()
        coEvery {
            repository.loadOrders(token, null, null, null, null)
        } throws RuntimeException("Ошибка загрузки заказов")

        val vm = PurchaserSupplierOrdersViewModel(repository, token)
        advanceUntilIdle()

        assertTrue(vm.uiState.error?.contains("Ошибка загрузки заказов") == true)
    }

    @Test
    fun supplierOrderCreateViewModel_loadSuppliers_and_loadProducts_success() = runTest {
        val suppliers = listOf(
            SupplierResponse(
                id = 1,
                name = "Поставщик 1",
                phone = null,
                email = null,
                address = null
            )
        )

        val products = listOf(
            SupplierCatalogItemResponse(
                assortmentId = 101,
                name = "Тирамису",
                category = "Десерты",
                article = "DES-001",
                price = 450.0,
                stockQuantity = 20
            )
        )

        coEvery { repository.loadSuppliers(token) } returns suppliers
        coEvery { repository.loadSupplierProducts(token, 1) } returns products

        val vm = PurchaserSupplierOrderCreateViewModel(repository, token)

        vm.loadSuppliers()
        advanceUntilIdle()
        assertEquals(suppliers, vm.uiState.suppliers)

        vm.loadProducts(1)
        advanceUntilIdle()
        assertEquals(products, vm.uiState.products)
        assertEquals(null, vm.uiState.error)
    }

    @Test
    fun supplierOrderCreateViewModel_createOrder_success_invokesCallback() = runTest {
        val request = CreateSupplierOrderRequest(
            supplierId = 1,
            items = listOf()
        )

        val createdOrder = SupplierOrderDetailResponse(
            id = 777,
            orderNumber = "PO-777",
            orderDate = "2026-05-26",
            status = "new",
            supplierId = 1,
            supplierName = "Поставщик 1",
            itemsList = null,
            items = emptyList()
        )

        coEvery { repository.createOrder(token, request) } returns createdOrder

        val vm = PurchaserSupplierOrderCreateViewModel(repository, token)

        var callbackId: Long? = null
        vm.createOrder(request) { createdId ->
            callbackId = createdId
        }
        advanceUntilIdle()

        assertEquals(777L, callbackId)
        assertFalse(vm.uiState.isLoading)
        assertEquals(null, vm.uiState.error)

        coVerify(exactly = 1) {
            repository.createOrder(token, request)
        }
    }

    @Test
    fun supplierOrderCreateViewModel_createOrder_failure_setsError() = runTest {
        val request = CreateSupplierOrderRequest(
            supplierId = 1,
            items = listOf()
        )

        coEvery { repository.createOrder(token, request) } throws RuntimeException("Не удалось создать заказ")

        val vm = PurchaserSupplierOrderCreateViewModel(repository, token)

        var callbackCalled = false
        vm.createOrder(request) {
            callbackCalled = true
        }
        advanceUntilIdle()

        assertFalse(callbackCalled)
        assertTrue(vm.uiState.error?.contains("Не удалось создать заказ") == true)
    }

    @Test
    fun supplierOrderCreateViewModel_loadProducts_failure_setsError() = runTest {
        coEvery { repository.loadSupplierProducts(token, 1) } throws RuntimeException("Ошибка товаров")

        val vm = PurchaserSupplierOrderCreateViewModel(repository, token)

        vm.loadProducts(1)
        advanceUntilIdle()

        assertTrue(vm.uiState.error?.contains("Ошибка товаров") == true)
        assertFalse(vm.uiState.isLoading)
    }

    @Test
    fun supplierOrderInfoViewModel_load_success() = runTest {
        val order = SupplierOrderDetailResponse(
            id = 100,
            orderNumber = "PO-100",
            orderDate = "2026-05-26",
            status = "received",
            supplierId = 1,
            supplierName = "Поставщик 1",
            itemsList = "Тирамису x 10",
            items = listOf(
                SupplierOrderItemResponse(
                    supplierOrderItemId = 1,
                    assortmentId = 101,
                    assortmentName = "Тирамису",
                    quantity = 10,
                    price = 450.0,
                    status = "received"
                )
            )
        )

        coEvery { repository.loadOrder(token, 100) } returns order

        val vm = PurchaserSupplierOrderInfoViewModel(repository, token)
        vm.load(100)
        advanceUntilIdle()

        assertEquals(order, vm.uiState.order)
        assertFalse(vm.uiState.isLoading)
        assertEquals(null, vm.uiState.error)
    }

    @Test
    fun supplierOrderInfoViewModel_load_failure_setsError() = runTest {
        coEvery { repository.loadOrder(token, 100) } throws RuntimeException("Не удалось загрузить заказ")

        val vm = PurchaserSupplierOrderInfoViewModel(repository, token)
        vm.load(100)
        advanceUntilIdle()

        assertTrue(vm.uiState.error?.contains("Не удалось загрузить заказ") == true)
        assertFalse(vm.uiState.isLoading)
    }
}