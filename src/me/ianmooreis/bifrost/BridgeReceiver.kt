package me.ianmooreis.bifrost

import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.exceptions.PermissionException

class BridgeReceiver(sender: TextChannel) {
    private val bifrostRegex = Regex("Bifrost:(\\d*)")
    val channel: TextChannel? = getBridgeReceiver(sender)
    val webhook: String? = if (channel != null) getBridgeWebhook(channel) else null
    val verified: Boolean = findBridgeReceiverId(channel?.topic) == sender.id

    private fun getBridgeWebhook(receiverChannel: TextChannel): String? {
        return receiverChannel.webhooks.complete().first()?.url ?: try {
            receiverChannel.createWebhook("Bifrost").complete().url
        } catch (e: PermissionException) {
            null
        }
    }

    private fun getBridgeReceiver(senderChannel: TextChannel): TextChannel? {
        val partnerId = findBridgeReceiverId(senderChannel.topic)
        return if (partnerId != null) senderChannel.jda.getTextChannelById(partnerId) else null
    }

    private fun findBridgeReceiverId(topic: String?): String? {
        return if (topic == null) null else bifrostRegex.find(topic)?.groups?.get(1)?.value
    }
}