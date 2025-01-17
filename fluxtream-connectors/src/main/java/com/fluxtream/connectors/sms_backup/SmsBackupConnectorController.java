package com.fluxtream.connectors.sms_backup;

import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.fluxtream.auth.AuthHelper;
import com.fluxtream.connectors.Connector;
import com.fluxtream.connectors.updaters.UpdateInfo.UpdateType;
import com.fluxtream.services.ConnectorUpdateService;
import com.fluxtream.services.GuestService;

@Controller()
@RequestMapping("/smsBackup")
public class SmsBackupConnectorController {

	@Autowired
	GuestService guestService;

	@Autowired
	ConnectorUpdateService connectorUpdateService;

	@RequestMapping(value = "/enterCredentials")
	public ModelAndView signin(HttpServletRequest request) {
		ModelAndView mav = new ModelAndView("connectors/smsBackup/enterCredentials");
		return mav;
	}

	@RequestMapping("/check")
	public ModelAndView check(@RequestParam("username") String email, @RequestParam("password") String password,
			HttpServletRequest request) throws MessagingException {
		List<String> required = new ArrayList<String>();
		email = email.trim();
		password = password.trim();
		request.setAttribute("username", email);
		if (email.equals(""))
			required.add("username");
		if (password.equals(""))
			required.add("password");
		if (required.size() != 0) {
			request.setAttribute("required", required);
			return new ModelAndView("connectors/smsBackup/enterCredentials");
		}
		ModelAndView mav = new ModelAndView("connectors/smsBackup/setFolderNames");
		long guestId = AuthHelper.getGuestId();
		boolean worked = false;
		try {
			worked = (new SmsBackupHelper(email, password)).testConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (worked) {
			guestService.setApiKeyAttribute(guestId, Connector.getConnector("SMS_BACKUP"), "username", email);
			guestService.setApiKeyAttribute(guestId, Connector.getConnector("SMS_BACKUP"), "password", password);
			return mav;
		} else {
			request.setAttribute("errorMessage", "Sorry, you must have entered wrong credentials.\nPlease try again.");
			return new ModelAndView("connectors/smsBackup/enterCredentials");
		}
	}

	@RequestMapping("/setFolderNames")
	public ModelAndView setFolderNames(@RequestParam("smsFolderName") String smsFolderName,
			@RequestParam("callLogFolderName") String callLogFolderName, HttpServletRequest request)
			throws MessagingException {

		List<String> required = new ArrayList<String>();
		smsFolderName = smsFolderName.trim();
		callLogFolderName = callLogFolderName.trim();
		request.setAttribute("smsFolderName", smsFolderName);
		request.setAttribute("callLogFolderName", callLogFolderName);
		if (smsFolderName.equals(""))
			required.add("smsFolderName");
		if (callLogFolderName.equals(""))
			required.add("callLogFolderName");
		if (required.size() != 0) {
			request.setAttribute("required", required);
			return new ModelAndView("connectors/smsBackup/enterCredentials");
		}

		ModelAndView mav = new ModelAndView("connectors/smsBackup/success");
		long guestId = AuthHelper.getGuestId();
		guestService.setApiKeyAttribute(guestId, Connector.getConnector("sms_backup"), "smsFolderName", smsFolderName);
		guestService.setApiKeyAttribute(guestId, Connector.getConnector("sms_backup"), "callLogFolderName",
				callLogFolderName);
		connectorUpdateService.scheduleUpdate(guestId, "sms_backup", 3, UpdateType.INITIAL_HISTORY_UPDATE,
				System.currentTimeMillis());
		return mav;
	}
}
