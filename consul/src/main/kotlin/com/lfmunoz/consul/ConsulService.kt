package com.lfmunoz.consul

import com.ecwid.consul.v1.ConsulClient
import org.fissore.slf4j.FluentLoggerFactory


class ConsulService(
  private val rootPath: String,
  private val instanceId: String,
  private val consul: ConsulClient
) {
  companion object {
    private val log = FluentLoggerFactory.getLogger(ConsulService::class.java)
  }


  /*
  private fun saveGlobalConfig(globalConfig: ApplicationConfigDTO) : Boolean {
    val key = "$rootPath"
    log.info().log("[saving global config] - $key : $globalConfig")
    val resp = consul.setKVValue(key, globalConfig.toJson())
    return resp.value
  }

  private fun saveInstanceConfig(globalConfig: ApplicationConfigDTO) : Boolean {
    val key = "$rootPath.${instanceId}"
    val instanceConfig = InstanceConfigDto(globalConfig.isActive, globalConfig.localAddressPrefix)
    log.info().log("[saving instance config] - $key : $instanceConfig")
    val resp = consul.setKVValue(key, instanceConfig.toJson())
    return resp.value
  }

  fun retrieveConfig() : ApplicationConfigDTO {
    val globalKey = "$rootPath"
    val instanceKey = "$rootPath.${instanceId}"
    val globalConfig = consul.getKVValue(globalKey)
    val instanceConfig = consul.getKVValue(instanceKey)
    return ApplicationConfigDTO.fromJson(globalConfig.value.decodedValue).apply {
      val consulInstanceConfig = ApplicationConfigDTO.fromJson(instanceConfig.value.decodedValue)
      isActive = consulInstanceConfig.isActive
      localAddressPrefix = consulInstanceConfig.localAddressPrefix
    }
  }
   */

}
