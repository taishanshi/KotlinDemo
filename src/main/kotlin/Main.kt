package org.example

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking

//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
@OptIn(DelicateCoroutinesApi::class)
fun main() {
    val name = "Kotlin"
    //TIP 当文本光标位于高亮显示的文本处时按 <shortcut actionId="ShowIntentionActions"/>
    // 查看 IntelliJ IDEA 建议如何修正。
    println("Hello, " + name + "!")

    for (i in 1..5) {
        //TIP 按 <shortcut actionId="Debug"/> 开始调试代码。我们已经设置了一个 <icon src="AllIcons.Debugger.Db_set_breakpoint"/> 断点
        // 但您始终可以通过按 <shortcut actionId="ToggleLineBreakpoint"/> 添加更多断点。
        println("i = $i")
    }

    runBlocking {

        println("GlobalScope 1 started")
        NewsRepository().loadNews()
        println("GlobalScope 2 started")
        demoFlow()

        listOf(1,2,3,4).asFlow().collect { println(it) }
        numbersFlow2()
        demoAdvancedFlow()
    }
}