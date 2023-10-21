package ch.ost.masse.quality.atm

import java.math.BigDecimal

interface Ident
interface Entity<K: Ident> {
    val id: K
}

data class CustomerId(val number: Int): Ident
data class AccountId(val number: Int): Ident
data class CardId(val number: Int): Ident

data class Card(override val id: CardId, val pinNumber: Int): Entity<CardId>

enum class AccountType { SAVINGS, CHECKING }

data class Account(override val id: AccountId, val type: AccountType, val balance: BigDecimal): Entity<AccountId>
