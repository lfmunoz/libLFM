package com.lfmunoz.web

import com.lfmunoz.bash.BashService
import com.lfmunoz.flink.web.*
import com.lfmunoz.web.actions.test.TestAction
import com.lfmunoz.web.http.HttpVerticle
import com.lfmunoz.web.ws.OurWSConnection
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.http.ServerWebSocket

// GLOBAL
val actionsMap = hashMapOf<Int, ActionInterface>(
  WsPacketType.TEST.id to TestAction()
)
val bashService = BashService()

// ________________________________________________________________________________
// MAIN
// ________________________________________________________________________________
fun main() {
  val vertxOpts = VertxOptions()
  val vertx = Vertx.vertx(vertxOpts)
  val aAppConfig = AppConfig()
  println(aAppConfig)

  startWs(vertx, aAppConfig)
  startHttp(vertx, aAppConfig)

}

// ________________________________________________________________________________
// HELPER METHODS
// ________________________________________________________________________________
private fun startWs(vertx: Vertx, aAppConfig: AppConfig) {
  val aWebSocketVerticle = WebSocketVerticle(aAppConfig.wsPort, ::connectionFactory)
  vertx.deployVerticle(aWebSocketVerticle) {
    if (it.succeeded()) {
      println("WS deployment id=${it.result()}")
    } else {
      println("Deployment failed! - ${it.cause()}")
    }
  }
}

private fun startHttp(vertx: Vertx, aAppConfig: AppConfig) {
  val aHttpVerticle = HttpVerticle(aAppConfig)
  vertx.deployVerticle(aHttpVerticle) {
    if (it.succeeded()) {
      println("HTTP deployment id=${it.result()}")
    } else {
      println("HTTP deployment failed! - ${it.cause()}")
    }
  }
}

private fun connectionFactory(vertx: Vertx, socket: ServerWebSocket): Handler<String> {
  return OurWSConnection(vertx, socket, actionsMap)
}
