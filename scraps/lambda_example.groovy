private def doGetSleepNumber() {
  log.trace "doSetSleepNumber(${value})"
  def bodyJson = new groovy.json.JsonOutput().toJson([
  	session: [
    	application: [
      		applicationId: "amzn1.ask.skill.c00e06ef-23c5-494d-9eba-febd4343a3a9"
    	],
    	new: false
  	],
  	request: [
    	type: "IntentRequest",
    	timestamp: "0",
   		requestId: "foo",
    	locale: "en-US",
    	intent: [
      		name: "INFO",
      		confirmationStatus: "NONE",
      		slots: [
        		bed: "Master"
      		]
    	]
	  ]
	])
 
  try {
    def setParams = [
      uri: 'https://ofk4arc01m.execute-api.us-east-1.amazonaws.com',
      path: '/testSleepNumber/SleepNumber',
      //body: ['session', ['application', ['applicationId', 'amzn1.ask.skill.c00e06ef-23c5-494d-9eba-febd4343a3a9']]],
      body: bodyJson,
    ]
    state.session.side = 'R'
    //log.trace "session key " + state.session._k
    //log.trace "WE ARE HERE " + state.session.cookies + "PARAMS " + setParams
    httpPutJson(setParams) { response ->
      if (response.status == 200) {
        log.trace "doSetSleepNumber() Success - Request was successful: ($response.status) $response.data"
        doStatus(true)
      } else {
        log.trace "doSetSleepNumber() Failure - Request was unsuccessful: ($response.status) $response.data"
        state.session = null
        state.requestData = [:]
      }
    }
  } catch(Exception e) {
    if (alreadyLoggedIn) {
      log.error "doSetSleepNumber() Error ($e)"
    } else {
      log.trace "doSetSleepNumber() Error ($e)"   
    }
  }
}
