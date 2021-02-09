/**
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
 */

metadata {
    definition (name: "3A Nue >2 Gang ZigBee 3.0 Switch", namespace: "parker", author: "Matt") {
//        capability "Actuator"
//        capability "Configuration"
        capability "Refresh"
        capability "Switch"
        capability "Health Check"
              
        fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "FeiBit", model: "FNB56-ZSW02LX2.0", deviceJoinName: "Nue Zigbee 2 Gang Switch"
        fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "FeiBit", model: "FNB56-SKT1DHG1.4", deviceJoinName: "Nue Zigbee Double GPO"
        fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0B05,0702", outClusters: "000A,0019",  manufacturer: "Feibit Inc co.", model: "FB56+ZSW1IKJ1.7", deviceJoinName: "Nue Double GPO"
        fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "3A Smart Home DE", model: "LXN-2S27LX1.0", deviceJoinName: "Nue Zigbee 2 Gang Switch"
	    fingerprint profileId: "C05E", inClusters: "0000, 0004, 0003, 0006, 0005, 1000, 0008", outClusters: "0006, 0008, 0019", manufacturer: "FeiBit", model: "FNB56-ZSW03LX2.0", deviceJoinName: "Nue 3 Gang Switch"
    	fingerprint profileId: "C05E", inClusters: "0000, 0004, 0003, 0006, 0005, 1000, 0008", outClusters: "0006, 0008, 0019", manufacturer: "3A Smart Home DE", model: "LXN-3S27LX1.0", deviceJoinName: "Nue 3 Gang Switch"
    	fingerprint profileId: "0104", inClusters: "0000, 0004, 0002, 0006, 0005, 1000, 0008", outClusters: "0006, 0008, 0000", manufacturer: "Feibit Inc co.", model: "FB56+ZSW1JKJ2.5", deviceJoinName: "Nue 4 Gang Switch"
    	fingerprint profileId: "0104", inClusters: "0000, 0004, 0002, 0006, 0005, 1000, 0008", outClusters: "0006, 0008, 0000", manufacturer: "Feibit Inc co.", model: "FB56+ZSW1JKJ2.7", deviceJoinName: "Nue 4 Gang Switch"
        
    attribute "lastCheckin", "string"
    attribute "switch", "string"
    attribute "switch1", "string"
    attribute "switch2", "string"
    attribute "switch3", "string"
    attribute "switch4", "string"
    command "on"
    command "off"
    command "on1"
    command "off1"
    command "on2"
    command "off2"
    command "on3"
    command "off3"
    command "on4"
    command "off4"
        
        attribute "switch","ENUM",["on","off"]
        attribute "switch1","ENUM",["on","off"]
        attribute "switch2","ENUM",["on","off"]
        attribute "switch3","ENUM",["on","off"]
        attribute "switch4","ENUM",["on","off"]
        attribute "switchstate","ENUM",["on","off"] 
    
    }

}


// Parse incoming device messages to generate events

def parse(String description) {
   log.debug "-------------------"
   log.debug "Parsing '${description}'"
   
//   def value = zigbee.parse(description)?.text
//   log.debug "Parse: $value"
   Map map = [:]
   
   if (description?.startsWith('catchall:')) {
		map = parseCatchAllMessage(description)
	}
	else if (description?.startsWith('read attr -')) {
		map = parseReportAttributeMessage(description)
	}
    else if (description?.startsWith('on/off: ')){
		log.debug "onoff"
		def model = device.getDataValue("model")
    	def noswitch = getSwitchCnt(model)
//		def childDevice
		def sstart = switchstart(noswitch)
		def sno
		def refreshCmds = []
        for (def i = 1; i <= noswitch; i++) {
			sno = Integer.toHexString(i+sstart).padLeft(2, "0")
 //           childDevice = childDevices.find{it.deviceNetworkId == "${device.deviceNetworkId}-${sno}"}
            refreshCmds += zigbee.readAttribute(0x0006, 0x0000, [destEndpoint: i+sstart])
		}
        return refreshCmds.collect { new physicalgraph.device.HubAction(it) }     
    }

	log.debug "Parse returned $map"
    //  send event for heartbeat    
    def now = new Date()
   
    sendEvent(name: "lastCheckin", value: now)
    
	def results = map ? createEvent(map) : null
	return results;
}

private Map parseCatchAllMessage(String description) {
	Map resultMap = [:]
	def cluster = zigbee.parse(description)
    log.debug cluster
	log.debug "parse Catch All"
    if (cluster.clusterId == 0x0006 && cluster.command == 0x01){
		log.debug "In the loop"
	    def sno = Integer.toHexString(cluster.sourceEndpoint).padLeft(2, "0")
	    def childDevice = childDevices.find{it.deviceNetworkId == "${device.deviceNetworkId}-${sno}"}
	    def onoff = cluster.data[-1]
        log.debug onoff
// important this is to see the child state before changing things around
		log.debug childDevice.currentValue("switch")
        if (onoff == 0)
    		childDevice.parse("off")
    	else if (onoff == 1)
    		childDevice.parse("on")
	}
        /*
    if (cluster.clusterId == 0x0006 && cluster.command == 0x0B){
    	if (cluster.sourceEndpoint == 0x0B)
        {
        log.debug "Its Switch one"
        log.debug onoff
        if (onoff == 1) {
//        	resultMap = createEvent(name: "switch1", value: "on")
            log.debug "Switch1 on"
         } else if (onoff == 0) {
 //           resultMap = createEvent(name: "switch1", value: "off")
            log.debug "Switch1 off" }
            }
            else if (cluster.sourceEndpoint == 0x0C)
            {
            log.debug "Its Switch two"
    	onoff = cluster.data[-1]
   //     if (onoff == 1)
   //     	resultMap = createEvent(name: "switch2", value: "on")
     //   else if (onoff == 0)
    //        resultMap = createEvent(name: "switch2", value: "off")
            }
    }
    */
	return resultMap
}

