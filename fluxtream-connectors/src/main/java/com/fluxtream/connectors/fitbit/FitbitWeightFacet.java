package com.fluxtream.connectors.fitbit;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.hibernate.search.annotations.Indexed;

import com.fluxtream.connectors.annotations.ObjectTypeSpec;
import com.fluxtream.domain.AbstractFloatingTimeZoneFacet;

/**
 * 
 * @author Candide Kemmler (candide@fluxtream.com)
 */
@Entity(name = "Facet_FitbitWeight")
@ObjectTypeSpec(name = "weight", value = 8, extractor = FitbitWeightFacetExtractor.class, prettyname = "Weight", isDateBased = true)
@NamedQueries({
		@NamedQuery(name = "fitbit.weight.byDate", query = "SELECT facet FROM Facet_FitbitWeight facet WHERE facet.guestId=? AND facet.date=?"),
		@NamedQuery(name = "fitbit.weight.byStartEnd", query = "SELECT facet FROM Facet_FitbitWeight facet WHERE facet.guestId=? AND facet.start=? AND facet.end=?"),
		@NamedQuery(name = "fitbit.weight.newest", query = "SELECT facet FROM Facet_FitbitWeight facet WHERE facet.guestId=? and facet.isEmpty=false ORDER BY facet.end DESC LIMIT 1"),
		@NamedQuery(name = "fitbit.weight.oldest", query = "SELECT facet FROM Facet_FitbitWeight facet WHERE facet.guestId=? and facet.isEmpty=false ORDER BY facet.start ASC LIMIT 1"),
		@NamedQuery(name = "fitbit.weight.deleteAll", query = "DELETE FROM Facet_FitbitWeight facet WHERE facet.guestId=?"),
		@NamedQuery(name = "fitbit.weight.between", query = "SELECT facet FROM Facet_FitbitWeight facet WHERE facet.guestId=? AND facet.start>=? AND facet.end<=? and facet.isEmpty=false"),
		@NamedQuery(name = "fitbit.weight.byDates", query = "SELECT facet FROM Facet_FitbitWeight facet WHERE facet.guestId=? AND facet.date IN ?") })
@Indexed
public class FitbitWeightFacet extends AbstractFloatingTimeZoneFacet {

	public double bmi;
	public double fat;
	public double weight;

	@Override
	protected void makeFullTextIndexable() {
	}

}
