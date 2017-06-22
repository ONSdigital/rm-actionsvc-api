# Action Service API
This page documents the Action service API endpoints. These endpoints will be secured using HTTP basic authentication initially. All endpoints return an `HTTP 200 OK` status code except where noted otherwise.

## List Actions
* `GET /actions` will return a list of all actions, most recent first.

*Optional parameters:* `actionType` as an action type to filter the list by, `state` as an action state to filter the list by.

### Example JSON Response
```json
[
  {
    "id": "d24b3f17-bbf8-4c71-b2f0-a4334125d78d",
    "caseId": "7bc5d41b-0549-40b3-ba76-42f6d4cf3fdb",
    "actionPlanId": "5381731e-e386-41a1-8462-26373744db86",
    "actionRuleId": 120,
    "actionTypeName": "HouseholdCreateVisit",
    "createdBy": "SYSTEM",
    "manuallyCreated": false,
    "priority": 3,
    "situation": null,
    "state": "SUBMITTED",
    "createdDateTime": "2017-05-15T10:00:00Z",
    "updatedDateTime": "2017-05-15T10:00:00Z"
  }
]
```

An `HTTP 204 No Content` status code is returned if there are no actions.

## List Actions for Case
* `GET /actions/caseid/7bc5d41b-0549-40b3-ba76-42f6d4cf3fdb` will return a list of actions for the case with an ID of `7bc5d41b-0549-40b3-ba76-42f6d4cf3fdb`.

### Example JSON Response
```json
[
  {
    "id": "d24b3f17-bbf8-4c71-b2f0-a4334125d78d",
    "caseId": "7bc5d41b-0549-40b3-ba76-42f6d4cf3fdb",
    "actionPlanId": "5381731e-e386-41a1-8462-26373744db86",
    "actionRuleId": 120,
    "actionTypeName": "HouseholdCreateVisit",
    "createdBy": "SYSTEM",
    "manuallyCreated": false,
    "priority": 3,
    "situation": null,
    "state": "SUBMITTED",
    "createdDateTime": "2017-05-15T10:00:00Z",
    "updatedDateTime": "2017-05-15T10:00:00Z"
  }
]
```

An `HTTP 404 Not Found` status code is returned if the case with the specified ID could not be found. An `HTTP 204 No Content` status code is returned if there are no actions.

## Cancel Actions for Case
* `PUT /actions/caseid/7bc5d41b-0549-40b3-ba76-42f6d4cf3fdb` will cancel all actions for the case with an ID of `7bc5d41b-0549-40b3-ba76-42f6d4cf3fdb`.

### Example JSON Response
```json
[
  {
    "id": "d24b3f17-bbf8-4c71-b2f0-a4334125d78d",
    "caseId": "7bc5d41b-0549-40b3-ba76-42f6d4cf3fdb",
    "actionPlanId": "5381731e-e386-41a1-8462-26373744db86",
    "actionRuleId": 120,
    "actionTypeName": "HouseholdCreateVisit",
    "createdBy": "SYSTEM",
    "manuallyCreated": false,
    "priority": 3,
    "situation": null,
    "state": "SUBMITTED",
    "createdDateTime": "2017-05-15T10:00:00Z",
    "updatedDateTime": "2017-05-15T10:00:00Z"
  }
]
```

An `HTTP 404 Not Found` status code is returned if the case with the specified ID could not be found. An `HTTP 204 No Content` status code is returned if there are no actions to be cancelled.

## Get Action
* `GET /actions/d24b3f17-bbf8-4c71-b2f0-a4334125d78d` will return the details of the action with an ID of `d24b3f17-bbf8-4c71-b2f0-a4334125d78d`.

### Example JSON Response
```json
{
  "id": "d24b3f17-bbf8-4c71-b2f0-a4334125d78d",
  "caseId": "7bc5d41b-0549-40b3-ba76-42f6d4cf3fdb",
  "actionPlanId": "5381731e-e386-41a1-8462-26373744db86",
  "actionRuleId": 120,
  "actionTypeName": "HouseholdCreateVisit",
  "createdBy": "SYSTEM",
  "manuallyCreated": false,
  "priority": 3,
  "situation": null,
  "state": "SUBMITTED",
  "createdDateTime": "2017-05-15T10:00:00Z",
  "updatedDateTime": "2017-05-15T10:00:00Z"
}
```

