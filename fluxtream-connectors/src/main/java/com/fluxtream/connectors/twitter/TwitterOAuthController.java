package com.fluxtream.connectors.twitter;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fluxtream.Configuration;
import com.fluxtream.auth.AuthHelper;
import com.fluxtream.connectors.Connector;
import com.fluxtream.domain.Guest;
import com.fluxtream.services.GuestService;
import com.google.gdata.client.authn.oauth.OAuthException;

@Controller
@RequestMapping(value = "/twitter")
public class TwitterOAuthController {

	@Autowired
	GuestService guestService;

	@Autowired
	Configuration env;
	private static final String TWITTER_OAUTH_CONSUMER = "twitterOAuthConsumer";
	private static final String TWITTER_OAUTH_PROVIDER = "twitterOAuthProvider";

	@RequestMapping(value = "/token")
	public String getToken(HttpServletRequest request, HttpServletResponse response) throws IOException,
			ServletException, OAuthMessageSignerException, OAuthNotAuthorizedException,
			OAuthExpectationFailedException, OAuthCommunicationException {

		String oauthCallback = env.get("homeBaseUrl") + "twitter/upgradeToken";
		if (request.getParameter("guestId") != null)
			oauthCallback += "?guestId=" + request.getParameter("guestId");

		OAuthConsumer consumer = new DefaultOAuthConsumer(getConsumerKey(), getConsumerSecret());

		HttpClient httpClient = env.getHttpClient();

		OAuthProvider provider = new CommonsHttpOAuthProvider("https://api.twitter.com/oauth/request_token",
				"https://api.twitter.com/oauth/access_token", "https://api.twitter.com/oauth/authorize", httpClient);

		request.getSession().setAttribute(TWITTER_OAUTH_CONSUMER, consumer);
		request.getSession().setAttribute(TWITTER_OAUTH_PROVIDER, provider);
		System.out.println("the token secret is: " + consumer.getTokenSecret());

		String approvalPageUrl = provider.retrieveRequestToken(consumer, oauthCallback);

		return "redirect:" + approvalPageUrl;
	}

	@RequestMapping(value = "/upgradeToken")
	public String upgradeToken(HttpServletRequest request, HttpServletResponse response) throws OAuthException,
			OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException,
			OAuthCommunicationException {
		OAuthConsumer consumer = (OAuthConsumer) request.getSession().getAttribute(TWITTER_OAUTH_CONSUMER);
		OAuthProvider provider = (OAuthProvider) request.getSession().getAttribute(TWITTER_OAUTH_PROVIDER);
		String verifier = request.getParameter("oauth_verifier");
		provider.retrieveAccessToken(consumer, verifier);
		Guest guest = AuthHelper.getGuest();

		guestService.setApiKeyAttribute(guest.getId(), Connector.getConnector("TWITTER"), "accessToken",
				consumer.getToken());
		guestService.setApiKeyAttribute(guest.getId(), Connector.getConnector("TWITTER"), "tokenSecret",
				consumer.getTokenSecret());

		return "redirect:/analyze/from/" + Connector.getConnector("TWITTER").getName();
	}

	String getConsumerKey() {
		return env.get("twitterConsumerKey");
	}

	String getConsumerSecret() {
		return env.get("twitterConsumerSecret");
	}

}
