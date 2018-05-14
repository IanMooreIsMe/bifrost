package me.ianmooreis.bifrost

import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder

object Bifrost : JDABuilder(AccountType.BOT) {
    init {
        this.setToken(System.getenv("DISCORD_TOKEN")).addEventListener(Heimdallr)
    }
}

fun main(args: Array<String>) {
    Bifrost.buildAsync()
}