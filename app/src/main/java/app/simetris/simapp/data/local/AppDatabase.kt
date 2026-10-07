package app.simetris.simapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ChecklistModelo::class, ChecklistModeloItem::class, Veiculo::class, Parceiro::class, CursorCatalogo::class, Checklist::class, ChecklistItem::class, Foto::class, Assinatura::class, PendenciaEnvio::class, ServidorFakeChecklist::class, ServidorFakeAnexo::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun checklistDao(): ChecklistDao
    abstract fun checklistModeloDao(): ChecklistModeloDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun driverDao(): DriverDao
    abstract fun catalogCursorDao(): CatalogCursorDao
    abstract fun photoDao(): PhotoDao
    abstract fun signatureDao(): SignatureDao
    abstract fun pendingUploadDao(): PendingUploadDao
    abstract fun mediaPathDao(): MediaPathDao
    abstract fun fakeServerDao(): FakeServerDao
}
