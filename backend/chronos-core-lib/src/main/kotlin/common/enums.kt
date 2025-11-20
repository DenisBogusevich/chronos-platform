package com.chronos.core.common

enum class SourceType {
    TELEGRAM, INSTAGRAM, VK, FACEBOOK, LINKEDIN,
    DARK_FORUM, TOR_HIDDEN_SERVICE,
    BITCOIN_BLOCKCHAIN, ETHEREUM_BLOCKCHAIN,
    LEAK_DATABASE,
    INTERNAL_MANUAL
}

enum class TLPLevel {
    WHITE, // public osint data
    GREEN,
    AMBER, // Sensitive
    RED // no sharing
}

enum class IngestionStrategy {
    API_OFFICIAL,
    WEB_SCRAPING,
    MOBILE_EMULATION,
    TOR_PROXY
}