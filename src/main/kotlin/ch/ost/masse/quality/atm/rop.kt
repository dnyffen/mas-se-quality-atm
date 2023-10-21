package ch.ost.masse.quality.atm

sealed class Result<T>
data class Success<T>(val value: T) : Result<T>()
data class Failure<T>(val errorMessage: String) : Result<T>()

infix fun <T, U> Result<T>.then(f: T.() -> Result<U>) =
    when (this) {
        is Success -> this.value.run(f)
        is Failure -> Failure(this.errorMessage)
    }

infix fun <T, U> Result<T>.map(f: T.() -> U) =
    when (this) {
        is Success -> Success(this.value.run(f))
        is Failure -> Failure(this.errorMessage)
    }