An `HTTP 404 Not Found` status code is returned if the action with the specified ID could not be found.

## Create Action
* `POST /actions` will create an action.

**Required parameters**: `caseId` as the ID of the case the action is for, `actionTypeName` as the name of the action type the action is for and `createdBy` as the creator of the action.

*Optional parameters:* `priority` as the action priority (1 = highest, 5 = lowest) as passed to the remote handler.

### Example JSON Response
```json
{
  "id": "d24b3f17-bbf8-4c71-b2f0-a4334125d78d",
  "caseId": "7bc5d41b-0549-40b3-ba76-42f6d4cf3fdb",
  "actionPlanId": "5381731e-e386-41a1-8462-26373744db86",
  "actionRuleId": 120,
  "actionTypeName": "HouseholdCreateVisit",
  "createdBy": "SYSTEM",
  "manuallyCreated": false,
  "priority": 3,
  "situation": null,
  "state": "SUBMITTED",
  "createdDateTime": "2017-05-15T10:00:00Z",
  "updatedDateTime": "2017-05-15T10:00:00Z"
}
```

An `HTTP 201 Created` status code is returned if the action creation was a success. An `HTTP 400 Bad Request` is returned if any of the required parameters are missing.


## Update Action
* `PUT /actions/d24b3f17-bbf8-4c71-b2f0-a4334125d78d` will update the details of the action with an ID of `d24b3f17-bbf8-4c71-b2f0-a4334125d78d`.

*Optional parameters:* `priority` as the action priority (1 = highest, 5 = lowest) as passed to the remote handler, `situation` as the action status as recorded by the remote handler.

### Example JSON Response
```json
{
  "actionId": "d24b3f17-bbf8-4c71-b2f0-a4334125d78d",
  "caseId": "7bc5d41b-0549-40b3-ba76-42f6d4cf3fdb",
  "actionPlanId": "5381731e-e386-41a1-8462-26373744db86",
  "actionRuleId": 120,
  "actionTypeName": "HouseholdCreateVisit",
  "createdBy": "SYSTEM",
  "manuallyCreated": false,
  "priority": 1,
  "situation": null,
  "state": "SUBMITTED",
  "createdDateTime": "2017-05-15T10:00:00Z",
  "updatedDateTime": "2017-05-15T10:00:00Z"
}
```

An `HTTP 404 Not Found` status code is returned if the action with the specified ID could not be found. An `HTTP 400 Bad Request` status code is returned if any of the parameters are invalid.

## Update Action Feedback
* `PUT /actions/d24b3f17-bbf8-4c71-b2f0-a4334125d78d/feedback` will transition the state of the action with an ID of `d24b3f17-bbf8-4c71-b2f0-a4334125d78d`.

**Required parameters**: `situation` as the action status as recorded by the remote handler, `outcome` as the outcome of the action within the context of the remote handler.

### Example JSON Response
```json
{
  "id": "d24b3f17-bbf8-4c71-b2f0-a4334125d78d",
  "caseId": "7bc5d41b-0549-40b3-ba76-42f6d4cf3fdb",
  "actionPlanId": "5381731e-e386-41a1-8462-26373744db86",
  "actionRuleId": 120,
  "actionTypeName": "HouseholdCreateVisit",
  "createdBy": "SYSTEM",
  "manuallyCreated": false,
  "priority": 1,
  "situation": null,
  "state": "SUBMITTED",
  "createdDateTime": "2017-05-15T10:00:00Z",
  "updatedDateTime": "2017-05-15T10:00:00Z"
}
```

An `HTTP 404 Not Found` status code is returned if the action with the specified ID could not be found. An `HTTP 400 Bad Request` status code is returned if any of the parameters are invalid.

## List Action Plans
* `GET /actionplans` will return a list of all action plans, most recent first.

### Example JSON Response
```json
[
  {
    "id": "5381731e-e386-41a1-8462-26373744db86",
    "name": "C1O331D10E",
    "description": "Component 1 - England/online/field day ten/three reminders",
    "createdBy": "SYSTEM",
    "lastGoodRunDateTime": "2017-06-15T10:00:00Z"
  }
]
```

