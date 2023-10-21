package ch.ost.masse.quality.atm

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal

internal class ATMLogicTest {


    @Test
    fun testPinChange() {
        val cards: ByCustomerRepository<CardId, Card> = ByCustomerRepository()
        val cardId = CardId(1)
        cards.addEntity(CustomerId(1), Card(cardId, 1234))

        val atmLogic = ATMLogic(cards = cards)

        assertEquals(Success(Unit), atmLogic.dispatchCommand(ChangePin(cardId, 1234, 4332)))
        assertEquals(4332, cards.getById(cardId).pinNumber)

        assertEquals(Failure<Unit>("Invalid pin"), atmLogic.dispatchCommand(ChangePin(cardId, 0, 4332)))
    }

    @Test
    fun testWithdraw() {
        val accounts: ByCustomerRepository<AccountId, Account> = ByCustomerRepository()
        val accountId = AccountId(1)
        accounts.addEntity(CustomerId(1), Account(accountId, AccountType.SAVINGS, BigDecimal(100)))

        val atmLogic = ATMLogic(accounts = accounts)
        assertEquals(Success(Unit), atmLogic.dispatchCommand(WithDraw(accountId, BigDecimal(80))))
        assertEquals(BigDecimal(20), accounts.getById(accountId).balance)

        assertEquals(Failure<Unit>("not enough money"), atmLogic.dispatchCommand(WithDraw(accountId, BigDecimal(21))))

    }

    @Test
    fun testTransfer() {
        val accounts: ByCustomerRepository<AccountId, Account> = ByCustomerRepository()
        val fromAccountId = AccountId(1)
        accounts.addEntity(CustomerId(1), Account(fromAccountId, AccountType.SAVINGS, BigDecimal(100)))
        val toAccountId = AccountId(2)
        accounts.addEntity(CustomerId(1), Account(toAccountId, AccountType.CHECKING, BigDecimal(0)))

        val atmLogic = ATMLogic(accounts = accounts)

        fun transfer(amount: Int, balanceFrom: Int, balanceTo: Int, result: Result<Unit> = Success(Unit)) {
            assertEquals(result, atmLogic.dispatchCommand(Transfer(fromAccountId, toAccountId, BigDecimal(amount))))
            assertEquals(BigDecimal(balanceFrom), accounts.getById(fromAccountId).balance)
            assertEquals(BigDecimal(balanceTo), accounts.getById(toAccountId).balance)

        }
        transfer(111, 100, 0, Failure("not enough money"))
        transfer(20, 80, 20)
        transfer(80, 0, 100)
        transfer(1, 0, 100, Failure("not enough money"))
    }


}
