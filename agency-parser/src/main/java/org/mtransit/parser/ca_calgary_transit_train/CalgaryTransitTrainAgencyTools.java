package org.mtransit.parser.ca_calgary_transit_train;

import static org.mtransit.commons.RegexUtils.ANY;
import static org.mtransit.commons.RegexUtils.BEGINNING;
import static org.mtransit.commons.RegexUtils.WORD_CAR;
import static org.mtransit.commons.RegexUtils.atLeastOne;
import static org.mtransit.commons.RegexUtils.group;
import static org.mtransit.commons.StringUtils.EMPTY;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.Cleaner;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.mt.data.MAgency;

import java.util.Locale;

// https://data.calgary.ca/Transportation-Transit/Calgary-Transit-Scheduling-Data/npk7-z3bj/about_data
public class CalgaryTransitTrainAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new CalgaryTransitTrainAgencyTools().start(args);
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "Calgary Transit";
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_LIGHT_RAIL;
	}

	@Override
	public boolean defaultRouteIdEnabled() {
		return true;
	}

	@Override
	public boolean useRouteShortNameForRouteId() {
		return false; // used GTFS-RT
	}

	@Nullable
	@Override
	public String getRouteIdCleanupRegex() {
		return "\\-\\d+$";
	}

	@Override
	public boolean verifyRouteIdsUniqueness() {
		returh true; // merge routes
	}

	private static final Cleaner RLN_STARTS_WITH_RSN = new Cleaner(
			group(atLeastOne(WORD_CAR)) + " line \\- " + atLeastOne(ANY),
			"$1",
			true
	);

	@Override
	public @NotNull String getRouteShortName(@NotNull GRoute gRoute) {
		return RLN_STARTS_WITH_RSN.clean(gRoute.getRouteLongNameOrDefault());
	}

	private static final int RSN_RED = 201;
	private static final int RSN_BLUE = 202;

	@Override
	public boolean defaultRouteLongNameEnabled() {
		return true;
	}

	private static final Cleaner RLN_STARTS_WITH_RSN_LINE_DASH_ = new Cleaner(
			BEGINNING + atLeastOne(WORD_CAR) + " line \\- ",
			EMPTY, true);

	private static final Cleaner CTRAIN_ = new Cleaner(CleanUtils.cleanWord("ctrain"), EMPTY);

	@NotNull
	@Override
	public String cleanRouteLongName(@NotNull String routeLongName) {
		routeLongName = CleanUtils.cleanSlashes(routeLongName);
		routeLongName = RLN_STARTS_WITH_RSN_LINE_DASH_.clean(routeLongName);
		routeLongName = CTRAIN_.clean(routeLongName);
		return super.cleanRouteLongName(routeLongName);
	}

	@Override
	public boolean defaultAgencyColorEnabled() {
		return true;
	}

	private static final String AGENCY_COLOR_RED = "B83A3F"; // LIGHT RED (from web site CSS)

	private static final String AGENCY_COLOR = AGENCY_COLOR_RED;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final String COLOR_RED_LINE = "EE2622"; // RED (from PDF map)
	private static final String COLOR_BLUE_LINE = "0F4076"; // BLUE (from PDF map)

	@Override
	public @Nullable String provideMissingRouteColor(@NotNull GRoute gRoute) {
		final int rsn = Integer.parseInt(gRoute.getRouteShortName());
		switch (rsn) {
		// @formatter:off
		case RSN_RED: return COLOR_RED_LINE;
		case RSN_BLUE: return COLOR_BLUE_LINE;
		// @formatter:on
		default:
			throw new MTLog.Fatal("Unexpected route color for %s!", gRoute);
		}
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, tripHeadsign, getIgnoredWords());
		tripHeadsign = CLEAN_AT_SPACE.clean(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private String[] getIgnoredWords() {
		return new String[]{
				"AM", "PM",
				"EB", "WB", "NB", "SB",
				"SE", "SW", "NE", "NW",
				"LRT", "YYC", "TRW", "MRU", "SAIT", "JG", "EEEL",
				"AUArts", "CTrain",
		};
	}

	private static final Cleaner ENDS_WITH_C_TRAIN_STATION = new Cleaner(
			"( (ctrain )?stat?ion$)",
			EMPTY,
			true);

	private static final Cleaner CLEAN_AT_SPACE = new Cleaner(
			"(\\w)\\s*@\\s*(\\w)",
			"$1 @ $2"
	);

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, gStopName, getIgnoredWords());
		gStopName = ENDS_WITH_C_TRAIN_STATION.clean(gStopName);
		gStopName = CLEAN_AT_SPACE.clean(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@Override
	public int getStopId(@NotNull GStop gStop) {
		return super.getStopId(gStop); // used for GTFS-RT
	}
}
