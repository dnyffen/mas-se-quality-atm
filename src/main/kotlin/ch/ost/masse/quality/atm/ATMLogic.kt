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
        val customerId = fetchCustomer(command.cardId)
        when (command) {
            is WithDraw -> withdraw(customerId, command.accountId, command.amount)
            is Deposit -> deposit(customerId, command.accountId, command.amount)
            is Transfer -> transfer(customerId, command)
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

    private fun transfer(customerId: CustomerId, command: Transfer): Result<Unit> = with(command) {
        withdraw(customerId, fromAccountId, amount)
            .then { deposit(customerId, toAccountId, amount) }
    }

    private fun deposit(customerId: CustomerId, accountId: AccountId, amount: BigDecimal): Result<Unit> = fetchAccount(customerId, accountId)
        .map { accounts.update(Account(id, type, balance.plus(amount))) }

    private fun withdraw(customerId: CustomerId, accountId: AccountId, amount: BigDecimal): Result<Unit> = fetchAccount(customerId, accountId)
        .then { ensureMinBalance(amount) }
        .map { accounts.update(Account(id, type, balance.minus(amount))) }

    private fun fetchCustomer(cardId: CardId): CustomerId = cards.getCustomerForId(cardId)
    private fun fetchAccount(customerId: CustomerId, accountId: AccountId) =
        accounts.getByIdAndCustomer(customerId, accountId, "Account not found")

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
