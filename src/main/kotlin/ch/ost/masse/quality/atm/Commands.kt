package ch.ost.masse.quality.atm

import java.math.BigDecimal

sealed interface Command {
    val cardId: CardId
}
data class StartSession(override val cardId: CardId, val pin: Int): Command
data class EndSession(override val cardId: CardId): Command

sealed interface CommandInSession : Command

data class WithDraw(override val cardId: CardId, val accountId: AccountId, val amount: BigDecimal): CommandInSession
data class Deposit(override val cardId: CardId, val accountId: AccountId, val amount: BigDecimal): CommandInSession
data class Transfer(override val cardId: CardId, val fromAccountId: AccountId, val toAccountId: AccountId, val amount: BigDecimal): CommandInSession
data class ChangePin(override val cardId: CardId, val oldPin: Int, val newPin: Int): CommandInSession
