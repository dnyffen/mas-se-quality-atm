package ch.ost.masse.quality.atm

import java.math.BigDecimal


class ATMLogic(
    private val cards: ByCustomerRepository<CardId, Card> = ByCustomerRepository(),
    private val accounts: ByCustomerRepository<AccountId, Account> = ByCustomerRepository(),
    private val sessions: Sessions = Sessions()
) {


    fun dispatchCommand(command: Command): Result<Unit> = when (command) {
        is StartSession -> startSession(command)
        is EndSession -> endSession(command)
        is CommandInSession -> dispatchSessionCommands(command)
    }

    private fun dispatchSessionCommands(command: CommandInSession) = validateSession(command.cardId).then {
        when (command) {
            is WithDraw -> withdraw(command.accountId, command.amount)
            is Deposit -> deposit(command.accountId, command.amount)
            is Transfer -> transfer(command)
            is ChangePin -> changePin(command)
        }
    }

    private fun endSession(command: EndSession): Result<Unit> = Success(sessions.remove(command.cardId))

    private fun startSession(command: StartSession): Result<Unit> =
        cards.getById(command.cardId, "Invalid card")
            .then { validatePin(command.pin) }
            .map { sessions.add(id) }

    private fun changePin(command: ChangePin): Result<Unit> = with(command) {
        cards.getById(cardId, "Card not found")
            .then { validatePin(oldPin) }
            .map { cards.update(Card(id, newPin)) }
    }

    private fun transfer(command: Transfer): Result<Unit> = with(command) {
        withdraw(fromAccountId, amount)
            .then { deposit(toAccountId, amount) }
    }

    private fun deposit(accountId: AccountId, amount: BigDecimal): Result<Unit> = fetchAccount(accountId)
        .map { accounts.update(Account(id, type, balance.plus(amount))) }

    private fun withdraw(accountId: AccountId, amount: BigDecimal): Result<Unit> = fetchAccount(accountId)
        .then { ensureMinBalance(amount) }
        .map { accounts.update(Account(id, type, balance.minus(amount))) }

    private fun fetchAccount(accountId: AccountId) =
        accounts.getById(accountId, "Account $accountId not found")

    private fun validateSession(cardId: CardId): Result<Unit> = if (sessions.hasSession(cardId)) {
        Success(Unit)
    } else {
        Failure("no session")
    }

    private fun Account.ensureMinBalance(minBalance: BigDecimal): Result<Account> = if (this.balance >= minBalance) {
        Success(this)
    } else {
        Failure("not enough money")
    }

    private fun Card.validatePin(pin: Int): Result<Card> = if (pinNumber == pin) {
        Success(this)
    } else {
        Failure("Invalid pin")
    }

}
