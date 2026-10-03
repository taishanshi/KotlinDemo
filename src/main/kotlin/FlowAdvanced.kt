package org.example

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch

suspend fun demoAdvancedFlow() {
    demoFlatMapLatest()
    demoZipAndCombine()
    demoRetryAndCatch()
    demoConflate()
    demoHotFlows()
}

private suspend fun demoFlatMapLatest() {
    println("\n=== flatMapLatest：新值到来时取消上一次 ===")
    flow {
        emit("k")
        delay(100)
        emit("ko")
        delay(100)
        emit("kot")
    }.flatMapLatest { query -> search(query) }
        .collect { println(it) }
}

private fun search(query: String): Flow<String> = flow {
    emit("搜索「$query」...")
    delay(400)
    emit("「$query」的结果")
}

private suspend fun demoZipAndCombine() {
    println("\n=== zip：按顺序配对，短的流结束就停止 ===")
    flowOf("张三", "李四", "王五")
        .zip(flowOf(90, 80)) { name, score -> "$name=$score" }
        .collect { println(it) }

    println("=== combine：任一侧更新，都用两边的最新值重组 ===")
    val names = flow {
        emit("张三")
        delay(100)
        emit("李四")
    }
    val status = flow {
        delay(40)
        emit("离线")
        delay(100)
        emit("在线")
    }
    names.combine(status) { name, state -> "$name / $state" }
        .collect { println(it) }
}

private suspend fun demoRetryAndCatch() {
    println("\n=== onStart / retry / catch ===")
    var attempt = 0
    flow {
        attempt++
        println("第 $attempt 次请求")
        emit(attempt)
        if (attempt < 3) error("网络错误")
        emit(100)
    }.retry(2)
        .onStart { println("收集开始") }
        .catch { println("重试后仍失败: ${it.message}") }
        .onCompletion { println("流结束") }
        .collect { println("收到 $it") }

    flow<Int> { error("网络错误") }
        .retry(1)
        .catch { println("重试耗尽: ${it.message}") }
        .collect { println("收到 $it") }
}

private suspend fun demoConflate() {
    println("\n=== conflate：收集太慢时丢掉中间值 ===")
    flow {
        for (i in 1..3) {
            delay(100)
            emit(i)
        }
    }.conflate()
        .collect { value ->
            delay(300)
            println("消费 $value")
        }
}

private suspend fun demoHotFlows() = coroutineScope {
    println("\n=== StateFlow：始终保存最新值 ===")
    val state = MutableStateFlow("初始")
    val stateJob = launch {
        state.collect { println("订阅者 A: $it") }
    }
    delay(50)
    state.value = "已更新"
    delay(50)
    println("直接读当前值: ${state.value}")
    stateJob.cancel()

    println("=== SharedFlow(replay=1)：后来的订阅者能拿到上一条 ===")
    val events = MutableSharedFlow<String>(replay = 1)
    events.emit("点击")
    val eventJob = launch {
        events.collect { println("订阅者: $it") }
    }
    delay(50)
    events.emit("刷新")
    delay(50)
    eventJob.cancel()

    println("=== shareIn：冷流转成热流，上游只执行一次 ===")
    val scope = CoroutineScope(SupervisorJob())
    try {
        val shared = flow {
            println("上游执行")
            emit("新闻")
        }.shareIn(scope, SharingStarted.Eagerly, replay = 1)
        delay(50)
        shared.take(1).collect { println("订阅 1: $it") }
        shared.take(1).collect { println("订阅 2: $it") }
    } finally {
        scope.cancel()
    }
}
