package sa.factory.sumuauctionsauctio.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import sa.factory.sumuauctionsauctio.domain.usecase.*
import sa.factory.sumuauctionsauctio.model.SumuAuctionsItem
import sa.factory.sumuauctionsauctio.model.SumuAuctionsOrder
import sa.factory.sumuauctionsauctio.model.SumuAuctionsTelemetry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SumuAuctionsUiState(
    val catalog: List<SumuAuctionsItem> = emptyList(),
    val filteredCatalog: List<SumuAuctionsItem> = emptyList(),
    val selectedCategory: String = "الكل",
    val cart: List<SumuAuctionsItem> = emptyList(),
    val telemetry: SumuAuctionsTelemetry? = null,
    val lastOrder: SumuAuctionsOrder? = null,
    val isCheckingOut: Boolean = false,
    val activeTab: String = "home"
)

class SumuAuctionsViewModel(
    private val getCatalogUseCase: GetCatalogUseCase,
    private val manageCartUseCase: ManageCartUseCase,
    private val processCheckoutUseCase: ProcessCheckoutUseCase,
    private val observeHardwareTelemetryUseCase: ObserveHardwareTelemetryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SumuAuctionsUiState())
    val uiState: StateFlow<SumuAuctionsUiState> = _uiState.asStateFlow()

    init {
        // Observe Catalog
        getCatalogUseCase().onEach { list ->
            _uiState.update { it.copy(catalog = list, filteredCatalog = list) }
        }.launchIn(viewModelScope)

        // Observe Cart
        manageCartUseCase.cart.onEach { cartItems ->
            _uiState.update { it.copy(cart = cartItems) }
        }.launchIn(viewModelScope)

        // Observe Telemetry
        observeHardwareTelemetryUseCase().onEach { telem ->
            _uiState.update { it.copy(telemetry = telem) }
        }.launchIn(viewModelScope)
    }

    fun selectCategory(category: String) {
        _uiState.update { state ->
            val filtered = if (category == "الكل") state.catalog else state.catalog.filter { it.category == category }
            state.copy(selectedCategory = category, filteredCatalog = filtered)
        }
    }

    fun switchTab(tab: String) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun addToCart(item: SumuAuctionsItem) = manageCartUseCase.add(item)
    fun removeFromCart(item: SumuAuctionsItem) = manageCartUseCase.remove(item)

    fun checkout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingOut = true) }
            val order = processCheckoutUseCase()
            _uiState.update { it.copy(isCheckingOut = false, lastOrder = order, activeTab = "checkout") }
        }
    }
}
