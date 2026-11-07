package sa.factory.sumuauctionsauctio.di

import sa.factory.sumuauctionsauctio.data.repository.SumuAuctionsRepositoryImpl
import sa.factory.sumuauctionsauctio.domain.repository.SumuAuctionsRepository
import sa.factory.sumuauctionsauctio.domain.usecase.*
import sa.factory.sumuauctionsauctio.hardware.HardwareSensorDriver
import sa.factory.sumuauctionsauctio.ui.viewmodel.SumuAuctionsViewModel

class DiContainer {
    val sensorDriver = HardwareSensorDriver()
    val repository: SumuAuctionsRepository = SumuAuctionsRepositoryImpl(sensorDriver)

    val getCatalogUseCase = GetCatalogUseCase(repository)
    val manageCartUseCase = ManageCartUseCase(repository)
    val processCheckoutUseCase = ProcessCheckoutUseCase(repository)
    val observeHardwareTelemetryUseCase = ObserveHardwareTelemetryUseCase(repository)

    fun provideViewModel(): SumuAuctionsViewModel = SumuAuctionsViewModel(
        getCatalogUseCase,
        manageCartUseCase,
        processCheckoutUseCase,
        observeHardwareTelemetryUseCase
    )
}
