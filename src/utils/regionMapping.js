// src/utils/regionMapping.js

const regionMap = {
    'alaska': 'Alaska',
    'northwest_territory': 'Kuzeybatı Toprakları',
    'alberta': 'Alberta',
    'ontario': 'Ontario',
    'eastern_canada': 'Doğu Kanada',
    'western_united_states': 'Batı Amerika Birleşik Devletleri',
    'eastern_united_states': 'Doğu Amerika Birleşik Devletleri',
    'greenland': 'Grönland',
    'venezuela': 'Venezuela',
    'peru': 'Peru',
    'brazil': 'Brezilya',
    'argentina': 'Arjantin',
    'iceland': 'İzlanda',
    'scandinavia': 'İskandinavya',
    'northern_europe': 'Kuzey Avrupa',
    'western_europe': 'Batı Avrupa',
    'southern_europe': 'Güney Avrupa',
    'ukraine': 'Ukrayna', // Bu listede yoktu, ekledim
    'russia': 'Rusya', // Enum'da vardı, burada eksikti, ekledim
    'ural': 'Ural',
    'siberia': 'Sibirya',
    'yakutsk': 'Yakutistan',
    'kamchatka': 'Kamçatka',
    'mongolia': 'Moğolistan',
    'irkutsk': 'İrkutsk',
    'china': 'Çin',
    'japan': 'Japonya', // Bu listede yoktu, ekledim
    'india': 'Hindistan',
    'afghanistan': 'Afganistan',
    'middle_east': 'Orta Doğu',
    'southeast_asia': 'Güneydoğu Asya', // Enum'da vardı, burada eksikti, ekledim
    'indonesia': 'Endonezya',
    'new_guinea': 'Yeni Gine',
    'western_australia': 'Batı Avustralya',
    'eastern_australia': 'Doğu Avustralya',
    'egypt': 'Mısır',
    'north_africa': 'Kuzey Afrika',
    'central_africa': 'Orta Afrika',
    'east_africa': 'Doğu Afrika',
    'south_africa': 'Güney Afrika',
    'great_britain': 'Büyük Britanya', // Bu listede yoktu, ekledim
    'madagascar': 'Madagaskar' // Bu listede yoktu, ekledim
};

// Bir SVG ID'sinden kullanıcıya gösterilecek ismi getiren yardımcı fonksiyon
export const getRegionDisplayName = (id) => {
    // Artık tire/alt tire değişikliğine gerek yok, doğrudan eşleştirme yapıyoruz.
    return regionMap[id] || id;
};