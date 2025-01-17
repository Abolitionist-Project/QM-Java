package com.fluxtream.auth;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fluxtream.domain.CoachingBuddy;
import com.fluxtream.domain.Guest;
import com.fluxtream.domain.SharedConnector;

public class AuthHelper {

	private static Map<Long, Set<CoachingBuddy>> viewees = new Hashtable<Long, Set<CoachingBuddy>>();

	public static long getGuestId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		long guestId = ((FlxUserDetails) auth.getPrincipal()).getGuest().getId();
		return guestId;
	}

	public static boolean isViewingGranted(String connectorName) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		final FlxUserDetails principal = (FlxUserDetails) auth.getPrincipal();
		if (principal.coachee == null)
			return true;
		else {
			for (SharedConnector sharedConnector : principal.coachee.sharedConnectors) {
				if (sharedConnector.connectorName.equals(connectorName))
					return true;
			}
			return false;
		}
	}

	public static void as(CoachingBuddy coachee) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		final FlxUserDetails principal = (FlxUserDetails) auth.getPrincipal();
		addViewee(principal.getGuest().getId(), coachee);
		principal.coachee = coachee;
	}

	private static void addViewee(final Long id, final CoachingBuddy coachee) {
		if (viewees.get(id) == null)
			;
		viewees.put(id, new HashSet<CoachingBuddy>());
		if (!viewees.get(id).contains(coachee))
			viewees.get(id).add(coachee);
	}

	/**
	 * This is called by coachingService when a coachee no longer wants to be
	 * coached by some coach
	 * 
	 * @param id
	 *            coach id
	 * @param coachee
	 *            The user who just revoked the coach
	 */
	public static void revokeCoach(final Long id, final CoachingBuddy coachee) {
		final Set<CoachingBuddy> buddies = viewees.get(id);
		if (buddies == null)
			return;
		CoachingBuddy toRemove = null;
		for (CoachingBuddy buddy : buddies) {
			if (buddy.getId().equals(coachee.getId())) {
				toRemove = buddy;
				break;
			}
		}
		buddies.remove(toRemove);
	}

	public static long getVieweeId() throws CoachRevokedException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		final FlxUserDetails principal = (FlxUserDetails) auth.getPrincipal();
		if (principal.coachee == null)
			return principal.getGuest().getId();
		else {
			final Set<CoachingBuddy> guestsCoachees = viewees.get(principal.getGuest().getId());
			if (guestsCoachees.contains(principal.coachee))
				return principal.coachee.guestId;
			else {
				principal.coachee = null;
				throw new CoachRevokedException();
			}
		}
	}

	public static CoachingBuddy getCoachee() throws CoachRevokedException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		final FlxUserDetails principal = (FlxUserDetails) auth.getPrincipal();
		if (principal.coachee != null)
			if (viewees.get(principal.getGuest().getId()).contains(principal.coachee))
				return principal.coachee;
			else {
				principal.coachee = null;
				throw new CoachRevokedException();
			}
		return null;
	}

	public static Guest getGuest() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null)
			return null;
		Guest guest = ((FlxUserDetails) auth.getPrincipal()).getGuest();
		return guest;
	}
}
