package com.example.productsalessupportclient.presentation.role

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.productsalessupportclient.data.network.ClientOrderResponse
import com.example.productsalessupportclient.data.network.ClientResponse
import com.example.productsalessupportclient.data.network.CreateManagerClientOrderItemRequest
import com.example.productsalessupportclient.data.network.CreateManagerClientOrderRequest
import com.example.productsalessupportclient.data.network.ManagerAssortmentResponse
import com.example.productsalessupportclient.data.network.ManagerClientOrderDetailResponse
import com.example.productsalessupportclient.data.network.ManagerClientOrderItemResponse
import com.example.productsalessupportclient.data.network.ProductBatchResponse
import com.example.productsalessupportclient.data.repository.ClientManagerDashboardRepository
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClientManagerViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: ClientManagerDashboardRepository
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
    fun clientManagerMainViewModel_initLoadsPromotionsAndPendingOrders() = runTest {
        val promotions = listOf(
            ProductBatchResponse(
                id = 1,
                assortmentId = 101,
                assortmentName = "Тирамису",
                quantity = 10,
                expiryDate = "2026-06-01T00:00:00",
                receivedDate = "2026-05-01"
            )
        )
        val pendingOrders = listOf(
            ClientOrderResponse(
                id = 11,
                orderNumber = "ORD-11",
                orderDate = "2026-05-26",
                status = "new",
                totalAmount = 150.0,
                source = "online"
            )
        )

        coEvery { repository.loadPromotionProducts(token, 30) } returns promotions
        coEvery { repository.loadPendingOrders(token) } returns pendingOrders

        val vm = ClientManagerMainViewModel(repository, token)
        advanceUntilIdle()

        assertEquals(promotions, vm.uiState.promotionProducts)
        assertEquals(pendingOrders, vm.uiState.pendingOrders)
        assertFalse(vm.uiState.isLoading)
        assertEquals(null, vm.uiState.error)
    }

    @Test
    fun clientManagerMainViewModel_load_successUpdatesState() = runTest {
        val promotions = listOf(
            ProductBatchResponse(
                id = 2,
                assortmentId = 102,
                assortmentName = "Эклер",
                quantity = 7,
                expiryDate = "2026-06-05T00:00:00",
                receivedDate = "2026-05-10"
            )
        )
        val pendingOrders = listOf(
            ClientOrderResponse(
                id = 12,
                orderNumber = "ORD-12",
                orderDate = "2026-05-25",
                status = "new",
                totalAmount = 240.0,
                source = "manual"
            )
        )

        coEvery { repository.loadPromotionProducts(token, 14) } returns promotions
        coEvery { repository.loadPendingOrders(token) } returns pendingOrders

        val vm = ClientManagerMainViewModel(repository, token)

        vm.load(14)
        advanceUntilIdle()

        assertEquals(promotions, vm.uiState.promotionProducts)
        assertEquals(pendingOrders, vm.uiState.pendingOrders)
        assertFalse(vm.uiState.isLoading)
        assertEquals(null, vm.uiState.error)
    }

    @Test
    fun clientManagerMainViewModel_load_failureSetsError() = runTest {
        coEvery { repository.loadPromotionProducts(token, 30) } throws RuntimeException("Ошибка промо")
        coEvery { repository.loadPendingOrders(token) } returns emptyList()

        val vm = ClientManagerMainViewModel(repository, token)
        advanceUntilIdle()

        assertTrue(vm.uiState.error?.contains("Ошибка промо") == true)
        assertFalse(vm.uiState.isLoading)
    }

    @Test
    fun clientManagerOrdersViewModel_loadOrders_blankStatusUsesLoadAllOrders() = runTest {
        val orders = listOf(
            ClientOrderResponse(
                id = 21,
                orderNumber = "ORD-21",
                orderDate = "2026-05-26",
                status = "confirmed",
                totalAmount = 500.0,
                source = "online"
            )
        )

        coEvery { repository.loadAllOrders(token) } returns orders

        val vm = ClientManagerOrdersViewModel(repository, token)
        vm.loadOrders(statusFilter = null)
        advanceUntilIdle()

        assertEquals(orders, vm.uiState.orders)
        assertFalse(vm.uiState.isLoading)
        assertEquals(null, vm.uiState.error)
        coVerify(exactly = 1) { repository.loadAllOrders(token) }
    }

    @Test
    fun clientManagerOrdersViewModel_loadOrders_withStatusUsesLoadOrdersByStatus() = runTest {
        val orders = listOf(
            ClientOrderResponse(
                id = 22,
                orderNumber = "ORD-22",
                orderDate = "2026-05-26",
                status = "confirmed",
                totalAmount = 650.0,
                source = "manual"
            )
        )

        coEvery { repository.loadOrdersByStatus(token, "confirmed") } returns orders

        val vm = ClientManagerOrdersViewModel(repository, token)
        vm.loadOrders(statusFilter = "confirmed")
        advanceUntilIdle()

        assertEquals(orders, vm.uiState.orders)
        assertFalse(vm.uiState.isLoading)
        assertEquals(null, vm.uiState.error)
        coVerify(exactly = 1) { repository.loadOrdersByStatus(token, "confirmed") }
    }

    @Test
    fun clientManagerOrdersViewModel_loadOrderDetail_success() = runTest {
        val detail = ManagerClientOrderDetailResponse(
            id = 31,
            orderNumber = "ORD-31",
            orderDate = "2026-05-26",
            status = "reserved",
            totalAmount = 400.0,
            source = "online",
            items = listOf(
                ManagerClientOrderItemResponse(
                    orderItemId = 1,
                    assortmentId = 101,
                    assortmentName = "Тирамису",
                    quantity = 2,
                    price = 200.0
                )
            )
        )

        coEvery { repository.loadOrderDetail(token, 31) } returns detail

        val vm = ClientManagerOrdersViewModel(repository, token)
        vm.loadOrderDetail(31)
        advanceUntilIdle()

        assertEquals(detail, vm.uiState.orderDetail)
        assertFalse(vm.uiState.isLoading)
        assertEquals(null, vm.uiState.error)
    }

    @Test
    fun clientManagerOrdersViewModel_loadAssortment_success() = runTest {
        val assortment = listOf(
            ManagerAssortmentResponse(
                id = 101,
                name = "Тирамису",
                price = 450.0,
                category = "Десерты",
                article = "DES-001",
                stockQuantity = 12
            )
        )

        coEvery { repository.loadAssortment(token) } returns assortment

        val vm = ClientManagerOrdersViewModel(repository, token)
        vm.loadAssortment()
        advanceUntilIdle()

        assertEquals(assortment, vm.uiState.assortment)
        assertEquals(null, vm.uiState.error)
    }

    @Test
    fun clientManagerOrdersViewModel_loadClients_success() = runTest {
        val clients = listOf(
            ClientResponse(
                id = 1,
                fullName = "Иванов Иван",
                phone = "+79990000000",
                email = "ivanov@mail.ru",
                address = "Москва",
                birthDate = "1990-01-01",
                preferredContactMethod = "phone",
                notes = "VIP"
            )
        )

        coEvery { repository.loadClients(token) } returns clients

        val vm = ClientManagerOrdersViewModel(repository, token)
        vm.loadClients()
        advanceUntilIdle()

        assertEquals(clients, vm.uiState.clients)
        assertEquals(null, vm.uiState.error)
    }

    @Test
    fun clientManagerOrdersViewModel_confirmOrder_reloadsOrdersAndCallsSuccess() = runTest {
        val loadedOrders = listOf(
            ClientOrderResponse(
                id = 41,
                orderNumber = "ORD-41",
                orderDate = "2026-05-26",
                status = "confirmed",
                totalAmount = 900.0,
                source = "online"
            )
        )

        coEvery { repository.confirmOrder(token, 41) } returns Unit
        coEvery { repository.loadAllOrders(token) } returns loadedOrders

        val vm = ClientManagerOrdersViewModel(repository, token)

        var successCalled = false
        vm.confirmOrder(
            orderId = 41,
            statusFilter = null,
            onSuccess = { successCalled = true }
        )
        advanceUntilIdle()

        assertTrue(successCalled)
        assertEquals(loadedOrders, vm.uiState.orders)
        assertFalse(vm.uiState.isLoading)
        assertEquals(null, vm.uiState.error)

        coVerify(exactly = 1) { repository.confirmOrder(token, 41) }
        coVerify(exactly = 1) { repository.loadAllOrders(token) }
    }

    @Test
    fun clientManagerOrdersViewModel_createOrder_reloadsOrdersAndCallsSuccess() = runTest {
        val request = CreateManagerClientOrderRequest(
            clientId = 1,
            items = listOf(
                CreateManagerClientOrderItemRequest(
                    assortmentId = 101,
                    quantity = 2
                )
            ),
            source = "manual"
        )

        val createdId = 77L
        val refreshedOrders = listOf(
            ClientOrderResponse(
                id = 77,
                orderNumber = "ORD-77",
                orderDate = "2026-05-26",
                status = "new",
                totalAmount = 500.0,
                source = "manual"
            )
        )

        coEvery { repository.createOrder(token, request) } returns createdId
        coEvery { repository.loadAllOrders(token) } returns refreshedOrders

        val vm = ClientManagerOrdersViewModel(repository, token)

        var successCalled = false
        vm.createOrder(
            request = request,
            statusFilter = null,
            onSuccess = { successCalled = true }
        )
        advanceUntilIdle()

        assertTrue(successCalled)
        assertEquals(refreshedOrders, vm.uiState.orders)
        assertFalse(vm.uiState.isLoading)
        assertEquals(null, vm.uiState.error)

        coVerify(exactly = 1) { repository.createOrder(token, request) }
        coVerify(exactly = 1) { repository.loadAllOrders(token) }
    }

    @Test
    fun clientManagerOrdersViewModel_loadOrderDetail_failureSetsError() = runTest {
        coEvery { repository.loadOrderDetail(token, 99) } throws RuntimeException("Не найден заказ")

        val vm = ClientManagerOrdersViewModel(repository, token)
        vm.loadOrderDetail(99)
        advanceUntilIdle()

        assertNotNull(vm.uiState.error)
        assertTrue(vm.uiState.error!!.contains("Не найден заказ"))
        assertFalse(vm.uiState.isLoading)
    }
}