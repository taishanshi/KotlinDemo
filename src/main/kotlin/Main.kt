package org.example

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
//https://juejin.cn/post/6956115368578383902
//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
@OptIn(DelicateCoroutinesApi::class)
fun main() {
//    runBlocking {
//        testUndispatched()
//        delay(3000)
//    }
    testCoroutineContext()
    Thread.sleep(2000)

}

private fun testCoroutineContext(){
    val job = Job()
    val coroutineContext1 = job + CoroutineName("这是第一个上下文")
    println(Job().key)
    println("coroutineContext1 $coroutineContext1")
    println("coroutineContext1 minus job,key ${coroutineContext1.minusKey(job.key)}")
    coroutineContext1.job.invokeOnCompletion {}
    val  coroutineContext2 = coroutineContext1 + Dispatchers.Default + CoroutineName("这是第二个上下文")
    println("coroutineContext2  $coroutineContext2")
    val coroutineContext3 = coroutineContext2 + Dispatchers.Main + CoroutineName("这是第三个上下文")
    println("coroutineContext3  $coroutineContext3")
}


private fun testCoroutineExceptionHandler(){
    GlobalScope.launch {
        val job = launch {
            println("${Thread.currentThread().name} 抛出未捕获异常")
            throw NullPointerException("异常测试")
        }
        job.join()
        println("${Thread.currentThread().name} end")
    }
}

suspend fun testUndispatched() {
    GlobalScope.launch {
        println("最开始： ${Thread.currentThread().name}")
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            println("job start： ${Thread.currentThread().name}")
            delay(1000)
            println("job end： ${Thread.currentThread().name}")
        }
        println("Join 前： ${Thread.currentThread().name}")
        job.join()
        println("join 后： ${Thread.currentThread().name}")
    }

}

//不使用语法糖的样子
suspend fun testFlowBase() {
    val myFlow = object : Flow<Int> {
        override suspend fun collect(collector: FlowCollector<Int>) {
            for (i in 1..5) {
                collector.emit(i)
            }
        }
    }
    // 2. 手动实现 FlowCollector 接口（消费者）
    val myCollector = object : FlowCollector<Int> {
        override suspend fun emit(value: Int) {
            println(value)
        }
    }
    myFlow.collect(myCollector)
}