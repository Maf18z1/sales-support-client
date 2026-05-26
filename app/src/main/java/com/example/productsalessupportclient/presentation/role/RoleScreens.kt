package com.example.productsalessupportclient.presentation.role

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.productsalessupportclient.data.repository.AuthSession

@Composable
fun PurchaserHomeScreen(
    session: AuthSession,
    onLogout: () -> Unit
) {
    RoleShell(
        session = session,
        onLogout = onLogout,
        menuItems = listOf(
            RoleMenuItem("Главная", "purchaser_home") { PurchaserMainScreen(session = session, onLogout = onLogout) },
            RoleMenuItem("Управление ассортиментом", "purchaser_assortment") { PurchaserAssortmentScreen(session = session, onLogout = onLogout) },
            RoleMenuItem("Формирование заказов", "purchaser_orders") { PurchaserSupplierOrdersScreen(session = session, onLogout = onLogout) },
            RoleMenuItem("Отслеживание остатков", "purchaser_stock") { PurchaserStockScreen(session = session, onLogout = onLogout) }
        )
    )
}

@Composable
fun ManagerHomeScreen(
    session: AuthSession,
    onLogout: () -> Unit
) {
    RoleShell(
        session = session,
        onLogout = onLogout,
        menuItems = listOf(
            RoleMenuItem("Главная", "manager_home") { ClientManagerMainScreen(session = session, onLogout = onLogout) },
            RoleMenuItem("Обработка заказов клиентов", "manager_orders") { ClientManagerOrdersScreen(session = session, onLogout = onLogout) }
        )
    )
}

@Composable
fun StorekeeperHomeScreen(
    session: AuthSession,
    onLogout: () -> Unit
) {
    RoleShell(
        session = session,
        onLogout = onLogout,
        menuItems = listOf(
            RoleMenuItem("Главная", "storekeeper_home") {
                StorekeeperMainScreen(session = session, onLogout = onLogout)
            },
            RoleMenuItem("Ожидаемые поступления", "storekeeper_supplier_orders") {
                StorekeeperSupplierOrdersScreen(session = session, onLogout = onLogout)
            },
            RoleMenuItem("Заказы к отгрузке", "storekeeper_client_orders") {
                StorekeeperClientOrdersScreen(session = session, onLogout = onLogout)
            },
            RoleMenuItem("Возвраты и списания", "storekeeper_returns") {
                StorekeeperReturnsScreen(session = session, onLogout = onLogout)
            }
        )
    )
}