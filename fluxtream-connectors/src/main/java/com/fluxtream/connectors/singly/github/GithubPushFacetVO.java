package com.fluxtream.connectors.singly.github;

import java.util.Date;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.fluxtream.TimeInterval;
import com.fluxtream.connectors.vos.AbstractInstantFacetVO;
import com.fluxtream.domain.GuestSettings;

public class GithubPushFacetVO extends AbstractInstantFacetVO<GithubPushFacet> {

	public String repoName;
	public String repoURL;

	@Override
	protected void fromFacet(final GithubPushFacet facet, final TimeInterval timeInterval, final GuestSettings settings) {
		startMinute = toMinuteOfDay(new Date(facet.start), timeInterval.timeZone);
		this.start = facet.start;
		this.repoName = facet.repoName;
		this.repoURL = facet.repoURL;
		JSONArray jsonCommits = JSONArray.fromObject(facet.commitsJSON);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < jsonCommits.size(); i++) {
			JSONObject jsonObject = jsonCommits.getJSONObject(i);
			if (i > 0)
				sb.append(", ");
			sb.append(jsonObject.getString("message"));
		}
		this.description = sb.toString();
	}

}
