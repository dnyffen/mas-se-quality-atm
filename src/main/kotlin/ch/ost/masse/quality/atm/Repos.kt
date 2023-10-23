package ch.ost.masse.quality.atm

class ByCustomerRepository<ID: Ident, E: Entity<ID>>{
    private val customerAssignment = HashMap<CustomerId, List<ID>>()
    private val storage = ArrayList<E>()

    fun byCustomer(customerId :CustomerId): List<E> = customerAssignment.getOrDefault(customerId, emptyList()).map { getById(it) }
    fun getCustomerForId(id: ID): CustomerId = customerAssignment.filter { it.value.contains(id) }.map { it.key }.first()
    fun getById(id: ID) = storage.first{ it.id == id}

    fun getByIdAndCustomer(customerId: CustomerId, id: ID, errorMsg: String): Result<E> =
        find (byCustomer(customerId).filter { it.id == id }, errorMsg)
    fun getById(id: ID, errorMsg: String): Result<E> = find(storage.filter { it.id == id }, errorMsg)

    private fun find(entities: List<E>, errorMsg: String): Result<E> {
        return if (entities.isEmpty()) {
            Failure(errorMsg)
        } else {
            Success(entities.first())
        }
    }

    fun addEntity(customerId: CustomerId, entity: E) {
        if (storage.any { it.id == entity.id }) throw RuntimeException("An entity with the same id exists already")
        storage.add(entity)
        customerAssignment[customerId] = customerAssignment.getOrDefault(customerId, emptyList()) + entity.id
    }

    fun update(entity: E) {
        storage.removeIf { it.id == entity.id }
        storage.add(entity)
    }
}

class Sessions {
    private val cardIds = HashSet<CardId>()

    fun add(id: CardId) {
        cardIds.add(id)
    }

    fun hasSession(id: CardId) = cardIds.contains(id)

    fun remove(cardId: CardId) {
        cardIds.remove(cardId)
    }
}
