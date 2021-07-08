package com.jeromepaulos.hyaddons.features;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jeromepaulos.hyaddons.config.Config;
import com.jeromepaulos.hyaddons.utils.HttpUtils;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColoredNames {

    public static HashMap<String, String> users = new HashMap<>();

    public static void loadNames() {
        new Thread(() -> {
            String response = HttpUtils.fetch("https://raw.githubusercontent.com/HyAddons/HyAddons-data/main/names.json");
            if(response != null) {
                Gson gson = new Gson();
                Type stringStringMap = new TypeToken<HashMap<String, String>>(){}.getType();
                users = gson.fromJson(response, stringStringMap);
            }
        }, "HyAddons-Names").start();
    }

    public static String replaceString(String text) {
        if(Config.coloredNames) {
            for (Map.Entry<String, String> user : users.entrySet()) {
                if(text.contains(user.getKey())) {
                    text = replaceName(text, user.getKey(), user.getValue());
                }
            }
        }
        return text;
    }

    /*@SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if(Config.coloredNames && event.type == 0) {
            for (Map.Entry<String, String> user : users.entrySet()) {
                if(event.message.getFormattedText().contains(user.getKey())) {
                    *//*ChatComponentText text = (ChatComponentText) event.message;
                    IChatComponent newComponent = new ChatComponentText(replaceName(text.getFormattedText(), user.getKey(), user.getValue()));
                    newComponent.setChatStyle(text.getChatStyle().createShallowCopy());
                    event.message = newComponent;*//*
                    event.message = replaceChat(event.message, user.getKey(), user.getValue());
                }
            }
        }
    }*/

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        if(Config.coloredNames) {
            for (Map.Entry<String, String> user : users.entrySet()) {
                for (int i = 0; i < event.toolTip.size(); i++) {
                    if (event.toolTip.get(i).contains(user.getKey())) {
                        event.toolTip.set(i, replaceName(event.toolTip.get(i), user.getKey(), user.getValue()));
                    }
                }
            }
        }
    }

    public static String replaceName(String text, String name, String color) {
        String regex = "(§[0-9a-f])\\[(VIP|MVP)([§0-9a-fr+]*)?\\] "+name;
        String replacement = "§"+color+"[$2$3§"+color+"] "+name;

        Matcher matcher = Pattern.compile(regex).matcher(text);
        String result = matcher.replaceAll(replacement);
        result = result.replace(name+"§", "§"+color+name+"§");
        return result;
    }

    public static IChatComponent replaceChat(IChatComponent component, String user, String color) {
        IChatComponent newComponent;
        ChatComponentText text = (ChatComponentText) component;

        newComponent = new ChatComponentText(replaceName(text.getFormattedText(), user, color));
        ChatStyle oldChatStyle = text.getChatStyle();
        ChatStyle newChatStyle = new ChatStyle()
                .setChatClickEvent(oldChatStyle.getChatClickEvent())
                .setChatHoverEvent(oldChatStyle.getChatHoverEvent());
        newComponent.setChatStyle(newChatStyle);

        for(IChatComponent sibling : component.getSiblings()) {
            newComponent.appendSibling(replaceChatComponent(sibling, user, color));
        }

        return newComponent;
    }

    /**
     * Modified from HyChat under Creative Commons Attribution-NonCommercial 3.0
     * https://github.com/Moulberry/HyChat/blob/master/LICENSE
     * @author Moulberry
     */
    public static IChatComponent replaceChatComponent(IChatComponent component, String user, String color) {
        IChatComponent newComponent;
        // ChatComponentText text = (ChatComponentText) component;

        newComponent = new ChatComponentText(replaceName(component.getUnformattedTextForChat(), user, color));
        newComponent.setChatStyle(component.getChatStyle().createShallowCopy());

        for(IChatComponent sibling : component.getSiblings()) {
            newComponent.appendSibling(replaceChat(sibling, user, color));
        }

        return newComponent;
    }

}