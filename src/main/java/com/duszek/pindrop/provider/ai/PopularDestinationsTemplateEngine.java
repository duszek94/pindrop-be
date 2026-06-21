package com.duszek.pindrop.provider.ai;

import org.springframework.stereotype.Component;

import java.time.Month;
import java.util.List;

@Component
public class PopularDestinationsTemplateEngine {

	private static final String UNSPLASH = "https://images.unsplash.com";

	public List<PopularDestinationSuggestion> suggest(PopularDestinationsRequest request) {
		List<PopularDestinationSuggestion> seasonal = switch (request.periodEnd().getMonth()) {
			case DECEMBER, JANUARY, FEBRUARY -> List.of(
					destination("Reykjavik", "Iceland", "city", "photo-1529963180234-94d383b4d27c"),
					destination("Swiss Alps", "Switzerland", "mountain", "photo-1464822759023-fed622ff2c3b"),
					destination("Lake Bled", "Slovenia", "lake", "photo-1599946347372-9b8a791b9a1d"),
					destination("Lapland", "Finland", "region", "photo-1483347758567-0936f1e55864"),
					destination("Tokyo", "Japan", "city", "photo-1734560918594-d93bf520cfb4"),
					destination("Banff", "Canada", "region", "photo-1503614472-8c93d83e9813"),
					destination("Prague", "Czech Republic", "city", "photo-1541849546449-79d4d1b6085b"),
					destination("Paris", "France", "city", "photo-1713513281437-7da8f102dcd9"));
			case MARCH, APRIL, MAY -> List.of(
					destination("Kyoto", "Japan", "city", "photo-1493976040374-85c8e78512ef"),
					destination("Amalfi Coast", "Italy", "region", "photo-1729763077607-63597d3201be"),
					destination("Lake Como", "Italy", "lake", "photo-1523906834658-6e24ef2386f9"),
					destination("Tatra Mountains", "Poland", "mountain", "photo-1464822759023-fed622ff2c3b"),
					destination("Lisbon", "Portugal", "city", "photo-1707597244387-5eee53dda38e"),
					destination("Cappadocia", "Turkey", "region", "photo-1605647540924-8522905496bf"),
					destination("Barcelona", "Spain", "city", "photo-1583422409516-2895a77efded"),
					destination("Bali", "Indonesia", "city", "photo-1537996194471-e657df975ab4"));
			case JUNE, JULY, AUGUST -> List.of(
					destination("Santorini", "Greece", "city", "photo-1719607526486-96f27a995fcc"),
					destination("Bali", "Indonesia", "city", "photo-1537996194471-e657df975ab4"),
					destination("Lake Geneva", "Switzerland", "lake", "photo-1752346168950-518d474037d3"),
					destination("Scottish Highlands", "United Kingdom", "region", "photo-1742758521359-7f5edec0a9b1"),
					destination("Barcelona", "Spain", "city", "photo-1583422409516-2895a77efded"),
					destination("Dolomites", "Italy", "mountain", "photo-1464822759023-fed622ff2c3b"),
					destination("Amalfi Coast", "Italy", "region", "photo-1729763077607-63597d3201be"),
					destination("Lisbon", "Portugal", "city", "photo-1707597244387-5eee53dda38e"));
			case SEPTEMBER, OCTOBER, NOVEMBER -> List.of(
					destination("Paris", "France", "city", "photo-1713513281437-7da8f102dcd9"),
					destination("New England", "United States", "region", "photo-1506905925346-21bda4d32df4"),
					destination("Plitvice Lakes", "Croatia", "lake", "photo-1599946347372-9b8a791b9a1d"),
					destination("Patagonia", "Argentina", "region", "photo-1516026672322-bc52d61a55d5"),
					destination("Prague", "Czech Republic", "city", "photo-1541849546449-79d4d1b6085b"),
					destination("Atlas Mountains", "Morocco", "mountain", "photo-1489749791425-4a977b313225"),
					destination("Kyoto", "Japan", "city", "photo-1493976040374-85c8e78512ef"),
					destination("Barcelona", "Spain", "city", "photo-1583422409516-2895a77efded"));
		};

		return seasonal.stream().limit(request.limit()).toList();
	}

	private static PopularDestinationSuggestion destination(
			String name,
			String country,
			String placeType,
			String unsplashPhotoId) {
		String photoUrl = UNSPLASH + "/" + unsplashPhotoId + "?ixlib=rb-4.1.0&auto=format&fit=crop&w=720&q=80";
		return new PopularDestinationSuggestion(name, country, placeType, photoUrl);
	}
}
