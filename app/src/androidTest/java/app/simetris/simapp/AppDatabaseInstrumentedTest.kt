package app.simetris.simapp

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.simetris.simapp.data.local.*
import app.simetris.simapp.data.remote.*
import app.simetris.simapp.data.repository.CatalogRepository
import app.simetris.simapp.media.PrivateFiles
import app.simetris.simapp.sync.ChecklistSynchronizer
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseInstrumentedTest {
    private lateinit var database: AppDatabase
    @Before fun setUp() {
        database = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java).build()
    }
    @After fun tearDown() { database.close() }

    @Test fun catalogReplacementIsAtomicAndScopedToCompanyAndUser() = runBlocking {
        val identity = LocalIdentity("1", "usuario-7", 7)
        val other = LocalIdentity("1", "usuario-8", 8)
        val fake = DemoChecklistApi(database)
        var current = Registrations(listOf(RemoteDriver(1, "Ana")),
            listOf(RemoteVehicle(1, "AAA1111")),
            listOf(RemoteTemplate(1, "Original", "Descrição",
                listOf(RemoteTemplateItem(10, 1, "Item", "Descrição")), true, true, 1, 3)))
        val api = object : ChecklistApi by fake {
            override suspend fun registrations(identity: LocalIdentity) = current
        }
        val repository = CatalogRepository(database, api)
        assertTrue(repository.refresh(identity))
        assertTrue(repository.refresh(other))
        val checklistDao = database.checklistDao()
        checklistDao.insertChecklist(Checklist("uuid", empresaId = "1", perfilLocalId = "usuario-7",
            modeloIdOrigem = 1, modeloVersaoOrigem = "", modeloNome = "Original",
            modeloLayout = "padrao", dataHoraUtc = 1))
        current = Registrations(emptyList(), emptyList(), listOf(RemoteTemplate(2, "Novo", "",
            listOf(RemoteTemplateItem(20, 1, "Novo item", "")), false, false, 0, 2)))
        assertTrue(repository.refresh(identity))
        assertEquals(listOf("Novo"), database.checklistModeloDao().observeTemplates("1", "usuario-7").first().map { it.nome })
        assertEquals(listOf("Original"), database.checklistModeloDao().observeTemplates("1", "usuario-8").first().map { it.nome })
        assertEquals("Original", checklistDao.findChecklist("uuid")!!.modeloNome)
        assertTrue(database.vehicleDao().observeVehicles("1", "usuario-7", "").first().isEmpty())
        current = Registrations(emptyList(), emptyList(), listOf(
            RemoteTemplate(3, "Duplicado", "", listOf(RemoteTemplateItem(30, 1, "A", "")), false, false, 0, 2),
            RemoteTemplate(3, "Duplicado", "", listOf(RemoteTemplateItem(31, 1, "B", "")), false, false, 0, 2)))
        assertFalse(repository.refresh(identity))
        assertEquals(listOf("Novo"), database.checklistModeloDao().observeTemplates("1", "usuario-7").first().map { it.nome })
    }

    @Test fun draftAndFinalizedUseOneUuidAndSynchronizeOnlyAfterBatchReply() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val files = PrivateFiles(context)
        val image = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
        val output = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, output)
        val saved = files.save(output.toByteArray(), "image/png")
        val dao = database.checklistDao()
        val checklist = Checklist("same-uuid", empresaId = "1", perfilLocalId = "usuario-7",
            modeloIdOrigem = 1, modeloVersaoOrigem = "", modeloNome = "Modelo", modeloLayout = "padrao",
            dataHoraUtc = 1, veiculoIdOrigem = 1, veiculoNome = "AAA1111",
            motoristaIdOrigem = 2, motoristaNome = "Ana")
        dao.insertChecklist(checklist)
        dao.insertItems(listOf(ChecklistItem("item-uuid", "same-uuid", 1, "Item", "",
            false, false, 0, 3)))
        database.photoDao().insertPhoto(Foto("photo-uuid", checklistId = "same-uuid", itemId = "item-uuid",
            caminhoRelativo = saved.relativePath, ordem = 0, mime = saved.mime,
            tamanhoBytes = saved.sizeBytes, sha256 = saved.sha256, dataHoraUtc = 1))
        database.pendingUploadDao().insertPendingUpload(PendenciaEnvio("same-uuid"))
        val fake = DemoChecklistApi(database)
        val responseLost = object : ChecklistApi by fake {
            override suspend fun sendAttachments(id: String, attachments: List<ChecklistAttachmentPayload>) {
                fake.sendAttachments(id, attachments)
                error("Resposta perdida")
            }
        }
        val identity = LocalIdentity("1", "usuario-7", 7)
        assertFalse(ChecklistSynchronizer(database, files, responseLost, identity).synchronizePendingUploads())
        assertNull(dao.findChecklist("same-uuid")!!.dataHoraSincronizado)
        assertEquals(0, dao.deleteDraft("same-uuid"))
        assertTrue(files.file(saved.relativePath).exists())
        assertTrue(ChecklistSynchronizer(database, files, fake, identity).synchronizePendingUploads())
        assertNotNull(dao.findChecklist("same-uuid")!!.dataHoraSincronizado)
        assertEquals("same-uuid", database.fakeServerDao().findFakeServerChecklist("same-uuid")!!.idLocal)
        assertNull(database.pendingUploadDao().findPendingUpload("same-uuid"))
        dao.updateChecklist(dao.findChecklist("same-uuid")!!.copy(estadoPreenchimento = "FINALIZADO",
            dataHoraSincronizado = null, revisaoLocal = 1))
        database.pendingUploadDao().insertPendingUpload(PendenciaEnvio("same-uuid"))
        assertTrue(ChecklistSynchronizer(database, files, fake, identity).synchronizePendingUploads())
        assertNotNull(dao.findChecklist("same-uuid")!!.dataHoraSincronizado)
        files.delete(saved.relativePath)
        Unit
    }

    @Test fun editDuringUploadKeepsChecklistPending() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val files = PrivateFiles(context)
        val dao = database.checklistDao()
        dao.insertChecklist(Checklist("editing-uuid", empresaId = "1", perfilLocalId = "usuario-7",
            modeloIdOrigem = 1, modeloVersaoOrigem = "", modeloNome = "Modelo", modeloLayout = "padrao",
            dataHoraUtc = 1, veiculoIdOrigem = 1, veiculoNome = "AAA1111",
            motoristaIdOrigem = 2, motoristaNome = "Ana"))
        database.pendingUploadDao().insertPendingUpload(PendenciaEnvio("editing-uuid"))
        val fake = DemoChecklistApi(database)
        val editedDuringUpload = object : ChecklistApi by fake {
            override suspend fun sendAttachments(id: String, attachments: List<ChecklistAttachmentPayload>) {
                fake.sendAttachments(id, attachments)
                val current = dao.findChecklist(id)!!
                dao.updateChecklist(current.copy(observacao = "Editado", revisaoLocal = current.revisaoLocal + 1))
            }
        }
        val identity = LocalIdentity("1", "usuario-7", 7)
        assertFalse(ChecklistSynchronizer(database, files, editedDuringUpload, identity).synchronizePendingUploads())
        assertNull(dao.findChecklist("editing-uuid")!!.dataHoraSincronizado)
        assertNotNull(database.pendingUploadDao().findPendingUpload("editing-uuid"))
        assertTrue(ChecklistSynchronizer(database, files, fake, identity).synchronizePendingUploads())
        assertNotNull(dao.findChecklist("editing-uuid")!!.dataHoraSincronizado)
    }
}
