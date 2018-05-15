package me.ianmooreis.bifrost

import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder

class Bifrost(token: String, prefix: String) : JDABuilder(AccountType.BOT) {
    init {
        this.setToken(token).addEventListener(Heimdallr(prefix))
    }
}

fun main(args: Array<String>) {
    Bifrost(token = System.getenv("DISCORD_TOKEN") ?: args[0], prefix = "bf->").buildAsync()
}