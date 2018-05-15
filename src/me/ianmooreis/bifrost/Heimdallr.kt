package me.ianmooreis.bifrost

import me.ianmooreis.bifrost.extensions.reply
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.channel.text.update.TextChannelUpdateTopicEvent
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory

class Heimdallr(private val prefix: String) : ListenerAdapter() {
    private val log : Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)
    private val receiverCache = mutableMapOf<TextChannel, BridgeReceiver>()

    override fun onReady(event: ReadyEvent) {
        log.info("The Bifrost is ready to send for ${event.jda.guilds.size} guilds!")
        event.jda.presence.game = Game.watching("the nine realms | ${prefix}help")
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (event.message.contentStripped.startsWith(prefix) && !event.author.isBot) {
            val command = event.message.contentStripped.removePrefix(prefix).trim().split(" ").getOrNull(0)
            when (command) {
                "help" -> event.message.reply("**Help**\n" +
                        "```setup - get setup info\nstatus - check bridge status\nsource - view source code\ninvite - get invite link for this bot```")
                "setup" -> {
                    event.message.reply("**Setup**\n" +
                            "- Add `Bifrost:${event.channel.id}` to the topic of the partner channel.\n" +
                            "- Run this command again in the partner channel to get the `Bifrost:ID` to put in this channel's topic.")
                }
                "status" -> {
                    println("succ")
                    val receiver: BridgeReceiver = receiverCache.getOrPut(event.channel) {
                        BridgeReceiver(event.channel)
                    }
                    val partner: TextChannel? = receiver.channel
                    val verified: Boolean = receiver.verified
                    event.message.reply("**Status**\n" +
                            when {
                                partner != null && verified -> "Partner ${partner.name} (${partner.id}) from ${partner.guild.name} is paired!"
                                partner != null -> "Partner ${partner.id} is not paired!"
                                else -> "No partner yet!"
                            })
                }
                "source" -> event.message.reply("**Source**\nhttps://github.com/IanMooreIsMe/bifrost")
                "invite" -> event.message.reply("**Invite**\n<https://discordapp.com/oauth2/authorize?client_id=${event.jda.selfUser.id}&scope=bot&permissions=536870912>")
            }
        } else if (!event.isWebhookMessage && event.channel.topic.contains("Bifrost") && event.author != event.jda.selfUser) {
            // Get the webhook (if any), and load the partnership if not already loaded
            val receiver = receiverCache.getOrPut(event.channel) {
                BridgeReceiver(event.channel)
            }
            val webhook = receiver.webhook
            if (webhook != null && receiver.verified) {
                Bridge.send(event.message, webhook)
            }
        }
    }

    override fun onTextChannelUpdateTopic(event: TextChannelUpdateTopicEvent) {
        // Automatically refresh partnerships when channel topics are edited
        val oldPartnership = receiverCache[event.channel]
        if (oldPartnership?.verified == true) {
            log.info("The partnership of ${event.channel} with ${oldPartnership.channel} has been broken.")
            receiverCache.remove(oldPartnership.channel)
        }
        receiverCache[event.channel] = BridgeReceiver(event.channel)
        println(receiverCache)
    }
}