An `HTTP 204 No Content` status code is returned if there are no action plans.

## Get Action Plan
* `GET /actionplans/5381731e-e386-41a1-8462-26373744db86` will return the details of the action plan with an ID of `5381731e-e386-41a1-8462-26373744db86`.

### Example JSON Response
```json
{
  "id": "5381731e-e386-41a1-8462-26373744db86",
  "name": "C1O331D10E",
  "description": "Component 1 - England/online/field day ten/three reminders",
  "createdBy": "SYSTEM",
  "lastGoodRunDateTime": "2017-06-15T10:00:00Z"
}
```

An `HTTP 404 Not Found` status code is returned if the action plan with the specified ID could not be found.

## Update Action Plan
* `PUT /actionplans/5381731e-e386-41a1-8462-26373744db86` will update the details of the action plan with an ID of `5381731e-e386-41a1-8462-26373744db86`.

*Optional parameters:* `description` as the action plan description, `lastGoodRunDateTime` as the date/time the action plan was last successfully run.

### Example JSON Response
```json
{
  "id": "5381731e-e386-41a1-8462-26373744db86",
  "name": "C1O331D10E",
  "description": "Component 1 - England/online/field day ten/three reminders",
  "createdBy": "SYSTEM",
  "lastGoodRunDateTime": "2017-06-15T10:00:00Z"
}
```

An `HTTP 404 Not Found` status code is returned if the action plan with the specified ID could not be found. An `HTTP 400 Bad Request` status code is returned if any of the parameters are invalid.

## List Action Plan Jobs
* `GET /actionplans/5381731e-e386-41a1-8462-26373744db86/jobs` will return a list of action plan jobs (most recent first) for the action plan with an ID of `5381731e-e386-41a1-8462-26373744db86`.

### Example JSON Response
```json
[
  {
    "id": "714356ba-7236-4179-8007-f09190eed323",
    "actionPlanId": "5381731e-e386-41a1-8462-26373744db86",
    "createdBy": "SYSTEM",
    "state": "SUBMITTED",
    "createdDateTime": "2017-05-15T10:00:00Z",
    "updatedDateTime": "2017-05-15T10:00:00Z"
  }
]
```

An `HTTP 404 Not Found` status code is returned if the action plan with the specified ID could not be found. An `HTTP 204 No Content` status code is returned if there are no action plan jobs for the action plan.

## Get Action Plan Job
* `GET /actionplans/jobs/714356ba-7236-4179-8007-f09190eed323` will return the details of the action plan job with an ID of `714356ba-7236-4179-8007-f09190eed323`.

### Example JSON Response
```json
{
  "id": "714356ba-7236-4179-8007-f09190eed323",
  "actionPlanId": "5381731e-e386-41a1-8462-26373744db86",
  "createdBy": "SYSTEM",
  "state": "SUBMITTED",
  "createdDateTime": "2017-05-15T10:00:00Z",
  "updatedDateTime": "2017-05-15T10:00:00Z"
}
```

An `HTTP 404 Not Found` status code is returned if the action plan job with the specified ID could not be found.

## Create Action Plan Job
* `POST /actionplans/5381731e-e386-41a1-8462-26373744db86/jobs` will create an action plan job (i.e. execute the action plan) for the action plan with an ID of `5381731e-e386-41a1-8462-26373744db86`.

**Required parameters**: `createdBy` as the creator of the action plan job.

### Example JSON Response
```json
{
  "id": "714356ba-7236-4179-8007-f09190eed323",
  "actionPlanId": "5381731e-e386-41a1-8462-26373744db86",
  "createdBy": "SYSTEM",
  "state": "SUBMITTED",
  "createdDateTime": "2017-05-15T10:00:00Z",
  "updatedDateTime": "2017-05-15T10:00:00Z"
}
```

An `HTTP 201 Created` status code is returned if the action plan job creation was a success. An `HTTP 404 Not Found` status code is returned if the action plan with the specified ID could not be found. An `HTTP 400 Bad Request` is returned if any of the required parameters are missing.
