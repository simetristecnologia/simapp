package app.simetris.simapp.data.remote

import android.util.Base64
import androidx.room.withTransaction
import app.simetris.simapp.data.local.*

/** Servidor fake explícito, isolado da API HTTP. */
class DemoChecklistApi(private val database: AppDatabase) : ChecklistApi {
    private val dao = database.fakeServerDao()

    override suspend fun registrations(identity: LocalIdentity) = Registrations(
        listOf(RemoteDriver(1, "Ana Souza"), RemoteDriver(2, "João Lima")),
        listOf(RemoteVehicle(1, "ABC-1234"), RemoteVehicle(2, "DEF-5678")),
        listOf(RemoteTemplate(1, "Inspeção de veículo", "Verificação antes da viagem",
            listOf(RemoteTemplateItem(1, 1, "Pneus", "Condição e calibragem"),
                RemoteTemplateItem(2, 2, "Luzes", "Faróis e lanternas"),
                RemoteTemplateItem(3, 3, "Documentos", "Documentos do veículo")),
            true, true, 1, 3)))

    override suspend fun insert(checklist: ChecklistPayload) {
        dao.insertFakeServerChecklist(ServidorFakeChecklist(checklist.id))
    }

    override suspend fun update(id: String, checklist: ChecklistPayload) {
        require(id == checklist.id && dao.findFakeServerChecklist(id) != null)
    }

    override suspend fun sendAttachments(id: String, attachments: List<ChecklistAttachmentPayload>) {
        database.withTransaction {
            require(dao.findFakeServerChecklist(id) != null)
            attachments.forEach { attachment ->
                require(Base64.decode(attachment.file, Base64.DEFAULT).size.toLong() == attachment.fileSize)
            }
            dao.deleteFakeAttachments(id)
            dao.insertFakeAttachments(attachments.map { ServidorFakeAnexo(it.id, id, true) })
        }
    }
}
