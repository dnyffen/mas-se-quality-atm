package ch.ost.masse.quality.atm

class ByCustomerRepository<ID: Ident, E: Entity<ID>>{
    private val customerAssignment = HashMap<CustomerId, List<ID>>()
    private val storage = ArrayList<E>()

    fun byCustomer(customerId :CustomerId): List<E> = customerAssignment.getOrDefault(customerId, emptyList()).map { getById(it) }
    fun getById(id: ID) = storage.first{ it.id == id}
    fun getById(id: ID, errorMsg: String): Result<E> {
        val entities = storage.filter { it.id == id }
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

