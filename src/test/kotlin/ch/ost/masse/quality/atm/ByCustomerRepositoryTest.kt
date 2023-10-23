package ch.ost.masse.quality.atm

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import kotlin.test.assertContains
import kotlin.test.assertFails

internal class ByCustomerRepositoryTest {

    @Test
    fun byCustomer() {
        val repo = ByCustomerRepository<CardId, Card>()
        val cust1 = CustomerId(1)
        val cust2 = CustomerId(2)
        val card1 = Card(CardId(1), 1233)
        val card2 = Card(CardId(2), 1233)
        val card3 = Card(CardId(3), 1233)

        repo.addEntity(cust1, card1)
        repo.addEntity(cust1, card2)
        repo.addEntity(cust2, card3)

        fun check(customerId: CustomerId, vararg cards: Card) {
            val result = repo.byCustomer(customerId)
            assertEquals(cards.size, result.size)
            cards.forEach { assertContains(result, it) }

        }
        check(cust1, card1, card2)
        check(cust2, card3)

        assertFails { repo.addEntity(cust1, card3) }

    }

    @Test
    fun getById() {
        val repo = ByCustomerRepository<CardId, Card>()
        val cust1 = CustomerId(1)
        val card1 = Card(CardId(1), 1233)
        val card2 = Card(CardId(2), 1233)

        repo.addEntity(cust1, card1)
        repo.addEntity(cust1, card2)

        assertEquals(card1, repo.getById(card1.id))
        assertEquals(card2, repo.getById(card2.id))
        assertFails { repo.getById(CardId(3)) }
    }

    @Test
    fun testGetById() {
        val repo = ByCustomerRepository<CardId, Card>()
        val cust1 = CustomerId(1)
        val card1 = Card(CardId(1), 1233)
        val card2 = Card(CardId(2), 1233)

        repo.addEntity(cust1, card1)
        repo.addEntity(cust1, card2)

        assertEquals(Success(card1), repo.getById(card1.id, "Card not found"))
        assertEquals(Failure<Card>("Card not found"), repo.getById(CardId(3), "Card not found"))
    }

    @Test
    fun testGetCustomerForId() {
        val repo = ByCustomerRepository<CardId, Card>()
        val cust1 = CustomerId(1)
        val cust2 = CustomerId(2)
        val card1 = Card(CardId(1), 1233)
        val card2 = Card(CardId(2), 1233)
        val card3 = Card(CardId(3), 1233)

        repo.addEntity(cust1, card1)
        repo.addEntity(cust1, card2)
        repo.addEntity(cust2, card3)

        assertEquals(cust1, repo.getCustomerForId(card1.id))
        assertEquals(cust1, repo.getCustomerForId(card2.id))
        assertEquals(cust2, repo.getCustomerForId(card3.id))
    }

    @Test
    fun testUpdate() {
        val repo = ByCustomerRepository<CardId, Card>()
        val cust1 = CustomerId(1)
        val cust2 = CustomerId(2)
        val card1 = Card(CardId(1), 1233)
        val card2 = Card(CardId(2), 1233)
        val card3 = Card(CardId(3), 1233)

        repo.addEntity(cust1, card1)
        repo.addEntity(cust1, card2)
        repo.addEntity(cust2, card3)

        val card1Update = Card(card1.id, 444)
        repo.update(card1Update)
        assertEquals(card1Update, repo.getById(card1.id))
    }
}
