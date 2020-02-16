/**
 *  RoomMe
 *
 *  Copyright 2020 Intellithings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  ***************************************************************************************************************************
 *  *********************** You must enable oAuth and provide client ID and client secret to RoomMe ***************************
 *  ******************************************** http://intellithings.net/hubitat *********************************************
 *  ***************************************************************************************************************************
 */

definition(
    name: "RoomMe",
    namespace: "Intellithings.net",
    author: "Intellithings",
    description: "RoomMe by Intellithings is the first smart home Personal Location Sensor (PLS) that leverages patented presence sensing technology to make room-to-room, person-specific smart home automations a reality.\r\n\r\nRoomMe is not a replacement for your smart home system\u2014instead, it exists as an additional layer over your installed smart home devices.\r\nBy identifying the person in the room using the unique Bluetooth signature of a matched smartphone, RoomMe adds a layer of intelligence that is lacking in smart home devices\u2014the ability to sense who is and isn\u2019t in each room to trigger personalized smart home scenes for lighting, music, comfort, and lifestyle simplicity.\r\n\r\nRead the full story at http://www.getroomme.com",
    category: "SmartThings Labs",
    iconUrl: "https://roomme-icons.s3.amazonaws.com/appicon_small.png",
    iconX2Url: "https://roomme-icons.s3.amazonaws.com/app_icon_120x120.png",
    iconX3Url: "https://roomme-icons.s3.amazonaws.com/app_icon_120x120.png",
    oauth: [displayName: "RoomMe REST API", displayLink: ""]
)
​
mappings {
  path("/devices") {
    action: [
        GET: "listDevices"
    ]
  }
  path("/device/:id") {
    action: [
        GET: "deviceDetails"
    ]
  }
  path("/device/:id/attribute/:name") {
    action: [
        GET: "deviceGetAttributeValue"
    ]
  }
  path("/device/:id/command/:name") {
    action: [
        POST: "deviceCommand"
    ]
  }
}

preferences {
  section() {
    input "devices", "capability.actuator", title: "Devices", multiple: true
    input "devices", "capability.polling", title: "Devices", multiple: true
  }
}

def installed() {
  log.debug "Installed with settings: ${settings}"
  initialize()
}

def updated() {
  log.debug "Updated with settings: ${settings}"
  unsubscribe()
  initialize()
}

def initialize() {
}

def listDevices() {
  def resp = []
  devices.each {
  
    def supportedAttributes = []
  it.supportedAttributes.each {
    supportedAttributes << it.name
  }

  def supportedCommands = []
  it.supportedCommands.each {
    def arguments = []
    it.arguments.each { arg ->
      arguments << "" + arg
    }
    supportedCommands << [
        name: it.name,
        arguments: arguments
    ]
  }
  
    resp << [
        id: it.id,
        label: it.label,
        manufacturerName: it.manufacturerName,
        modelName: it.modelName,
        name: it.name,
        displayName: it.displayName,
     	capabilities: supportedAttributes,
        supportedCommands: supportedCommands
    ]
  }
  return resp
}

def deviceDetails() {
  def device = getDeviceById(params.id)

  def supportedAttributes = []
  def state = []
  device.supportedAttributes.each {
    supportedAttributes << it.name
    state << [
    name: it.name,
    value: device.currentValue(it.name)]
  }  


  /*def supportedCommands = []
  device.supportedCommands.each {
    def arguments = []
    it.arguments.each { arg ->
      arguments << "" + arg
    }
    supportedCommands << [
        name: it.name,
        arguments: arguments
    ]
  }*/

  return [
      id: device.id,
      label: device.label,
      manufacturerName: device.manufacturerName,
      modelName: device.modelName,
      name: device.name,
      displayName: device.displayName,
      supportedAttributes: supportedAttributes,
      states: state
      //,supportedCommands: supportedCommands
  ]
}

def deviceGetAttributeValue() {
  def device = getDeviceById(params.id)
  def name = params.name
  def value = device.currentValue(name);
  return [
      value: value
  ]
}

def deviceCommand() {
  def device = getDeviceById(params.id)
  log.debug "device: ${device}"

  def name = params.name
  
  if (name == "setLightState") {
    log.debug "dimmer: ${params.dimmer}"
    log.debug "hue: ${params.hue}"
    log.debug "saturation: ${params.saturation}"
  
  	def dimmer = params.dimmer
  	def hue = params.hue
  	def saturation = params.saturation
    
    if (dimmer != null) {
    	device.setLevel(dimmer.toInteger())
    }
    
    if (hue != null) {
    	device.setHue(hue.toInteger())
    }
     
    if (device.hasCapability(saturation)) {
        if (saturation != null) {
        	device.setSaturation(saturation.toInteger())
        }      
    }
  } else if (name == "setTemperature") {
  	def temperature = params.temperature
    log.debug "temperature: ${params.temperature}"

    if (temperature != null) {
    	device.setHeatingSetpoint(temperature - 2)
    	device.setCoolingSetpoint(temperature)
    }
  } else {
 	 def args = params.arg

 	 if (args == null) {
 	   args = []
 	 } else if (args instanceof String) {
  	  args = [args]
  	 }
    
  	log.debug "device command: ${name} ${args}"     

      if (args.size == 0) {
          device."$name"()
      } else if (args.size == 1) {
          if (args[0].isNumber()) {
           device."$name"(args[0].toInteger())
        } else {
           device."$name"(args[0])
        }
      } else if (args.size == 2) {
          device."$name"(args[0], args[1])
      } else {
          log.debug "Unhandled number of args"
      }
  }
}

def getDeviceById(id) {
  return devices.find { it.id == id }
}