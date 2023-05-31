import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.*

/**
 * Параллельные вычисления позволяют выполнять сразу несколько задач
 * Асинхронность позволяет не блокировать главный ui поток
 * Корутина - это блок кода, который может выполняться параллельно с остальным кодом
 * Корутина может приостановить выполнение в одном потоке, а возобновить в другом потоке
 * Корутина приостанавливается, поток освобождается и может начать выполнять другую задачу
 * RunBlocking - блокирует поток, пока не выполняться все корутины, а coroutineScope приостанавливает
 * чтобы поток мог выполнять другую задачу
 * launch - не возвращает нам результат, возвращает обьект Job, с помощью него можно управлять корутиной
 * async - если нужно получить некий результат, возвращает обьект Deferred унаследован от Job
 * Channel - работает с потоком данных, имеет два метода: send and receive
 * Producer-Consumer
 * Асинхронные потоки позволяют решить проблему, чтобы при получении большого количества данных мы могли
 * ими манипулировать в процессе работа
 * Способы создания асинхронных потоков: flow, (flowOf, asFlow - автоматически эмитируют данные в поток)
 */

suspend fun main() {
    function1()
    function2()
    function3()
    function4()
    function5()
    function6()
    function7()
    function8()
    function9()
    function10()
    function11()
}

suspend fun function1() = coroutineScope {

    val job = launch(start = CoroutineStart.LAZY) {
        delay(200L)
        println("Coroutine has started")
    }

    delay(1000L)
    job.start()
    println("Other actions in main method")
}

fun function2() = runBlocking {
    launch {
        for (i in 0..5) {
            delay(400L)
            println(i)
        }
    }

    println("Hello Coroutines")
}


suspend fun function3() = coroutineScope {

    val sum = async(start = CoroutineStart.LAZY) { sum(1, 2) }

    delay(1000L)
    println("Actions after the coroutine creation")
    sum.start()
    println("sum: ${sum.await()}")
}

fun sum(a: Int, b: Int): Int {
    println("Coroutine has started")
    return a + b
}

suspend fun function4() = coroutineScope {

    val downloader: Job = launch {
        try {
            println("Начинаем загрузку файлов")
            for (i in 1..5) {
                println("Загружен файл $i")
                delay(500L)
            }
        } catch (e: CancellationException) {
            println("Загрузка файлов прервана")
        } finally {
            println("Загрузка завершена")
        }
    }
    delay(800L)
    println("Надоело ждать, пока все файлы загрузятся. Прерву-ка я загрузку...")
    downloader.cancelAndJoin()
    println("Работа программы завершена")
}

suspend fun function5() = coroutineScope {

    val channel = Channel<String>()
    launch {
        val users = listOf("Tom", "Bob", "Sam")
        for (user in users) {
            channel.send(user)
        }
        channel.close()
    }

    for (user in channel) {
        println(user)
    }
    println("End")
}

suspend fun function6() = coroutineScope {

    val users = getUsers()
    users.consumeEach { user -> println(user) }
    println("End")
}

@OptIn(ExperimentalCoroutinesApi::class)
fun CoroutineScope.getUsers(): ReceiveChannel<String> = produce {
    val users = listOf("Tom", "Bob", "Sam")
    for (user in users) {
        send(user)
    }
}

suspend fun function7() {
    getUsers().collect { user -> println(user) }
}

fun getUsers(): Flow<String> = flow {
    val database = listOf("Tom", "Bob", "Sam")
    var i = 1
    for (item in database) {
        delay(400L)
        println("Emit $i item")
        emit(item)
        i++
    }
}

suspend fun function8() {

    val numberFlow: Flow<Int> = flowOf(1, 2, 3, 5, 8)
    numberFlow.collect { n -> println(n) }
}

suspend fun function9() {
    val userFlow = listOf("Tom", "Bob", "Kate", "Sam", "Alice").asFlow()
    val firstUser = userFlow.first()
    val lastUser = userFlow.last()
    userFlow.take(3).collect { user -> println(user) }
    userFlow.drop(3).collect { user -> println(user) }
    val count = userFlow.count { username -> username.length > 3 }
    println("Count: $count")
    println("First User: $firstUser")
    println("Last User: $lastUser")
    val user = listOf("Tom").asFlow()
    try {
        val singleUser = user.single()
        println("Single User: $singleUser")
    } catch (e: Exception) {
        println(e.message)
    }
}

suspend fun function10() {

    val peopleFlow = listOf(
            Person("Tom", 37),
            Person("Alice", 32),
            Person("Bill", 5),
            Person("Sam", 14),
            Person("Bob", 25),
    ).asFlow()

    peopleFlow.map { person ->
        object {
            val name = person.name
            val isAdult = person.age > 17
        }
    }.collect { user -> println("name: ${user.name}   adult:  ${user.isAdult} ") }

    val numbersFlow = listOf(2, 3, 4).asFlow()
    numbersFlow.transform { n ->
        emit(n)
        emit(n * n)
    }.collect { n -> println(n) }

    peopleFlow.filter { person -> person.age > 17 }
            .collect { person -> println("name: ${person.name}   age:  ${person.age} ") }

    peopleFlow.takeWhile { person -> person.age > 17 }
            .collect { person -> println("name: ${person.name}   age:  ${person.age} ") }

    peopleFlow.dropWhile { person -> person.age > 17 }
            .collect { person -> println("name: ${person.name}   age:  ${person.age} ") }
}

data class Person(val name: String, val age: Int)

suspend fun function11() {

    val numberFlow = listOf(1, 2, 3, 4, 5).asFlow()
    val reducedValue = numberFlow.reduce { a, b -> a + b }
    println(reducedValue)

    val userFlow = listOf("Tom", "Bob", "Kate", "Sam", "Alice").asFlow()
    val foldedValue = userFlow.fold("Users:") { a, b -> "$a $b" }
    println(foldedValue)

    val english = listOf("red", "yellow", "blue").asFlow()
    val russian = listOf("красный", "желтый", "синий").asFlow()
    english.zip(russian) { a, b -> "$a: $b" }
            .collect { word -> println(word) }
}