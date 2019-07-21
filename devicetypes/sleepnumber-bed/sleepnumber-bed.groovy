/**
 *  SleepNumber Bed Device
 *
 *  Copyright 2019 Peter Nealy
 *
 *  Derivative work evolved from:
 *  SleepIQ Presence Sensor
 *
 *  Copyright 2015 Nathan Jacobson
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 *  use this file except in compliance with the License. You may obtain a copy 
 *  of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 *  License for the specific language governing permissions and limitations 
 *  under the License.
 *
 */
metadata {
  definition (name: "SleepNumber Bed", namespace: "panealy", author: "Peter Nealy") {
    capability "Refresh"
    capability "Presence Sensor"
    capability "Switch"
    capability "Polling"
    capability "Switch Level"

    command "arrived"
    command "departed"
    
    attribute "bedId", "String"
    attribute "mode", "enum", ["Both", "Either", "Left", "Right"]
    attribute "sleepNumber", "number"
    attribute "sleepnum", "string"
    attribute "favSleepNumber", "number"
    attribute "favnum", "string"

    command "setStatus", ["string"]
    command "setBedId", ["string"]
    command "setMode", ["string"]
    command "setFavSleepNumber", ["number"]
    command "setSleepNumber", ["number"]
  }

  simulator {
    status "present": "presence: present"
    status "not present": "presence: not present"
	status "on": "switch: on"
    status "off": "switch: not off"
  }

/*
  preferences {
    section("Settings:") {
      input("mode", title: "Mode", "enum", required: false, defaultValue: "Either", options: ["Left", "Right", "Both", "Either"], description: "The side(s) of the bed to monitor")
    }
  }
*/

  tiles (scale: 2) {
    standardTile("presence", "device.presence", width: 6, height: 3, canChangeBackground: true) {
      state("not present", label:'not present', icon:"st.presence.tile.not-present", backgroundColor:"#ffffff", action:"arrived")
	  state("present", label:'present', icon:"st.presence.tile.present", backgroundColor:"#53a7c0", action:"departed")
    }
    
    valueTile("sleepNumLabel", "sleepnum", height: 1, width: 2) {
      state "default", label: "Sleep Number"
    }
    controlTile("sleepNumberSliderControl", "device.sleepNumber", "slider", height: 1, width: 3, inactiveLabel: false) {
      state "level", action:"setSleepNumber", label: 'SN'
    }
    valueTile("favLabel", "favnum", height: 1, width: 2) {
      state "default", label: "Favorite"
    }
    controlTile("favSleepNumberSliderControl", "device.favSleepNumber", "slider", height: 1, width: 3, inactiveLabel: false) {
      state "level", action:"setFavSleepNumber"
    }
    standardTile("refresh", "device.poll", inactiveLabel: false, decoration: "flat") {
      state "default", action:"polling.poll", icon:"st.secondary.refresh"
    }
    valueTile("bedId", "device.bedId", width: 3, height: 1) {
      state "default", label: '${currentValue}'
    }
    valueTile("mode", "device.mode", width: 1, height: 2) {
      state "default", label: '${currentValue} Side'
    }
    valueTile("sleepNumber", "device.sleepNumber", width: 1, height: 1) {
      state "default", label: '${currentValue}'
    }
    
    main "presence"
    details(["presence", "mode", "sleepNumLabel", "sleepNumberSliderControl", "favLabel", "favSleepNumberSliderControl", "refresh", "bedId"])
  }
}

def installed() {
  log.trace 'installed()'
}

def updated() {
  log.trace 'updated()'
}

def poll() {
  log.trace "poll()"
  parent.refreshChildDevices()
}

def refresh() {
  log.trace "refresh()"
  parent.refreshChildDevices()
}

def parse(String description) {
  log.trace "parse() - Description: ${description}"
  def results = []
  /*
  def pair = description.split(":")
  results = createEvent(name: pair[0].trim(), value: pair[1].trim())
  //results = createEvent(name: "contact", value: "closed", descriptionText: "$device.displayName is closed")
  */
  log.debug "parse() - Results: ${results.inspect()}"
  results
}

def arrived() {
  log.trace "arrived()"
  sendEvent(name: "presence", value: "present")
  sendEvent(name: "switch", value: "on")
}

def departed() {
  log.trace "departed()"
  sendEvent(name: "presence", value: "not present")
  sendEvent(name: "switch", value: "off")
}

def on() {
  log.trace "on()"
  arrived()
}

def off() {
  log.trace "off()"
  departed()
}

def setStatus(val) {
  log.trace "setStatus($val)"
  if (val) {
    arrived()
  } else {
    departed()
  }
}

def setBedId(val) {
  log.trace "setBedId($val)"
  state.bedId = val
  sendEvent(name: "bedId", value: val)
}

def setMode(val) {
  log.trace "setMode($val)"
  state.mode = val
  sendEvent(name: "mode", value: val)
}

def setFavSleepNumber(val) {
  log.trace "setFavSleepNumber($val)"
  
  setSleepNumber(val, true)
}

def setSleepNumber(val, favorite=false) {
  // convert to a multiple of 5
  val = Math.round(val/5)*5
  log.trace "setSleepNumber(${val}, ${favorite})"

  // only change the level if it is new
  def currentNumber = null
  if (favorite == true) {
    currentNumber = device.currentValue("favSleepNumber")
  } else {
    currentNumber = device.currentValue("sleepNumber")
  }
  if (currentNumber == val) {
    //log.trace "amazonCommandResp | Status: (${response?.status})${resp != null ? " | Response: ${resp}" : ""} | ${data?.cmdDesc} was Successfully Sent!!!"
	log.debug "setSleepNumber: current ${favorite ? "favorite" : ""} setting (${currentNumber}) same as new setting (${val}), skipping..."
    return
  }

  if (favorite == true) {
    sendEvent(name: "favSleepNumber", value: val)
  } else {
    sendEvent(name: "sleepNumber", value: val)
  }
  def currentMode = device.currentValue("mode")
  def currentBedId = device.currentValue("bedId")
  parent.doSetSleepNumber(val, currentMode, currentBedId, favorite)
}

def updateFavSleepNumber(val) {
    sendEvent(name: "favSleepNumber", value: val)
}

def updateSleepNumber(val) {
    sendEvent(name: "sleepNumber", value: val)
}
