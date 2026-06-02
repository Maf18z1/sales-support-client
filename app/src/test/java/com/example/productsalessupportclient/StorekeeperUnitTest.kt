package com.example.productsalessupportclient.presentation.role

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.productsalessupportclient.data.network.ClientOrderResponse
import com.example.productsalessupportclient.data.network.ProductBatchResponse
import com.example.productsalessupportclient.data.network.ReturnItemResponse
import com.example.productsalessupportclient.data.network.ReturnResponse
import com.example.productsalessupportclient.data.network.StorekeeperDashboardApi
import com.example.productsalessupportclient.data.network.SupplierOrderDetailResponse
import com.example.productsalessupportclient.data.network.SupplierOrderItemResponse
import com.example.productsalessupportclient.data.network.SupplierOrderSummaryResponse
import com.example.productsalessupportclient.data.repository.StorekeeperDashboardRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class StorekeeperRoleUnitTests {

    @get:Rule
    val instantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val token = "test-token"

    @Test
    fun storekeeperMainViewModel_initLoadsAllSections() = runTest {
        val repository = mockk<StorekeeperDashboardRepository>()

        val supplierOrders = listOf(
            SupplierOrderSummaryResponse(
                id = 1,
                orderNumber = "PO-1",
                orderDate = "2026-05-26",
                status = "in_transit",
                supplierId = 10,
                supplierName = "Bakery Group",
                itemsList = "Тирамису x 20"
            )
        )
        val expiredBatches = listOf(
            ProductBatchResponse(
                id = 2,
                assortmentId = 101,
                assortmentName = "Эклер",
                quantity = 5,
                expiryDate = "2026-05-20",
                receivedDate = "2026-05-01"
            )
        )
        val readyOrders = listOf(
            ClientOrderResponse(
                id = 3,
                orderNumber = "ORD-3",
                orderDate = "2026-05-25",
                status = "confirmed",
                totalAmount = 250.0,
                source = "online"
            )
        )

        coEvery { repository.loadSupplierOrders(token) } returns supplierOrders
        coEvery { repository.loadExpiredBatches(token) } returns expiredBatches
        coEvery { repository.loadClientOrders(token, status = "confirmed") } returns readyOrders

        val vm = StorekeeperMainViewModel(repository, token)
        advanceUntilIdle()

        assertEquals(supplierOrders, vm.uiState.supplierOrders)
        assertEquals(expiredBatches, vm.uiState.writeOffBatches)
        assertEquals(readyOrders, vm.uiState.readyOrders)
        assertFalse(vm.uiState.isLoading)
        assertNull(vm.uiState.error)

        coVerify(exactly = 1) { repository.loadSupplierOrders(token) }
        coVerify(exactly = 1) { repository.loadExpiredBatches(token) }
        coVerify(exactly = 1) { repository.loadClientOrders(token, status = "confirmed") }
    }

    @Test
    fun storekeeperMainViewModel_loadFailureSetsError() = runTest {
        val repository = mockk<StorekeeperDashboardRepository>()

        coEvery { repository.loadSupplierOrders(token) } throws RuntimeException("Не удалось загрузить поставщиков")

        val vm = StorekeeperMainViewModel(repository, token)
        advanceUntilIdle()

        assertTrue(vm.uiState.error?.contains("Не удалось загрузить поставщиков") == true)
        assertFalse(vm.uiState.isLoading)
        assertEquals(emptyList<SupplierOrderSummaryResponse>(), vm.uiState.supplierOrders)
        assertEquals(emptyList<ProductBatchResponse>(), vm.uiState.writeOffBatches)
        assertEquals(emptyList<ClientOrderResponse>(), vm.uiState.readyOrders)

        coVerify(exactly = 1) { repository.loadSupplierOrders(token) }
        coVerify(exactly = 0) { repository.loadExpiredBatches(token) }
        coVerify(exactly = 0) { repository.loadClientOrders(token, status = "confirmed") }
    }

    @Test
    fun storekeeperDashboardRepository_loadSupplierOrders_sortsByDateAndId() = runTest {

        val api = mockk<StorekeeperDashboardApi>()
        val repository = StorekeeperDashboardRepository(api)

        val response = listOf(
            SupplierOrderSummaryResponse(
                id = 2,
                orderNumber = "SO-2",
                orderDate = "2024-01-01",
                status = "new",
                supplierId = 1,
                supplierName = "Supplier",
                itemsList = null
            ),
            SupplierOrderSummaryResponse(
                id = 3,
                orderNumber = "SO-3",
                orderDate = "2024-01-03",
                status = "new",
                supplierId = 1,
                supplierName = "Supplier",
                itemsList = null
            ),
            SupplierOrderSummaryResponse(
                id = 5,
                orderNumber = "SO-5",
                orderDate = "2024-01-03",
                status = "new",
                supplierId = 1,
                supplierName = "Supplier",
                itemsList = null
            )
        )

        coEvery {
            api.getSupplierOrders(
                token = token,
                status = any(),
                supplierId = any(),
                dateFrom = any(),
                dateTo = any()
            )
        } returns response

        val result = repository.loadSupplierOrders(token)

        assertEquals(listOf(5L, 3L, 2L), result.map { it.id })

        coVerify(exactly = 1) {
            api.getSupplierOrders(
                token = token,
                status = any(),
                supplierId = any(),
                dateFrom = any(),
                dateTo = any()
            )
        }
    }

    @Test
    fun storekeeperDashboardRepository_loadClientOrders_sortsByDateAndId() = runTest {
        val api = mockk<StorekeeperDashboardApi>()
        val repository = StorekeeperDashboardRepository(api)

        val unordered = listOf(
            ClientOrderResponse(
                id = 10,
                orderNumber = "ORD-10",
                orderDate = "2026-05-01",
                status = "reserved",
                totalAmount = 100.0,
                source = "online"
            ),
            ClientOrderResponse(
                id = 12,
                orderNumber = "ORD-12",
                orderDate = "2026-05-03",
                status = "reserved",
                totalAmount = 120.0,
                source = "online"
            ),
            ClientOrderResponse(
                id = 11,
                orderNumber = "ORD-11",
                orderDate = "2026-05-03",
                status = "reserved",
                totalAmount = 110.0,
                source = "manual"
            )
        )

        coEvery { api.getClientOrders(token, status = "reserved", dateFrom = null, dateTo = null) } returns unordered

        val result = repository.loadClientOrders(token, status = "reserved")

        assertEquals(listOf(12L, 11L, 10L), result.map { it.id })
        coVerify(exactly = 1) { api.getClientOrders(token, status = "reserved", dateFrom = null, dateTo = null) }
    }

    @Test
    fun storekeeperDashboardRepository_loadExpiredBatches_proxiesApiCall() = runTest {
        val api = mockk<StorekeeperDashboardApi>()
        val repository = StorekeeperDashboardRepository(api)

        val batches = listOf(
            ProductBatchResponse(
                id = 1,
                assortmentId = 100,
                assortmentName = "Эклер",
                quantity = 4,
                expiryDate = "2026-05-01",
                receivedDate = "2026-04-01"
            )
        )

        coEvery { api.getExpiredBatches(token) } returns batches

        val result = repository.loadExpiredBatches(token)

        assertEquals(batches, result)
        coVerify(exactly = 1) { api.getExpiredBatches(token) }
    }

    @Test
    fun storekeeperDashboardRepository_reserveClientOrder_proxiesApiCall() = runTest {
        val api = mockk<StorekeeperDashboardApi>()
        val repository = StorekeeperDashboardRepository(api)

        coEvery { api.reserveClientOrder(token, 21) } returns Unit

        repository.reserveClientOrder(token, 21)

        coVerify(exactly = 1) { api.reserveClientOrder(token, 21) }
    }

    @Test
    fun storekeeperDashboardRepository_shipClientOrder_proxiesApiCall() = runTest {
        val api = mockk<StorekeeperDashboardApi>()
        val repository = StorekeeperDashboardRepository(api)

        coEvery { api.shipClientOrder(token, 21) } returns Unit

        repository.shipClientOrder(token, 21)

        coVerify(exactly = 1) { api.shipClientOrder(token, 21) }
    }

    @Test
    fun storekeeperDashboardRepository_cancelClientOrder_proxiesApiCall() = runTest {
        val api = mockk<StorekeeperDashboardApi>()
        val repository = StorekeeperDashboardRepository(api)

        val response = ReturnResponse(
            id = 1,
            returnNumber = "RET-1",
            orderId = 21,
            orderNumber = "ORD-21",
            returnDate = "2026-05-26",
            reason = "Ошибка",
            items = listOf(
                ReturnItemResponse(
                    orderItemId = 5,
                    assortmentId = 100,
                    assortmentName = "Эклер",
                    quantity = 1,
                    price = 140.0
                )
            )
        )

        coEvery { api.cancelClientOrder(token, 21, "Ошибка") } returns response

        val result = repository.cancelClientOrder(token, 21, "Ошибка")

        assertEquals(response, result)
        coVerify(exactly = 1) { api.cancelClientOrder(token, 21, "Ошибка") }
    }

    @Test
    fun storekeeperDashboardRepository_deleteBatch_proxiesApiCall() = runTest {
        val api = mockk<StorekeeperDashboardApi>()
        val repository = StorekeeperDashboardRepository(api)

        val response = mapOf("success" to true)

        coEvery { api.deleteBatch(token, 44) } returns response

        val result = repository.deleteBatch(token, 44)

        assertEquals(response, result)
        coVerify(exactly = 1) { api.deleteBatch(token, 44) }
    }

    @Test
    fun storekeeperSupplierOrdersViewModel_loadSuccess() = runTest {
        val repository = mockk<StorekeeperDashboardRepository>()

        val orders = listOf(
            SupplierOrderSummaryResponse(
                id = 7,
                orderNumber = "PO-7",
                orderDate = "2026-05-26",
                status = "in_transit",
                supplierId = 3,
                supplierName = "Fresh Line",
                itemsList = "Хлеб x 60"
            )
        )

        coEvery {
            repository.loadSupplierOrders(
                token = token,
                status = "in_transit",
                dateFrom = "2026-05-01",
                dateTo = "2026-05-31"
            )
        } returns orders

        val vm = StorekeeperSupplierOrdersViewModel(repository, token)
        vm.load(status = "in_transit", dateFrom = "2026-05-01", dateTo = "2026-05-31")
        advanceUntilIdle()

        assertEquals(orders, vm.uiState.orders)
        assertFalse(vm.uiState.isLoading)
        assertNull(vm.uiState.error)

        coVerify(exactly = 1) {
            repository.loadSupplierOrders(
                token = token,
                status = "in_transit",
                dateFrom = "2026-05-01",
                dateTo = "2026-05-31"
            )
        }
    }

    @Test
    fun storekeeperSupplierOrdersViewModel_receiveOrderRefreshesList() = runTest {
        val repository = mockk<StorekeeperDashboardRepository>()

        val refreshedOrders = listOf(
            SupplierOrderSummaryResponse(
                id = 7,
                orderNumber = "PO-7",
                orderDate = "2026-05-26",
                status = "received",
                supplierId = 3,
                supplierName = "Fresh Line",
                itemsList = "Хлеб x 60"
            )
        )

        val detail = SupplierOrderDetailResponse(
            id = 7,
            orderNumber = "PO-7",
            orderDate = "2026-05-26",
            status = "received",
            supplierId = 3,
            supplierName = "Fresh Line",
            itemsList = "Хлеб x 60",
            items = listOf(
                SupplierOrderItemResponse(
                    supplierOrderItemId = 1,
                    assortmentId = 10,
                    assortmentName = "Хлеб цельнозерновой",
                    quantity = 60,
                    price = 48.0,
                    status = "received"
                )
            )
        )

        coEvery { repository.receiveSupplierOrder(token, 7) } returns detail
        coEvery { repository.loadSupplierOrders(token, status = "in_transit", dateFrom = null, dateTo = null) } returns refreshedOrders

        val vm = StorekeeperSupplierOrdersViewModel(repository, token)
        vm.receiveOrder(orderId = 7, status = "in_transit")
        advanceUntilIdle()

        assertEquals(refreshedOrders, vm.uiState.orders)
        assertFalse(vm.uiState.isLoading)
        assertNull(vm.uiState.error)

        coVerify(exactly = 1) { repository.receiveSupplierOrder(token, 7) }
        coVerify(exactly = 1) { repository.loadSupplierOrders(token, status = "in_transit", dateFrom = null, dateTo = null) }
    }

    @Test
    fun storekeeperSupplierOrdersViewModel_receiveOrderFailureSetsError() = runTest {
        val repository = mockk<StorekeeperDashboardRepository>()

        coEvery { repository.receiveSupplierOrder(token, 7) } throws RuntimeException("Only in_transit supplier orders can be received")

        val vm = StorekeeperSupplierOrdersViewModel(repository, token)
        vm.receiveOrder(orderId = 7, status = "in_transit")
        advanceUntilIdle()

        assertTrue(vm.uiState.error?.contains("Only in_transit supplier orders can be received") == true)
        assertFalse(vm.uiState.isLoading)

        coVerify(exactly = 1) { repository.receiveSupplierOrder(token, 7) }
    }

    @Test
    fun storekeeperReturnsFilteringRule_keepsOnlyReservedAndShippedOrders() {
        val orders = listOf(
            ClientOrderResponse(1, "ORD-1", "2026-05-26", "new", 100.0, "online"),
            ClientOrderResponse(2, "ORD-2", "2026-05-26", "reserved", 200.0, "online"),
            ClientOrderResponse(3, "ORD-3", "2026-05-26", "shipped", 300.0, "manual"),
            ClientOrderResponse(4, "ORD-4", "2026-05-26", "cancelled", 0.0, "manual")
        )

        val filtered = orders.filter { it.status == "shipped" || it.status == "reserved" }

        assertEquals(listOf(2L, 3L), filtered.map { it.id })
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}