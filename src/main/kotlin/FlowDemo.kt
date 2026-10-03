package org.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

fun numbersFlow(): Flow<Int> = flow {
    println("开始生产，线程=${Thread.currentThread().name}")
    for (i in 1..5) {
        delay(200)
        emit(i)
    }
}.flowOn(Dispatchers.Default)

suspend fun demoFlow() {
    println("开始收集")
    numbersFlow()
        .filter { it % 2 == 1 }
        .map { "奇数: $it" }
        .collect { println(it) }
    println("收集结束")
}

suspend fun numbersFlow2() {
    flowOf("one", "two", "three").filter { it -> it != "two" }.collect { println(it) }
}
