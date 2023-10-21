# ATM

A very simple backend of an ATM.

Business objects:
- Cards (id, pin number)
- Accounts (id, type, balance)
- Customers (id, display name)

Relations:
- a customer may have 0..n cards and 0..n accounts
- there is _no_ relation between cards and accounts

Available actions:
- change pin 
- withdraw from account
- deposit to account
- transfer from account to another account

# Concepts

- The storage layer is done by simple repository classes with in-memory storage, see `Repos.kt`
- In `Model.kt`, the business objects are modelled as data classes (without any business logic). 
- Commands are used to map all actions, that change the state, see `Commands.kt`. 
- Finally, the class `ATMLogic` receives the commands and executes it.

# Commands

Test: `./gradlew test`

