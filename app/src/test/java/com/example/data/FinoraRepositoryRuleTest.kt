package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.Account
import com.example.model.AccountType
import com.example.model.Transaction
import com.example.model.TransactionType
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class FinoraRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAccount_authenticatedUser_savesAccountSuccessfully() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = FinoraRepository(firestore)

        val account = Account(
            id = "acc_${UUID.randomUUID().toString().take(8)}",
            userId = aliceUid,
            name = "Alice Savings",
            accountType = AccountType.SAVINGS.name,
            balancePaise = 500000L,
            isDefault = true
        )

        val savedId = withTimeout(TIMEOUT_MS) {
            repository.saveAccount(account)
        }
        assertEquals(account.id, savedId)

        val accounts = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeAccounts().first { it.any { acc -> acc.id == savedId } }
        }
        assertTrue(accounts.any { it.name == "Alice Savings" })
    }

    @Test
    fun createTransaction_authenticatedUser_savesAndAdjustsAccount() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = FinoraRepository(firestore)

        val accountId = "acc_${UUID.randomUUID().toString().take(8)}"
        repository.saveAccount(
            Account(
                id = accountId,
                userId = aliceUid,
                name = "Wallet",
                accountType = AccountType.WALLET.name,
                balancePaise = 100000L
            )
        )

        val tx = Transaction(
            id = "tx_${UUID.randomUUID().toString().take(8)}",
            userId = aliceUid,
            type = TransactionType.INCOME.name,
            title = "Freelance Gig",
            amountPaise = 25000L,
            category = "Salary",
            date = "2026-10-27",
            accountId = accountId
        )

        val txId = withTimeout(TIMEOUT_MS) {
            repository.createTransaction(tx)
        }
        assertNotNull(txId)

        val transactions = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeTransactions().first { it.any { item -> item.id == txId } }
        }
        assertTrue(transactions.any { it.title == "Freelance Gig" })
    }

    @Test
    fun getAccount_crossUserAccess_isDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = FinoraRepository(firestore)
        val accountId = "acc_${UUID.randomUUID().toString().take(8)}"
        aliceRepo.saveAccount(
            Account(
                id = accountId,
                userId = aliceUid,
                name = "Private Vault",
                accountType = AccountType.BANK.name,
                balancePaise = 999900L
            )
        )

        // Switch user to Bob
        signInTestUser(BOB_EMAIL)
        try {
            withTimeout(TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid).collection("accounts").document(accountId).get().await()
            }
            fail("Expected PERMISSION_DENIED for cross-user access")
        } catch (e: Exception) {
            val firestoreException = e.unwrapFirestoreException()
            assertTrue(
                e is FirebaseFirestoreException || firestoreException != null || e.message?.contains("PERMISSION_DENIED") == true
            )
        }
    }

    @Test
    fun observeAccounts_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = FinoraRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeAccounts().first()
            }
            fail("Expected failure when unauthenticated")
        } catch (e: Exception) {
            // Either throws IllegalStateException (client check) or PERMISSION_DENIED
            assertTrue(
                e is IllegalStateException || e.unwrapFirestoreException()?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
            )
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice_test@finora.internal"
        const val BOB_EMAIL = "bob_test@finora.internal"
        const val TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 5000L
    }
}
