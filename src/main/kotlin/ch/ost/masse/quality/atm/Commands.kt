package ch.ost.masse.quality.atm

import java.math.BigDecimal

sealed interface Command

data class WithDraw(val accountId: AccountId, val amount: BigDecimal): Command
data class Deposit(val accountId: AccountId, val amount: BigDecimal): Command
data class Transfer(val fromAccountId: AccountId, val toAccountId: AccountId, val amount: BigDecimal): Command
data class ChangePin(val cardId: CardId, val oldPin: Int, val newPin: Int): Command

