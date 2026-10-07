package app.simetris.simapp

import app.simetris.simapp.domain.*
import org.junit.Assert.*
import org.junit.Test

class ChecklistRulesTest {
    private fun item(answer: Answer, reason: String = "", photos: Int = 0) = ItemValidationData(
        "Pneus", answer, reason, reasonRequired = true, photoRequired = true,
        minPhotos = 2, maxPhotos = 3, photoCount = photos)

    @Test fun nonconformingItemRequiresReasonAndPhotos() {
        val missingFields = ChecklistRules.itemMissingFields(item(Answer.NONCONFORMING))
        assertTrue(missingFields.any { it.type == MissingFieldType.REASON })
        assertTrue(missingFields.any { it.type == MissingFieldType.MIN_PHOTOS && it.quantity == 2 })
        assertTrue(ChecklistRules.itemMissingFields(item(Answer.NONCONFORMING, "Desgaste", 2)).isEmpty())
    }

    @Test fun conformingItemNeedsNeitherReasonNorMinimumPhotos() {
        assertTrue(ChecklistRules.itemMissingFields(item(Answer.CONFORMING)).isEmpty())
        assertTrue(ChecklistRules.itemMissingFields(item(Answer.CONFORMING, photos = 4)).any { it.type == MissingFieldType.MAX_PHOTOS })
    }

    @Test fun cannotFinalizeWithoutSignatureAndValidOdometer() {
        val missingFields = ChecklistRules.missingFields("Veículo", "Motorista", -1, listOf(item(Answer.CONFORMING)), false)
        assertTrue(missingFields.any { it.type == MissingFieldType.ODOMETER })
        assertTrue(missingFields.any { it.type == MissingFieldType.SIGNATURE })
    }

    @Test fun signatureStageOpensOnlyAfterDataAndItemsAreComplete() {
        val pendingItem = ChecklistRules.missingFields("Veículo", "Motorista", 100,
            listOf(item(Answer.PENDING)), false)
        assertFalse(ChecklistRules.readyForSignature(pendingItem))
        val missingVehicle = ChecklistRules.missingFields(null, "Motorista", 100,
            listOf(item(Answer.CONFORMING)), false)
        assertFalse(ChecklistRules.readyForSignature(missingVehicle))
        val completeBeforeSignature = ChecklistRules.missingFields("Veículo", "Motorista", 100,
            listOf(item(Answer.CONFORMING)), false)
        assertTrue(ChecklistRules.readyForSignature(completeBeforeSignature))
    }

    @Test fun lastNonconformingItemBlocksSignatureUntilEvidenceIsComplete() {
        val preceding = item(Answer.CONFORMING)
        val incomplete = ChecklistRules.missingFields("Veículo", "Motorista", 100,
            listOf(preceding, item(Answer.NONCONFORMING)), false)
        assertFalse(ChecklistRules.readyForSignature(incomplete))
        val complete = ChecklistRules.missingFields("Veículo", "Motorista", 100,
            listOf(preceding, item(Answer.NONCONFORMING, "Desgaste", 2)), false)
        assertTrue(ChecklistRules.readyForSignature(complete))
    }

    @Test fun nonconformingLastItemNeedsEvidenceEvenWhenModelFlagsAreOptional() {
        val last = ItemValidationData("Documentos", Answer.NONCONFORMING, "",
            reasonRequired = false, photoRequired = false, minPhotos = 0, maxPhotos = 2,
            photoCount = 0)
        val missing = ChecklistRules.missingFields("Veículo", "Motorista", 100,
            listOf(item(Answer.CONFORMING), last), false)
        assertFalse(ChecklistRules.readyForSignature(missing))
        assertTrue(missing.any { it.type == MissingFieldType.REASON })
        assertTrue(missing.any { it.type == MissingFieldType.MIN_PHOTOS && it.quantity == 1 })
        assertTrue(ChecklistRules.readyForSignature(ChecklistRules.missingFields(
            "Veículo", "Motorista", 100,
            listOf(item(Answer.CONFORMING), last.copy(reason = "Falta de documento", photoCount = 1)),
            false)))
    }
}
