package me.ianmooreis.bifrost

import me.ianmooreis.bifrost.extensions.reply
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory

object Heimdallr : ListenerAdapter() {
    private val log : Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)
    private val linksCache = mutableMapOf<TextChannel, String>()
    private val bifrostRegex = Regex("Bifrost:(\\d*)")

    override fun onReady(event: ReadyEvent) {
        log.info("The Bifrost is ready to send for ${event.jda.guilds.size} guilds!")
        event.jda.presence.game = Game.watching("the nine realms")
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (event.message.mentionedUsers.contains(event.jda.selfUser) && !event.author.isBot) {
            val partnerId = findBridgePartnerId(event.channel.topic)
            val partner = getBridgePartner(event.channel)
            event.message.reply(embed = EmbedBuilder()
                    .setTitle("Bifrost")
                    .addField("Setup",
                            "- Add `Bifrost:${event.channel.id}` to the topic of the partner channel.\n" +
                            "- @me in the partner channel to get the `Bifrost:ID` to put in this channel's topic.", false)
                    .addField("Status",
                            when {
                                partner != null -> "Partner ${partner.name} (${partner.id}) from ${partner.guild.name} is paired!"
                                partnerId != null -> "Partner $partnerId is not paired!"
                                else -> "No partner yet!"
                            }, false)
                    .setFooter("A Discord bot for connecting channels between servers.", event.jda.selfUser.avatarUrl.toString())
                    .build())
        } else if (!event.isWebhookMessage && event.channel.topic.contains("Bifrost") && event.author != event.jda.selfUser) {
            val webhook = getBridgeWebhook(event.channel)
            if (webhook != null) {
                Bridge.send(event.message, webhook)
            }
        }
    }

    private fun getBridgeWebhook(channel: TextChannel): String? {
        val partner = getBridgePartner(channel)
        if (partner != null) {
            return linksCache.getOrPut(partner) {
                val webhooks = partner.webhooks.complete()
                if (webhooks.isEmpty()) {
                    partner.createWebhook("Bifrost").complete().url
                } else {
                    webhooks.first().url
                }
            }
        }
        return null
    }

    private fun getBridgePartner(channel: TextChannel): TextChannel? {
        val partnerId = findBridgePartnerId(channel.topic)
        if (partnerId != null) {
            val partner = channel.jda.getTextChannelById(partnerId)
            val truePartners = findBridgePartnerId(partner.topic).equals(channel.id)
            return if (truePartners) partner else null
        }
        return null
    }

    private fun findBridgePartnerId(topic: String): String? {
        return bifrostRegex.find(topic)?.groups?.get(1)?.value
    }
}