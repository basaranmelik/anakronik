package com.badsector.anakronik.model;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum WorldRegion {
    ALASKA("Alaska"),
    NORTHWEST_TERRITORY("Kuzeybatı Toprakları"),
    ALBERTA("Alberta"),
    ONTARIO("Ontario"),
    EASTERN_CANADA("Doğu Kanada"),
    WESTERN_US("Batı Amerika Birleşik Devletleri"),
    EASTERN_US("Doğu Amerika Birleşik Devletleri"),
    GREENLAND("Grönland"),
    VENEZUELA("Venezuela"),
    PERU("Peru"),
    BRAZIL("Brezilya"),
    ARGENTINA("Arjantin"),
    ICELAND("İzlanda"),
    SCANDINAVIA("İskandinavya"),
    NORTHERN_EUROPE("Kuzey Avrupa"),
    WESTERN_EUROPE("Batı Avrupa"),
    SOUTHERN_EUROPE("Güney Avrupa"),
    RUSSIA("Rusya"),
    URAL("Ural"),
    SIBERIA("Sibirya"),
    YAKUTSK("Yakutistan"),
    KAMCHATKA("Kamçatka"),
    MONGOLIA("Moğolistan"),
    IRKUTSK("İrkutsk"),
    CHINA("Çin"),
    INDIA("Hindistan"),
    AFGHANISTAN("Afganistan"),
    MIDDLE_EAST("Orta Doğu"),
    SOUTHEAST_ASIA("Güneydoğu Asya"),
    INDONESIA("Endonezya"),
    NEW_GUINEA("Yeni Gine"),
    WESTERN_AUSTRALIA("Batı Avustralya"),
    EASTERN_AUSTRALIA("Doğu Avustralya"),
    EGYPT("Mısır"),
    NORTH_AFRICA("Kuzey Afrika"),
    CENTRAL_AFRICA("Orta Afrika"),
    EAST_AFRICA("Doğu Afrika"),
    SOUTH_AFRICA("Güney Afrika");

    private final String turkishName;

    // Türkçe isme göre arama yapmak için statik bir Map (çok daha performanslıdır)
    private static final Map<String, WorldRegion> BY_TURKISH_NAME =
            Stream.of(values()).collect(Collectors.toMap(
                    region -> region.turkishName.toLowerCase(), // Anahtar olarak küçük harfe çevrilmiş Türkçe isim
                    Function.identity() // Değer olarak enum sabitinin kendisi
            ));

    WorldRegion(String turkishName) {
        this.turkishName = turkishName;
    }

    public String getTurkishName() {
        return turkishName;
    }

    /**
     * Verilen Türkçe isme karşılık gelen WorldRegion enum sabitini bulur.
     * Büyük/küçük harf duyarsızdır. Bulamazsa null döner.
     * @param text "Batı Avrupa" gibi Türkçe bölge adı.
     * @return Eşleşen WorldRegion sabiti veya null.
     */
    public static WorldRegion fromTurkishName(String text) {
        if (text == null) {
            return null;
        }
        return BY_TURKISH_NAME.get(text.toLowerCase());
    }
}