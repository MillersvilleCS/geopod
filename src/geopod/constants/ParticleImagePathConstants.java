package geopod.constants;

import java.util.Arrays;
import java.util.List;

public final class ParticleImagePathConstants
{
	public static final List<String> DENDRITES;
	public static final List<String> NEEDLES;
	public static final List<String> HOLLOW_COLUMNS;
	public static final List<String> SECTORED_PLATES;
	public static final List<String> DOUBLE_SPLIT_PLATES;
	public static final List<String> THIN_SMALL_PLATES;
	public static final String LIQUID;
	public static final String DEFAULT;

	static
	{
		DENDRITES = Arrays.asList ("//Resources/Images/Particles/Dendrites/Stellar_Dendrites/Stellar00.jpg",
				"//Resources/Images/Particles/Dendrites/Stellar_Dendrites/Stellar01.jpg",
				"//Resources/Images/Particles/Dendrites/Stellar_Dendrites/Stellar02.jpg",
				"//Resources/Images/Particles/Dendrites/Fernlike_Stellar_Dendrite/Fern00.jpg",
				"//Resources/Images/Particles/Dendrites/Fernlike_Stellar_Dendrite/Fern01.jpg",
				"//Resources/Images/Particles/Dendrites/Fernlike_Stellar_Dendrite/Fern02.jpg");

		NEEDLES = Arrays.asList ("//Resources/Images/Particles/Needles/needle00.jpg",
				"//Resources/Images/Particles/Needles/needle01.jpg");

		HOLLOW_COLUMNS = Arrays.asList ("//Resources/Images/Particles/Columns/Hollow_Columns/hc00.jpg",
				"//Resources/Images/Particles/Columns/Hollow_Columns/hc01.jpg",
				"//Resources/Images/Particles/Columns/Hollow_Columns/hc02.jpg",
				"//Resources/Images/Particles/Columns/Hollow_Columns/hc03.jpg");

		SECTORED_PLATES = Arrays.asList ("//Resources/Images/Particles/Plates/Sectored_Plates/sectored00.jpg",
				"//Resources/Images/Particles/Plates/Sectored_Plates/sectored01.jpg",
				"//Resources/Images/Particles/Plates/Sectored_Plates/sectored02.jpg",
				"//Resources/Images/Particles/Plates/Sectored_Plates/sectored03.jpg",
				"//Resources/Images/Particles/Plates/Sectored_Plates/sectored04.jpg",
				"//Resources/Images/Particles/Plates/Sectored_Plates/sectored05.jpg",
				"//Resources/Images/Particles/Plates/Sectored_Plates/sectored06.jpg",
				"//Resources/Images/Particles/Plates/Sectored_Plates/sectored07.jpg");

		DOUBLE_SPLIT_PLATES = Arrays.asList (
				"//Resources/Images/Particles/Plates/Double_Split_Plates/double_plate00.jpg",
				"//Resources/Images/Particles/Plates/Double_Split_Plates/split_plate00.jpg");

		THIN_SMALL_PLATES = Arrays.asList ("//Resources/Images/Particles/Plates/Thin_Small_Plates/thin_plate00.jpg",
				"//Resources/Images/Particles/Plates/Thin_Small_Plates/thin_plate01.jpg");

		LIQUID = "//Resources/Images/Particles/Liquid/Liquid00.png";

		DEFAULT = "//Resources/Images/Particles/defaultImage.jpg";
	}

	private ParticleImagePathConstants ()
	{
		// Static final class. No instantiation.
	}
}
