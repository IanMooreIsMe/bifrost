package me.ianmooreis.bifrost

import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.webhook.WebhookClient
import net.dv8tion.jda.webhook.WebhookClientBuilder
import net.dv8tion.jda.webhook.WebhookMessageBuilder

object Bridge {

    fun send(message: Message, webhookUrl: String) {
        getWebhookClient(message.author, webhookUrl) { client, base ->
            base.setContent(message.contentRaw).addEmbeds(message.embeds)
            if (message.attachments.isNotEmpty() && message.contentRaw.length < 2056) {
                base.append("\n" + message.attachments.joinToString{ it.url })
            }
            client.send(base.build())
        }
    }

    private fun getWebhookClient(user: User, webhookUrl: String, success: (WebhookClient, WebhookMessageBuilder) -> Unit) {
        val client = WebhookClientBuilder(webhookUrl).build()
        val baseMessage = WebhookMessageBuilder().setUsername(user.name).setAvatarUrl(user.avatarUrl)
        success(client, baseMessage)
        client.close()
    }
}