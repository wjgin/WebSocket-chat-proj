# [개인 프로젝트] 포트폴리오 공개 사이트 
 
> 자신의 포트폴리오를 기록, 공개하는 웹사이트 입니다.

> 포트폴리오를 확인하는 게스트와 실시한 소통 혹은 메모를 받는 것에 목적을 둡니다.

> Spring-boot, WebSocket, JPA를 적용해 구현했습니다. 

> 개발 기간: 2021-11-12 ~ 2021-11-24

> [참고 블로그](https://reinvestment.tistory.com/57?category=910568)

## 개발환경
* spring-boot
* java
* javascript
* mysql
* jpa
* html
* css
* tomcat

## 주요기능
  * 게스트의 메모 남기기 CU
  * 다중 게스트와 호스트 간의 실시간 1:1 채팅

## 추가 고려사항
* 접속한 게스트에게 동일시간 채팅 구현
  * 실시간 채팅의 경우, 호스트가 접속한다면, 접속한 모든 게스트에게 순차적으로 메세지 전송을 합니다.
  모든 게스트에게 동시에 메세지를 보낼 필요가 있다면 쓰레드 사용법을 고려해야할 것 입니다.

