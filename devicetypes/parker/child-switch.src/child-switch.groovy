/**
 *  Child Switch
 *
 *  2021 Matt Parker
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
 *  Change History:
 *
 *    Date        Who            What
 *    ----        ---            ----
 *    2020-01-25  Matt P         Original Creation (based on cpde from Dan O Arduino code)
 *
 * 
 */
metadata {
	definition (name: "Child Switch", namespace: "parker", author: "Matt", ocfDeviceType: "oic.d.switch", vid: "generic-light-switch") {
		capability "Switch"
//		capability "Actuator"
	}

}

def on() {
    sendEvent(name: "switch", value: "on")
	sendData("on")
}

def off() {
    sendEvent(name: "switch", value: "off")
	sendData("off")
}

def sendData(String value) {
    def name = device.deviceNetworkId.split("-")[-1]
    log.debug "${name} ${value}"
    parent.sendData(name, value)
}

def parse(String value) {
    log.debug "parse(${value}) called"
//	def parts = description.split(" ")
//    def name  = parts.length>0?parts[0].trim():null
//    def value = parts.length>1?parts[1].trim():null
//    if (name && value) {
        sendEvent(name: "switch", value: value)
//    }
 //   else {
  //  	log.debug "Missing either name or value.  Cannot parse!"
 //   }
}

def installed() {
}