private Map parseReportAttributeMessage(String description) {
	Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}

	Map resultMap = [:]

	if (descMap.cluster == "0001" && descMap.attrId == "0020") {
    	resultMap = getBatteryResult(convertHexToInt(descMap.value / 2))
	} else if (descMap.cluster == "0008" && descMap.attrId == "0000") {
   	 	resultMap = createEvent(name: "switch", value: "off")
	} 
	return resultMap
}

def sendData(switchno, command) {
	log.debug "sendData()"
    log.debug "${device.deviceNetworkId} ${switchno},${command}"
    if (command == "off")
	    "st cmd 0x${device.deviceNetworkId} 0x${switchno} 0x0006 0x0 {}" 
    else if (command == "on")
	    "st cmd 0x${device.deviceNetworkId} 0x${switchno} 0x0006 0x1 {}" 
}

def on() {
	log.debug "on()"
	sendEvent(name: "switch", value: "on")
    def model = device.getDataValue("model")
    def noswitch = getSwitchCnt(model)
  	def childDevice
    def sno
    List<String> cmds = []
    def sstart = switchstart(noswitch)
	for (def i = 1; i <= noswitch; i++) {
        sno = Integer.toHexString(i+sstart).padLeft(2, "0")
	    childDevice = childDevices.find{it.deviceNetworkId == "${device.deviceNetworkId}-${sno}"}
	    childDevice.parse("off")
       	cmds.add("st cmd 0x${device.deviceNetworkId} 0x${sno} 0x0006 0x1 {}")
        if (i!=(noswitch)) cmds.add("delay 100")
    }
cmds
}

def off() {
	log.debug "off()"
	sendEvent(name: "switch", value: "off")
    def model = device.getDataValue("model")
    def noswitch = getSwitchCnt(model)
  	def childDevice
    def sno
    List<String> cmds = []
    def sstart = switchstart(noswitch)
	for (def i = 1; i <= noswitch; i++) {
        sno = Integer.toHexString(i+sstart).padLeft(2, "0")
	    childDevice = childDevices.find{it.deviceNetworkId == "${device.deviceNetworkId}-${sno}"}
	    childDevice.parse("off")
       	cmds.add("st cmd 0x${device.deviceNetworkId} 0x${sno} 0x0006 0x0 {}")
        if (i!=(noswitch)) cmds.add("delay 100")
    }
cmds
}


def refresh() {
	log.debug "refreshing"
	sendEvent(name: "switch", value: "off")
    def model = device.getDataValue("model")
    def noswitch = getSwitchCnt(model)
  	def childDevice
    def sno
    List<String> cmds = []
    def sstart = switchstart(noswitch)
	for (def i = 1; i <= noswitch; i++) {
        sno = Integer.toHexString(i+sstart).padLeft(2, "0")
	    childDevice = childDevices.find{it.deviceNetworkId == "${device.deviceNetworkId}-${sno}"}
	    childDevice.parse("off")
       	cmds.add("st rattr 0x${device.deviceNetworkId} 0x${sno} 0x0006 0x0 {}")
        if (i!=(noswitch)) cmds.add("delay 100")
    }
cmds
}

def installed() {
	log.debug "Executing 'installed()'"
	def noswitch = 0
	def childDevice
	def sno = 0
    
    def model = device.getDataValue("model")
    def startno = 10;
	
    noswitch = getSwitchCnt(model)
	switch (noswitch) {
         	case 2:
                startno = 10
                break
         	case 3:
                startno = 0
                break
         	case 4:
                startno = 15
                break
    }    
    
    for (def i = 1; i <= noswitch; i++) {
		log.debug "Beginning loop"
		log.debug i
        sno = Integer.toHexString(i+startno).padLeft(2, "0")
    
    	childDevice = childDevices.find{it.deviceNetworkId == "${device.deviceNetworkId}-${sno}"}
		log.debug childDevice
		if (childDevice == null) {
        	log.debug "isChild = true, but no child found - Auto Add it!"
			childDevice = createChildDevice(Integer.toString(i),sno)
		} else log.debug "Device exists"
	}
}

def uninstalled() {
	log.debug "Executing 'uninstalled()'"
}

def initialize() {
	log.debug "Executing 'initialize()'"
}

private getSwitchCnt(String model) {
	switch (model) {
         	case "LXN-2S27LX1.0":
                return(2)
                break
         	case "LXN-3S27LX1.0": 
                return(3)
                break                
			case "FB56+ZSW1JKJ2.7": 
                return(4)
                break
			default: 
                return(0)
    }

}

private createChildDevice(String deviceName, String sno) {
	log.debug "Creating Child Device"
    log.debug device.deviceNetworkId
    def deviceHandlerName = "Child Light Switch"
 
    if (deviceHandlerName != "") {
                return addChildDevice(deviceHandlerName, "${device.deviceNetworkId}-${sno}", null,
         			[completedSetup: true, label: "${device.displayName} (${deviceName})", 
                	isComponent: false])
    }
}

private switchstart(Integer noswitch) {
	switch (noswitch) {
         	case 2:
                return(10)
                break
         	case 3:
                return(0)
                break
         	case 4:
                return(15)
                break
            default:
            	return(0)
                breal
    }    
}