package ch.ost.masse.quality.atm

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal

internal class ATMLogicTest {


    @Test
    fun testStartSession() {
        val cards: ByCustomerRepository<CardId, Card> = ByCustomerRepository()
        val cardId = CardId(1)
        cards.addEntity(CustomerId(1), Card(cardId, 1234))

        val atmLogic = ATMLogic(cards = cards)
        assertEquals(Success(Unit), atmLogic.dispatchCommand(StartSession(cardId, 1234)))
        assertEquals(Failure<Unit>("Invalid pin"), atmLogic.dispatchCommand(StartSession(cardId, 43)))
    }

    @Test
    fun testEndSession() {
        val cards: ByCustomerRepository<CardId, Card> = ByCustomerRepository()
        val cardId = CardId(1)
        cards.addEntity(CustomerId(1), Card(cardId, 1234))

        val atmLogic = ATMLogic(cards = cards)
        assertEquals(Success(Unit), atmLogic.dispatchCommand(StartSession(cardId, 1234)))
        assertEquals(Success(Unit), atmLogic.dispatchCommand(EndSession(cardId)))
        assertEquals(Failure<Unit>("no session"), atmLogic.dispatchCommand(ChangePin(cardId, 1234, 4332)))
    }


    @Test
    fun testPinChange() {
        val cards: ByCustomerRepository<CardId, Card> = ByCustomerRepository()
        val cardId = CardId(1)
        cards.addEntity(CustomerId(1), Card(cardId, 1234))

        val atmLogic = ATMLogic(cards = cards)
        atmLogic.dispatchCommand(StartSession(cardId, 1234))

        assertEquals(Success(Unit), atmLogic.dispatchCommand(ChangePin(cardId, 1234, 4332)))
        assertEquals(4332, cards.getById(cardId).pinNumber)

        assertEquals(Failure<Unit>("Invalid pin"), atmLogic.dispatchCommand(ChangePin(cardId, 0, 4332)))
    }

    @Test
    fun testWithdraw() {
        val cardId = CardId(1)
        val customerId = CustomerId(1)
        val accountId = AccountId(1)
        val cards: ByCustomerRepository<CardId, Card> = ByCustomerRepository()
        val accounts: ByCustomerRepository<AccountId, Account> = ByCustomerRepository()
        cards.addEntity(customerId, Card(cardId, 1234))
        accounts.addEntity(customerId, Account(accountId, AccountType.SAVINGS, BigDecimal(100)))

        val atmLogic = ATMLogic(accounts = accounts, cards = cards)
        atmLogic.dispatchCommand(StartSession(cardId, 1234))

        assertEquals(Success(Unit), atmLogic.dispatchCommand(WithDraw(cardId, accountId, BigDecimal(80))))
        assertEquals(BigDecimal(20), accounts.getById(accountId).balance)

        assertEquals(Failure<Unit>("not enough money"), atmLogic.dispatchCommand(WithDraw(cardId, accountId, BigDecimal(21))))

    }

    @Test
    fun testWithdrawFromOtherCustomer() {
        val cardId = CardId(1)
        val customerId = CustomerId(1)
        val customerId2 = CustomerId(2)
        val accountId = AccountId(1)
        val cards: ByCustomerRepository<CardId, Card> = ByCustomerRepository()
        val accounts: ByCustomerRepository<AccountId, Account> = ByCustomerRepository()
        cards.addEntity(customerId, Card(cardId, 1234))
        accounts.addEntity(customerId2, Account(accountId, AccountType.SAVINGS, BigDecimal(100)))

        val atmLogic = ATMLogic(accounts = accounts, cards = cards)
        atmLogic.dispatchCommand(StartSession(cardId, 1234))

        assertEquals(Failure<Unit>("Account not found"), atmLogic.dispatchCommand(WithDraw(cardId, accountId, BigDecimal(80))))

    }

    @Test
    fun testTransfer() {
        val cardId = CardId(1)
        val customerId = CustomerId(1)
        val fromAccountId = AccountId(1)
        val toAccountId = AccountId(2)
        val cards: ByCustomerRepository<CardId, Card> = ByCustomerRepository()
        val accounts: ByCustomerRepository<AccountId, Account> = ByCustomerRepository()
        cards.addEntity(customerId, Card(cardId, 1234))
        accounts.addEntity(customerId, Account(fromAccountId, AccountType.SAVINGS, BigDecimal(100)))
        accounts.addEntity(customerId, Account(toAccountId, AccountType.CHECKING, BigDecimal(0)))

        val atmLogic = ATMLogic(accounts = accounts, cards = cards)
        atmLogic.dispatchCommand(StartSession(cardId, 1234))

        fun transfer(amount: Int, balanceFrom: Int, balanceTo: Int, result: Result<Unit> = Success(Unit)) {
            assertEquals(result, atmLogic.dispatchCommand(Transfer(cardId, fromAccountId, toAccountId, BigDecimal(amount))))
            assertEquals(BigDecimal(balanceFrom), accounts.getById(fromAccountId).balance)
            assertEquals(BigDecimal(balanceTo), accounts.getById(toAccountId).balance)

        }
        transfer(111, 100, 0, Failure("not enough money"))
        transfer(20, 80, 20)
        transfer(80, 0, 100)
        transfer(1, 0, 100, Failure("not enough money"))
    }


}
