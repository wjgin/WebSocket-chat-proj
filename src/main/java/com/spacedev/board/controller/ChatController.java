package com.spacedev.board.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.log4j.Log4j2;

@Controller
@Log4j2
public class ChatController {

	@GetMapping("/chat")	// client uri
	public void getChat(HttpServletRequest request, @RequestParam String id) {

		HttpSession session = request.getSession();

		if (id.equals("guest")) {
			// make random nickname using sessionid
			String name = "guest" + session.toString().substring(session.toString().indexOf("@"));
			session.setAttribute("sessionId", name);
		} else if(id.equals("master")) {
			String name = "master";
			session.setAttribute("sessionId", name);
		}

		log.info("@ChatController, getChat()");
	}

	@GetMapping("/chat/master")	// master uri
	public String enterChatAsMaster(HttpServletRequest request) {

		log.info("@ChatController, getChat()");
		return "/chat";
	}
}
