// BlockedAppsManager file

package com.example.minimalphone

object BlockedAppsManager {
    // Expanded list of commonly blocked apps
    val blockedPackages = setOf(
// Social Media
        "com.instagram.android",
        "com.zhiliaoapp.musically", // TikTok
        "com.snapchat.android",
        "com.facebook.katana", // Facebook
        "com.facebook.orca", // Facebook Messenger
        "com.twitter.android",
        "com.reddit.frontpage",
        "com.linkedin.android",
        "com.pinterest",
        "com.tumblr",

        // Entertainment & Video
        "com.google.android.youtube",
        "com.netflix.mediaclient",
        "com.hulu.plus",
        "com.disney.disneyplus",
        "com.amazon.avod.thirdpartyclient", // Prime Video
        "com.spotify.music",
        "tv.twitch.android.app",

        // Gaming
        "com.roblox.client",
        "com.supercell.clashofclans",
        "com.supercell.brawlstars",
        "com.epicgames.fortnite",
        "com.miHoYo.GenshinImpact",
        "com.pubg.imobile",
        "com.mojang.minecraftpe",

        // Shopping & Browsing
        "com.android.vending", // Google Play Store
        "com.amazon.mShop.android.shopping",
        "com.ebay.mobile",
        "com.contextlogic.wish",
        "com.google.android.apps.magazines", // Google News

        // Messaging (optional - be careful with these)
        "com.whatsapp",
        "com.telegram.messenger",
        "com.discord",
        "com.snapchat.android",

        // Browsers (use with caution)
        "com.android.chrome",
        "org.mozilla.firefox",
        "com.opera.browser",
        "com.brave.browser"
    )

    fun isBlocked(packageName: String): Boolean {
        return blockedPackages.contains(packageName)
    }

    fun getAppDisplayName(packageName: String): String {
        return when(packageName) {
            // Social Media
            "com.instagram.android" -> "Instagram"
            "com.zhiliaoapp.musically" -> "TikTok"
            "com.snapchat.android" -> "Snapchat"
            "com.facebook.katana" -> "Facebook"
            "com.facebook.orca" -> "Messenger"
            "com.twitter.android" -> "Twitter"
            "com.reddit.frontpage" -> "Reddit"
            "com.linkedin.android" -> "LinkedIn"
            "com.pinterest" -> "Pinterest"
            "com.tumblr" -> "Tumblr"

            // Entertainment & Video
            "com.google.android.youtube" -> "YouTube"
            "com.netflix.mediaclient" -> "Netflix"
            "com.hulu.plus" -> "Hulu"
            "com.disney.disneyplus" -> "Disney+"
            "com.amazon.avod.thirdpartyclient" -> "Prime Video"
            "com.spotify.music" -> "Spotify"
            "tv.twitch.android.app" -> "Twitch"

            // Gaming
            "com.roblox.client" -> "Roblox"
            "com.supercell.clashofclans" -> "Clash of Clans"
            "com.supercell.brawlstars" -> "Brawl Stars"
            "com.epicgames.fortnite" -> "Fortnite"
            "com.miHoYo.GenshinImpact" -> "Genshin Impact"
            "com.pubg.imobile" -> "PUBG Mobile"
            "com.mojang.minecraftpe" -> "Minecraft"

            // Shopping & Browsing
            "com.android.vending" -> "Play Store"
            "com.amazon.mShop.android.shopping" -> "Amazon"
            "com.ebay.mobile" -> "eBay"
            "com.contextlogic.wish" -> "Wish"
            "com.google.android.apps.magazines" -> "Google News"

            // Messaging
            "com.whatsapp" -> "WhatsApp"
            "com.telegram.messenger" -> "Telegram"
            "com.discord" -> "Discord"

            // Browsers
            "com.android.chrome" -> "Chrome"
            "org.mozilla.firefox" -> "Firefox"
            "com.opera.browser" -> "Opera"
            "com.brave.browser" -> "Brave"

            // Default fallback
            else -> packageName.substringAfterLast(".").replaceFirstChar { it.uppercase() }
        }
    }

    // Helper function to get category for UI purposes (optional)
    fun getAppCategory(packageName: String): String {
        return when(packageName) {
            in setOf("com.instagram.android", "com.zhiliaoapp.musically", "com.snapchat.android",
                "com.facebook.katana", "com.twitter.android", "com.reddit.frontpage",
                "com.linkedin.android", "com.pinterest", "com.tumblr") -> "Social Media"

            in setOf("com.google.android.youtube", "com.netflix.mediaclient", "com.hulu.plus",
                "com.disney.disneyplus", "com.amazon.avod.thirdpartyclient",
                "com.spotify.music", "tv.twitch.android.app") -> "Entertainment"

            in setOf("com.roblox.client", "com.supercell.clashofclans", "com.supercell.brawlstars",
                "com.epicgames.fortnite", "com.miHoYo.GenshinImpact", "com.pubg.imobile",
                "com.mojang.minecraftpe") -> "Gaming"

            in setOf("com.android.vending", "com.amazon.mShop.android.shopping", "com.ebay.mobile",
                "com.contextlogic.wish") -> "Shopping"

            in setOf("com.whatsapp", "com.telegram.messenger", "com.discord",
                "com.facebook.orca") -> "Messaging"

            in setOf("com.android.chrome", "org.mozilla.firefox", "com.opera.browser",
                "com.brave.browser") -> "Browsers"

            else -> "Other"
        }
    }
}