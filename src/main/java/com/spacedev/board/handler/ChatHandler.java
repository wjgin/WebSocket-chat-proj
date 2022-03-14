package com.spacedev.board.handler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ChatHandler extends TextWebSocketHandler {

	private static List<String> onlineList = new ArrayList<>();	// clients list online
	private static List<WebSocketSession> sessionList = new ArrayList<>(); // clients's session list
	Map<String, WebSocketSession> userSession = new HashMap<>();

	ObjectMapper json = new ObjectMapper();

	// message
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

		// json test
		Map<String, Object> dataMap = new HashMap<>();

		// master status
		String masterStatus = null;
		if (userSession.containsKey("master")) {
			masterStatus = "online";
		} else {
			masterStatus = "offline";
		}

		// sending time
		LocalDateTime currentTime = LocalDateTime.now();
		String time = currentTime.format(DateTimeFormatter.ofPattern("hh:mm a, E"));

		// message data
		String senderId = (String) session.getAttributes().get("sessionId");
		String payload = message.getPayload();

		// message parsing to json
		dataMap = jsonToMap(payload);
		dataMap.put("senderId", senderId);
		dataMap.put("time", time);
		dataMap.put("masterStatus", masterStatus);
		dataMap.put("onlineList", onlineList);

		String receiverId = (String) dataMap.get("receiverId");

		log.info("final dataMap >>> " + dataMap);

		// send a message
		String msg = json.writeValueAsString(dataMap);
		if (userSession.get(receiverId) != null) { // send a message to receiver
			userSession.get(receiverId).sendMessage(new TextMessage(msg));
		}

		// send a message myself
		if(!senderId.equals(receiverId)) {	// send a message to myself
			dataMap.put("receiverId", senderId);
			msg = json.writeValueAsString(dataMap);
			session.sendMessage(new TextMessage(msg));
		}
	}

	// connection established
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {

		// save session into list
		String senderId = (String) session.getAttributes().get("sessionId");
		sessionList.add(session);	// add online session inton list
		log.info("sessionId >>> " + senderId);
		onlineList.add(senderId);	// add online session id to list
		userSession.put(senderId, session);	// add user session into map

		// as master, send message to all online client
		if (senderId.equals("master")) {
			TextMessage msg = new TextMessage(senderId + " 님이 접속했습니다.");
			sendToAll(msg, senderId);

		} else {	// as client, send a connection message to master(host)
			Map<String, Object> data = new HashMap<>();	// message data as Map
			data.put("message", senderId + "님이 접속하셨습니다.");
			data.put("receiverId", "master");
			data.put("newOne", senderId);

			// send a message only master
			TextMessage msgToMaster = new TextMessage(json.writeValueAsString(data));
			handleMessage(session, msgToMaster);
		}

		log.info(session + " client connected");
	}

	// connection closed
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

		String senderId = (String) session.getAttributes().get("sessionId");
		// remove session data into all lists
		sessionList.remove(session);
		onlineList.remove(senderId);
		userSession.remove(senderId);
		
		// as master send to all
		if (senderId.equals("master")) {
			TextMessage msg = new TextMessage(senderId + " 님이 퇴장했습니다.");
			sendToAll(msg, senderId);
			
		} else { // as client, send a off message to master(host)
			Map<String, Object> data = new HashMap<>();
			data.put("message", senderId + "님이 퇴장하셨습니다.");
			data.put("receiverId", "master");
			data.put("outOne", senderId );

			// send a message only master
			TextMessage msg = new TextMessage(json.writeValueAsString(data));
			handleMessage(session, msg);
		}

		log.info(session + "client disconnected");
	}


	// send a message to all
	public void sendToAll(TextMessage message, String senderId) throws Exception {
		// map about message data
		Map<String, Object> dataMap = new HashMap<>();

		// master status as client view
		String masterStatus = null;
		if (userSession.containsKey("master")) {
			masterStatus = "online";
		} else {
			masterStatus = "offline";
		}

		// sending time
		LocalDateTime currentTime = LocalDateTime.now();
		String time = currentTime.format(DateTimeFormatter.ofPattern("hh:mm a E"));

		// message data
		String payload = message.getPayload();

		log.info("payload >>> " + payload);

		// add message data map
		dataMap.put("message", message.getPayload());
		dataMap.put("senderId", senderId);
		dataMap.put("time", time);
		dataMap.put("masterStatus", masterStatus);
		dataMap.put("onlineList", onlineList);	// online client list
		dataMap.put("newOne", "master");
	
		String receiverId = (String) dataMap.get("receiverId");

		log.info("final dataMap >>> " + dataMap);

		// send a message
		log.info("receiver session >>> " + userSession.get(receiverId));

		// send a message all online client -> need change as ansynx 동시성 문제 해결 요망
		for (String r : userSession.keySet()) {
			dataMap.put("receiverId", r);
			String msg = json.writeValueAsString(dataMap);
			userSession.get(r).sendMessage(new TextMessage(msg));
		}
	}

	// json string parsing to map
	public Map<String, Object> jsonToMap(String jsonString) throws JsonMappingException, JsonProcessingException {
		Map<String, Object> map = new HashMap<>();
		ObjectMapper jmapper = new ObjectMapper();
		map = jmapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {
		});

		return map;
	}
}
