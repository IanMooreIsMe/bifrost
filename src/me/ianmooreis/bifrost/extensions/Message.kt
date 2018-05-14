package me.ianmooreis.bifrost.extensions

import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException
import java.util.concurrent.TimeUnit

fun Message.reply(content: String? = null, embed: MessageEmbed? = null, deleteAfterDelay: Long = 0, deleteAfterUnit: TimeUnit = TimeUnit.SECONDS) {
    if (content == null && embed == null) { return }
    val message = MessageBuilder().setContent(content?.trim()).setEmbed(embed).build()
    try {
        this.channel.sendMessage(message).queue {
            if (deleteAfterDelay > 0) {
                it.delete().queueAfter(deleteAfterDelay, deleteAfterUnit)
            }
        }
    } catch (e: InsufficientPermissionException) { }
}